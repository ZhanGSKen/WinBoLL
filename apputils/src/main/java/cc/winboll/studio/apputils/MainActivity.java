package cc.winboll.studio.apputils;

import cc.winboll.studio.apputils.R;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.Toolbar;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.LogView;
import cc.winboll.studio.libappbase.utils.ToastUtils;
import cc.winboll.studio.libapputils.activities.AssetsHtmlActivity;
import cc.winboll.studio.libapputils.activities.LogActivity;
import cc.winboll.studio.libapputils.activities.QRCodeDecodeActivity;
import cc.winboll.studio.libapputils.app.AboutActivityFactory;
import cc.winboll.studio.libapputils.bean.APPInfo;
import cc.winboll.studio.libapputils.view.AboutView;
import cc.winboll.studio.libapputils.view.YesNoAlertDialog;
import java.util.List;
import java.util.Set;

final public class MainActivity extends Activity {

	public static final String TAG = "MainActivity";

    public static final int REQUEST_QRCODEDECODE_ACTIVITY = 0;

    Toolbar mToolbar;
    LogView mLogView;
//
//    @Override
//    public Activity getActivity() {
//        return this;
//    }

//    @Override
//    public APPInfo getAppInfo() {
//        String szBranchName = "apputils";
//
//        APPInfo appInfo = AboutActivityFactory.buildDefaultAPPInfo();
//        appInfo.setAppName("APPUtils");
//        appInfo.setAppIcon(cc.winboll.studio.libapputils.R.drawable.ic_winboll);
//        appInfo.setAppDescription("APPUtils Description");
//        appInfo.setAppGitName("APP");
//        appInfo.setAppGitOwner("Studio");
//        appInfo.setAppGitAPPBranch(szBranchName);
//        appInfo.setAppGitAPPSubProjectFolder(szBranchName);
//        appInfo.setAppHomePage("https://www.winboll.cc/studio/details.php?app=APP");
//        appInfo.setAppAPKName("APPUtils");
//        appInfo.setAppAPKFolderName("APPUtils");
//        return appInfo;
//        //return null;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLogView = findViewById(R.id.logview);
        mLogView.start();

        // 初始化工具栏
        mToolbar = findViewById(R.id.activitymainToolbar1);
        setActionBar(mToolbar);
//        if (isEnableDisplayHomeAsUp()) {
//            // 显示后退按钮
//            getActionBar().setDisplayHomeAsUpEnabled(true);
//        }
//        getActionBar().setSubtitle(getTag());

        checkResolveActivity();
        archiveInstance();



        // 接收并处理 Intent 数据，函数 Intent 处理接收就直接返回
        //if (prosessIntents(getIntent())) return;
        // 以下正常创建主窗口

//        // 设置 WinBoll 应用 UI 类型
//        WinBollApplication.setWinBollUI_TYPE(WinBollApplication.WinBollUI_TYPE.Aplication);
//        //ToastUtils.show("WinBollUI_TYPE " + WinBollApplication.getWinBollUI_TYPE());
//        LogUtils.d(TAG, "BuildConfig.DEBUG : " + Boolean.toString(BuildConfig.DEBUG));
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
                    if (intent.getAction().equals(cc.winboll.studio.libapputils.intent.action.DEBUGVIEW)) {
                        App.setIsDebug(true);
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
        // 缓存当前 activity
        //WinBollActivityManager.getInstance(this).add(this);
    }

    @Override
    public void onDestroy() {
        //WinBollActivityManager.getInstance(this).registeRemove(this);
        super.onDestroy();
    }

    public void onTestLogClick(View view) {
        LogUtils.d(TAG, "onTestLogClick");
        Toast.makeText(getApplication(), "onTestLogClick", Toast.LENGTH_SHORT).show();
    }

    public void onTestLogActivity(View view) {
        Intent intent = new Intent(this, LogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivity(intent);

        //WinBollActivityManager.getInstance().printAvtivityListInfo();
        //WinBollActivityManager.getInstance(this).startWinBollActivity(this, LogActivity.class);
    }

    //
    // 处理传入的 Intent 数据
    //
    boolean prosessIntents(Intent intent) {
        if (intent == null 
            || intent.getAction() == null
            || intent.getAction().equals(""))
            return false;

//        if (intent.getAction().equals(StringToQrCodeView.ACTION_UNITTEST_QRCODE)) {
//            try {
//                WinBollActivity clazzActivity = UnitTestActivity.class.newInstance();
//                String tag = clazzActivity.getTag();
//                LogUtils.d(TAG, "String tag = clazzActivity.getTag(); tag " + tag);
//                Intent subIntent = new Intent(this, UnitTestActivity.class);
//                subIntent.setAction(intent.getAction());
//                File file = new File(getCacheDir(), UUID.randomUUID().toString());
//                //取出文件uri
//                Uri uri = intent.getData();
//                if (uri == null) {
//                    uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
//                }
//                //获取文件真实地址
//                String szSrcPath = UriUtils.getFileFromUri(getApplication(), uri);
//                if (TextUtils.isEmpty(szSrcPath)) {
//                    return false;
//                }
//
//                Files.copy(Paths.get(szSrcPath), Paths.get(file.getPath()));
//                //startWinBollActivity(subIntent, tag);
//                WinBollActivityManager.getInstance(this).startWinBollActivity(this, subIntent, UnitTestActivity.class);
//            } catch (IllegalAccessException | InstantiationException | IOException e) {
//                LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
//                // 函数处理异常返回失败
//                return false;
//            }
//        } else {
//            LogUtils.d(TAG, "prosessIntents|" + intent.getAction() + "|yet");
//            return false;
//        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //ToastUtils.show("onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
//        if (isAddWinBollToolBar()) {
//            //ToastUtils.show("mIWinBoll.isAddWinBollToolBar()");
//            getMenuInflater().inflate(R.menu.toolbar_winboll_shared_main, menu);
//        }
        if (App.isDebug()) {
            getMenuInflater().inflate(R.menu.toolbar_studio_debug, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_exit) {
            exit();
            return true;
//        } else if (item.getItemId() == R.id.item_about) {
//            AboutActivityFactory.showAboutActivity(this, getAppInfo());
//            return true;
        } else if (item.getItemId() == R.id.item_teststringtoqrcodeview) {
            Intent intent = new Intent(this, TestStringToQrCodeViewActivity.class);
            startActivityForResult(intent, REQUEST_QRCODEDECODE_ACTIVITY);
            //WinBollActivityManager.getInstance(this).startWinBollActivity(this, TestStringToQrCodeViewActivity.class);
        } else if (item.getItemId() == R.id.item_testqrcodedecodeactivity) {
            Intent intent = new Intent(this, QRCodeDecodeActivity.class);
            startActivityForResult(intent, REQUEST_QRCODEDECODE_ACTIVITY);
        } else if (item.getItemId() == R.id.item_testcrashreport) {
            for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++) {
                getString(i);
            }
            return true;
        } else if (item.getItemId() == R.id.item_log) {
            //WinBollActivityManager.getInstance(this).startWinBollActivity(this, LogActivity.class);
            return true;
        } else if (item.getItemId() == R.id.item_exitdebug) {
            //AboutView.setApp2NormalMode(this);
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            //WinBollActivityManager.getInstance(this).finish(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void about() {
//        Intent intent = new Intent(this, AboutActivity.class);
//        intent.putExtra(AboutActivity.EXTRA_APPINFO, AboutActivityFactory.buildAPPBranchInfo(this));
//        WinBollActivityManager.getInstance(this).startWinBollActivity(this, intent, AboutActivity.class);
    }

    void exit() {
        YesNoAlertDialog.OnDialogResultListener listener = new YesNoAlertDialog.OnDialogResultListener(){

            @Override
            public void onYes() {
                //WinBollActivityManager.getInstance(getApplicationContext()).finishAll();
            }

            @Override
            public void onNo() {
            }
        };
        YesNoAlertDialog.show(this, "[ " + getString(R.string.app_name) + " ]", "Exit(Yes/No).\nIs close all activity?", listener);

    }

    @Override
    public void onBackPressed() {
//        if (WinBollActivityManager.getInstance(getApplicationContext()).isFirstIWinBollActivity(this)) {
//            exit();
//        } else {
//            WinBollActivityManager.getInstance(this).finish(this);
//            super.onBackPressed();
//        }
    }

    public void onTestAboutActivity(View view) {
        about();
    }

    public void onTestHtmlActivity(View view) {
        Intent intent = new Intent(this, AssetsHtmlActivity.class);
        intent.putExtra(AssetsHtmlActivity.EXTRA_HTMLFILENAME, "javascript_test.html");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivity(intent);
        //WinBollActivityManager.getInstance(this).startWinBollActivity(this, intent, AssetsHtmlActivity.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLogView.start();
    }

    /*@Override
     protected void onActivithyResult(int requestCode, int resultCode, Intent data) {
     switch (requestCode) {
     case REQUEST_QRCODEDECODE_ACTIVITY : {
     if (data != null) {
     String text = data.getStringExtra(QRCodeDecodeActivity.EXTRA_RESULT);
     ToastUtils.show(text);
     }
     break;
     }
     default : {
     //ToastUtils.show(String.format("%d, %d", requestCode, resultCode));
     super.prosessActivityResult(requestCode, resultCode, data);
     }
     }
     }*/
}
