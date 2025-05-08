package cc.winboll.studio.contacts.services;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/14 03:38:31
 * @Describe 守护进程服务
 */
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import cc.winboll.studio.contacts.beans.MainServiceBean;
import cc.winboll.studio.contacts.services.MainService;
import cc.winboll.studio.libappbase.LogUtils;

public class AssistantService extends Service {

    public static final String TAG = "AssistantService";

    MainServiceBean mMainServiceBean;
    MyServiceConnection mMyServiceConnection;
    MainService mMainService;
    boolean isBound = false;
    volatile boolean isThreadAlive = false;

    public synchronized void setIsThreadAlive(boolean isThreadAlive) {
        LogUtils.d(TAG, "setIsThreadAlive(...)");
        LogUtils.d(TAG, String.format("isThreadAlive %s", isThreadAlive));
        this.isThreadAlive = isThreadAlive;
    }

    public boolean isThreadAlive() {
        return isThreadAlive;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public void onCreate() {
        LogUtils.d(TAG, "onCreate");
        super.onCreate();

        //mMyBinder = new MyBinder();
        if (mMyServiceConnection == null) {
            mMyServiceConnection = new MyServiceConnection();
        }
        // 设置运行参数
        setIsThreadAlive(false);
        assistantService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "call onStartCommand(...)");
        assistantService();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //LogUtils.d(TAG, "onDestroy");
        setIsThreadAlive(false);
        // 解除绑定
        if (isBound) {
            unbindService(mMyServiceConnection);
            isBound = false;
        }
        super.onDestroy();
    }

    // 运行服务内容
    //
    void assistantService() {
        LogUtils.d(TAG, "assistantService()");
        mMainServiceBean = MainServiceBean.loadBean(this, MainServiceBean.class);
        LogUtils.d(TAG, String.format("mMainServiceBean.isEnable() %s", mMainServiceBean.isEnable()));
        if (mMainServiceBean.isEnable()) {
            LogUtils.d(TAG, String.format("mIsThreadAlive %s", isThreadAlive()));
            if (isThreadAlive() == false) {
                // 设置运行状态
                setIsThreadAlive(true);
                // 唤醒和绑定主进程
                wakeupAndBindMain();
            }
        }
    }

    // 唤醒和绑定主进程
    //
    void wakeupAndBindMain() {
        LogUtils.d(TAG, "wakeupAndBindMain()");
        // 绑定服务的Intent
        Intent intent = new Intent(this, MainService.class);
        startService(new Intent(this, MainService.class));
        bindService(intent, mMyServiceConnection, Context.BIND_IMPORTANT);

//        startService(new Intent(this, MainService.class));
//        bindService(new Intent(AssistantService.this, MainService.class), mMyServiceConnection, Context.BIND_IMPORTANT);
    }

    // 主进程与守护进程连接时需要用到此类
    //
    class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.d(TAG, "onServiceConnected(...)");
            MainService.MyBinder binder = (MainService.MyBinder) service;
            mMainService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.d(TAG, "onServiceDisconnected(...)");
            mMainServiceBean = MainServiceBean.loadBean(AssistantService.this, MainServiceBean.class);
            if (mMainServiceBean.isEnable()) {
                wakeupAndBindMain();
            }
            isBound = false;
            mMainService = null;
        }
    }

    // 用于返回服务实例的Binder
    public class MyBinder extends Binder {
        AssistantService getService() {
            LogUtils.d(TAG, "AssistantService MyBinder getService()");
            return AssistantService.this;
        }
    }
}
