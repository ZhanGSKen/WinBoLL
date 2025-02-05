package cc.winboll.studio.libapputils.app;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/02/04 10:20:16
 * @Describe WinBoll Activity 接口
 */
public interface IWinBollActivity {

    public static final String TAG = "IWinBollActivity";

    // 获取当前具有 IWinBoll 特征的 AppCompatActivity 活动窗口
    AppCompatActivity getActivity();
    
    abstract public String getTag();
    abstract Toolbar initToolBar();
    abstract boolean isEnableDisplayHomeAsUp();
    abstract boolean isAddWinBollToolBar();
}
