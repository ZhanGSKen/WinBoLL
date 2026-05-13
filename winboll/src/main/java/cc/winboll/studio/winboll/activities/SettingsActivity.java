package cc.winboll.studio.winboll.activities;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libaes.utils.AESThemeUtil;
import cc.winboll.studio.winboll.R;
import android.app.Activity;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/12/05 18:48
 * @Describe Settings Activity
 */
public class SettingsActivity extends BaseWinBoLLActivity {

	@Override
	public Activity getActivity() {
		return this;
	}


    public static final String TAG = "SettingsActivity";

	@Override
	public String getTag() {
		return TAG;
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AESThemeUtil.applyAppTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
		
		// 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
        getSupportActionBar().setSubtitle(TAG);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish(); // 点击导航栏返回按钮，触发 finish()
                }
            });

    }

}
