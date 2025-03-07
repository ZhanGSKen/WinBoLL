package cc.winboll.studio.appbase.services;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/07 12:45:49
 * @Describe 启动时申请绑定到APPBase主服务的服务示例
 */
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import cc.winboll.studio.appbase.beans.TestDemoBindServiceBean;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.sos.WinBoll;
import cc.winboll.studio.appbase.App;
import cc.winboll.studio.libappbase.sos.SOS;

public class TestDemoBindService extends Service {

    public static final String TAG = "TestDemoBindService";

    public static final String ACTION_ENABLE = TestDemoBindService.class.getName() + ".ACTION_ENABLE";
    public static final String ACTION_DISABLE = TestDemoBindService.class.getName() + ".ACTION_DISABLE";

    volatile static TestThread _TestThread;

    volatile static boolean _IsRunning;

    public synchronized static void setIsRunning(boolean isRunning) {
        _IsRunning = isRunning;
    }

    public static boolean isRunning() {
        return _IsRunning;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        public TestDemoBindService getService() {
            return TestDemoBindService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "onCreate()");

        run();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "onStartCommand(...)");
        TestDemoBindServiceBean bean = TestDemoBindServiceBean.loadBean(this, TestDemoBindServiceBean.class);
        if (bean == null) {
            bean = new TestDemoBindServiceBean();
        }

        if (intent.getAction() != null) {
            if (intent.getAction().equals(ACTION_ENABLE)) {
                bean.setIsEnable(true);
                LogUtils.d(TAG, "setIsEnable(true);");
                TestDemoBindServiceBean.saveBean(this, bean);
            } else if (intent.getAction().equals(ACTION_DISABLE)) {
                bean.setIsEnable(false);
                LogUtils.d(TAG, "setIsEnable(false);");
                TestDemoBindServiceBean.saveBean(this, bean);
            }
        }

        run();

        return (bean.isEnable()) ? START_STICKY : super.onStartCommand(intent, flags, startId);
        //return super.onStartCommand(intent, flags, startId);
    }

    void run() {
        LogUtils.d(TAG, "run()");
        TestDemoBindServiceBean bean = TestDemoBindServiceBean.loadBean(this, TestDemoBindServiceBean.class);
        if (bean == null) {
            bean = new TestDemoBindServiceBean();
            TestDemoBindServiceBean.saveBean(this, bean);
        }
        if (bean.isEnable()) {
            LogUtils.d(TAG, "run() bean.isEnable()");
            TestThread.getInstance(this).start();
            LogUtils.d(TAG, "_TestThread.start()");
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy()");
        TestDemoBindServiceBean bean = TestDemoBindServiceBean.loadBean(this, TestDemoBindServiceBean.class);
        if (bean == null) {
            bean = new TestDemoBindServiceBean();
        }

        TestThread.getInstance(this).setIsExit(true);
        
        // 预防 APPBase 应用重启绑定失效。
        // 所以退出时检查本服务是否配置启用，如果启用就发送一个 SOS 信号。
        // 这样 APPBase 就会用组件方式启动本服务。
        if (bean.isEnable()) {
            if (App.isDebuging()) {
                SOS.sosToAppBaseBeta(this, TestDemoBindService.class.getName());
            } else {
                SOS.sosToAppBase(this, TestDemoBindService.class.getName());
            }
        }

        _IsRunning = false;
    }

    static class TestThread extends Thread {

        volatile static TestThread _TestThread;
        Context mContext;
        volatile boolean isStarted = false;
        volatile boolean isExit = false;

        TestThread(Context context) {
            super();
            mContext = context;
        }

        public static synchronized TestThread getInstance(Context context) {
            if (_TestThread != null) {
                _TestThread.setIsExit(true);
            }
            _TestThread = new TestThread(context);

            return _TestThread;
        }

        public synchronized void setIsExit(boolean isExit) {
            this.isExit = isExit;
        }

        public boolean isExit() {
            return isExit;
        }

        @Override
        public void run() {
            if (isStarted == false) {
                isStarted = true;
                super.run();
                LogUtils.d(TAG, "run() start");
                if (App.isDebuging()) {
                    WinBoll.bindToAPPBaseBeta(mContext, TestDemoBindService.class.getName());
                } else {
                    WinBoll.bindToAPPBase(mContext, TestDemoBindService.class.getName());
                }

                while (!isExit()) {
                    LogUtils.d(TAG, "run()");

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                    }
                }

                LogUtils.d(TAG, "run() exit");
            }
        }
    }
}
