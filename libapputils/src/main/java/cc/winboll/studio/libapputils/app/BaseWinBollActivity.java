package cc.winboll.studio.libapputils.app;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/08/12 14:32:08
 * @Describe WinBoll 活动窗口基础类
 */
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libapputils.R;
import cc.winboll.studio.libapputils.activities.AboutActivity;
import cc.winboll.studio.libapputils.log.LogActivity;
import cc.winboll.studio.libapputils.log.LogUtils;
import cc.winboll.studio.libapputils.view.AboutView;
import cc.winboll.studio.libapputils.view.YesNoAlertDialog;
import com.hjq.toast.ToastUtils;
import java.util.List;
import java.util.Set;

public class BaseWinBollActivity extends AppCompatActivity implements IWinBollActivity {

    public static final String TAG = "BaseWinBollActivity";

    public static final int REQUEST_LOG_ACTIVITY = 0;

    //IWinBollActivity mIWinBollActivity;
    AppCompatActivity mCurrentAppCompatActivity;

    Toolbar mToolBar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCurrentAppCompatActivity();
        checkResolveActivity();
        LogUtils.d(TAG, "onCreate");
        //super.onCreate(savedInstanceState);
        archiveInstance();
    }

    @Override
    public void onDestroy() {
        WinBollActivityManager.getInstance(mCurrentAppCompatActivity).registeRemove(this);
        super.onDestroy();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
    }

    @Override
    public AppCompatActivity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public Toolbar initToolBar() {
        return null;
    }

    @Override
    public boolean isEnableDisplayHomeAsUp() {
        return false;
    }

    @Override
    public boolean isAddWinBollToolBar() {
        return false;
    }

    public void initCurrentAppCompatActivity() {
        mCurrentAppCompatActivity = getActivity();
    }

    boolean checkResolveActivity() {
        PackageManager packageManager = mCurrentAppCompatActivity.getPackageManager();
        //Intent intent = new Intent("your_action_here");
        Intent intent = mCurrentAppCompatActivity.getIntent();
        if (intent != null) {
            List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfoList.size() > 0) {
                // 传入的Intent action在Activity清单的intent-filter的action节点里有定义
                if (intent.getAction() != null) {
                    if (intent.getAction().equals(cc.winboll.studio.libapputils.intent.action.DEBUGVIEW)) {
                        WinBollGlobalApplication.setIsDebug(true);
                        //ToastUtils.show!("WinBollApplication.setIsDebug(true) by action : " + intent.getAction());

                    }
                }
                return true;
            } else {
                // 传入的Intent action在Activity清单的intent-filter的action节点里没有定义
                //ToastUtils.show("false : " + intent.getAction());
                return false;
            }

        }

        // action在清单文件中没有声明
        ToastUtils.show("false");
        return false;
    }

    void archiveInstance() {
        Intent intent = mCurrentAppCompatActivity.getIntent();
        StringBuilder sb = new StringBuilder("\n### Archive Instance ###\n");

        if (intent != null) {
            ComponentName componentName = intent.getComponent();
            if (componentName != null) {
                String packageName = componentName.getPackageName();
                //Log.d("AppStarter", "启动本应用的应用包名: " + packageName);
                sb.append("启动本应用的应用包名: \n" + packageName);
            }

            sb.append("\nImplicit Intent Tracker ：\n接收到的 Intent 动作: \n" + intent.getAction());
            Set<String> categories = intent.getCategories();
            if (categories != null) {
                for (String category : categories) {
                    sb.append("\n接收到的 Intent 类别 :\n" + category);
                }
            }
            Uri data = intent.getData();
            if (data != null) {
                sb.append("\n接收到的 Intent 数据 :\n" + data.toString());
            }
        } else {
            sb.append("Intent is null.");
        }
        sb.append("\n\n");
        LogUtils.d(TAG, sb.toString());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // 缓存当前 activity
        WinBollActivityManager.getInstance(mCurrentAppCompatActivity).add(this);

        // 初始化工具栏
        mToolBar = initToolBar();
        if (mToolBar != null) {
            mCurrentAppCompatActivity.setSupportActionBar(mToolBar);
            if (isEnableDisplayHomeAsUp()) {
                // 显示后退按钮
                mCurrentAppCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            mCurrentAppCompatActivity.getSupportActionBar().setSubtitle(getTag());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isAddWinBollToolBar()) {
            //ToastUtils.show("mIWinBoll.isAddWinBollToolBar()");
            mCurrentAppCompatActivity.getMenuInflater().inflate(R.menu.toolbar_winboll_shared_main, menu);
        }
        if (WinBollGlobalApplication.isDebug()) {
            mCurrentAppCompatActivity.getMenuInflater().inflate(R.menu.toolbar_studio_debug, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        LogUtils.d(TAG, "onOptionsItemSelected");
        //ToastUtils.show(String.format("%s onOptionsItemSelected", TAG));
        if (item.getItemId() == R.id.item_testcrashreport) {
            for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++) {
                mCurrentAppCompatActivity.getString(i);
            }
            return true;
        } else if (item.getItemId() == R.id.item_log) {
//            LogUtils.d(TAG, "item_log not yet.");
//            Intent intent = new Intent(this, LogActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
//            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
//            startActivity(intent);

            //WinBollActivityManager.getInstance().printAvtivityListInfo();
            WinBollActivityManager.getInstance(mCurrentAppCompatActivity).startWinBollActivity(mCurrentAppCompatActivity, LogActivity.class);
            return true;
        } else if (item.getItemId() == R.id.item_exit) {
            exit();
            //ToastUtils.show("item_exit");
            //WinBollActivityManager.getInstance(mCurrentAppCompatActivity).finishAll();
            return true;
        } else if (item.getItemId() == R.id.item_info) {
            LogUtils.d(TAG, "item_info not yet.");
            return true;
            //WinBollApplication application = (WinBollApplication) getApplication();
            //application.getMyActivityLifecycleCallbacks().showActivityeInfo();
        } else if (item.getItemId() == R.id.item_exitdebug) {
            AboutView.setApp2NormalMode(mCurrentAppCompatActivity);
            return true;
        } else if (item.getItemId() == R.id.item_about) {
            startAboutActivity();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            WinBollActivityManager.getInstance(mCurrentAppCompatActivity).finish(this);
            return true;
        }
        // else if (item.getItemId() == android.R.id.home) {
        // 回到主窗口速度缓慢，方法备用。现在用 WinBollActivityManager resumeActivity 和 finish 方法管理。
        // _mMainWinBollActivity 是 WinBollActivity 的静态属性
        // onCreate 函数下 _mMainWinBollActivity 为空时就用 _mMainWinBollActivity = this 赋值。
        //startWinBollActivity(new Intent(_mMainWinBollActivity, _mMainWinBollActivity.getClass()), _mMainWinBollActivity.getTag(), _mMainWinBollActivity);
        //}
        return super.onOptionsItemSelected(item);
    }

    public boolean isAddWinBollInfoMenu() {
        return true;
    }

    @Override
    public void onBackPressed() {
        if (WinBollActivityManager.getInstance(getApplicationContext()).isFirstIWinBollActivity(this)) {
            exit();
        } else {
            WinBollActivityManager.getInstance(mCurrentAppCompatActivity).finish(this);
            super.onBackPressed();
        }
    }

    protected String getAppName() {
        return getString(R.string.lib_name);
    }

    void exit() {
        YesNoAlertDialog.OnDialogResultListener listener = new YesNoAlertDialog.OnDialogResultListener(){

            @Override
            public void onYes() {
                WinBollActivityManager.getInstance(getApplicationContext()).finishAll();
            }

            @Override
            public void onNo() {
            }
        };
        YesNoAlertDialog.show(this, "[ " + getAppName() + " ]", "Exit(Yes/No).\nIs close all activity?", listener);

    }

    //
    // activity: 为 null 时，
    // intent.putExtra 函数 "tag" 参数为 tag
    // activity: 不为 null 时，
    // intent.putExtra 函数 "tag" 参数为 activity.getTag()
    //
    public <T extends AboutActivity> void startAboutActivity() {
        Intent intent = new Intent(mCurrentAppCompatActivity, AboutActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.putExtra("tag", AboutActivity.TAG);
        mCurrentAppCompatActivity.startActivity(intent);
    }

//    @Override
//    public void startActivity(Intent intent) {
//        //绑定唯一标识 tag，存在 则根据 taskId 移动到前台
//        String tag = intent.getStringExtra("tag");
//        //ToastUtils.show("startActivityForResult tag " + tag);
//        //WinBollActivityManager.getInstance(this).printAvtivityListInfo();
//        if (WinBollActivityManager.getInstance(this).isActive(tag)) {
//            //ToastUtils.show("resumeActivity");
//            LogUtils.d(TAG, "resumeActivity");
//            WinBollActivityManager.getInstance(this).resumeActivity(this, tag);
//        } else {
//            //ToastUtils.show("super.startActivity(intent);");
//            super.startActivity(intent);
//        }
//    }

    protected void prosessActivityResult(int requestCode, int targetFragment, Intent data) {
        LogUtils.d(TAG, "onActivityResult");
        switch (requestCode) {
            case REQUEST_LOG_ACTIVITY : {
                    LogUtils.d(TAG, "REQUEST_LOG_ACTIVITY");
                    break;
                }
        }
    }
}
