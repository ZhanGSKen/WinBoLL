package cc.winboll.studio.powerbell;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import cc.winboll.studio.libaes.views.AToolbar;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.LogView;
import cc.winboll.studio.powerbell.MainActivity;
import cc.winboll.studio.powerbell.activities.AboutActivity;
import cc.winboll.studio.powerbell.activities.BackgroundPictureActivity;
import cc.winboll.studio.powerbell.activities.BatteryReporterActivity;
import cc.winboll.studio.powerbell.activities.ClearRecordActivity;
import cc.winboll.studio.powerbell.fragments.MainViewFragment;

public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";
    public static final int BACKGROUND_PICTURE_REQUEST_CODE = 0;

    public static MainActivity _mMainActivity;
    LogView mLogView;
    //ArrayList<Fragment> mlistFragment;
    App mApplication;
    //AppConfigUtils mAppConfigUtils;
    Menu mMenu;
    Fragment mCurrentShowFragment;
    MainViewFragment mMainViewFragment;
    AToolbar mAToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //LogUtils.d(TAG, "onCreate(...)");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 设置调试日志
        mLogView = findViewById(R.id.logview);
        mLogView.start();
        //LogUtils.d(TAG, "LogView Start.");
        mLogView.updateLogView();

        _mMainActivity = MainActivity.this;
        mApplication = (App) getApplication();
        //mAppConfigUtils = AppConfigUtils.getInstance(mApplication);

        // 初始化工具栏
        mAToolbar = (AToolbar) findViewById(R.id.toolbar);
        setActionBar(mAToolbar);
        //mAToolbar.setSubtitle("Main");
        mAToolbar.setTitleTextAppearance(this, R.style.Toolbar_TitleText);

        if (mMainViewFragment == null) {
            FragmentTransaction tx = getFragmentManager().beginTransaction();
            mMainViewFragment = new MainViewFragment();
            tx.add(R.id.activitymainFrameLayout1, mMainViewFragment, MainViewFragment.TAG);
            tx.commit();
        }
        showFragment(mMainViewFragment);

//        NotificationHelper notificationUtils = new NotificationHelper(this);
//        notificationUtils.createNotificationChannels();


    }

    void showFragment(Fragment fragment) {
        FragmentTransaction tx = getFragmentManager().beginTransaction();
        for (Fragment item : getFragmentManager().getFragments()) {
            tx.hide(item);
        }
        tx.show(fragment);
        tx.commit();
        mCurrentShowFragment = fragment;
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        reloadBackground();
    }

    public static void reloadBackground() {
        if (_mMainActivity != null) {
            _mMainActivity.mMainViewFragment.loadBackground();
        }
    }

    /**
     * 通过Uri获取文件在本地存储的真实路径
     */
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.MediaColumns.DATA};
        Cursor cursor=getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToNext()) {
			int nColumnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
			if (nColumnIndex > -1) {
				return cursor.getString(nColumnIndex);
			} else {
				LogUtils.d(TAG, "getRealPathFromURI nColumnIndex is -1.");
			}
        }
        cursor.close();
        return null;
	}

    @Override
    protected void onResume() {
        super.onResume();
        // 回到窗口自动取消提醒消息
        //NotificationHelper.cancelRemindNotification(this);

        reloadBackground();
    }

    // Menu icons are inflated just as they were with actionbar
    //
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        mMenu = menu;
        getMenuInflater().inflate(R.menu.toolbar_main, mMenu);
        return true;
    }

    // 回退开发者选项重设UI到初始用户状态
    //
    /*public void resetUI(MainActivity MainActivity) {
     mMenu.clear();
     getMenuInflater().inflate(R.menu.toolbar_main, mMenu);
     }*/

    @Override 
    public boolean onOptionsItemSelected(MenuItem item) { 
        super.onOptionsItemSelected(item); 
        int menuItemId = item.getItemId();
        if (menuItemId == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        } else if (menuItemId == R.id.action_battery_reporter) {
            Intent intent = new Intent();
            intent.setClass(this, BatteryReporterActivity.class);
            startActivity(intent);
        } else if (menuItemId == R.id.action_clearrecord) {
            Intent intent = new Intent();
            intent.setClass(this, ClearRecordActivity.class);
            startActivity(intent);
        } else if (menuItemId == R.id.action_changepicture) {
            Intent intent = new Intent();
            intent.setClass(this, BackgroundPictureActivity.class);
            startActivity(intent);
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //LogUtils.d(TAG, "onActivityResult(...)");
        //LogUtils.d(TAG, "requestCode is : " + Integer.toString(requestCode));
        //LogUtils.d(TAG, "resultCode is : " + Integer.toString(resultCode));

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BACKGROUND_PICTURE_REQUEST_CODE) {
            // 处理选择后图片
            if (resultCode == RESULT_OK) {
                //mMainViewFragment.loadBackgroundPicture();
                Toast.makeText(getApplication(), "OK", Toast.LENGTH_SHORT).show();
            }
        } else {
            String sz = "Unsolved requestCode = " + Integer.toString(requestCode);
            Toast.makeText(getApplication(), sz, Toast.LENGTH_SHORT).show();
            LogUtils.d(TAG, sz);
        }
    }

    /**
     * 返回键
     */
    @Override
    public void onBackPressed() {
        if (mCurrentShowFragment != mMainViewFragment) {
            showFragment(mMainViewFragment);
        } else {
            moveTaskToBack(true);
        }
    }
} 
