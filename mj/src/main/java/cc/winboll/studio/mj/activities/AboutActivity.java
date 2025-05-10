package cc.winboll.studio.mj.activities;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/07/14 13:20:33
 * @Describe 应用关于对话窗口
 */
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.mj.R;
import cc.winboll.studio.shared.app.WinBollActivity;
import cc.winboll.studio.shared.app.WinBollActivityManager;
import com.hjq.toast.ToastUtils;

final public class AboutActivity extends WinBollActivity {

    public static final String TAG = "AboutActivity";

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected boolean isEnableDisplayHomeAsUp() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    @Override
    protected boolean isAddWinBollToolBar() {
        return true;
    }

    @Override
    protected Toolbar initToolBar() {
        return findViewById(R.id.activityaboutToolbar1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_about, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_help) {
            ToastUtils.show("R.id.item_help");
        } else if (item.getItemId() == android.R.id.home) {
            WinBollActivityManager.getInstance(getApplicationContext()).finish(this);
        }
        return super.onOptionsItemSelected(item);
    }
}
