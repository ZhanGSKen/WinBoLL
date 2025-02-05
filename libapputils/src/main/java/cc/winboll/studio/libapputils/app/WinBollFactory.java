package cc.winboll.studio.libapputils.app;


/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/02/04 10:19:34
 * @Describe WinBoll 工厂
 */
import androidx.appcompat.app.AppCompatActivity;

public class WinBollFactory {

    public static final String TAG = "WinBollFactory";

    public static AppCompatActivity buildAppCompatActivity(IWinBollActivity iWinBoll) {
        return iWinBoll.getActivity();
    }
}
