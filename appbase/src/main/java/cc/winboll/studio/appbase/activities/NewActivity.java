package cc.winboll.studio.appbase.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import cc.winboll.studio.appbase.R;
import cc.winboll.studio.libappbase.winboll.IWinBollActivity;
import cc.winboll.studio.libappbase.winboll.WinBollActivityManager;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/25 05:04:22
 */
public class NewActivity extends Activity implements IWinBollActivity {

    public static final String TAG = "NewActivity";

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
    }

    public void onCloseThisActivity(View view) {
        WinBollActivityManager.getInstance(this).finish(this);
    }
    
    public void onCloseAllActivity(View view) {
        WinBollActivityManager.getInstance(this).finishAll();
    }
}
