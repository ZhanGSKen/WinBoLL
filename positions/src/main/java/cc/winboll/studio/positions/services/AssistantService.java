package cc.winboll.studio.positions.services;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2024/07/19 14:30:57
 * @Describe 应用主要服务组件类守护进程服务组件类
 */
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import cc.winboll.studio.positions.services.MainService;
import cc.winboll.studio.positions.utils.AppConfigsUtil;
import cc.winboll.studio.positions.utils.ServiceUtil;

public class AssistantService extends Service {

    public final static String TAG = "AssistantService";
	public static final String EXTRA_IS_SETTING_TO_ENABLE = "EXTRA_IS_SETTING_TO_ENABLE";
	
    MyServiceConnection mMyServiceConnection;
    volatile boolean mIsServiceRunning;
    AppConfigsUtil mAppConfigsUtil;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAppConfigsUtil = AppConfigsUtil.getInstance(this);
        if (mMyServiceConnection == null) {
            mMyServiceConnection = new MyServiceConnection();
        }
        // 设置运行参数
        mIsServiceRunning = false;
        if (mAppConfigsUtil.isEnableMainService(true)) {
			run();
		}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		if (mAppConfigsUtil.isEnableMainService(true)) {
			run();
		}

        return  mAppConfigsUtil.isEnableMainService(true) ? Service.START_STICKY : super.onStartCommand(intent, flags, startId);
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
        if (mAppConfigsUtil.isEnableMainService(true)) {
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
            if (mAppConfigsUtil.isEnableMainService(true)) {
                wakeupAndBindMain();
            }
        }
    }
}
