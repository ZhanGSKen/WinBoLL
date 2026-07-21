package cc.winboll.studio.winboll.activities;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.models.APPInfo;
import cc.winboll.studio.libappbase.views.AboutView;
import cc.winboll.studio.winboll.MainActivity;
import cc.winboll.studio.winboll.R;
import android.app.Activity;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/13 11:54
 * @Describe 应用介绍窗口
 */
public class AboutActivity extends BaseWinBoLLActivity {

	@Override
	public Activity getActivity() {
		return this;
	}


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

		AboutView aboutView = getActivity().findViewById(R.id.aboutview);
		aboutView.setAPPInfo(genDefaultAppInfo());
    }

	private void initToolbar() {
        LogUtils.d(TAG, "initToolbar() 开始初始化");
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null) {
            LogUtils.e(TAG, "initToolbar() | Toolbar未找到");
            return;
        }
        setSupportActionBar(mToolbar);
        mToolbar.setSubtitle(getTag());
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(TAG, "导航栏 点击返回按钮");
					getActivity().finish();
				}
			});
        LogUtils.d(TAG, "initToolbar() 配置完成");
    }

	private APPInfo genDefaultAppInfo() {
        LogUtils.d(TAG, "genDefaultAppInfo() 调用");
        String branchName = "winboll";
        APPInfo appInfo = new APPInfo();
        appInfo.setAppName(getActivity().getString(R.string.app_name));
        appInfo.setAppIcon(R.drawable.ic_winboll);
        appInfo.setAppDescription(getActivity().getString(R.string.app_description));
        appInfo.setAppGitName("WinBoLL");
        appInfo.setAppGitOwner("Studio");
        appInfo.setAppGitAPPBranch(branchName);
        appInfo.setAppGitAPPSubProjectFolder(branchName);
        appInfo.setAppHomePage("https://www.winboll.cc/apks/index.php?project=WinBoLL");
        appInfo.setAppAPKName("WinBoLL");
        appInfo.setAppAPKFolderName("WinBoLL");
        LogUtils.d(TAG, "genDefaultAppInfo: 应用信息已生成");
        return appInfo;
    }
}
