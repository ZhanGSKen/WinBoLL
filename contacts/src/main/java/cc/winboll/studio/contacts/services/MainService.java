package cc.winboll.studio.contacts.services;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import cc.winboll.studio.contacts.bobulltoon.TomCat;
import cc.winboll.studio.contacts.dun.Rules;
import cc.winboll.studio.contacts.handlers.MainServiceHandler;
import cc.winboll.studio.contacts.listenphonecall.CallListenerService;
import cc.winboll.studio.contacts.model.MainServiceBean;
import cc.winboll.studio.contacts.model.RingTongBean;
import cc.winboll.studio.contacts.receivers.MainReceiver;
import cc.winboll.studio.contacts.utils.NotificationManagerUtils;
import cc.winboll.studio.libappbase.LogUtils;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/02/13 06:56:41
 * @Describe 拨号主服务，负责核心业务逻辑、守护进程绑定、铃声音量监控及通话监听启动
 * 严格适配 Android API 30 + Java 7 语法规范 | 解决前台服务启动超时崩溃
 * 核心优化：1. 移除延迟启动逻辑 2. 标准化日志管理 3. 强化资源清理 4. 结构分层重构
 */
public class MainService extends Service {

    // ====================== 常量定义区（全硬编码，无高版本API依赖） ======================
    public static final String TAG = "MainService";
    public static final int MSG_UPDATE_STATUS = 0;

    // 铃声音量监控参数（定时检查+恢复）
    private static final long VOLUME_CHECK_DELAY = 1000L;    // 首次检查延迟1s
    private static final long VOLUME_CHECK_PERIOD = 60000L; // 后续每60s检查一次

    // 前台服务配置（固定ID+渠道，避免重复创建）
    private static final String FOREGROUND_CHANNEL_ID = "main_service_foreground_channel";
    private static final int FOREGROUND_NOTIFICATION_ID = 1001;
    private static final int FOREGROUND_SERVICE_TYPE_DATA_SYNC = 0x00000001; // dataSync类型硬编码

    // Android版本常量（替代Build.VERSION_CODES，适配Java7）
    private static final int ANDROID_8_API = 26;  // Android 8.0
    private static final int ANDROID_10_API = 29; // Android 10
    private static final int ANDROID_12_API = 31; // Android 12

    // 守护服务重绑定延迟（仅保留核心重试逻辑）
    private static final long RETRY_DELAY_MS = 3000L;

    // ====================== 静态成员属性区（全局共享实例，统一前缀s） ======================
    private static MainService sMainServiceInstance;    // 主服务全局实例
    private static volatile TomCat sTomCatInstance;     // 号码识别核心实例（volatile保证可见性）

    // ====================== 成员属性区（业务+UI+资源，统一前缀m） ======================
    private volatile boolean mIsServiceRunning;         // 服务运行状态标记（volatile防指令重排）
    private MainServiceBean mMainServiceBean;           // 服务配置实体（启用状态存储）
    private MainServiceHandler mMainServiceHandler;     // 服务消息处理器（主线程通信）
    private MyServiceConnection mServiceConnection;     // 守护服务连接实例
    private AssistantService mAssistantService;         // 绑定的守护服务实例
    private boolean mIsAssistantBound;                  // 守护服务绑定状态标记
    private MainReceiver mMainReceiver;                 // 全局广播接收器（监听系统事件）
    private Timer mVolumeCheckTimer;                    // 铃声音量检查定时器（定时恢复配置）

    // ====================== 内部类：Binder（服务绑定通信，优先定义） ======================
    public class MyBinder extends Binder {
        /**
         * 外部组件绑定服务时，获取主服务实例
         * @return MainService 主服务实例
         */
        public MainService getService() {
            LogUtils.d(TAG, "MyBinder.getService: 外部获取主服务实例");
            return MainService.this;
        }
    }

    // ====================== 内部类：ServiceConnection（守护服务绑定回调） ======================
    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service == null) {
                LogUtils.w(TAG, "MyServiceConnection.onServiceConnected: 绑定的IBinder为空，绑定失败");
                mIsAssistantBound = false;
                return;
            }

            try {
                // 类型转换获取守护服务实例
                AssistantService.MyBinder binder = (AssistantService.MyBinder) service;
                mAssistantService = binder.getService();
                mIsAssistantBound = true;
                LogUtils.d(TAG, "MyServiceConnection.onServiceConnected: 守护服务绑定成功");
            } catch (ClassCastException e) {
                LogUtils.e(TAG, "MyServiceConnection.onServiceConnected: IBinder类型转换失败", e);
                mIsAssistantBound = false;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.w(TAG, "MyServiceConnection.onServiceDisconnected: 守护服务连接断开");
            mAssistantService = null;
            mIsAssistantBound = false;

            // 服务启用状态下，重试绑定守护服务（主服务存活核心保障）
            if (mMainServiceBean != null && mMainServiceBean.isEnable()) {
                LogUtils.d(TAG, "MyServiceConnection.onServiceDisconnected: " + RETRY_DELAY_MS + "ms后重试绑定守护服务");
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
						@Override
						public void run() {
							wakeupAndBindAssistantService();
						}
					}, RETRY_DELAY_MS);
            } else {
                LogUtils.w(TAG, "MyServiceConnection.onServiceDisconnected: 主服务已禁用，跳过重试绑定");
            }
        }
    }

    // ====================== 对外静态方法区（服务启停/重启/状态查询，全局调用） ======================
    /**
     * 检查号码是否在BoBullToon库中（外部组件调用，静态入口）
     * @param phone 待查询号码
     * @return true=是BoBullToon号码，false=否/初始化失败
     */
    public static boolean isPhoneInBoBullToon(String phone) {
        if (sTomCatInstance != null && phone != null && !phone.isEmpty()) {
            boolean result = sTomCatInstance.isPhoneBoBullToon(phone);
            LogUtils.d(TAG, "isPhoneInBoBullToon: 号码" + phone + "查询结果：" + (result ? "是" : "否"));
            return result;
        }
        LogUtils.w(TAG, "isPhoneInBoBullToon: TomCat未初始化或号码为空，查询失败");
        return false;
    }

    /**
     * 停止主服务（仅停止，不修改配置）
     * @param context 上下文（非空校验）
     */
    public static void stopMainService(Context context) {
        if (context == null) {
            LogUtils.e(TAG, "stopMainService: 上下文为空，无法停止服务");
            return;
        }
        LogUtils.d(TAG, "stopMainService: 执行停止主服务操作");
        context.stopService(new Intent(context, MainService.class));
    }

    /**
     * 启动主服务（仅启动，不修改配置）
     * @param context 上下文（非空校验）
     */
    public static void startMainService(Context context) {
        if (context == null) {
            LogUtils.e(TAG, "startMainService: 上下文为空，无法启动服务");
            return;
        }
        LogUtils.d(TAG, "startMainService: 执行启动主服务操作（前台服务模式）");
        Intent intent = new Intent(context, MainService.class);
        context.startForegroundService(intent);
    }

    /**
     * 重启主服务（先停后启，需服务已启用）
     * @param context 上下文（非空校验）
     */
    public static void restartMainService(Context context) {
        if (context == null) {
            LogUtils.e(TAG, "restartMainService: 上下文为空，无法重启服务");
            return;
        }
        LogUtils.d(TAG, "restartMainService: 执行主服务重启流程");

        MainServiceBean config = MainServiceBean.loadBean(context, MainServiceBean.class);
        if (config != null && config.isEnable()) {
            stopMainService(context);
            startMainService(context);
            LogUtils.i(TAG, "restartMainService: 主服务重启完成");
        } else {
            LogUtils.w(TAG, "restartMainService: 服务未启用或配置为空，跳过重启");
        }
    }

    /**
     * 停止服务并保存禁用状态（更新配置+停止服务）
     * @param context 上下文（非空校验）
     */
    public static void stopMainServiceAndSaveStatus(Context context) {
        if (context == null) {
            LogUtils.e(TAG, "stopMainServiceAndSaveStatus: 上下文为空，操作失败");
            return;
        }
        LogUtils.d(TAG, "stopMainServiceAndSaveStatus: 保存禁用状态并停止服务");
        MainServiceBean config = new MainServiceBean();
        config.setIsEnable(false);
        MainServiceBean.saveBean(context, config);
        stopMainService(context);
    }

    /**
     * 启动服务并保存启用状态（更新配置+启动服务，先停后启避免重复）
     * @param context 上下文（非空校验）
     */
    public static void startMainServiceAndSaveStatus(Context context) {
        if (context == null) {
            LogUtils.e(TAG, "startMainServiceAndSaveStatus: 上下文为空，操作失败");
            return;
        }
        LogUtils.d(TAG, "startMainServiceAndSaveStatus: 保存启用状态并启动服务");
        MainServiceBean config = new MainServiceBean();
        config.setIsEnable(true);
        MainServiceBean.saveBean(context, config);
        stopMainService(context); // 先停止旧服务，避免冲突
        startMainService(context);
    }

    // ====================== 核心工具方法区（服务状态检查+前台通知创建，通用功能） ======================
    /**
     * 补充消息追加方法（外部组件向服务发送消息）
     * @param message 待追加消息（空值防护）
     */
    public void appenMessage(String message) {
        String msg = message == null ? "null" : message;
        LogUtils.d(TAG, "appenMessage: 接收外部消息：" + msg);

        if (mMainServiceHandler != null) {
            android.os.Message handlerMsg = android.os.Message.obtain();
            handlerMsg.what = MSG_UPDATE_STATUS;
            handlerMsg.obj = msg;
            mMainServiceHandler.sendMessage(handlerMsg);
            LogUtils.d(TAG, "appenMessage: 消息已发送至Handler处理");
        } else {
            LogUtils.w(TAG, "appenMessage: MainServiceHandler未初始化，消息发送失败");
        }
    }

    /**
     * 创建前台服务通知（Android8.0+需渠道，低版本兼容）
     * @return Notification 前台服务通知实例
     */
//    private Notification createForegroundNotification() {
//        // 1. Android8.0+创建通知渠道（必需，否则通知不显示）
//        if (Build.VERSION.SDK_INT >= ANDROID_8_API) {
//            NotificationChannel channel = new NotificationChannel(
//				FOREGROUND_CHANNEL_ID,
//				"拨号主服务",
//				NotificationManager.IMPORTANCE_LOW
//            );
//            channel.setDescription("主服务后台运行，保障通话监听与号码识别功能正常");
//            channel.setSound(null, null); // 关闭通知声音
//            channel.enableVibration(false); // 关闭振动
//
//            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            if (notificationManager != null) {
//                notificationManager.createNotificationChannel(channel);
//                LogUtils.d(TAG, "createForegroundNotification: Android8.0+通知渠道创建成功");
//            } else {
//                LogUtils.e(TAG, "createForegroundNotification: NotificationManager获取失败，渠道创建失败");
//            }
//        }
//
//        // 2. 构建通知实例（分版本兼容Builder）
//        Notification.Builder builder;
//        if (Build.VERSION.SDK_INT >= ANDROID_8_API) {
//            builder = new Notification.Builder(this, FOREGROUND_CHANNEL_ID);
//        } else {
//            builder = new Notification.Builder(this);
//        }
//        builder.setSmallIcon(R.drawable.ic_launcher);
//        builder.setContentTitle("拨号服务运行中");
//        builder.setContentText("后台保障通话监听与号码识别，请勿手动关闭");
//        builder.setPriority(Notification.PRIORITY_LOW); // 低优先级，不打扰用户
//        builder.setOngoing(true); // 不可手动清除，保障服务存活
//
//        LogUtils.d(TAG, "createForegroundNotification: 前台服务通知构建完成");
//        return builder.build();
//    }

    /**
     * 检查指定服务是否正在运行（通过ActivityManager查询）
     * @param serviceClass 待检查服务类
     * @return true=运行中，false=未运行/查询失败
     */
    private boolean isServiceRunning(Class<?> serviceClass) {
        if (serviceClass == null) {
            LogUtils.e(TAG, "isServiceRunning: 服务类为空，检查失败");
            return false;
        }

        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            LogUtils.w(TAG, "isServiceRunning: ActivityManager获取失败，检查失败");
            return false;
        }

        // 遍历运行中服务，匹配类名
        for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                LogUtils.d(TAG, "isServiceRunning: 服务" + serviceClass.getSimpleName() + "正在运行");
                return true;
            }
        }
        LogUtils.d(TAG, "isServiceRunning: 服务" + serviceClass.getSimpleName() + "未运行");
        return false;
    }

    // ====================== Service生命周期方法区（按执行顺序：创建→绑定→启动→销毁） ======================
    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "===== onCreate: 主服务开始创建 =====");
        sMainServiceInstance = this;
        mIsServiceRunning = false;

        // 初始化核心组件（无延迟，直接初始化）
        mMainServiceBean = MainServiceBean.loadBean(this, MainServiceBean.class);
        mServiceConnection = new MyServiceConnection();
        mMainServiceHandler = new MainServiceHandler(this);

        // 初始化音量监控定时器（服务启动即开启，保障音量配置）
        initVolumeCheckTimer();

        // 执行核心业务启动逻辑（无延迟，优先启动）
        startCoreBusiness();

        LogUtils.d(TAG, "===== onCreate: 主服务创建完成 =====");
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.d(TAG, "onBind: 主服务被外部组件绑定，Intent=" + (intent != null ? intent.getAction() : "null"));
        return new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "onStartCommand: 主服务被启动，startId=" + startId);
        // 重复启动时再次执行核心业务（避免服务被杀死后重启失败）
        startCoreBusiness();

        // 服务启用则返回START_STICKY（系统杀死后自动重启），否则默认行为
        int result = (mMainServiceBean != null && mMainServiceBean.isEnable()) ? START_STICKY : super.onStartCommand(intent, flags, startId);
        LogUtils.d(TAG, "onStartCommand: 服务启动模式：" + (result == START_STICKY ? "START_STICKY（自动重启）" : "默认模式"));
        return result;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "===== onDestroy: 主服务开始销毁，全量清理资源 =====");

        // 1. 停止音量监控定时器（释放线程资源）
        cancelVolumeCheckTimer();

        // 2. 解除守护服务绑定（避免内存泄漏）
        if (mIsAssistantBound && mServiceConnection != null) {
            try {
                unbindService(mServiceConnection);
                LogUtils.d(TAG, "onDestroy: 守护服务绑定已解除");
            } catch (IllegalArgumentException e) {
                LogUtils.w(TAG, "onDestroy: 解除守护服务绑定失败（服务未绑定）", e);
            }
            mIsAssistantBound = false;
            mServiceConnection = null;
        }

        // 3. 注销广播接收器（释放系统资源）
        if (mMainReceiver != null) {
            mMainReceiver.unregisterAction(this);
            mMainReceiver = null;
            LogUtils.d(TAG, "onDestroy: 全局广播接收器已注销");
        }

        // 4. 停止所有子服务（强制停止，避免子服务残留）
        stopService(new Intent(this, AssistantService.class));
        stopService(new Intent(this, CallListenerService.class));
        stopService(new Intent(this, MyCallScreeningService.class));
        LogUtils.d(TAG, "onDestroy: 所有子服务已强制停止");

        // 5. 清空所有引用（静态+成员，彻底避免内存泄漏，不修改配置Bean）
        sMainServiceInstance = null;
        sTomCatInstance = null;
        mMainServiceHandler = null;
        mAssistantService = null;
        mMainServiceBean = null;

        // 标记服务为未运行
        mIsServiceRunning = false;
        LogUtils.d(TAG, "===== onDestroy: 主服务销毁完成，资源全清理 =====");
    }

    // ====================== 核心业务逻辑区（服务启动核心流程，无延迟） ======================
    /**
     * 启动核心业务（主服务核心入口，避免重复启动）
     */
    private synchronized void startCoreBusiness() {
        // 服务已运行则直接返回，避免重复执行启动流程
        if (mIsServiceRunning) {
            LogUtils.d(TAG, "startCoreBusiness: 主服务已运行，跳过重复启动");
            return;
        }
        mIsServiceRunning = true;

        // 重新加载最新配置（避免配置修改后未生效）
        mMainServiceBean = MainServiceBean.loadBean(this, MainServiceBean.class);
        if (mMainServiceBean == null || !mMainServiceBean.isEnable()) {
            LogUtils.w(TAG, "startCoreBusiness: 服务配置未启用或配置为空，启动流程终止");
            mIsServiceRunning = false;
            stopSelf(); // 未启用则主动停止服务
            return;
        }

        LogUtils.i(TAG, "startCoreBusiness: 服务配置已启用，启动核心流程");

        // 1. 优先启动前台服务（避免前台服务启动超时崩溃，核心优先级）
        try {
			NotificationManagerUtils notificationManagerUtils = new NotificationManagerUtils(this);
			notificationManagerUtils.startForegroundServiceNotify(this, "主要拨号服务已启动。");
            LogUtils.i(TAG, "startCoreBusiness: 前台服务启动成功，通知ID=" + FOREGROUND_NOTIFICATION_ID);
        } catch (IllegalArgumentException e) {
            LogUtils.e(TAG, "startCoreBusiness: 前台服务启动失败（服务类型不匹配）", e);
            mIsServiceRunning = false;
            stopSelf();
            return;
        } catch (Exception e) {
            LogUtils.e(TAG, "startCoreBusiness: 前台服务启动异常", e);
            mIsServiceRunning = false;
            stopSelf();
            return;
        }

        // 2. 绑定守护服务（保障主服务存活，防系统杀死）
        wakeupAndBindAssistantService();

        // 3. 初始化核心业务组件（号码识别+黑白名单规则）
        initTomCatComponent();
        initRulesConfig();

        // 4. 启动通话监听相关服务（无延迟，直接启动，保障功能生效）
        startCallRelatedServices();

        LogUtils.i(TAG, "startCoreBusiness: 主服务核心流程启动完成，服务正常运行");
    }

    /**
     * 唤醒并绑定守护服务（分版本适配启动方式，Android10+前台服务启动）
     */
    private void wakeupAndBindAssistantService() {
        if (mServiceConnection == null) {
            LogUtils.e(TAG, "wakeupAndBindAssistantService: 服务连接实例未初始化，绑定失败");
            return;
        }

        Intent assistantIntent = new Intent(this, AssistantService.class);
        // Android10+应用后台启动服务需用前台服务模式
        LogUtils.d(TAG, "wakeupAndBindAssistantService: Android10+，前台服务模式启动守护服务");
		startService(assistantIntent);

        // 绑定守护服务（BIND_IMPORTANT：高优先级绑定，断开时回调）
        bindService(assistantIntent, mServiceConnection, Context.BIND_IMPORTANT);
        LogUtils.d(TAG, "wakeupAndBindAssistantService: 守护服务启动并发起绑定请求");
    }

    // ====================== 铃声音量监控方法区（定时检查+恢复配置） ======================
    /**
     * 初始化音量检查定时器（先取消旧定时器，避免重复创建）
     */
    private void initVolumeCheckTimer() {
        cancelVolumeCheckTimer();
        mVolumeCheckTimer = new Timer();
        mVolumeCheckTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					checkAndRestoreRingerVolume();
				}
			}, VOLUME_CHECK_DELAY, VOLUME_CHECK_PERIOD);
        LogUtils.d(TAG, "initVolumeCheckTimer: 铃声音量监控定时器启动，周期" + VOLUME_CHECK_PERIOD + "ms");
    }

    /**
     * 检查并恢复铃声音量（对比配置值，不一致则恢复，保障用户配置生效）
     */
    private void checkAndRestoreRingerVolume() {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager == null) {
            LogUtils.e(TAG, "checkAndRestoreRingerVolume: AudioManager获取失败，音量检查终止");
            return;
        }

        // 加载音量配置（无配置则初始化默认值）
        RingTongBean ringConfig = RingTongBean.loadBean(this, RingTongBean.class);
        if (ringConfig == null) {
            ringConfig = new RingTongBean();
            RingTongBean.saveBean(this, ringConfig);
            LogUtils.d(TAG, "checkAndRestoreRingerVolume: 音量配置未存在，初始化默认配置");
            return;
        }

        try {
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
            int configVolume = ringConfig.getStreamVolume();
            // 音量不一致则恢复配置值
            if (currentVolume != configVolume) {
                audioManager.setStreamVolume(AudioManager.STREAM_RING, configVolume, 0);
                LogUtils.d(TAG, "checkAndRestoreRingerVolume: 铃声音量已恢复，配置值=" + configVolume + "，原当前值=" + currentVolume);
			} else {
                LogUtils.v(TAG, "checkAndRestoreRingerVolume: 铃声音量与配置一致，无需调整");
            }
        } catch (SecurityException e) {
            LogUtils.e(TAG, "checkAndRestoreRingerVolume: 音量设置权限不足，恢复失败", e);
        }
    }

    /**
     * 取消音量检查定时器（释放Timer资源，避免内存泄漏）
     */
    private void cancelVolumeCheckTimer() {
        if (mVolumeCheckTimer != null) {
            mVolumeCheckTimer.cancel();
            mVolumeCheckTimer = null;
            LogUtils.d(TAG, "cancelVolumeCheckTimer: 铃声音量监控定时器已取消");
        }
    }

    // ====================== 辅助初始化方法区（业务组件初始化，统一归类） ======================
    /**
     * 初始化TomCat组件（号码识别核心，加载本地数据）
     */
    private void initTomCatComponent() {
        sTomCatInstance = TomCat.getInstance(this);
        if (sTomCatInstance.loadPhoneBoBullToon()) {
            LogUtils.d(TAG, "initTomCatComponent: BoBullToon号码库加载成功");
        } else {
            LogUtils.w(TAG, "initTomCatComponent: BoBullToon号码库未下载，加载失败（不影响服务运行）");
        }
    }

    /**
     * 初始化黑白名单规则配置（加载本地规则，保障通话筛选生效）
     */
    private void initRulesConfig() {
        Rules rules = Rules.getInstance(this);
        if (rules != null) {
            rules.loadRules();
            LogUtils.d(TAG, "initRulesConfig: 黑白名单通话规则加载完成");
        } else {
            LogUtils.e(TAG, "initRulesConfig: Rules实例获取失败，通话规则加载失败");
        }
    }

    /**
     * 初始化广播接收器（监听系统事件，如开机启动、网络变化）
     */
    private void initMainReceiver() {
        if (mMainReceiver == null) {
            mMainReceiver = new MainReceiver(this);
            mMainReceiver.registerAction(this);
            LogUtils.d(TAG, "initMainReceiver: 全局广播接收器注册完成");
        } else {
            LogUtils.d(TAG, "initMainReceiver: 广播接收器已初始化，跳过重复注册");
        }
    }

    /**
     * 启动通话相关服务（通话监听+通话筛选，核心功能服务）
     */
    private void startCallRelatedServices() {
        // 启动通话监听服务
        try {
            Intent callListenerIntent = new Intent(this, CallListenerService.class);
            startService(callListenerIntent);
            LogUtils.d(TAG, "startCallRelatedServices: CallListenerService启动成功");
        } catch (Exception e) {
            LogUtils.e(TAG, "startCallRelatedServices: CallListenerService启动失败", e);
        }

        // 启动通话筛选服务（API10+生效，低版本兼容）
        try {
            Intent screeningIntent = new Intent(this, MyCallScreeningService.class);
            startService(screeningIntent);
            LogUtils.d(TAG, "startCallRelatedServices: MyCallScreeningService启动成功");
        } catch (Exception e) {
            LogUtils.e(TAG, "startCallRelatedServices: MyCallScreeningService启动失败", e);
        }
    }
}

