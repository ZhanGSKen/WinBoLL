package cc.winboll.studio.libaes.unittests;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2024/07/16 01:14:00
 * @Describe TestASupportToolbarActivity
 */
import android.app.Activity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libaes.R;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libaes.utils.AESThemeUtil;
import cc.winboll.studio.libappbase.LogUtils;

public class TestASupportToolbarActivity extends AppCompatActivity implements IWinBoLLActivity  {

    public static final String TAG = "TestASupportToolbarActivity";

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
        LogUtils.d(TAG, "onCreate() start");
        // 替换此处：原 applyAppTheme -> 新方法 applyAppCompatTheme
        AESThemeUtil.applyAppCompatTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testasupporttoolbar);
        LogUtils.d(TAG, "setContentView() done");
        Toolbar toolbar = findViewById(R.id.activitytestasupporttoolbarASupportToolbar1);
        LogUtils.d(TAG, "findViewById() done, toolbar=" + toolbar.getClass().getSimpleName());
        setSupportActionBar(toolbar);
        LogUtils.d(TAG, "setSupportActionBar() done");
        getSupportActionBar().setTitle(TAG);
        LogUtils.d(TAG, "setTitle() done");
        LogUtils.d(TAG, "onCreate() end");
    }
}

