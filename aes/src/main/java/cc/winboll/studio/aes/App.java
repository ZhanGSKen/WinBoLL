package cc.winboll.studio.aes;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2024/06/13 19:03:58
 * @Describe AES应用类
 */
import android.view.Gravity;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.ToastUtils;


public class App extends GlobalApplication {

    public static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
		setIsDebugging(BuildConfig.DEBUG);
		//setIsDebugging(false);
		WinBoLLActivityManager.init(this);

        // 初始化 Toast 框架
        ToastUtils.init(this);
    }

	@Override
	public void onTerminate() {
		super.onTerminate();
		ToastUtils.release();
	}
}
