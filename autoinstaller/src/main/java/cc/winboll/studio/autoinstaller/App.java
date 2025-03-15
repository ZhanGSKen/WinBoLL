package cc.winboll.studio.autoinstaller;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/04/28 02:39:58
 * @Describe 全局应用类
 */


import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.utils.ToastUtils;

public class App extends GlobalApplication {
    
    public static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
        setIsDebuging(this, BuildConfig.DEBUG);
        // 初始化 Toast 框架
        ToastUtils.init(this);
        // 设置 Toast 布局样式
        //ToastUtils.setView(R.layout.toast_custom_view);
        //ToastUtils.setStyle(new WhiteToastStyle());
        //ToastUtils.setGravity(Gravity.BOTTOM, 0, 200);
    }
}

    
    
    
