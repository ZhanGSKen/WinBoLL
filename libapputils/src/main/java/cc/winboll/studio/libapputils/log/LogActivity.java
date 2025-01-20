package cc.winboll.studio.libapputils.log;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/08/12 15:07:58
 * @Describe WinBoll 应用日志窗口
 */
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libapputils.R;
import cc.winboll.studio.libapputils.app.WinBollActivity;
import cc.winboll.studio.libapputils.app.WinBollApplication;

public class LogActivity extends WinBollActivity {

    public static final String TAG = "LogActivity";

    LogView mLogView;

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected Toolbar initToolBar() {
        return null;
    }

    @Override
    protected boolean isEnableDisplayHomeAsUp() {
        return false;
    }

    @Override
    protected boolean isAddWinBollToolBar() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        mLogView = findViewById(R.id.logview);

        if (WinBollApplication.isDebug()) { mLogView.start(); }
    }

    @Override
    protected void onResume() {
        LogUtils.d(TAG, "onResume");
        super.onResume();
        mLogView.start();
    }
}
