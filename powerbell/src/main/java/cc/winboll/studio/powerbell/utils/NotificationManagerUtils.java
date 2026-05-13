package cc.winboll.studio.powerbell.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.provider.Settings;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.MainActivity;
import cc.winboll.studio.powerbell.R;
import cc.winboll.studio.powerbell.models.NotificationMessage;

/**
 * 通知工具类：统一管理前台服务/电池提醒/应用配置信息通知
 * 适配：API19-30 | Java7 | 小米手机
 * 特性：前台服务无铃声、提醒通知系统默认铃声、配置通知系统默认铃声无振动、API分级适配、内存泄漏防护
 */
public class NotificationManagerUtils {
    // ================================== 静态常量（置顶统一管理，杜绝魔法值）=================================
    public static final String TAG = "NotificationManagerUtils";
    // 通知渠道ID（API26+ 必需，区分通知类型）
    public static final String CHANNEL_ID_FOREGROUND = "cc.winboll.studio.powerbell.channel.foreground";
    public static final String CHANNEL_ID_REMIND = "cc.winboll.studio.powerbell.channel.remind";
    public static final String CHANNEL_ID_CONFIG = "cc.winboll.studio.powerbell.channel.config"; // 新增：应用配置信息渠道
    // 通知ID（唯一标识，避免重复）
    public static final int NOTIFY_ID_FOREGROUND_SERVICE = 1001;
    public static final int NOTIFY_ID_REMIND = 1002;
    public static final int NOTIFY_ID_CONFIG = 1003; // 新增：应用配置信息通知ID
    // 低版本兼容：默认通知图标（API<21 避免显示异常）
    private static final int NOTIFICATION_DEFAULT_ICON = R.drawable.ic_launcher;
    // 通知内容兜底常量
    private static final String FOREGROUND_NOTIFY_TITLE_DEFAULT = "电池服务运行中";
    private static final String FOREGROUND_NOTIFY_CONTENT_DEFAULT = "后台监测电池状态";
    private static final String REMIND_NOTIFY_TITLE_DEFAULT = "电池状态提醒";
    private static final String REMIND_NOTIFY_CONTENT_DEFAULT = "电池状态异常，请及时处理";
    private static final String CONFIG_NOTIFY_TITLE_DEFAULT = "应用配置更新"; // 新增：配置通知默认标题
    private static final String CONFIG_NOTIFY_CONTENT_DEFAULT = "配置信息已更新，生效中"; // 新增：配置通知默认内容
    // PendingIntent请求码
    private static final int PENDING_INTENT_REQUEST_CODE_FOREGROUND = 0;
    private static final int PENDING_INTENT_REQUEST_CODE_REMIND = 1;
    private static final int PENDING_INTENT_REQUEST_CODE_CONFIG = 2; // 新增：配置通知请求码
	private static int snMessageNotificationID = 10000;

    // ================================== 成员变量（私有封装，按依赖优先级排序）=================================
    // 核心上下文（应用级，避免内存泄漏）
    private Context mContext;
    // 系统通知服务（核心依赖）
    private NotificationManager mNotificationManager;
    // 前台服务通知实例（单独持有，便于更新/取消）
    private Notification mForegroundServiceNotify;

    // ================================== 构造方法（初始化核心资源，前置校验）=================================
    public NotificationManagerUtils(Context context) {
        LogUtils.d(TAG, "NotificationManagerUtils() 构造 | context=" + context);
        // 前置校验：Context非空
        if (context == null) {
            LogUtils.e(TAG, "NotificationManagerUtils() 构造失败：context is null");
            return;
        }
        // 初始化核心资源
        this.mContext = context.getApplicationContext();
        this.mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        LogUtils.d(TAG, "NotificationManagerUtils() 核心资源初始化完成 | mContext=" + mContext + " | mNotificationManager=" + mNotificationManager);
        // 初始化通知渠道（API26+ 必需）
        initNotificationChannels();
        LogUtils.d(TAG, "NotificationManagerUtils() 构造完成");
    }

    // ================================== 核心初始化方法（通知渠道，API分级适配）=================================
    /**
     * 初始化通知渠道：前台服务渠道（无铃声+无振动）、提醒渠道（系统默认铃声+无振动）、配置信息渠道（系统默认铃声+无振动）
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

        // 1. 前台服务渠道（低优先级，后台保活无打扰）
        NotificationChannel foregroundChannel = new NotificationChannel(
			CHANNEL_ID_FOREGROUND,
			"电池服务保活",
			NotificationManager.IMPORTANCE_LOW
        );
        foregroundChannel.setDescription("电池监测服务后台运行，无声音、无振动");
        foregroundChannel.enableLights(false);
        foregroundChannel.enableVibration(false);
        foregroundChannel.setSound(null, null); // 强制无铃声
        foregroundChannel.setShowBadge(false);
        foregroundChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
        LogUtils.d(TAG, "initNotificationChannels() 前台服务渠道配置完成");

        // 2. 电池提醒渠道（中优先级，系统默认铃声，无振动）
        NotificationChannel remindChannel = new NotificationChannel(
			CHANNEL_ID_REMIND,
			"电池状态提醒",
			NotificationManager.IMPORTANCE_DEFAULT
        );
        remindChannel.setDescription("电池满电/低电量提醒，系统默认铃声，无振动");
        remindChannel.enableLights(true);
        remindChannel.enableVibration(false);
        remindChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), Notification.AUDIO_ATTRIBUTES_DEFAULT);
        remindChannel.setShowBadge(false);
        remindChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        LogUtils.d(TAG, "initNotificationChannels() 电池提醒渠道配置完成");

        // 3. 应用配置信息渠道（方案1修复：默认优先级，系统默认铃声，无振动）
        NotificationChannel configChannel = new NotificationChannel(
			CHANNEL_ID_CONFIG,
			"应用配置信息",
			NotificationManager.IMPORTANCE_DEFAULT
        );
        configChannel.setDescription("应用配置更新、参数变更等提示，系统默认铃声、无振动");
        configChannel.enableLights(true);
        configChannel.enableVibration(false);
        configChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), Notification.AUDIO_ATTRIBUTES_DEFAULT);
        configChannel.setShowBadge(false);
        configChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        LogUtils.d(TAG, "initNotificationChannels() 应用配置信息渠道配置完成");

        // 注册渠道到系统
        mNotificationManager.createNotificationChannel(foregroundChannel);
        mNotificationManager.createNotificationChannel(remindChannel);
        mNotificationManager.createNotificationChannel(configChannel); // 注册新增渠道
        LogUtils.d(TAG, "initNotificationChannels() 成功：创建前台服务+电池提醒+应用配置信息渠道");
    }

    // ================================== 对外核心方法（前台服务通知：启动/更新/取消）=================================
    /**
     * 启动前台服务通知（API30适配，无铃声）
     */
    public void startForegroundServiceNotify(Service service, NotificationMessage message) {
        LogUtils.d(TAG, "startForegroundServiceNotify() 执行 | notifyId=" + NOTIFY_ID_FOREGROUND_SERVICE + " | service=" + service + " | message=" + message);
        // 前置校验：参数非空
        if (service == null || message == null || mNotificationManager == null) {
            LogUtils.e(TAG, "startForegroundServiceNotify() 失败：param is null | service=" + service + " | message=" + message + " | mNotificationManager=" + mNotificationManager);
            return;
        }

        // 构建前台通知
        mForegroundServiceNotify = buildForegroundNotification(message);
        if (mForegroundServiceNotify == null) {
            LogUtils.e(TAG, "startForegroundServiceNotify() 失败：构建通知为空");
            return;
        }

        // 启动前台服务（API30无FOREGROUND_SERVICE_TYPE限制，全版本通用）
        try {
            service.startForeground(NOTIFY_ID_FOREGROUND_SERVICE, mForegroundServiceNotify);
            LogUtils.d(TAG, "startForegroundServiceNotify() 成功");
        } catch (Exception e) {
            LogUtils.e(TAG, "startForegroundServiceNotify() 异常", e);
        }
    }

    /**
     * 更新前台服务通知内容（复用通知ID，保持无铃声）
     */
    public void updateForegroundServiceNotify(NotificationMessage message) {
        LogUtils.d(TAG, "updateForegroundServiceNotify() 执行 | notifyId=" + NOTIFY_ID_FOREGROUND_SERVICE + " | message=" + message);
        if (message == null || mNotificationManager == null) {
            LogUtils.e(TAG, "updateForegroundServiceNotify() 失败：param is null | message=" + message + " | mNotificationManager=" + mNotificationManager);
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
        LogUtils.d(TAG, "cancelForegroundServiceNotify() 执行 | notifyId=" + NOTIFY_ID_FOREGROUND_SERVICE);
        cancelNotification(NOTIFY_ID_FOREGROUND_SERVICE);
        mForegroundServiceNotify = null; // 置空释放
        LogUtils.d(TAG, "cancelForegroundServiceNotify() 成功");
    }

    // ================================== 对外核心方法（电池提醒通知：发送）=================================
    /**
     * 发送电池提醒通知（系统默认铃声，无振动）
     */
    public void showRemindNotification(Context context, NotificationMessage message) {
        LogUtils.d(TAG, "showRemindNotification() 执行 | notifyId=" + NOTIFY_ID_REMIND + " | context=" + context + " | message=" + message);
        if (context == null || message == null || mNotificationManager == null) {
            LogUtils.e(TAG, "showRemindNotification() 失败：param is null | context=" + context + " | message=" + message + " | mNotificationManager=" + mNotificationManager);
            return;
        }

        Notification remindNotify = buildRemindNotification(context, message);
        if (remindNotify == null) {
            LogUtils.e(TAG, "showRemindNotification() 失败：构建通知为空");
            return;
        }

        try {
            mNotificationManager.notify(NOTIFY_ID_REMIND, remindNotify);
            LogUtils.d(TAG, "showRemindNotification() 成功");
        } catch (Exception e) {
            LogUtils.e(TAG, "showRemindNotification() 异常", e);
        }
    }

	public synchronized void showMessageNotification(Context context, NotificationMessage message) {
		snMessageNotificationID++;
        LogUtils.d(TAG, "showMessageNotification() 执行 | notifyId=" + snMessageNotificationID + " | context=" + context + " | message=" + message);
        if (context == null || message == null || mNotificationManager == null) {
            LogUtils.e(TAG, "showMessageNotification() 失败：param is null | context=" + context + " | message=" + message + " | mNotificationManager=" + mNotificationManager);
            return;
        }

        Notification configNotify = buildConfigNotification(context, message);
        if (configNotify == null) {
            LogUtils.e(TAG, "showMessageNotification() 失败：构建通知为空");
            return;
        }

        try {
            mNotificationManager.notify(snMessageNotificationID, configNotify);
            LogUtils.d(TAG, "showMessageNotification() 成功");
        } catch (Exception e) {
            LogUtils.e(TAG, "showMessageNotification() 异常", e);
        }
    }

    // ================================== 对外核心方法（应用配置信息通知：发送）=================================
    /**
     * 发送应用配置信息通知（方案1修复：系统默认铃声，无振动）
     */
    public void showConfigNotification(Context context, NotificationMessage message) {
        LogUtils.d(TAG, "showConfigNotification() 执行 | notifyId=" + NOTIFY_ID_CONFIG + " | context=" + context + " | message=" + message);
        if (context == null || message == null || mNotificationManager == null) {
            LogUtils.e(TAG, "showConfigNotification() 失败：param is null | context=" + context + " | message=" + message + " | mNotificationManager=" + mNotificationManager);
            return;
        }

        Notification configNotify = buildConfigNotification(context, message);
        if (configNotify == null) {
            LogUtils.e(TAG, "showConfigNotification() 失败：构建通知为空");
            return;
        }

        try {
            mNotificationManager.notify(NOTIFY_ID_CONFIG, configNotify);
            LogUtils.d(TAG, "showConfigNotification() 成功");
        } catch (Exception e) {
            LogUtils.e(TAG, "showConfigNotification() 异常", e);
        }
    }

    // ================================== 对外工具方法（通知取消：单个/全部）=================================
    /**
     * 取消指定ID的通知
     */
    public void cancelNotification(int notifyId) {
        LogUtils.d(TAG, "cancelNotification() 执行 | notifyId=" + notifyId);
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
        LogUtils.d(TAG, "cancelAllNotifications() 执行");
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
     */
    private Notification buildForegroundNotification(NotificationMessage message) {
        LogUtils.d(TAG, "buildForegroundNotification() 执行 | message=" + message);
        if (message == null || mContext == null) {
            LogUtils.e(TAG, "buildForegroundNotification() 失败：param is null | message=" + message + " | mContext=" + mContext);
            return null;
        }

        // 内容兜底
        String title = message.getTitle() != null && !message.getTitle().isEmpty() ? message.getTitle() : FOREGROUND_NOTIFY_TITLE_DEFAULT;
        String content = message.getContent() != null && !message.getContent().isEmpty() ? message.getContent() : FOREGROUND_NOTIFY_CONTENT_DEFAULT;
        LogUtils.d(TAG, "buildForegroundNotification() 内容兜底完成 | title=" + title + " | content=" + content);

        Notification.Builder builder;
        // API分级构建
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // API26+：绑定前台渠道（渠道已配置无铃声）
            builder = new Notification.Builder(mContext, CHANNEL_ID_FOREGROUND);
            LogUtils.d(TAG, "buildForegroundNotification() 使用API26+渠道构建");
        } else {
            // API<26：直接构建，手动禁用铃声振动
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
			.setOngoing(true) // 不可手动关闭
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

    // ================================== 内部辅助方法（通知构建：电池提醒通知）=================================
    /**
     * 构建电池提醒通知（全版本系统默认铃声+无振动）
     */
    private Notification buildRemindNotification(Context context, NotificationMessage message) {
        LogUtils.d(TAG, "buildRemindNotification() 执行 | context=" + context + " | message=" + message);
        if (context == null || message == null) {
            LogUtils.e(TAG, "buildRemindNotification() 失败：param is null | context=" + context + " | message=" + message);
            return null;
        }

        // 内容兜底
        String title = message.getTitle() != null && !message.getTitle().isEmpty() ? message.getTitle() : REMIND_NOTIFY_TITLE_DEFAULT;
        String content = message.getContent() != null && !message.getContent().isEmpty() ? message.getContent() : REMIND_NOTIFY_CONTENT_DEFAULT;
        LogUtils.d(TAG, "buildRemindNotification() 内容兜底完成 | title=" + title + " | content=" + content);

        Notification.Builder builder;
        // API分级构建
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // API26+：绑定提醒渠道（渠道已配置默认铃声）
            builder = new Notification.Builder(context, CHANNEL_ID_REMIND);
            LogUtils.d(TAG, "buildRemindNotification() 使用API26+渠道构建");
        } else {
            // API<26：手动配置默认铃声，关闭振动
            builder = new Notification.Builder(context);
            builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI) // 显式默认铃声
				.setVibrate(new long[]{0})
				.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);
            LogUtils.d(TAG, "buildRemindNotification() 使用API<26手动配置");
        }

        // 通用配置
        builder.setSmallIcon(NOTIFICATION_DEFAULT_ICON)
			.setContentTitle(title)
			.setContentText(content)
			.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), Notification.AUDIO_ATTRIBUTES_DEFAULT)
			.setAutoCancel(true) // 点击关闭
			.setOngoing(false)
			.setWhen(System.currentTimeMillis())
			.setContentIntent(createJumpPendingIntent(context, PENDING_INTENT_REQUEST_CODE_REMIND));

        // API21+ 新增大图标+主题色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setLargeIcon(getAppIcon(context))
				.setColor(context.getResources().getColor(R.color.colorPrimary))
				.setPriority(Notification.PRIORITY_DEFAULT);
            LogUtils.d(TAG, "buildRemindNotification() 补充API21+配置");
        }

        Notification notification = builder.build();
        LogUtils.d(TAG, "buildRemindNotification() 成功构建提醒通知");
        return notification;
    }

    // ================================== 内部辅助方法（通知构建：应用配置信息通知）=================================
    /**
     * 构建应用配置信息通知（方案1修复：全版本系统默认铃声+无振动）
     */
    private Notification buildConfigNotification(Context context, NotificationMessage message) {
        LogUtils.d(TAG, "buildConfigNotification() 执行 | context=" + context + " | message=" + message);
        if (context == null || message == null) {
            LogUtils.e(TAG, "buildConfigNotification() 失败：param is null | context=" + context + " | message=" + message);
            return null;
        }

        // 内容兜底
        String title = message.getTitle() != null && !message.getTitle().isEmpty() ? message.getTitle() : CONFIG_NOTIFY_TITLE_DEFAULT;
        String content = message.getContent() != null && !message.getContent().isEmpty() ? message.getContent() : CONFIG_NOTIFY_CONTENT_DEFAULT;
        LogUtils.d(TAG, "buildConfigNotification() 内容兜底完成 | title=" + title + " | content=" + content);

        Notification.Builder builder;
        // API分级构建
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // API26+：绑定配置渠道（渠道已配置默认铃声）
            builder = new Notification.Builder(context, CHANNEL_ID_CONFIG);
            LogUtils.d(TAG, "buildConfigNotification() 使用API26+渠道构建");
        } else {
            // API<26：手动配置默认铃声，关闭振动（方案1修复：保留铃声配置，删除冗余DEFAULT_SOUND）
            builder = new Notification.Builder(context);
            builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
            builder.setVibrate(new long[]{0});
            builder.setDefaults(Notification.DEFAULT_LIGHTS);
            LogUtils.d(TAG, "buildConfigNotification() 使用API<26手动配置");
        }

        // 通用配置
        builder.setSmallIcon(NOTIFICATION_DEFAULT_ICON)
			.setContentTitle(title)
			.setContentText(content)
			.setAutoCancel(true) // 点击关闭
			.setOngoing(false)
			.setWhen(System.currentTimeMillis())
			.setContentIntent(createJumpPendingIntent(context, PENDING_INTENT_REQUEST_CODE_CONFIG));

        // API21+ 新增大图标+主题色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setLargeIcon(getAppIcon(context))
				.setColor(context.getResources().getColor(R.color.colorPrimary))
				.setPriority(Notification.PRIORITY_DEFAULT);
            LogUtils.d(TAG, "buildConfigNotification() 补充API21+配置");
        }

        Notification notification = builder.build();
        LogUtils.d(TAG, "buildConfigNotification() 成功构建配置信息通知");
        return notification;
    }

    // ================================== 内部辅助方法（创建跳转PendingIntent，API30安全适配）=================================
    /**
     * 创建跳转MainActivity的PendingIntent，API23+ 添加IMMUTABLE标记（避免安全异常）
     */
    private PendingIntent createJumpPendingIntent(Context context, int requestCode) {
        LogUtils.d(TAG, "createJumpPendingIntent() 执行 | requestCode=" + requestCode + " | context=" + context);
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
     */
    private Bitmap getAppIcon(Context context) {
        LogUtils.d(TAG, "getAppIcon() 执行 | context=" + context);
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
        LogUtils.d(TAG, "release() 执行资源释放");
        cancelForegroundServiceNotify();
        mNotificationManager = null;
        mContext = null;
        LogUtils.d(TAG, "release() 成功：所有资源已释放");
    }

    // ================================== 对外 getter 方法（仅前台通知实例，只读）=================================
    public Notification getForegroundServiceNotify() {
        return mForegroundServiceNotify;
    }
}

