package cc.winboll.studio.libappbase.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * 分享崩溃日志窗口类
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @CreateTime 2026/05/11 22:30:00
 */
public class ShareLogActivity extends Activity {

    public static final String TAG = "ShareLogActivity";
    public static final String EXTRA_CRASH_LOG_FILEPATH = "crash_log_filepath";
    public static final String EXTRA_CRASH_LOG_SUBJECT = "crash_log_subject";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate 进入方法");

        final Intent intent = getIntent();
        if (intent == null) {
            Log.e(TAG, "onCreate intent 为空");
            finish();
            return;
        }

        final String crashLogFilePath = intent.getStringExtra(EXTRA_CRASH_LOG_FILEPATH);
        if (crashLogFilePath == null || crashLogFilePath.isEmpty()) {
            Log.e(TAG, "onCreate crashLogFilePath 为空");
            Toast.makeText(this, "日志文件路径无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final String subject = intent.getStringExtra(EXTRA_CRASH_LOG_SUBJECT);
        handleShareCrashLog(crashLogFilePath, subject);
    }

    private void handleShareCrashLog(final String crashLogFilePath, final String subject) {
        Log.d(TAG, "handleShareCrashLog crashLogFilePath = " + crashLogFilePath);

        final File crashLogFile = new File(crashLogFilePath);
        if (!crashLogFile.exists()) {
            Log.e(TAG, "handleShareCrashLog 文件不存在");
            Toast.makeText(this, "日志文件不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(crashLogFile), "UTF-8"));
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            final String logContent = sb.toString();

            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, logContent);
            if (subject != null && !subject.isEmpty()) {
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            } else {
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "崩溃日志");
            }

            startActivity(Intent.createChooser(shareIntent, "分享日志到"));
            Log.d(TAG, "handleShareCrashLog 分享成功");
        } catch (Exception e) {
            Log.e(TAG, "handleShareCrashLog 异常", e);
            Toast.makeText(this, "分享失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (Exception e) {}
            }
            finish();
        }
    }
}