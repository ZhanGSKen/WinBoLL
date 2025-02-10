package cc.winboll.studio.appbase;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/05 09:54:42
 * @Describe APPbase 应用类
 */
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogUtils;

public class App extends GlobalApplication {

    public static final String TAG = "App";

    @Override
    public void onCreate() {
        GlobalApplication.setIsDebuging(this, BuildConfig.DEBUG);
        super.onCreate();
        LogUtils.setLogLevel(LogUtils.LOG_LEVEL.Debug);
        LogUtils.setALlTAGListEnable(true);
        LogUtils.d(TAG, "LogUtils init");
    }
}
