package cc.winboll.studio.apputils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/12/08 15:10:51
 * @Describe 全局应用类
 */
import android.widget.Toast;
import cc.winboll.studio.libapputils.app.WinBollGlobalApplication;

public class App extends WinBollGlobalApplication {

    public static final String TAG = "App";

    public static final String _ACTION_DEBUGVIEW = App.class.getName() + "_ACTION_DEBUGVIEW";

    @Override
    public void onCreate() {
        super.onCreate();
        //Toast.makeText(getApplication(), "Toast Test", Toast.LENGTH_SHORT).show();
    }
}
