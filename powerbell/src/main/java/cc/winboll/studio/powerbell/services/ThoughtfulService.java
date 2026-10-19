package cc.winboll.studio.powerbell.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.App;
import cc.winboll.studio.powerbell.models.AppConfigBean;
import cc.winboll.studio.powerbell.models.ThoughtfulServiceBean;
import cc.winboll.studio.powerbell.utils.AppConfigUtils;

/**
 * 智能电池服务(充电/放电状态处理)
 * 适配：Java7 语法规范 | Android API30 系统版本
 * 功能：接收充电/放电状态指令，根据不同状态执行对应业务任务
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/12/29 19:29
 */
public class ThoughtfulService extends Service {

    // ====================================== 常量区 ======================================
    public static final String TAG = "ThoughtfulService";
    public static final String EXTRA_SERVICE_TYPE = "EXTRA_SERVICE_TYPE";

    // 防止重复播报的标志（静态，全局唯一）
    private static boolean sIsPlaying = false;

    // ====================================== 枚举 ======================================
    public enum ServiceType {
        CHARGE_STATE,
        DISCHARGE_STATE
		}

    // ====================================== 外部启动入口（加固） ======================================
    public static void startServiceWithType(Context context, ServiceType serviceType) {
        LogUtils.d(TAG, "【startServiceWithType】调用 | type=" + (serviceType == null ? "null" : serviceType.name()));

        if (context == null || serviceType == null) {
            LogUtils.d(TAG, "【startServiceWithType】空参数，直接返回");
            return;
        }

        // 1. 预先读取配置，不满足直接不启动服务（最关键拦截）
        ThoughtfulServiceBean ttsBean = ThoughtfulServiceBean.loadBean(context, ThoughtfulServiceBean.class);
        if (ttsBean == null) {
            ttsBean = new ThoughtfulServiceBean();
        }

        // 充电TTS未开 → 不启动
        if (serviceType == ServiceType.CHARGE_STATE && !ttsBean.isEnableChargeTts()) {
            LogUtils.d(TAG, "【startServiceWithType】充电TTS未启用，不启动服务");
            return;
        }

        // 用电TTS未开 → 不启动
        if (serviceType == ServiceType.DISCHARGE_STATE && !ttsBean.isEnableUsePowerTts()) {
            LogUtils.d(TAG, "【startServiceWithType】用电TTS未启用，不启动服务");
            return;
        }

        // 2. 防止重复启动导致叠加播报
        if (sIsPlaying) {
            LogUtils.d(TAG, "【startServiceWithType】已有播报任务，跳过本次启动");
            return;
        }

        // 3. 真正启动
        Intent intent = new Intent(context, ThoughtfulService.class);
        intent.putExtra(EXTRA_SERVICE_TYPE, serviceType);
        context.startService(intent);
        LogUtils.d(TAG, "【startServiceWithType】服务启动成功");
    }

    // ====================================== 生命周期 ======================================
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "【onStartCommand】进入");

        if (intent == null) {
            LogUtils.d(TAG, "【onStartCommand】intent = null，停止");
            stopSelf();
            return START_NOT_STICKY;
        }

        // 再次防止重复播报（双重保险）
        if (sIsPlaying) {
            LogUtils.d(TAG, "【onStartCommand】已有播报，直接停止服务");
            stopSelf();
            return START_NOT_STICKY;
        }

        // 解析类型
        ServiceType type = (ServiceType) intent.getSerializableExtra(EXTRA_SERVICE_TYPE);
        if (type == null) {
            LogUtils.d(TAG, "【onStartCommand】type = null，停止");
            stopSelf();
            return START_NOT_STICKY;
        }

        // 二次校验开关（防止外部绕过 startServiceWithType 直接启动）
        ThoughtfulServiceBean ttsBean = ThoughtfulServiceBean.loadBean(this, ThoughtfulServiceBean.class);
        if (ttsBean == null) ttsBean = new ThoughtfulServiceBean();

        boolean allowPlay = false;
        if (type == ServiceType.CHARGE_STATE && ttsBean.isEnableChargeTts()) {
            allowPlay = true;
        }
        if (type == ServiceType.DISCHARGE_STATE && ttsBean.isEnableUsePowerTts()) {
            allowPlay = true;
        }

        if (!allowPlay) {
            LogUtils.d(TAG, "【onStartCommand】TTS开关已关闭，不执行播报");
            stopSelf();
            return START_NOT_STICKY;
        }

        // 执行任务
        if (type == ServiceType.CHARGE_STATE) {
            executeChargeStateTask();
        } else if (type == ServiceType.DISCHARGE_STATE) {
            executeDischargeStateTask();
        }

        return START_NOT_STICKY; // 重要：执行完自动销毁，不保留服务
    }

    // ====================================== 充电任务 ======================================
    private void executeChargeStateTask() {
        LogUtils.d(TAG, "【executeChargeStateTask】执行充电任务");

        sIsPlaying = true; // 锁定播报

        try {
            AppConfigBean config = AppConfigUtils.getInstance(this).loadAppConfig();
            if (config == null) {
                LogUtils.e(TAG, "配置为空，停止");
                return;
            }

            ThoughtfulServiceBean ttsBean = ThoughtfulServiceBean.loadBean(this, ThoughtfulServiceBean.class);
            if (ttsBean == null) ttsBean = new ThoughtfulServiceBean();

            // 主提醒开关未开 → 直接不播
            if (!config.isEnableChargeReminder()) {
                LogUtils.d(TAG, "充电提醒总开关关闭，不播报");
                return;
            }

            int limit = config.getChargeReminderValue();
            int battery = App.sQuantityOfElectricity;

            // 电量无效值 → 不拼接电量
            String batteryStr = "";
            if (battery >= 0 && battery <= 100 && ttsBean.isEnableChargeTtsWithBattary()) {
                batteryStr = String.format("当前电量百分之%d。", battery);
            }

            String text = batteryStr + String.format("限量充电提醒已启用，限值百分之%d。", limit);
            TTSPlayService.startPlayTTS(this, text);
            LogUtils.d(TAG, "充电TTS已下发：" + text);

        } finally {
            sIsPlaying = false; // 释放锁
            stopSelf();
        }
    }

    // ====================================== 放电任务 ======================================
    private void executeDischargeStateTask() {
        LogUtils.d(TAG, "【executeDischargeStateTask】执行放电任务");

        sIsPlaying = true;

        try {
            AppConfigBean config = AppConfigUtils.getInstance(this).loadAppConfig();
            if (config == null) {
                LogUtils.e(TAG, "配置为空，停止");
                return;
            }

            ThoughtfulServiceBean ttsBean = ThoughtfulServiceBean.loadBean(this, ThoughtfulServiceBean.class);
            if (ttsBean == null) ttsBean = new ThoughtfulServiceBean();

            // 主提醒开关未开 → 不播
            if (!config.isEnableUsageReminder()) {
                LogUtils.d(TAG, "低电提醒总开关关闭，不播报");
                return;
            }

            int limit = config.getUsageReminderValue();
            int battery = App.sQuantityOfElectricity;

            String batteryStr = "";
            if (battery >= 0 && battery <= 100 && ttsBean.isEnableUseageTtsWithBattary()) {
                batteryStr = String.format("当前电量百分之%d。", battery);
            }

            String text = batteryStr + String.format("低电量提醒已启用，限值百分之%d。", limit);
            TTSPlayService.startPlayTTS(this, text);
            LogUtils.d(TAG, "放电TTS已下发：" + text);

        } finally {
            sIsPlaying = false;
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "【onDestroy】服务已销毁");
    }
}

