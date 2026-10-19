package cc.winboll.studio.contacts;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2024/12/08 15:10:51
 * @Describe 全局应用类
 */
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.ToastUtils;

public class App extends GlobalApplication {

    public static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
		// 设置应用调试标志
        setIsDebugging(BuildConfig.DEBUG);

		// 初始化窗口管理类
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
