package cc.winboll.studio.timestamp;

/**
 * @Author ZhanGSKen
 * @Date 2025/05/05 09:47
 * @Describe 主要服务
 */
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.RemoteViews;
import android.widget.TextView;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.timestamp.AssistantService;
import cc.winboll.studio.timestamp.MainService;
import cc.winboll.studio.timestamp.models.AppConfigs;
import cc.winboll.studio.timestamp.receivers.ButtonClickReceiver;
import cc.winboll.studio.timestamp.utils.NotificationHelper;
import cc.winboll.studio.timestamp.utils.ServiceUtil;
import java.lang.ref.WeakReference;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import android.content.IntentFilter;

public class MainService extends Service {

    public static String TAG = "MainService";

    public static final int MSG_UPDATE_TIMESTAMP = 0;

    ButtonClickReceiver mButtonClickReceiver;
    Intent intentMainService;
    Intent mButtonBroadcastIntent;
    PendingIntent mButtonPendingIntent;
    NotificationHelper mNotificationHelper;
    Notification notification;
    RemoteViews mRemoteViews;
    TextView mtvTimeStamp;
    Timer mTimer;
    private static boolean _mIsServiceAlive;
    public static final String EXTRA_APKFILEPATH = "EXTRA_APKFILEPATH";
    final static int MSG_INSTALL_APK = 0;
    MyHandler mMyHandler;
    MyServiceConnection mMyServiceConnection;
    MainActivity mInstallCompletedFollowUpActivity;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mRemoteViews = new RemoteViews(getPackageName(), R.layout.remoteviews_timestamp);
        intentMainService = new Intent(this, MainActivity.class);

        // 创建点击按钮后要发送的广播 Intent
        mButtonBroadcastIntent = new Intent(ButtonClickReceiver.BUTTON_COPYTIMESTAMP_ACTION);
        mButtonPendingIntent = PendingIntent.getBroadcast(
            this, // 上下文
            0, // 请求码，用于区分不同的 PendingIntent
            mButtonBroadcastIntent, // Intent
            PendingIntent.FLAG_UPDATE_CURRENT // 标志位，用于更新已存在的 PendingIntent
        );
        // 为按钮设置点击事件
        mRemoteViews.setOnClickPendingIntent(R.id.btn_copytimestamp, mButtonPendingIntent);
        
        // 创建广播接收器实例
        mButtonClickReceiver = new ButtonClickReceiver();

        // 创建 IntentFilter 并设置要接收的广播动作
        IntentFilter filter = new IntentFilter(ButtonClickReceiver.BUTTON_COPYTIMESTAMP_ACTION);

        // 注册广播接收器
        registerReceiver(mButtonClickReceiver, filter);
        
        LogUtils.d(TAG, "onCreate()");
        _mIsServiceAlive = false;
        mMyHandler = new MyHandler(MainService.this);
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
                mNotificationHelper = new NotificationHelper(this);
                //notification = helper.showForegroundNotification(intent, getString(R.string.app_name), getString(R.string.text_aboutservernotification));
                notification = mNotificationHelper.showCustomForegroundNotification(intentMainService, mRemoteViews, mRemoteViews);
                startForeground(NotificationHelper.FOREGROUND_NOTIFICATION_ID, notification);
                
                // 唤醒守护进程
                wakeupAndBindAssistant();

                LogUtils.d(TAG, "running...");
                mTimer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        //System.out.println("定时任务执行了");
                        mMyHandler.sendEmptyMessage(MSG_UPDATE_TIMESTAMP);
                    }
                };
                // 延迟1秒后开始执行，之后每隔2秒执行一次
                mTimer.schedule(task, 1000, 2000);


            } else {
                LogUtils.d(TAG, "_mIsServiceAlive is " + Boolean.toString(_mIsServiceAlive));

            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
        }

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

    void updateTimeStamp() {
        long currentMillis = System.currentTimeMillis();
        Instant instant = Instant.ofEpochMilli(currentMillis);
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = ldt.format(formatter);
        //System.out.println(formattedDateTime);
        mRemoteViews.setTextViewText(R.id.tv_timestamp, formattedDateTime);
        notification = mNotificationHelper.showCustomForegroundNotification(intentMainService, mRemoteViews, mRemoteViews);
        //startForeground(NotificationHelper.FOREGROUND_NOTIFICATION_ID, notification);
    }

    // 
    // 服务事务处理类
    //
    class MyHandler extends Handler {
        WeakReference<MainService> weakReference;  
        MyHandler(MainService service) {  
            weakReference = new WeakReference<MainService>(service);  
        }
        public void handleMessage(Message message) {
            MainService theService = weakReference.get();
            switch (message.what) {
                case MSG_UPDATE_TIMESTAMP:
                    {
                        if (theService != null) {
                            theService.updateTimeStamp();
                        }
                        break;
                    }
                default:
                    break;
            }
            super.handleMessage(message);
        }


    }

    public void sendUpdateTimeStampMessage() {

    }
}
