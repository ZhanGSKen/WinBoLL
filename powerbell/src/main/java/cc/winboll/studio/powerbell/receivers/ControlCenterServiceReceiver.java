package cc.winboll.studio.powerbell.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.App;
import cc.winboll.studio.powerbell.models.AppConfigBean;
import cc.winboll.studio.powerbell.models.NotificationMessage;
import cc.winboll.studio.powerbell.services.ControlCenterService;
import cc.winboll.studio.powerbell.utils.AppConfigUtils;
import cc.winboll.studio.powerbell.utils.BatteryUtils;
import cc.winboll.studio.powerbell.utils.NotificationManagerUtils;
import java.lang.ref.WeakReference;
import cc.winboll.studio.powerbell.services.ThoughtfulService;

/**
 * 控制中心广播接收器
 * 功能：监听电池状态变化、前台通知更新、配置变更指令
 * 适配：Java7 | API30 | 内存泄漏防护 | 多线程状态同步
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/12/19 20:23
 * @Describe 统一处理系统与应用内广播，同步电池状态与配置，保障多线程数据一致性
 */
public class ControlCenterServiceReceiver extends BroadcastReceiver {
    // ====================== 静态常量区 ======================
    public static final String TAG = "ControlCenterServiceReceiver";

    public static final String ACTION_UPDATE_FOREGROUND_NOTIFICATION = "cc.winboll.studio.powerbell.action.ACTION_UPDATE_FOREGROUND_NOTIFICATION";
    public static final String ACTION_APPCONFIG_CHANGED = "cc.winboll.studio.powerbell.action.ACTION_APPCONFIG_CHANGED";
    public static final String EXTRA_APP_CONFIG_BEAN = "extra_app_config_bean";

    private static final int BROADCAST_PRIORITY = IntentFilter.SYSTEM_HIGH_PRIORITY - 10;
    private static final int BATTERY_LEVEL_MIN = 0;
    private static final int BATTERY_LEVEL_MAX = 100;
    private static final int INVALID_BATTERY = -1;

    // ====================== 静态状态（防抖动 + 防重复播报） ======================
    private static volatile int sLastBatteryLevel = INVALID_BATTERY;
    private static volatile boolean sIsCharging = false;

    // 【新增】防重复触发：3秒内只允许一次播报（关键修复）
    private static final long MIN_TRIGGER_INTERVAL = 3000;
    private static long sLastTriggerTime = 0;

    // ====================== 成员变量 ======================
    private WeakReference<ControlCenterService> mwrControlCenterService;
    private boolean isRegistered = false;

    // ====================== 构造 ======================
    public ControlCenterServiceReceiver(ControlCenterService service) {
        LogUtils.d(TAG, String.format("ControlCenterServiceReceiver() 构造 | 服务：%s",
									  service != null ? service.getClass().getSimpleName() : "null"));
        this.mwrControlCenterService = new WeakReference<>(service);
    }

    // ====================== 广播入口 ======================
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent != null ? intent.getAction() : "null";
        LogUtils.d(TAG, String.format("onReceive() | Action=%s", action));

        if (context == null || intent == null || action == null) {
            LogUtils.e(TAG, "onReceive() 终止：参数无效");
            return;
        }

        ControlCenterService service = mwrControlCenterService != null ? mwrControlCenterService.get() : null;
        if (service == null || service.isDestroyed()) {
            LogUtils.e(TAG, "onReceive() 终止：服务已销毁");
            unregisterAction(context);
            return;
        }

        switch (action) {
            case Intent.ACTION_BATTERY_CHANGED:
                handleBatteryStateChanged(service, intent);
                break;
            case ACTION_UPDATE_FOREGROUND_NOTIFICATION:
                handleUpdateForegroundNotification(service);
                break;
            case ACTION_APPCONFIG_CHANGED:
                handleNotifyAppConfigUpdate(service);
                break;
            default:
                LogUtils.w(TAG, "未知Action：" + action);
        }
    }

    // ====================== 电池状态（核心修复区） ======================
    private void handleBatteryStateChanged(ControlCenterService service, Intent intent) {
        LogUtils.d(TAG, "handleBatteryStateChanged() 解析电池状态");
        try {
            boolean currentCharging = BatteryUtils.isCharging(intent);
            int currentBatteryLevel = BatteryUtils.getCurrentBatteryLevel(intent);
            currentBatteryLevel = Math.min(Math.max(currentBatteryLevel, BATTERY_LEVEL_MIN), BATTERY_LEVEL_MAX);

            LogUtils.d(TAG, String.format("当前：充电=%b | 电量=%d%%", currentCharging, currentBatteryLevel));

            // ================ 【关键修复1】状态没变直接跳过 ================
            if (currentCharging == sIsCharging && currentBatteryLevel == sLastBatteryLevel) {
                LogUtils.d(TAG, "状态无变化，跳过");
                return;
            }

            // ================ 【关键修复2】只有 真正插拔充电 才播报 ================
            boolean isRealPlugSwitch = (currentCharging != sIsCharging);
            boolean isBatteryValid = (sLastBatteryLevel != INVALID_BATTERY);

            // ================ 【关键修复3】防抖动：3秒内不重复触发 ================
            long now = System.currentTimeMillis();
            boolean canTrigger = (now - sLastTriggerTime >= MIN_TRIGGER_INTERVAL);

            if (isRealPlugSwitch && isBatteryValid && canTrigger) {
                LogUtils.d(TAG, "检测到充电状态切换 → 执行TTS提醒");

                // 更新触发时间
                sLastTriggerTime = now;

                // 执行播报
                if (currentCharging) {
                    ThoughtfulService.startServiceWithType(service, ThoughtfulService.ServiceType.CHARGE_STATE);
                } else {
                    ThoughtfulService.startServiceWithType(service, ThoughtfulService.ServiceType.DISCHARGE_STATE);
                }
            }

            // 更新缓存
            sIsCharging = currentCharging;
            sLastBatteryLevel = currentBatteryLevel;

            // 同步配置
            handleNotifyAppConfigUpdate(service);

        } catch (Exception e) {
            LogUtils.e(TAG, "handleBatteryStateChanged 异常", e);
        }
    }

    // ====================== 配置同步 ======================
    private void handleNotifyAppConfigUpdate(ControlCenterService service) {
        LogUtils.d(TAG, "handleNotifyAppConfigUpdate() 同步配置");
        try {
            AppConfigBean latestConfig = AppConfigUtils.getInstance(service).loadAppConfig();
            if (latestConfig == null) {
                LogUtils.e(TAG, "配置为空，终止");
                return;
            }

            App.sQuantityOfElectricity = sLastBatteryLevel;
            latestConfig.setIsCharging(sIsCharging);
            service.notifyAppConfigUpdate(latestConfig);

        } catch (Exception e) {
            LogUtils.e(TAG, "handleNotifyAppConfigUpdate 异常", e);
        }
    }

    // ====================== 通知更新 ======================
    private void handleUpdateForegroundNotification(ControlCenterService service) {
        LogUtils.d(TAG, "handleUpdateForegroundNotification() 更新通知");
        try {
            NotificationManagerUtils notifyUtils = service.getNotificationManager();
            NotificationMessage notifyMsg = service.getForegroundNotifyMsg();

            if (notifyUtils == null || notifyMsg == null) {
                LogUtils.e(TAG, "通知工具或消息为空");
                return;
            }
            notifyUtils.updateForegroundServiceNotify(notifyMsg);
        } catch (Exception e) {
            LogUtils.e(TAG, "更新通知异常", e);
        }
    }

    // ====================== 注册/注销 ======================
    public void registerAction(Context context) {
        if (context == null || isRegistered) return;
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            filter.addAction(ACTION_UPDATE_FOREGROUND_NOTIFICATION);
            filter.addAction(ACTION_APPCONFIG_CHANGED);
            filter.setPriority(BROADCAST_PRIORITY);
            context.registerReceiver(this, filter);
            isRegistered = true;
            LogUtils.d(TAG, "广播注册完成");
        } catch (Exception e) {
            LogUtils.e(TAG, "注册异常", e);
        }
    }

    public void unregisterAction(Context context) {
        if (context == null || !isRegistered) return;
        try {
            context.unregisterReceiver(this);
            isRegistered = false;
            LogUtils.d(TAG, "广播已注销");
        } catch (Exception e) {
            LogUtils.w(TAG, "注销异常", e);
        }
    }

    // ====================== 释放 ======================
    public void release() {
        LogUtils.d(TAG, "release() 释放资源");
        if (mwrControlCenterService != null) {
            mwrControlCenterService.clear();
            mwrControlCenterService = null;
        }
        // 重置静态状态
        sLastBatteryLevel = INVALID_BATTERY;
        sIsCharging = false;
        sLastTriggerTime = 0; // 【新增】重置防抖时间
    }

    // ====================== Getter ======================
    public static int getLastBatteryLevel() {
        return sLastBatteryLevel;
    }

    public static boolean isLastCharging() {
        return sIsCharging;
    }
}

