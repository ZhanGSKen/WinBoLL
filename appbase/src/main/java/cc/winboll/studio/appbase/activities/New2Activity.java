package cc.winboll.studio.appbase.activities;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/25 11:46:40
 * @Describe 测试窗口2
 */
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;
import cc.winboll.studio.appbase.R;
import cc.winboll.studio.appbase.WinBoLLActivityBase;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.winboll.IWinBoLLActivity;

public class New2Activity extends WinBoLLActivityBase implements IWinBoLLActivity {

    public static final String TAG = "New2Activity";

    Toolbar mToolbar;
    //LogView mLogView;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new2);

//        mLogView = findViewById(R.id.logview);
//        mLogView.start();
        mToolbar = findViewById(R.id.toolbar);
        setActionBar(mToolbar);
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mLogView.start();
    }

    public void onCloseThisActivity(View view) {
        GlobalApplication.getWinBoLLActivityManager().finish(this);
    }

    public void onCloseAllActivity(View view) {
        GlobalApplication.getWinBoLLActivityManager().finishAll();
    }

    public void onNewActivity(View view) {
        GlobalApplication.getWinBoLLActivityManager().startWinBoLLActivity(this, NewActivity.class);
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        getMenuInflater().inflate(R.menu.toolbar_appbase, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == cc.winboll.studio.appbase.R.id.item_log) {
            GlobalApplication.getWinBoLLActivityManager().startLogActivity(this);
            return true;
        }
        // 在switch语句中处理每个ID，并在处理完后返回true，未处理的情况返回false。
        return super.onOptionsItemSelected(item);
    }
}
