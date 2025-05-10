package cc.winboll.studio.apputils;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/12/08 15:10:51
 * @Describe 全局应用类
 */
import android.app.Application;
import android.content.Context;
import cc.winboll.studio.libappbase.GlobalApplication;

public class App extends GlobalApplication {

    public static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
