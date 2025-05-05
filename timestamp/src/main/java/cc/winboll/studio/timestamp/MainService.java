package cc.winboll.studio.timestamp;

/**
 * @Author ZhanGSKen
 * @Date 2025/05/05 09:47
 * @Describe 主要服务
 */
import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.timestamp.AssistantService;
import cc.winboll.studio.timestamp.MainService;
import cc.winboll.studio.timestamp.models.AppConfigs;
import cc.winboll.studio.timestamp.utils.NotificationHelper;
import cc.winboll.studio.timestamp.utils.ServiceUtil;

public class MainService extends Service {

    public static String TAG = "MainService";

    Notification notification;
    private static boolean _mIsServiceAlive;
    public static final String EXTRA_APKFILEPATH = "EXTRA_APKFILEPATH";
    final static int MSG_INSTALL_APK = 0;
    //Handler mHandler;
    MyServiceConnection mMyServiceConnection;
    MainActivity mInstallCompletedFollowUpActivity;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "onCreate()");
        _mIsServiceAlive = false;
        //mHandler = new MyHandler(MainService.this);
        if (mMyServiceConnection == null) {
            mMyServiceConnection = new MyServiceConnection();
        }

        run();
    }

    private void run() {
        AppConfigs appConfigs = AppConfigs.getInstance(MainService.this).loadAppConfigs();
        if (appConfigs.isEnableService()) {
            if (_mIsServiceAlive == false) {
                // 设置运行状态
                _mIsServiceAlive = true;


                // 显示前台通知栏
                NotificationHelper helper = new NotificationHelper(this);
                Intent intent = new Intent(this, MainActivity.class);
                notification = helper.showForegroundNotification(intent, getString(R.string.app_name), getString(R.string.text_aboutservernotification));
                startForeground(NotificationHelper.FOREGROUND_NOTIFICATION_ID, notification);

                // 唤醒守护进程
                wakeupAndBindAssistant();

                LogUtils.d(TAG, "running...");

            } else {
                LogUtils.d(TAG, "_mIsServiceAlive is " + Boolean.toString(_mIsServiceAlive));

            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        _mIsServiceAlive = false;
        LogUtils.d(TAG, "onDestroy()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "onStartCommand");

        run();
        AppConfigs appConfigs = AppConfigs.getInstance(MainService.this).loadAppConfigs();

        return appConfigs.isEnableService() ? Service.START_STICKY: super.onStartCommand(intent, flags, startId);
    }

    public static void setMainServiceStatus(Context context, boolean isEnable) {
        AppConfigs appConfigs = AppConfigs.getInstance(context).loadAppConfigs();
        appConfigs.setIsEnableService(isEnable);
        Intent intent = new Intent(context, MainService.class);
        if (isEnable) {
            context.startService(intent);
        } else {
            context.stopService(intent);
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
            AppConfigs appConfigs = AppConfigs.getInstance(MainService.this).loadAppConfigs();
            if (appConfigs.isEnableService()) {
                // 唤醒守护进程
                wakeupAndBindAssistant();
            }
        }
    }

    // 唤醒和绑定守护进程
    //
    void wakeupAndBindAssistant() {
        if (ServiceUtil.isServiceAlive(getApplicationContext(), AssistantService.class.getName()) == false) {
            startService(new Intent(MainService.this, AssistantService.class));
            //LogUtils.d(TAG, "call wakeupAndBindAssistant() : Binding... AssistantService");
            bindService(new Intent(MainService.this, AssistantService.class), mMyServiceConnection, Context.BIND_IMPORTANT);
        }
    }

    // 
    // 服务事务处理类
    //
//    static class MyHandler extends Handler {
//        WeakReference<MainService> weakReference;  
//        MyHandler(MainService service) {  
//            weakReference = new WeakReference<MainService>(service);  
//        }
//        public void handleMessage(Message message) {
//            MainService theActivity = weakReference.get();
//            switch (message.what) {
//                case MSG_INSTALL_APK:
//                    {
//                        break;
//                    }
//                default:
//                    break;
//            }
//            super.handleMessage(message);
//        }
//    }
}
