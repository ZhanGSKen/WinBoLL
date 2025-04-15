package cc.winboll.studio.autoinstaller.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import cc.winboll.studio.autoinstaller.FileListener;
import cc.winboll.studio.autoinstaller.MainActivity;
import cc.winboll.studio.autoinstaller.models.APKModel;
import cc.winboll.studio.autoinstaller.models.AppConfigs;
import cc.winboll.studio.autoinstaller.services.AssistantService;
import cc.winboll.studio.autoinstaller.services.MainService;
import cc.winboll.studio.autoinstaller.utils.NotificationUtil;
import cc.winboll.studio.autoinstaller.utils.PackageUtil;
import cc.winboll.studio.autoinstaller.utils.ServiceUtil;
import cc.winboll.studio.libappbase.LogUtils;
import com.hjq.toast.ToastUtils;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import cc.winboll.studio.autoinstaller.models.MainServiceBean;

public class MainService extends Service {

    public static String TAG = "MainService";

    ArrayList<APKModel> _APKModelList = new ArrayList<APKModel>();
    private static boolean _mIsServiceAlive;
    //String mszAPKFilePath;
    //String mszAPKFileName;
    FileListener mFileListener;
    public static final String EXTRA_APKFILEPATH = "EXTRA_APKFILEPATH";
	final static int MSG_INSTALL_APK = 0;
    Handler mHandler;
    MyServiceConnection mMyServiceConnection;
    MainActivity mInstallCompletedFollowUpActivity;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "onCreate()");
        _mIsServiceAlive = false;
        mHandler = new MyHandler(MainService.this);
        if (mMyServiceConnection == null) {
            mMyServiceConnection = new MyServiceConnection();
        }

        run();
    }

    private void run() {

        AppConfigs appConfigs = AppConfigs.getInstance(MainService.this).loadAppConfigs(MainService.this);
        if (appConfigs.isEnableService()) {
            if (_mIsServiceAlive == false) {
                // 设置运行状态
                _mIsServiceAlive = true;

                // 显示前台通知栏
                NotificationUtil nu = new NotificationUtil();
                nu.sendForegroundNotification(MainService.this);

                // 唤醒守护进程
                wakeupAndBindAssistant();

                startWatchingFile(appConfigs.getWatchingFilePath());

                //LogUtils.d(TAG, "running...");
                //ToastUtils.show("running...");

            } else {
                LogUtils.d(TAG, "_mIsServiceAlive is " + Boolean.toString(_mIsServiceAlive));

            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFileListener != null) {
            mFileListener.stopWatching();
            mFileListener = null;
            LogUtils.d(TAG, "stopWatching");

        }
        _mIsServiceAlive = false;
        LogUtils.d(TAG, "onDestroy()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "onStartCommand");

        run();
        AppConfigs appConfigs = AppConfigs.getInstance(MainService.this).loadAppConfigs(MainService.this);

        return appConfigs.isEnableService() ? Service.START_STICKY: super.onStartCommand(intent, flags, startId);
    }


    // 主进程与守护进程连接时需要用到此类
    //
    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //LogUtils.d(TAG, "call onServiceConnected(...)");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //LogUtils.d(TAG, "call onServiceConnected(...)");
            AppConfigs appConfigs = AppConfigs.getInstance(MainService.this).loadAppConfigs(MainService.this);
            if (appConfigs.isEnableService()) {
                // 唤醒守护进程
                wakeupAndBindAssistant();
            }
        }
    }

    // 唤醒和绑定守护进程
    //
    void wakeupAndBindAssistant() {
        if (ServiceUtil.isServiceAlive(getApplicationContext(), AssistantService.class.getName()) == false) {
            startService(new Intent(MainService.this, AssistantService.class));
            //LogUtils.d(TAG, "call wakeupAndBindAssistant() : Binding... AssistantService");
            bindService(new Intent(MainService.this, AssistantService.class), mMyServiceConnection, Context.BIND_IMPORTANT);
        }
    }

    void startWatchingFile(String szAPKFilePath) {
        try {
            File fTemp = new File(szAPKFilePath);
            final File fParentDir = fTemp.getParentFile();
            final String szAPKFileName = fTemp.getName();
            if (fParentDir.exists()) {
                mFileListener = new FileListener(fParentDir.toString());

                mFileListener.setEventCallback(new FileListener.EventCallback() {

                        @Override
                        public void onEvent(String path) {
                            //LogUtils.d(TAG, "path : " + path);
                            File fTemo = new File(fParentDir, path);
                            if (fTemo.getName().equals(szAPKFileName)) {
                                Message message = mHandler.obtainMessage(MSG_INSTALL_APK);
                                message.obj = fTemo.toString();
                                mHandler.sendMessage(message);
                            }
                        }


                    });
                mFileListener.startWatching();
                //ToastUtils.show("Start watching.");
            } else {
                // 父级文件夹不存在，就提示用户
                Toast.makeText(getApplication(), fParentDir.toString() + " no exist.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(getApplication(), szAPKFilePath.toString() + " watching failed.", Toast.LENGTH_SHORT).show();
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
    }

    //
    // 调用[应用信息查看器]打开应用包
    //
    private void installAPK(String szAPKFilePath) {
        String szAPKPackageName = PackageUtil.getPackageNameFromApk(this, szAPKFilePath);
        saveAPKInfo(szAPKPackageName);
        
        long nTimeNow = System.currentTimeMillis();
        /*SimpleDateFormat dateFormat = new SimpleDateFormat(
         "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
         Date d = new Date(nTimeNow);
         Map<String,Object> map =new HashMap<>();
         map.put(MAP_NAME, dateFormat.format(d));
         mAdapterData.add(0, map);
         mSimpleAdapter.notifyDataSetChanged();
         LogUtils.d(TAG, "Start install APK");*/

        File file = new File(szAPKFilePath);
        LogUtils.i(TAG, "New APK File is : " + szAPKFilePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName("com.kk.xx.newapkinfo", "sk.styk.martin.apkanalyzer.ui.activity.appdetail.oninstall.OnInstallAppDetailActivity");

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // 7.0+以上版本
            Uri apkUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            LogUtils.d(TAG, "Uri is : " + apkUri.toString());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        this.startActivity(intent);
        LogUtils.i(TAG, "Start Install Activity.");
    }

    //
    // 直接调用系统安装工具进行安装
    //
    void installAPK2(String szAPKFilePath) {
        LogUtils.d(TAG, "installAPK2()");
        String szAPKPackageName = PackageUtil.getPackageNameFromApk(this, szAPKFilePath);
        saveAPKInfo(szAPKPackageName);
        
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.ACTION_NEW_INSTALLTASK);
        intent.putExtra(MainActivity.EXTRA_INSTALLED_PACKAGENAME, szAPKPackageName);
        intent.putExtra(MainActivity.EXTRA_INSTALLED_APKFILEPATH, szAPKFilePath);
        // Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    void saveAPKInfo(String szApkPackageName) {
        APKModel.loadBeanList(this, _APKModelList, APKModel.class);
        _APKModelList.add(new APKModel(szApkPackageName));
        APKModel.saveBeanList(this, _APKModelList, APKModel.class);
    }

    // 
    //
    static class MyHandler extends Handler {
        WeakReference<MainService> weakReference;  
        MyHandler(MainService service) {  
            weakReference = new WeakReference<MainService>(service);  
        }
        public void handleMessage(Message message) {
            MainService theActivity = weakReference.get();
            switch (message.what) {
                case MSG_INSTALL_APK:
                    {
                        AppConfigs appConfigs = AppConfigs.getInstance(theActivity).loadAppConfigs(theActivity);
                        if (appConfigs.getSetupMode() == AppConfigs.SetupMode.WATCHOUTPUTINSTALLER) {
                            theActivity.installAPK2((String)message.obj);
                        } else if (appConfigs.getSetupMode() == AppConfigs.SetupMode.NEWAPKINFONEWAPKINFO) {
                            theActivity.installAPK((String)message.obj);
                        }
                        break;
                    }
                default:
                    break;
            }
            super.handleMessage(message);
        }
	}
}
