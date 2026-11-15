package cc.winboll.studio.aes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.aes.R;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.models.APPInfo;
import cc.winboll.studio.libappbase.views.AboutView;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/13 11:25
 * @Describe 应用介绍窗口
 */
public class AboutActivity extends BaseWinBoLLActivity {

    public static final String TAG = "AboutActivity";
	
	private Toolbar mToolbar;

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

		// 开源许可入口放在介绍页底部，不再单独占一个菜单项
		TextView licenseButton = findViewById(R.id.about_license_button);
		if (licenseButton != null) {
			licenseButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						LogUtils.d(TAG, "about_license_button 点击，打开开源许可页");
						startActivity(new Intent(AboutActivity.this, OpenSourceLicenseActivity.class));
					}
				});
		} else {
			LogUtils.e(TAG, "开源许可入口未找到");
		}
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
					WinBoLLActivityManager.getInstance().resumeActivity(MainActivity.class);
					WinBoLLActivityManager.getInstance().finish(AboutActivity.this);
				}
			});
        LogUtils.d(TAG, "initToolbar() 配置完成");
    }

	private APPInfo genDefaultAppInfo() {
        LogUtils.d(TAG, "genDefaultAppInfo() 调用");
        String branchName = "aes";
        APPInfo appInfo = new APPInfo();
        appInfo.setAppName(getString(R.string.app_name));
        appInfo.setAppIcon(R.drawable.ic_winboll);
        appInfo.setAppDescription(getString(R.string.app_description));
        appInfo.setAppGitName("WinBoLL");
        appInfo.setAppGitOwner("Studio");
        appInfo.setAppGitAPPBranch(branchName);
        appInfo.setAppGitAPPSubProjectFolder(branchName);
        appInfo.setAppHomePage("https://www.winboll.cc/apks/index.php?project=AES");
        appInfo.setAppAPKName("AES");
        appInfo.setAppAPKFolderName("AES");
        LogUtils.d(TAG, "genDefaultAppInfo: 应用信息已生成");
        return appInfo;
    }
}
