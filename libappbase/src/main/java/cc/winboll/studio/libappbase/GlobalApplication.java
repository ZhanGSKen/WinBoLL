package cc.winboll.studio.libappbase;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/05 10:10:23
 * @Describe 全局应用类
 */
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import cc.winboll.studio.libappbase.utils.ToastUtils;
import cc.winboll.studio.libappbase.winboll.WinBoLLActivityManager;
import cc.winboll.studio.libappbase.winboll.MyActivityLifecycleCallbacks;

public class GlobalApplication extends Application {

    public static final String TAG = "GlobalApplication";

    volatile static GlobalApplication _GlobalApplication;
    // 是否处于调试状态
    volatile static boolean isDebuging = false;
    MyActivityLifecycleCallbacks mMyActivityLifecycleCallbacks;

    public static void setIsDebuging(boolean isDebuging) {
        GlobalApplication.isDebuging = isDebuging;
    }
    
    public static void saveDebugStatus() {
        if (_GlobalApplication != null) {
            APPBaseModel.saveBeanToFile(getAPPBaseModelFilePath(), new APPBaseModel(GlobalApplication.isDebuging));
        }
    }

    public static GlobalApplication getInstance() {
        return _GlobalApplication;
    }

    static String getAPPBaseModelFilePath() {
        return _GlobalApplication.getDataDir().getPath() + "/APPBaseModel.json";
    }

    public static boolean isDebuging() {
        return isDebuging;
    }

    public static WinBoLLActivityManager getWinBoLLActivityManager() {
        return WinBoLLActivityManager.getInstance(_GlobalApplication);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 保存初始实例
        _GlobalApplication = this;
        
        setIsDebuging(true);
        // 添加日志模块
        LogUtils.init(this);
        //LogUtils.setLogLevel(LogUtils.LOG_LEVEL.Debug);
        //LogUtils.setTAGListEnable(GlobalApplication.TAG, true);
        //LogUtils.setALlTAGListEnable(true);
        //LogUtils.d(TAG, "LogUtils init");
        // 设置应用异常处理窗口
        CrashHandler.init(this);
        // 初始化 Toast 框架
        ToastUtils.init(this);

        // 应用保存的调试标志
        APPBaseModel appBaseModel = APPBaseModel.loadBeanFromFile(getAPPBaseModelFilePath(), APPBaseModel.class);
        if (appBaseModel == null) {
            setIsDebuging(false);
            saveDebugStatus();
        } else {
            setIsDebuging(appBaseModel.isDebuging());
        }
        
        getWinBoLLActivityManager().setWinBoLLUI_TYPE(WinBoLLActivityManager.WinBoLLUI_TYPE.Service);
        // 注册窗口回调监听
        mMyActivityLifecycleCallbacks = new MyActivityLifecycleCallbacks();
        registerActivityLifecycleCallbacks(mMyActivityLifecycleCallbacks);
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        // 注销回调（非必须，但建议释放资源）
        unregisterActivityLifecycleCallbacks(mMyActivityLifecycleCallbacks);
    }

    public static String getAppName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(
                context.getPackageName(), 0);
            return (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
//
//    @Override
//    public void helpISOSService(Intent intent) {
//        String szServiceName = intent.getStringExtra(EXTRA_SERVICE);
//        String szPackageName = intent.getStringExtra(EXTRA_PACKAGE);
//        if (szServiceName != null && !szServiceName.equals("")
//            && szPackageName != null && !szPackageName.equals("")) {
//            LogUtils.d(TAG, "szPackageName " + szPackageName);
//            LogUtils.d(TAG, "szServiceName " + szServiceName);
//
//            // 目标服务的包名和类名
//            //String packageName = this.getPackageName();
//            //String serviceClassName = SimpleOperateSignalCenterService.class.getName();
//
//            // 构建Intent
//            Intent intentService = new Intent();
//            intentService.setComponent(new ComponentName(szPackageName, szServiceName));
//            intentService.putExtra(ISOSService.EXTRA_ENABLE, true);
//            startService(intentService);
//            LogUtils.d(TAG, "startService(intentService)");
//        }
//        LogUtils.d(TAG, "helpISOSService");
//    }
}
