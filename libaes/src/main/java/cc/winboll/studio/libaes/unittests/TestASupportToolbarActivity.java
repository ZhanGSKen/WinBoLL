package cc.winboll.studio.libaes.unittests;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/16 01:14:00
 * @Describe TestASupportToolbarActivity
 */
import android.app.Activity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libaes.R;
import cc.winboll.studio.libaes.utils.AESThemeUtil;
import cc.winboll.studio.libappbase.winboll.IWinBollActivity;

public class TestASupportToolbarActivity extends AppCompatActivity implements IWinBollActivity  {

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
        AESThemeUtil.applyAppTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testasupporttoolbar);
        Toolbar toolbar = findViewById(R.id.activitytestasupporttoolbarASupportToolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(TAG);

    }

    
}
