package cc.winboll.studio.winboll.activities;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/03/25 11:46:40
 * @Describe 测试窗口2
 */
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.winboll.R;
import android.app.Activity;

public class New2Activity extends BaseWinBoLLActivity {

	@Override
	public Activity getActivity() {
		return this;
	}


    public static final String TAG = "New2Activity";

    Toolbar mToolbar;

	@Override
	public String getTag() {
		return TAG;
	}

    //LogView mLogView;
	
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new2);

//        mLogView = findViewById(R.id.logview);
//        mLogView.start();
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        
    }


    public void onCloseThisActivity(View view) {
        //WinBoLLActivityManager.getInstance().finish(this);
    }

    public void onCloseAllActivity(View view) {
        //WinBoLLActivityManager.getInstance().finishAll();
    }

    public void onNewActivity(View view) {
        //WinBoLLActivityManager.getInstance().startWinBoLLActivity(this, NewActivity.class);
    }
}
