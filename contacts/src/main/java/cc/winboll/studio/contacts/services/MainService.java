package cc.winboll.studio.contacts.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import cc.winboll.studio.contacts.beans.MainServiceBean;
import cc.winboll.studio.libappbase.ISOSAPP;
import cc.winboll.studio.libappbase.ISOSService;
import cc.winboll.studio.libappbase.LogUtils;
import com.hjq.toast.ToastUtils;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/13 06:56:41
 * @Describe 拨号主服务
 */
public class MainService extends Service implements ISOSService {
    
    public static final String TAG = "MainService";
    
    public static final String ACTION_ENABLE = MainService.class.getName() + ".ACTION_ENABLE";
    public static final String ACTION_DISABLE = MainService.class.getName() + ".ACTION_DISABLE";

    MainServiceBean mMainServiceBean;
    static MainThread _MainThread;
    public static synchronized MainThread getMainThreadInstance() {
        if (_MainThread == null) {
            _MainThread = new MainThread();
        }
        return _MainThread;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "onCreate");
        mMainServiceBean = MainServiceBean.loadBean(this, MainServiceBean.class);
        runMainThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "onStartCommand");
        if (intent.getBooleanExtra(ISOSService.EXTRA_ENABLE, false)) {
            LogUtils.d(TAG, "onStartCommand enable service");
            mMainServiceBean.setIsEnable(true);
            MainServiceBean.saveBean(this, mMainServiceBean);
        }

        runMainThread();

        //return super.onStartCommand(intent, flags, startId);
        return mMainServiceBean.isEnable() ? Service.START_STICKY: super.onStartCommand(intent, flags, startId);
    }

    void runMainThread() {
        mMainServiceBean = MainServiceBean.loadBean(this, MainServiceBean.class);
        if (mMainServiceBean.isEnable()
            && _MainThread == null) {
            getMainThreadInstance().start();
        }
    }

    @Override
    public Intent getISOSServiceIntentWhichAskForHelp() {
        Intent intentService = new Intent();
        intentService.putExtra(ISOSAPP.EXTRA_PACKAGE, this.getPackageName());
        intentService.putExtra(ISOSAPP.EXTRA_SERVICE, this.getClass().getName());
        return intentService;
    }

    @Override
    public boolean isEnable() {
        mMainServiceBean = MainServiceBean.loadBean(this, MainServiceBean.class);
        return mMainServiceBean.isEnable();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy");
        mMainServiceBean = MainServiceBean.loadBean(this, MainServiceBean.class);
        if (mMainServiceBean.isEnable()) {
            LogUtils.d(TAG, "mSimpleOperateSignalCenterServiceBean.isEnable()");
            ISOSAPP iSOSAPP = (ISOSAPP)getApplication();
            iSOSAPP.helpISOSService(getISOSServiceIntentWhichAskForHelp());
        } 
        if (_MainThread != null) {
            _MainThread.isExist = true;
            _MainThread = null;
        }
    }

    public static void stopISOSService(Context context) {
        LogUtils.d(TAG, "stopISOSService");
        MainServiceBean bean = new MainServiceBean();
        bean.setIsEnable(false);
        MainServiceBean.saveBean(context, bean);
        context.stopService(new Intent(context, MainService.class));
    }

    public static void startISOSService(Context context) {
        LogUtils.d(TAG, "startISOSService");
        MainServiceBean bean = new MainServiceBean();
        bean.setIsEnable(true);
        MainServiceBean.saveBean(context, bean);
        context.startService(new Intent(context, MainService.class));
    }

    static class MainThread extends Thread {
        volatile boolean isExist = false;

        public void setIsExist(boolean isExist) {
            this.isExist = isExist;
        }

        public boolean isExist() {
            return isExist;
        }

        @Override
        public void run() {
            super.run();
            while (!isExist) {
                LogUtils.d(TAG, "run");
                ToastUtils.show("run");
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                }
            }
        }

    }
}
