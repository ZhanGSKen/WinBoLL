package cc.winboll.studio.powerbell.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.powerbell.handlers.ControlCenterServiceHandler;
import cc.winboll.studio.powerbell.models.AppConfigBean;
import cc.winboll.studio.powerbell.models.ControlCenterServiceBean;
import cc.winboll.studio.powerbell.models.NotificationMessage;
import cc.winboll.studio.powerbell.receivers.ControlCenterServiceReceiver;
import cc.winboll.studio.powerbell.threads.RemindThread;
import cc.winboll.studio.powerbell.utils.NotificationManagerUtils;
import java.util.List;

/**
 * 电池提醒核心服务
 * 功能：管理前台服务生命周期、控制提醒线程启停、处理配置更新
 * 适配：Java7 | API30 | 前台服务超时防护 | 电池优化忽略引导
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Describe 核心服务：实现电池监测、提醒控制与前台服务保活
 */
public class ControlCenterService extends Service {
    // ====================== 静态常量区（置顶归类，消除魔法值） ======================
    public static final String TAG = "ControlCenterService";
    // 线程与服务常量
    private static final long THREAD_STOP_TIMEOUT = 1000L;
    private static final int SERVICE_RETURN_STICKY = START_STICKY;
    private static final int RUNNING_SERVICE_LIST_LIMIT = 100;
    // 默认配置常量
    private static final int DEFAULT_CHARGE_REMINDER_VALUE = 80;
    private static final int DEFAULT_USAGE_REMINDER_VALUE = 20;
    private static final int DEFAULT_BATTERY_DETECT_INTERVAL = 1000;
    // API版本常量
    private static final int API_LEVEL_26 = Build.VERSION_CODES.O;
    private static final int API_LEVEL_30 = Build.VERSION_CODES.R;
    private static final int API_LEVEL_23 = Build.VERSION_CODES.M;

    // ====================== 静态状态标记（volatile保证多线程可见性） ======================
    private static volatile boolean isServiceRunning = false;
    private static volatile boolean mIsDestroyed = true;

    // ====================== 成员变量区（按功能分层：配置→核心组件→通知相关） ======================
    // 服务控制配置
    private ControlCenterServiceBean mServiceControlBean;
    private AppConfigBean mCurrentConfigBean;
    // 业务核心组件
    private ControlCenterServiceHandler mServiceHandler;
    private ControlCenterServiceReceiver mControlCenterServiceReceiver;
    // 通知相关
    private NotificationManagerUtils mNotificationManager;
    private NotificationMessage mForegroundNotifyMsg;

    // ====================== 服务生命周期方法（按执行顺序：onCreate→onStartCommand→onBind→onDestroy） ======================
    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, String.format("onCreate() 执行 | 线程=%s | 进程ID=%d", Thread.currentThread().getName(), android.os.Process.myPid()));
        runCoreServiceLogic();
        boolean serviceEnabled = mServiceControlBean != null && mServiceControlBean.isEnableService();
        LogUtils.d(TAG, String.format("onCreate() 完成 | 前台状态=%b | 服务启用=%b", isServiceRunning, serviceEnabled));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : "null";
        LogUtils.d(TAG, String.format("onStartCommand() 执行 | startId=%d | action=%s", startId, action));
        loadLatestServiceControlConfig();
        runCoreServiceLogic();

        int returnFlag = (mServiceControlBean != null && mServiceControlBean.isEnableService())
			? SERVICE_RETURN_STICKY
			: super.onStartCommand(intent, flags, startId);
        LogUtils.d(TAG, String.format("onStartCommand() 完成 | 返回策略=%s", returnFlag == SERVICE_RETURN_STICKY ? "START_STICKY" : "DEFAULT"));
        return returnFlag;
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.d(TAG, String.format("onBind() 执行 | intent=%s", intent));
        return null;
    }

    @Override
    public void onDestroy() {
        LogUtils.d(TAG, "onDestroy() 执行：服务销毁流程启动");
        super.onDestroy();

        // 资源释放顺序：前台服务 → 线程 → 广播接收器 → Handler → 通知 → 引用（避免内存泄漏）
        stopForegroundService();
        RemindThread.stopRemindThread();
        releaseBroadcastReceiver();
        destroyHandler();
        releaseNotificationResource();
        clearAllReferences();

        // 状态重置
        mCurrentConfigBean = null;
        mForegroundNotifyMsg = null;
        mServiceHandler = null;
        isServiceRunning = false;
        mIsDestroyed = true;

        LogUtils.d(TAG, "onDestroy() 完成：服务销毁完成");
    }

    // ====================== 核心业务逻辑（独立抽取，统一调用） ======================
    /**
     * 服务核心运行逻辑，在onCreate/onStartCommand复用
     * 避免重复初始化，保证前台服务优先启动
     */
    private synchronized void runCoreServiceLogic() {
        LogUtils.d(TAG, "runCoreServiceLogic() 执行");
        loadLatestServiceControlConfig();

        boolean serviceEnabled = mServiceControlBean != null && mServiceControlBean.isEnableService();
        LogUtils.d(TAG, String.format("runCoreServiceLogic() | 服务启用=%b | 已运行=%b | 已销毁=%b", serviceEnabled, isServiceRunning, mIsDestroyed));

        if (serviceEnabled && !isServiceRunning) {
            isServiceRunning = true;
            mIsDestroyed = false;

            if (initForegroundNotificationImmediately()) {
                loadDefaultConfig();
                initServiceBusinessLogic();
                LogUtils.d(TAG, "runCoreServiceLogic() | 核心组件初始化成功");
            } else {
                LogUtils.e(TAG, "runCoreServiceLogic() | 前台通知初始化失败，终止业务");
                stopForegroundService();
                isServiceRunning = false;
            }
        } else {
            LogUtils.d(TAG, "runCoreServiceLogic() | 无需执行核心逻辑");
        }
    }

    // ====================== 前台通知管理（优先执行，防止API26+前台服务5秒超时） ======================
    /**
     * 立即初始化前台通知，防止API26+前台服务超时异常
     * @return true=成功 false=失败
     */
    private boolean initForegroundNotificationImmediately() {
        LogUtils.d(TAG, "initForegroundNotificationImmediately() 执行");
        try {
            if (mNotificationManager == null) {
                mNotificationManager = new NotificationManagerUtils(this);
                LogUtils.d(TAG, "initForegroundNotificationImmediately() | 通知工具类初始化完成");
            }

            if (mForegroundNotifyMsg == null) {
                mForegroundNotifyMsg = new NotificationMessage();
                mForegroundNotifyMsg.setTitle("电池监测服务");
                mForegroundNotifyMsg.setContent("后台运行中");
                mForegroundNotifyMsg.setRemindMSG("service_running");
                LogUtils.d(TAG, "initForegroundNotificationImmediately() | 通知消息构建完成");
            }

            mNotificationManager.startForegroundServiceNotify(this, mForegroundNotifyMsg);
            ToastUtils.show("电池监测服务已启动");
            LogUtils.d(TAG, String.format("initForegroundNotificationImmediately() | 前台通知发送成功 | ID=%d", NotificationManagerUtils.NOTIFY_ID_FOREGROUND_SERVICE));
            return true;
        } catch (Exception e) {
            LogUtils.e(TAG, "initForegroundNotificationImmediately() | 通知初始化异常", e);
            return false;
        }
    }

    /**
     * 停止前台服务并取消通知
     */
    private void stopForegroundService() {
        LogUtils.d(TAG, "stopForegroundService() 执行");
        try {
            stopForeground(true);
            LogUtils.d(TAG, "stopForegroundService() | 前台服务已停止，通知已取消");
        } catch (Exception e) {
            LogUtils.e(TAG, "stopForegroundService() | 停止异常", e);
        }
    }

    // ====================== 配置管理（本地持久化+内存同步） ======================
    /**
     * 加载本地最新服务控制配置
     */
    private void loadLatestServiceControlConfig() {
        LogUtils.d(TAG, "loadLatestServiceControlConfig() 执行");
        ControlCenterServiceBean latestBean = ControlCenterServiceBean.loadBean(this, ControlCenterServiceBean.class);
        if (latestBean != null) {
            mServiceControlBean = latestBean;
            LogUtils.d(TAG, String.format("loadLatestServiceControlConfig() | 配置读取成功 | 启用=%b", mServiceControlBean.isEnableService()));
        } else {
            LogUtils.w(TAG, "loadLatestServiceControlConfig() | 本地无配置，沿用内存配置");
        }
    }

    /**
     * 加载默认业务配置（首次启动兜底）
     */
    private void loadDefaultConfig() {
        LogUtils.d(TAG, "loadDefaultConfig() 执行");
        if (mCurrentConfigBean == null) {
            mCurrentConfigBean = new AppConfigBean();
            mCurrentConfigBean.setEnableChargeReminder(true);
            mCurrentConfigBean.setChargeReminderValue(DEFAULT_CHARGE_REMINDER_VALUE);
            mCurrentConfigBean.setEnableUsageReminder(true);
            mCurrentConfigBean.setUsageReminderValue(DEFAULT_USAGE_REMINDER_VALUE);
            mCurrentConfigBean.setBatteryDetectInterval(DEFAULT_BATTERY_DETECT_INTERVAL);
            LogUtils.d(TAG, String.format("loadDefaultConfig() | 默认配置加载完成 | 充电阈值=%d | 耗电阈值=%d | 检测间隔=%dms",
										  DEFAULT_CHARGE_REMINDER_VALUE, DEFAULT_USAGE_REMINDER_VALUE, DEFAULT_BATTERY_DETECT_INTERVAL));
        } else {
            LogUtils.d(TAG, "loadDefaultConfig() | 内存已有配置，无需加载");
        }
    }

    // ====================== 业务组件初始化与销毁（Handler/广播/线程等） ======================
    /**
     * 初始化Handler等核心业务组件
     */
    private void initServiceBusinessLogic() {
        LogUtils.d(TAG, "initServiceBusinessLogic() 执行");
        // 初始化Handler
        if (mServiceHandler == null) {
            mServiceHandler = new ControlCenterServiceHandler(this);
            LogUtils.d(TAG, "initServiceBusinessLogic() | Handler初始化完成");
        } else {
            LogUtils.d(TAG, "initServiceBusinessLogic() | Handler已存在");
        }
        // 初始化广播接收器
        if (mControlCenterServiceReceiver == null) {
            mControlCenterServiceReceiver = new ControlCenterServiceReceiver(this);
            mControlCenterServiceReceiver.registerAction(this);
            LogUtils.d(TAG, "initServiceBusinessLogic() | 广播接收器初始化并注册完成");
        } else {
            LogUtils.d(TAG, "initServiceBusinessLogic() | 广播接收器已存在");
        }
    }

    /**
     * 释放广播接收器资源
     */
    private void releaseBroadcastReceiver() {
        LogUtils.d(TAG, "releaseBroadcastReceiver() 执行");
        if (mControlCenterServiceReceiver != null) {
            mControlCenterServiceReceiver.release();
            mControlCenterServiceReceiver = null;
            LogUtils.d(TAG, "releaseBroadcastReceiver() | 广播接收器已释放");
        } else {
            LogUtils.w(TAG, "releaseBroadcastReceiver() | 广播接收器实例为空");
        }
    }

    /**
     * 销毁Handler，移除所有消息和回调，防止内存泄漏
     */
    private void destroyHandler() {
        LogUtils.d(TAG, "destroyHandler() 执行");
        if (mServiceHandler != null) {
            mServiceHandler.removeCallbacksAndMessages(null);
            mServiceHandler = null;
            LogUtils.d(TAG, "destroyHandler() | Handler已销毁");
        } else {
            LogUtils.w(TAG, "destroyHandler() | Handler实例为空");
        }
    }

    /**
     * 释放通知工具类资源
     */
    private void releaseNotificationResource() {
        LogUtils.d(TAG, "releaseNotificationResource() 执行");
        if (mNotificationManager != null) {
            mNotificationManager.release();
            mNotificationManager = null;
            LogUtils.d(TAG, "releaseNotificationResource() | 通知资源已释放");
        } else {
            LogUtils.w(TAG, "releaseNotificationResource() | 通知工具类实例为空");
        }
    }

    /**
     * 置空所有引用，防止内存泄漏
     */
    private void clearAllReferences() {
        LogUtils.d(TAG, "clearAllReferences() 执行");
        mForegroundNotifyMsg = null;
        mServiceControlBean = null;
        LogUtils.d(TAG, "clearAllReferences() | 引用清理完成");
    }

    // ====================== 外部调用接口（静态方法，提供服务启停/配置更新入口） ======================
    /**
     * 外部启动服务的统一入口
     * @param context 上下文
     */
    public static void startControlCenterService(Context context) {
        LogUtils.d(TAG, String.format("startControlCenterService() 执行 | context=%s", context));
        if (context == null) {
            LogUtils.e(TAG, "startControlCenterService() | Context为空，启动失败");
            return;
        }

        // 保存启用配置
        ControlCenterServiceBean controlBean = new ControlCenterServiceBean(true);
        ControlCenterServiceBean.saveBean(context, controlBean);
        LogUtils.d(TAG, "startControlCenterService() | 服务启用配置已保存");

        // 启动服务（区分API版本）
        Intent intent = new Intent(context, ControlCenterService.class);
        if (Build.VERSION.SDK_INT >= API_LEVEL_26) {
            context.startForegroundService(intent);
            LogUtils.d(TAG, "startControlCenterService() | 以前台服务方式启动（API26+）");
        } else {
            context.startService(intent);
            LogUtils.d(TAG, "startControlCenterService() | 以普通服务方式启动（API26-）");
        }
    }

    /**
     * 外部停止服务的统一入口
     * @param context 上下文
     */
    public static void stopControlCenterService(Context context) {
        LogUtils.d(TAG, String.format("stopControlCenterService() 执行 | context=%s", context));
        if (context == null) {
            LogUtils.e(TAG, "stopControlCenterService() | Context为空，停止失败");
            return;
        }

        // 保存停用配置
        ControlCenterServiceBean controlBean = new ControlCenterServiceBean(false);
        ControlCenterServiceBean.saveBean(context, controlBean);
        LogUtils.d(TAG, "stopControlCenterService() | 服务停用配置已保存");

        // 停止服务
        Intent intent = new Intent(context, ControlCenterService.class);
        context.stopService(intent);
        LogUtils.d(TAG, "stopControlCenterService() | 停止指令已发送");
    }

    /**
     * 外部更新配置并触发线程重启
     * @param context 上下文
     */
    public static void sendAppConfigStatusUpdateMessage(Context context) {
        LogUtils.d(TAG, String.format("sendAppConfigStatusUpdateMessage() 执行 | context=%s", context));
        if (context == null) {
            LogUtils.e(TAG, "sendAppConfigStatusUpdateMessage() | 参数为空，更新失败");
            return;
        }

        Intent intent = new Intent(ControlCenterServiceReceiver.ACTION_APPCONFIG_CHANGED);
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
        LogUtils.d(TAG, String.format("sendAppConfigStatusUpdateMessage() | 配置更新广播发送 | action=%s", ControlCenterServiceReceiver.ACTION_APPCONFIG_CHANGED));
    }

    /**
     * 检查并引导用户开启忽略电池优化（API23+）
     * @param context 上下文
     */
    public static void checkIgnoreBatteryOptimization(Context context) {
        LogUtils.d(TAG, String.format("checkIgnoreBatteryOptimization() 执行 | context=%s", context));
        if (context == null || Build.VERSION.SDK_INT < API_LEVEL_23) {
            LogUtils.w(TAG, "checkIgnoreBatteryOptimization() | 无需检查（Context为空或API<23）");
            return;
        }

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager == null) {
            LogUtils.e(TAG, "checkIgnoreBatteryOptimization() | PowerManager获取失败");
            return;
        }

        String packageName = context.getPackageName();
        boolean isIgnored = powerManager.isIgnoringBatteryOptimizations(packageName);
        LogUtils.d(TAG, String.format("checkIgnoreBatteryOptimization() | 已忽略电池优化=%b", isIgnored));

        if (!isIgnored) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
            LogUtils.d(TAG, String.format("checkIgnoreBatteryOptimization() | 已跳转至系统设置页 | package=%s", packageName));
        }
    }

    /**
     * 检查服务是否运行（适配API30+）
     * @param context      上下文
     * @param serviceClass 服务类
     * @return true=运行中 false=未运行
     */
    private static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        LogUtils.d(TAG, String.format("isServiceRunning() 执行 | context=%s | service=%s", context, serviceClass != null ? serviceClass.getName() : "null"));
        if (context == null || serviceClass == null) {
            LogUtils.e(TAG, "isServiceRunning() | 参数为空");
            return false;
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            LogUtils.e(TAG, "isServiceRunning() | ActivityManager获取失败");
            return false;
        }

        boolean isRunning = false;
        String packageName = context.getPackageName();
        String serviceClassName = serviceClass.getName();

        if (Build.VERSION.SDK_INT >= API_LEVEL_30) {
            // API30+ 禁止获取其他应用服务，通过进程状态判断
            List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
            if (processes != null) {
                for (ActivityManager.RunningAppProcessInfo process : processes) {
                    if (packageName.equals(process.processName) &&
						(process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE ||
						process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)) {
                        isRunning = true;
                        break;
                    }
                }
            }
            LogUtils.d(TAG, String.format("isServiceRunning() | API30+ 判断结果=%b", isRunning));
        } else {
            // API30- 通过服务列表判断
            List<ActivityManager.RunningServiceInfo> services = am.getRunningServices(RUNNING_SERVICE_LIST_LIMIT);
            if (services != null) {
                for (ActivityManager.RunningServiceInfo info : services) {
                    if (serviceClassName.equals(info.service.getClassName())) {
                        isRunning = true;
                        break;
                    }
                }
            }
            LogUtils.d(TAG, String.format("isServiceRunning() | API30- 判断结果=%b", isRunning));
        }

        // 兜底判断：配置启用状态
        if (!isRunning) {
            isRunning = isServiceStarted(context, serviceClass);
            LogUtils.d(TAG, String.format("isServiceRunning() | 兜底判断结果=%b", isRunning));
        }
        return isRunning;
    }

    /**
     * 兜底判断服务是否已启动（通过配置文件）
     */
    private static boolean isServiceStarted(Context context, Class<?> serviceClass) {
        LogUtils.d(TAG, "isServiceStarted() 执行");
        try {
            ControlCenterServiceBean controlBean = ControlCenterServiceBean.loadBean(context, ControlCenterServiceBean.class);
            return controlBean != null && controlBean.isEnableService();
        } catch (Exception e) {
            LogUtils.e(TAG, "isServiceStarted() | 兜底判断异常", e);
            return false;
        }
    }

    // ====================== 业务方法（配置更新/电池状态回调） ======================
    /**
     * 接收外部配置更新，同步到提醒线程
     * @param latestConfig 最新配置
     */
    public void notifyAppConfigUpdate(AppConfigBean latestConfig) {
        int chargeThreshold = latestConfig != null ? latestConfig.getChargeReminderValue() : -1;
        int usageThreshold = latestConfig != null ? latestConfig.getUsageReminderValue() : -1;
        LogUtils.d(TAG, String.format("notifyAppConfigUpdate() 执行 | 充电阈值=%d | 耗电阈值=%d", chargeThreshold, usageThreshold));
        if (latestConfig != null && mServiceHandler != null) {
            mCurrentConfigBean = latestConfig;
            RemindThread.startRemindThreadWithAppConfig(this, mServiceHandler, latestConfig);
            LogUtils.d(TAG, "notifyAppConfigUpdate() | 配置已同步到提醒线程");
        } else {
            LogUtils.e(TAG, String.format("notifyAppConfigUpdate() | 参数为空，同步失败 | latestConfig=%s | mServiceHandler=%s", latestConfig, mServiceHandler));
        }
    }

    // ====================== Getter 方法（按需开放，避免冗余Setter） ======================
    public ControlCenterServiceBean getServiceControlBean() {
        return mServiceControlBean;
    }

    public NotificationManagerUtils getNotificationManager() {
        return mNotificationManager;
    }

    public NotificationMessage getForegroundNotifyMsg() {
        return mForegroundNotifyMsg;
    }

    public AppConfigBean getCurrentConfigBean() {
        return mCurrentConfigBean;
    }

    public boolean isDestroyed() {
        return mIsDestroyed;
    }
}

