package cc.winboll.studio.aes;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/26 18:01
 * @Describe SettingsActivity 【设置页：带主题工具栏的占位页，具体设置项后续补充】
 */
public class SettingsActivity extends BaseWinBoLLActivity {

    public static final String TAG = "SettingsActivity";

    private Toolbar mToolbar;

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 设置工具栏
        initToolbar();
    }

    private void initToolbar() {
        LogUtils.d(TAG, "initToolbar() 开始初始化");
        mToolbar = findViewById(R.id.toolbar);
        if (mToolbar == null) {
            LogUtils.e(TAG, "initToolbar() | Toolbar未找到");
            return;
        }
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getString(R.string.settings_title));
        mToolbar.setSubtitle(getTag());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtils.d(TAG, "导航栏 点击返回按钮");
                    WinBoLLActivityManager.getInstance().resumeActivity(MainActivity.class);
                    WinBoLLActivityManager.getInstance().finish(SettingsActivity.this);
                }
            });
        LogUtils.d(TAG, "initToolbar() 配置完成");
    }
}
