package cc.winboll.studio.shared.service;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/12/08 20:15:42
 * @Describe 应用主要服务组件类守护进程服务组件类
 */
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import cc.winboll.studio.shared.util.ServiceUtils;

public class AssistantService extends Service {

    public final static String TAG = "AssistantService";

    WinBollClientServiceBean mWinBollServiceBean;
    MyServiceConnection mMyServiceConnection;
    volatile boolean mIsServiceRunning;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWinBollServiceBean = WinBollClientServiceBean.loadWinBollClientServiceBean(this);
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
        mWinBollServiceBean = WinBollClientServiceBean.loadWinBollClientServiceBean(this);
        if (mWinBollServiceBean.isEnable()) {
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
        if (ServiceUtils.isServiceAlive(getApplicationContext(), WinBollClientService.class.getName()) == false) {
            startForegroundService(new Intent(AssistantService.this, WinBollClientService.class));
        }

        bindService(new Intent(AssistantService.this, WinBollClientService.class), mMyServiceConnection, Context.BIND_IMPORTANT);
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
            mWinBollServiceBean = WinBollClientServiceBean.loadWinBollClientServiceBean(AssistantService.this);
            if (mWinBollServiceBean.isEnable()) {
                wakeupAndBindMain();
            }
        }
    }
}
