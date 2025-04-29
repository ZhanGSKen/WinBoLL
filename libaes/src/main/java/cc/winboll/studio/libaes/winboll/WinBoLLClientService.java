package cc.winboll.studio.libaes.winboll;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/28 19:06:54
 * @Describe WinBoLL 客户端服务
 */
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import cc.winboll.studio.libaes.winboll.AssistantService;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.utils.ServiceUtils;
import cc.winboll.studio.libapputils.utils.PrefUtils;
import com.hjq.toast.ToastUtils;

public class WinBoLLClientService extends Service implements IWinBoLLClientServiceBinder {

    public static final String TAG = "WinBoLLClientService";

    WinBoLLClientServiceBean mWinBoLLClientServiceBean;
    MyServiceConnection mMyServiceConnection;
    volatile boolean mIsWinBoLLClientThreadRunning;
    volatile boolean mIsEnableService;
    volatile WinBoLLClientThread mWinBoLLClientThread;

    public boolean isWinBoLLClientThreadRunning() {
        return mIsWinBoLLClientThreadRunning;
    }

    @Override
    public WinBoLLClientService getService() {
        return WinBoLLClientService.this;
    }

    @Override
    public Drawable getCurrentStatusIconDrawable() {
        return mIsWinBoLLClientThreadRunning ?
            getDrawable(EWUIStatusIconDrawable.getIconDrawableId(EWUIStatusIconDrawable.NORMAL))
            : getDrawable(EWUIStatusIconDrawable.getIconDrawableId(EWUIStatusIconDrawable.NEWS));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //ToastUtils.show("onCreate");
        super.onCreate();
        mWinBoLLClientThread = null;
        mWinBoLLClientServiceBean = WinBoLLClientServiceBean.loadWinBoLLClientServiceBean(this);
        mIsEnableService = mWinBoLLClientServiceBean.isEnable();

        if (mMyServiceConnection == null) {
            mMyServiceConnection = new MyServiceConnection();
        }

        // 由系统启动时，应用可以通过下面函数实例化实际服务进程。
        runMainThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //ToastUtils.show("onStartCommand");
        // 由应用 Intent 启动时，应用可以通过下面函数实例化实际服务进程。
        runMainThread();

        // 返回运行参数持久化存储后，服务状态控制参数
        // 无论 Intent 传入如何，服务状态一直以持久化存储后的参数控制，
        // PS: 另外当然可以通过 Intent 传入的指标来修改 mWinBoLLServiceBean，
        //     不过本服务的应用方向会变得繁琐，
        //     现阶段只要满足手机端启动与停止本服务，WinBoLL 客户端实例运行在手机端就可以了。
        return mIsEnableService ? Service.START_STICKY: super.onStartCommand(intent, flags, startId);
    }

    synchronized void runMainThread() {
        if (mWinBoLLClientThread == null) {
            //ToastUtils.show("runMainThread()");
            mWinBoLLClientThread = new WinBoLLClientThread();
            mWinBoLLClientThread.start();
        }
    }

    void syncWinBoLLClientThreadStatus() {
        mWinBoLLClientServiceBean = WinBoLLClientServiceBean.loadWinBoLLClientServiceBean(this);
        mIsEnableService = mWinBoLLClientServiceBean.isEnable();
        LogUtils.d(TAG, String.format("mIsEnableService %s", mIsEnableService));
    }


    // 唤醒和绑定守护进程
    //
    void wakeupAndBindAssistant() {
        if (ServiceUtils.isServiceRunning(getApplicationContext(), AssistantService.class.getName()) == false) {
            startService(new Intent(WinBoLLClientService.this, AssistantService.class));
            //LogUtils.d(TAG, "call wakeupAndBindAssistant() : Binding... AssistantService");
            bindService(new Intent(WinBoLLClientService.this, AssistantService.class), mMyServiceConnection, Context.BIND_IMPORTANT);
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
            mWinBoLLClientServiceBean = WinBoLLClientServiceBean.loadWinBoLLClientServiceBean(WinBoLLClientService.this);
            if (mWinBoLLClientServiceBean.isEnable()) {
                // 唤醒守护进程
                wakeupAndBindAssistant();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //ToastUtils.show("onDestroy");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    void setWinBoLLServiceEnableStatus(boolean isEnable) {
        WinBoLLClientServiceBean bean = WinBoLLClientServiceBean.loadWinBoLLClientServiceBean(this);
        bean.setIsEnable(isEnable);
        WinBoLLClientServiceBean.saveWinBoLLServiceBean(this, bean);
    }

    boolean getWinBoLLServiceEnableStatus(Context context) {
        mWinBoLLClientServiceBean = WinBoLLClientServiceBean.loadWinBoLLClientServiceBean(context);
        return mWinBoLLClientServiceBean.isEnable();
    }

    /*public interface OnServiceStatusChangeListener {
     void onServerStatusChange(boolean isServiceAlive);
     }

     public void setOnServerStatusChangeListener(OnServiceStatusChangeListener l) {
     mOnServerStatusChangeListener = l;
     }*/

    class WinBoLLClientThread extends Thread {
        @Override
        public void run() {
            super.run();
            LogUtils.d(TAG, "run syncWinBoLLClientThreadStatus");
            syncWinBoLLClientThreadStatus();
            if (mIsEnableService) {
                if (mIsWinBoLLClientThreadRunning == false) {
                    // 设置运行状态
                    mIsWinBoLLClientThreadRunning = true;
                    LogUtils.d(TAG, "WinBoLLClientThread run()");

                    // 唤醒守护进程
                    //wakeupAndBindAssistant();
                    
                    while (mIsEnableService) {
                        // 显示运行状态
                        WinBoLLServiceStatusView.startConnection();
                        LogUtils.d(TAG, String.format("while mIsEnableService is %s", mIsEnableService));
                        
                        try {
                            Thread.sleep(10 * 1000);
                        } catch (InterruptedException e) {
                            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                        }
                        syncWinBoLLClientThreadStatus();
                    }

                    // 服务进程退出, 重置进程运行状态
                    WinBoLLServiceStatusView.stopConnection();
                    mIsWinBoLLClientThreadRunning = false;
                    mWinBoLLClientThread = null;
                }
            }
        }
    }
}
