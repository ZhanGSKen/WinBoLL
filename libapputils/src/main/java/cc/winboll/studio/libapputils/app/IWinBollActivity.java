package cc.winboll.studio.libapputils.app;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/08 00:15:36
 * @Describe WinBoll 活动窗口通用接口
 */
import android.content.Context;
import android.widget.Toolbar;
import cc.winboll.studio.libapputils.bean.APPInfo;

public interface IWinBollActivity {

    public static final String TAG = "IWinBollActivity";

    // 获取应用资源上下文
    //abstract public Context getApplicationContext();
    abstract public APPInfo getAppInfo();
    abstract public String getTag();
    abstract public Toolbar initToolBar();
    abstract public boolean isEnableDisplayHomeAsUp();
    abstract public boolean isAddWinBollToolBar();
}
