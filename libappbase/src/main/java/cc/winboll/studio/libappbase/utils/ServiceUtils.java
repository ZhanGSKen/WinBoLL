package cc.winboll.studio.libappbase.utils;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/17 19:38:20
 * @Describe 服务工具集
 */
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import cc.winboll.studio.libappbase.LogUtils;
import java.util.List;

public class ServiceUtils {

    public static final String TAG = "ServiceUtils";

    /**
     * 检查指定服务是否正在运行
     * @param context 上下文
     * @param serviceClass 服务类
     * @return true 如果服务正在运行，否则返回 false
     */
    public static boolean isServiceRunning(Context context, String serviceClassName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return false;
        }
        List<ActivityManager.RunningServiceInfo> runningServices;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            Intent intent = new Intent(context, serviceClass);
//            runningServices = activityManager.getRunningServices(100, intent);
//        } else {
        runningServices = activityManager.getRunningServices(100);
        //}
        for (ActivityManager.RunningServiceInfo serviceInfo : runningServices) {
            if (serviceClassName.equals(serviceInfo.service.getClassName())) {
                LogUtils.d(TAG, "Service is running: " + serviceInfo.service.getClassName());
                return true;
            }
        }
        LogUtils.d(TAG, "Service is not running: " + serviceClassName);
        return false;
    }
}

