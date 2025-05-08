package cc.winboll.studio.watchoutputinstaller.utils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/12/11 06:28:50
 * @Describe 一个获取安卓APK安装文件的应用包名的函数
 */
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import cc.winboll.studio.shared.log.LogUtils;

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
}
