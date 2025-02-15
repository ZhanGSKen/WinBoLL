package cc.winboll.studio.appbase.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/15 20:48:36
 * @Describe TestService
 */
public class TestService extends Service {
    
    public static final String TAG = "TestService";
    
    @Override
    public IBinder onBind(Intent intent) {
        
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "onStartCommand(...)");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy()");
    }
    
    
}
