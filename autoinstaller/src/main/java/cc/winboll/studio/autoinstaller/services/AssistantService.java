package cc.winboll.studio.autoinstaller.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import cc.winboll.studio.autoinstaller.models.AppConfigs;
import cc.winboll.studio.autoinstaller.utils.ServiceUtil;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/05/27 20:16:00
 * @Describe MainService 守护进程服务
 */
public class AssistantService extends Service {

    public static final String TAG = "AssistantService";

    //MyBinder mMyBinder;
    MyServiceConnection mMyServiceConnection;
    volatile boolean mIsThreadAlive;

    @Override
    public IBinder onBind(Intent intent) {
        //return mMyBinder;
        return null;
    }

    @Override
    public void onCreate() {
        //LogUtils.d(TAG, "call onCreate()");
        super.onCreate();

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
        AppConfigs appConfigs = AppConfigs.getInstance(AssistantService.this).loadAppConfigs(AssistantService.this);
        return appConfigs.isEnableService() ? Service.START_STICKY: super.onStartCommand(intent, flags, startId);
    }

    /*class MyBinder extends IMyAidlInterface.Stub {
     @Override
     public String getServiceName() {
     return AssistantService.class.getSimpleName();
     }
     }*/

    @Override
    public void onDestroy() {
        //LogUtils.d(TAG, "call onDestroy()");
        mIsThreadAlive = false;
        super.onDestroy();
    }

    // 运行服务内容
    //
    void run() {
        //LogUtils.d(TAG, "call run()");
        AppConfigs appConfigs = AppConfigs.getInstance(AssistantService.this).loadAppConfigs(AssistantService.this);
        if (appConfigs.isEnableService()) {
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
        if (ServiceUtil.isServiceAlive(getApplicationContext(), MainService.class.getName()) == false) {
            //LogUtils.d(TAG, "wakeupAndBindMain() Wakeup... ControlCenterService");
            startForegroundService(new Intent(AssistantService.this, MainService.class));
        }
        //LogUtils.d(TAG, "wakeupAndBindMain() Bind... ControlCenterService");
        bindService(new Intent(AssistantService.this, MainService.class), mMyServiceConnection, Context.BIND_IMPORTANT);
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
            AppConfigs appConfigs = AppConfigs.getInstance(AssistantService.this).loadAppConfigs(AssistantService.this);
            if (appConfigs.isEnableService()) {
                wakeupAndBindMain();
            }
        }
    }
}
