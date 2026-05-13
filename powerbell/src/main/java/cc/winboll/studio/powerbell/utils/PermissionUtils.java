package cc.winboll.studio.powerbell.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import cc.winboll.studio.libaes.dialogs.YesNoAlertDialog;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.powerbell.App;
import cc.winboll.studio.powerbell.MainActivity;
import cc.winboll.studio.powerbell.R;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/12/14 03:05
 * @Describe 权限申请工具类（Java7兼容版）
 * 适配 小米手机+API29-30，整合自启动、电池优化、全文件管理权限，专注后台保活核心权限
 */
public class PermissionUtils {
    // ====================== 常量定义（首屏可见，统一管理，避免冲突）======================
    // 日志标签
    public static final String TAG = "PermissionUtils";
    // 权限请求码（按场景分段，避免重复）
    public static final int REQUEST_IGNORE_BATTERY_OPTIMIZATION = 1000; // 电池优化权限
    public static final int REQUEST_AUTO_START = 1001;                   // 自启动权限（小米专属）
    public static final int REQUEST_ALL_FILE_MANAGE = 1002;              // 全文件管理权限（API30+）
    // SDK版本常量（适配API29-30，替代系统枚举，Java7兼容）
    private static final int SDK_VERSION_Q = 29;                         // Android 10（API29）
    private static final int SDK_VERSION_R = 30;                         // Android 11（API30）
    // 小米自启动权限页面配置（专属跳转路径，精准适配）
    private static final String XIAOMI_AUTO_START_PACKAGE = "com.miui.securitycenter";
    private static final String XIAOMI_AUTO_START_CLASS = "com.miui.permcenter.autostart.AutoStartManagementActivity";

    // ====================== 单例模式（Java7标准双重校验锁，线程安全+懒加载）======================
    private static volatile PermissionUtils sInstance;

    private PermissionUtils() {}

    public static PermissionUtils getInstance() {
        if (sInstance == null) {
            synchronized (PermissionUtils.class) {
                if (sInstance == null) {
                    sInstance = new PermissionUtils();
                    LogUtils.d(TAG, "初始化：PermissionUtils 单例创建成功");
                }
            }
        }
        return sInstance;
    }

    // ====================== 核心权限1：全文件管理权限（API29-30适配，通用所有机型）======================
    /**
     * 检查全文件管理权限（适配API30+ MANAGE_EXTERNAL_STORAGE，兼容API29-旧权限）
     * @param activity 上下文Activity（不可为null）
     * @return true=权限已授予，false=权限未授予
     */
    public boolean checkAllFileManagePermission(Activity activity) {
        LogUtils.d(TAG, "全文件权限-检查：开始校验，系统版本=" + Build.VERSION.SDK_INT);
        if (activity == null) {
            LogUtils.e(TAG, "全文件权限-检查：失败，Activity为空");
            return false;
        }

        // API30+：校验 MANAGE_EXTERNAL_STORAGE 特殊权限
        if (Build.VERSION.SDK_INT >= SDK_VERSION_R) {
            boolean hasManagePerm = Environment.isExternalStorageManager();
            LogUtils.d(TAG, "全文件权限-检查：API30+，MANAGE_EXTERNAL_STORAGE权限=" + (hasManagePerm ? "已授予" : "未授予"));
            return hasManagePerm;
        } else if (Build.VERSION.SDK_INT == SDK_VERSION_Q) {
            LogUtils.d(TAG, "全文件权限-检查：API29，无需申请，默认支持文件管理");
            return true;
        } else {
            boolean hasWritePerm = ContextCompat.checkSelfPermission(activity,
																	 android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            LogUtils.d(TAG, "全文件权限-检查：API29以下，WRITE_EXTERNAL_STORAGE权限=" + (hasWritePerm ? "已授予" : "未授予"));
            return hasWritePerm;
        }
    }

    /**
     * 申请全文件管理权限（适配API30+特殊权限流程，兼容API29-旧权限申请）
     * @param activity 申请权限的Activity（不可为null）
     */
    public void requestAllFileManagePermission(Activity activity) {
        LogUtils.d(TAG, "全文件权限-申请：开始处理，系统版本=" + Build.VERSION.SDK_INT);
        if (activity == null || activity.isFinishing()) {
            LogUtils.e(TAG, "全文件权限-申请：失败，Activity无效/已销毁");
            return;
        }

        // 先检查权限，已授予直接返回
        if (checkAllFileManagePermission(activity)) {
            LogUtils.d(TAG, "全文件权限-申请：已拥有权限，无需发起");
            return;
        }

        // API30+：跳转系统特殊权限申请页（用户手动授权）
        if (Build.VERSION.SDK_INT >= SDK_VERSION_R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, REQUEST_ALL_FILE_MANAGE);
                LogUtils.d(TAG, "全文件权限-申请：API30+，跳转特殊权限申请页");
            } catch (Exception e) {
                // 备用跳转：系统设置首页，引导手动操作
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                activity.startActivityForResult(intent, REQUEST_ALL_FILE_MANAGE);
                LogUtils.w(TAG, "全文件权限-申请：跳转失败，引导手动开启");
                showAllFileManageTipsDialog(activity);
            }
        } else {
            ActivityCompat.requestPermissions(activity,
											  new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
											  REQUEST_ALL_FILE_MANAGE);
            LogUtils.d(TAG, "全文件权限-申请：API29以下，发起WRITE_EXTERNAL_STORAGE权限申请");
        }
    }

    // ====================== 核心权限2：自启动权限（小米专属，API29-30适配）======================
    /**
     * 检查自启动权限（仅小米机型需要，非小米直接返回无需申请）
     * @param activity 上下文Activity（不可为null）
     * @return true=小米机型（需手动开启）；false=非小米机型（无需申请）
     */
//    public boolean checkAutoStartPermission(Activity activity) {
//        LogUtils.d(TAG, "自启动权限-检查：开始，设备品牌=" + Build.BRAND);
//        if (activity == null) {
//            LogUtils.e(TAG, "自启动权限-检查：失败，Activity为空");
//            return false;
//        }
//
//        boolean isXiaomi = Build.BRAND.toLowerCase().contains("xiaomi");
//        LogUtils.d(TAG, "自启动权限-检查：结果=" + (isXiaomi ? "小米机型（需开启）" : "非小米机型（无需申请）"));
//        return isXiaomi;
//    }

    /**
     * 请求自启动权限（小米专属，多方案跳转，适配API29-30机型差异）
     * @param activity 申请权限的Activity（不可为null）
     */
    public void requestAutoStartPermission(Activity activity) {
        LogUtils.d(TAG, "自启动权限-申请：开始处理");
        if (activity == null || activity.isFinishing()) {
            LogUtils.e(TAG, "自启动权限-申请：失败，Activity无效/已销毁");
            return;
        }

        // 非小米机型，直接返回
//        if (!checkAutoStartPermission(activity)) {
//            LogUtils.d(TAG, "自启动权限-申请：非小米机型，无需处理");
//            return;
//        }

        // API30+ 小米：优先精准跳转自启动管理页
        if (Build.VERSION.SDK_INT >= SDK_VERSION_R) {
            try {
                // 方案1：组件名精准跳转（成功率最高）
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(XIAOMI_AUTO_START_PACKAGE, XIAOMI_AUTO_START_CLASS));
                activity.startActivityForResult(intent, REQUEST_AUTO_START);
                LogUtils.d(TAG, "自启动权限-申请：API30+，组件名跳转自启动管理页");
            } catch (Exception e1) {
                try {
                    // 方案2：Action备用跳转（兼容机型差异）
                    Intent intent = new Intent("miui.intent.action.OP_AUTO_START");
                    intent.setClassName(XIAOMI_AUTO_START_PACKAGE, XIAOMI_AUTO_START_CLASS);
                    activity.startActivityForResult(intent, REQUEST_AUTO_START);
                    LogUtils.d(TAG, "自启动权限-申请：API30+，Action跳转自启动管理页");
                } catch (Exception e2) {
                    // 方案3：终极备用，跳转系统设置+提示
                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                    activity.startActivityForResult(intent, REQUEST_AUTO_START);
                    LogUtils.w(TAG, "自启动权限-申请：跳转失败，引导手动操作");
                    showAutoStartTipsDialog(activity);
                }
            }
            return;
        }

        // API29 小米：低版本兼容跳转
        try {
            Intent intent = new Intent(XIAOMI_AUTO_START_CLASS);
            intent.setPackage(XIAOMI_AUTO_START_PACKAGE);
            activity.startActivityForResult(intent, REQUEST_AUTO_START);
            LogUtils.d(TAG, "自启动权限-申请：API29，低版本跳转自启动管理页");
        } catch (Exception e) {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            activity.startActivityForResult(intent, REQUEST_AUTO_START);
            showAutoStartTipsDialog(activity);
        }
    }

    // ====================== 核心权限3：电池优化权限（通用所有机型，API29-30适配）======================
    /**
     * 检查忽略电池优化权限（精准判断，API23+有效，低版本视为已拥有）
     * @param activity 上下文Activity（不可为null）
     * @return true=已忽略优化；false=未忽略（需申请）
     */
    public boolean checkIgnoreBatteryOptimizationPermission(Activity activity) {
        LogUtils.d(TAG, "电池优化权限-检查：开始，系统版本=" + Build.VERSION.SDK_INT);
        if (activity == null) {
            LogUtils.e(TAG, "电池优化权限-检查：失败，Activity为空");
            return false;
        }

        // API23以下无此权限，视为已拥有
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            LogUtils.d(TAG, "电池优化权限-检查：API23以下，无需校验，视为已拥有");
            return true;
        }

        // API23+ 精准校验权限状态
        PowerManager powerManager = (PowerManager) activity.getSystemService(Activity.POWER_SERVICE);
        if (powerManager == null) {
            LogUtils.e(TAG, "电池优化权限-检查：获取PowerManager失败，校验异常");
            return false;
        }
        boolean isIgnored = powerManager.isIgnoringBatteryOptimizations(activity.getPackageName());
        LogUtils.d(TAG, "电池优化权限-检查：结果=" + (isIgnored ? "已忽略优化" : "未忽略（需申请）"));
        return isIgnored;
    }

    /**
     * 请求忽略电池优化权限（多方案跳转，适配API29-30，自动判断是否需要申请）
     * @param activity 申请权限的Activity（不可为null）
     */
    public void requestIgnoreBatteryOptimizationPermission(Activity activity) {
        LogUtils.d(TAG, "电池优化权限-申请：开始处理");
        if (activity == null || activity.isFinishing()) {
            LogUtils.e(TAG, "电池优化权限-申请：失败，Activity无效/已销毁");
            return;
        }

        // 已拥有权限，直接返回
        if (checkIgnoreBatteryOptimizationPermission(activity)) {
            LogUtils.d(TAG, "电池优化权限-申请：已拥有权限，无需发起");
            return;
        }

        try {
            // 方案1：直接跳转一键授权页（优先使用，用户操作简单）
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, REQUEST_IGNORE_BATTERY_OPTIMIZATION);
            LogUtils.d(TAG, "电池优化权限-申请：跳转一键授权页");
        } catch (Exception e) {
            // 方案2：备用跳转优化管理页+提示
            Intent intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
            activity.startActivityForResult(intent, REQUEST_IGNORE_BATTERY_OPTIMIZATION);
            LogUtils.w(TAG, "电池优化权限-申请：跳转失败，引导手动操作");
            showBatteryOptTipsDialog(activity);
        }
    }

    // ====================== 辅助方法：手动开启提示弹窗（适配跳转失败场景）======================
    /**
     * 全文件管理权限手动开启提示弹窗
     */
    private void showAllFileManageTipsDialog(final Activity activity) {
        new AlertDialog.Builder(activity)
			.setTitle("全文件管理权限申请提示")
			.setMessage("请手动开启全文件管理权限，否则文件操作功能异常：\n1. 进入设置 → 应用 → 本应用 → 权限\n2. 找到「文件管理」/「存储」权限，开启「允许管理所有文件」")
			.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.setCancelable(false)
			.show();
        LogUtils.d(TAG, "全文件权限：显示手动开启提示弹窗");
    }

    /**
     * 自启动权限手动开启提示弹窗（小米专属）
     */
    private void showAutoStartTipsDialog(final Activity activity) {
        new AlertDialog.Builder(activity)
			.setTitle("自启动权限申请提示")
			.setMessage("请手动开启自启动权限，否则应用后台保活异常：\n1. 进入小米安全中心 → 应用管理 → 自启动管理\n2. 找到本应用，开启「允许自启动」开关")
			.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.setCancelable(false)
			.show();
        LogUtils.d(TAG, "自启动权限：显示手动开启提示弹窗");
    }

    /**
     * 电池优化权限手动开启提示弹窗
     */
    private void showBatteryOptTipsDialog(final Activity activity) {
        new AlertDialog.Builder(activity)
			.setTitle("电池优化权限申请提示")
			.setMessage("请手动忽略电池优化，否则应用后台运行被限制：\n1. 进入设置 → 电池 → 电池优化\n2. 找到本应用，选择「不优化」选项")
			.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.setCancelable(false)
			.show();
        LogUtils.d(TAG, "电池优化权限：显示手动开启提示弹窗");
    }

	public void startPermissionRequest(final Activity activity) {
		// 电池优化权限（通用所有机型）
		if (!checkIgnoreBatteryOptimizationPermission(activity)) {
			YesNoAlertDialog.show(activity, activity.getString(R.string.app_name) + "权限申请提示：", "本应用要正常使用，需要申请电池优化与自启动权限。是否进入权限设置步骤？", new YesNoAlertDialog.OnDialogResultListener(){
					@Override
					public void onNo() {
						ToastUtils.show(activity.getString(R.string.app_name) + "应用可能无法正常使用。");
					}
					@Override
					public void onYes() {
						requestIgnoreBatteryOptimizationPermission(activity);
					}
				});
		}
    }

	public void handlePermissionRequest(final Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == PermissionUtils.REQUEST_IGNORE_BATTERY_OPTIMIZATION) {
			// 自启动权限（小米专属）
			// 小米机型，发起自启动权限申请
			requestAutoStartPermission(activity);
		} else if (requestCode == PermissionUtils.REQUEST_AUTO_START) {
			// 自启动权限（小米专属）
			if (App.isDebugging() && !checkAllFileManagePermission(activity)) {
				// 小米机型，发起自启动权限申请
				requestAllFileManagePermission(activity);
			}
		}
    }
}

