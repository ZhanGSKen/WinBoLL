package cc.winboll.studio.libappbase;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/02/15 20:05:03
 * @Describe AppUtils
 */
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class AppUtils {
    
    public static final String TAG = "AppUtils";
    
    public static String getAppNameByPackageName(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            return (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (NameNotFoundException e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
            return "";
        }
    }
}

