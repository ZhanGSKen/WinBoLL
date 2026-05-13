package cc.winboll.studio.powerbell.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.powerbell.App;
import cc.winboll.studio.powerbell.R;
import cc.winboll.studio.powerbell.utils.APPPlusUtils;

/**
 * 应用快捷方式活动类，处理应用图标快捷菜单的切换请求
 * 适配 API30，基于 Java7 开发
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/15 13:45
 * @Describe 应用快捷方式活动类
 */
public class ShortcutActionActivity extends Activity {
    // ======================== 静态常量 =========================
    public static final String TAG = "ShortcutActionActivity";
    // 快捷指令常量
    private static final String ACTION_SWITCH_TO_EN1 = "switchto_en1";
    private static final String ACTION_SWITCH_TO_CN1 = "switchto_cn1";
    private static final String ACTION_SWITCH_TO_CN2 = "switchto_cn2";

    // ======================== 生命周期方法 =========================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "【onCreate】ShortcutActionActivity 启动，开始处理快捷方式请求");

        // 处理应用图标快捷菜单的切换请求
        handleSwitchRequest();

        LogUtils.d(TAG, "【onCreate】快捷方式请求处理完成，关闭活动");
        finish();
    }

    // ======================== 业务逻辑方法 =========================
    /**
     * 处理应用图标快捷菜单的请求，根据意图数据切换应用启动组件
     */
    private void handleSwitchRequest() {
        Intent intent = getIntent();
        if (intent == null) {
            LogUtils.w(TAG, "【handleSwitchRequest】意图为空，无法处理快捷方式请求");
            return;
        }

        String dataString = intent.getDataString();
        LogUtils.d(TAG, "【handleSwitchRequest】获取到快捷指令：" + dataString);

        // 匹配快捷指令并切换组件
        if (ACTION_SWITCH_TO_EN1.equals(dataString)) {
            APPPlusUtils.switchAppLauncherToComponent(this, App.COMPONENT_EN1);
            String toastMsg = "切换至" + getString(R.string.app_name) + "图标";
            ToastUtils.show(toastMsg);
            LogUtils.d(TAG, "【handleSwitchRequest】已切换至EN1组件：" + App.COMPONENT_EN1);
        } else if (ACTION_SWITCH_TO_CN1.equals(dataString)) {
            APPPlusUtils.switchAppLauncherToComponent(this, App.COMPONENT_CN1);
            String toastMsg = "切换至" + getString(R.string.app_name_cn1) + "图标";
            ToastUtils.show(toastMsg);
            LogUtils.d(TAG, "【handleSwitchRequest】已切换至CN1组件：" + App.COMPONENT_CN1);
        } else if (ACTION_SWITCH_TO_CN2.equals(dataString)) {
            APPPlusUtils.switchAppLauncherToComponent(this, App.COMPONENT_CN2);
            String toastMsg = "切换至" + getString(R.string.app_name_cn2) + "图标";
            ToastUtils.show(toastMsg);
            LogUtils.d(TAG, "【handleSwitchRequest】已切换至CN2组件：" + App.COMPONENT_CN2);
        } else {
            LogUtils.w(TAG, "【handleSwitchRequest】未匹配到有效快捷指令：" + dataString);
        }
    }
}

