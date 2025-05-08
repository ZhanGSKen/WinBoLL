package cc.winboll.studio.appbase.services;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/07 12:39:24
 * @Describe 普通服务示例
 */
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import cc.winboll.studio.appbase.models.TestDemoServiceBean;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.sos.WinBoLL;

public class TestDemoService extends Service {

    public static final String TAG = "TestDemoService";

    public static final String ACTION_ENABLE = TestDemoService.class.getName() + ".ACTION_ENABLE";
    public static final String ACTION_DISABLE = TestDemoService.class.getName() + ".ACTION_DISABLE";

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
        public TestDemoService getService() {
            return TestDemoService.this;
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
        TestDemoServiceBean bean = TestDemoServiceBean.loadBean(this, TestDemoServiceBean.class);
        if (bean == null) {
            bean = new TestDemoServiceBean();
        }

        if (intent.getAction() != null) {
            if (intent.getAction().equals(ACTION_ENABLE)) {
                bean.setIsEnable(true);
                LogUtils.d(TAG, "setIsEnable(true);");
                TestDemoServiceBean.saveBean(this, bean);
            } else if (intent.getAction().equals(ACTION_DISABLE)) {
                bean.setIsEnable(false);
                LogUtils.d(TAG, "setIsEnable(false);");
                TestDemoServiceBean.saveBean(this, bean);
            }
        }

        run();

        return (bean.isEnable()) ? START_STICKY : super.onStartCommand(intent, flags, startId);
        //return super.onStartCommand(intent, flags, startId);
    }

    void run() {
        LogUtils.d(TAG, "run()");
        TestDemoServiceBean bean = TestDemoServiceBean.loadBean(this, TestDemoServiceBean.class);
        if (bean == null) {
            bean = new TestDemoServiceBean();
            TestDemoServiceBean.saveBean(this, bean);
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
        TestThread.getInstance(this).setIsExit(true);

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
