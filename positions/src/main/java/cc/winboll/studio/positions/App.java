package cc.winboll.studio.positions;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/12/08 15:10:51
 * @Describe 全局应用类
 */
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libapputils.app.WinBollActivityManager;

public class App extends GlobalApplication {

    public static final String TAG = "App";

    @Override
    public void onCreate() {
        // 必须在调用基类前设置应用调试标志，
        // 这样可以预先设置日志与数据的存储根目录。
        setIsDebuging(this, BuildConfig.DEBUG);
        super.onCreate();
        // 设置 WinBoll 应用 UI 类型
        WinBollActivityManager.getInstance(this).setWinBollUI_TYPE(WinBollActivityManager.WinBollUI_TYPE.Aplication);

        
        LogUtils.d(TAG, "onCreate");
    }

}
