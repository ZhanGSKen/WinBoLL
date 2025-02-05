package cc.winboll.studio.libapputils.log;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/08/12 15:07:58
 * @Describe WinBoll 应用日志窗口
 */
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libapputils.R;
import cc.winboll.studio.libapputils.app.IWinBollActivity;

public class LogActivity extends AppCompatActivity implements IWinBollActivity {

    public static final String TAG = "LogActivity";

    LogView mLogView;
    
    @Override
    public AppCompatActivity getActivity() {
        return this;
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

        if (GlobalApplication.isDebuging()) { mLogView.start(); }
    }

    @Override
    protected void onResume() {
        LogUtils.d(TAG, "onResume");
        super.onResume();
        mLogView.start();
    }
}
