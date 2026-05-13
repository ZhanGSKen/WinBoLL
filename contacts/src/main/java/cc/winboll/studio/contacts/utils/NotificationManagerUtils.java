package cc.winboll.studio.contacts.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.provider.Settings;

import cc.winboll.studio.contacts.MainActivity;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * 通知工具类：统一管理前台服务/临时通知
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @CreateTime 2025/12/14 21:01:00
 * @LastEditTime 2026/01/07 22:00:00
 * @Describe 适配API30，支持拨号服务前台保活通知、临时通知提醒，提供通知的创建、更新、取消功能；支持通知版本管理，版本变更时自动清理旧渠道
 */
public class NotificationManagerUtils {
    // ================================== 静态常量（置顶统一管理，杜绝魔法值）=================================
    public static final String TAG = "NotificationManagerUtils";
    // ********** 新增：通知版本管理常量 **********
    /** 通知版本标识（源码标记版本，变更时会清理旧渠道） */
    public static final String NOTIFICATION_VERSION = "v1.0.3";
    /** SP存储键：已保存的通知版本 */
    private static final String SP_KEY_NOTIFICATION_VERSION = "sp_key_notification_version";
    /** SP文件名 */
    private static final String SP_NAME_NOTIFICATION = "sp_notification_manager";
    // ******************************************
    // 通知渠道ID（API26+ 必需，区分通知类型）
    public static final String CHANNEL_ID_FOREGROUND = "cc.winboll.studio.contacts.channel.foreground";
    public static final String CHANNEL_ID_TEMPORARY = "cc.winboll.studio.contacts.channel.temporary";
    // 通知ID（唯一标识，避免重复）
    public static final int NOTIFY_ID_FOREGROUND_SERVICE = 1001;
    public static final int NOTIFY_ID_TEMPORARY = 1002;
    // 低版本兼容：默认通知图标（API<21 避免显示异常）
    private static final int NOTIFICATION_DEFAULT_ICON = R.drawable.ic_launcher;
    // 通知内容兜底常量
    private static final String FOREGROUND_NOTIFY_TITLE_DEFAULT = "拨号服务前台通知";
    private static final String FOREGROUND_NOTIFY_CONTENT_DEFAULT = "前台通知内容";
    private static final String TEMPORARY_NOTIFY_TITLE_DEFAULT = "拨号服务临时通知";
    private static final String TEMPORARY_NOTIFY_CONTENT_DEFAULT = "临时通知内容";
    // PendingIntent请求码
    private static final int PENDING_INTENT_REQUEST_CODE_FOREGROUND = 0;
    private static final int PENDING_INTENT_REQUEST_CODE_TEMPORARY = 1;
    // 消息通知自增ID（起始值）
    private static int snMessageNotificationID = 10000;

    // ================================== 成员变量（私有封装，按依赖优先级排序）=================================
    // 核心上下文（应用级，避免内存泄漏）
    private Context mContext;
    // 系统通知服务（核心依赖）
    private NotificationManager mNotificationManager;
    // 前台服务通知实例（单独持有，便于更新/取消）
    private Notification mForegroundServiceNotify;
    // ********** 新增：SP实例（用于版本存储） **********
    private SharedPreferences mSp;

    // ================================== 构造方法（初始化核心资源，前置校验）=================================
    public NotificationManagerUtils(Context context) {
        LogUtils.d(TAG, "NotificationManagerUtils() 构造方法调用 | context=" + context);
        // 前置校验：Context非空
        if (context == null) {
            LogUtils.e(TAG, "NotificationManagerUtils() 构造失败：context is null");
            return;
        }
        // 初始化核心资源
        this.mContext = context.getApplicationContext();
        this.mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        // ********** 新增：初始化SP **********
        this.mSp = mContext.getSharedPreferences(SP_NAME_NOTIFICATION, Context.MODE_PRIVATE);
        LogUtils.d(TAG, "NotificationManagerUtils() 核心资源初始化完成 | mContext=" + mContext + " | mNotificationManager=" + mNotificationManager + " | mSp=" + mSp);

        // ********** 新增：版本检查与旧渠道清理 **********
        checkNotificationVersionAndCleanOldChannels();

        // 初始化通知渠道（API26+ 必需）
        initNotificationChannels();
        LogUtils.d(TAG, "NotificationManagerUtils() 构造完成");
    }

    // ================================== 新增核心方法：通知版本检查与旧渠道清理 =================================
    /**
     * 检查当前通知版本与SP中保存的版本是否一致
     * 若不一致，清理所有旧通知渠道，并更新SP中的版本记录
     */
    private void checkNotificationVersionAndCleanOldChannels() {
        LogUtils.d(TAG, "checkNotificationVersionAndCleanOldChannels() 方法调用 | 源码版本=" + NOTIFICATION_VERSION + " | SP版本=" + getSavedNotificationVersion());
        // 1. 版本一致，无需处理
        if (NOTIFICATION_VERSION.equals(getSavedNotificationVersion())) {
            LogUtils.d(TAG, "checkNotificationVersionAndCleanOldChannels() 版本一致，无需清理渠道");
            return;
        }

        // 2. 版本不一致，清理所有旧渠道
        LogUtils.w(TAG, "checkNotificationVersionAndCleanOldChannels() 版本不一致，开始清理所有旧通知渠道");
        cleanAllNotificationChannels();

        // 3. 取消所有旧通知
        cancelAllNotifications();
        LogUtils.d(TAG, "checkNotificationVersionAndCleanOldChannels() 已取消所有旧通知");

        // 4. 更新SP中的版本记录
        saveNotificationVersion(NOTIFICATION_VERSION);
        LogUtils.d(TAG, "checkNotificationVersionAndCleanOldChannels() 已更新SP版本记录为：" + NOTIFICATION_VERSION);
    }

    /**
     * 从SP中获取已保存的通知版本
     * @return 已保存的版本号，无则返回空字符串
     */
    private String getSavedNotificationVersion() {
        return mSp.getString(SP_KEY_NOTIFICATION_VERSION, "");
    }

    /**
     * 将当前通知版本保存到SP
     * @param version 要保存的版本号
     */
    private void saveNotificationVersion(String version) {
        mSp.edit().putString(SP_KEY_NOTIFICATION_VERSION, version).commit();
    }

    /**
     * 清理所有已创建的通知渠道（API26+ 有效）
     */
    private void cleanAllNotificationChannels() {
        LogUtils.d(TAG, "cleanAllNotificationChannels() 方法调用");
        // API<26 无渠道机制，直接返回
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || mNotificationManager == null) {
            LogUtils.d(TAG, "cleanAllNotificationChannels() API<26 或 NotificationManager为空，无需清理");
            return;
        }

        // 遍历所有渠道并删除
        for (NotificationChannel channel : mNotificationManager.getNotificationChannels()) {
            LogUtils.d(TAG, "cleanAllNotificationChannels() 正在删除渠道：" + channel.getId() + " | " + channel.getName());
            mNotificationManager.deleteNotificationChannel(channel.getId());
        }
        LogUtils.d(TAG, "cleanAllNotificationChannels() 所有旧渠道清理完成");
    }

    // ================================== 核心初始化方法（通知渠道，API分级适配）=================================
    /**
     * 初始化通知渠道：前台服务渠道（无铃声+无振动）、临时提醒渠道（系统默认铃声+无振动）
     */
    private void initNotificationChannels() {
        LogUtils.d(TAG, "initNotificationChannels() 执行通知渠道初始化");
        // API<26 无渠道机制，直接返回
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            LogUtils.d(TAG, "initNotificationChannels() API<26，无需创建渠道");
            return;
        }
        // 通知服务为空，避免空指针
        if (mNotificationManager == null) {
            LogUtils.e(TAG, "initNotificationChannels() 失败：NotificationManager is null");
            return;
        }

        // 1. 拨号前台服务渠道（低优先级，后台保活无打扰）
        NotificationChannel foregroundChannel = new NotificationChannel(
			CHANNEL_ID_FOREGROUND,
			"拨号前台服务",
			NotificationManager.IMPORTANCE_LOW
        );
        foregroundChannel.setDescription("拨号前台服务后台运行，无声音、无振动");
        foregroundChannel.enableLights(false);
        foregroundChannel.enableVibration(false);
        foregroundChannel.setSound(null, null); // 强制无铃声
        foregroundChannel.setShowBadge(false);
        foregroundChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
        LogUtils.d(TAG, "initNotificationChannels() 拨号前台服务渠道配置完成");

        // 2. 其他临时通知渠道（中优先级，系统默认铃声，无振动）
        NotificationChannel temporaryChannel = new NotificationChannel(
			CHANNEL_ID_TEMPORARY,
			"临时通知",
			NotificationManager.IMPORTANCE_DEFAULT
        );
        temporaryChannel.setDescription("其他临时通知，系统默认铃声，无振动");
        temporaryChannel.enableLights(true);
        temporaryChannel.enableVibration(false);
        temporaryChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), Notification.AUDIO_ATTRIBUTES_DEFAULT);
        temporaryChannel.setShowBadge(false);
        temporaryChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        LogUtils.d(TAG, "initNotificationChannels() 其他临时通知渠道配置完成");

        // 注册渠道到系统
        mNotificationManager.createNotificationChannel(foregroundChannel);
        mNotificationManager.createNotificationChannel(temporaryChannel);
        LogUtils.d(TAG, "initNotificationChannels() 成功：创建前台服务+其他临时通知渠道");
    }

    // ================================== 对外核心方法（前台服务通知：启动/更新/取消）=================================
    /**
     * 启动前台服务通知（API30适配，无铃声）
     * @param service 前台服务实例
     * @param message 通知内容
     */
    public void startForegroundServiceNotify(Service service, String message) {
        LogUtils.d(TAG, "startForegroundServiceNotify() 方法调用 | notifyId=" + NOTIFY_ID_FOREGROUND_SERVICE + " | service=" + service + " | message=" + message);
        // 前置校验：参数非空
        if (service == null || mNotificationManager == null) {
            LogUtils.e(TAG, "startForegroundServiceNotify() 失败：param is null | service=" + service + " | mNotificationManager=" + mNotificationManager);
            return;
        }

        // 构建前台通知
        mForegroundServiceNotify = buildForegroundNotification(message);
        if (mForegroundServiceNotify == null) {
            LogUtils.e(TAG, "startForegroundServiceNotify() 失败：构建通知为空");
            return;
        }

        // 启动前台服务（API30无FOREGROUND_SERVICE_TYPE限制）
        try {
            service.startForeground(NOTIFY_ID_FOREGROUND_SERVICE, mForegroundServiceNotify);
            LogUtils.d(TAG, "startForegroundServiceNotify() 成功");
        } catch (Exception e) {
            LogUtils.e(TAG, "startForegroundServiceNotify() 异常", e);
        }
    }

    /**
     * 更新前台服务通知内容（复用通知ID，保持无铃声）
     * @param message 新的通知内容
     */
    public void updateForegroundServiceNotify(String message) {
        LogUtils.d(TAG, "updateForegroundServiceNotify() 方法调用 | notifyId=" + NOTIFY_ID_FOREGROUND_SERVICE + " | message=" + message);
        if (mNotificationManager == null) {
            LogUtils.e(TAG, "updateForegroundServiceNotify() 失败：mNotificationManager is null");
            return;
        }

        mForegroundServiceNotify = buildForegroundNotification(message);
        if (mForegroundServiceNotify == null) {
            LogUtils.e(TAG, "updateForegroundServiceNotify() 失败：构建通知为空");
            return;
        }

        try {
            mNotificationManager.notify(NOTIFY_ID_FOREGROUND_SERVICE, mForegroundServiceNotify);
            LogUtils.d(TAG, "updateForegroundServiceNotify() 成功");
        } catch (Exception e) {
            LogUtils.e(TAG, "updateForegroundServiceNotify() 异常", e);
        }
    }

    /**
     * 取消前台服务通知（Service销毁时调用）
     */
    public void cancelForegroundServiceNotify() {
        LogUtils.d(TAG, "cancelForegroundServiceNotify() 方法调用 | notifyId=" + NOTIFY_ID_FOREGROUND_SERVICE);
        cancelNotification(NOTIFY_ID_FOREGROUND_SERVICE);
        mForegroundServiceNotify = null;
        LogUtils.d(TAG, "cancelForegroundServiceNotify() 成功");
    }

    // ================================== 对外核心方法（临时通知：发送）=================================
    /**
     * 发送临时通知（自增ID，避免覆盖，系统默认铃声）
     * @param context 上下文
     * @param message 通知内容
     */
    public synchronized void showTemporaryNotification(Context context, String message) {
        snMessageNotificationID++;
        LogUtils.d(TAG, "showTemporaryNotification() 方法调用 | notifyId=" + snMessageNotificationID + " | context=" + context + " | message=" + message);
        // 前置校验：参数非空
        if (context == null || mNotificationManager == null) {
            LogUtils.e(TAG, "showTemporaryNotification() 失败：param is null | context=" + context + " | mNotificationManager=" + mNotificationManager);
            return;
        }

        Notification temporaryNotify = buildTemporaryNotification(context, message);
        if (temporaryNotify == null) {
            LogUtils.e(TAG, "showTemporaryNotification() 失败：构建通知为空");
            return;
        }

        try {
            mNotificationManager.notify(snMessageNotificationID, temporaryNotify);
            LogUtils.d(TAG, "showTemporaryNotification() 成功 | notifyId=" + snMessageNotificationID);
        } catch (Exception e) {
            LogUtils.e(TAG, "showTemporaryNotification() 异常 | notifyId=" + snMessageNotificationID, e);
        }
    }

    // ================================== 对外工具方法（通知取消：单个/全部）=================================
    /**
     * 取消指定ID的通知
     * @param notifyId 通知唯一标识
     */
    public void cancelNotification(int notifyId) {
        LogUtils.d(TAG, "cancelNotification() 方法调用 | notifyId=" + notifyId);
        if (mNotificationManager == null) {
            LogUtils.e(TAG, "cancelNotification() 失败：NotificationManager is null");
            return;
        }
        try {
            mNotificationManager.cancel(notifyId);
            LogUtils.d(TAG, "cancelNotification() 成功 | notifyId=" + notifyId);
        } catch (Exception e) {
            LogUtils.e(TAG, "cancelNotification() 异常 | notifyId=" + notifyId, e);
        }
    }

    /**
     * 取消所有通知（兜底场景使用）
     */
    public void cancelAllNotifications() {
        LogUtils.d(TAG, "cancelAllNotifications() 方法调用");
        if (mNotificationManager == null) {
            LogUtils.e(TAG, "cancelAllNotifications() 失败：NotificationManager is null");
            return;
        }
        try {
            mNotificationManager.cancelAll();
            LogUtils.d(TAG, "cancelAllNotifications() 成功");
        } catch (Exception e) {
            LogUtils.e(TAG, "cancelAllNotifications() 异常", e);
        }
    }

    // ================================== 内部辅助方法（通知构建：前台服务通知）=================================
    /**
     * 构建前台服务通知（全版本无铃声+无振动）
     * @param message 通知内容
     * @return 构建完成的前台通知实例
     */
    private Notification buildForegroundNotification(String message) {
        LogUtils.d(TAG, "buildForegroundNotification() 方法调用 | message=" + message);
        if (mContext == null) {
            LogUtils.e(TAG, "buildForegroundNotification() 失败：mContext is null");
            return null;
        }

        // 内容兜底
        String title = FOREGROUND_NOTIFY_TITLE_DEFAULT;
        String content = (message != null && !message.isEmpty()) ? message : FOREGROUND_NOTIFY_CONTENT_DEFAULT;
        LogUtils.d(TAG, "buildForegroundNotification() 内容兜底完成 | title=" + title + " | content=" + content);

        Notification.Builder builder;
        // API分级构建
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(mContext, CHANNEL_ID_FOREGROUND);
            LogUtils.d(TAG, "buildForegroundNotification() 使用API26+渠道构建");
        } else {
            builder = new Notification.Builder(mContext);
            builder.setSound(null);
            builder.setVibrate(new long[]{0});
            builder.setDefaults(0);
            LogUtils.d(TAG, "buildForegroundNotification() 使用API<26手动配置");
        }

        // 通用配置
        builder.setSmallIcon(NOTIFICATION_DEFAULT_ICON)
			.setContentTitle(title)
			.setContentText(content)
			.setAutoCancel(false)
			.setOngoing(true)
			.setWhen(System.currentTimeMillis())
			.setContentIntent(createJumpPendingIntent(mContext, PENDING_INTENT_REQUEST_CODE_FOREGROUND));

        // API21+ 新增大图标+主题色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setLargeIcon(getAppIcon(mContext))
				.setColor(mContext.getResources().getColor(R.color.colorPrimary))
				.setPriority(Notification.PRIORITY_LOW);
            LogUtils.d(TAG, "buildForegroundNotification() 补充API21+配置");
        }

        Notification notification = builder.build();
        LogUtils.d(TAG, "buildForegroundNotification() 成功构建前台通知");
        return notification;
    }

    // ================================== 内部辅助方法（通知构建：临时通知）=================================
    /**
     * 构建临时通知（全版本系统默认铃声+无振动）
     * @param context 上下文
     * @param message 通知内容
     * @return 构建完成的临时通知实例
     */
    private Notification buildTemporaryNotification(Context context, String message) {
        LogUtils.d(TAG, "buildTemporaryNotification() 方法调用 | context=" + context + " | message=" + message);
        if (context == null) {
            LogUtils.e(TAG, "buildTemporaryNotification() 失败：context is null");
            return null;
        }

        // 内容兜底
        String title = TEMPORARY_NOTIFY_TITLE_DEFAULT;
        String content = (message != null && !message.isEmpty()) ? message : TEMPORARY_NOTIFY_CONTENT_DEFAULT;
        LogUtils.d(TAG, "buildTemporaryNotification() 内容兜底完成 | title=" + title + " | content=" + content);

        Notification.Builder builder;
        // API分级构建
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, CHANNEL_ID_TEMPORARY);
            LogUtils.d(TAG, "buildTemporaryNotification() 使用API26+渠道构建");
        } else {
            builder = new Notification.Builder(context);
            builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
            builder.setVibrate(new long[]{0});
            builder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);
            LogUtils.d(TAG, "buildTemporaryNotification() 使用API<26手动配置");
        }

        // 通用配置
        builder.setSmallIcon(NOTIFICATION_DEFAULT_ICON)
			.setContentTitle(title)
			.setContentText(content)
			.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), Notification.AUDIO_ATTRIBUTES_DEFAULT)
			.setAutoCancel(true)
			.setOngoing(false)
			.setWhen(System.currentTimeMillis())
			.setContentIntent(createJumpPendingIntent(context, PENDING_INTENT_REQUEST_CODE_TEMPORARY));

        // API21+ 新增大图标+主题色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setLargeIcon(getAppIcon(context))
				.setColor(context.getResources().getColor(R.color.colorPrimary))
				.setPriority(Notification.PRIORITY_DEFAULT);
            LogUtils.d(TAG, "buildTemporaryNotification() 补充API21+配置");
        }

        Notification notification = builder.build();
        LogUtils.d(TAG, "buildTemporaryNotification() 成功构建临时通知");
        return notification;
    }

    // ================================== 内部辅助方法（创建跳转PendingIntent，API30安全适配）=================================
    /**
     * 创建跳转MainActivity的PendingIntent，API23+ 添加IMMUTABLE标记
     * @param context 上下文
     * @param requestCode 请求码
     * @return 构建完成的PendingIntent
     */
    private PendingIntent createJumpPendingIntent(Context context, int requestCode) {
        LogUtils.d(TAG, "createJumpPendingIntent() 方法调用 | requestCode=" + requestCode + " | context=" + context);
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        LogUtils.d(TAG, "createJumpPendingIntent() 跳转Intent配置完成");

        // API23+ 必需添加IMMUTABLE，适配API30安全规范
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
            LogUtils.d(TAG, "createJumpPendingIntent() 添加FLAG_IMMUTABLE标记（API23+）");
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, intent, flags);
        LogUtils.d(TAG, "createJumpPendingIntent() 成功 | requestCode=" + requestCode);
        return pendingIntent;
    }

    // ================================== 内部辅助方法（获取APP图标，异常兜底）=================================
    /**
     * 获取APP图标，失败返回默认图标
     * @param context 上下文
     * @return APP图标Bitmap实例
     */
    private Bitmap getAppIcon(Context context) {
        LogUtils.d(TAG, "getAppIcon() 方法调用 | context=" + context);
        try {
            PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            Bitmap appIcon = BitmapFactory.decodeResource(context.getResources(), pkgInfo.applicationInfo.icon);
            LogUtils.d(TAG, "getAppIcon() 成功：获取应用图标");
            return appIcon;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(TAG, "getAppIcon() 异常：获取应用图标失败，使用默认图标", e);
            return BitmapFactory.decodeResource(context.getResources(), NOTIFICATION_DEFAULT_ICON);
        }
    }

    // ================================== 资源释放方法（避免内存泄漏）=================================
    /**
     * 释放资源，销毁时调用
     */
    public void release() {
        LogUtils.d(TAG, "release() 方法调用：执行资源释放");
        cancelForegroundServiceNotify();
        mNotificationManager = null;
        mContext = null;
        mSp = null; // ********** 新增：释放SP实例 **********
        LogUtils.d(TAG, "release() 成功：所有资源已释放");
    }

    // ================================== 对外 getter 方法（仅前台通知实例，只读）=================================
    public Notification getForegroundServiceNotify() {
        return mForegroundServiceNotify;
    }
}

