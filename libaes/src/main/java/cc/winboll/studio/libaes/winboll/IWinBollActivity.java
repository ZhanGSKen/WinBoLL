package cc.winboll.studio.libaes.winboll;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/24 08:23:40
 * @Describe WinBoll 活动窗口通用接口
 */
import android.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

public interface IWinBollActivity {

    public static final String TAG = "IWinBollActivity";

    // 获取应用资源上下文
    abstract public AppCompatActivity getActivity();
    abstract public String getTag();
    abstract public Toolbar initToolBar();
    abstract public boolean isEnableDisplayHomeAsUp();
    abstract public boolean isAddWinBollToolBar();
}
