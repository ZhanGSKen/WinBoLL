package cc.winboll.studio.mymessagemanager.services;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/07/19 14:30:57
 * @Describe 应用主要服务组件类守护进程服务组件类
 */
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import cc.winboll.studio.mymessagemanager.utils.AppConfigUtil;
import cc.winboll.studio.mymessagemanager.utils.ServiceUtil;

public class AssistantService extends Service {

    public final static String TAG = "AssistantService";

    MyServiceConnection mMyServiceConnection;
    volatile boolean mIsServiceRunning;
    AppConfigUtil mConfigUtil;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mConfigUtil = AppConfigUtil.getInstance(this);
        if (mMyServiceConnection == null) {
            mMyServiceConnection = new MyServiceConnection();
        }
        // 设置运行参数
        mIsServiceRunning = false;
        run();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        run();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mIsServiceRunning = false;
        super.onDestroy();
    }

    //
    // 运行服务内容
    //
    void run() {
        mConfigUtil.reLoadConfig();
        if (mConfigUtil.mAppConfigBean.isEnableService()) {
            if (mIsServiceRunning == false) {
                // 设置运行状态
                mIsServiceRunning = true;
                // 唤醒和绑定主进程
                wakeupAndBindMain();
            }
        }
    }

    //
    // 唤醒和绑定主进程
    //
    void wakeupAndBindMain() {
        if (ServiceUtil.isServiceAlive(getApplicationContext(), MainService.class.getName()) == false) {
            startForegroundService(new Intent(AssistantService.this, MainService.class));
        }

        bindService(new Intent(AssistantService.this, MainService.class), mMyServiceConnection, Context.BIND_IMPORTANT);
    }

    //
    // 主进程与守护进程连接时需要用到此类
    //
    class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mConfigUtil.reLoadConfig();
            if (mConfigUtil.mAppConfigBean.isEnableService()) {
                wakeupAndBindMain();
            }
        }
    }
}
