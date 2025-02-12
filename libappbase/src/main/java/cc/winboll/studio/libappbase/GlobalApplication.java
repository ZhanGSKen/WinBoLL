package cc.winboll.studio.libappbase;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/05 10:10:23
 * @Describe 全局应用类
 */
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.WhiteToastStyle;

public class GlobalApplication extends Application implements ISOSAPP {

    public static final String TAG = "GlobalApplication";

    final static String PREFS = GlobalApplication.class.getName() + "PREFS";
    final static String PREFS_ISDEBUGING = "PREFS_ISDEBUGING";


    private static Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    // 是否处于调试状态
    volatile static boolean isDebuging = false;

    public static void setIsDebuging(Context context, boolean isDebuging) {
        GlobalApplication.isDebuging = isDebuging;
        // 获取SharedPreferences实例
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        // 获取编辑器
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // 保存数据
        editor.putBoolean(PREFS_ISDEBUGING, GlobalApplication.isDebuging);
        // 提交更改
        editor.apply();
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
        //GlobalApplication.isDebuging = true;
        //GlobalApplication.setIsDebuging(this, true);
        LogUtils.init(this);
        //LogUtils.setLogLevel(LogUtils.LOG_LEVEL.Debug);
        //LogUtils.setTAGListEnable(GlobalApplication.TAG, true);
        //LogUtils.setALlTAGListEnable(true);
        //LogUtils.d(TAG, "LogUtils init");

        // 设置应用异常处理窗口
        CrashHandler.init(this);

        // 设置应用调试状态
        //SharedPreferences sharedPreferences = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        //GlobalApplication.isDebuging = sharedPreferences.getBoolean(PREFS_ISDEBUGING, GlobalApplication.isDebuging);

        // 初始化 Toast 框架
        ToastUtils.init(this);
        // 设置 Toast 布局样式
        //ToastUtils.setView(R.layout.toast_custom_view);
        ToastUtils.setStyle(new WhiteToastStyle());
        ToastUtils.setGravity(Gravity.BOTTOM, 0, 200);
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

    @Override
    public void helpISOSService(Intent intent) {
        String szServiceName = intent.getStringExtra(EXTRA_SERVICE);
        String szPackageName = intent.getStringExtra(EXTRA_PACKAGE);
        if (szServiceName != null && !szServiceName.equals("")
            && szPackageName != null && !szPackageName.equals("")) {
            LogUtils.d(TAG, "szPackageName " + szPackageName);
            LogUtils.d(TAG, "szServiceName " + szServiceName);

            // 目标服务的包名和类名
            //String packageName = this.getPackageName();
            //String serviceClassName = SimpleOperateSignalCenterService.class.getName();

            // 构建Intent
            Intent intentService = new Intent();
            intentService.setComponent(new ComponentName(szPackageName, szServiceName));
            intentService.putExtra(ISOSService.EXTRA_ENABLE, true);
            startService(intentService);
            LogUtils.d(TAG, "startService(intentService)");
        }
        LogUtils.d(TAG, "helpISOSService");
    }
}
