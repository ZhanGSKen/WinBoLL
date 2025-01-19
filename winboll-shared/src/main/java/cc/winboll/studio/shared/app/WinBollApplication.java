package cc.winboll.studio.shared.app;

import android.app.Application;
import android.view.Gravity;
import cc.winboll.studio.shared.bean.DebugBean;
import cc.winboll.studio.shared.log.LogUtils;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.WhiteToastStyle;

public class WinBollApplication extends Application {

    public static final String TAG = "WinBollApplication";

    public static final String _ACTION_DEBUGVIEW = WinBollApplication.class.getName() + "_ACTION_DEBUGVIEW";

    // 保存当前 WINBOLL 应用是否处于调试状态。
    volatile static boolean isDebug;
    static WinBollApplication _WinBollApplication;

    MyActivityLifecycleCallbacks mMyActivityLifecycleCallbacks;

    synchronized public static void setIsDebug(boolean isDebug) {
        WinBollApplication.isDebug = isDebug;
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static enum WinBollUI_TYPE {
        Aplication, // 退出应用后，保持最近任务栏任务记录主窗口
        Service // 退出应用后，清理所有最近任务栏任务记录窗口
        };

    // 应用类型标志
    volatile static WinBollUI_TYPE _mWinBollUI_TYPE = WinBollUI_TYPE.Service;

    //
    // 设置 WinBoll 应用 UI 类型
    //
    public static void setWinBollUI_TYPE(WinBollUI_TYPE mWinBollUI_TYPE) {
        _mWinBollUI_TYPE = mWinBollUI_TYPE;
    }

    //
    // 获取 WinBoll 应用 UI 类型
    //
    public static WinBollUI_TYPE getWinBollUI_TYPE() {
        return _mWinBollUI_TYPE;
    }

    MyActivityLifecycleCallbacks getMyActivityLifecycleCallbacks() {
        return mMyActivityLifecycleCallbacks;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _WinBollApplication = this;
        // 应用环境初始化, 基本调试环境
        //
        CrashHandler.init(this);
        LogUtils.init(this);
        // 初始化 Toast 框架
        ToastUtils.init(this);
        // 设置 Toast 布局样式
        //ToastUtils.setView(R.layout.view_toast);
        ToastUtils.setStyle(new WhiteToastStyle());
        ToastUtils.setGravity(Gravity.BOTTOM, 0, 200);

        DebugBean debugBean = DebugBean.loadBean(this, DebugBean.class);
        if (debugBean == null) {
            //ToastUtils.show("debugBean == null");
            setIsDebug(false);
        } else {
            //ToastUtils.show("saveDebugStatus(" + String.valueOf(debugBean.isDebuging()) + ")");
            setIsDebug(debugBean.isDebuging());
        }

        // 应用运行状态环境设置
        //
        mMyActivityLifecycleCallbacks = new MyActivityLifecycleCallbacks(this);
        registerActivityLifecycleCallbacks(mMyActivityLifecycleCallbacks);
        // 设置默认 WinBoll 应用 UI 类型
        setWinBollUI_TYPE(WinBollUI_TYPE.Service);
        //ToastUtils.show("WinBollUI_TYPE " + getWinBollUI_TYPE());
    }
}
