package cc.winboll.studio.positions.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.models.APPInfo;
import cc.winboll.studio.libappbase.views.AboutView;
import cc.winboll.studio.positions.R;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/13 11:25
 * @Describe 应用介绍窗口
 */
public class AboutActivity extends WinBoLLActivity {

    public static final String TAG = "AboutActivity";
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
        setContentView(R.layout.activity_about);

		// 设置工具栏
        initToolbar();

		AboutView aboutView = findViewById(R.id.aboutview);
		aboutView.setAPPInfo(genDefaultAppInfo());
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
        mToolbar.setTitleTextAppearance(this, R.style.Toolbar_TitleText);
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

	private APPInfo genDefaultAppInfo() {
        LogUtils.d(TAG, "genDefaultAppInfo() 调用");
        String branchName = "positions";
        APPInfo appInfo = new APPInfo();
        appInfo.setAppName(getString(R.string.app_name));
        appInfo.setAppIcon(R.drawable.ic_winboll);
        appInfo.setAppDescription(getString(R.string.app_description));
        appInfo.setAppGitName("Positions");
        appInfo.setAppGitOwner("Studio");
        appInfo.setAppGitAPPBranch(branchName);
        appInfo.setAppGitAPPSubProjectFolder(branchName);
        appInfo.setAppHomePage("https://www.winboll.cc/apks/index.php?project=Positions");
        appInfo.setAppAPKName("Positions");
        appInfo.setAppAPKFolderName("Positions");
        LogUtils.d(TAG, "genDefaultAppInfo: 应用信息已生成");
        return appInfo;
    }
}
