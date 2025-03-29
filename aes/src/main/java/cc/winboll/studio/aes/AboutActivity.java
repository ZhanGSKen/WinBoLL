package cc.winboll.studio.aes;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/24 23:52:29
 * @Describe AES应用介绍窗口
 */
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import cc.winboll.studio.libaes.winboll.APPInfo;
import cc.winboll.studio.libaes.winboll.AboutView;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.winboll.IWinBollActivity;
import android.app.Activity;

public class AboutActivity extends AppCompatActivity implements IWinBollActivity {

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
        mContext = this;

        AboutView aboutView = CreateAboutView();
        // 在 Activity 的 onCreate 或其他生命周期方法中调用
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        // 创建布局参数（宽度和高度）
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        );
        addContentView(aboutView, params);

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
}
