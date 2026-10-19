package cc.winboll.studio.contacts.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.telecom.TelecomManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/12/12 16:28
 * @Describe 敏感权限申请工具类（完全适配 Android API 30 + Java 7 语法）
 * 修复 ACTION_CHANGE_DEFAULT_CALL_SCREENING_APP / EXTRA_PACKAGE_NAME 未定义问题
 */
public class PermissionUtils {
    public static final String TAG = "PermissionUtils";

    // API 版本硬编码常量（Java 7 兼容，不依赖 Build.VERSION_CODES 高版本字段）
    private static final int ANDROID_6_API = 23;
    private static final int ANDROID_10_API = 29;
    private static final int ANDROID_13_API = 33;
    private static final int ANDROID_14_API = 34;

    // 硬编码系统常量字符串，解决 API 30 下未定义问题
    private static final String ACTION_CHANGE_DEFAULT_CALL_SCREENING_APP =
	"android.telecom.action.CHANGE_DEFAULT_CALL_SCREENING_APP";
    private static final String EXTRA_PACKAGE_NAME =
	"android.telecom.extra.PACKAGE_NAME";

    // 基础权限组（严格适配 API 30，移除废弃/不存在的权限）
    public static final String[] BASE_PERMISSIONS = {
		android.Manifest.permission.READ_CONTACTS,
		android.Manifest.permission.WRITE_CONTACTS,
		android.Manifest.permission.READ_CALL_LOG,
		android.Manifest.permission.CALL_PHONE,
		android.Manifest.permission.RECORD_AUDIO,
		android.Manifest.permission.MODIFY_AUDIO_SETTINGS
    };

    /**
     * 获取所有需要申请的权限（Java 7 传统 for 循环，无菱形运算符）
     */
    public static String[] getAllNeedPermissions() {
        List<String> permissions = new ArrayList<String>();
        // Java 7 传统循环遍历数组
        for (int i = 0; i < BASE_PERMISSIONS.length; i++) {
            permissions.add(BASE_PERMISSIONS[i]);
        }
        // 显式创建数组并转换，避免 Java 7 泛型转换警告
        String[] permissionArray = new String[permissions.size()];
        return permissions.toArray(permissionArray);
    }

    /**
     * 检查单个权限是否授予（使用 PackageManager 标准常量）
     */
    public static boolean checkPermission(@NonNull Context context, @NonNull String permission) {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 检查权限组是否全部授予（Java 7 传统循环）
     */
    public static boolean checkPermissions(@NonNull Context context, @NonNull String[] permissions) {
        // Java 7 遍历数组，避免增强 for 循环的语法糖问题
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            if (!checkPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 申请权限组（Activity 中调用，Java 7 兼容）
     */
    public static void requestPermissions(@NonNull FragmentActivity activity,
                                          @NonNull String[] permissions,
                                          int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    /**
     * 申请权限组（Fragment 中调用，Java 7 兼容）
     */
    public static void requestPermissions(@NonNull Fragment fragment,
                                          @NonNull String[] permissions,
                                          int requestCode) {
        fragment.requestPermissions(permissions, requestCode);
    }

    /**
     * 检查悬浮窗权限（API 30 适配）
     */
    public static boolean isOverlayPermissionGranted(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= ANDROID_6_API) {
            return Settings.canDrawOverlays(context);
        }
        // 6.0 以下默认授予
        return true;
    }

    /**
     * 申请悬浮窗权限（Java 7 规范，拆分 Intent 创建步骤）
     */
    public static void requestOverlayPermission(@NonNull Context context, int requestCode) {
        if (Build.VERSION.SDK_INT >= ANDROID_6_API && !isOverlayPermissionGranted(context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            Uri uri = Uri.parse("package:" + context.getPackageName());
            intent.setData(uri);
            if (context instanceof FragmentActivity) {
                ((FragmentActivity) context).startActivityForResult(intent, requestCode);
            }
        }
    }

    /**
     * 检查修改系统设置权限（API 30 适配）
     */
    public static boolean isWriteSettingsPermissionGranted(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= ANDROID_6_API) {
            return Settings.System.canWrite(context);
        }
        // 6.0 以下默认授予
        return true;
    }

    /**
     * 申请修改系统设置权限（Java 7 规范）
     */
    public static void requestWriteSettingsPermission(@NonNull Context context, int requestCode) {
        if (Build.VERSION.SDK_INT >= ANDROID_6_API && !isWriteSettingsPermissionGranted(context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            Uri uri = Uri.parse("package:" + context.getPackageName());
            intent.setData(uri);
            if (context instanceof FragmentActivity) {
                ((FragmentActivity) context).startActivityForResult(intent, requestCode);
            }
        }
    }

    /**
     * 检查通话筛选权限（适配 API 30，优化反射逻辑 + 异常捕获）
     */
    public static boolean isCallScreeningPermissionGranted(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= ANDROID_10_API) {
            TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager == null) {
                return false;
            }
            String defaultPackage = null;
            // 反射调用高版本方法，捕获所有异常避免崩溃（Java 7 必须显式捕获 Exception）
            try {
                Method method = TelecomManager.class.getMethod("getDefaultCallScreeningAppPackage");
                defaultPackage = (String) method.invoke(telecomManager);
            } catch (NoSuchMethodException e) {
                // API 30-32 无此方法，返回 false
                return false;
            } catch (Exception e) {
                // 其他反射异常，返回 false
                return false;
            }
            return defaultPackage != null && defaultPackage.equals(context.getPackageName());
        }
        // 10.0 以下无此权限，默认返回 true
        return true;
    }

    /**
     * 申请通话筛选权限（完全适配 API 30，解决 ActivityNotFoundException 崩溃）
     */
    public static void requestCallScreeningPermission(@NonNull Context context, int requestCode) {
        if (Build.VERSION.SDK_INT >= ANDROID_10_API && !isCallScreeningPermissionGranted(context)) {
            FragmentActivity activity = null;
            if (context instanceof FragmentActivity) {
                activity = (FragmentActivity) context;
            }
            if (activity == null) {
                return;
            }

            Intent intent = null;
            // 版本分级处理：避免高版本 ACTION 失效
            if (Build.VERSION.SDK_INT >= ANDROID_14_API) {
                // Android 14+：跳转默认应用设置页
                intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
                Uri uri = Uri.parse("package:" + context.getPackageName());
                intent.setData(uri);
            } else if (Build.VERSION.SDK_INT >= ANDROID_13_API) {
                // Android 13：使用硬编码 ACTION
                intent = new Intent(ACTION_CHANGE_DEFAULT_CALL_SCREENING_APP);
                intent.putExtra(EXTRA_PACKAGE_NAME, context.getPackageName());
            } else {
                // API 30-32：直接跳转应用详情页
                goAppDetailsSettings(context);
                return;
            }

            // 捕获 Activity 找不到异常，兜底处理（Java 7 必须显式捕获）
            try {
                activity.startActivityForResult(intent, requestCode);
            } catch (android.content.ActivityNotFoundException e) {
                // 兜底：跳转应用详情页
                goAppDetailsSettings(context);
            }
        }
    }

    /**
     * 跳转应用详情页（权限兜底引导，Java 7 规范）
     */
    public static void goAppDetailsSettings(@NonNull Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 解析被拒绝的权限（Java 7 字符串操作，无 Lambda）
     */
    public static String getDeniedPermissions(@NonNull Context context, @NonNull String[] permissions) {
        StringBuilder deniedPerms = new StringBuilder();
        // Java 7 传统循环遍历权限数组
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            if (!checkPermission(context, permission)) {
                // 截取权限名称，优化展示
                int lastDotIndex = permission.lastIndexOf(".");
                if (lastDotIndex != -1 && lastDotIndex < permission.length() - 1) {
                    String permName = permission.substring(lastDotIndex + 1);
                    deniedPerms.append(permName).append("、");
                }
            }
        }
        // 移除最后一个分隔符（Java 7 字符串操作）
        if (deniedPerms.length() > 0) {
            deniedPerms.deleteCharAt(deniedPerms.length() - 1);
        }
        return deniedPerms.toString();
    }
}

