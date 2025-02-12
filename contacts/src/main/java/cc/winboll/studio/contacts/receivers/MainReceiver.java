package cc.winboll.studio.contacts.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.hjq.toast.ToastUtils;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/13 06:58:04
 * @Describe 主要广播接收器
 */
public class MainReceiver extends BroadcastReceiver {
    
    public static final String TAG = "MainReceiver";
    public static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String szAction = intent.getAction();
        if (szAction.equals(ACTION_BOOT_COMPLETED)) {
            ToastUtils.show("ACTION_BOOT_COMPLETED");
        } else {
            ToastUtils.show("szAction");
        }
    }
    
}
