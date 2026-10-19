package cc.winboll.studio.contacts.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import cc.winboll.studio.contacts.MainActivity;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/09/27 14:27
 * @Describe 应用权限设置页跳转工具类，适配主流手机厂商的权限页路径，跳转失败时降级到应用详情页
 */
public class AppGoToSettingsUtil {
    // ====================== 常量定义区 ======================
    public static final String TAG = "AppGoToSettingsUtil";
    // 跳转设置页的 Activity 结果码，复用 MainActivity 的请求码
    public static final int ACTIVITY_RESULT_APP_SETTINGS = MainActivity.REQUEST_APP_SETTINGS;

    // 主流手机厂商品牌常量
    private static final String MANUFACTURER_HUAWEI = "Huawei";
    private static final String MANUFACTURER_MEIZU = "Meizu";
    private static final String MANUFACTURER_XIAOMI = "Xiaomi";
    private static final String MANUFACTURER_SONY = "Sony";
    private static final String MANUFACTURER_OPPO = "OPPO";
    private static final String MANUFACTURER_LG = "LG";
    private static final String MANUFACTURER_VIVO = "vivo";
    private static final String MANUFACTURER_SAMSUNG = "samsung";
    private static final String MANUFACTURER_LETV = "Letv";
    private static final String MANUFACTURER_ZTE = "ZTE";
    private static final String MANUFACTURER_YULONG = "YuLong";
    private static final String MANUFACTURER_LENOVO = "LENOVO";

    // ====================== 成员变量区 ======================
    // 标记当前跳转的是应用详情页(true)还是厂商权限页(false)
    public static boolean isAppSettingOpen = false;

    // ====================== 核心跳转方法区 ======================
    /**
     * 跳转到对应品牌手机的系统权限设置页，跳转失败则降级到应用详情页
     * @param activity 上下文 Activity
     */
    public static void goToSetting(Activity activity) {
        // 空值校验，避免空指针异常
        if (activity == null) {
            LogUtils.e(TAG, "goToSetting: Activity 为 null，无法跳转设置页");
            return;
        }

        String manufacturer = Build.MANUFACTURER;
        LogUtils.d(TAG, "goToSetting: 当前设备厂商 | " + manufacturer);

        // 根据厂商跳转对应权限页
        switch (manufacturer) {
            case MANUFACTURER_HUAWEI:
                gotoHuaweiSetting(activity);
                break;
            case MANUFACTURER_MEIZU:
                gotoMeizuSetting(activity);
                break;
            case MANUFACTURER_XIAOMI:
                gotoXiaomiSetting(activity);
                break;
            case MANUFACTURER_SONY:
                gotoSonySetting(activity);
                break;
            case MANUFACTURER_OPPO:
                gotoOppoSetting(activity);
                break;
            case MANUFACTURER_LG:
                gotoLgSetting(activity);
                break;
            case MANUFACTURER_LETV:
                gotoLetvSetting(activity);
                break;
            default:
                LogUtils.w(TAG, "goToSetting: 未适配当前厂商，跳转应用详情页");
                openAppDetailSetting(activity);
                break;
        }
    }

    // ====================== 各厂商权限页跳转方法区 ======================
    /**
     * 跳转华为手机权限设置页
     */
    private static void gotoHuaweiSetting(Activity activity) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("packageName", activity.getPackageName());
            intent.setComponent(new ComponentName("com.huawei.systemmanager",
												  "com.huawei.permissionmanager.ui.MainActivity"));
            activity.startActivityForResult(intent, ACTIVITY_RESULT_APP_SETTINGS);
            isAppSettingOpen = false;
            LogUtils.d(TAG, "gotoHuaweiSetting: 跳转华为权限设置页成功");
        } catch (Exception e) {
            LogUtils.e(TAG, "gotoHuaweiSetting: 跳转失败，降级到应用详情页", e);
            openAppDetailSetting(activity);
        }
    }

    /**
     * 跳转魅族手机权限设置页
     */
    private static void gotoMeizuSetting(Activity activity) {
        try {
            Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("packageName", activity.getPackageName());
            activity.startActivity(intent);
            isAppSettingOpen = false;
            LogUtils.d(TAG, "gotoMeizuSetting: 跳转魅族权限设置页成功");
        } catch (Exception e) {
            LogUtils.e(TAG, "gotoMeizuSetting: 跳转失败，降级到应用详情页", e);
            openAppDetailSetting(activity);
        }
    }

    /**
     * 跳转小米手机权限设置页
     */
    private static void gotoXiaomiSetting(Activity activity) {
        try {
            // 适配 MIUI 8/9 及以上版本
            Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter",
								"com.miui.permcenter.permissions.PermissionsEditorActivity");
            intent.putExtra("extra_pkgname", activity.getPackageName());
            activity.startActivityForResult(intent, ACTIVITY_RESULT_APP_SETTINGS);
            isAppSettingOpen = false;
            LogUtils.d(TAG, "gotoXiaomiSetting: 跳转小米权限设置页(MIUI8+)成功");
        } catch (Exception e) {
            try {
                // 适配 MIUI 5/6/7 版本
                Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                intent.setClassName("com.miui.securitycenter",
									"com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                intent.putExtra("extra_pkgname", activity.getPackageName());
                activity.startActivityForResult(intent, ACTIVITY_RESULT_APP_SETTINGS);
                isAppSettingOpen = false;
                LogUtils.d(TAG, "gotoXiaomiSetting: 跳转小米权限设置页(MIUI5-7)成功");
            } catch (Exception e1) {
                LogUtils.e(TAG, "gotoXiaomiSetting: 所有版本适配失败，降级到应用详情页", e1);
                openAppDetailSetting(activity);
            }
        }
    }

    /**
     * 跳转索尼手机权限设置页
     */
    private static void gotoSonySetting(Activity activity) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("packageName", activity.getPackageName());
            intent.setComponent(new ComponentName("com.sonymobile.cta",
												  "com.sonymobile.cta.SomcCTAMainActivity"));
            activity.startActivity(intent);
            isAppSettingOpen = false;
            LogUtils.d(TAG, "gotoSonySetting: 跳转索尼权限设置页成功");
        } catch (Exception e) {
            LogUtils.e(TAG, "gotoSonySetting: 跳转失败，降级到应用详情页", e);
            openAppDetailSetting(activity);
        }
    }

    /**
     * 跳转OPPO手机权限设置页
     */
    private static void gotoOppoSetting(Activity activity) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("packageName", activity.getPackageName());
            intent.setComponent(new ComponentName("com.color.safecenter",
												  "com.color.safecenter.permission.PermissionManagerActivity"));
            activity.startActivity(intent);
            isAppSettingOpen = false;
            LogUtils.d(TAG, "gotoOppoSetting: 跳转OPPO权限设置页成功");
        } catch (Exception e) {
            LogUtils.e(TAG, "gotoOppoSetting: 跳转失败，降级到应用详情页", e);
            openAppDetailSetting(activity);
        }
    }

    /**
     * 跳转LG手机权限设置页
     */
    private static void gotoLgSetting(Activity activity) {
        try {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("packageName", activity.getPackageName());
            intent.setComponent(new ComponentName("com.android.settings",
												  "com.android.settings.Settings$AccessLockSummaryActivity"));
            activity.startActivity(intent);
            isAppSettingOpen = false;
            LogUtils.d(TAG, "gotoLgSetting: 跳转LG权限设置页成功");
        } catch (Exception e) {
            LogUtils.e(TAG, "gotoLgSetting: 跳转失败，降级到应用详情页", e);
            openAppDetailSetting(activity);
        }
    }

    /**
     * 跳转乐视手机权限设置页
     */
    private static void gotoLetvSetting(Activity activity) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("packageName", activity.getPackageName());
            intent.setComponent(new ComponentName("com.letv.android.letvsafe",
												  "com.letv.android.letvsafe.PermissionAndApps"));
            activity.startActivity(intent);
            isAppSettingOpen = false;
            LogUtils.d(TAG, "gotoLetvSetting: 跳转乐视权限设置页成功");
        } catch (Exception e) {
            LogUtils.e(TAG, "gotoLetvSetting: 跳转失败，降级到应用详情页", e);
            openAppDetailSetting(activity);
        }
    }

    // ====================== 降级跳转方法区 ======================
    /**
     * 跳转系统设置主界面
     */
    public static void gotoSystemConfig(Activity activity) {
        if (activity == null) {
            LogUtils.e(TAG, "gotoSystemConfig: Activity 为 null，无法跳转");
            return;
        }
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        activity.startActivity(intent);
        LogUtils.d(TAG, "gotoSystemConfig: 跳转系统设置主界面成功");
    }

    /**
     * 获取应用详情页的 Intent
     */
    private static Intent getAppDetailSettingIntent(Activity activity) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        return intent;
    }

    /**
     * 打开应用详情设置页
     */
    public static void openAppDetailSetting(Activity activity) {
        if (activity == null) {
            LogUtils.e(TAG, "openAppDetailSetting: Activity 为 null，无法跳转");
            return;
        }
        activity.startActivityForResult(getAppDetailSettingIntent(activity), ACTIVITY_RESULT_APP_SETTINGS);
        isAppSettingOpen = true;
        LogUtils.d(TAG, "openAppDetailSetting: 跳转应用详情设置页成功");
    }
}

