package cc.winboll.studio.winboll.media;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Describe Hosted-mode native MIDI engine (MIDI bridge spec v1.2 §4/§10 phase-2).
 * Downloads the .mid files referenced by the playlist into the app cache and
 * plays them with the framework MediaPlayer (built-in Sonivox/EAS MIDI
 * synthesis). Per spec D-5 the browser plays with its own MIDI tool strategy;
 * timbre/behavior do not need to match the web synthesizer. Java 7 syntax,
 * zero external dependencies.
 */

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

public class HostedMidiPlayer {

    public static final String TAG = "HostedMidiPlayer";

    // Playback states, aligned with android.media.session.PlaybackState values.
    public static final int STATE_STOPPED = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_PLAYING = 3;

    /** One playable entry of the bridge playlist. */
    public static class Track {
        public final String id;
        public final String title;
        public final String url;
        public final long durationMs;

        public Track(String id, String title, String url, long durationMs) {
            this.id = id;
            this.title = title;
            this.url = url;
            this.durationMs = durationMs;
        }
    }

    /** Engine callbacks, always delivered on the main thread. */
    public interface Listener {
        /** Prepare finished and (optional) playback started for a track. */
        void onTrackReady(String trackId, String title, long durationMs);

        /** Fired on every explicit state change (play/pause/stop/seek/complete). */
        void onStateChanged(int state, long positionMs, long durationMs);

        /** Track reached its end (non-looping). */
        void onTrackEnded();

        /** Download/prepare/playback failure. The caller should unwind hosting. */
        void onError(String message);
    }

    private final Context mAppContext;
    private final Listener mListener;
    private final Handler mMain = new Handler(Looper.getMainLooper());

    private MediaPlayer mPlayer;
    private Track mTrack;
    private int mState = STATE_STOPPED;
    private float mRate = 1.0f;
    private boolean mLoop = false;
    private long mPendingSeekMs = -1;
    /**
     * Pause intent that survives the async prepare window. Set by
     * playTrack(startPaused=true) and by pause() when the player is still
     * preparing; honoured in onPrepared (which then parks in PAUSED instead
     * of starting). Cleared by play()/stopInternal().
     */
    private boolean mPausePending = false;
    /** True between playTrack() and onPrepared/notifyError. The engine state
     *  reads STOPPED during this window, but a STOPPED push would tear the
     *  media session down mid-takeover (MediaPlaybackService treats stopped
     *  as terminal), so MidiBridge suppresses state pushes while preparing. */
    private boolean mPreparing = false;
    private boolean mReleased = false;

    public HostedMidiPlayer(Context context, Listener listener) {
        mAppContext = context.getApplicationContext();
        mListener = listener;
    }

    // ------------------- public API (main thread) -------------------

    /**
     * Download (cached) and play the given track. Replaces any current track.
     * startPosMs > 0 seeks after prepare; rate/loop are applied best-effort.
     */
    public void playTrack(Track track, long startPosMs, float rate, boolean loop) {
        playTrack(track, startPosMs, rate, loop, false);
    }

    /**
     * Download (cached) and load the given track, optionally starting paused
     * (spec D-6 / bridge v1.2: hosting takeover and process-death recovery
     * must respect the web playback state — a paused session restores parked
     * at the handoff position instead of auto-playing).
     */
    public void playTrack(final Track track, final long startPosMs, final float rate,
                          final boolean loop, final boolean startPaused) {
        if (mReleased || track == null) {
            return;
        }
        stopInternal(false);
        mTrack = track;
        mRate = rate > 0 ? rate : 1.0f;
        mLoop = loop;
        mPendingSeekMs = startPosMs > 0 ? startPosMs : -1;
        mPausePending = startPaused;
        mPreparing = true;
        final File cacheFile = cacheFileFor(track.url);
        new Thread(new Runnable() {
            @Override
            public void run() {
                File local = cacheFile;
                if (!local.exists()) {
                    local = download(track.url, cacheFile);
                }
                if (local == null || !local.exists()) {
                    notifyError("download failed: " + track.url);
                    return;
                }
                final File ready = local;
                mMain.post(new Runnable() {
                    @Override
                    public void run() {
                        prepareAndPlay(ready);
                    }
                });
            }
        }, "midi-dl").start();
    }

    /** Resume after pause (also cancels a pending prepare-time pause). */
    public void play() {
        mPausePending = false;
        if (mPlayer != null && mState == STATE_PAUSED) {
            try {
                mPlayer.start();
                mState = STATE_PLAYING;
                notifyState();
            } catch (IllegalStateException e) {
                notifyError("play failed: " + e.getMessage());
            }
        }
    }

    /** Pause and keep the position. Safe to call while still preparing. */
    public void pause() {
        mPausePending = true;
        if (mPlayer != null && mState == STATE_PLAYING) {
            try {
                mPlayer.pause();
            } catch (IllegalStateException e) {
                // Already stopped underneath; fall through to state update.
            }
            mState = STATE_PAUSED;
            notifyState();
        }
    }

    /** Stop playback and release the player (track selection is kept). */
    public void stop() {
        stopInternal(true);
    }

    /** Seek within the current track (playing continues after seek). */
    public void seekTo(long positionMs) {
        long clamped = Math.max(0, positionMs);
        if (mPlayer != null && (mState == STATE_PLAYING || mState == STATE_PAUSED)) {
            try {
                mPlayer.seekTo((int) clamped);
                notifyState();
            } catch (IllegalStateException e) {
                mPendingSeekMs = clamped;
            }
        } else {
            mPendingSeekMs = clamped;
        }
    }

    /** Playback rate (tempo multiplier), best-effort on the framework synth. */
    public void setRate(float rate) {
        mRate = rate > 0 ? rate : 1.0f;
        applyRate();
    }

    public void setLooping(boolean loop) {
        mLoop = loop;
        if (mPlayer != null) {
            try {
                mPlayer.setLooping(loop);
            } catch (IllegalStateException e) {
                // Ignore: player not in a settable state.
            }
        }
    }

    public int getState() {
        return mState;
    }

    /** True while a track is downloading/preparing (state not yet meaningful). */
    public boolean isPreparing() {
        return mPreparing;
    }

    public long getPositionMs() {
        if (mPlayer != null && (mState == STATE_PLAYING || mState == STATE_PAUSED)) {
            try {
                return mPlayer.getCurrentPosition();
            } catch (IllegalStateException e) {
                return 0;
            }
        }
        return 0;
    }

    public long getDurationMs() {
        if (mPlayer != null) {
            try {
                int d = mPlayer.getDuration();
                if (d > 0) {
                    return d;
                }
            } catch (IllegalStateException e) {
                // Fall through to the playlist value.
            }
        }
        return mTrack != null ? mTrack.durationMs : 0;
    }

    public Track getTrack() {
        return mTrack;
    }

    public float getRate() {
        return mRate;
    }

    public boolean isLooping() {
        return mLoop;
    }

    /** Release all resources. The player cannot be reused afterwards. */
    public void release() {
        mReleased = true;
        stopInternal(true);
    }

    // ------------------- internals (main thread unless noted) -------------------

    private void prepareAndPlay(File local) {
        if (mReleased) {
            return;
        }
        releasePlayer();
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
            mPlayer.setDataSource(local.getAbsolutePath());
            mPlayer.setLooping(mLoop);
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (mReleased || mp != mPlayer) {
                        return;
                    }
                    mPreparing = false;
                    if (mPendingSeekMs > 0) {
                        // Clamp: the web clock position can exceed the engine
                        // track length; seeking past the end makes MediaPlayer
                        // fire onCompletion immediately (which pushed STOPPED
                        // and tore the media session down mid-takeover).
                        int target = (int) mPendingSeekMs;
                        try {
                            int dur = mp.getDuration();
                            if (dur > 0 && target > dur - 250) {
                                target = Math.max(0, dur - 250);
                            }
                        } catch (IllegalStateException e) {
                            // Keep the raw target below.
                        }
                        try {
                            mp.seekTo(target);
                        } catch (IllegalStateException e) {
                            // Ignore: start from the beginning instead.
                        }
                        mPendingSeekMs = -1;
                    }
                    applyRate();
                    if (mPausePending) {
                        // Pause intent arrived while preparing (startPaused
                        // restore / pause before prepare finished): park in
                        // PAUSED without starting so no audio blips out.
                        mState = STATE_PAUSED;
                        if (mListener != null) {
                            mListener.onTrackReady(mTrack.id, mTrack.title, getDurationMs());
                        }
                        notifyState();
                        return;
                    }
                    try {
                        mp.start();
                        mState = STATE_PLAYING;
                    } catch (IllegalStateException e) {
                        notifyError("start failed: " + e.getMessage());
                        return;
                    }
                    if (mListener != null) {
                        mListener.onTrackReady(mTrack.id, mTrack.title, getDurationMs());
                    }
                    notifyState();
                }
            });
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mReleased || mp != mPlayer) {
                        return;
                    }
                    // A completion without active playback (e.g. seek past the
                    // end while parked paused by mPausePending) is not a real
                    // track end and must not report STOPPED.
                    if (mState != STATE_PLAYING) {
                        return;
                    }
                    if (!mLoop) {
                        mState = STATE_STOPPED;
                        notifyState();
                        if (mListener != null) {
                            mListener.onTrackEnded();
                        }
                    }
                }
            });
            mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (mReleased) {
                        return true;
                    }
                    notifyError("MediaPlayer error what=" + what + " extra=" + extra);
                    return true;
                }
            });
            mPlayer.prepareAsync();
        } catch (IOException e) {
            notifyError("prepare failed: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            notifyError("bad data source: " + e.getMessage());
        } catch (IllegalStateException e) {
            notifyError("player state error: " + e.getMessage());
        }
    }

    private void applyRate() {
        if (mPlayer == null || Build.VERSION.SDK_INT < 23) {
            return;
        }
        try {
            mPlayer.setPlaybackParams(new PlaybackParams().setSpeed(mRate));
        } catch (IllegalStateException e) {
            // Best-effort (spec D-5): rate is a reference parameter only.
        } catch (IllegalArgumentException e) {
            // Device synth may not support rate changes; ignore.
        }
    }

    private void stopInternal(boolean notify) {
        releasePlayer();
        mPausePending = false;
        mPreparing = false;
        mState = STATE_STOPPED;
        if (notify) {
            notifyState();
        }
    }

    private void releasePlayer() {
        if (mPlayer != null) {
            try {
                mPlayer.stop();
            } catch (IllegalStateException e) {
                // Not started yet; release below is enough.
            }
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void notifyState() {
        if (mListener == null) {
            return;
        }
        final long pos = getPositionMs();
        final long dur = getDurationMs();
        mMain.post(new Runnable() {
            @Override
            public void run() {
                if (!mReleased && mListener != null) {
                    mListener.onStateChanged(mState, pos, dur);
                }
            }
        });
    }

    private void notifyError(final String message) {
        Log.w(TAG, message);
        mPreparing = false;
        mMain.post(new Runnable() {
            @Override
            public void run() {
                if (!mReleased && mListener != null) {
                    mListener.onError(message);
                }
            }
        });
    }

    // ------------------- download / cache (background thread) -------------------

    private File cacheFileFor(String url) {
        File dir = new File(mAppContext.getCacheDir(), "midi_bridge");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, md5(url) + ".mid");
    }

    private File download(String urlStr, File target) {
        File tmp = new File(target.getAbsolutePath() + ".part");
        HttpURLConnection conn = null;
        InputStream in = null;
        FileOutputStream out = null;
        try {
            // Manual redirect following: HttpURLConnection never crosses
            // protocols on its own (http -> https 301 is dropped), and the
            // real sites redirect plain-http asset URLs to https. Follow up
            // to 5 hops ourselves via the Location header.
            String current = urlStr;
            int code = -1;
            for (int hop = 0; hop < 5; hop++) {
                URL url = new URL(current);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                conn.setInstanceFollowRedirects(false);
                code = conn.getResponseCode();
                if (code == 301 || code == 302 || code == 303
                        || code == 307 || code == 308) {
                    String loc = conn.getHeaderField("Location");
                    conn.disconnect();
                    conn = null;
                    if (loc == null || loc.length() == 0) {
                        Log.w(TAG, "download redirect without Location for " + current);
                        return null;
                    }
                    current = new URL(url, loc).toString();
                    continue;
                }
                break;
            }
            if (conn == null || code != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "download HTTP " + code + " for " + current);
                return null;
            }
            in = conn.getInputStream();
            out = new FileOutputStream(tmp);
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            out.close();
            out = null;
            if (!tmp.renameTo(target)) {
                target.delete();
                if (!tmp.renameTo(target)) {
                    return null;
                }
            }
            return target;
        } catch (IOException e) {
            Log.w(TAG, "download failed: " + e.getMessage());
            tmp.delete();
            return null;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // Ignore close failure.
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Ignore close failure.
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String md5(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(bytes[i] & 0xFF);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            // Digest/encoding always available; fall back to a sanitized hash.
            return String.valueOf(Math.abs(input.hashCode()));
        }
    }
}
