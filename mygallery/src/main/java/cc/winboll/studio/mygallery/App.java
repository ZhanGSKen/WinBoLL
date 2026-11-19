package cc.winboll.studio.mygallery;

import cc.winboll.studio.mygallery.utils.BackgroundUtils;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.libappbase.CrashActivity;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.libappbase.utils.CrashHandleNotifyUtils;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/07/19
 * @Describe 应用全局入口类（继承 GlobalWinBoLLApplication）
 * 负责应用初始化、全局资源管理与生命周期回调处理，是整个应用的核心入口
 */
public class App extends GlobalApplication {

    public static final String TAG = "App";

    @Override
    public void onCreate() {
        try {
            super.onCreate();
			
			WinBoLLActivityManager.init(this);

			BackgroundUtils.initFromPreferences(this);

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
                CrashActivity.class
            );
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ToastUtils.release();
    }
}
