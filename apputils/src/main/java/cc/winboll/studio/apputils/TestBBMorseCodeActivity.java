package cc.winboll.studio.apputils;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/23 16:14:45
 */
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toolbar;
import cc.winboll.studio.libapputils.app.IWinBollActivity;
import cc.winboll.studio.libapputils.bean.APPInfo;

public class TestBBMorseCodeActivity extends Activity implements IWinBollActivity {

    public static final String TAG = "TestBBMorseCodeActivity";

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
        return findViewById(R.id.activityteststringtoqrcodeviewToolbar1);
    }

    @Override
    public boolean isEnableDisplayHomeAsUp() {
        return true;
    }

    @Override
    public boolean isAddWinBollToolBar() {
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testbbmorsecode);

    }
}
