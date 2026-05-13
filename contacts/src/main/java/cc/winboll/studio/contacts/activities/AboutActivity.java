package cc.winboll.studio.contacts.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import cc.winboll.studio.contacts.R;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libaes.models.APPInfo;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.libaes.views.AboutView;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/03/31 15:15:54
 * @Describe 应用介绍窗口
 */
public class AboutActivity extends WinBollActivity implements IWinBoLLActivity {

    // ====================== 常量定义区 ======================
    public static final String TAG = "AboutActivity";
    private static final String BRANCH_NAME = "contacts";

    // ====================== 成员变量区 ======================
    private Context mContext;
    private Toolbar mToolbar;

    // ====================== 接口实现区 ======================
    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    // ====================== 生命周期函数区 ======================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate: 关于页面开始创建");

        mContext = this;
        setContentView(R.layout.activity_about);

        // 初始化工具栏
        initToolbar();
        // 初始化关于页面视图
        initAboutView();
        // 注册Activity管理
        WinBoLLActivityManager.getInstance().add(this);

        LogUtils.d(TAG, "onCreate: 关于页面初始化完成");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy: 关于页面开始销毁");
        WinBoLLActivityManager.getInstance().registeRemove(this);
        LogUtils.d(TAG, "onDestroy: 关于页面销毁完成");
    }

    // ====================== 控件初始化函数区 ======================
    private void initToolbar() {
        LogUtils.d(TAG, "initToolbar: 初始化工具栏");
        // Java7 适配：添加强制类型转换
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setSubtitle(TAG);
        // 非空判断，避免空指针异常
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initAboutView() {
        LogUtils.d(TAG, "initAboutView: 初始化关于页面内容视图");
        AboutView aboutView = createAboutView();
        LinearLayout layout = (LinearLayout) findViewById(R.id.aboutviewroot_ll);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.MATCH_PARENT
        );
        layout.addView(aboutView, params);
        LogUtils.d(TAG, "initAboutView: AboutView已添加到布局");
    }

    // ====================== 业务逻辑函数区 ======================
    private AboutView createAboutView() {
        LogUtils.d(TAG, "createAboutView: 构建APP信息并创建AboutView");
        APPInfo appInfo = new APPInfo();
        appInfo.setAppName("Contacts");
        appInfo.setAppIcon(cc.winboll.studio.libaes.R.drawable.ic_winboll);
        appInfo.setAppDescription("这是可以根据正则表达式匹配拦截骚扰电话的手机拨号应用。");
        appInfo.setAppGitName("WinBoLL");
        appInfo.setAppGitOwner("Studio");
        appInfo.setAppGitAPPBranch(BRANCH_NAME);
        appInfo.setAppGitAPPSubProjectFolder(BRANCH_NAME);
        appInfo.setAppHomePage("https://www.winboll.cc/apks/index.php?project=Contacts");
        appInfo.setAppAPKName("Contacts");
        appInfo.setAppAPKFolderName("Contacts");

        return new AboutView(mContext, appInfo);
    }
}

