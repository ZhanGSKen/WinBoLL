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
import cc.winboll.studio.libappbase.winboll.WinBollActivityManager;
import cc.winboll.studio.libappbase.winboll.MyActivityLifecycleCallbacks;

public class GlobalApplication extends Application {

    public static final String TAG = "GlobalApplication";

    final static String PREFS = GlobalApplication.class.getName() + "PREFS";
    final static String PREFS_ISDEBUGING = "PREFS_ISDEBUGING";

    private static Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    volatile static GlobalApplication _GlobalApplication;
    // 是否处于调试状态
    volatile static boolean isDebuging = false;
    WinBollActivityManager mWinBollActivityManager;
    MyActivityLifecycleCallbacks mMyActivityLifecycleCallbacks;

    public static void setIsDebuging(boolean isDebuging) {
        if (_GlobalApplication != null) {
            GlobalApplication.isDebuging = isDebuging;
            APPBaseModel.saveBeanToFile(getAPPBaseModelFilePath(), new APPBaseModel(isDebuging));
            // 获取SharedPreferences实例
//        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
//        // 获取编辑器
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        // 保存数据
//        editor.putBoolean(PREFS_ISDEBUGING, GlobalApplication.isDebuging);
//        // 提交更改
//        editor.apply();
        }
    }

    static String getAPPBaseModelFilePath() {
        return _GlobalApplication.getDataDir().getPath() + "/APPBaseModel.json";
    }

    public static boolean isDebuging() {
        return isDebuging;
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    public Application getApplication() {
        return this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _GlobalApplication = this;

        // 设置应用调试标志
        APPBaseModel appBaseModel = APPBaseModel.loadBeanFromFile(getAPPBaseModelFilePath(), APPBaseModel.class);
        if (appBaseModel == null) {
            setIsDebuging(false);
        } else {
            setIsDebuging(appBaseModel.isDebuging());
        }

        LogUtils.init(this);
        //LogUtils.setLogLevel(LogUtils.LOG_LEVEL.Debug);
        //LogUtils.setTAGListEnable(GlobalApplication.TAG, true);
        //LogUtils.setALlTAGListEnable(true);
        //LogUtils.d(TAG, "LogUtils init");

        // 设置应用异常处理窗口
        CrashHandler.init(this);

        // 初始化 Toast 框架
        ToastUtils.init(this);

        mWinBollActivityManager = WinBollActivityManager.getInstance(this);
        mWinBollActivityManager.setWinBollUI_TYPE(WinBollActivityManager.WinBollUI_TYPE.Service);
        // 注册回调
        mMyActivityLifecycleCallbacks = new MyActivityLifecycleCallbacks(mWinBollActivityManager);
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
