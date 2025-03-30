package cc.winboll.studio.mymessagemanager.activitys;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/14 13:20:33
 * @Describe 应用介绍窗口
 */
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libaes.winboll.APPInfo;
import cc.winboll.studio.libaes.winboll.AboutView;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.winboll.IWinBollActivity;
import cc.winboll.studio.mymessagemanager.R;
import android.view.MenuItem;

public class AboutActivity extends WinBollActivity implements IWinBollActivity {

    public static final String TAG = "AboutActivity";

    Context mContext;
    Toolbar mToolbar;

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
        mContext = this;
        setContentView(R.layout.activity_about);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setSubtitle(TAG);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AboutView aboutView = CreateAboutView();
        // 在 Activity 的 onCreate 或其他生命周期方法中调用
//        LinearLayout layout = new LinearLayout(this);
//        layout.setOrientation(LinearLayout.VERTICAL);
//        // 创建布局参数（宽度和高度）
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.MATCH_PARENT
//        );
//        addContentView(aboutView, params);

        LinearLayout layout = findViewById(R.id.aboutviewroot_ll);
        // 创建布局参数（宽度和高度）
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        );
        layout.addView(aboutView, params);

        GlobalApplication.getWinBollActivityManager().add(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GlobalApplication.getWinBollActivityManager().registeRemove(this);
    }

    public AboutView CreateAboutView() {
        String szBranchName = "mymessagemanager";
        APPInfo appInfo = new APPInfo();
        appInfo.setAppName(getString(R.string.app_name));
        appInfo.setAppIcon(cc.winboll.studio.libaes.R.drawable.ic_winboll);
        appInfo.setAppDescription(getString(R.string.app_description));
        appInfo.setAppGitName("APP");
        appInfo.setAppGitOwner("Studio");
        appInfo.setAppGitAPPBranch(szBranchName);
        appInfo.setAppGitAPPSubProjectFolder(szBranchName);
        appInfo.setAppHomePage("https://www.winboll.cc/studio/details.php?app=MyMessageManager");
        appInfo.setAppAPKName("MyMessageManager");
        appInfo.setAppAPKFolderName("MyMessageManager");
        return new AboutView(mContext, appInfo);
    }
}
