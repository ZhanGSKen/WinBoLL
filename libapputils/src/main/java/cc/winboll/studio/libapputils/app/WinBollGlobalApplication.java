package cc.winboll.studio.libapputils.app;

import android.app.Application;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libapputils.bean.DebugBean;
import cc.winboll.studio.libapputils.log.LogUtils;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.WhiteToastStyle;

public class WinBollGlobalApplication extends GlobalApplication {

    public static final String TAG = "WinBollGlobalApplication";

    public static enum WinBollUI_TYPE {
        Aplication, // 退出应用后，保持最近任务栏任务记录主窗口
        Service // 退出应用后，清理所有最近任务栏任务记录窗口
        };

    // 应用类型标志
    volatile static WinBollUI_TYPE _mWinBollUI_TYPE = WinBollUI_TYPE.Service;

    //static volatile WinBollApplication _WinBollApplication = null;
    MyActivityLifecycleCallbacks mMyActivityLifecycleCallbacks;

    // 标记当前应用是否处于调试状态
    static volatile boolean isDebug = false;

    public synchronized static void setIsDebug(boolean isDebug) {
        WinBollGlobalApplication.isDebug = isDebug;
    }

    public static boolean isDebug() {
        return isDebug;
    }

    //
    // 设置 WinBoll 应用 UI 类型
    //
    public synchronized static void setWinBollUI_TYPE(WinBollUI_TYPE mWinBollUI_TYPE) {
        _mWinBollUI_TYPE = mWinBollUI_TYPE;
    }

    //
    // 获取 WinBoll 应用 UI 类型
    //
    public synchronized static WinBollUI_TYPE getWinBollUI_TYPE() {
        return _mWinBollUI_TYPE;
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
        LogUtils.init(this);

        try {
            // 初始化 Toast 框架
            ToastUtils.init(this);
            // 设置 Toast 布局样式
            //ToastUtils.setView(R.layout.view_toast);
            ToastUtils.setStyle(new WhiteToastStyle());
            ToastUtils.setGravity(Gravity.BOTTOM, 0, 200);
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
            setWinBollUI_TYPE(WinBollUI_TYPE.Service);
            //ToastUtils.show("WinBollUI_TYPE " + getWinBollUI_TYPE());
        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
