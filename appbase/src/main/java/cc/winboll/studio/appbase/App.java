package cc.winboll.studio.appbase;

import cc.winboll.studio.libappbase.CrashHandler;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.libappbase.utils.CrashHandleNotifyUtils;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/01/05 09:54:42
 * @Describe 应用全局入口类（继承基础库 GlobalApplication）
 * 负责应用初始化、全局资源管理与生命周期回调处理，是整个应用的核心入口
 */
public class App extends GlobalApplication {

    /** 当前应用类的日志 TAG（用于调试输出，标识日志来源） */
    public static final String TAG = "App";

    /**
     * 应用创建时回调（全局初始化入口）
     * 在应用进程启动时执行，仅调用一次，用于初始化全局工具类、第三方库等
     */
    @Override
    public void onCreate() {
		try {
			super.onCreate();
			
			// 初始化 Toast 工具类（传入应用全局上下文，确保 Toast 可在任意地方调用）
			ToastUtils.init(getApplicationContext());
		} catch (Throwable e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.close();
            String stackTraceStr = sw.toString();
            CrashHandleNotifyUtils.handleUncaughtException(
                this,
                getPackageName(),
                stackTraceStr,
                CrashHandler.CrashActivity.class
            );
        }
    }

    /**
     * 应用终止时回调（资源释放入口）
     * 仅在模拟环境（如 Android Studio 模拟器）中可靠触发，真机上可能因系统回收进程不执行
     * 用于释放全局资源，避免内存泄漏
     */
    @Override
    public void onTerminate() {
        super.onTerminate(); // 调用父类终止逻辑（如基础库资源释放）
        // 释放 Toast 工具类资源（销毁全局 Toast 实例，避免内存泄漏）
        ToastUtils.release();
    }
}

