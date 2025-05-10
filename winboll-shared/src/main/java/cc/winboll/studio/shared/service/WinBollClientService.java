package cc.winboll.studio.shared.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import cc.winboll.studio.shared.log.LogUtils;
import cc.winboll.studio.shared.util.ServiceUtils;
import com.hjq.toast.ToastUtils;
import java.io.FileDescriptor;
import android.graphics.drawable.Drawable;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/12/08 19:42:07
 * @Describe WinBoll 客户端服务
 */
public class WinBollClientService extends Service implements IWinBollClientServiceBinder {

    public static final String TAG = "WinBollClientService";

    WinBollClientServiceBean mWinBollClientServiceBean;
    MyServiceConnection mMyServiceConnection;
    volatile boolean mIsWinBollClientThreadRunning;
    volatile boolean mIsEnableService;
    WinBollClientThread mWinBollClientThread;

    public boolean isWinBollClientThreadRunning() {
        return mIsWinBollClientThreadRunning;
    }

    @Override
    public WinBollClientService getService() {
        return WinBollClientService.this;
    }

    @Override
    public Drawable getCurrentStatusIconDrawable() {
        return mIsWinBollClientThreadRunning ?
            getDrawable(EWUIStatusIconDrawable.getIconDrawableId(EWUIStatusIconDrawable.NORMAL))
            : getDrawable(EWUIStatusIconDrawable.getIconDrawableId(EWUIStatusIconDrawable.NEWS));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        ToastUtils.show("onCreate");
        super.onCreate();
        mWinBollClientThread = null;
        mWinBollClientServiceBean = WinBollClientServiceBean.loadWinBollClientServiceBean(this);
        mIsEnableService = mWinBollClientServiceBean.isEnable();

        if (mMyServiceConnection == null) {
            mMyServiceConnection = new MyServiceConnection();
        }

        // 由系统启动时，应用可以通过下面函数实例化实际服务进程。
        runMainThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ToastUtils.show("onStartCommand");
        // 由应用 Intent 启动时，应用可以通过下面函数实例化实际服务进程。
        runMainThread();

        // 返回运行参数持久化存储后，服务状态控制参数
        // 无论 Intent 传入如何，服务状态一直以持久化存储后的参数控制，
        // PS: 另外当然可以通过 Intent 传入的指标来修改 mWinBollServiceBean，
        //     不过本服务的应用方向会变得繁琐，
        //     现阶段只要满足手机端启动与停止本服务，WinBoll 客户端实例运行在手机端就可以了。
        return mIsEnableService ? Service.START_STICKY: super.onStartCommand(intent, flags, startId);
    }

    void runMainThread() {
        if (mWinBollClientThread == null) {
            ToastUtils.show("runMainThread()");
            mWinBollClientThread = new WinBollClientThread();
            mWinBollClientThread.start();
        }
    }

    void syncWinBollClientThreadStatus() {
        mWinBollClientServiceBean = WinBollClientServiceBean.loadWinBollClientServiceBean(this);
        mIsEnableService = mWinBollClientServiceBean.isEnable();
    }


    // 唤醒和绑定守护进程
    //
    void wakeupAndBindAssistant() {
        if (ServiceUtils.isServiceAlive(getApplicationContext(), AssistantService.class.getName()) == false) {
            startService(new Intent(WinBollClientService.this, AssistantService.class));
            //LogUtils.d(TAG, "call wakeupAndBindAssistant() : Binding... AssistantService");
            bindService(new Intent(WinBollClientService.this, AssistantService.class), mMyServiceConnection, Context.BIND_IMPORTANT);
        }
    }

    // 主进程与守护进程连接时需要用到此类
    //
    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {            
            mWinBollClientServiceBean = WinBollClientServiceBean.loadWinBollClientServiceBean(WinBollClientService.this);
            if (mWinBollClientServiceBean.isEnable()) {
                // 唤醒守护进程
                wakeupAndBindAssistant();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ToastUtils.show("onDestroy");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    void setWinBollServiceEnableStatus(boolean isEnable) {
        WinBollClientServiceBean bean = WinBollClientServiceBean.loadWinBollClientServiceBean(this);
        bean.setIsEnable(isEnable);
        WinBollClientServiceBean.saveWinBollServiceBean(this, bean);
    }

    boolean getWinBollServiceEnableStatus(Context context) {
        mWinBollClientServiceBean = WinBollClientServiceBean.loadWinBollClientServiceBean(context);
        return mWinBollClientServiceBean.isEnable();
    }

    /*public interface OnServiceStatusChangeListener {
     void onServerStatusChange(boolean isServiceAlive);
     }

     public void setOnServerStatusChangeListener(OnServiceStatusChangeListener l) {
     mOnServerStatusChangeListener = l;
     }*/

    class WinBollClientThread extends Thread {
        @Override
        public void run() {
            ToastUtils.show("WinBollClientThread");
            super.run();
            syncWinBollClientThreadStatus();
            if (mIsEnableService) {
                if (mIsWinBollClientThreadRunning == false) {
                    // 设置运行状态
                    mIsWinBollClientThreadRunning = true;

                    ToastUtils.show("run()");

                    // 唤醒守护进程
                    //wakeupAndBindAssistant();

                    while (mIsEnableService) {
                        // 显示运行状态
                        ToastUtils.show(TAG + " is running.");
                        try {
                            Thread.sleep(2 * 1000);
                        } catch (InterruptedException e) {
                            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                        }
                        syncWinBollClientThreadStatus();
                        //ToastUtils.show("syncServiceThreadStatus OK.");
                        //ToastUtils.show("mIsExist : " + Boolean.toString(!mIsEnableService));
                        //break;
                    }

                    // 服务进程退出, 重置进程运行状态
                    mIsWinBollClientThreadRunning = false;
                    mWinBollClientThread = null;
                }
            }
        }
    }
}
