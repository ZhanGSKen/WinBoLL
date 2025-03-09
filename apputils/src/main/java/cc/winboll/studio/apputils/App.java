package cc.winboll.studio.apputils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/12/08 15:10:51
 * @Describe 全局应用类
 */
import android.app.Application;
import android.content.Context;
import android.widget.Toast;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libapputils.app.MyActivityLifecycleCallbacks;
import cc.winboll.studio.libapputils.app.WinBollActivityManager;
import cc.winboll.studio.libapputils.bean.DebugBean;

public class App extends GlobalApplication {

    public static final String TAG = "App";

    public static final String _ACTION_DEBUGVIEW = App.class.getName() + "_ACTION_DEBUGVIEW";

    //static volatile WinBollApplication _WinBollApplication = null;
    MyActivityLifecycleCallbacks mMyActivityLifecycleCallbacks;

    // 标记当前应用是否处于调试状态
    static volatile boolean isDebug = false;

    public synchronized static void setIsDebug(boolean isDebug) {
        App.isDebug = isDebug;
    }

    public static boolean isDebug() {
        return isDebug;
    }

    MyActivityLifecycleCallbacks getMyActivityLifecycleCallbacks() {
        return mMyActivityLifecycleCallbacks;
    }

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
        // 应用环境初始化, 基本调试环境
        //
        // 初始化日志模块
        //LogUtils.init(this);

        try {
            // 初始化 Toast 框架
//            ToastUtils.init(this);
//            // 设置 Toast 布局样式
//            //ToastUtils.setView(R.layout.view_toast);
//            ToastUtils.setStyle(new WhiteToastStyle());
//            ToastUtils.setGravity(Gravity.BOTTOM, 0, 200);
            // 设置应用调试标志
            DebugBean debugBean = DebugBean.loadBean(this, DebugBean.class);
            if (debugBean == null) {
                //ToastUtils.show("debugBean == null");
                setIsDebug(false);
            } else {
                //ToastUtils.show("saveDebugStatus(" + String.valueOf(debugBean.isDebuging()) + ")");
                setIsDebug(debugBean.isDebuging());
            }
            // 应用窗口管理模块参数设置
            //
            mMyActivityLifecycleCallbacks = new MyActivityLifecycleCallbacks();
            registerActivityLifecycleCallbacks(mMyActivityLifecycleCallbacks);
            // 设置默认 WinBoll 应用 UI 类型
            WinBollActivityManager.getInstance(this).setWinBollUI_TYPE(WinBollActivityManager.WinBollUI_TYPE.Service);
            //ToastUtils.show("WinBollUI_TYPE " + getWinBollUI_TYPE());
        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
