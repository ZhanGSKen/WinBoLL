package cc.winboll.studio.apputils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/12/08 15:10:51
 * @Describe 全局应用类
 */
import android.app.Application;
import android.content.Context;
import cc.winboll.studio.libappbase.GlobalApplication;

public class App extends GlobalApplication {

    public static final String TAG = "App";

    public static final String _ACTION_DEBUGVIEW = App.class.getName() + "_ACTION_DEBUGVIEW";

    //static volatile WinBollApplication _WinBollApplication = null;
    //MyActivityLifecycleCallbacks mMyActivityLifecycleCallbacks;

    // 标记当前应用是否处于调试状态
    static volatile boolean isDebug = false;

    public synchronized static void setIsDebug(boolean isDebug) {
        App.isDebug = isDebug;
    }

    public static boolean isDebug() {
        return isDebug;
    }

//    MyActivityLifecycleCallbacks getMyActivityLifecycleCallbacks() {
//        return mMyActivityLifecycleCallbacks;
//    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    @Override
    public Application getApplication() {
        return this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
