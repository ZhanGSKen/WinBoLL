package cc.winboll.studio.mymessagemanager.services;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/07/19 14:48:01
 * @Describe 默认短信应用服务组件类
 *           注册安卓系统默认短信应用使用。
 */
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DefaultSMSManagerService extends Service {

    public static final String TAG = "DefaultSMSManagerService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
