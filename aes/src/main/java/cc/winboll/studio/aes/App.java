package cc.winboll.studio.aes;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/06/13 19:03:58
 * @Describe AES应用类
 */
import android.view.Gravity;
import cc.winboll.studio.libappbase.GlobalApplication;
import com.hjq.toast.ToastUtils;


public class App extends GlobalApplication {

    public static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化 Toast 框架
        ToastUtils.init(this);
        // 设置 Toast 布局样式
        ToastUtils.setView(R.layout.view_toast);
        //ToastUtils.setStyle(new WhiteToastStyle());
        ToastUtils.setGravity(Gravity.BOTTOM, 0, 200);

    }

}
