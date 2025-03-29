package cc.winboll.studio.mymessagemanager.activitys;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/14 13:20:33
 * @Describe 应用关于对话窗口
 */
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import cc.winboll.studio.libaes.utils.AESThemeUtil;
import cc.winboll.studio.libaes.winboll.APPInfo;
import cc.winboll.studio.libaes.winboll.AboutView;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.winboll.IWinBollActivity;
import cc.winboll.studio.mymessagemanager.R;

public class AboutActivity extends BaseActivity implements IWinBollActivity {

    public static final String TAG = "AboutActivity";

    Context mContext;

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
        setTheme(AESThemeUtil.getThemeTypeID(getApplicationContext()));
        //setContentView(R.layout.activity_about);
        mContext = this;
        
        LinearLayout layout = findViewById(R.id.aboutmain_ll);

        AboutView aboutView = CreateAboutView();
        layout.addView(aboutView);
        // 在 Activity 的 onCreate 或其他生命周期方法中调用
//        LinearLayout layout = new LinearLayout(this);
//        layout.setOrientation(LinearLayout.VERTICAL);
//        // 创建布局参数（宽度和高度）
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.MATCH_PARENT
//        );
//        addContentView(aboutView, params);

        GlobalApplication.getWinBollActivityManager().add(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GlobalApplication.getWinBollActivityManager().registeRemove(this);
    }

    public AboutView CreateAboutView() {
        String szBranchName = "aes";
        APPInfo appInfo = new APPInfo();
        appInfo.setAppName("AES");
        appInfo.setAppIcon(cc.winboll.studio.libaes.R.drawable.ic_winboll);
        appInfo.setAppDescription("AES Description");
        appInfo.setAppGitName("APP");
        appInfo.setAppGitOwner("Studio");
        appInfo.setAppGitAPPBranch(szBranchName);
        appInfo.setAppGitAPPSubProjectFolder(szBranchName);
        appInfo.setAppHomePage("https://www.winboll.cc/studio/details.php?app=AES");
        appInfo.setAppAPKName("AES");
        appInfo.setAppAPKFolderName("AES");
        return new AboutView(mContext, appInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_about, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*if (item.getItemId() == R.id.item_help) {
         ToastUtils.show("R.id.item_help");
         } else */if (item.getItemId() == android.R.id.home) {
            GlobalApplication.getWinBollActivityManager().finish(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(mContext.getString(R.string.text_about) + mContext.getString(R.string.app_name));
    }
}
