package cc.winboll.studio.libappbase;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/01/05 10:10:23
 * @Describe 全局应用类
 */
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.models.APPModel;
import cc.winboll.studio.libappbase.utils.ToastUtils;
import cc.winboll.studio.libappbase.winboll.MyActivityLifecycleCallbacks;
import cc.winboll.studio.libappbase.winboll.WinBoLLActivityManager;

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
            APPModel.saveBeanToFile(getAPPModelFilePath(), new APPModel(GlobalApplication.isDebuging));
        }
    }

    public static GlobalApplication getInstance() {
        return _GlobalApplication;
    }

    static String getAPPModelFilePath() {
        return _GlobalApplication.getDataDir().getPath() + "/APPModel.json";
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
        APPModel appModel = APPModel.loadBeanFromFile(getAPPModelFilePath(), APPModel.class);
        if (appModel == null) {
            setIsDebuging(false);
            saveDebugStatus();
        } else {
            setIsDebuging(appModel.isDebuging());
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
