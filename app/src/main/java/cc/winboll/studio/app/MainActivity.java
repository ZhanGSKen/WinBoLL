package cc.winboll.studio.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.app.R;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.dialogs.YesNoAlertDialog;
import cc.winboll.studio.libappbase.winboll.IWinBollActivity;
import cc.winboll.studio.libappbase.winboll.WinBollActivityManager;

final public class MainActivity extends WinBollActivity implements IWinBollActivity {

	public static final String TAG = "MainActivity";

    Toolbar mToolbar;

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.d(TAG, "onCreate(Bundle savedInstanceState)");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setSubtitle(TAG);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    void exit() {
        YesNoAlertDialog.OnDialogResultListener listener = new YesNoAlertDialog.OnDialogResultListener(){

            @Override
            public void onYes() {
                App.getWinBollActivityManager().finishAll();
            }

            @Override
            public void onNo() {
            }
        };
        YesNoAlertDialog.show(this, "[ " + getString(R.string.app_name) + " ]", "Exit(Yes/No).\nIs close all activity?", listener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_log) {
            App.getWinBollActivityManager().startLogActivity(this);
        } else if (item.getItemId() == R.id.item_about) {
            App.getWinBollActivityManager().startWinBollActivity(this, AboutActivity.class);
        } else if (item.getItemId() == R.id.item_exit) {
            exit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
