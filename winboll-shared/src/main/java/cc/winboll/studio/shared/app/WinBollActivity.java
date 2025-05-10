package cc.winboll.studio.shared.app;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/08/12 14:32:08
 * @Describe WinBoll 活动窗口基础类
 */
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import cc.winboll.studio.R;
import cc.winboll.studio.shared.activities.AboutActivity;
import cc.winboll.studio.shared.log.LogActivity;
import cc.winboll.studio.shared.log.LogUtils;
import cc.winboll.studio.shared.view.AboutView;
import com.hjq.toast.ToastUtils;
import java.util.List;
import java.util.Set;

abstract public class WinBollActivity extends AppCompatActivity {

    public static final String TAG = "WinBollActivity";

    public static final int REQUEST_LOG_ACTIVITY = 0;

    Toolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkResolveActivity();
        LogUtils.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        archiveInstance();

    }

    boolean checkResolveActivity() {
        PackageManager packageManager = getPackageManager();
        //Intent intent = new Intent("your_action_here");
        Intent intent = getIntent();
        if (intent != null) {
            List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfoList.size() > 0) {
                // 传入的Intent action在Activity清单的intent-filter的action节点里有定义
                if (intent.getAction() != null) {
                    if (intent.getAction().equals(cc.winboll.studio.intent.action.DEBUGVIEW)) {
                        WinBollApplication.setIsDebug(true);
                        //ToastUtils.show!("WinBollApplication.setIsDebug(true) by action : " + intent.getAction());

                    }
                }
                return true;
            } else {
                // 传入的Intent action在Activity清单的intent-filter的action节点里没有定义
                //ToastUtils.show("false : " + intent.getAction());
                return false;
            }

            /* ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
             if (resolveInfo != null) {
             // action在清单文件中有声明

             } else {

             }*/
        }

        // action在清单文件中没有声明
        ToastUtils.show("false");
        return false;
    }

    void archiveInstance() {
        Intent intent = getIntent();
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
        mToolBar = initToolBar();
        setSupportActionBar(mToolBar);
        if (isEnableDisplayHomeAsUp() && mToolBar != null) {
            // 显示后退按钮
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 缓存当前 activity
        LogUtils.d(TAG, "ActManager.getInstance().add(this);");
        //ToastUtils.show("getTag() " + getTag());
        WinBollActivityManager.getInstance(this).add(this);
        //WinBollActivityManager.getInstance().printAvtivityListInfo();
        //ToastUtils.show("WinBollUI_TYPE " + WinBollApplication.getWinBollUI_TYPE());
        //boolean isDebuging = WinBollApplication.loadDebugStatusIsDebuging();
        //ToastUtils.show(String.valueOf(isDebuging));
        //WinBollApplication.setIsDebug(true);

        setSubTitle(getTag());
    }

    @Override
    public void recreate() {
        super.recreate();
    }

    @Override
    protected void onDestroy() {
        WinBollActivityManager.getInstance(this).registeRemove(this);
        super.onDestroy();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    @Override
    public <T extends View> T findViewById(int id) {
        return super.findViewById(id);
    }

    @Override
    public MenuInflater getMenuInflater() {
        return super.getMenuInflater();
    }

    public WinBollActivity getActivity() {
        return this;
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    public <T extends FragmentManager> T getWinBollActivitySupportFragmentManager() {
        return (T)super.getSupportFragmentManager();
    }

    @Override
    public ActionBar getSupportActionBar() {
        return super.getSupportActionBar();
    }

    @Override
    public void setSupportActionBar(Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
    }

    @Override
    public void setActionBar(android.widget.Toolbar toolbar) {
        super.setActionBar(toolbar);
    }

    public void setSubTitle(CharSequence title) {
        if (super.getSupportActionBar() != null) {
            super.getSupportActionBar().setSubtitle(title);
        }
    }

    public CharSequence getSubTitle() {
        if (super.getSupportActionBar() != null) {
            return super.getSupportActionBar().getSubtitle();
        }
        return "";
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
    }

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
    }

    @Override
    public void setTheme(Resources.Theme theme) {
        super.setTheme(theme);
    }

    @Override
    public void setTheme(int resid) {
        super.setTheme(resid);
    }

    @Override
    public void setTitleColor(int textColor) {
        super.setTitleColor(textColor);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return super.getSharedPreferences(name, mode);
    }


    @Override
    public boolean releaseInstance() {
        return super.releaseInstance();
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, int flags) {
        return super.registerReceiver(receiver, filter, flags);
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        return super.registerReceiver(receiver, filter, broadcastPermission, scheduler);
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler, int flags) {
        return super.registerReceiver(receiver, filter, broadcastPermission, scheduler, flags);
    }

    @Override
    public void startActivities(Intent[] intents) {
        super.startActivities(intents);
    }

    @Override
    public void startActivityFromFragment(Fragment fragment, Intent intent, int requestCode) {
        super.startActivityFromFragment(fragment, intent, requestCode);
    }

    @Override
    public void startActivityFromFragment(android.app.Fragment fragment, Intent intent, int requestCode, Bundle options) {
        super.startActivityFromFragment(fragment, intent, requestCode, options);
    }

    @Override
    public void startActivityFromChild(Activity child, Intent intent, int requestCode, Bundle options) {
        super.startActivityFromChild(child, intent, requestCode, options);
    }

    @Override
    public void startActivities(Intent[] intents, Bundle options) {
        super.startActivities(intents, options);
    }

    @Override
    public boolean startActivityIfNeeded(Intent intent, int requestCode) {
        return super.startActivityIfNeeded(intent, requestCode);
    }

    @Override
    public boolean startActivityIfNeeded(Intent intent, int requestCode, Bundle options) {
        return super.startActivityIfNeeded(intent, requestCode, options);
    }

    @Override
    public void startActivityFromChild(Activity child, Intent intent, int requestCode) {
        super.startActivityFromChild(child, intent, requestCode);
    }

    @Override
    public ComponentName startService(Intent service) {
        return super.startService(service);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
    }

    @Override
    public void startActivityFromFragment(android.app.Fragment fragment, Intent intent, int requestCode) {
        super.startActivityFromFragment(fragment, intent, requestCode);
    }

    @Override
    public void startActivityFromFragment(Fragment requestIndex, Intent fragment, int intent, Bundle requestCode) {
        super.startActivityFromFragment(requestIndex, fragment, intent, requestCode);
    }

    @Override
    public void startActivity(Intent intent, Bundle options) {
        super.startActivity(intent, options);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isAddWinBollToolBar()) {
            getMenuInflater().inflate(R.menu.toolbar_winboll_shared_main, menu);
        }
        if (WinBollApplication.isDebug()) {
            getMenuInflater().inflate(R.menu.toolbar_studio_debug, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LogUtils.d(TAG, "onOptionsItemSelected");
        if (item.getItemId() == R.id.item_log) {
            //WinBollActivityManager.getInstance().printAvtivityListInfo();
            WinBollActivityManager.getInstance(this).startWinBollActivity(this, LogActivity.class);
        } else if (item.getItemId() == R.id.item_exit) {
            WinBollActivityManager.getInstance(this).finishAll();
        } else if (item.getItemId() == R.id.item_info) {
            WinBollApplication application = (WinBollApplication) getApplication();
            application.getMyActivityLifecycleCallbacks().showActivityeInfo();
        } else if (item.getItemId() == R.id.item_exitdebug) {
            AboutView.setApp2NormalMode(getApplicationContext());
        } else if (item.getItemId() == R.id.item_about) {
            startAboutActivity();
        } else if (item.getItemId() == android.R.id.home) {
            WinBollActivityManager.getInstance(this).finish(this);
        }
        // else if (item.getItemId() == android.R.id.home) {
        // 回到主窗口速度缓慢，方法备用。现在用 WinBollActivityManager resumeActivity 和 finish 方法管理。
        // _mMainWinBollActivity 是 WinBollActivity 的静态属性
        // onCreate 函数下 _mMainWinBollActivity 为空时就用 _mMainWinBollActivity = this 赋值。
        //startWinBollActivity(new Intent(_mMainWinBollActivity, _mMainWinBollActivity.getClass()), _mMainWinBollActivity.getTag(), _mMainWinBollActivity);
        //}
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //ToastUtils.show("onBackPressed");
        WinBollActivityManager.getInstance(this).finish(this);
    }


    /*public void getInstanse() {
     startWinBollActivity(new Intent(), getTag(), null);
     }*/

    //
    // activity: 为 null 时，
    // intent.putExtra 函数 "tag" 参数为 tag
    // activity: 不为 null 时，
    // intent.putExtra 函数 "tag" 参数为 activity.getTag()
    //
//    protected <T extends WinBollActivity> void startWinBollActivity(Intent intent, String tag, T activity) {
//        LogUtils.d(TAG, "startWinBollActivityForResult tag " + tag);
//        //ToastUtils.show("startWinBollActivityForResult tag " + tag);
//        //打开多任务窗口 flags
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
//        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
//        if (activity != null) {
//            intent.putExtra("tag", activity.getTag());
//        } else {
//            intent.putExtra("tag", tag);
//        }
//        //ToastUtils.show("super.startActivityForResult(intent, requestCode); tag " + tag);
//        LogUtils.d(TAG, "startActivityForResult(intent, requestCode);" + tag);
//        startActivity(intent);
//    }

    //
    // activity: 为 null 时，
    // intent.putExtra 函数 "tag" 参数为 tag
    // activity: 不为 null 时，
    // intent.putExtra 函数 "tag" 参数为 activity.getTag()
    //
    protected <T extends AboutActivity> void startAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.putExtra("tag", AboutActivity.TAG);
        startActivity(intent);
    }

    @Override
    public void startActivity(Intent intent) {
        //绑定唯一标识 tag，存在 则根据 taskId 移动到前台
        String tag = intent.getStringExtra("tag");
        //ToastUtils.show("startActivityForResult tag " + tag);
        //WinBollActivityManager.getInstance(this).printAvtivityListInfo();
        if (WinBollActivityManager.getInstance(this).isActive(tag)) {
            //ToastUtils.show("resumeActivity");
            LogUtils.d(TAG, "resumeActivity");
            WinBollActivityManager.getInstance(this).resumeActivity(this, tag);
        } else {
            //ToastUtils.show("super.startActivity(intent);");
            super.startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int targetFragment, Intent data) {
        LogUtils.d(TAG, "onActivityResult");
        switch (requestCode) {
            case REQUEST_LOG_ACTIVITY : {
                    LogUtils.d(TAG, "REQUEST_LOG_ACTIVITY");
                    break;
                }
            default : {
                    super.onActivityResult(requestCode, targetFragment, data);
                }
        }
    }

    public boolean isAddWinBollInfoMenu() {
        return true;
    }

    abstract public String getTag();
    abstract protected Toolbar initToolBar();
    abstract protected boolean isEnableDisplayHomeAsUp();
    abstract protected boolean isAddWinBollToolBar();
}
