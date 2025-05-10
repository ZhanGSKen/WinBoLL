package cc.winboll.studio.mymessagemanager.services;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/07/19 14:30:57
 * @Describe 应用主要服务组件类
 */
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.beans.MessageNotificationBean;
import cc.winboll.studio.mymessagemanager.receivers.SMSRecevier;
import cc.winboll.studio.mymessagemanager.services.MainService;
import cc.winboll.studio.mymessagemanager.utils.AppConfigUtil;
import cc.winboll.studio.mymessagemanager.utils.NotificationUtil;
import cc.winboll.studio.mymessagemanager.utils.ServiceUtil;
import cc.winboll.studio.shared.log.LogUtils;
import com.hjq.toast.ToastUtils;

public class MainService extends Service {

    public static String TAG = "ManagerService";

    AppConfigUtil mConfigUtil;
    //MyBinder mMyBinder;
    MyServiceConnection mMyServiceConnection;
    volatile static boolean _mIsServiceAlive;
    SMSRecevier mSMSRecevier;

    @Override
    public IBinder onBind(Intent intent) {
        //return mMyBinder;
        return null;
    }

    @Override
    public void onCreate() {
        LogUtils.d(TAG, "onCreate");
        super.onCreate();
        _mIsServiceAlive = false;
        mConfigUtil = AppConfigUtil.getInstance(this);

        //mMyBinder = new MyBinder();
        if (mMyServiceConnection == null) {
            mMyServiceConnection = new MyServiceConnection();
        }

        // 运行服务内容
        run();

    }

    private void run() {
        //LogUtils.d(TAG, "run");
        mConfigUtil.reLoadConfig();
        if (mConfigUtil.mAppConfigBean.isEnableService()) {
            if (_mIsServiceAlive == false) {
                // 设置运行状态
                _mIsServiceAlive = true;
                //LogUtils.d(TAG, "_mIsServiceAlive set to true.");

                // 唤醒守护进程
                wakeupAndBindAssistant();

                // 运行其它服务内容
                IntentFilter localIntentFilter = new IntentFilter(SMSRecevier.ACTION_SMS_RECEIVED);
                localIntentFilter.setPriority(1);
                mSMSRecevier = new SMSRecevier();
                registerReceiver(mSMSRecevier, localIntentFilter);


                // 显示前台通知栏
                MessageNotificationBean notificationMessage = createNotificationMessage();
                NotificationUtil nu = new NotificationUtil();
                nu.sendForegroundNotification(MainService.this, notificationMessage);

                /*if (mConfigUtil.isEnableTTS()) {
                 TTSPlayRuleUtil.speakText(ManagerService.this, getString(R.string.text_iamhere), 0);
                 GlobalApplication.showApplicationMessage(getString(R.string.text_iamhere));
                 }*/
                 
                ToastUtils.show("Service is start.");
                LogUtils.i(TAG, "Service is start.");
            }
        }
    }

    public interface SMSListener {
        void speakMessage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mSMSRecevier);

    }

    private MessageNotificationBean createNotificationMessage() {
        String szTitle = getApplicationContext().getString(R.string.app_name);
        String szContent = getString(R.string.text_aboutservernotification);
        return new MessageNotificationBean(NotificationUtil.ID_MSG_SERVICE, "", szTitle, szContent);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        run();
        mConfigUtil.reLoadConfig();
        return mConfigUtil.mAppConfigBean.isEnableService() ? Service.START_STICKY: super.onStartCommand(intent, flags, startId);
    }

   /*private class MyBinder extends IMyAidlInterface.Stub {
        @Override
        public String getServiceName() {
            return MainService.class.getSimpleName();
        }
    }*/

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
            mConfigUtil.reLoadConfig();
            if (mConfigUtil.mAppConfigBean.isEnableService()) {
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



}
