package cc.winboll.studio.libaes.unittests;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2024/07/16 01:16:07
 * @Describe TestAToolbarActivity
 */
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toolbar;
import cc.winboll.studio.libaes.R;
import cc.winboll.studio.libaes.utils.AESThemeUtil;

public class TestAToolbarActivity extends Activity {

    public static final String TAG = "TestAToolbarActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 原生Activity 使用 applyTheme
        AESThemeUtil.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testatoolbar);
        Toolbar toolbar = findViewById(R.id.activitytestatoolbarAToolbar1);
        setActionBar(toolbar);
        getActionBar().setTitle(TAG);
    }

}

