package cc.winboll.studio.gallery;

import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.gallery.utils.BackgroundUtils;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/04/24 15:23
 */
public class GlobalWinBoLLApplication extends GlobalApplication {
    
    public static final String TAG = "GlobalWinBoLLApplication";
    

@Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "onCreate");
        setIsDebugging(BuildConfig.DEBUG);
        //setIsDebugging(false);

        WinBoLLActivityManager.init(this);

        BackgroundUtils.initFromPreferences(this);

        // 初始化 Toast 框架
        ToastUtils.init(this);
        // 设置 Toast 布局样式
        //ToastUtils.setView(R.layout.view_toast);
        //ToastUtils.setStyle(new WhiteToastStyle());
        //ToastUtils.setGravity(Gravity.BOTTOM, 0, 200);

        //CrashHandler.getInstance().registerGlobal(this);
        //CrashHandler.getInstance().registerPart(this);
    }

@Override
    public void onTerminate() {
        super.onTerminate();
        LogUtils.d(TAG, "onTerminate");
        ToastUtils.release();
    }
    
}
