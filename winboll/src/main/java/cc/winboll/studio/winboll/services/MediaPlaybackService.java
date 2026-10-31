package cc.winboll.studio.winboll.services;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Describe Foreground media playback service for web page audio (Java 7 syntax).
 * Registers the app as a music player via MediaBrowserService + MediaSession,
 * and shows a persistent media notification with Play/Pause/Stop controls while
 * the WWWBrowser WebView is playing audio.
 * Zero external dependencies: uses framework android.media.session.MediaSession
 * and Notification.MediaStyle only (no androidx.media / support libs).
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.media.MediaBrowserService;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;

import java.util.Collections;
import java.util.List;

import cc.winboll.studio.winboll.MainActivity;
import cc.winboll.studio.winboll.R;

public class MediaPlaybackService extends MediaBrowserService {

    public static final String TAG = "MediaPlaybackService";

    // Service actions
    public static final String ACTION_STATE_CHANGED = "cc.winboll.studio.winboll.action.MEDIA_STATE_CHANGED";
    public static final String ACTION_PLAY = "cc.winboll.studio.winboll.action.MEDIA_PLAY";
    public static final String ACTION_PAUSE = "cc.winboll.studio.winboll.action.MEDIA_PAUSE";
    public static final String ACTION_STOP = "cc.winboll.studio.winboll.action.MEDIA_STOP";

    // Intent extras
    public static final String EXTRA_PLAYING = "playing";
    public static final String EXTRA_TITLE = "title";

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
    }

    private static volatile PlaybackController sController;

    /** Register the WebView-side controller (single browser instance). */
    public static void setPlaybackController(PlaybackController controller) {
        sController = controller;
    }

    /**
     * Push playback state from the WebView poller into the service.
     * Starts the service in foreground when playing; updates notification otherwise.
     */
    public static void pushState(Context context, boolean playing, String title) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, MediaPlaybackService.class);
        intent.setAction(ACTION_STATE_CHANGED);
        intent.putExtra(EXTRA_PLAYING, playing);
        intent.putExtra(EXTRA_TITLE, title);
        if (playing) {
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
    private boolean mPlaying = false;
    // True once this service instance has served actual playback. Used to avoid
    // resurrecting the notification after an explicit stop: the WebView poller
    // may deliver one final "stopped" update which would restart a fresh service
    // instance; a never-playing instance dismisses itself silently.
    private boolean mEverPlaying = false;
    private String mTitle = "WinBoLL+ 网页音频";

    @Override
    public void onCreate() {
        super.onCreate();
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
        if (ACTION_PLAY.equals(action)) {
            dispatchPlay();
        } else if (ACTION_PAUSE.equals(action)) {
            dispatchPause();
        } else if (ACTION_STOP.equals(action)) {
            dispatchStop();
        } else if (ACTION_STATE_CHANGED.equals(action)) {
            if (intent != null) {
                mPlaying = intent.getBooleanExtra(EXTRA_PLAYING, false);
                String title = intent.getStringExtra(EXTRA_TITLE);
                if (title != null && title.length() > 0) {
                    mTitle = title;
                }
            }
            if (mPlaying) {
                mEverPlaying = true;
                startForeground(NOTIFICATION_ID, buildNotification());
            } else if (!mEverPlaying) {
                // Fresh instance receiving only "stopped" (post-stop poll update):
                // dismiss silently instead of resurrecting the paused notification.
                stopSelf();
                return START_NOT_STICKY;
            } else {
                // Paused: keep the bar visible (play button) while service alive;
                // actual dismissal happens on explicit stop.
                NotificationManager nm = getNotificationManager();
                if (nm != null) {
                    nm.notify(NOTIFICATION_ID, buildNotification());
                }
            }
            updatePlaybackState();
        }
        return START_STICKY;
    }

    /** Media button / notification play: resume web page audio. */
    private void dispatchPlay() {
        mPlaying = true;
        mEverPlaying = true;
        PlaybackController controller = sController;
        if (controller != null) {
            controller.onPlayRequested();
        }
        startForeground(NOTIFICATION_ID, buildNotification());
        updatePlaybackState();
    }

    /** Media button / notification pause: suspend web page audio (bar stays). */
    private void dispatchPause() {
        mPlaying = false;
        PlaybackController controller = sController;
        if (controller != null) {
            controller.onPauseRequested();
        }
        NotificationManager nm = getNotificationManager();
        if (nm != null) {
            nm.notify(NOTIFICATION_ID, buildNotification());
        }
        updatePlaybackState();
    }

    /** Media button / notification stop: stop web page audio and dismiss the bar. */
    private void dispatchStop() {
        mPlaying = false;
        PlaybackController controller = sController;
        if (controller != null) {
            controller.onStopRequested();
        }
        updatePlaybackState();
        stopForeground(true);
        stopSelf();
    }

    private void updatePlaybackState() {
        if (mSession == null) {
            return;
        }
        int state = mPlaying ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED;
        long actions = PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_STOP;
        mSession.setPlaybackState(new PlaybackState.Builder()
            .setState(state, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1.0f)
            .setActions(actions)
            .build());
        mSession.setMetadata(new MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, mTitle)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, "WinBoLL+ WWWBrowser")
            .build());
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

        Notification.Action playAction = new Notification.Action.Builder(
            android.R.drawable.ic_media_play, "播放", playPi).build();
        Notification.Action pauseAction = new Notification.Action.Builder(
            android.R.drawable.ic_media_pause, "暂停", pausePi).build();
        Notification.Action stopAction = new Notification.Action.Builder(
            android.R.drawable.ic_media_previous, "停止", stopPi).build();

        Notification.Builder builder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_winboll_logo)
            .setContentTitle(mTitle)
            .setContentText(mPlaying ? "网页音频播放中（后台持续播放）" : "网页音频已暂停")
            .setContentIntent(contentPi)
            .setOngoing(mPlaying)
            .setShowWhen(false)
            .setStyle(new Notification.MediaStyle()
                .setMediaSession(mSession.getSessionToken())
                .setShowActionsInCompactView(0, 1))
            .addAction(mPlaying ? pauseAction : playAction)
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
        // play/pause/stop of the current web page audio via MediaSession.
        return new BrowserRoot(MEDIA_BROWSER_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(String parentId, Result<List<android.media.browse.MediaBrowser.MediaItem>> result) {
        // No browsable media tree: this player controls live web page audio only.
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
