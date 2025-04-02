package cc.winboll.studio.libappbase.winboll;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/24 08:23:40
 * @Describe WinBoll 活动窗口通用接口
 */
import android.app.Activity;
import android.widget.Toolbar;

public interface IWinBollActivity {

    public static final String TAG = "IWinBollActivity";

    // 获取活动窗口
    abstract public Activity getActivity();
    abstract public String getTag();
}
