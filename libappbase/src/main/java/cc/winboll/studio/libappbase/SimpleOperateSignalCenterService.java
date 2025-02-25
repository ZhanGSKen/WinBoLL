package cc.winboll.studio.libappbase;


/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/12 11:12:25
 * @Describe 简单信号服务中心
 */
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import cc.winboll.studio.libappbase.bean.SimpleOperateSignalCenterServiceBean;
import java.io.FileDescriptor;

public class SimpleOperateSignalCenterService extends Service {

    public static final String TAG = "SimpleOperateSignalCenterService";
    public static final String ACTION_ENABLE = SimpleOperateSignalCenterService.class.getName() + ".ACTION_ENABLE";
    public static final String ACTION_DISABLE = SimpleOperateSignalCenterService.class.getName() + ".ACTION_DISABLE";
    
    private final IBinder binder =(IBinder)new SOSBinder();
    
    SimpleOperateSignalCenterServiceBean mSimpleOperateSignalCenterServiceBean;
    static MainThread _MainThread;
    public static synchronized MainThread getMainThreadInstance() {
        if (_MainThread == null) {
            _MainThread = new MainThread();
        }
        return _MainThread;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    public class SOSBinder implements IBinder {

        @Override
        public void dump(FileDescriptor fileDescriptor, String[] string) throws RemoteException {
        }

        @Override
        public void dumpAsync(FileDescriptor fileDescriptor, String[] string) throws RemoteException {
        }

        @Override
        public String getInterfaceDescriptor() throws RemoteException {
            return null;
        }

        @Override
        public boolean isBinderAlive() {
            return false;
        }

        @Override
        public void linkToDeath(IBinder.DeathRecipient deathRecipient, int p) throws RemoteException {
        }

        @Override
        public boolean pingBinder() {
            return false;
        }

        @Override
        public IInterface queryLocalInterface(String string) {
            return null;
        }

        @Override
        public boolean transact(int p, Parcel parcel, Parcel parcel1, int p1) throws RemoteException {
            return false;
        }

        @Override
        public boolean unlinkToDeath(IBinder.DeathRecipient deathRecipient, int p) {
            return false;
        }
        
        public static final String TAG = "SOSBinder";
        SimpleOperateSignalCenterService getService() {
            return SimpleOperateSignalCenterService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "onCreate");
        mSimpleOperateSignalCenterServiceBean = SimpleOperateSignalCenterServiceBean.loadBean(this, SimpleOperateSignalCenterServiceBean.class);
        if(mSimpleOperateSignalCenterServiceBean == null) {
            mSimpleOperateSignalCenterServiceBean = new SimpleOperateSignalCenterServiceBean();
            SimpleOperateSignalCenterServiceBean.saveBean(this, mSimpleOperateSignalCenterServiceBean);
        }
        runMainThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "onStartCommand");
//        if (intent.getBooleanExtra(ISOSService.EXTRA_ENABLE, false)) {
//            LogUtils.d(TAG, "onStartCommand enable service");
//            mSimpleOperateSignalCenterServiceBean.setIsEnable(true);
//            SimpleOperateSignalCenterServiceBean.saveBean(this, mSimpleOperateSignalCenterServiceBean);
//        }

        runMainThread();

        //return super.onStartCommand(intent, flags, startId);
        return mSimpleOperateSignalCenterServiceBean.isEnable() ? Service.START_STICKY: super.onStartCommand(intent, flags, startId);
    }

    void runMainThread() {
        mSimpleOperateSignalCenterServiceBean = SimpleOperateSignalCenterServiceBean.loadBean(this, SimpleOperateSignalCenterServiceBean.class);
        if (mSimpleOperateSignalCenterServiceBean.isEnable()
            && _MainThread == null) {
            getMainThreadInstance().start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy");
        mSimpleOperateSignalCenterServiceBean = SimpleOperateSignalCenterServiceBean.loadBean(this, SimpleOperateSignalCenterServiceBean.class);
        if (mSimpleOperateSignalCenterServiceBean.isEnable()) {
            LogUtils.d(TAG, "mSimpleOperateSignalCenterServiceBean.isEnable()");
//            ISOSAPP iSOSAPP = (ISOSAPP)getApplication();
//            iSOSAPP.helpISOSService(getISOSServiceIntentWhichAskForHelp());
        } 
        if (_MainThread != null) {
            _MainThread.isExist = true;
            _MainThread = null;
        }
    }

    public static void stopISOSService(Context context) {
        LogUtils.d(TAG, "stopISOSService");
        SimpleOperateSignalCenterServiceBean bean = new SimpleOperateSignalCenterServiceBean();
        bean.setIsEnable(false);
        SimpleOperateSignalCenterServiceBean.saveBean(context, bean);
        context.stopService(new Intent(context, SimpleOperateSignalCenterService.class));
    }

    public static void startISOSService(Context context) {
        LogUtils.d(TAG, "startISOSService");
        SimpleOperateSignalCenterServiceBean bean = new SimpleOperateSignalCenterServiceBean();
        bean.setIsEnable(true);
        SimpleOperateSignalCenterServiceBean.saveBean(context, bean);
        context.startService(new Intent(context, SimpleOperateSignalCenterService.class));
    }
    
    public String getMessage() {
        return "Hello from SimpleOperateSignalCenterService";
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
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                }
            }
        }

    }
}
