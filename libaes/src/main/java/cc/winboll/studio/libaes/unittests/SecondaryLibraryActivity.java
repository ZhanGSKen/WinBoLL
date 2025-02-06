package cc.winboll.studio.libaes.unittests;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import cc.winboll.studio.libaes.R;
import cc.winboll.studio.libaes.activitys.DrawerFragmentActivity;
import cc.winboll.studio.libapputils.bean.APPInfo;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/06/15 00:58:10
 * @Describe 第二级窗口
 */
public class SecondaryLibraryActivity extends DrawerFragmentActivity {

    public static final String TAG = "SecondaryLibraryActivity";

    SecondaryLibraryFragment mSecondaryLibraryFragment;

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
        return null;
    }

    @Override
    public Toolbar initToolBar() {
        return null;
    }

    @Override
    public boolean isAddWinBollToolBar() {
        return false;
    }

    @Override
    public boolean isEnableDisplayHomeAsUp() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mSecondaryLibraryFragment == null) {
            mSecondaryLibraryFragment = new SecondaryLibraryFragment();
            addFragment(mSecondaryLibraryFragment);
        }
        showFragment(mSecondaryLibraryFragment);
    }

    @Override
    public DrawerFragmentActivity.ActivityType initActivityType() {
        return DrawerFragmentActivity.ActivityType.Secondary;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_secondarylibrary, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int nItemId = item.getItemId();
        if (nItemId == R.id.item_test) {
            Toast.makeText(getApplicationContext(), "item_test", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
