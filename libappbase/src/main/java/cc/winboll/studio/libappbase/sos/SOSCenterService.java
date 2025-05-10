package cc.winboll.studio.libappbase.sos;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/02/27 14:00:21
 * @Describe Simple Operate Signal Service Center.
 *           简单操作信号服务中心
 */
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.io.FileDescriptor;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import cc.winboll.studio.libappbase.LogUtils;

public class SOSCenterService extends Service {

    public static final String TAG = "SOSCenterService";
    
    private final IBinder binder =(IBinder)new SOSBinder();

    SOSCenterServiceModel mSOSCenterServiceModel;
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
        SOSCenterService getService() {
            return SOSCenterService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "onCreate");
        mSOSCenterServiceModel = SOSCenterServiceModel.loadBean(this, SOSCenterServiceModel.class);
        if(mSOSCenterServiceModel == null) {
            mSOSCenterServiceModel = new SOSCenterServiceModel();
            SOSCenterServiceModel.saveBean(this, mSOSCenterServiceModel);
        }
        runMainThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "onStartCommand");

        runMainThread();

        return mSOSCenterServiceModel.isEnable() ? Service.START_STICKY: super.onStartCommand(intent, flags, startId);
    }

    void runMainThread() {
        mSOSCenterServiceModel = mSOSCenterServiceModel.loadBean(this, SOSCenterServiceModel.class);
        if (mSOSCenterServiceModel.isEnable()
            && _MainThread == null) {
            getMainThreadInstance().start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy");
        mSOSCenterServiceModel = SOSCenterServiceModel.loadBean(this, SOSCenterServiceModel.class);
        if (mSOSCenterServiceModel.isEnable()) {
            LogUtils.d(TAG, "mSOSCenterServiceModel.isEnable()");
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
        SOSCenterServiceModel bean = new SOSCenterServiceModel();
        bean.setIsEnable(false);
        SOSCenterServiceModel.saveBean(context, bean);
        context.stopService(new Intent(context, SOSCenterServiceModel.class));
    }

    public static void startISOSService(Context context) {
        LogUtils.d(TAG, "startISOSService");
        SOSCenterServiceModel bean = new SOSCenterServiceModel();
        bean.setIsEnable(true);
        SOSCenterServiceModel.saveBean(context, bean);
        context.startService(new Intent(context, SOSCenterServiceModel.class));
    }

    public String getMessage() {
        return "Hello from SOSCenterServiceModel";
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
