package cc.winboll.studio.libapputils.activities;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/14 13:20:33
 * @Describe 应用介绍页
 */
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libapputils.R;
import cc.winboll.studio.libapputils.app.IWinBollActivity;
import cc.winboll.studio.libapputils.app.WinBollActivityManager;
import cc.winboll.studio.libapputils.bean.APPInfo;
import cc.winboll.studio.libapputils.view.AboutView;
import com.hjq.toast.ToastUtils;

final public class AboutActivity extends AppCompatActivity implements IWinBollActivity {

    public static final String TAG = "AboutActivity";

    public static final String EXTRA_APPINFO = "EXTRA_APPINFO";


    APPInfo mAPPInfo;

    @Override
    public AppCompatActivity getActivity() {
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
        return findViewById(R.id.activityaboutToolbar1);
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
        if (item.getItemId() == R.id.item_help) {
            WinBollActivityManager.getInstance(this).startWinBollActivity(this, AssetsHtmlActivity.class);
        }
        return super.onOptionsItemSelected(item);
    }
}
