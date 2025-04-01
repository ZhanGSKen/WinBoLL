package cc.winboll.studio.app;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/12/08 15:10:51
 * @Describe 全局应用类
 */
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.winboll.WinBollActivityManager;

public class App extends GlobalApplication {

    public static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
        getWinBollActivityManager().setWinBollUI_TYPE(WinBollActivityManager.WinBollUI_TYPE.Service);
    }
}
