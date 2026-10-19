package cc.winboll.studio.aes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cc.winboll.studio.libaes.views.ADsControlView;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/26 18:01
 * @Describe SettingsActivity
 */
public class SettingsActivity extends Activity {

    public static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ADsControlView adsControlView = (ADsControlView) findViewById(R.id.ads_control_view);
		
//		adsControlView.setOnAdsModeSelectedListener(new ADsControlView.OnAdsModeSelectedListener() {
//				@Override
//				public void onModeSelected(ADsMode selectedMode) {
//					if (selectedMode == ADsMode.STANDALONE) {
//						// 处理单机模式逻辑（如释放米盟资源）
//						ToastUtils.show("STANDALONE");
//					} else if (selectedMode == ADsMode.MIMO_SDK) {
//						// 处理米盟SDK模式逻辑（如初始化SDK）
//						ToastUtils.show("MIMO_SDK");
//					}
//				}
//			});
    }
	
}
