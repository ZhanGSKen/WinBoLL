package cc.winboll.studio.winboll.services;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Describe Foreground media playback service for web page audio (Java 7 syntax).
 * Registers the app as a music player via MediaBrowserService + MediaSession,
 * and shows a persistent media notification with transport controls while the
 * WWWBrowser WebView (or the hosted native MIDI engine) is playing audio.
 * Zero external dependencies: uses framework android.media.session.MediaSession
 * and Notification.MediaStyle only (no androidx.media / support libs).
 *
 * MIDI bridge spec v1.2 (§6): the session carries real position/duration/rate
 * and handles seek (+ next/previous while hosted). Callbacks route to the
 * WebView owner (protocol pages -> __midiControl, heuristic pages -> injected
 * JS) or straight to the native engine in cover-hosting mode.
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.service.media.MediaBrowserService;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;

import java.util.Collections;
import java.util.List;

import cc.winboll.studio.winboll.MainActivity;
import cc.winboll.studio.winboll.R;
import cc.winboll.studio.winboll.media.MidiBridge;

public class MediaPlaybackService extends MediaBrowserService {

    public static final String TAG = "MediaPlaybackService";

    // Service actions
    public static final String ACTION_STATE_CHANGED = "cc.winboll.studio.winboll.action.MEDIA_STATE_CHANGED";
    public static final String ACTION_PLAY = "cc.winboll.studio.winboll.action.MEDIA_PLAY";
    public static final String ACTION_PAUSE = "cc.winboll.studio.winboll.action.MEDIA_PAUSE";
    public static final String ACTION_STOP = "cc.winboll.studio.winboll.action.MEDIA_STOP";
    public static final String ACTION_PREV = "cc.winboll.studio.winboll.action.MEDIA_PREV";
    public static final String ACTION_NEXT = "cc.winboll.studio.winboll.action.MEDIA_NEXT";

    // Intent extras
    public static final String EXTRA_PLAYING = "playing";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_ARTIST = "artist";
    public static final String EXTRA_STATE = "state";
    public static final String EXTRA_POSITION = "position";
    public static final String EXTRA_DURATION = "duration";
    public static final String EXTRA_RATE = "rate";
    public static final String EXTRA_HOSTED = "hosted";

    // Playback states (aligned with PlaybackState constants).
    public static final int PLAY_STATE_STOPPED = 1;
    public static final int PLAY_STATE_PAUSED = 2;
    public static final int PLAY_STATE_PLAYING = 3;

    // MediaSession / notification identifiers
    public static final String MEDIA_SESSION_TAG = "WinBoLLWebAudio";
    private static final String MEDIA_BROWSER_ROOT_ID = "winboll_webaudio_root";
    private static final String NOTIFICATION_CHANNEL_ID = "winboll_media_playback";
    private static final int NOTIFICATION_ID = 0x5742;

    /** Controller bridge to the WebView owner (WWWBrowserFragment). */
    public interface PlaybackController {
        void onPlayRequested();

        void onPauseRequested();

        void onStopRequested();

        void onSeekRequested(long positionMs);

        void onSkipToNextRequested();

        void onSkipToPreviousRequested();
    }

    private static volatile PlaybackController sController;

    /** Register the WebView-side controller (single browser instance). */
    public static void setPlaybackController(PlaybackController controller) {
        sController = controller;
    }

    /**
     * Push a full playback snapshot into the service (MIDI bridge spec §6).
     * Starts the service in foreground when playing; updates or dismisses the
     * notification otherwise.
     */
    public static void pushState(Context context, int playState, long positionMs,
                                 long durationMs, float rate, String title, String artist,
                                 boolean hosted) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, MediaPlaybackService.class);
        intent.setAction(ACTION_STATE_CHANGED);
        intent.putExtra(EXTRA_STATE, playState);
        intent.putExtra(EXTRA_POSITION, positionMs);
        intent.putExtra(EXTRA_DURATION, durationMs);
        intent.putExtra(EXTRA_RATE, rate);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_ARTIST, artist);
        intent.putExtra(EXTRA_HOSTED, hosted);
        if (playState == PLAY_STATE_PLAYING) {
            // Must use startForegroundService: app may be in background when audio starts.
            context.startForegroundService(intent);
        } else {
            try {
                context.startService(intent);
            } catch (IllegalStateException e) {
                // Background start blocked and service not running: nothing to update.
            }
        }
    }

    /**
     * Legacy boolean snapshot used by the heuristic web-audio poller.
     * false maps to PAUSED (bar stays with a play button, as before).
     */
    public static void pushState(Context context, boolean playing, String title) {
        pushState(context, playing ? PLAY_STATE_PLAYING : PLAY_STATE_PAUSED,
            0, 0, 1.0f, title, "WinBoLL+ WWWBrowser", false);
    }

    /** Stop playback service and remove the notification. */
    public static void stopService(Context context) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, MediaPlaybackService.class);
        intent.setAction(ACTION_STOP);
        try {
            context.startService(intent);
        } catch (IllegalStateException e) {
            // Service not running: ignore.
        }
    }

    private MediaSession mSession;
    private int mPlayState = PLAY_STATE_STOPPED;
    private boolean mHosted = false;
    private long mPositionMs = 0;
    private long mDurationMs = 0;
    private float mRate = 1.0f;
    // True once this service instance has served actual playback. Used to avoid
    // resurrecting the notification after an explicit stop: the WebView poller
    // may deliver one final "stopped" update which would restart a fresh service
    // instance; a never-playing instance dismisses itself silently.
    private boolean mEverPlaying = false;
    private String mTitle = "WinBoLL+ 网页音频";
    private String mArtist = "WinBoLL+ WWWBrowser";

    // --- Seekbar drag smoothing (bridge spec §6 ACTION_SEEK_TO) ---
    // The system seekbar fires onSeekTo continuously while the thumb is being
    // dragged. Running a real seek (engine prepare / web JS reschedule) per
    // tick is heavy and each resulting state push used to rebuild the whole
    // notification, which resets the SystemUI thumb mid-drag (jerky scrubbing).
    // Instead: track the thumb position instantly and coalesce the real seek
    // to one dispatch after the drag settles (SEEK_SETTLE_MS of quiet).
    private static final long SEEK_SETTLE_MS = 180;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private long mPendingSeekPos = -1;
    private boolean mSeekPending = false;
    private final Runnable mSeekDispatch = new Runnable() {
        @Override
        public void run() {
            mSeekPending = false;
            mScrubbing = false;
            long pos = mPendingSeekPos;
            mPendingSeekPos = -1;
            PlaybackController controller = sController;
            if (controller != null) {
                controller.onSeekRequested(pos);
            } else {
                MidiBridge.onMediaSeekCommand(pos);
            }
        }
    };
    // While a drag is in progress, ignore position overwrites from heartbeat
    // state pushes so the thumb does not fight the finger.
    private volatile boolean mScrubbing = false;

    // Notification rebuild dedup: only re-post when visible content changes
    // (play/pause icon, title, hosted text). Position-only updates go through
    // PlaybackState alone (the session moves the seekbar smoothly by itself).
    private int mPostedState = 0;
    private String mPostedTitle = null;
    private boolean mPostedHosted = false;
    private String mPostedText = null;
    // Metadata rebuild dedup (see updatePlaybackState).
    private String mMetaTitle = null;
    private String mMetaArtist = null;
    private long mMetaDuration = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        MidiBridge.init(this);
        createNotificationChannel();
        mSession = new MediaSession(this, MEDIA_SESSION_TAG);
        mSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                dispatchPlay();
            }

            @Override
            public void onPause() {
                dispatchPause();
            }

            @Override
            public void onStop() {
                dispatchStop();
            }

            @Override
            public void onSeekTo(long pos) {
                dispatchSeek(pos);
            }

            @Override
            public void onSkipToNext() {
                dispatchSkip(true);
            }

            @Override
            public void onSkipToPrevious() {
                dispatchSkip(false);
            }
        });
        // Open the browser UI when the notification body is tapped.
        Intent openIntent = new Intent(this, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        mSession.setSessionActivity(PendingIntent.getActivity(this, 0, openIntent, flags));
        mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS
            | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mSession.setActive(true);
        setSessionToken(mSession.getSessionToken());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        if (action == null) {
            // Sticky restart after process death: restore hosted MIDI playback
            // from the persisted snapshot (bridge spec §3.1 recovery), or stop.
            if (MidiBridge.restoreHostedPlayback(this)) {
                mHosted = true;
                mEverPlaying = true;
                mPlayState = PLAY_STATE_PLAYING;
                mTitle = "WinBoLL MIDI 播放器";
                mArtist = "WinBoLL MIDI 播放器";
                startForeground(NOTIFICATION_ID, buildNotification());
                markNotificationPosted();
                updatePlaybackState();
                return START_STICKY;
            }
            stopSelf();
            return START_NOT_STICKY;
        }
        if (ACTION_PLAY.equals(action)) {
            dispatchPlay();
        } else if (ACTION_PAUSE.equals(action)) {
            dispatchPause();
        } else if (ACTION_STOP.equals(action)) {
            dispatchStop();
        } else if (ACTION_PREV.equals(action)) {
            dispatchSkip(false);
        } else if (ACTION_NEXT.equals(action)) {
            dispatchSkip(true);
        } else if (ACTION_STATE_CHANGED.equals(action)) {
            if (intent != null) {
                if (intent.hasExtra(EXTRA_STATE)) {
                    mPlayState = intent.getIntExtra(EXTRA_STATE, PLAY_STATE_STOPPED);
                } else {
                    mPlayState = intent.getBooleanExtra(EXTRA_PLAYING, false)
                        ? PLAY_STATE_PLAYING : PLAY_STATE_PAUSED;
                }
                // While the user is scrubbing the notification seekbar, do not
                // let heartbeat/poll positions overwrite the thumb location.
                long newPos = intent.getLongExtra(EXTRA_POSITION, 0);
                if (!mScrubbing) {
                    mPositionMs = newPos;
                }
                mDurationMs = intent.getLongExtra(EXTRA_DURATION, 0);
                mRate = intent.getFloatExtra(EXTRA_RATE, 1.0f);
                mHosted = intent.getBooleanExtra(EXTRA_HOSTED, false);
                String title = intent.getStringExtra(EXTRA_TITLE);
                if (title != null && title.length() > 0) {
                    mTitle = title;
                }
                String artist = intent.getStringExtra(EXTRA_ARTIST);
                if (artist != null && artist.length() > 0) {
                    mArtist = artist;
                }
            }
            if (mPlayState == PLAY_STATE_PLAYING) {
                mEverPlaying = true;
                postNotificationIfChanged(true);
            } else if (!mEverPlaying) {
                // Fresh instance receiving only a non-playing update (post-stop
                // poll update): dismiss instead of resurrecting a notification.
                stopSelf();
                return START_NOT_STICKY;
            } else if (mPlayState == PLAY_STATE_PAUSED) {
                // Paused: keep the bar visible (play button) while service alive;
                // actual dismissal happens on explicit stop.
                postNotificationIfChanged(false);
            } else {
                // Stopped (bridge spec §6): deactivate the session and dismiss.
                updatePlaybackState();
                if (mSession != null) {
                    mSession.setActive(false);
                }
                stopForeground(true);
                stopSelf();
                return START_NOT_STICKY;
            }
            updatePlaybackState();
        }
        return START_STICKY;
    }

    /** Media button / notification play: resume audio (engine or web page). */
    private void dispatchPlay() {
        mPlayState = PLAY_STATE_PLAYING;
        mEverPlaying = true;
        PlaybackController controller = sController;
        if (controller != null) {
            controller.onPlayRequested();
        } else {
            MidiBridge.onMediaPlayCommand();
        }
        startForeground(NOTIFICATION_ID, buildNotification());
        markNotificationPosted();
        updatePlaybackState();
    }

    /** Media button / notification pause: suspend audio (bar stays). */
    private void dispatchPause() {
        mPlayState = PLAY_STATE_PAUSED;
        PlaybackController controller = sController;
        if (controller != null) {
            controller.onPauseRequested();
        } else {
            MidiBridge.onMediaPauseCommand();
        }
        NotificationManager nm = getNotificationManager();
        if (nm != null) {
            nm.notify(NOTIFICATION_ID, buildNotification());
        }
        markNotificationPosted();
        updatePlaybackState();
    }

    /** Media button / notification stop: stop audio and dismiss the bar. */
    private void dispatchStop() {
        mPlayState = PLAY_STATE_STOPPED;
        PlaybackController controller = sController;
        if (controller != null) {
            controller.onStopRequested();
        } else {
            MidiBridge.onMediaStopCommand();
        }
        updatePlaybackState();
        stopForeground(true);
        stopSelf();
    }

    /** Seek from the system media controls (bridge spec §6 ACTION_SEEK_TO). */
    private void dispatchSeek(long positionMs) {
        // Thumb tracking: update the session position instantly (smooth drag)
        // but only dispatch the real seek after the drag settles.
        mScrubbing = true;
        mPositionMs = Math.max(0, positionMs);
        updatePlaybackState();
        mPendingSeekPos = mPositionMs;
        mSeekPending = true;
        mMainHandler.removeCallbacks(mSeekDispatch);
        mMainHandler.postDelayed(mSeekDispatch, SEEK_SETTLE_MS);
    }

    /** Skip to next/previous track (hosted mode only, bridge spec §6). */
    private void dispatchSkip(boolean next) {
        PlaybackController controller = sController;
        boolean handled = false;
        if (controller != null) {
            if (next) {
                controller.onSkipToNextRequested();
            } else {
                controller.onSkipToPreviousRequested();
            }
            handled = true;
        } else {
            handled = MidiBridge.onMediaSkipCommand(next);
        }
        if (handled) {
            updatePlaybackState();
        }
    }

    private void updatePlaybackState() {
        if (mSession == null) {
            return;
        }
        int state;
        if (mPlayState == PLAY_STATE_PLAYING) {
            state = PlaybackState.STATE_PLAYING;
        } else if (mPlayState == PLAY_STATE_PAUSED) {
            state = PlaybackState.STATE_PAUSED;
        } else {
            state = PlaybackState.STATE_STOPPED;
        }
        long actions = PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE
            | PlaybackState.ACTION_STOP | PlaybackState.ACTION_SEEK_TO
            | PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS;
        mSession.setPlaybackState(new PlaybackState.Builder()
            .setState(state, mPositionMs, mRate)
            .setActions(actions)
            .build());
        // Metadata rebinds the SystemUI media view; only push when it changed
        // so scrub-time PlaybackState updates stay cheap and smooth.
        if (!strEq(mTitle, mMetaTitle) || !strEq(mArtist, mMetaArtist)
                || mDurationMs != mMetaDuration) {
            mMetaTitle = mTitle;
            mMetaArtist = mArtist;
            mMetaDuration = mDurationMs;
            MediaMetadata.Builder meta = new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, mTitle)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, mArtist);
            if (mDurationMs > 0) {
                meta.putLong(MediaMetadata.METADATA_KEY_DURATION, mDurationMs);
            }
            mSession.setMetadata(meta.build());
        }
        // Bridge spec §6: non-hosted sessions deactivate on stop (bar removed);
        // hosted sessions stay active while the native engine owns playback.
        mSession.setActive(mPlayState != PLAY_STATE_STOPPED || mHosted);
    }

    /**
     * Re-post the notification only when its visible content changed
     * (play/pause icon, title, hosted text). Position-only updates — heartbeat
     * pushes and seekbar scrubbing — must NOT rebuild the notification: each
     * rebuild makes SystemUI re-anchor the seekbar thumb and the drag stutters.
     */
    private void postNotificationIfChanged(boolean foreground) {
        String text = mPlayState == PLAY_STATE_PLAYING
            ? (mHosted ? "MIDI 播放托管中（原生合成）" : "网页音频播放中（后台持续播放）")
            : "播放已暂停";
        if (mPlayState == mPostedState && mHosted == mPostedHosted
                && strEq(mTitle, mPostedTitle) && strEq(text, mPostedText)) {
            return;
        }
        mPostedState = mPlayState;
        mPostedHosted = mHosted;
        mPostedTitle = mTitle;
        mPostedText = text;
        if (foreground && mPlayState == PLAY_STATE_PLAYING) {
            startForeground(NOTIFICATION_ID, buildNotification());
        } else {
            NotificationManager nm = getNotificationManager();
            if (nm != null) {
                nm.notify(NOTIFICATION_ID, buildNotification());
            }
        }
    }

    /** Sync the dedup cache after a direct (unconditional) notification post. */
    private void markNotificationPosted() {
        mPostedState = mPlayState;
        mPostedHosted = mHosted;
        mPostedTitle = mTitle;
        mPostedText = mPlayState == PLAY_STATE_PLAYING
            ? (mHosted ? "MIDI 播放托管中（原生合成）" : "网页音频播放中（后台持续播放）")
            : "播放已暂停";
    }

    private static boolean strEq(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    private Notification buildNotification() {
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        // Content intent opens the browser activity.
        Intent openIntent = new Intent(this, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentPi = PendingIntent.getActivity(this, 1, openIntent, flags);

        // Transport control intents come back to this service.
        PendingIntent playPi = PendingIntent.getService(this, 2,
            new Intent(this, MediaPlaybackService.class).setAction(ACTION_PLAY), flags);
        PendingIntent pausePi = PendingIntent.getService(this, 3,
            new Intent(this, MediaPlaybackService.class).setAction(ACTION_PAUSE), flags);
        PendingIntent stopPi = PendingIntent.getService(this, 4,
            new Intent(this, MediaPlaybackService.class).setAction(ACTION_STOP), flags);
        PendingIntent prevPi = PendingIntent.getService(this, 5,
            new Intent(this, MediaPlaybackService.class).setAction(ACTION_PREV), flags);
        PendingIntent nextPi = PendingIntent.getService(this, 6,
            new Intent(this, MediaPlaybackService.class).setAction(ACTION_NEXT), flags);

        // Toolbar order: previous / play-pause / next / stop. The stop action
        // must NOT reuse the skip-previous glyph (it read as a track button).
        Notification.Action prevAction = new Notification.Action.Builder(
            android.R.drawable.ic_media_previous, "上一曲", prevPi).build();
        Notification.Action playAction = new Notification.Action.Builder(
            android.R.drawable.ic_media_play, "播放", playPi).build();
        Notification.Action pauseAction = new Notification.Action.Builder(
            android.R.drawable.ic_media_pause, "暂停", pausePi).build();
        Notification.Action nextAction = new Notification.Action.Builder(
            android.R.drawable.ic_media_next, "下一曲", nextPi).build();
        Notification.Action stopAction = new Notification.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel, "停止", stopPi).build();

        String contentText;
        if (mPlayState == PLAY_STATE_PLAYING) {
            contentText = mHosted ? "MIDI 播放托管中（原生合成）" : "网页音频播放中（后台持续播放）";
        } else {
            contentText = "播放已暂停";
        }

        Notification.Builder builder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_winboll_logo)
            .setContentTitle(mTitle)
            .setContentText(contentText)
            .setContentIntent(contentPi)
            .setOngoing(mPlayState == PLAY_STATE_PLAYING)
            .setShowWhen(false)
            .setStyle(new Notification.MediaStyle()
                .setMediaSession(mSession.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2))
            .addAction(prevAction)
            .addAction(mPlayState == PLAY_STATE_PLAYING ? pauseAction : playAction)
            .addAction(nextAction)
            .addAction(stopAction);
        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = getNotificationManager();
            if (nm != null) {
                NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "网页音频播放",
                    NotificationManager.IMPORTANCE_LOW);
                channel.setDescription("WWWBrowser 网页音频后台播放控制");
                channel.setShowBadge(false);
                nm.createNotificationChannel(channel);
            }
        }
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    // MediaBrowserService registration: exposes the app to system media clients
    // (Bluetooth, output switcher, media browsers) as a music player.
    @Override
    public BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle rootHints) {
        // Accept any media browser client; playback control is limited to
        // transport controls of the current audio via MediaSession.
        return new BrowserRoot(MEDIA_BROWSER_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(String parentId, Result<List<android.media.browse.MediaBrowser.MediaItem>> result) {
        // No browsable media tree: this player controls live playback only.
        result.sendResult(Collections.<android.media.browse.MediaBrowser.MediaItem>emptyList());
    }

    @Override
    public void onDestroy() {
        if (mSession != null) {
            mSession.setActive(false);
            mSession.release();
            mSession = null;
        }
        super.onDestroy();
    }
}
