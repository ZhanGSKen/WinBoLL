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
import cc.winboll.studio.libappbase.winboll.IWinBoLLActivity;
import cc.winboll.studio.libappbase.winboll.WinBoLLActivityManager;

public class WinBoLLActivityBase extends Activity implements IWinBoLLActivity {

    public static final String TAG = "WinBoLLActivityBase";

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    WinBoLLActivityManager getWinBoLLActivityManager() {
        return WinBoLLActivityManager.getInstance(GlobalApplication.getInstance());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWinBoLLActivityManager().add(this);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWinBoLLActivityManager().registeRemove(this);
    }
}
