package cc.winboll.studio.apputils;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/28 17:11:37
 * @Describe 应用活动窗口基类
 */
import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.winboll.IWinBollActivity;
import cc.winboll.studio.libappbase.winboll.WinBollActivityManager;

public class WinBollActivityBase extends Activity implements IWinBollActivity {

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
