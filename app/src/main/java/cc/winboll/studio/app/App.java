package cc.winboll.studio.app;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/12/08 15:10:51
 * @Describe 全局应用类
 */
import cc.winboll.studio.shared.app.WinBollApplication;
import cc.winboll.studio.shared.log.LogUtils;

public class App extends WinBollApplication {

    public static final String TAG = "App";

    @Override
    public void onCreate() {
        // 必须在调用基类前设置应用调试标志，
        // 这样可以预先设置日志与数据的存储根目录。
        //setIsDebug(BuildConfig.DEBUG);
        super.onCreate();
        LogUtils.d(TAG, "onCreate");
    }

}
