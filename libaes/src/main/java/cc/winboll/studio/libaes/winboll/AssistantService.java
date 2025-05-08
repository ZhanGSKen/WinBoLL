package cc.winboll.studio.libaes.winboll;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/28 19:12:12
 * @Describe 应用主要服务组件类守护进程服务组件类
 */
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import cc.winboll.studio.libaes.beans.WinBoLLClientServiceBean;
import cc.winboll.studio.libaes.winboll.AssistantService;
import cc.winboll.studio.libappbase.utils.ServiceUtils;

public class AssistantService extends Service {

    public final static String TAG = "AssistantService";

    WinBoLLClientServiceBean mWinBoLLServiceBean;
    MyServiceConnection mMyServiceConnection;
    volatile boolean mIsServiceRunning;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWinBoLLServiceBean = WinBoLLClientServiceBean.loadWinBoLLClientServiceBean(this);
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
        mWinBoLLServiceBean = WinBoLLClientServiceBean.loadWinBoLLClientServiceBean(this);
        if (mWinBoLLServiceBean.isEnable()) {
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
        if (ServiceUtils.isServiceRunning(getApplicationContext(), WinBoLLClientService.class.getName()) == false) {
            startForegroundService(new Intent(AssistantService.this, WinBoLLClientService.class));
        }

        bindService(new Intent(AssistantService.this, WinBoLLClientService.class), mMyServiceConnection, Context.BIND_IMPORTANT);
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
            mWinBoLLServiceBean = WinBoLLClientServiceBean.loadWinBoLLClientServiceBean(AssistantService.this);
            if (mWinBoLLServiceBean.isEnable()) {
                wakeupAndBindMain();
            }
        }
    }
}
