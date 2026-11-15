package cc.winboll.studio.aes;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/07/16
 * @Describe SMTP邮件设置窗口，使用SMTPConfigView控件进行配置操作
 */
public class SMTPSettingsActivity extends BaseWinBoLLActivity {

    public static final String TAG = "SMTPSettingsActivity";

    private Toolbar mToolbar;

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smtp_settings);

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
        mToolbar.setSubtitle(getTag());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtils.d(TAG, "导航栏 点击返回按钮");
                    finish();
                }
            });
        LogUtils.d(TAG, "initToolbar() 配置完成");
    }
}
