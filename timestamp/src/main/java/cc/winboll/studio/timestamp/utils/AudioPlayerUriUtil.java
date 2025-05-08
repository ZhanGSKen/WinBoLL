package cc.winboll.studio.timestamp.utils;

/**
 * @Author ZhanGSKen
 * @Date 2025/05/07 02:31
 * @Describe AudioPlayerUtil
 */
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

public class AudioPlayerUriUtil {

    public static final String TAG = "AudioPlayerUtil";

    private static MediaPlayer mediaPlayer;

    /**
     * 播放指定Uri的音频
     * @param context 上下文
     * @param audioUri 音频的Uri
     */
    public static void playAudio(Context context, Uri audioUri) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(context, audioUri);
            mediaPlayer.prepare();
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        releaseMediaPlayer();
                    }
                });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Log.e("AudioPlayer", "播放音频时出错: what=" + what + ", extra=" + extra);
                        releaseMediaPlayer();
                        return true;
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
            releaseMediaPlayer();
        }
    }

    /**
     * 释放MediaPlayer资源
     */
    private static void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}

