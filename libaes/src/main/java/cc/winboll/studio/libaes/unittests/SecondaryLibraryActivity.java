package cc.winboll.studio.libaes.unittests;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import cc.winboll.studio.libaes.R;
import cc.winboll.studio.libaes.activitys.DrawerFragmentActivity;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/06/15 00:58:10
 * @Describe 第二级窗口
 */
public class SecondaryLibraryActivity extends DrawerFragmentActivity {

    public static final String TAG = "SecondaryLibraryActivity";

    SecondaryLibraryFragment mSecondaryLibraryFragment;

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
            Toast.makeText(getApplication(), "item_test", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
