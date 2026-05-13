package cc.winboll.studio.libaes.utils;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import cc.winboll.studio.libaes.R;
import cc.winboll.studio.libappbase.LogActivity;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/12/07 13:31
 * @Describe 应用开发工具类
 */
public class DevelopUtils {

    public static final String TAG = "DevelopUtils";

	public static <T extends Activity> void inflateMenu(T activity, Menu menu) {
        activity.getMenuInflater().inflate(R.menu.toolbar_appdebug, menu);
    }
	
	public static <T extends Activity> boolean onDevelopItemSelected(T activity, MenuItem item) {
		if (R.id.item_testappcrash == item.getItemId()) {
            for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++) {
				activity.getString(i);
            }
        } else if (R.id.item_log == item.getItemId()) {
			//ToastUtils.show("Test");
            LogActivity.startLogActivity(activity);
        } else {
			return false;
		}
		return true;
	}
}
