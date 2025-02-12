package cc.winboll.studio.libappbase;


/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/12 11:12:25
 * @Describe 简单信号服务中心
 */
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SimpleOperateSignalCenterService extends Service {

    public static final String TAG = "SimpleOperateSignalCenterService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
