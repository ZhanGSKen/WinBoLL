package cc.winboll.studio.winboll.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.winboll.App;
import cc.winboll.studio.winboll.R;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/27 08:56
 * @Describe APPPlusUtils
 */
public class APPPlusUtils {
    public static final String TAG = "APPPlusUtils";

    // 快捷方式配置（名称+图标，需与实际资源匹配）
//    private static final String PLUS_SHORTCUT_NAME = "位置服务-Laojun";
//    private static final int PLUS_SHORTCUT_ICON = R.mipmap.ic_launcher; // Laojun 图标资源

    /**
     * 添加Plus组件与图标
     */
    public static boolean switchAppLauncherToComponent(Context context, String componentName) {
        if (context == null) {
            LogUtils.d(TAG, "切换失败：上下文为空");
            Toast.makeText(context, context.getString(R.string.app_name) + "图标切换失败", Toast.LENGTH_SHORT).show();
            return false;
        }

        PackageManager pm = context.getPackageManager();

        ComponentName plusComponentSwitchTo = new ComponentName(context, componentName);
        ComponentName plusComponentEN1 = new ComponentName(context, App.COMPONENT_EN1);
        ComponentName plusComponentCN1 = new ComponentName(context, App.COMPONENT_CN1);
        ComponentName plusComponentCN2 = new ComponentName(context, App.COMPONENT_CN2);

        try {
			disableComponent(pm, plusComponentEN1);
			disableComponent(pm, plusComponentCN1);
			disableComponent(pm, plusComponentCN2);
            enableComponent(pm, plusComponentSwitchTo);

            return true;

        } catch (Exception e) {
            LogUtils.e(TAG, "图标切换失败：" + e.getMessage());
            Toast.makeText(context, context.getString(R.string.app_name) + "图标切换失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * 创建指定组件的桌面快捷方式（自动去重，兼容 Android 8.0+）
     * @param component 目标组件（如 LAOJUN_ACTIVITY）
     * @param name 快捷方式名称
     * @param iconRes 快捷方式图标资源ID
     * @return 是否创建成功
     */
    private static boolean createComponentShortcut(Context context, ComponentName component, String name, int iconRes) {
        if (context == null || component == null || name == null || iconRes == 0) {
            LogUtils.d(TAG, "快捷方式创建失败：参数为空");
            return false;
        }

        // Android 8.0+（API 26+）：使用 ShortcutManager（系统推荐）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                PackageManager pm = context.getPackageManager();
                android.content.pm.ShortcutManager shortcutManager = context.getSystemService(android.content.pm.ShortcutManager.class);
                if (shortcutManager == null || !shortcutManager.isRequestPinShortcutSupported()) {
                    LogUtils.d(TAG, "系统不支持创建快捷方式");
                    return false;
                }

                // 检查是否已存在该组件的快捷方式（去重）
                for (android.content.pm.ShortcutInfo info : shortcutManager.getPinnedShortcuts()) {
                    if (component.getClassName().equals(info.getIntent().getComponent().getClassName())) {
                        LogUtils.d(TAG, "快捷方式已存在：" + component.getClassName());
                        return true;
                    }
                }

                // 构建启动目标组件的意图
                Intent launchIntent = new Intent(Intent.ACTION_MAIN)
					.setComponent(component)
					.addCategory(Intent.CATEGORY_LAUNCHER)
					.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                // 构建快捷方式信息
                android.content.pm.ShortcutInfo shortcutInfo = new android.content.pm.ShortcutInfo.Builder(context, component.getClassName())
					.setShortLabel(name)
					.setLongLabel(name)
					.setIcon(android.graphics.drawable.Icon.createWithResource(context, iconRes))
					.setIntent(launchIntent)
					.build();

                // 请求创建快捷方式（需用户确认）
                shortcutManager.requestPinShortcut(shortcutInfo, null);
                return true;

            } catch (Exception e) {
                LogUtils.d(TAG, "Android O+ 快捷方式创建失败：" + e.getMessage());
                return false;
            }
        } else {
            // Android 8.0 以下：使用广播（兼容旧机型）
            try {
                // 构建启动目标组件的意图
                Intent launchIntent = new Intent(Intent.ACTION_MAIN)
					.setComponent(component)
					.addCategory(Intent.CATEGORY_LAUNCHER)
					.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                // 构建创建快捷方式的广播意图
                Intent installIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
                installIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
                installIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
                installIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
									   Intent.ShortcutIconResource.fromContext(context, iconRes));
                installIntent.putExtra("duplicate", false); // 禁止重复创建

                context.sendBroadcast(installIntent);
                return true;

            } catch (Exception e) {
                LogUtils.d(TAG, "Android O- 快捷方式创建失败：" + e.getMessage());
                return false;
            }
        }
    }

    /**
     * 启用组件（带状态检查，避免重复操作）
     */
    private static void enableComponent(PackageManager pm, ComponentName component) {
        if (pm.getComponentEnabledSetting(component) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            pm.setComponentEnabledSetting(
				component,
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP | PackageManager.SYNCHRONOUS
            );
        }
    }

    /**
     * 禁用组件（带状态检查，避免重复操作）
     */
    private static void disableComponent(PackageManager pm, ComponentName component) {
        if (pm.getComponentEnabledSetting(component) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            pm.setComponentEnabledSetting(
				component,
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP | PackageManager.SYNCHRONOUS
            );
        }
    }
}

