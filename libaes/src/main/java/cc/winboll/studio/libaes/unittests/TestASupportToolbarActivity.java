package cc.winboll.studio.libaes.unittests;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/16 01:14:00
 * @Describe TestASupportToolbarActivity
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libaes.R;
import cc.winboll.studio.libaes.beans.AESThemeBean;
import cc.winboll.studio.libaes.utils.AESThemeUtil;

public class TestASupportToolbarActivity extends AppCompatActivity {

    public static final String TAG = "TestASupportToolbarActivity";

    
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
