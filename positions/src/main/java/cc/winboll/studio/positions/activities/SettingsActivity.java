package cc.winboll.studio.positions.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.positions.R;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/12/07 23:29
 * @Describe 应用设置活动窗口
 */
public class SettingsActivity extends WinBoLLActivity implements IWinBoLLActivity {

    public static final String TAG = "SettingsActivity";

	private Toolbar mToolbar;

	@Override
	public Activity getActivity() {
		return this;
	}

	@Override
	public String getTag() {
		return TAG;
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

		mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
		mToolbar.setSubtitle(getTag());
        mToolbar.setTitleTextAppearance(this, R.style.Toolbar_TitleText);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(TAG, "【导航栏】点击返回");
					finish();
				}
			});
    }
}
