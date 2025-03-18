package cc.winboll.studio.libapputils.activities;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/03 11:02:49
 * @Describe 一个可以浏览随 APP 附带的 Html 文档的窗口
 */
import cc.winboll.studio.libapputils.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toolbar;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libapputils.app.IWinBollActivity;
import cc.winboll.studio.libapputils.bean.APPInfo;
import cc.winboll.studio.libapputils.view.SimpleWebView;
import java.io.IOException;
import java.io.InputStream;

public class AssetsHtmlActivity extends Activity implements IWinBollActivity {

    public static final String TAG = "AssetsHtmlActivity";

    public static final String EXTRA_HTMLFILENAME = "EXTRA_HTMLFILENAME";

    String mszHelpIndexFilePath = "";
    Uri mszHelpIndexFileUri;
    Context mContext;

    // Assets 文件夹里的 Html 文件的名称
    String mszHtmlFileName;

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public APPInfo getAppInfo() {
        return null;
    }
    
    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public boolean isEnableDisplayHomeAsUp() {
        return true;
    }


    @Override
    public boolean isAddWinBollToolBar() {
        return false;
    }

    @Override
    public Toolbar initToolBar() {
        return findViewById(R.id.activityassetshtmlToolbar1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            WinBollActivityManager.getInstance(this).finish(this);
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assetshtml);

        mszHtmlFileName = "index.html";

        Intent intent = getIntent();
        if (intent != null) {
            String szTemp = intent.getStringExtra(EXTRA_HTMLFILENAME);
            if (szTemp != null && !szTemp.trim().equals("")) {
                mszHtmlFileName = szTemp.trim();
            }
            //ToastUtils.show(mszHtmlFileName);
        }

        // 与其他应用分享 html 帮助
        //FileUtils.shareHtmlFile(this, mszHelpIndexFilePath);

        // 直接读取 assets 文件显示 WebView
        //
        initWebViewFromAssets(mszHtmlFileName);

        // 复制 assets 文件到数据目录 files 后，
        // 显示 WebView
        //
//        if (App.isDebug()) {
//            initAssetsToSD();
//        }
//        MyWebView myWebView = findViewById(R.id.activityhelpMyWebView1);
//        myWebView.getSettings().setSupportZoom(true);
//
//        // 使用file协议加载本地HTML文件
//        //Test OK //String url = "content://cc.winboll.studio.app.beta.fileprovider/files_path/winboll/studio/html/index.html";
//        //myWebView.loadUrl(url);
//
//        myWebView.loadUrl(mszHelpIndexFileUri.toString());
    }

    //
    void initWebViewFromAssets(String szHtmlFileName) {
        try {
            SimpleWebView webView = findViewById(R.id.activityassetshtmlSimpleWebView1);
            webView.getSettings().setSupportZoom(true);
            // 读取assets文件夹下的index.html文件
            InputStream inputStream = getAssets().open("winboll/studio/html/" + szHtmlFileName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String html = new String(buffer);
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        } catch (IOException e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
    }

//    void initAssetsToSD() {
//        String szHtmlFileName = "index.html";
//        String szAssetsFilePath = "winboll/studio/html/" + szHtmlFileName;
//        StringBuilder sbDst = new StringBuilder();
//        sbDst.append(getFilesDir());
//        sbDst.append(File.separator);
//        sbDst.append("winboll");
//        sbDst.append(File.separator);
//        sbDst.append("studio");
//        sbDst.append(File.separator);
//        sbDst.append("html");
//        sbDst.append(File.separator);
//        File fDstFolder = new File(sbDst.toString());
//        if (!fDstFolder.exists()) {
//            fDstFolder.mkdirs();
//        }
//        File fDst = new File(fDstFolder, szHtmlFileName);
//
//        mszHelpIndexFilePath = fDst.getPath();
//        mszHelpIndexFileUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", fDst);
//
//        FileUtils.copyAssetsToSD(this, szAssetsFilePath, mszHelpIndexFilePath);
//
//        // 设置只读权限
//        //fDst.setReadOnly();
//    }
}
