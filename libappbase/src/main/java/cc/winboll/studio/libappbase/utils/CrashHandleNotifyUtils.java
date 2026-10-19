package cc.winboll.studio.libappbase.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import cc.winboll.studio.libappbase.CrashHandler;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * 应用崩溃处理通知实用工具集（类库兼容版）
 * 核心功能：作为独立类库使用，发送崩溃通知，点击跳转宿主应用的 GlobalCrashActivity 并传递日志
 * 适配说明：移除固定包名依赖，通过外部传入宿主包名，支持任意应用集成使用
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @CreateTime 2025/11/29 21:12:00
 * @EditTime 2026/05/11 21:55:00
 */
public class CrashHandleNotifyUtils {

    // ====================== 常量定义 ======================
    public static final String TAG = "CrashHandleNotifyUtils";
    /** 通知渠道ID（Android 8.0+ 必须） */
    private static final String CRASH_NOTIFY_CHANNEL_ID = "crash_notify_channel";
    /** 通知渠道名称（用户可见） */
    private static final String CRASH_NOTIFY_CHANNEL_NAME = "应用崩溃通知";
    /** 通知ID（唯一） */
    public static final int CRASH_NOTIFY_ID = 0x001;
    /** Android 12 对应 API 版本号（31） */
    private static final int API_LEVEL_ANDROID_12 = 31;
    /** PendingIntent.FLAG_IMMUTABLE 常量值（API 31+） */
    private static final int FLAG_IMMUTABLE = 0x00000040;
    /** 通知摘要最大长度 */
    private static final int SUMMARY_MAX_LENGTH = 200;
    /** 分享日志请求码 */
    private static final int REQUEST_CODE_SHARE_LOG = 0x002;
    /** 缓存崩溃日志子目录 */
    private static final String CRASH_LOG_CACHE_SUBDIR = "crashnotify";
    /** 缓存崩溃日志文件名 */
    private static final String CRASH_LOG_CACHE_FILENAME = "crash_log.txt";

    // ====================== 静态成员 ======================
    private static String sHostPackageName = "";
    private static String sCrashLogCacheFilePath = "";

    // ====================== 正则表达式定义 ======================
    private static final String REGEX_EXCEPTION_TYPE = "([\\w.]+Exception|[\\w.]+Error)";
    private static final String REGEX_EXCEPTION_MESSAGE = "(?<=:\\s)(.+?)(?=\\n|\\r|$)";
    private static final String REGEX_STACK_TRACE = "\\s+at\\s+([\\w.$]+)\\.([\\w<>]+)\\(([^:]+\\.java):(\\d+)\\)";
    private static final String REGEX_CAUSE = "(?<=Caused by:\\s)" + REGEX_EXCEPTION_TYPE + "\\s*:";

    // ====================== 对外核心方法 ======================
    /**
     * 处理未捕获异常（类库入口核心方法）
     * @param hostApp 宿主Application实例
     * @param hostPackageName 宿主应用包名
     * @param errorLog 崩溃日志内容
     * @param reportCrashActivity 崩溃详情跳转Activity类
     */
    public static void handleUncaughtException(final android.app.Application hostApp,
                                               final String hostPackageName,
                                               final String errorLog,
                                               final Class<?> reportCrashActivity) {
        LogUtils.d(TAG, "handleUncaughtException 进入方法");
        if (hostApp == null || TextUtils.isEmpty(hostPackageName) || TextUtils.isEmpty(errorLog)) {
            LogUtils.e(TAG, "handleUncaughtException 参数为空校验不通过");
            return;
        }
        sHostPackageName = hostPackageName;
        final String hostAppName = getHostAppName(hostApp, hostPackageName);
        final String crashLogFilePath = saveCrashLogToCache(hostApp, errorLog);
        if (TextUtils.isEmpty(crashLogFilePath)) {
            LogUtils.e(TAG, "保存崩溃日志到缓存文件失败");
            return;
        }
        sCrashLogCacheFilePath = crashLogFilePath;
        final Intent shareIntent = new Intent(hostApp, ShareLogActivity.class);
        shareIntent.putExtra(ShareLogActivity.EXTRA_CRASH_LOG_FILEPATH, crashLogFilePath);
        shareIntent.putExtra(ShareLogActivity.EXTRA_CRASH_LOG_SUBJECT, "崩溃日志");
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final PendingIntent sharePendingIntent = createSharePendingIntent(hostApp, shareIntent);
        sendCrashNotification(hostApp, hostPackageName, hostAppName, errorLog, reportCrashActivity, sharePendingIntent);
    }

    /**
     * 重载兼容方法：适配原有CrashHandler调用方式
     * @param hostApp 宿主Application实例
     * @param intent 携带崩溃信息Intent
     * @param reportCrashActivity 崩溃详情Activity
     */
    public static void handleUncaughtException(final android.app.Application hostApp,
                                               final Intent intent,
                                               final Class<?> reportCrashActivity) {
        LogUtils.d(TAG, "handleUncaughtException 重载方法进入");
        String hostPackageName = intent.getStringExtra("EXTRA_HOST_PACKAGE_NAME");
        if (TextUtils.isEmpty(hostPackageName)) {
            hostPackageName = hostApp.getPackageName();
            LogUtils.w(TAG, "未携带宿主包名，默认使用应用自身包名");
        }
        final String errorLog = intent.getStringExtra(CrashHandler.EXTRA_CRASH_LOG);
        handleUncaughtException(hostApp, hostPackageName, errorLog, reportCrashActivity);
    }

    /**
     * 资源释放
     * @param hostContext 宿主上下文
     */
    public static void release(final Context hostContext) {
        LogUtils.d(TAG, "release 执行资源释放");
        sHostPackageName = "";
        sCrashLogCacheFilePath = "";
    }

    // ====================== 内部工具方法 ======================
    /**
     * 获取宿主应用名称
     * @param hostContext 宿主上下文
     * @param hostPackageName 宿主包名
     * @return 应用名称，失败返回未知应用
     */
    private static String getHostAppName(final Context hostContext, final String hostPackageName) {
        try {
            return hostContext.getPackageManager()
                .getApplicationLabel(hostContext.getPackageManager()
                                     .getApplicationInfo(hostPackageName, 0)).toString();
        } catch (Exception e) {
            LogUtils.e(TAG, "获取宿主应用名称失败", e);
            return "未知应用";
        }
    }

    /**
     * 保存崩溃日志到缓存文件
     * @param hostContext 宿主上下文
     * @param crashLog 崩溃日志内容
     * @return 缓存文件路径，失败返回空字符串
     */
    private static String saveCrashLogToCache(final Context hostContext, final String crashLog) {
        if (hostContext == null || TextUtils.isEmpty(crashLog)) {
            return "";
        }
        BufferedReader reader = null;
        FileOutputStream fos = null;
        try {
            final File cacheDir = new File(hostContext.getCacheDir(), CRASH_LOG_CACHE_SUBDIR);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            final File cacheFile = new File(cacheDir, CRASH_LOG_CACHE_FILENAME);
            if (cacheFile.exists()) {
                cacheFile.delete();
            }
            cacheFile.createNewFile();
            fos = new FileOutputStream(cacheFile);
            fos.write(crashLog.getBytes("UTF-8"));
            fos.flush();
            LogUtils.d(TAG, "saveCrashLogToCache 保存崩溃日志到缓存: " + cacheFile.getAbsolutePath());
            return cacheFile.getAbsolutePath();
        } catch (Exception e) {
            LogUtils.e(TAG, "saveCrashLogToCache 异常", e);
            return "";
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (Exception e) {}
            }
            if (fos != null) {
                try { fos.close(); } catch (Exception e) {}
            }
        }
    }

    /**
     * 读取缓存的崩溃日志文件内容
     * @param hostContext 宿主上下文
     * @return 日志内容，失败返回空字符串
     */
    private static String readCachedCrashLog(final Context hostContext) {
        if (hostContext == null || TextUtils.isEmpty(sCrashLogCacheFilePath)) {
            return "";
        }
        BufferedReader reader = null;
        try {
            final File cacheFile = new File(sCrashLogCacheFilePath);
            if (!cacheFile.exists()) {
                LogUtils.w(TAG, "readCachedCrashLog 缓存文件不存在");
                return "";
            }
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(cacheFile), "UTF-8"));
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            LogUtils.e(TAG, "readCachedCrashLog 异常", e);
            return "";
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (Exception e) {}
            }
        }
    }

    /**
     * 发送崩溃系统通知
     * @param hostContext 宿主上下文
     * @param hostPackageName 宿主包名
     * @param hostAppName 宿主应用名
     * @param errorLog 崩溃日志
     * @param reportCrashActivity 跳转Activity
     * @param sharePendingIntent 分享日志PendingIntent
     */
    private static void sendCrashNotification(final Context hostContext,
                                              final String hostPackageName,
                                              final String hostAppName,
                                              final String errorLog,
                                              final Class<?> reportCrashActivity,
                                              final PendingIntent sharePendingIntent) {
        final NotificationManager notificationManager =
            (NotificationManager) hostContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            LogUtils.e(TAG, "获取NotificationManager失败");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createCrashNotifyChannel(hostContext, notificationManager);
        }
        final PendingIntent jumpIntent = getGlobalCrashPendingIntent(hostContext,
                                                                     hostPackageName, errorLog, reportCrashActivity);
        if (jumpIntent == null) {
            LogUtils.e(TAG, "构建跳转PendingIntent失败");
            return;
        }
        final Notification notification = buildNotification(hostContext, hostPackageName, hostAppName, errorLog, jumpIntent, sharePendingIntent);
        notificationManager.notify(CRASH_NOTIFY_ID, notification);
        LogUtils.d(TAG, "崩溃通知发送成功，宿主包名：" + hostPackageName);
    }

    /**
     * 创建分享日志PendingIntent
     * @param hostContext 宿主上下文
     * @param shareIntent 分享意图
     * @return PendingIntent实例
     */
    private static PendingIntent createSharePendingIntent(final Context hostContext, final Intent shareIntent) {
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= API_LEVEL_ANDROID_12) {
            flags |= FLAG_IMMUTABLE;
        }
        return PendingIntent.getActivity(
            hostContext,
            REQUEST_CODE_SHARE_LOG,
            shareIntent,
            flags
        );
    }

    /**
     * 创建通知渠道（适配Android O及以上）
     * @param hostContext 宿主上下文
     * @param notificationManager 通知管理器
     */
    private static void createCrashNotifyChannel(final Context hostContext,
                                                 final NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                CRASH_NOTIFY_CHANNEL_ID,
                CRASH_NOTIFY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("应用崩溃通知（由 WinBoLL Studio 类库提供，点击查看详情）");
            notificationManager.createNotificationChannel(channel);
            LogUtils.d(TAG, "通知渠道创建完成");
        }
    }

    /**
     * 构建跳转崩溃详情页PendingIntent
     * @param hostContext 宿主上下文
     * @param hostPackageName 宿主包名
     * @param errorLog 崩溃日志
     * @param reportCrashActivity 目标Activity
     * @return PendingIntent实例
     */
    private static PendingIntent getGlobalCrashPendingIntent(final Context hostContext,
                                                             final String hostPackageName,
                                                             final String errorLog,
                                                             final Class<?> reportCrashActivity) {
        try {
            final Intent crashIntent = new Intent(hostContext, reportCrashActivity);
            crashIntent.setPackage(hostPackageName);
            crashIntent.putExtra(CrashHandler.EXTRA_CRASH_LOG, errorLog);
            crashIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= API_LEVEL_ANDROID_12) {
                flags |= FLAG_IMMUTABLE;
            }
            return PendingIntent.getActivity(
                hostContext,
                CRASH_NOTIFY_ID,
                crashIntent,
                flags
            );
        } catch (Exception e) {
            LogUtils.e(TAG, "构建跳转Intent异常", e);
            return null;
        }
    }

    /**
     * 构建Notification通知实例
     * @param hostContext 宿主上下文
     * @param hostPackageName 宿主包名
     * @param hostAppName 宿主应用名
     * @param errorLog 崩溃日志
     * @param jumpIntent 点击跳转意图
     * @param shareIntent 分享日志意图
     * @return 构建好的Notification
     */
    @SuppressWarnings("deprecation")
    private static Notification buildNotification(final Context hostContext,
                                                 final String hostPackageName,
                                                 final String hostAppName,
                                                 final String errorLog,
                                                 final PendingIntent jumpIntent,
                                                 final PendingIntent shareIntent) {
        Notification.Builder builder = new Notification.Builder(hostContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CRASH_NOTIFY_CHANNEL_ID);
        }
        final String briefInfo = extractBriefInfo(errorLog);
        final Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.setBigContentTitle(hostAppName + " 崩溃");
        bigTextStyle.bigText(briefInfo);
        bigTextStyle.setSummaryText("点击查看详情");
        builder.setStyle(bigTextStyle);
        builder.setSmallIcon(hostContext.getApplicationInfo().icon)
            .setContentTitle(hostAppName + " 崩溃")
            .setContentText(briefInfo.split("\n")[0])
            .setContentIntent(jumpIntent)
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setPriority(Notification.PRIORITY_DEFAULT);
        if (shareIntent != null) {
            builder.addAction(android.R.drawable.ic_menu_send, "分享日志", shareIntent);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return builder.build();
        } else {
            return builder.getNotification();
        }
    }

    /**
     * 截取缩略日志文本
     * @param content 原始日志
     * @return 缩略文案
     */
    private static String getShortContent(final String errorLog) {
        if (errorLog == null || errorLog.isEmpty()) {
            return "无崩溃日志";
        }
        final String brief = extractBriefInfo(errorLog);
        final String firstLine = brief.split("\n")[0];
        if (firstLine.length() > 80) {
            return firstLine.substring(0, 80) + "...";
        }
        return firstLine;
    }

    /**
     * 使用正则表达式从崩溃日志中提取简要信息
     * @param crashLog 完整崩溃日志
     * @return 简要崩溃信息
     */
    private static String extractBriefInfo(final String crashLog) {
        if (crashLog == null || crashLog.isEmpty()) {
            return "无崩溃日志";
        }
        final StringBuilder brief = new StringBuilder();
        try {
            java.util.regex.Pattern exceptionPattern = java.util.regex.Pattern.compile(REGEX_EXCEPTION_TYPE);
            java.util.regex.Matcher exceptionMatcher = exceptionPattern.matcher(crashLog);
            if (exceptionMatcher.find()) {
                brief.append(exceptionMatcher.group(1));
            }
            java.util.regex.Pattern messagePattern = java.util.regex.Pattern.compile(REGEX_EXCEPTION_MESSAGE);
            java.util.regex.Matcher messageMatcher = messagePattern.matcher(crashLog);
            if (messageMatcher.find()) {
                String message = messageMatcher.group(1).trim();
                if (message.length() > 100) {
                    message = message.substring(0, 100) + "...";
                }
                if (brief.length() > 0) {
                    brief.append(" - ");
                }
                brief.append(message);
            }
            java.util.regex.Pattern causePattern = java.util.regex.Pattern.compile(REGEX_CAUSE);
            java.util.regex.Matcher causeMatcher = causePattern.matcher(crashLog);
            if (causeMatcher.find()) {
                if (brief.length() > 0) {
                    brief.append("\n");
                }
                brief.append("原因: ").append(causeMatcher.group(1));
            }
            java.util.regex.Pattern stackPattern = java.util.regex.Pattern.compile(REGEX_STACK_TRACE);
            java.util.regex.Matcher stackMatcher = stackPattern.matcher(crashLog);
            int lineCount = 0;
            while (stackMatcher.find() && lineCount < 3) {
                if (brief.length() > 0) {
                    brief.append("\n");
                }
                brief.append("  at ").append(stackMatcher.group(1)).append(".")
                     .append(stackMatcher.group(2)).append("(")
                     .append(stackMatcher.group(3)).append(":")
                     .append(stackMatcher.group(4)).append(")");
                lineCount++;
            }
            if (brief.length() == 0) {
                brief.append(crashLog.length() > SUMMARY_MAX_LENGTH ? crashLog.substring(0, SUMMARY_MAX_LENGTH) + "..." : crashLog);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "提取崩溃简要信息失败", e);
            brief.setLength(0);
            brief.append(crashLog.length() > SUMMARY_MAX_LENGTH ? crashLog.substring(0, SUMMARY_MAX_LENGTH) + "..." : crashLog);
        }
        return brief.toString();
    }
}