package cc.winboll.studio.autoinstaller.utils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/12/11 06:28:50
 * @Describe 一个获取安卓APK安装文件的应用包名的函数
 */
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import cc.winboll.studio.libappbase.LogUtils;

public class PackageUtil {

    public static final String TAG = "PackageUtil";

    public static String getPackageNameFromApk(Context context, String apkFilePath) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageArchiveInfo(apkFilePath, 0);
            if (packageInfo != null) {
                return packageInfo.packageName;
            }
        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
        return "";
    }

    public static void openAPP(Context context, String packageName) {
        // 这里假设要打开微信，微信的包名是com.tencent.mm
        //String packageName = "com.tencent.mm"; 
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            ResolveInfo resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfo != null) {
                context.startActivity(intent);
            }
        }

    }
}
