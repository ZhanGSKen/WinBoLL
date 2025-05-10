package cc.winboll.studio.shared.activities;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/01/03 11:02:49
 * @Describe HelpActivity
 */
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import cc.winboll.studio.R;
import cc.winboll.studio.shared.app.WinBollActivity;
import cc.winboll.studio.shared.app.WinBollActivityManager;
import cc.winboll.studio.shared.log.LogUtils;
import cc.winboll.studio.shared.util.FileUtils;
import cc.winboll.studio.shared.view.SimpleWebView;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class HelpActivity extends WinBollActivity {

    public static final String TAG = "HelpActivity";

    String mszHelpIndexFilePath = "";
    Uri mszHelpIndexFileUri;
    Context mContext;

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected boolean isEnableDisplayHomeAsUp() {
        return true;
    }


    @Override
    protected boolean isAddWinBollToolBar() {
        return false;
    }

    @Override
    protected Toolbar initToolBar() {
        return findViewById(R.id.activityhelpToolbar1);
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
        setContentView(R.layout.activity_help);

        // 与其他应用分享 html 帮助
        //FileUtils.shareHtmlFile(this, mszHelpIndexFilePath);

        // 直接读取 assets 文件显示 WebView
        //
        initWebViewFromAssets();
        
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
    void initWebViewFromAssets() {
        try {
            SimpleWebView webView = findViewById(R.id.activityhelpSimpleWebView1);
            webView.getSettings().setSupportZoom(true);
            // 读取assets文件夹下的index.html文件
            InputStream inputStream = getAssets().open("winboll/studio/html/index.html");
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

    void initAssetsToSD() {
        String szHtmlFileName = "index.html";
        String szAssetsFilePath = "winboll/studio/html/" + szHtmlFileName;
        StringBuilder sbDst = new StringBuilder();
        sbDst.append(getFilesDir());
        sbDst.append(File.separator);
        sbDst.append("winboll");
        sbDst.append(File.separator);
        sbDst.append("studio");
        sbDst.append(File.separator);
        sbDst.append("html");
        sbDst.append(File.separator);
        File fDstFolder = new File(sbDst.toString());
        if (!fDstFolder.exists()) {
            fDstFolder.mkdirs();
        }
        File fDst = new File(fDstFolder, szHtmlFileName);

        mszHelpIndexFilePath = fDst.getPath();
        mszHelpIndexFileUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", fDst);

        FileUtils.copyAssetsToSD(this, szAssetsFilePath, mszHelpIndexFilePath);

        // 设置只读权限
        //fDst.setReadOnly();
    }
}
