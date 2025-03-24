package cc.winboll.studio.libapputils.activities;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/14 13:20:33
 * @Describe 应用介绍页
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import cc.winboll.studio.libapputils.R;
import cc.winboll.studio.libapputils.bean.APPInfo;
import cc.winboll.studio.libapputils.view.AboutView;

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
        LinearLayout llMain = findViewById(R.id.activityaboutLinearLayout1);
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
