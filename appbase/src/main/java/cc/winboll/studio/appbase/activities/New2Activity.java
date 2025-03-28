package cc.winboll.studio.appbase.activities;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/25 11:46:40
 * @Describe 测试窗口2
 */
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import cc.winboll.studio.appbase.R;
import cc.winboll.studio.appbase.WinBollActivityBase;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.winboll.IWinBollActivity;

public class New2Activity extends WinBollActivityBase implements IWinBollActivity {

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
        setSupportActionBar(mToolbar);
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mLogView.start();
    }

    public void onCloseThisActivity(View view) {
        GlobalApplication.getWinBollActivityManager().finish(this);
    }

    public void onCloseAllActivity(View view) {
        GlobalApplication.getWinBollActivityManager().finishAll();
    }

    public void onNewActivity(View view) {
        GlobalApplication.getWinBollActivityManager().startWinBollActivity(this, NewActivity.class);
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
            GlobalApplication.getWinBollActivityManager().startLogActivity(this);
            return true;
        } else if(item.getItemId() == cc.winboll.studio.appbase.R.id.item_minimal) {
            moveTaskToBack(true);
        }
        // 在switch语句中处理每个ID，并在处理完后返回true，未处理的情况返回false。
        return super.onOptionsItemSelected(item);
    }
}
