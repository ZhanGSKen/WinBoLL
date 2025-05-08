package cc.winboll.studio.powerbell.threads;

import android.content.Context;
import android.os.Message;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.handlers.ControlCenterServiceHandler;
import java.lang.ref.WeakReference;

public class RemindThread extends Thread {
    
    public static final String TAG = RemindThread.class.getSimpleName();

    Context mContext;
    
    // 控制线程是否退出的标志
    volatile boolean isExist = false;
    // 消息提醒开关
    static volatile boolean isReminding = false;
    // 充电提醒开关
    static volatile boolean isEnableUsegeReminder = false;
    // 耗电提醒开关
    static volatile boolean isEnableChargeReminder = false;
    // 电量比较停顿时间
    static volatile int sleepTime = 1000;
    // 充电提醒电量
    static volatile int chargeReminderValue = -1;
    // 耗电提醒电量
    static volatile int usegeReminderValue = -1;
    // 当前电量
    static volatile int quantityOfElectricity = -1;
    // 是否正在充电
    static volatile boolean isCharging = false;
    // 服务Handler, 用于线程发送消息使用
    WeakReference<ControlCenterServiceHandler> mwrControlCenterServiceHandler;

    public void setIsExist(boolean isExist) {
        this.isExist = isExist;
    }

    public boolean isExist() {
        return isExist;
    }

    public static void setIsReminding(boolean isReminding) {
        RemindThread.isReminding = isReminding;
    }

    public static boolean isReminding() {
        return isReminding;
    }

    public static void setIsEnableUsegeReminder(boolean isEnableUsegeReminder) {
        RemindThread.isEnableUsegeReminder = isEnableUsegeReminder;
    }

    public static boolean isEnableUsegeReminder() {
        return isEnableUsegeReminder;
    }

    public static void setIsEnableChargeReminder(boolean isEnableChargeReminder) {
        RemindThread.isEnableChargeReminder = isEnableChargeReminder;
    }

    public static boolean isEnableChargeReminder() {
        return isEnableChargeReminder;
    }

    public static void setSleepTime(int sleepTime) {
        RemindThread.sleepTime = sleepTime;
    }

    public static int getSleepTime() {
        return sleepTime;
    }

    public static void setChargeReminderValue(int chargeReminderValue) {
        RemindThread.chargeReminderValue = chargeReminderValue;
    }

    public static int getChargeReminderValue() {
        return chargeReminderValue;
    }

    public static void setUsegeReminderValue(int usegeReminderValue) {
        RemindThread.usegeReminderValue = usegeReminderValue;
    }

    public static int getUsegeReminderValue() {
        return usegeReminderValue;
    }

    public static void setQuantityOfElectricity(int quantityOfElectricity) {
        RemindThread.quantityOfElectricity = quantityOfElectricity;
    }

    public static int getQuantityOfElectricity() {
        return quantityOfElectricity;
    }

    public static void setIsCharging(boolean isCharging) {
        RemindThread.isCharging = isCharging;
    }

    public static boolean isCharging() {
        return isCharging;
    }

    // 发送消息给用户
    //
    void sendNotificationMessage(String sz) {
        //LogUtils.d(TAG, "sz is " + sz);
        Message message = Message.obtain();
        message.what = ControlCenterServiceHandler.MSG_REMIND_TEXT;
        //message.obj = new NotificationMessage(mContext.getString(R.string.app_name), sz);
        message.obj = sz;
        ControlCenterServiceHandler handler = mwrControlCenterServiceHandler.get();
        if (isReminding && handler != null) {
            handler.sendMessage(message);
        }
    }

    public RemindThread(Context context, ControlCenterServiceHandler handler) {
        mContext = context;
        mwrControlCenterServiceHandler = new WeakReference<ControlCenterServiceHandler>(handler);
    }

    @Override
    public void run() {
        //LogUtils.d(TAG, "call run()");
        if (isReminding == false) {
            isReminding = true;

            // 等待些许时间，等所有数据初始化完成再执行下面的程序
            // 解决窗口移除后自动重启后会发送一个错误消息的问题
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}

            // 发送提醒线程开始的参数设置
            //sendMessageToUser(Integer.toString(_mnTheQuantityOfElectricity) + ">>>" + Integer.toString(_mnTargetNumber));
            //ToastUtils.show("Service Is Start.");
            //LogUtils.i(TAG, "Service Is Start.");
            while (!isExist()) {

                /*
                LogUtils.d(TAG, "isCharging is " + Boolean.toString(isCharging));
                LogUtils.d(TAG, "usegeReminderValue is " + Integer.toString(usegeReminderValue));
                LogUtils.d(TAG, "quantityOfElectricity is " + Integer.toString(quantityOfElectricity));
                LogUtils.d(TAG, "chargeReminderValue is " + Integer.toString(chargeReminderValue));
                LogUtils.d(TAG, "isEnableChargeReminder is " + Boolean.toString(isEnableChargeReminder));
                LogUtils.d(TAG, "isEnableUsegeReminder is " + Boolean.toString(isEnableUsegeReminder));
                */
                
                try {
                    if (isCharging) {
                        if ((quantityOfElectricity >= chargeReminderValue) 
                            && (isEnableChargeReminder)) {
                            // 正在充电时电量大于指定电量发送提醒
                            sendNotificationMessage("+");
                            // 应用需要继续提醒，设置退出标志为否
                            setIsExist(false);
                            //sendNotificationMessage("I am ready! +");
                        } else {
                            // 设置退出标志，如果后续不需要继续提醒就退出当前进程，用于应用节能。
                            setIsExist(true);
                            isReminding = false;
                            return;
                        }

                    } else {
                        if ((quantityOfElectricity <= usegeReminderValue)
                            && (isEnableUsegeReminder)) {
                            // 正在放电时电量小于指定电量发送提醒
                            sendNotificationMessage("-");
                            // 应用需要继续提醒，设置退出标志为否
                            setIsExist(false);
                            //sendNotificationMessage("I am ready! -");
                        } else {
                            // 设置退出标志，如果后续不需要继续提醒就退出当前进程，用于应用节能。
                            setIsExist(true);
                            isReminding = false;
                            return;
                        }
                    }
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                }
            }
            //ToastUtils.show("Service Is Stop.");
            //LogUtils.i(TAG, "Service Is Stop.");
            isReminding = false;
        }

    }

}
