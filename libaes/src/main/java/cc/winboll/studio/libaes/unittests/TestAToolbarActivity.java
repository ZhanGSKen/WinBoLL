package cc.winboll.studio.libaes.unittests;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/16 01:16:07
 * @Describe TestAToolbarActivity
 */
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toolbar;
import cc.winboll.studio.libaes.R;
import cc.winboll.studio.libaes.utils.AESThemeUtil;
import cc.winboll.studio.libappbase.winboll.IWinBoLLActivity;

public class TestAToolbarActivity extends Activity implements IWinBoLLActivity {

    public static final String TAG = "TestAToolbarActivity";
    
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
        setContentView(R.layout.activity_testatoolbar);
        Toolbar toolbar = findViewById(R.id.activitytestatoolbarAToolbar1);
        setActionBar(toolbar);
        getActionBar().setTitle(TAG);
    }

}
