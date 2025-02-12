package cc.winboll.studio.appbase;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/05 09:54:42
 * @Describe APPbase 应用类
 */
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.SOSCSBroadcastReceiver;
import android.content.IntentFilter;

public class App extends GlobalApplication {

    public static final String TAG = "App";
    
    SOSCSBroadcastReceiver mSOSCSBroadcastReceiver;
    
    @Override
    public void onCreate() {
        super.onCreate();
        GlobalApplication.setIsDebuging(this, BuildConfig.DEBUG);
        mSOSCSBroadcastReceiver = new SOSCSBroadcastReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SOSCSBroadcastReceiver.ACTION_SOS);
        registerReceiver(mSOSCSBroadcastReceiver, intentFilter);
    }
}
