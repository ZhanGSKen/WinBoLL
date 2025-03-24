package cc.winboll.studio.libaes.winboll;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/24 16:39:29
 * @Describe WinBoll Android 应用介绍窗口
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import cc.winboll.studio.libaes.R;

final public class AboutActivity extends Activity {

    public static final String TAG = "AboutActivity";

    public static final String EXTRA_APPINFO = "EXTRA_APPINFO";

    APPInfo mAPPInfo;

    @Override
    public Context getApplicationContext() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Intent intent = getIntent();
        if (intent != null) {
            mAPPInfo = (APPInfo)intent.getSerializableExtra(EXTRA_APPINFO);
        }
        if (mAPPInfo == null) {
            mAPPInfo = new APPInfo();
        }

        AboutView aboutView = new AboutView(this, mAPPInfo);
        LinearLayout llMain = findViewById(R.id.aboutroot_ll);
        llMain.addView(aboutView);

        //ToastUtils.show(TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_winboll_shared_about, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.item_help) {
//            WinBollActivityManager.getInstance(this).startWinBollActivity(this, AssetsHtmlActivity.class);
//        }
        return super.onOptionsItemSelected(item);
    }
}
