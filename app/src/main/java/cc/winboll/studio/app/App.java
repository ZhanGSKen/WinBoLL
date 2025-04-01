package cc.winboll.studio.app;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/12/08 15:10:51
 * @Describe 全局应用类
 */
import android.view.Gravity;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.winboll.WinBollActivityManager;
import com.hjq.toast.ToastUtils;

public class App extends GlobalApplication {

    public static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化 Toast 框架
        ToastUtils.init(this);
        // 设置 Toast 布局样式
        //ToastUtils.setView(R.layout.toast_custom_view);
        //ToastUtils.setStyle(new WhiteToastStyle());
        ToastUtils.setGravity(Gravity.BOTTOM, 0, 200);

        getWinBollActivityManager().setWinBollUI_TYPE(WinBollActivityManager.WinBollUI_TYPE.Service);
    }
}
