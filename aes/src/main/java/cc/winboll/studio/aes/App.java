package cc.winboll.studio.aes;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2024/06/13 19:03:58
 * @Describe AES应用类
 */
import cc.winboll.studio.libaes.utils.AESThemeUtil;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.libappbase.CrashActivity;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.libappbase.utils.CrashHandleNotifyUtils;
import java.io.PrintWriter;
import java.io.StringWriter;


public class App extends GlobalApplication {

    public static final String TAG = "App";

    @Override
    public void onCreate() {
		try {
			super.onCreate();
			ToastUtils.init(this);
			WinBoLLActivityManager.init(this);
			AESThemeUtil.init(null);
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
