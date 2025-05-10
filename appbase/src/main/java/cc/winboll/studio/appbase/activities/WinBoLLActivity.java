package cc.winboll.studio.appbase.activities;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/05/10 09:48
 * @Describe WinBoLL 窗口基础类
 */
import android.app.Activity;
import cc.winboll.studio.libappbase.winboll.IWinBoLLActivity;

public class WinBoLLActivity extends Activity implements IWinBoLLActivity {

    public static final String TAG = "WinBoLLActivity";

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }
}
