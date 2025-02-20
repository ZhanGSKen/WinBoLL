package cc.winboll.studio.appbase.services;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/13 06:56:41
 * @Describe 拨号主服务
 * 参考:
 * 进程保活-双进程守护的正确姿势
 * https://blog.csdn.net/sinat_35159441/article/details/75267380
 * Android Service之onStartCommand方法研究
 * https://blog.csdn.net/cyp331203/article/details/38920491
 */
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import cc.winboll.studio.appbase.MyTileService;
import cc.winboll.studio.appbase.beans.MainServiceBean;
import cc.winboll.studio.appbase.handlers.MainServiceHandler;
import cc.winboll.studio.appbase.receivers.MainReceiver;
import cc.winboll.studio.appbase.services.AssistantService;
import cc.winboll.studio.appbase.threads.MainServiceThread;
import cc.winboll.studio.appbase.widgets.SOSWidget;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.bean.APPSOSBean;
import java.util.ArrayList;

public class MainService extends Service {

    public static final String TAG = "MainService";

    public static final int MSG_UPDATE_STATUS = 0;

    static MainService _mControlCenterService;

    volatile boolean isServiceRunning;

    MainServiceBean mMainServiceBean;
    MainServiceThread mMainServiceThread;
    MainServiceHandler mMainServiceHandler;
    MyServiceConnection mMyServiceConnection;
    AssistantService mAssistantService;
    boolean isBound = false;
    MainReceiver mMainReceiver;
    ArrayList<SOSConnection> mSOSConnectionList;

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public MainServiceThread getRemindThread() {
        return mMainServiceThread;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "onCreate()");
        mSOSConnectionList = new ArrayList<SOSConnection>();

        _mControlCenterService = MainService.this;
        isServiceRunning = false;
        mMainServiceBean = MainServiceBean.loadBean(this, MainServiceBean.class);

        if (mMyServiceConnection == null) {
            mMyServiceConnection = new MyServiceConnection();
        }
        mMainServiceHandler = new MainServiceHandler(this);

        // 运行服务内容
        mainService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "onStartCommand(...)");
        // 运行服务内容
        mainService();
        return (mMainServiceBean.isEnable()) ? START_STICKY : super.onStartCommand(intent, flags, startId);
    }

    // 运行服务内容
    //
    void mainService() {
        LogUtils.d(TAG, "mainService()");
        mMainServiceBean = MainServiceBean.loadBean(this, MainServiceBean.class);
        if (mMainServiceBean.isEnable() && isServiceRunning == false) {
            LogUtils.d(TAG, "mainService() start running");
            isServiceRunning = true;
            // 唤醒守护进程
            wakeupAndBindAssistant();

            if (mMainReceiver == null) {
                // 注册广播接收器
                mMainReceiver = new MainReceiver(this);
                mMainReceiver.registerAction(this);
            }

            // 启动小部件
            Intent intentTimeWidget = new Intent(this, SOSWidget.class);
            intentTimeWidget.setAction(SOSWidget.ACTION_RELOAD_REPORT);
            this.sendBroadcast(intentTimeWidget);

            startMainServiceThread();

            MyTileService.updateServiceIconStatus(this);

            LogUtils.i(TAG, "Main Service Is Start.");
        }
    }

    // 唤醒和绑定守护进程
    //
    void wakeupAndBindAssistant() {
        LogUtils.d(TAG, "wakeupAndBindAssistant()");
//        if (ServiceUtils.isServiceAlive(getApplicationContext(), AssistantService.class.getName()) == false) {
//            startService(new Intent(MainService.this, AssistantService.class));
//            //LogUtils.d(TAG, "call wakeupAndBindAssistant() : Binding... AssistantService");
//            bindService(new Intent(MainService.this, AssistantService.class), mMyServiceConnection, Context.BIND_IMPORTANT);
//        }
        Intent intent = new Intent(this, AssistantService.class);
        startService(intent);
        // 绑定服务的Intent
        //Intent intent = new Intent(this, AssistantService.class);
        bindService(intent, mMyServiceConnection, Context.BIND_IMPORTANT);

//        Intent intent = new Intent(this, AssistantService.class);
//        startService(intent);
//        LogUtils.d(TAG, "startService(intent)");
//        bindService(new Intent(this, AssistantService.class), mMyServiceConnection, Context.BIND_IMPORTANT);
    }

    // 开启提醒铃声线程
    //
    public void startMainServiceThread() {
        LogUtils.d(TAG, "startMainServiceThread");
        if (mMainServiceThread == null) {
            mMainServiceThread = new MainServiceThread(this, mMainServiceHandler);
            LogUtils.d(TAG, "new MainServiceThread");
        } else {
            if (mMainServiceThread.isExist() == true) {
                mMainServiceThread = new MainServiceThread(this, mMainServiceHandler);
                LogUtils.d(TAG, "renew MainServiceThread");
            } else {
                // 提醒进程正在进行中就更新状态后退出
                LogUtils.d(TAG, "A mMainServiceThread running.");
                return;
            }
        }
        mMainServiceThread.start();
    }

    public void stopRemindThread() {
        if (mMainServiceThread != null) {
            mMainServiceThread.setIsExist(true);
            mMainServiceThread = null;
        }
    }

    @Override
    public void onDestroy() {
        //LogUtils.d(TAG, "onDestroy");
        mMainServiceBean = MainServiceBean.loadBean(this, MainServiceBean.class);
        if (mMainServiceBean.isEnable() == false) {
            // 设置运行状态
            isServiceRunning = false;// 解除绑定
            if (isBound) {
                unbindService(mMyServiceConnection);
                isBound = false;
            }
            // 停止守护进程
            Intent intent = new Intent(this, AssistantService.class);
            stopService(intent);
            // 停止Receiver
            if (mMainReceiver != null) {
                unregisterReceiver(mMainReceiver);
                mMainReceiver = null;
            }
            // 停止前台通知栏
            stopForeground(true);
            // 停止消息提醒进程
            stopRemindThread();

            MyTileService.updateServiceIconStatus(this);

            super.onDestroy();
            //LogUtils.d(TAG, "onDestroy done");
        }
    }

    public void bindSOSConnection(APPSOSBean bean) {
        LogUtils.d(TAG, "bindSOSConnection(...)");
        // 清理旧的绑定链接
        for (int i = mSOSConnectionList.size() - 1; i > -1; i--) {
            SOSConnection item = mSOSConnectionList.get(i);
            if (item.isBindToAPPSOSBean(bean)) {
                LogUtils.d(TAG, "Bind Servive exist.");
                unbindService(item);
                mSOSConnectionList.remove(i);
            }
        }

        // 绑定服务
        SOSConnection sosConnection = new SOSConnection();
        Intent intentService = new Intent();
        intentService.setComponent(new ComponentName(bean.getSosPackage(), bean.getSosClassName()));
        bindService(intentService, sosConnection, Context.BIND_IMPORTANT);
        mSOSConnectionList.add(sosConnection);
        
        Intent intentWidget = new Intent(this, SOSWidget.class);
        intentWidget.setAction(SOSWidget.ACTION_WAKEUP_SERVICE);
        APPSOSBean appSOSBean = new APPSOSBean(bean.getSosPackage(), bean.getSosClassName());
        intentWidget.putExtra("APPSOSBean", appSOSBean.toString());
        sendBroadcast(intentWidget);
    }

    public class SOSConnection implements ServiceConnection {

        ComponentName mComponentName;

        boolean isBindToAPPSOSBean(APPSOSBean bean) {
            return mComponentName != null
                && mComponentName.getClassName().equals(bean.getSosClassName())
                && mComponentName.getPackageName().equals(bean.getSosPackage());
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.d(TAG, "onServiceConnected(...)");
            mComponentName = name;
            LogUtils.d(TAG, String.format("onServiceConnected : \ngetClassName %s\ngetPackageName %s", name.getClassName(), name.getPackageName()));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.d(TAG, "onServiceDisconnected(...)");
            LogUtils.d(TAG, String.format("onServiceDisconnected : \ngetClassName %s\ngetPackageName %s", name.getClassName(), name.getPackageName()));

            // 尝试无参数启动一下服务
            String sosPackage = mComponentName.getPackageName();
            LogUtils.d(TAG, String.format("sosPackage %s", sosPackage));
            String sosClassName = mComponentName.getClassName();
            LogUtils.d(TAG, String.format("sosClassName %s", sosClassName));

            Intent intentService = new Intent();
            intentService.setComponent(new ComponentName(sosPackage, sosClassName));
            startService(intentService);
        }

    }

    // 主进程与守护进程连接时需要用到此类
    //
    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.d(TAG, "onServiceConnected(...)");
            AssistantService.MyBinder binder = (AssistantService.MyBinder) service;
            mAssistantService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.d(TAG, "onServiceDisconnected(...)");

            if (mMainServiceBean.isEnable()) {
                // 唤醒守护进程
                wakeupAndBindAssistant();
            }
            isBound = false;
            mAssistantService = null;
        }

    }


    // 用于返回服务实例的Binder
    public class MyBinder extends Binder {
        MainService getService() {
            LogUtils.d(TAG, "MainService MyBinder getService()");
            return MainService.this;
        }
    }

//    //
//    // 启动服务
//    //
//    public static void startControlCenterService(Context context) {
//        Intent intent = new Intent(context, MainService.class);
//        context.startForegroundService(intent);
//    }
//
//    //
//    // 停止服务
//    //
//    public static void stopControlCenterService(Context context) {
//        Intent intent = new Intent(context, MainService.class);
//        context.stopService(intent);
//    }

    public void appenMessage(String message) {
        LogUtils.d(TAG, String.format("Message : %s", message));
    }

    public static void stopMainService(Context context) {
        LogUtils.d(TAG, "stopMainService");
        MainServiceBean bean = new MainServiceBean();
        bean.setIsEnable(false);
        MainServiceBean.saveBean(context, bean);
        context.stopService(new Intent(context, MainService.class));
    }

    public static void startMainService(Context context) {
        LogUtils.d(TAG, "startMainService");
        MainServiceBean bean = new MainServiceBean();
        bean.setIsEnable(true);
        MainServiceBean.saveBean(context, bean);
        context.startService(new Intent(context, MainService.class));
    }
}

