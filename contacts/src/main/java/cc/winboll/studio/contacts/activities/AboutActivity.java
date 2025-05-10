package cc.winboll.studio.contacts.activities;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/03/31 15:15:54
 * @Describe 应用介绍窗口
 */
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.libaes.winboll.APPInfo;
import cc.winboll.studio.libaes.winboll.AboutView;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.winboll.IWinBollActivity;

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
        String szBranchName = "contacts";
        APPInfo appInfo = new APPInfo();
        appInfo.setAppName("Contacts");
        appInfo.setAppIcon(cc.winboll.studio.libaes.R.drawable.ic_winboll);
        appInfo.setAppDescription("通讯录与拨号");
        appInfo.setAppGitName("APP");
        appInfo.setAppGitOwner("Studio");
        appInfo.setAppGitAPPBranch(szBranchName);
        appInfo.setAppGitAPPSubProjectFolder(szBranchName);
        appInfo.setAppHomePage("https://www.winboll.cc/studio/details.php?app=Contacts");
        appInfo.setAppAPKName("Contacts");
        appInfo.setAppAPKFolderName("Contacts");
        return new AboutView(mContext, appInfo);
    }
}
