package cc.winboll.studio.apputils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/12/08 15:10:51
 * @Describe 全局应用类
 */
import android.app.Application;
import android.view.Gravity;
import cc.winboll.studio.GlobalApplication;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.WhiteToastStyle;
import cc.winboll.studio.libapputils.app.WinBollUtils;

public class App extends GlobalApplication {

    public static final String TAG = "App";

    public static final String _ACTION_DEBUGVIEW = App.class.getName() + "_ACTION_DEBUGVIEW";

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化 WinBoll 框架
        WinBollUtils.init(this);
        // 初始化 Toast 框架
        ToastUtils.init(this);
        // 设置 Toast 布局样式
        //ToastUtils.setView(R.layout.view_toast);
        ToastUtils.setStyle(new WhiteToastStyle());
        ToastUtils.setGravity(Gravity.BOTTOM, 0, 200);
    }

}
