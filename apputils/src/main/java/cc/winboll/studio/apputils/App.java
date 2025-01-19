package cc.winboll.studio.apputils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/12/08 15:10:51
 * @Describe 全局应用类
 */
import android.view.Gravity;
import cc.winboll.studio.libapputils.app.WinBollApplication;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.WhiteToastStyle;

public class App extends WinBollApplication {

    public static final String TAG = "App";

    public static final String _ACTION_DEBUGVIEW = WinBollApplication.class.getName() + "_ACTION_DEBUGVIEW";

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化 Toast 框架
        //
        ToastUtils.init(this);
        // 设置 Toast 布局样式
        //ToastUtils.setView(R.layout.view_toast);
        ToastUtils.setStyle(new WhiteToastStyle());
        ToastUtils.setGravity(Gravity.BOTTOM, 0, 200);
    }

}
