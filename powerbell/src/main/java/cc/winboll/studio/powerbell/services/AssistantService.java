package cc.winboll.studio.powerbell.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import cc.winboll.studio.powerbell.GlobalApplication;
import cc.winboll.studio.powerbell.services.ControlCenterService;
import cc.winboll.studio.powerbell.utils.AppConfigUtils;
import cc.winboll.studio.powerbell.utils.ServiceUtils;

public class AssistantService extends Service {
    private final static String TAG = "AssistantService";

    //MyBinder mMyBinder;
    MyServiceConnection mMyServiceConnection;
    volatile boolean mIsThreadAlive;
    AppConfigUtils mAppConfigUtils;

    @Override
    public IBinder onBind(Intent intent) {
        //return mMyBinder;
        return null;
    }

    @Override
    public void onCreate() {
        //LogUtils.d(TAG, "onCreate");
        super.onCreate();
        mAppConfigUtils = GlobalApplication.getAppConfigUtils(this);

        //mMyBinder = new MyBinder();
        if (mMyServiceConnection == null) {
            mMyServiceConnection = new MyServiceConnection();
        }
        // 设置运行参数
        mIsThreadAlive = false;
        run();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //LogUtils.d(TAG, "call onStartCommand(...)");
        run();
        return START_STICKY;
    }

    /*class MyBinder extends IMyAidlInterface.Stub {
        @Override
        public String getServiceName() {
            return AssistantService.class.getSimpleName();
        }
    }*/

    @Override
    public void onDestroy() {
        //LogUtils.d(TAG, "onDestroy");
        mIsThreadAlive = false;
        super.onDestroy();
    }

    // 运行服务内容
    //
    void run() {
        //LogUtils.d(TAG, "run");
        if (mAppConfigUtils.getIsEnableService()) {
            if (mIsThreadAlive == false) {
                // 设置运行状态
                mIsThreadAlive = true;
                // 唤醒和绑定主进程
                wakeupAndBindMain();
            }
        }
    }

    // 唤醒和绑定主进程
    //
    void wakeupAndBindMain() {
        if (ServiceUtils.isServiceAlive(getApplicationContext(), ControlCenterService.class.getName()) == false) {
            //LogUtils.d(TAG, "wakeupAndBindMain() Wakeup... ControlCenterService");
            startForegroundService(new Intent(AssistantService.this, ControlCenterService.class));
        }
        //LogUtils.d(TAG, "wakeupAndBindMain() Bind... ControlCenterService");
        bindService(new Intent(AssistantService.this, ControlCenterService.class), mMyServiceConnection, Context.BIND_IMPORTANT);
    }

    // 主进程与守护进程连接时需要用到此类
    //
    class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //LogUtils.d(TAG, "call onServiceConnected(...)");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //LogUtils.d(TAG, "call onServiceDisconnected(...)");
            if (mAppConfigUtils.getIsEnableService()) {
                wakeupAndBindMain();
            }
        }
    }
}
