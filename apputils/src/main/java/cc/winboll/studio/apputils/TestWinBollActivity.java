package cc.winboll.studio.apputils;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libapputils.activities.AssetsHtmlActivity;
import cc.winboll.studio.libapputils.app.WinBollActivity;
import cc.winboll.studio.libapputils.app.WinBollActivityManager;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/13 15:09:46
 */
public class TestWinBollActivity extends WinBollActivity {

    public static final String TAG = "TestWinBollActivity";

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected Toolbar initToolBar() {
        return findViewById(R.id.activitytestwinbollToolbar1);
    }

    @Override
    protected boolean isEnableDisplayHomeAsUp() {
        return true;
    }

    @Override
    protected boolean isAddWinBollToolBar() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testwinboll);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setSubTitle(TAG);
    }
    
    
}
