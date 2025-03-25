package cc.winboll.studio.libappbase.winboll;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/25 20:34:47
 * @Describe 应用日志窗口
 */
import android.app.Activity;
import android.os.Bundle;
import cc.winboll.studio.libappbase.LogView;
import cc.winboll.studio.libappbase.R;

public class LogActivity extends Activity implements IWinBollActivity {

    public static final String TAG = "LogActivity";

    LogView mLogView;

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
        setContentView(R.layout.activity_log);
        //ToastUtils.show("LogActivity onCreate");

        mLogView = findViewById(R.id.logview);
        mLogView.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLogView.start();
    }
}
