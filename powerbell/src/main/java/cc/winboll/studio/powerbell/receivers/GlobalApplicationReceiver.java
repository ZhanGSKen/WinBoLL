package cc.winboll.studio.powerbell.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.App;
import cc.winboll.studio.powerbell.MainActivity;
import cc.winboll.studio.powerbell.utils.AppConfigUtils;
import cc.winboll.studio.powerbell.utils.BatteryUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/12/19 20:13
 * @Describe 全局应用广播接收器
 * 功能：监听系统电池状态变化，同步状态到配置工具类，通知页面更新
 * 适配：Java7 | API30 | 内存泄漏防护
 */
public class GlobalApplicationReceiver extends BroadcastReceiver {
    // ====================== 静态常量区（置顶归类，消除魔法值） ======================
    public static final String TAG = "GlobalApplicationReceiver";
    private static final int BATTERY_LEVEL_MIN = 0;
    private static final int BATTERY_LEVEL_MAX = 100;

    // ====================== 静态状态标记（volatile保证多线程可见性） ======================
    private static volatile int sLastBatteryLevel = -1;   // 历史电量（0-100）
    private static volatile boolean sLastIsCharging = false;  // 历史充电状态

    // ====================== 成员变量区（按功能分层，移除冗余的mCurrentReceiver） ======================
    private App mGlobalApplication;
    private AppConfigUtils mAppConfigUtils;

    // ====================== 构造方法（强化参数校验，初始化核心依赖） ======================
    public GlobalApplicationReceiver(App globalApplication) {
        LogUtils.d(TAG, String.format("构造接收器 | App实例：%s", globalApplication));
        if (globalApplication == null) {
            LogUtils.e(TAG, "构造失败：App实例为空");
            throw new IllegalArgumentException("App cannot be null");
        }
        this.mGlobalApplication = globalApplication;
        this.mAppConfigUtils = App.getAppConfigUtils(mGlobalApplication);
        LogUtils.d(TAG, String.format("构造完成 | AppConfigUtils：%s", mAppConfigUtils));
    }

    // ====================== 广播核心接收逻辑（入口方法，过滤电池状态广播） ======================
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent != null ? intent.getAction() : "null";
        LogUtils.d(TAG, String.format("onReceive: 接收广播 | 上下文：%s | Action：%s", context, action));

        // 基础参数校验
        if (context == null || intent == null || action == null) {
            LogUtils.e(TAG, "onReceive: 参数无效，终止处理");
            return;
        }

        // 仅处理电池状态变化广播
        if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
            handleBatteryStateChanged(context, intent);
        }

        LogUtils.d(TAG, "onReceive: 广播处理完成");
    }

    // ====================== 业务逻辑方法（处理电池状态变化，同步配置+通知页面） ======================
    /**
     * 处理电池状态变化广播
     * @param context 上下文
     * @param intent  电池状态广播意图
     */
    private void handleBatteryStateChanged(Context context, Intent intent) {
        LogUtils.d(TAG, "handleBatteryStateChanged: 解析电池状态");
        try {
            // 1. 解析当前电池状态（复用工具类，二次校验电量范围）
            boolean currentIsCharging = BatteryUtils.isCharging(intent);
            int currentBatteryLevel = BatteryUtils.getCurrentBatteryLevel(intent);
            currentBatteryLevel = Math.min(Math.max(currentBatteryLevel, BATTERY_LEVEL_MIN), BATTERY_LEVEL_MAX);
            LogUtils.d(TAG, String.format("handleBatteryStateChanged: 当前状态 | 充电=%b | 电量=%d%%", currentIsCharging, currentBatteryLevel));

            // 2. 状态无变化则跳过，减少无效运算
            if (currentIsCharging == sLastIsCharging && currentBatteryLevel == sLastBatteryLevel) {
                LogUtils.d(TAG, "handleBatteryStateChanged: 状态无变化，跳过处理");
                return;
            }

            // 3. 同步最新状态到配置工具类
            if (mAppConfigUtils != null) {
                if (currentIsCharging != sLastIsCharging) {
                    mAppConfigUtils.setCharging(currentIsCharging);
                    LogUtils.d(TAG, String.format("handleBatteryStateChanged: 同步充电状态 | %b", currentIsCharging));
                }
                if (currentBatteryLevel != sLastBatteryLevel) {
                    mAppConfigUtils.setCurrentBatteryValue(currentBatteryLevel);
                    LogUtils.d(TAG, String.format("handleBatteryStateChanged: 同步电量 | %d%%", currentBatteryLevel));
                }
            } else {
                LogUtils.e(TAG, "handleBatteryStateChanged: AppConfigUtils为空，同步失败");
            }

            // 4. 执行状态变化后的业务逻辑
            // 记录电量变化时间
            if (App.getAppCacheUtils(context) != null) {
                App.getAppCacheUtils(context).addChangingTime(currentBatteryLevel);
                LogUtils.d(TAG, "handleBatteryStateChanged: 记录电量变化时间");
            }
            // 通知MainActivity更新电量
            MainActivity.sendCurrentBatteryValueMessage(currentBatteryLevel);
            LogUtils.d(TAG, String.format("handleBatteryStateChanged: 发送电量更新消息到MainActivity | %d%%", currentBatteryLevel));

            // 5. 更新历史状态缓存
            sLastIsCharging = currentIsCharging;
            sLastBatteryLevel = currentBatteryLevel;
            LogUtils.d(TAG, "handleBatteryStateChanged: 更新历史状态完成");
        } catch (Exception e) {
            LogUtils.e(TAG, "handleBatteryStateChanged: 处理失败", e);
        }
    }

    // ====================== 广播注册/注销（强化容错，避免重复操作） ======================
    /**
     * 注册广播接收器
     */
    public void registerAction() {
        LogUtils.d(TAG, "registerAction: 注册广播");
        if (mGlobalApplication == null) {
            LogUtils.e(TAG, "注册失败：App实例为空");
            return;
        }

        try {
            // 先注销再注册，避免重复注册异常
            unregisterAction();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            mGlobalApplication.registerReceiver(this, filter);
            LogUtils.d(TAG, "registerAction: 广播注册成功");
        } catch (Exception e) {
            LogUtils.e(TAG, "registerAction: 注册失败", e);
        }
    }

    /**
     * 注销广播接收器
     */
    public void unregisterAction() {
        LogUtils.d(TAG, "unregisterAction: 注销广播");
        if (mGlobalApplication == null) {
            LogUtils.e(TAG, "注销失败：App实例为空");
            return;
        }

        try {
            mGlobalApplication.unregisterReceiver(this);
            LogUtils.d(TAG, "unregisterAction: 广播注销成功");
        } catch (IllegalArgumentException e) {
            LogUtils.w(TAG, "unregisterAction: 广播未注册，跳过注销");
        } catch (Exception e) {
            LogUtils.e(TAG, "unregisterAction: 注销失败", e);
        }
    }

    // ====================== 资源释放方法（主动释放，彻底避免内存泄漏） ======================
    /**
     * 释放接收器资源，供App销毁时调用
     */
    public void release() {
        LogUtils.d(TAG, "release: 释放接收器资源");
        // 注销广播
        unregisterAction();
        // 置空引用，帮助GC回收
        mGlobalApplication = null;
        mAppConfigUtils = null;
        // 重置静态状态缓存
        sLastBatteryLevel = -1;
        sLastIsCharging = false;
        LogUtils.d(TAG, "release: 资源释放完成");
    }
}

