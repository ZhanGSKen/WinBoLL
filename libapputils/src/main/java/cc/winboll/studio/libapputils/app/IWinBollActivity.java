package cc.winboll.studio.libapputils.app;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/02/04 10:20:16
 * @Describe WinBoll Activity 接口
 */
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libapputils.bean.APPInfo;

public interface IWinBollActivity {

    public static final String TAG = "IWinBollActivity";

    // 获取当前具有 IWinBoll 特征的 AppCompatActivity 活动窗口
    AppCompatActivity getActivity();

    abstract public APPInfo getAppInfo();
    abstract public String getTag();
    abstract Toolbar initToolBar();
    abstract boolean isEnableDisplayHomeAsUp();
    abstract boolean isAddWinBollToolBar();
}
