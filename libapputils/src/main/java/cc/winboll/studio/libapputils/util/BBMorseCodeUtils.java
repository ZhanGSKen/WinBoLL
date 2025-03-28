package cc.winboll.studio.libapputils.util;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/23 16:17:54
 * @Describe 哔哔振动响铃工具集
 */
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BBMorseCodeUtils {
    
    public static final String TAG = "BBMorseCodeUtils";

//    private static final int DOT_DURATION = 100; // 点时长 (ms)
//    private static final int DASH_DURATION = 300; // 划时长 (ms)
//    private static final int SYMBOL_GAP = 100; // 符号间隔 (ms)
//    private static final int CHARACTER_GAP = 300; // 字符间隔 (ms)
//    private static final int WORD_GAP = 700; // 单词间隔 (ms)
//
//    private final Context context;
//    private final Handler handler = new Handler();
//    private boolean isPlaying = false;
//
//    public BBMorseCodeUtils(Context context) {
//        this.context = context.getApplicationContext();
//    }
//
//    public void playMorseCode(String morseCode, boolean vibrateEnabled, boolean ringEnabled) {
//        if (isPlaying) return;
//        isPlaying = true;
//
//        List<List<String>> words = parseMorseCode(morseCode);
//        startSequence(words, vibrateEnabled, ringEnabled);
//    }
//
//    private void startSequence(final List<List<String>> words, final boolean vibrateEnabled, final boolean ringEnabled) {
//        final int[] wordIndex = {0};
//        final int[] charIndex = {0};
//        final int[] symbolIndex = {0};
//
//        Runnable sequenceRunner = new Runnable() {
//            @Override
//            public void run() {
//                if (wordIndex[0] >= words.size()) {
//                    stop();
//                    return;
//                }
//
//                List<String> characters = words.get(wordIndex[0]);
//                if (charIndex[0] >= characters.size()) {
//                    charIndex[0] = 0;
//                    symbolIndex[0] = 0;
//                    wordIndex[0]++;
//                    handler.postDelayed(this, WORD_GAP);
//                    return;
//                }
//
//                String character = characters.get(charIndex[0]);
//                if (symbolIndex[0] >= character.length()) {
//                    symbolIndex[0] = 0;
//                    charIndex[0]++;
//                    handler.postDelayed(this, CHARACTER_GAP);
//                    return;
//                }
//
//                char symbol = character.charAt(symbolIndex[0]);
//                symbolIndex[0]++;
//
//                long duration = (symbol == '.') ? DOT_DURATION : DASH_DURATION;
//
//                // 执行振动
//                if (vibrateEnabled) {
//                    vibrate(duration);
//                }
//
//                // 播放声音
//                if (ringEnabled) {
//                    playSound(symbol, duration);
//                }
//
//                // 安排下一个符号
//                handler.postDelayed(this, duration + SYMBOL_GAP);
//            }
//        };
//
//        handler.post(sequenceRunner);
//    }
//
//    private void vibrate(long duration) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            VibratorManager vibratorManager = context.getSystemService(VibratorManager.class);
//            Vibrator vibrator = vibratorManager.getDefaultVibrator();
//            vibrator.vibrate(duration);
//        } else {
//            Vibrator vibrator = context.getSystemService(Vibrator.class);
//            if (vibrator != null && vibrator.hasVibrator()) {
//                vibrator.vibrate(duration);
//            }
//        }
//    }
//
//    private void playSound(char symbol, long duration) {
//        int soundRes = (symbol == '.') ? R.raw.morse_dot : R.raw.morse_dash;
//        MediaPlayer mediaPlayer = MediaPlayer.create(context, soundRes);
//
//        mediaPlayer.setOnPreparedListener(> {
//            mp.start();
//            handler.postDelayed(mp::stop, duration);
//        });
//
//        mediaPlayer.setOnCompletionListener(> {
//            mp.release();
//        });
//    }
//
//    private List<List<String>> parseMorseCode(String code) {
//        List<List<String>> words = new ArrayList<>();
//        String[] wordParts = code.split("/");
//
//        for (String word : wordParts) {
//            String[] chars = word.trim().split("\\s+");
//            words.add(new ArrayList<>(Arrays.asList(chars)));
//        }
//        return words;
//    }
//
//    public void stop() {
//        isPlaying = false;
//        handler.removeCallbacksAndMessages(null);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            VibratorManager vibratorManager = context.getSystemService(VibratorManager.class);
//            Vibrator vibrator = vibratorManager.getDefaultVibrator();
//            vibrator.cancel();
//        } else {
//            Vibrator vibrator = context.getSystemService(Vibrator.class);
//            if (vibrator != null) {
//                vibrator.cancel();
//            }
//        }
//    }
}

//    使用说明：
//
//    1. 在AndroidManifest.xml中添加权限：
//
//    xml
//
//    <uses-permission android:name="android.permission.VIBRATE"/>
//     
//
//    2. 在res/raw目录下添加两个声音文件：
//
//    - morse_dot.wav（短音）
//
//    - morse_dash.wav（长音）
//
//    3. 使用示例：
//
//    java
//
//    MorsePlayer morsePlayer = new MorsePlayer(this);
//morsePlayer.playMorseCode(".... . / .- --", true, true);
//
// 停止播放
// morsePlayer.stop();
//    MorsePlayer morsePlayer = new MorsePlayer(this);
//morsePlayer.playMorseCode(".... . / .- --", true, true);
//
//// 停止播放
//// morsePlayer.stop();
//
