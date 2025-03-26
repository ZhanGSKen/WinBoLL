package cc.winboll.studio.appbase.activities;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/25 11:46:40
 * @Describe 测试窗口2
 */
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import cc.winboll.studio.appbase.R;
import cc.winboll.studio.libappbase.LogView;
import cc.winboll.studio.libappbase.winboll.IWinBollActivity;
import cc.winboll.studio.libappbase.winboll.WinBollActivityManager;

public class New2Activity extends Activity implements IWinBollActivity {

    public static final String TAG = "New2Activity";

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

    public void onNewActivity(View view) {
        WinBollActivityManager.getInstance(this).startWinBollActivity(this, NewActivity.class);
    }
}
