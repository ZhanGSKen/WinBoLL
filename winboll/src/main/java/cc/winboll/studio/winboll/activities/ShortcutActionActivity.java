package cc.winboll.studio.winboll.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.winboll.App;
import cc.winboll.studio.winboll.R;
import cc.winboll.studio.winboll.utils.APPPlusUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/27 09:00
 * @Describe 应用快捷方式活动类
 */
public class ShortcutActionActivity extends Activity {

    public static final String TAG = "ShortcutActionActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 处理应用级别的切换请求
        handleSwitchRequest();
		finish();
	}

	/**
     * 处理应用图标快捷菜单的请求
     */
    private void handleSwitchRequest() {
		//ToastUtils.show("handleSwitchRequest");
        Intent intent = getIntent();
        if (intent != null && "switchto_en1".equals(intent.getDataString())) {
			APPPlusUtils.switchAppLauncherToComponent(this, App.COMPONENT_EN1);
            ToastUtils.show("切换至" + getString(R.string.app_name) + "图标");
			//moveTaskToBack(true);
        }
		if (intent != null && "switchto_cn1".equals(intent.getDataString())) {
			APPPlusUtils.switchAppLauncherToComponent(this, App.COMPONENT_CN1);
            ToastUtils.show("切换至" + getString(R.string.app_name_cn1) + "图标");
			//moveTaskToBack(true);
        }
		if (intent != null && "switchto_cn2".equals(intent.getDataString())) {
			APPPlusUtils.switchAppLauncherToComponent(this, App.COMPONENT_CN2);
            ToastUtils.show("切换至" + getString(R.string.app_name_cn2) + "图标");
			//moveTaskToBack(true);
        }
    }
}
