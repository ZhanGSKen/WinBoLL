package cc.winboll.studio.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.shared.app.WinBollActivity;
import cc.winboll.studio.shared.app.WinBollActivityManager;
import cc.winboll.studio.shared.app.WinBollApplication;
import cc.winboll.studio.shared.log.LogUtils;
import cc.winboll.studio.shared.util.UriUtils;
import cc.winboll.studio.shared.view.StringToQrCodeView;
import cc.winboll.studio.shared.view.YesNoAlertDialog;
import cc.winboll.studio.unittest.UnitTestActivity;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

final public class MainActivity extends WinBollActivity {

	public static final String TAG = "MainActivity";

    public static final int REQUEST_HOME_ACTIVITY = 0;
    public static final int REQUEST_ABOUT_ACTIVITY = 1;

    @Override
    protected boolean isEnableDisplayHomeAsUp() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 接收并处理 Intent 数据，函数 Intent 处理接收就直接返回
        if (prosessIntents(getIntent())) return;
        // 以下正常创建主窗口
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 设置 WinBoll 应用 UI 类型
        WinBollApplication.setWinBollUI_TYPE(WinBollApplication.WinBollUI_TYPE.Aplication);
        //ToastUtils.show("WinBollUI_TYPE " + WinBollApplication.getWinBollUI_TYPE());
        LogUtils.d(TAG, "BuildConfig.DEBUG : " + Boolean.toString(BuildConfig.DEBUG));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setSubTitle("");
    }

    //
    // 处理传入的 Intent 数据
    //
    boolean prosessIntents(Intent intent) {
        if (intent == null 
            || intent.getAction() == null
            || intent.getAction().equals(""))
            return false;

        if (intent.getAction().equals(StringToQrCodeView.ACTION_UNITTEST_QRCODE)) {
            try {
                WinBollActivity clazzActivity = UnitTestActivity.class.newInstance();
                String tag = clazzActivity.getTag();
                LogUtils.d(TAG, "String tag = clazzActivity.getTag(); tag " + tag);
                Intent subIntent = new Intent(this, UnitTestActivity.class);
                subIntent.setAction(intent.getAction());
                File file = new File(getCacheDir(), UUID.randomUUID().toString());
                //取出文件uri
                Uri uri = intent.getData();
                if (uri == null) {
                    uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                }
                //获取文件真实地址
                String szSrcPath = UriUtils.getFileFromUri(getApplication(), uri);
                if (TextUtils.isEmpty(szSrcPath)) {
                    return false;
                }

                Files.copy(Paths.get(szSrcPath), Paths.get(file.getPath()));
                //startWinBollActivity(subIntent, tag);
                WinBollActivityManager.getInstance(this).startWinBollActivity(this, subIntent, UnitTestActivity.class);
            } catch (IllegalAccessException | InstantiationException | IOException e) {
                LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                // 函数处理异常返回失败
                return false;
            }
        } else {
            LogUtils.d(TAG, "prosessIntents|" + intent.getAction() + "|yet");
            return false;
        }
        return true;
    }

    @Override
    public String getTag() {
        return TAG;
    }


    @Override
    protected boolean isAddWinBollToolBar() {
        return true;
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
    protected Toolbar initToolBar() {
        return findViewById(R.id.activitymainToolbar1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_winboll_app_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_unittest) {
            WinBollActivityManager.getInstance(this).startWinBollActivity(this, UnitTestActivity.class);
        } else if (item.getItemId() == R.id.item_exit) {
            exit();
            return true;
        }
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
