package cc.winboll.studio.libappbase.winboll;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/28 13:35:28
 * @Describe WinBoll应用窗口基类
 */
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.R;

public class WinBollActivityBase extends AppCompatActivity implements IWinBollActivity {

    public static final String TAG = "WinBollActivityBase";

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    WinBollActivityManager getWinBollActivityManager() {
        return WinBollActivityManager.getInstance(GlobalApplication.getInstance());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWinBollActivityManager().add(this);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWinBollActivityManager().registeRemove(this);
    }
}
