package cc.winboll.studio.powerbell.threads;

import android.content.Context;
import android.os.Message;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.App;
import cc.winboll.studio.powerbell.handlers.ControlCenterServiceHandler;
import cc.winboll.studio.powerbell.models.AppConfigBean;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 电量通知提醒线程（多实例列表管理）
 * 功能：管理充电/耗电提醒逻辑，触发条件时向Handler发送提醒消息
 * 适配：Java7 | API30 | 内存泄漏防护 | 多线程状态同步
 * 对外接口：{@link #startRemindThreadWithAppConfig(Context, ControlCenterServiceHandler, AppConfigBean)}、
 * {@link #startRemindThreadWithBatteryInfo(Context, ControlCenterServiceHandler, boolean, int)}、{@link #stopRemindThread()}
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Describe 电量通知提醒线程
 */
public class RemindThread extends Thread {
    // ====================== 静态常量区（置顶归类，消除魔法值） ======================
    public static final String TAG = "RemindThread";

    // 时间常量 (ms)
    private static final int MIN_SLEEP_TIME = 2000;
    private static final long THREAD_JOIN_TIMEOUT = 1000L;

    // 状态常量
    private static final int BATTERY_LEVEL_MIN = 0;
    private static final int BATTERY_LEVEL_MAX = 100;

    // 提醒类型常量
    private static final String REMIND_TYPE_CHARGE = "+";
    private static final String REMIND_TYPE_USAGE = "-";

    // ====================== 静态成员（多实例列表管理） ======================
    private static volatile ArrayList<RemindThread> sRemindThreadList;

    // ====================== 成员变量区（按功能分层，volatile保证多线程可见性） ======================
    // 并发安全锁（保护线程状态变更）
    private final Object mRemindLock = new Object();

    // 弱引用依赖（防内存泄漏，ApplicationContext 避免 Activity 引用）
    private Context mContext;
    private WeakReference<ControlCenterServiceHandler> mwrControlCenterServiceHandler;

    // 线程状态标记（volatile 确保多线程可见）
    private volatile boolean isReminding;
    public volatile boolean isExist;

    // 业务配置参数（volatile 确保配置变更实时生效）
    private volatile boolean isEnableChargeReminder;
    private volatile boolean isEnableUsageReminder;
    private volatile long sleepTime;
    private volatile int chargeReminderValue;
    private volatile int usageReminderValue;
    private volatile boolean isCharging;

    // ====================== 私有构造器（禁止外部实例化） ======================
    private RemindThread(Context context, ControlCenterServiceHandler handler) {
        LogUtils.d(TAG, String.format("RemindThread() 构造器调用 | context=%s | handler=%s", context, handler));
        this.mContext = context.getApplicationContext();
        this.mwrControlCenterServiceHandler = new WeakReference<>(handler);
        resetThreadStateInternal();
        LogUtils.d(TAG, String.format("RemindThread() 构造完成 | threadId=%d | 初始状态重置成功", getId()));
    }

    // ====================== 对外公开静态接口（多实例列表管理） ======================
    /**
     * 启动提醒线程，同步最新配置
     * 逻辑：停止所有旧线程 → 创建新线程 → 加入列表管理
     * @param context  上下文（非空）
     * @param handler  服务处理器（非空）
     * @param config   应用配置Bean（非空）
     * @return true: 启动成功；false: 入参非法
     */
    public static boolean startRemindThreadWithAppConfig(Context context, ControlCenterServiceHandler handler, AppConfigBean config) {
        LogUtils.d(TAG, String.format("startRemindThreadWithAppConfig() 调用 | context=%s | handler=%s | config=%s", context, handler, config));

        // 入参严格校验
        if (context == null || handler == null || config == null) {
            LogUtils.e(TAG, String.format("启动失败：入参为空 | context=%s | handler=%s | config=%s", context, handler, config));
            return false;
        }

        // 初始化线程列表（双重校验锁）
        if (sRemindThreadList == null) {
            synchronized (RemindThread.class) {
                if (sRemindThreadList == null) {
                    sRemindThreadList = new ArrayList<RemindThread>();
                    LogUtils.d(TAG, "线程列表初始化完成");
                }
            }
        }

        // 停止所有旧线程
        stopAllOldThreadsInternal();

        // 创建并启动新线程
        RemindThread newRemindThread = new RemindThread(context, handler);
        newRemindThread.setAppConfigBean(config);
        newRemindThread.isExist = false;
        newRemindThread.start();
        sRemindThreadList.add(newRemindThread);
        LogUtils.d(TAG, String.format("新线程启动成功 | threadId=%d | 列表大小=%d", newRemindThread.getId(), sRemindThreadList.size()));
        return true;
    }

    /**
     * 安全停止所有线程，清空列表
     */
    public static void stopRemindThread() {
        int listSize = sRemindThreadList != null ? sRemindThreadList.size() : 0;
        LogUtils.d(TAG, String.format("stopRemindThread() 调用 | 列表存在=%b | 列表大小=%d", sRemindThreadList != null, listSize));
        if (sRemindThreadList == null || sRemindThreadList.isEmpty()) {
            LogUtils.w(TAG, "停止失败：线程列表为空");
            return;
        }

        // 标记所有线程退出
        for (RemindThread remindThread : sRemindThreadList) {
            remindThread.isExist = true;
            LogUtils.d(TAG, String.format("标记线程退出 | threadId=%d", remindThread.getId()));
        }
        // 清空列表
        sRemindThreadList.clear();
        LogUtils.d(TAG, "所有线程已标记退出，列表已清空");
    }

    // ====================== 私有静态辅助方法（多实例管理） ======================
    /**
     * 停止所有旧线程并清空列表
     */
    private static void stopAllOldThreadsInternal() {
        if (sRemindThreadList == null || sRemindThreadList.isEmpty()) {
            return;
        }

        // 标记所有旧线程退出
        for (RemindThread remindThread : sRemindThreadList) {
            remindThread.isExist = true;
            LogUtils.d(TAG, String.format("标记旧线程退出 | threadId=%d", remindThread.getId()));
        }
        // 清空旧线程列表
        sRemindThreadList.clear();
        LogUtils.d(TAG, "旧线程已全部标记退出，列表已清空");
    }

    // ====================== 线程核心运行逻辑 ======================
    @Override
    public void run() {
        LogUtils.d(TAG, String.format("run() 执行 | threadId=%d | 状态=%s", getId(), getState()));

        // 初始化提醒状态（加锁保护，避免多线程竞争）
        synchronized (mRemindLock) {
            if (isReminding) {
                LogUtils.w(TAG, String.format("线程已在提醒状态，退出运行 | threadId=%d", getId()));
                return;
            }
            isReminding = true;
        }

        // 核心电量检测循环
        LogUtils.d(TAG, String.format("进入电量检测循环 | 休眠时间=%dms | threadId=%d", sleepTime, getId()));
        while (!isExist) {
            try {
                // 快速退出判断
                if (isExist) break;

                // 电量有效性校验（非0-100视为无效），退出电量提醒线程
                if (App.sQuantityOfElectricity < BATTERY_LEVEL_MIN || App.sQuantityOfElectricity > BATTERY_LEVEL_MAX) {
                    LogUtils.w(TAG, String.format("电量无效，退出电量提醒线程 | 当前电量=%d | threadId=%d", App.sQuantityOfElectricity, getId()));
                    break;
                }

                // 充电/耗电提醒触发逻辑
                boolean chargeRemindTrigger = isCharging && isEnableChargeReminder && App.sQuantityOfElectricity >= chargeReminderValue;
                boolean usageRemindTrigger = !isCharging && isEnableUsageReminder && App.sQuantityOfElectricity <= usageReminderValue;

                if (chargeRemindTrigger) {
                    LogUtils.d(TAG, String.format("触发充电提醒 | 当前电量=%d ≥ 阈值=%d | threadId=%d", App.sQuantityOfElectricity, chargeReminderValue, getId()));
                    sendNotificationMessageInternal(REMIND_TYPE_CHARGE, App.sQuantityOfElectricity, isCharging);
                } else if (usageRemindTrigger) {
                    LogUtils.d(TAG, String.format("触发耗电提醒 | 当前电量=%d ≤ 阈值=%d | threadId=%d", App.sQuantityOfElectricity, usageReminderValue, getId()));
                    sendNotificationMessageInternal(REMIND_TYPE_USAGE, App.sQuantityOfElectricity, isCharging);
                } else {
                    LogUtils.d(TAG, String.format("未有合适类型提醒，退出提醒线程 | threadId=%d", getId()));
                    break;
                }
				
				// 启动电量通知 TTS 语音提醒线程
				TTSRemindThread.start(this.mContext, App.sQuantityOfElectricity, isCharging);

                // 安全休眠，保留中断标记
                safeSleepInternal(sleepTime);

            } catch (Exception e) {
                LogUtils.e(TAG, String.format("循环运行异常，退出电量提醒线程 | 当前电量=%d | threadId=%d", App.sQuantityOfElectricity, getId()), e);
                break;
            }
        }

        // 循环退出，清理状态
        cleanThreadStateInternal();
        LogUtils.d(TAG, String.format("run() 结束 | threadId=%d", getId()));
    }

    // ====================== 内部业务辅助方法 ======================
    /**
     * 发送提醒消息到Handler（弱引用避免内存泄漏）
     * @param type        提醒类型：+充电/-耗电
     * @param battery     当前电量
     * @param isCharging  充电状态
     */
    private void sendNotificationMessageInternal(String type, int battery, boolean isCharging) {
        LogUtils.d(TAG, String.format("sendNotificationMessageInternal() 调用 | 类型=%s | 电量=%d | isCharging=%b | threadId=%d", type, battery, isCharging, getId()));
        // 前置状态校验
        if (isExist || !isReminding) {
            LogUtils.d(TAG, String.format("消息发送跳过：线程已退出或提醒关闭 | threadId=%d", getId()));
            return;
        }

        // 获取弱引用的Handler（校验有效性）
        ControlCenterServiceHandler handler = mwrControlCenterServiceHandler.get();
        if (handler == null) {
            LogUtils.w(TAG, String.format("消息发送失败：Handler已被回收 | threadId=%d", getId()));
            return;
        }

        // 构建并发送消息
        Message message = Message.obtain(handler, ControlCenterServiceHandler.MSG_REMIND_TEXT);
        message.obj = type;
        message.arg1 = battery;
        message.arg2 = isCharging ? 1 : 0;

        try {
            handler.sendMessage(message);
            LogUtils.d(TAG, String.format("提醒消息发送成功 | 类型=%s | 电量=%d | threadId=%d", type, battery, getId()));
        } catch (Exception e) {
            LogUtils.e(TAG, String.format("消息发送异常 | threadId=%d", getId()), e);
            // 异常时回收Message，避免内存泄漏
            if (message != null) {
                message.recycle();
            }
        }
    }

    /**
     * 安全休眠，响应线程中断
     * @param millis 休眠时长(ms)
     */
    private void safeSleepInternal(long millis) {
        LogUtils.d(TAG, String.format("safeSleepInternal() 调用 | 休眠时长=%dms | threadId=%d", millis, getId()));
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LogUtils.w(TAG, String.format("休眠被中断，线程准备退出 | threadId=%d", getId()));
        }
    }

    /**
     * 重置线程初始状态（构造器专用）
     */
    private void resetThreadStateInternal() {
        LogUtils.d(TAG, String.format("resetThreadStateInternal() 调用 | threadId=%d", getId()));
        // 状态标记初始化
        isExist = false;
        isReminding = false;
        // 配置参数初始化
        isEnableChargeReminder = false;
        isEnableUsageReminder = false;
        sleepTime = MIN_SLEEP_TIME;
        chargeReminderValue = -1;
        usageReminderValue = -1;
        isCharging = false;
        LogUtils.d(TAG, String.format("线程初始状态重置完成 | threadId=%d", getId()));
    }

    /**
     * 清理线程运行状态（循环退出时调用）
     */
    private void cleanThreadStateInternal() {
        LogUtils.d(TAG, String.format("cleanThreadStateInternal() 调用 | threadId=%d", getId()));
		TTSRemindThread.stopTTS();
        isReminding = false;
        isExist = true;
        // 中断当前线程（如果存活）
        if (isAlive()) {
            interrupt();
            LogUtils.d(TAG, String.format("线程已中断 | threadId=%d", getId()));
        }
        LogUtils.d(TAG, String.format("线程运行状态清理完成 | threadId=%d", getId()));
    }

    /**
     * 同步应用配置，校验参数有效性
     * @param config 应用配置Bean
     */
    public void setAppConfigBean(AppConfigBean config) {
        LogUtils.d(TAG, String.format("setAppConfigBean() 调用 | config=%s | threadId=%d", config, getId()));
        if (config == null) {
            LogUtils.e(TAG, String.format("配置同步失败：配置Bean为空 | threadId=%d", getId()));
            return;
        }

        // 配置参数同步 + 范围校验（确保参数合法）
        isEnableChargeReminder = config.isEnableChargeReminder();
        isEnableUsageReminder = config.isEnableUsageReminder();
        chargeReminderValue = Math.min(Math.max(config.getChargeReminderValue(), BATTERY_LEVEL_MIN), BATTERY_LEVEL_MAX);
        usageReminderValue = Math.min(Math.max(config.getUsageReminderValue(), BATTERY_LEVEL_MIN), BATTERY_LEVEL_MAX);
        sleepTime = Math.max(config.getBatteryDetectInterval(), MIN_SLEEP_TIME);
//        sQuantityOfElectricity = (config.getCurrentBatteryValue() >= BATTERY_LEVEL_MIN && config.getCurrentBatteryValue() <= BATTERY_LEVEL_MAX)
//            ? config.getCurrentBatteryValue() : INVALID_BATTERY_VALUE;
        isCharging = config.isCharging();

        LogUtils.d(TAG, String.format("配置同步完成 | 休眠时间=%dms | 充电提醒=%b | 耗电提醒=%b | 当前电量=%d | 充电阈值=%d | 耗电阈值=%d | threadId=%d",
									  sleepTime, isEnableChargeReminder, isEnableUsageReminder, App.sQuantityOfElectricity, chargeReminderValue, usageReminderValue, getId()));
    }

    /**
     * 判断线程是否处于运行状态
     * @return true: 运行中；false: 已停止
     */
    private boolean isRunning() {
        boolean running = !isExist && isAlive();
        LogUtils.d(TAG, String.format("isRunning() 调用 | 运行中=%b | 退出标记=%b | 存活=%b | threadId=%d", running, isExist, isAlive(), getId()));
        return running;
    }

    // ====================== Getter/Setter（按需开放） ======================
    public void setIsExist(boolean isExist) {
        LogUtils.d(TAG, String.format("setIsExist() 调用 | isExist=%b | threadId=%d", isExist, getId()));
        this.isExist = isExist;
    }

    public boolean isExist() {
        return isExist;
    }

    // ====================== 调试辅助方法 ======================
    @Override
    public String toString() {
        return "RemindThread{" +
            "threadId=" + getId() +
            ", threadName='" + getName() + '\'' +
            ", isRunning=" + isRunning() +
            ", isReminding=" + isReminding +
            ", chargeThreshold=" + chargeReminderValue +
            ", usageThreshold=" + usageReminderValue +
            ", currentBattery=" + App.sQuantityOfElectricity +
            ", isCharging=" + isCharging +
            ", sleepTime=" + sleepTime + "ms" +
            '}';
    }
}

