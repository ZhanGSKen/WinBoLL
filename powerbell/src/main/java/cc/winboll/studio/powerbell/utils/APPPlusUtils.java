package cc.winboll.studio.powerbell.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.App;
import cc.winboll.studio.powerbell.R;

/**
 * 应用图标切换工具类（启用组件时创建对应快捷方式）
 * 适配：Java7 | API30 | 高低版本快捷方式创建兼容
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Describe 应用启动器组件切换与桌面快捷方式创建工具，支持多组件管理与版本兼容
 */
public class APPPlusUtils {
    // ======================== 静态常量区（魔法值与标签管理）========================
    public static final String TAG = "APPPlusUtils";
    private static final int SHORTCUT_ICON_DEFAULT = R.drawable.ic_launcher; // 默认快捷方式图标
    private static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT"; // 旧版快捷方式广播Action

    // ======================== 公共业务方法区（对外核心接口）========================
    /**
     * 切换应用启动器组件（禁用其他组件，启用目标组件）
     * @param context 上下文
     * @param componentName 目标组件完整类名
     * @return 切换是否成功
     */
    public static boolean switchAppLauncherToComponent(Context context, String componentName) {
        LogUtils.d(TAG, String.format("switchAppLauncherToComponent调用 | 传入组件名=%s", componentName));

        // 参数校验
        if (context == null) {
            LogUtils.e(TAG, "switchAppLauncherToComponent失败：上下文为空");
            return false;
        }
        if (componentName == null || componentName.isEmpty()) {
            LogUtils.e(TAG, "switchAppLauncherToComponent失败：组件名为空");
            return false;
        }

        PackageManager pm = context.getPackageManager();
        ComponentName targetComponent = new ComponentName(context, componentName);
        ComponentName en1Component = new ComponentName(context, App.COMPONENT_EN1);
        ComponentName cn1Component = new ComponentName(context, App.COMPONENT_CN1);
        ComponentName cn2Component = new ComponentName(context, App.COMPONENT_CN2);

        try {
            // 禁用所有其他启动器组件
            disableComponent(pm, en1Component);
            disableComponent(pm, cn1Component);
            disableComponent(pm, cn2Component);
            // 启用目标组件
            enableComponent(pm, targetComponent);

            LogUtils.d(TAG, String.format("switchAppLauncherToComponent成功 | 目标组件=%s", componentName));
            Toast.makeText(context, context.getString(R.string.app_name) + "图标切换成功", Toast.LENGTH_SHORT).show();
            return true;

        } catch (Exception e) {
            LogUtils.e(TAG, String.format("switchAppLauncherToComponent失败 | 异常信息=%s", e.getMessage()), e);
            Toast.makeText(context, context.getString(R.string.app_name) + "图标切换失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // ======================== 私有辅助方法区（组件状态控制）========================
    /**
     * 启用组件（带状态检查，避免重复操作）
     * @param pm 包管理器
     * @param component 目标组件
     */
    private static void enableComponent(PackageManager pm, ComponentName component) {
        int currentState = pm.getComponentEnabledSetting(component);
        String componentName = component.getClassName();

        if (currentState != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            pm.setComponentEnabledSetting(
				component,
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP | PackageManager.SYNCHRONOUS
            );
            LogUtils.d(TAG, String.format("enableComponent成功 | 组件=%s", componentName));
        } else {
            LogUtils.d(TAG, String.format("enableComponent无需操作 | 组件已启用=%s", componentName));
        }
    }

    /**
     * 禁用组件（带状态检查，避免重复操作）
     * @param pm 包管理器
     * @param component 目标组件
     */
    private static void disableComponent(PackageManager pm, ComponentName component) {
        int currentState = pm.getComponentEnabledSetting(component);
        String componentName = component.getClassName();

        if (currentState != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            pm.setComponentEnabledSetting(
				component,
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP | PackageManager.SYNCHRONOUS
            );
            LogUtils.d(TAG, String.format("disableComponent成功 | 组件=%s", componentName));
        } else {
            LogUtils.d(TAG, String.format("disableComponent无需操作 | 组件已禁用=%s", componentName));
        }
    }

    // ======================== 私有辅助方法区（快捷方式创建）========================
    /**
     * 创建指定组件的桌面快捷方式（自动去重，兼容 Android 8.0+）
     * @param context 上下文
     * @param component 目标组件
     * @param name 快捷方式名称
     * @param iconRes 快捷方式图标资源ID
     * @return 是否创建成功
     */
    private static boolean createComponentShortcut(Context context, ComponentName component, String name, int iconRes) {
        // 参数校验
        String componentName = component != null ? component.getClassName() : "null";
        LogUtils.d(TAG, String.format("createComponentShortcut调用 | 组件=%s | 名称=%s", componentName, name));

        if (context == null || component == null || name == null || name.isEmpty()) {
            LogUtils.e(TAG, "createComponentShortcut失败：上下文、组件或名称为空");
            return false;
        }

        // 图标资源默认值补全
        int finalIconRes = iconRes != 0 ? iconRes : SHORTCUT_ICON_DEFAULT;

        // Android 8.0+（API 26+）：使用 ShortcutManager（系统推荐）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                android.content.pm.ShortcutManager shortcutManager = context.getSystemService(android.content.pm.ShortcutManager.class);
                if (shortcutManager == null || !shortcutManager.isRequestPinShortcutSupported()) {
                    LogUtils.w(TAG, "createComponentShortcut：系统不支持创建快捷方式");
                    return false;
                }

                // 检查是否已存在该组件的快捷方式（去重）
                for (android.content.pm.ShortcutInfo info : shortcutManager.getPinnedShortcuts()) {
                    if (component.getClassName().equals(info.getIntent().getComponent().getClassName())) {
                        LogUtils.d(TAG, String.format("createComponentShortcut：快捷方式已存在=%s", componentName));
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
					.setIcon(android.graphics.drawable.Icon.createWithResource(context, finalIconRes))
					.setIntent(launchIntent)
					.build();

                // 请求创建快捷方式（需用户确认）
                shortcutManager.requestPinShortcut(shortcutInfo, null);
                LogUtils.d(TAG, "createComponentShortcut：Android O+ 快捷方式创建请求已发送");
                return true;

            } catch (Exception e) {
                LogUtils.e(TAG, String.format("createComponentShortcut失败 | Android O+ 异常=%s", e.getMessage()), e);
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
                Intent installIntent = new Intent(ACTION_INSTALL_SHORTCUT);
                installIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
                installIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
                installIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
									   Intent.ShortcutIconResource.fromContext(context, finalIconRes));
                installIntent.putExtra("duplicate", false); // 禁止重复创建

                context.sendBroadcast(installIntent);
                LogUtils.d(TAG, "createComponentShortcut：Android O- 快捷方式创建广播已发送");
                return true;

            } catch (Exception e) {
                LogUtils.e(TAG, String.format("createComponentShortcut失败 | Android O- 异常=%s", e.getMessage()), e);
                return false;
            }
        }
    }
}

