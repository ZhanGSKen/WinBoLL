package cc.winboll.studio.mj;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/11/28 15:42:47
 * @Describe 全局应用类
 */
 import cc.winboll.studio.shared.app.WinBollApplication;

public class App extends WinBollApplication {
    
    public static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
        setIsDebug(BuildConfig.DEBUG);
    }
}
