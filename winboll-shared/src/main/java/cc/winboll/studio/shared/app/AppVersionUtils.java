package cc.winboll.studio.shared.app;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/08/12 14:45:35
 * @Describe 应用版本工具集
 */
import com.hjq.toast.ToastUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppVersionUtils {

    public static final String TAG = "AppVersionUtils";

    //
    // 检查新版本是否成立
    // szCurrentCode : 当前版本应用包名
    // szNextCode : 新版本应用包名
    // 返回 ：情况1：当前版本是发布版
    //       返回 true (新版本 > 当前版本)
    //       情况1：当前版本是Beta版
    //       true 新版本 == 当前版本
    //
    public static boolean isHasNewVersion2(String szCurrentName, String szNextName) {
        //szCurrentName = "AES_6.2.0-beta0_3234.apk";
        //szNextName = "AES_6.1.12.apk";
        //szCurrentName = "AES_6.2.0-beta0_3234.apk";
        //szNextName = "AES_6.2.0.apk";
        //szCurrentName = "AES_6.2.0-beta0_3234.apk";
        //szNextName = "AES_6.2.2.apk";
        //szCurrentName = "AES_6.2.0-beta0_3234.apk";
        //szNextName = "AES_6.2.0.apk";
        //szCurrentName = "AES_6.1.0.apk";
        //szNextName = "AES_6.2.0.apk";
        //LogUtils.d(TAG, "szCurrentName : " + szCurrentName);
        //LogUtils.d(TAG, "szNextName : " + szNextName);

        //boolean isVersionNewer = false;
        //if(szCurrentName.equals(szNextName)) {
        //    isVersionNewer = false;
        //} else {
        //ToastUtils.show("szCurrent : " + szCurrent + "\nszNext : " + szNext);
        //int nApk = szNextName.lastIndexOf(".apk");
        //ToastUtils.show("nApk : " + Integer.toString(nApk));
        //String szNextNoApkName = szNextName.substring(0, nApk);
        //ToastUtils.show("szNextNoApkName : " + szNextNoApkName);
        //String szCurrentNoApkName = szCurrentName.substring(0, szNextNoApkName.length());
        //ToastUtils.show("szCurrentNoApkName : " + szCurrentNoApkName);
        //String str1 = "3.4.50";
        //String str2 = "3.3.60";
        //String str1 = getCodeInPackageName(szCurrentName);
        //String str2 = getCodeInPackageName(szNextName);
        //String str1 = getCodeInPackageName(szNextName);
        //String str2 = getCodeInPackageName(szCurrentName);
        //Boolean isVersionNewer2 = checkNewVersion(str1,str2);
        //ToastUtils.show("isVersionNewer2 : " + Boolean.toString(isVersionNewer2));
        //ToastUtils.show(checkNewVersion(getCodeInPackageName(szCurrentName), getCodeInPackageName(szNextName)));
        //return checkNewVersion(getCodeInPackageName(szCurrentName), getCodeInPackageName(szNextName));
        //}
        //return isVersionNewer;
        if (checkNewVersion(getCodeInPackageName(szCurrentName), getCodeInPackageName(szNextName))) {
            // 比 AES_6.2.0.apk 版本大，如 AES_6.2.1.apk。
            // 比 AES_6.2.0-beta0_3234.apk 大，如 AES_6.2.1.apk。
            //LogUtils.d(TAG, "App newer stage version is released. Release name : " + szNextName);
            return true;
        } 
        if (szCurrentName.matches(".*_\\d+\\.\\d+\\.\\d+-beta.*\\.apk")) {
            String szCurrentReleasePackageName = getReleasePackageName(szCurrentName);
            //LogUtils.d(TAG, "szCurrentReleasePackageName : " + szCurrentReleasePackageName);
            if (szCurrentReleasePackageName.equals(szNextName)) {
                // 与 AES_6.2.0-beta0_3234.apk 版本相同，如 AES_6.2.0.apk。
                //LogUtils.d(TAG, "App stage version is released. Release name : " + szNextName);
                return true;
            }
        }
        //LogUtils.d(TAG, "App version is the newest. ");
        return false;
    }
    
    public static boolean isHasNewStageReleaseVersion(String szCurrentName, String szNextName) {
        //szCurrentName = "AES_6.2.12.apk";
        //szNextName = "AES_6.3.12.apk";
        if (checkNewVersion(getCodeInPackageName(szCurrentName), getCodeInPackageName(szNextName))) {
            // 比 AES_6.2.0.apk 版本大，如 AES_6.2.1.apk。
            //LogUtils.d(TAG, "App newer stage version is released. Release name : " + szNextName);
            return true;
        }
        return false;
    }

    //
    // 检查新版本是否成立
    // szCurrentCode : 当前版本
    // szNextCode : 新版本
    // 返回 ：true 新版本 > 当前版本
    //
    public static Boolean checkNewVersion(String szCurrentCode, String szNextCode) {
        boolean isNew = false;
        String[] appVersionCurrent = szCurrentCode.split("\\.");
        String[] appVersionNext = szNextCode.split("\\.");
        //根据位数最短的判断
        int lim = appVersionCurrent.length > appVersionNext.length ? appVersionNext.length : appVersionCurrent.length;
        //根据位数循环判断各个版本
        for (int i = 0; i < lim; i++) {
            if (Integer.parseInt(appVersionNext[i]) > Integer.parseInt(appVersionCurrent[i])) {
                isNew = true;
                return isNew;
            } else if(Integer.parseInt(appVersionNext[i]) == Integer.parseInt(appVersionCurrent[i])) {
                continue ;
            } else {
                isNew = false;
                return isNew;
            }
        }
        return isNew;
    }

    //
    // 截取应用包名称版本号信息
    // 如 ：AppUtils_7.0.4-beta1_0120.apk 版本号为 7.0.4
    // 如 ：AppUtils_7.0.4.apk 版本号为 7.0.4
    //
    public static String getCodeInPackageName(String apkName) {
        //String apkName = "AppUtils_7.0.0.apk";
        Pattern pattern = Pattern.compile("\\d+\\.\\d+\\.\\d+");
        Matcher matcher = pattern.matcher(apkName);
        if (matcher.find()) {
            String version = matcher.group();
            return version;
            //System.out.println("Version number: " + version); // 输出：7.0.0
        }
        return "";
    }

    //
    // 根据Beta版名称生成发布版应用包名称
    // 如 AppUtils_7.0.4-beta1_0120.apk
    // 发布版名称就为AppUtils_7.0.4.apk
    //
    public static String getReleasePackageName(String szBetaPackageName) {
        //String szBetaPackageName = "AppUtils_7.0.4-beta1_0120.apk";
        Pattern pattern = Pattern.compile(".*\\d+\\.\\d+\\.\\d+");
        Matcher matcher = pattern.matcher(szBetaPackageName);
        if (matcher.find()) {
            String szReleasePackageName = matcher.group();
            return szReleasePackageName + ".apk";
            //System.out.println("Version number: " + version); // 输出：7.0.0
        }
        return "";
    }

}
