package cc.winboll.studio.appbase.activities;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/25 05:04:22
 */
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import cc.winboll.studio.appbase.R;
import cc.winboll.studio.libappbase.winboll.IWinBollActivity;
import cc.winboll.studio.libappbase.winboll.WinBollActivityManager;

public class NewActivity extends AppCompatActivity implements IWinBollActivity {

    public static final String TAG = "NewActivity";

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
        setContentView(R.layout.activity_new);
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
        WinBollActivityManager.getInstance(this).finish(this);
    }

    public void onCloseAllActivity(View view) {
        WinBollActivityManager.getInstance(this).finishAll();
    }

    public void onNew2Activity(View view) {
        WinBollActivityManager.getInstance(this).startWinBollActivity(this, New2Activity.class);
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
            WinBollActivityManager.getInstance(this).startLogActivity(this);
            return true;
        } else if(item.getItemId() == cc.winboll.studio.appbase.R.id.item_minimal) {
            moveTaskToBack(true);
        }
        // 在switch语句中处理每个ID，并在处理完后返回true，未处理的情况返回false。
        return super.onOptionsItemSelected(item);
    }
}
