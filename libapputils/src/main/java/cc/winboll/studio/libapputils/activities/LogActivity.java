package cc.winboll.studio.libapputils.activities;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/08 00:19:39
 * @Describe LogActivity
 */
import android.app.Activity;
import android.os.Bundle;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.LogView;
import cc.winboll.studio.libapputils.R;

public class LogActivity extends Activity {

    public static final String TAG = "LogActivity";

    LogView mLogView;
//
//    @Override
//    public Activity getActivity() {
//        return this;
//    }

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
