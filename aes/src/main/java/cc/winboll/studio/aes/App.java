package cc.winboll.studio.aes;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/06/13 19:03:58
 * @Describe AES应用类
 */
import cc.winboll.studio.libappbase.GlobalApplication;
import com.hjq.toast.ToastUtils;


public class App extends GlobalApplication {
    
    public static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtils.init(this);
        ToastUtils.show("App onCreate");
    }
    
}
