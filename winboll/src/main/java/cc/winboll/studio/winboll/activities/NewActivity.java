package cc.winboll.studio.winboll.activities;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/03/25 05:04:22
 * @Describe
 */
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.winboll.R;
import android.app.Activity;

public class NewActivity extends BaseWinBoLLActivity {

	@Override
	public Activity getActivity() {
		return this;
	}


    public static final String TAG = "NewActivity";

    Toolbar mToolbar;
    //LogView mLogView;

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


    public void onCloseThisActivity(View view) {
        WinBoLLActivityManager.getInstance().finish(this);
    }

    public void onCloseAllActivity(View view) {
        WinBoLLActivityManager.getInstance().finishAll();
    }

    public void onNew2Activity(View view) {
       // WinBoLLActivityManager.getInstance().startWinBoLLActivity(App.getInstance(), New2Activity.class);
    }
}
