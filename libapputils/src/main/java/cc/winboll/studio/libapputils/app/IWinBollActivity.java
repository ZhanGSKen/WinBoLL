package cc.winboll.studio.libapputils.app;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/08 00:15:36
 * @Describe WinBoll 活动窗口通用接口
 */
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libapputils.bean.APPInfo;

public interface IWinBollActivity {

    public static final String TAG = "IWinBollActivity";

    // 获取当前具有 IWinBoll 特征的 AppCompatActivity 活动窗口
    abstract public AppCompatActivity getActivity();
    abstract public APPInfo getAppInfo();
    abstract public String getTag();
    abstract public Toolbar initToolBar();
    abstract public boolean isEnableDisplayHomeAsUp();
    abstract public boolean isAddWinBollToolBar();
}
