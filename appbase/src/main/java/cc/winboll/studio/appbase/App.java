package cc.winboll.studio.appbase;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/01/05 09:54:42
 * @Describe APPbase 应用类
 */
import cc.winboll.studio.libappbase.GlobalApplication;
import android.content.IntentFilter;
import cc.winboll.studio.libappbase.sos.SOSCenterServiceReceiver;
import cc.winboll.studio.libappbase.sos.SOS;

public class App extends GlobalApplication {

    public static final String TAG = "App";
    
    SOSCenterServiceReceiver mSOSCenterServiceReceiver;
    
    @Override
    public void onCreate() {
        super.onCreate();
        mSOSCenterServiceReceiver = new SOSCenterServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SOS.ACTION_SOS);
        registerReceiver(mSOSCenterServiceReceiver, intentFilter);
    }
}
