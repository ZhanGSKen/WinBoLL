package cc.winboll.studio.powerbell.utils;

import android.content.Intent;
import android.os.BatteryManager;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2024/07/18 04:32:46
 * @Describe 电池状态工具类
 * 功能：解析电池广播Intent，获取充电状态、当前电量
 * 适配：Java7 | API30 | 小米手机
 */
public class BatteryUtils {
    // ================================== 静态常量区（置顶归类，消除魔法值）=================================
    public static final String TAG = "BatteryUtils";

    // 电池电量计算常量
    private static final int BATTERY_SCALE_DEFAULT = 100; // 电量刻度默认值
    private static final int BATTERY_LEVEL_MIN = 0;       // 电量百分比最小值
    private static final int BATTERY_LEVEL_MAX = 100;     // 电量百分比最大值
    private static final int EXTRA_STATUS_DEFAULT = -1;   // 电池状态默认值

    // ================================== 工具方法（静态方法，无状态设计）=================================
    /**
     * 判断当前是否处于充电状态
     * @param intent 电池状态广播Intent（非空）
     * @return true=充电中/已充满，false=未充电
     */
    public static boolean isCharging(Intent intent) {
        LogUtils.d(TAG, "【isCharging】调用开始");
        // 入参非空校验
        if (intent == null) {
            LogUtils.e(TAG, "【isCharging】入参异常：intent为空，返回false");
            return false;
        }

        // 解析电池状态
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, EXTRA_STATUS_DEFAULT);
        LogUtils.d(TAG, "【isCharging】解析电池状态：status=" + status);

        // 判断充电状态（充电中/已充满均视为充电状态）
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING 
			|| status == BatteryManager.BATTERY_STATUS_FULL;
        LogUtils.d(TAG, "【isCharging】调用结束 | 充电状态=" + isCharging);
        return isCharging;
    }

    /**
     * 获取当前电池电量百分比（0-100）
     * @param intent 电池状态广播Intent（非空）
     * @return 电量百分比，异常返回0
     */
    public static int getCurrentBatteryLevel(Intent intent) {
        LogUtils.d(TAG, "【getCurrentBatteryLevel】调用开始");
        // 入参非空校验
        if (intent == null) {
            LogUtils.e(TAG, "【getCurrentBatteryLevel】入参异常：intent为空，返回0");
            return BATTERY_LEVEL_MIN;
        }

        // 解析电量原始值与刻度值
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, BATTERY_LEVEL_MIN);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, BATTERY_SCALE_DEFAULT);
        LogUtils.d(TAG, "【getCurrentBatteryLevel】解析原始数据 | level=" + level + " | scale=" + scale);

        // 计算并校验电量百分比，避免除以0或数值越界
        int batteryLevel;
        if (scale <= 0) {
            LogUtils.w(TAG, "【getCurrentBatteryLevel】刻度值无效（scale=" + scale + "），直接使用level值");
            batteryLevel = level;
        } else {
            batteryLevel = level * BATTERY_SCALE_DEFAULT / scale;
        }

        // 确保电量值在0-100范围内
        batteryLevel = Math.max(BATTERY_LEVEL_MIN, Math.min(batteryLevel, BATTERY_LEVEL_MAX));
        LogUtils.d(TAG, "【getCurrentBatteryLevel】调用结束 | 电量百分比=" + batteryLevel + "%");
        return batteryLevel;
    }
}

