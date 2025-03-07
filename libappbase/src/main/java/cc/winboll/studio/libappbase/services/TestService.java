package cc.winboll.studio.libappbase.services;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/15 20:48:36
 * @Describe TestService
 */
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.bean.TestServiceBean;
import cc.winboll.studio.libappbase.sos.WinBoll;

public class TestService extends Service {

    public static final String TAG = "TestService";

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
        public TestService getService() {
            return TestService.this;
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
        TestServiceBean bean = TestServiceBean.loadBean(this, TestServiceBean.class);
        if (bean == null) {
            bean = new TestServiceBean();
        }
//        if (intent.getAction() != null && intent.getAction().equals(WinBoll.ACTION_SERVICE_ENABLE)) {
//            bean.setIsEnable(true);
//            TestServiceBean.saveBean(this, bean);
//            run();
//        } else if (intent.getAction() != null && intent.getAction().equals(WinBoll.ACTION_SERVICE_DISABLE)) {
//            bean.setIsEnable(false);
//            TestServiceBean.saveBean(this, bean);
//        }
        LogUtils.d(TAG, String.format("TestServiceBean.saveBean setIsEnable %s", bean.isEnable()));
        return (bean.isEnable()) ? START_STICKY : super.onStartCommand(intent, flags, startId);
        //return super.onStartCommand(intent, flags, startId);
    }

    void run() {
        LogUtils.d(TAG, "run()");
        TestServiceBean bean = TestServiceBean.loadBean(this, TestServiceBean.class);
        if (bean == null) {
            bean = new TestServiceBean();
            TestServiceBean.saveBean(this, bean);
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
                WinBoll.bindToAPPBase(mContext, new APPNewsBean(mContext.getPackageName(), TestService.class.getName()));

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
