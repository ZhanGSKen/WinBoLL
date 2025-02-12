package cc.winboll.studio.libappbase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/12 23:44:57
 * @Describe 简单信号通信中心接收器
 */
public class SOSCSBroadcastReceiver extends BroadcastReceiver {
    
    public static final String TAG = "SOSCSBroadcastReceiver";
    public static final String ACTION_SOS = SOSCSBroadcastReceiver.class.getName() + ".ACTION_SOS";
    
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(context.getString(R.string.action_sos))) {
            LogUtils.d(TAG, "action_sos");
        } else {
            LogUtils.d(TAG, String.format("action %s", action));
        }
    }
}
