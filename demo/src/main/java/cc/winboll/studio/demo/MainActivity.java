package cc.winboll.studio.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libapputils.app.WinBollActivity;
import cc.winboll.studio.libapputils.app.WinBollApplication;
import cc.winboll.studio.libapputils.log.LogUtils;

final public class MainActivity extends WinBollActivity {

	public static final String TAG = "MainActivity";

    public static final int REQUEST_HOME_ACTIVITY = 0;
    public static final int REQUEST_ABOUT_ACTIVITY = 1;

    @Override
    protected boolean isEnableDisplayHomeAsUp() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 设置 WinBoll 应用 UI 类型
        WinBollApplication.setWinBollUI_TYPE(WinBollApplication.WinBollUI_TYPE.Aplication);
        //ToastUtils.show("WinBollUI_TYPE " + WinBollApplication.getWinBollUI_TYPE());
    }

    @Override
    public String getTag() {
        return TAG;
    }


    @Override
    protected boolean isAddWinBollToolBar() {
        return true;
    }

    @Override
    protected Toolbar initToolBar() {
        return findViewById(R.id.activitymainToolbar1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.item_about) {
//            try {
//                WinBollActivity clazzActivity = AboutActivity.class.newInstance();
//                String tag = clazzActivity.getTag();
//                LogUtils.d(TAG, "String tag = clazzActivity.getTag(); tag " + tag);
//                Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
//                startWinBollActivity(intent, tag);
//            } catch (IllegalAccessException e) {} catch (InstantiationException e) {}
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case REQUEST_HOME_ACTIVITY : {
                    LogUtils.d(TAG, "REQUEST_HOME_ACTIVITY");
                    break;
                }
            case REQUEST_ABOUT_ACTIVITY : {
                    LogUtils.d(TAG, "REQUEST_ABOUT_ACTIVITY");
                    break;
                }
            default : {
                    super.onActivityResult(requestCode, resultCode, data);
                }
        }
    }
}
