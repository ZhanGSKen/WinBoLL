package cc.winboll.studio.powerbell.services;

/*
 * PowerBy : ZhanGSKen(ZhangShaojian2018@163.com)
 * 参考:
 * 进程保活-双进程守护的正确姿势
 * https://blog.csdn.net/sinat_35159441/article/details/75267380
 * Android Service之onStartCommand方法研究
 * https://blog.csdn.net/cyp331203/article/details/38920491
 */
import cc.winboll.studio.powerbell.R;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import cc.winboll.studio.powerbell.GlobalApplication;
import cc.winboll.studio.powerbell.beans.AppConfigBean;
import cc.winboll.studio.powerbell.beans.NotificationMessage;
import cc.winboll.studio.powerbell.handlers.ControlCenterServiceHandler;
import cc.winboll.studio.powerbell.receivers.ControlCenterServiceReceiver;
import cc.winboll.studio.powerbell.services.AssistantService;
import cc.winboll.studio.powerbell.threads.RemindThread;
import cc.winboll.studio.powerbell.utils.AppCacheUtils;
import cc.winboll.studio.powerbell.utils.AppConfigUtils;
import cc.winboll.studio.powerbell.utils.NotificationUtils;
import cc.winboll.studio.powerbell.utils.ServiceUtils;
import cc.winboll.studio.powerbell.utils.StringUtils;
import cc.winboll.studio.shared.log.LogUtils;
import com.hjq.toast.ToastUtils;

public class ControlCenterService extends Service {

    public static final String TAG = "ControlCenterService";

    public static final int MSG_UPDATE_STATUS = 0;

    static ControlCenterService _mControlCenterService;

    volatile boolean isServiceRunning;

    AppConfigUtils mAppConfigUtils;
    AppCacheUtils mAppCacheUtils;
    // 前台服务通知工具
    NotificationUtils mNotificationUtils;
    RemindThread mRemindThread;
    ControlCenterServiceHandler mControlCenterServiceHandler;
    MyServiceConnection mMyServiceConnection;
    ControlCenterServiceReceiver mControlCenterServiceReceiver;
    ControlCenterServiceReceiver mControlCenterServiceReceiverLocalBroadcast;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public RemindThread getRemindThread() {
        return mRemindThread;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _mControlCenterService = ControlCenterService.this;
        isServiceRunning = false;
        mAppConfigUtils = GlobalApplication.getAppConfigUtils(this);
        mAppCacheUtils = GlobalApplication.getAppCacheUtils(this);
        mNotificationUtils = new NotificationUtils(ControlCenterService.this);
        if (mMyServiceConnection == null) {
            mMyServiceConnection = new MyServiceConnection();
        }
        mControlCenterServiceHandler = new ControlCenterServiceHandler(this);

        // 运行服务内容
        run();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 运行服务内容
        run();
        return (mAppConfigUtils.getIsEnableService()) ? START_STICKY : super.onStartCommand(intent, flags, startId);
    }

    // 运行服务内容
    //
    void run() {
        if (mAppConfigUtils.getIsEnableService() && isServiceRunning == false) {
            LogUtils.d(TAG, "run");
            isServiceRunning = true;
            // 唤醒守护进程
            wakeupAndBindAssistant();
            // 显示前台通知栏
            NotificationMessage notificationMessage=createNotificationMessage();
            //Toast.makeText(getApplication(), "", Toast.LENGTH_SHORT).show();
            mNotificationUtils.createForegroundNotification(this, notificationMessage);
            mNotificationUtils.createRemindNotification(this, notificationMessage);

            if (mControlCenterServiceReceiver == null) {
                // 注册广播接收器
                mControlCenterServiceReceiver = new ControlCenterServiceReceiver(this);
                mControlCenterServiceReceiver.registerAction(this);
            }
            startRemindThread(mAppConfigUtils.mAppConfigBean);
            ToastUtils.show("Service Is Start.");
            LogUtils.i(TAG, "Service Is Start.");
        }
    }

    String getValuesString() {
        String szReturn = "Usege: ";
        szReturn += mAppConfigUtils.getIsEnableUsegeReminder() ? Integer.toString(mAppConfigUtils.getUsegeReminderValue()) : "?";
        szReturn += "%   Charge: ";
        szReturn += mAppConfigUtils.getIsEnableChargeReminder() ? Integer.toString(mAppConfigUtils.getChargeReminderValue()) : "?";
        szReturn += "%\nCurrent: " + Integer.toString(mAppConfigUtils.getCurrentValue()) + "%";
        return szReturn;
    }

    NotificationMessage createNotificationMessage() {
        String szTitle = ((GlobalApplication)getApplication()).getString(R.string.app_name);
        String szContent = getValuesString() + " {?} " + StringUtils.formatPCMListString(mAppCacheUtils.getArrayListBatteryInfo());
        return new NotificationMessage(szTitle, szContent);
    }

    // 更新前台通知
    //
    public void updateServiceNotification() {
        mNotificationUtils.updateForegroundNotification(ControlCenterService.this, createNotificationMessage());
    }

    // 更新前台通知
    //
    public void updateServiceNotification(NotificationMessage notificationMessage) {
        mNotificationUtils.updateForegroundNotification(ControlCenterService.this, notificationMessage);
    }

    // 更新前台通知
    //
    public void updateRemindNotification(NotificationMessage notificationMessage) {
        mNotificationUtils.updateRemindNotification(ControlCenterService.this, notificationMessage);
    }

    // 唤醒和绑定守护进程
    //
    void wakeupAndBindAssistant() {
        if (ServiceUtils.isServiceAlive(getApplicationContext(), AssistantService.class.getName()) == false) {
            startService(new Intent(ControlCenterService.this, AssistantService.class));
            //LogUtils.d(TAG, "call wakeupAndBindAssistant() : Binding... AssistantService");
            bindService(new Intent(ControlCenterService.this, AssistantService.class), mMyServiceConnection, Context.BIND_IMPORTANT);
        }
    }

    // 开启提醒铃声线程
    //
    public void startRemindThread(AppConfigBean appConfigBean) {
        //LogUtils.d(TAG, "startRemindThread");
        if (mRemindThread == null) {
            mRemindThread = new RemindThread(this, mControlCenterServiceHandler);
        } else {
            if (mRemindThread.isExist() == true) {
                mRemindThread = new RemindThread(this, mControlCenterServiceHandler);
            } else {
                // 提醒进程正在进行中就更新状态后退出
                mRemindThread.setChargeReminderValue(appConfigBean.getChargeReminderValue());
                mRemindThread.setUsegeReminderValue(appConfigBean.getUsegeReminderValue());
                mRemindThread.setIsEnableChargeReminder(appConfigBean.isEnableChargeReminder());
                mRemindThread.setIsEnableUsegeReminder(appConfigBean.isEnableUsegeReminder());
                mRemindThread.setSleepTime(appConfigBean.getReminderIntervalTime());
                mRemindThread.setIsCharging(appConfigBean.isCharging());
                mRemindThread.setQuantityOfElectricity(appConfigBean.getCurrentValue());
                //LogUtils.d(TAG, "mRemindThread update.");
                return;
            }
        }
        mRemindThread.setChargeReminderValue(appConfigBean.getChargeReminderValue());
        mRemindThread.setUsegeReminderValue(appConfigBean.getUsegeReminderValue());
        mRemindThread.setSleepTime(appConfigBean.getReminderIntervalTime());
        mRemindThread.setIsCharging(appConfigBean.isCharging());
        mRemindThread.setQuantityOfElectricity(appConfigBean.getCurrentValue());
        mRemindThread.setIsEnableChargeReminder(appConfigBean.isEnableChargeReminder());
        mRemindThread.setIsEnableUsegeReminder(appConfigBean.isEnableUsegeReminder());
        mRemindThread.start();
        //LogUtils.d(TAG, "mRemindThread.start()");
    }

    public void stopRemindThread() {
        if (mRemindThread != null) {
            mRemindThread.setIsExist(true);
            mRemindThread = null;
        }
    }

    @Override
    public void onDestroy() {
        //LogUtils.d(TAG, "onDestroy");
        mAppConfigUtils.loadAppConfigBean();
        if (mAppConfigUtils.getIsEnableService() == false) {
            // 设置运行状态
            isServiceRunning = false;
            // 停止守护进程
            Intent intent = new Intent(this, AssistantService.class);
            stopService(intent);
            // 停止Receiver
            if (mControlCenterServiceReceiver != null) {
                unregisterReceiver(mControlCenterServiceReceiver);
                mControlCenterServiceReceiver = null;
            }
            // 停止前台通知栏
            stopForeground(true);
            // 停止消息提醒进程
            stopRemindThread();
            super.onDestroy();
            //LogUtils.d(TAG, "onDestroy done");
        }
    }

    // 主进程与守护进程连接时需要用到此类
    //
    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //LogUtils.d(TAG, "call onServiceConnected(...)");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //LogUtils.d(TAG, "call onServiceConnected(...)");
            if (mAppConfigUtils.getIsEnableService()) {
                // 唤醒守护进程
                wakeupAndBindAssistant();
            }
        }
    }

    public void appenRemindMSG(String szRemindMSG) {
        NotificationMessage notificationMessage = createNotificationMessage();
        notificationMessage.setRemindMSG(szRemindMSG);
        //LogUtils.d(TAG, "notificationMessage : " + notificationMessage.getRemindMSG());
        updateRemindNotification(notificationMessage);
    }

    //
    // 启动服务
    //
    public static void startControlCenterService(Context context) {
        Intent intent = new Intent(context, ControlCenterService.class);
        context.startForegroundService(intent);
    }

    //
    // 停止服务
    //
    public static void stopControlCenterService(Context context) {
        Intent intent = new Intent(context, ControlCenterService.class);
        context.stopService(intent);
    }

    public static void updateStatus(Context context, AppConfigBean appConfigBean) {
        //LogUtils.d(TAG, "updateStatus");
        // 创建一个Intent实例，定义广播的内容
        Intent intent = new Intent(ControlCenterServiceReceiver.ACTION_START_REMINDTHREAD);
        // 设置可选的Action数据，如额外信息
        intent.putExtra("appConfigBean", appConfigBean);
        // 发送广播
        context.sendBroadcast(intent);
    }

}

