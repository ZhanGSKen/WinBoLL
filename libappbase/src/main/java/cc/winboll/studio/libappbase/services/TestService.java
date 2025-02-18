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
import cc.winboll.studio.libappbase.widgets.StatusWidget;

public class TestService extends Service {

    public static final String TAG = "TestService";

    TestThread mTestThread;
    
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
        mTestThread = new TestThread();
        mTestThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "onStartCommand(...)");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy()");
        mTestThread.setIsExit(true);
    }

    class TestThread extends Thread {
        volatile boolean isExit = false;

        public void setIsExit(boolean isExit) {
            this.isExit = isExit;
        }

        public boolean isExit() {
            return isExit;
        }

        @Override
        public void run() {
            super.run();

            LogUtils.d(TAG, "run() start");
            Intent intentStart = new Intent(TestService.this, StatusWidget.class);
            intentStart.setAction(StatusWidget.ACTION_STATUS_UPDATE);
            sendBroadcast(intentStart);

            while (!isExit) {
                //LogUtils.d(TAG, "run()");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                }
            }

            Intent intentStop = new Intent(TestService.this, StatusWidget.class);
            intentStop.setAction(StatusWidget.ACTION_STATUS_UPDATE);
            sendBroadcast(intentStop);

            LogUtils.d(TAG, "run() exit");
        }

    }

}
