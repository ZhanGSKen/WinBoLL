package cc.winboll.studio.libappbase.sos;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/27 14:04:35
 * @Describe SOSCenterServiceReceiver
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cc.winboll.studio.libappbase.LogUtils;

public class SOSCenterServiceReceiver extends BroadcastReceiver {

    public static final String TAG = "SOSCenterServiceReceiver";

    public static final String ACTION_SOS = SOSCenterServiceReceiver.class.getName() + ".ACTION_SOS";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ACTION_SOS)) {
            // 处理接收到的广播消息
            LogUtils.d(TAG, String.format("Action %s \n%s\n%s", action));
        } else {
            LogUtils.d(TAG, String.format("%s", action));
        }
    }
}
