package cc.winboll.studio.watchoutputinstaller;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/04/28 02:39:58
 * @Describe 全局应用类
 */
import android.view.Gravity;
import com.hjq.toast.ToastUtils;
import cc.winboll.studio.shared.app.WinBollApplication;

public class App extends WinBollApplication {
    
    public static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
        setIsDebug(BuildConfig.DEBUG);
        // 初始化 Toast 框架
        ToastUtils.init(this);
        // 设置 Toast 布局样式
        ToastUtils.setView(R.layout.toast_custom_view);
        //ToastUtils.setStyle(new WhiteToastStyle());
        ToastUtils.setGravity(Gravity.BOTTOM, 0, 200);
    }
}

    
    
    
