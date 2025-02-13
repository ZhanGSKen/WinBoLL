package cc.winboll.studio.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.contacts.BuildConfig;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.contacts.beans.MainServiceBean;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.LogView;
import cc.winboll.studio.libappbase.SOS;
import cc.winboll.studio.libapputils.app.AboutActivityFactory;
import cc.winboll.studio.libapputils.app.IWinBollActivity;
import cc.winboll.studio.libapputils.app.WinBollActivityManager;
import cc.winboll.studio.libapputils.bean.APPInfo;
import cc.winboll.studio.libapputils.view.YesNoAlertDialog;

final public class MainActivity extends AppCompatActivity implements IWinBollActivity {

	public static final String TAG = "MainActivity";

    public static final int REQUEST_HOME_ACTIVITY = 0;
    public static final int REQUEST_ABOUT_ACTIVITY = 1;
    
    LogView mLogView;
    Toolbar mToolbar;
    CheckBox cbMainService;
    MainServiceBean mMainServiceBean;

    @Override
    public AppCompatActivity getActivity() {
        return this;
    }

    @Override
    public APPInfo getAppInfo() {
        String szBranchName = "contacts";

        APPInfo appInfo = AboutActivityFactory.buildDefaultAPPInfo();
        appInfo.setAppName("Contacts");
        appInfo.setAppIcon(cc.winboll.studio.libapputils.R.drawable.ic_winboll);
        appInfo.setAppDescription("Contacts Description");
        appInfo.setAppGitName("APP");
        appInfo.setAppGitOwner("Studio");
        appInfo.setAppGitAPPBranch(szBranchName);
        appInfo.setAppGitAPPSubProjectFolder(szBranchName);
        appInfo.setAppHomePage("https://www.winboll.cc/studio/details.php?app=Contacts");
        appInfo.setAppAPKName("Contacts");
        appInfo.setAppAPKFolderName("Contacts");
        return appInfo;
        //return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 接收并处理 Intent 数据，函数 Intent 处理接收就直接返回
        //if (prosessIntents(getIntent())) return;
        // 以下正常创建主窗口
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mLogView = findViewById(R.id.activitymainLogView1);

        if (GlobalApplication.isDebuging()) { mLogView.start(); }
        
        // 初始化工具栏
        mToolbar = findViewById(R.id.activitymainToolbar1);
        setSupportActionBar(mToolbar);
        if (isEnableDisplayHomeAsUp()) {
            // 显示后退按钮
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setSubtitle(getTag());

        //ToastUtils.show("WinBollUI_TYPE " + WinBollApplication.getWinBollUI_TYPE());
        LogUtils.d(TAG, "BuildConfig.DEBUG : " + Boolean.toString(BuildConfig.DEBUG));

        mMainServiceBean = MainServiceBean.loadBean(this, MainServiceBean.class);
        if (mMainServiceBean == null) {
            mMainServiceBean = new MainServiceBean();
        }
        cbMainService = findViewById(R.id.activitymainCheckBox1);
        cbMainService.setChecked(mMainServiceBean.isEnable());
        cbMainService.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    SOS.sendToWinBoll(MainActivity.this);
//                    if (cbMainService.isChecked()) {
//                        MainService.startISOSService(MainActivity.this);
//                    } else {
//                        MainService.stopISOSService(MainActivity.this);
//                    }
                }
            });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //setSubTitle("");
    }

    //
    // 处理传入的 Intent 数据
    //
//    boolean prosessIntents(Intent intent) {
//        if (intent == null 
//            || intent.getAction() == null
//            || intent.getAction().equals(""))
//            return false;
//
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
//        return true;
//    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public Toolbar initToolBar() {
        return findViewById(R.id.activitymainToolbar1);
    }

    @Override
    public boolean isAddWinBollToolBar() {
        return true;
    }

    @Override
    public boolean isEnableDisplayHomeAsUp() {
        return false;
    }

    @Override
    public void onBackPressed() {
        exit();
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
        YesNoAlertDialog.show(this, "[ " + getString(R.string.app_name) + " ]", "Exit(Yes/No).\nIs close all activity?", listener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.item_unittest) {
//            WinBollActivityManager.getInstance(this).startWinBollActivity(this, UnitTestActivity.class);
//        } else 
//        if (item.getItemId() == R.id.item_exit) {
//            exit();
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case REQUEST_HOME_ACTIVITY : {
                    LogUtils.d(TAG, "REQUEST_HOME_ACTIVITY");
                    break;
                }
            case REQUEST_ABOUT_ACTIVITY : {
                    LogUtils.d(TAG, "REQUEST_ABOUT_ACTIVITY");
                    break;
                }
            default : {
                    super.onActivityResult(requestCode, resultCode, data);
                }
        }
    }
}
