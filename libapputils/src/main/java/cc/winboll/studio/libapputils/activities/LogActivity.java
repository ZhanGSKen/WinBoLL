package cc.winboll.studio.libapputils.activities;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/08 00:19:39
 * @Describe LogActivity
 */
import cc.winboll.studio.libapputils.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toolbar;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.LogView;
import cc.winboll.studio.libapputils.app.IWinBollActivity;
import cc.winboll.studio.libapputils.bean.APPInfo;

public class LogActivity extends Activity implements IWinBollActivity {

    public static final String TAG = "LogActivity";

    LogView mLogView;

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public APPInfo getAppInfo() {
        return null;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public Toolbar initToolBar() {
        return null;
    }

    @Override
    public boolean isEnableDisplayHomeAsUp() {
        return false;
    }

    @Override
    public boolean isAddWinBollToolBar() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        mLogView = findViewById(R.id.logview);
        mLogView.start();
        
    }

    @Override
    protected void onResume() {
        LogUtils.d(TAG, "onResume");
        super.onResume();
    }
}
