package cc.winboll.studio.gitsion;

import android.app.Activity;
import android.os.Bundle;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/05/07 15:39
 */
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.models.APPInfo;
import cc.winboll.studio.libappbase.views.AboutView;

public class AboutActivity extends AppCompatActivity {

    public static final String TAG = "AboutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});

        AboutView aboutView = findViewById(R.id.aboutview);
        aboutView.setAPPInfo(genDefaultAppInfo());
    }

    private APPInfo genDefaultAppInfo() {
        LogUtils.d(TAG, "genDefaultAppInfo() 调用");
        String branchName = "gpsrelaysentinel";
        APPInfo appInfo = new APPInfo();
        appInfo.setAppName("GPSRelaySentinel");
        appInfo.setAppIcon(R.drawable.ic_winboll);
        appInfo.setAppDescription(getString(R.string.app_description));
        appInfo.setAppGitName("WinBoLL");
        appInfo.setAppGitOwner("Studio");
        appInfo.setAppGitAPPBranch(branchName);
        appInfo.setAppGitAPPSubProjectFolder(branchName);
        appInfo.setAppHomePage("https://www.winboll.cc/apks/index.php?project=GPSRelaySentinel");
        appInfo.setAppAPKName("GPSRelaySentinel");
        appInfo.setAppAPKFolderName("GPSRelaySentinel");
        LogUtils.d(TAG, "genDefaultAppInfo: 应用信息已生成");
        return appInfo;
    }
}
