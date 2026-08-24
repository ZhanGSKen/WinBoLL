package cc.winboll.studio.libappbase.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
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
    public static final String EXTRA_CRASH_LOG_EMAIL_TO = "crash_log_email_to";

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
        final String emailTo = intent.getStringExtra(EXTRA_CRASH_LOG_EMAIL_TO);
        handleShareCrashLog(crashLogFilePath, subject, emailTo);
    }

    private String getAppInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("========== 应用信息 ==========\n");
        try {
            PackageInfo pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            sb.append("应用包名: ").append(getPackageName()).append("\n");
            sb.append("版本名称: ").append(pkgInfo.versionName).append("\n");
            sb.append("版本号: ").append(pkgInfo.versionCode).append("\n");
        } catch (PackageManager.NameNotFoundException e) {
            sb.append("应用包名: ").append(getPackageName()).append("\n");
            sb.append("版本信息: 获取失败\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    private String getDeviceInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("========== 设备信息 ==========\n");
        sb.append("设备制造商: ").append(Build.MANUFACTURER).append("\n");
        sb.append("设备型号: ").append(Build.MODEL).append("\n");
        sb.append("设备名称: ").append(Build.DEVICE).append("\n");
        sb.append("Android版本: ").append(Build.VERSION.RELEASE).append("\n");
        sb.append("Android API: ").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("系统指纹: ").append(Build.FINGERPRINT).append("\n");
        sb.append("\n");
        return sb.toString();
    }

    private void handleShareCrashLog(final String crashLogFilePath, final String subject, final String emailTo) {
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

            final StringBuilder emailContent = new StringBuilder();
            emailContent.append(getAppInfo());
            emailContent.append(getDeviceInfo());
            emailContent.append("========== 崩溃日志 ==========\n");
            emailContent.append(logContent);

            final Intent shareIntent = new Intent(Intent.ACTION_SENDTO);
            shareIntent.setData(android.net.Uri.parse("mailto:"));
            shareIntent.putExtra(Intent.EXTRA_TEXT, emailContent.toString());
            if (subject != null && !subject.isEmpty()) {
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            } else {
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "崩溃日志");
            }
            if (emailTo != null && !emailTo.isEmpty()) {
                shareIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailTo});
            }

            startActivity(Intent.createChooser(shareIntent, "发送日志到"));
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