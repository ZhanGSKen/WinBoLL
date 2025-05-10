package cc.winboll.studio.libappbase.winboll;
import android.app.Activity;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/05/10 09:34
 * @Describe WinBoLL 窗口操作接口
 */
public abstract interface IWinBoLLActivity {

    public static final String TAG = "IWinBoLLActivity";

    public static final String ACTION_BIND = IWinBoLLActivity.class.getName() + ".ACTION_BIND";

    public Activity getActivity();
    public String getTag();
}
