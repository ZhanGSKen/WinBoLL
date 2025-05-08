package cc.winboll.studio.timestamp.utils;

/**
 * @Author ZhanGSKen
 * @Date 2025/05/07 02:38
 * @Describe AudioPlayer
 */
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

public class AudioPlayerMP3Util {

    public static final String TAG = "AudioPlayer";

    private static MediaPlayer mediaPlayer;

    /**
     * 播放指定的 MP3 文件
     *
     * @param context 上下文
     * @param mp3FilePath MP3 文件的路径，例如："/storage/emulated/0/Music/song.mp3"
     */
    public static void playMp3(Context context, String mp3FilePath) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        try {
            mediaPlayer = new MediaPlayer();
            Uri uri = Uri.parse(mp3FilePath);
            mediaPlayer.setDataSource(context, uri);
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
     * 释放 MediaPlayer 资源
     */
    private static void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}

