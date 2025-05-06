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
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.RemoteViews;
import android.widget.TextView;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.timestamp.AssistantService;
import cc.winboll.studio.timestamp.MainService;
import cc.winboll.studio.timestamp.models.AppConfigsModel;
import cc.winboll.studio.timestamp.receivers.ButtonClickReceiver;
import cc.winboll.studio.timestamp.utils.AppConfigsUtil;
import cc.winboll.studio.timestamp.utils.NotificationHelper;
import cc.winboll.studio.timestamp.utils.ServiceUtil;
import cc.winboll.studio.timestamp.utils.TimeStampRemoteViewsUtil;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class MainService extends Service {

    public static String TAG = "MainService";

    public static final int MSG_UPDATE_TIMESTAMP = 0;

    ButtonClickReceiver mButtonClickReceiver;
    NotificationHelper mNotificationHelper;
    Notification mNotification;
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
        // 创建 RemoteViews 对象，并使用包含自定义 View 的布局
        //mRemoteViews = new RemoteViews(getPackageName(), R.layout.remoteviews_timestamp);


        // 创建广播接收器实例
        mButtonClickReceiver = new ButtonClickReceiver();

        // 创建 IntentFilter 并设置要接收的广播动作
        IntentFilter filter = new IntentFilter(ButtonClickReceiver.BUTTON_COPYTIMESTAMP_ACTION);

        // 注册广播接收器
        registerReceiver(mButtonClickReceiver, filter);

        LogUtils.d(TAG, "onCreate()");
        _mIsServiceAlive = false;

        mMyHandler = new MyHandler();
        if (mMyServiceConnection == null) {
            mMyServiceConnection = new MyServiceConnection();
        }

        run();
    }

    private void run() {
        AppConfigsModel appConfigs = AppConfigsUtil.getInstance(MainService.this).loadAppConfigs();
        if (appConfigs.isEnableService()) {
            if (_mIsServiceAlive == false) {
                // 设置运行状态
                _mIsServiceAlive = true;

                // 显示前台通知栏
//                mNotificationHelper = new NotificationHelper(this);
//                //notification = helper.showForegroundNotification(intent, getString(R.string.app_name), getString(R.string.text_aboutservernotification));
//                mNotification = mNotificationHelper.showCustomForegroundNotification(new Intent(this, MainActivity.class), mRemoteViews, mRemoteViews);
//                startForeground(NotificationHelper.FOREGROUND_NOTIFICATION_ID, mNotification);

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
                // 延迟1秒后开始执行，之后每隔100毫秒执行一次
                mTimer.schedule(task, 1000, 100);



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
        AppConfigsModel appConfigs = AppConfigsUtil.getInstance(MainService.this).loadAppConfigs();

        return appConfigs.isEnableService() ? Service.START_STICKY: super.onStartCommand(intent, flags, startId);
    }

    public static void setMainServiceStatus(Context context, boolean isEnable) {
        AppConfigsModel appConfigs = AppConfigsUtil.getInstance(context).loadAppConfigs();
        appConfigs.setIsEnableService(isEnable);
        AppConfigsUtil.getInstance(context).saveAppConfigs();

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
            AppConfigsModel appConfigs = AppConfigsUtil.getInstance(MainService.this).loadAppConfigs();
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

//    void updateTimeStamp() {
//        long currentMillis = System.currentTimeMillis();
//        Instant instant = Instant.ofEpochMilli(currentMillis);
//        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
//        String szTimeStampFormatString = AppConfigs.getInstance(this).getTimeStampFormatString();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(szTimeStampFormatString);
//        String formattedDateTime = ldt.format(formatter);
//        //System.out.println(formattedDateTime);
//        mRemoteViews.setTextViewText(R.id.tv_timestamp, formattedDateTime);
//        notification = mNotificationHelper.showCustomForegroundNotification(intentMainService, mRemoteViews, mRemoteViews);
//        //startForeground(NotificationHelper.FOREGROUND_NOTIFICATION_ID, notification);
//    }
//
    // 
    // 服务事务处理类
    //
    class MyHandler extends Handler {

        public void handleMessage(Message message) {
            switch (message.what) {
                case MSG_UPDATE_TIMESTAMP:
                    {
                        long currentMillis = System.currentTimeMillis();
                        Instant instant = Instant.ofEpochMilli(currentMillis);
                        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                        String szTimeStampFormatString = AppConfigsUtil.getInstance(MainService.this).getAppConfigsModel().getTimeStampFormatString();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(szTimeStampFormatString);
                        String formattedDateTime = ldt.format(formatter);
                        TimeStampRemoteViewsUtil.getInstance(MainService.this).showNotification(formattedDateTime);

                        //LogUtils.d(TAG, "Hello, World");
                        break;
                    }
                default:
                    break;
            }
            super.handleMessage(message);
        }
    }
}
