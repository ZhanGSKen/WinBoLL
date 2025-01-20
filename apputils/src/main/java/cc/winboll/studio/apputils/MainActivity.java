package cc.winboll.studio.apputils;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libapputils.activities.AssetsHtmlActivity;
import cc.winboll.studio.libapputils.activities.QRCodeDecodeActivity;
import cc.winboll.studio.libapputils.app.WinBollActivity;
import cc.winboll.studio.libapputils.app.WinBollActivityManager;
import cc.winboll.studio.libapputils.log.LogActivity;
import cc.winboll.studio.libapputils.log.LogUtils;
import com.hjq.toast.ToastUtils;

final public class MainActivity extends WinBollActivity {

	public static final String TAG = "MainActivity";

    public static final int REQUEST_QRCODEDECODE_ACTIVITY = 0;

    @Override
    protected boolean isEnableDisplayHomeAsUp() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Toolbar toolbar = findViewById(R.id.activitymainToolbar1);
        //setActionBar(toolbar);

        // 接收并处理 Intent 数据，函数 Intent 处理接收就直接返回
        //if (prosessIntents(getIntent())) return;
        // 以下正常创建主窗口

//        // 设置 WinBoll 应用 UI 类型
//        WinBollApplication.setWinBollUI_TYPE(WinBollApplication.WinBollUI_TYPE.Aplication);
//        //ToastUtils.show("WinBollUI_TYPE " + WinBollApplication.getWinBollUI_TYPE());
//        LogUtils.d(TAG, "BuildConfig.DEBUG : " + Boolean.toString(BuildConfig.DEBUG));
    }

    public void onTestLogClick(View view) {
        LogUtils.d(TAG, "onTestLogClick");
        Toast.makeText(getApplication(), "onTestLogClick", Toast.LENGTH_SHORT).show();
    }

    public void onLogUtilsClick(View view) {
//        Intent intent = new Intent(this, LogActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
//        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
//        startActivity(intent);

        //WinBollActivityManager.getInstance().printAvtivityListInfo();
        WinBollActivityManager.getInstance(this).startWinBollActivity(this, LogActivity.class);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        setSubTitle("");
    }

    @Override
    public void onBackPressed() {
//        exit();
    }

//    void exit() {
//        YesNoAlertDialog.OnDialogResultListener listener = new YesNoAlertDialog.OnDialogResultListener(){
//
//            @Override
//            public void onYes() {
//                WinBollActivityManager.getInstance(getApplicationContext()).finishAll();
//            }
//
//            @Override
//            public void onNo() {
//            }
//        };
//        YesNoAlertDialog.show(this, "[ " + getString(R.string.app_name) + " ]", "Exit(Yes/No).\nIs close all activity?", listener);
//    }

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
    public String getTag() {
        return TAG;
    }

    @Override
    protected boolean isAddWinBollToolBar() {
        return true;
    }

    @Override
    protected Toolbar initToolBar() {
        return findViewById(R.id.activitymainToolbar1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_testwinboll) {
            WinBollActivityManager.getInstance(this).startWinBollActivity(this, TestWinBollActivity.class);
        } else if (item.getItemId() == R.id.item_teststringtoqrcodeview) {
            WinBollActivityManager.getInstance(this).startWinBollActivity(this, TestStringToQrCodeViewActivity.class);
        } else if (item.getItemId() == R.id.item_testqrcodedecodeactivity) {
            Intent intent = new Intent(this, QRCodeDecodeActivity.class);
            startActivityForResult(intent, REQUEST_QRCODEDECODE_ACTIVITY);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onTestJavascriptHtmlActivity(View view) {
        Intent intent = new Intent(this, AssetsHtmlActivity.class);
        intent.putExtra(AssetsHtmlActivity.EXTRA_HTMLFILENAME, "javascript_test.html");
        WinBollActivityManager.getInstance(this).startWinBollActivity(this, intent, AssetsHtmlActivity.class);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_QRCODEDECODE_ACTIVITY : {
                    String text = data.getStringExtra(QRCodeDecodeActivity.EXTRA_RESULT);
                    ToastUtils.show(text);
                    break;
                }
            default : {
                    ToastUtils.show(String.format("%d, %d", requestCode, resultCode));
                    super.onActivityResult(requestCode, resultCode, data);
                }
        }
    }
}
