package cc.winboll.studio.contacts.services;

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
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import cc.winboll.studio.contacts.beans.MainServiceBean;
import cc.winboll.studio.contacts.beans.RingTongBean;
import cc.winboll.studio.contacts.bobulltoon.TomCat;
import cc.winboll.studio.contacts.dun.Rules;
import cc.winboll.studio.contacts.handlers.MainServiceHandler;
import cc.winboll.studio.contacts.listenphonecall.CallListenerService;
import cc.winboll.studio.contacts.receivers.MainReceiver;
import cc.winboll.studio.contacts.services.MainService;
import cc.winboll.studio.contacts.threads.MainServiceThread;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.sos.SOS;
import java.util.Timer;
import java.util.TimerTask;
import cc.winboll.studio.libappbase.sos.WinBoll;
import cc.winboll.studio.contacts.App;
import cc.winboll.studio.libappbase.sos.APPModel;

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
    Timer mStreamVolumeCheckTimer;
    static volatile TomCat _TomCat;

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
        _mControlCenterService = MainService.this;
        isServiceRunning = false;
        mMainServiceBean = MainServiceBean.loadBean(this, MainServiceBean.class);

        if (mMyServiceConnection == null) {
            mMyServiceConnection = new MyServiceConnection();
        }
        mMainServiceHandler = new MainServiceHandler(this);

        // 铃声检查定时器
        mStreamVolumeCheckTimer = new Timer();
        mStreamVolumeCheckTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                    int ringerVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                    // 恢复铃声音量，预防其他意外条件导致的音量变化问题
                    //

                    // 读取应用配置，未配置就初始化配置文件
                    RingTongBean bean = RingTongBean.loadBean(MainService.this, RingTongBean.class);
                    if (bean == null) {
                        // 初始化配置
                        bean = new RingTongBean();
                        RingTongBean.saveBean(MainService.this, bean);
                    }
                    // 如果当前音量和应用保存的不一致就恢复为应用设定值
                    // 恢复铃声音量
                    try {
                        if (ringerVolume != bean.getStreamVolume()) {
                            audioManager.setStreamVolume(AudioManager.STREAM_RING, bean.getStreamVolume(), 0);
                            //audioManager.setMode(AudioManager.RINGER_MODE_NORMAL);
                        }
                    } catch (java.lang.SecurityException e) {
                        LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                    }
                }
            }, 1000, 60000);

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
            // 召唤 WinBoll APP 绑定本服务
            if (App.isDebuging()) {
                WinBoll.bindToAPPBaseBeta(this, MainService.class.getName());
            } else {
                WinBoll.bindToAPPBase(this, MainService.class.getName());
            }

            // 初始化服务运行参数
            _TomCat = TomCat.getInstance(this);
            if (!_TomCat.loadPhoneBoBullToon()) {
                LogUtils.d(TAG, "没有下载 BoBullToon 数据。BoBullToon 参数无法加载。");
            }

            if (mMainReceiver == null) {
                // 注册广播接收器
                mMainReceiver = new MainReceiver(this);
                mMainReceiver.registerAction(this);
            }

            Rules.getInstance(this).loadRules();

            startPhoneCallListener();

            MainServiceThread.getInstance(this, mMainServiceHandler).start();

            LogUtils.i(TAG, "Main Service Is Start.");
        }
    }

    public static boolean isPhoneInBoBullToon(String phone) {
        if (_TomCat != null) {
            return _TomCat.isPhoneBoBullToon(phone);
        }
        return false;
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

    void startPhoneCallListener() {
        Intent callListener = new Intent(this, CallListenerService.class);
        startService(callListener);
    }

    @Override
    public void onDestroy() {
        //LogUtils.d(TAG, "onDestroy");
        mMainServiceBean = MainServiceBean.loadBean(this, MainServiceBean.class);
        //LogUtils.d(TAG, "onDestroy done");
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

            // 停止主要进程
            MainServiceThread.getInstance(this, mMainServiceHandler).setIsExit(true);

        }

        super.onDestroy();
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
                if (App.isDebuging()) {
                    SOS.sosToAppBase(getApplicationContext(), MainService.class.getName());
                } else {
                    SOS.sosToAppBaseBeta(getApplicationContext(), MainService.class.getName());
                }
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
        context.stopService(new Intent(context, MainService.class));
    }

    public static void startMainService(Context context) {
        LogUtils.d(TAG, "startMainService");
        context.startService(new Intent(context, MainService.class));
    }

    public static void restartMainService(Context context) {
        LogUtils.d(TAG, "restartMainService");

        MainServiceBean bean = MainServiceBean.loadBean(context, MainServiceBean.class);
        if (bean != null && bean.isEnable()) {
            context.stopService(new Intent(context, MainService.class));
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
//            }
            context.startService(new Intent(context, MainService.class));
            LogUtils.d(TAG, "已重启 MainService");
        }
    }

    public static void stopMainServiceAndSaveStatus(Context context) {
        LogUtils.d(TAG, "stopMainServiceAndSaveStatus");
        MainServiceBean bean = new MainServiceBean();
        bean.setIsEnable(false);
        MainServiceBean.saveBean(context, bean);
        context.stopService(new Intent(context, MainService.class));
    }

    public static void startMainServiceAndSaveStatus(Context context) {
        LogUtils.d(TAG, "startMainServiceAndSaveStatus");
        MainServiceBean bean = new MainServiceBean();
        bean.setIsEnable(true);
        MainServiceBean.saveBean(context, bean);
        context.startService(new Intent(context, MainService.class));
    }
}

