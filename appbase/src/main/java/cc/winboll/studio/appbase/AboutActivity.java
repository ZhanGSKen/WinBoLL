package cc.winboll.studio.appbase;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toolbar;
import cc.winboll.studio.appbase.R;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.models.APPInfo;
import cc.winboll.studio.libappbase.views.AboutView;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/11 12:55
 * @Describe AboutActivity
 */
public class AboutActivity extends Activity {

    public static final String TAG = "AboutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);

		AboutView aboutView = findViewById(R.id.aboutview);
		aboutView.setAPPInfo(genDefaultAppInfo());
    }

	private APPInfo genDefaultAppInfo() {
        LogUtils.d(TAG, "genDefaultAppInfo() 调用");
        String branchName = "appbase";
        APPInfo appInfo = new APPInfo();
        appInfo.setAppName("APPBase");
        appInfo.setAppIcon(R.drawable.ic_winboll);
        appInfo.setAppDescription(getString(R.string.app_description));
        appInfo.setAppGitName("WinBoLL");
        appInfo.setAppGitOwner("Studio");
        appInfo.setAppGitAPPBranch(branchName);
        appInfo.setAppGitAPPSubProjectFolder(branchName);
        appInfo.setAppHomePage("https://www.winboll.cc/apks/index.php?project=APPBase");
        appInfo.setAppAPKName("APPBase");
        appInfo.setAppAPKFolderName("APPBase");
        LogUtils.d(TAG, "genDefaultAppInfo: 应用信息已生成");
        return appInfo;
    }
}
