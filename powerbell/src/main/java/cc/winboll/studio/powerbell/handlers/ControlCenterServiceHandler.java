package cc.winboll.studio.powerbell.handlers;

import android.os.Handler;
import android.os.Message;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.models.NotificationMessage;
import cc.winboll.studio.powerbell.models.ThoughtfulServiceBean;
import cc.winboll.studio.powerbell.services.ControlCenterService;
import cc.winboll.studio.powerbell.threads.TTSRemindThread;
import java.lang.ref.WeakReference;

/**
 * 服务通信Handler
 * 功能：处理电量提醒消息，构建并发送标准化通知
 * 特性：弱引用防泄漏、参数严格校验、通知格式统一
 * 适配：Java7 | API30 | 小米手机
 */
public class ControlCenterServiceHandler extends Handler {
    // ================================== 静态常量区（置顶归类，消除魔法值）=================================
    public static final String TAG = "ControlCenterServiceHandler";
    public static final int MSG_REMIND_TEXT = 1001; // 电量提醒消息标识

    // 提醒类型常量
    private static final String REMIND_TYPE_CHARGE = "+";
    private static final String REMIND_TYPE_USAGE = "-";

    // 电量范围常量
    private static final int BATTERY_LEVEL_MIN = 0;
    private static final int BATTERY_LEVEL_MAX = 100;

    // 通知文案常量（抽离魔法值，便于统一修改）
    private static final String CHARGE_REMIND_TITLE = "充电提醒";
    private static final String USAGE_REMIND_TITLE = "耗电提醒";
    private static final String CHARGE_REMIND_CONTENT_FORMAT = "(+)电量已达额定值。当前电量%d%%，%s。";
    private static final String USAGE_REMIND_CONTENT_FORMAT = "(-)电量低于指定值。当前电量%d%%，%s。";
    private static final String CHARGE_STATE_CHARGING = "充电中";
    private static final String CHARGE_STATE_NOT_CHARGING = "未充电";

    // ================================== 成员变量区（弱引用防泄漏，final保证不可变）=================================
    private final WeakReference<ControlCenterService> mwrControlCenterService;

    // ================================== 构造方法（强制传入服务，初始化弱引用）=================================
    public ControlCenterServiceHandler(ControlCenterService service) {
        LogUtils.d(TAG, "构造方法执行 | service=" + (service != null ? service.getClass().getSimpleName() : "null"));
        this.mwrControlCenterService = new WeakReference<>(service);
    }

    // ================================== 核心消息处理（重写handleMessage，解析多参数消息）=================================
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        // 解析消息参数：obj=提醒类型(+/-)，arg1=当前电量，arg2=充电状态(1=充电/0=未充电)
        String remindType = (msg.obj != null) ? (String) msg.obj : "";
        int currentBattery = msg.arg1;
        boolean isCharging = msg.arg2 == 1;

        LogUtils.d(TAG, "handleMessage: 接收消息 | what=" + msg.what + " | type=" + remindType + " | battery=" + currentBattery + " | isCharging=" + isCharging);

        // 弱引用获取服务，避免内存泄漏
        ControlCenterService service = mwrControlCenterService.get();
        if (service == null) {
            LogUtils.e(TAG, "handleMessage: 服务实例已被GC回收，终止消息处理");
            return;
        }

        // 按消息类型分发处理
        switch (msg.what) {
            case MSG_REMIND_TEXT:
                handleRemindMessage(service, remindType, currentBattery, isCharging);
                break;
            default:
                LogUtils.w(TAG, "handleMessage: 未知消息类型，忽略处理 | what=" + msg.what);
                break;
        }
    }

    // ================================== 业务辅助方法（构建通知并发送，全链路参数校验）=================================
    /**
     * 处理电量提醒消息，构建带电量+充电状态的通知并发送
     * @param service         控制中心服务实例（已校验非空）
     * @param remindType      提醒类型（+充电/-耗电）
     * @param currentBattery  当前电量（0-100）
     * @param isCharging      充电状态
     */
    private void handleRemindMessage(ControlCenterService service, String remindType, int currentBattery, boolean isCharging) {
        LogUtils.d(TAG, "handleRemindMessage: 开始处理提醒消息 | type=" + remindType + " | battery=" + currentBattery + " | isCharging=" + isCharging);

        // 1. 前置校验：通知工具类+参数有效性
        if (service.getNotificationManager() == null) {
            LogUtils.e(TAG, "handleRemindMessage: 通知管理工具类未初始化，无法发送提醒");
            return;
        }
        if (!REMIND_TYPE_CHARGE.equals(remindType) && !REMIND_TYPE_USAGE.equals(remindType)) {
            LogUtils.w(TAG, "handleRemindMessage: 提醒类型无效，忽略 | type=" + remindType + " | 允许值：" + REMIND_TYPE_CHARGE + "/" + REMIND_TYPE_USAGE);
            return;
        }
        if (currentBattery < BATTERY_LEVEL_MIN || currentBattery > BATTERY_LEVEL_MAX) {
            LogUtils.w(TAG, "handleRemindMessage: 电量值超出范围，忽略 | battery=" + currentBattery + " | 允许范围：" + BATTERY_LEVEL_MIN + "-" + BATTERY_LEVEL_MAX);
            return;
        }

        // 2. 构建通知模型，使用统一格式
        NotificationMessage remindMsg = new NotificationMessage();
        String chargeStateDesc = isCharging ? CHARGE_STATE_CHARGING : CHARGE_STATE_NOT_CHARGING;
        if (REMIND_TYPE_CHARGE.equals(remindType)) {
            remindMsg.setTitle(CHARGE_REMIND_TITLE);
            remindMsg.setContent(String.format(CHARGE_REMIND_CONTENT_FORMAT, currentBattery, chargeStateDesc));
            remindMsg.setRemindMSG("charge_remind");
        } else {
            remindMsg.setTitle(USAGE_REMIND_TITLE);
            remindMsg.setContent(String.format(USAGE_REMIND_CONTENT_FORMAT, currentBattery, chargeStateDesc));
            remindMsg.setRemindMSG("usage_remind");
        }
        LogUtils.d(TAG, "handleRemindMessage: 通知模型构建完成 | title=" + remindMsg.getTitle() + " | content=" + remindMsg.getContent());

        // 3. 调用通知工具类发送提醒
        LogUtils.d(TAG, "handleRemindMessage: 调用通知工具类发送提醒 | remindMSG=" + remindMsg.getRemindMSG());
        service.getNotificationManager().showRemindNotification(service, remindMsg);
        LogUtils.d(TAG, "handleRemindMessage: 提醒通知发送流程执行完毕");
    }
}

