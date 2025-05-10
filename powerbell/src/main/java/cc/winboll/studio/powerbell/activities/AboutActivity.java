package cc.winboll.studio.powerbell.activities;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/03/25 01:16:32
 * @Describe 应用介绍窗口
 */
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import cc.winboll.studio.libaes.views.AToolbar;
import cc.winboll.studio.libaes.winboll.APPInfo;
import cc.winboll.studio.libaes.winboll.AboutView;
import cc.winboll.studio.powerbell.R;

public class AboutActivity extends Activity {

    Context mContext;

    public static final String TAG = "AboutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        mContext = this;

        // 初始化工具栏
        AToolbar mAToolbar = (AToolbar) findViewById(R.id.toolbar);
        setActionBar(mAToolbar);
        mAToolbar.setSubtitle(getString(R.string.text_about));
        //mAToolbar.setTitleTextAppearance(this, R.style.Toolbar_TitleText);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        AboutView aboutView = CreateAboutView();
        // 在 Activity 的 onCreate 或其他生命周期方法中调用
        LinearLayout llRoot = findViewById(R.id.root_ll);
        //layout.setOrientation(LinearLayout.VERTICAL);
        // 创建布局参数（宽度和高度）
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        );
        llRoot.addView(aboutView, params);

    }

    public AboutView CreateAboutView() {
        String szBranchName = "powerbell";
        APPInfo appInfo = new APPInfo();
        appInfo.setAppName(getString(R.string.app_name));
        appInfo.setAppIcon(R.drawable.ic_launcher);
        appInfo.setAppDescription(getString(R.string.app_description));
        appInfo.setAppGitName("APP");
        appInfo.setAppGitOwner("Studio");
        appInfo.setAppGitAPPBranch(szBranchName);
        appInfo.setAppGitAPPSubProjectFolder(szBranchName);
        appInfo.setAppHomePage("https://www.winboll.cc/studio/details.php?app=PowerBell");
        appInfo.setAppAPKName("PowerBell");
        appInfo.setAppAPKFolderName("PowerBell");
        return new AboutView(mContext, appInfo);
    }
}
