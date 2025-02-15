package cc.winboll.studio.libappbase.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.WinBoll;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/15 12:17:32
 * @Describe WinBollReceiver
 */
public class WinBollReceiver extends BroadcastReceiver {
    
    public static final String TAG = "WinBollReceiver";
    
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(WinBoll.ACTION_SOS)) {
            LogUtils.d(TAG, String.format("context.getPackageName() %s", context.getPackageName()));
            LogUtils.d(TAG, String.format("action %s", action));
        } else {
            LogUtils.d(TAG, String.format("action %s", action));
        }
    }
    
}
