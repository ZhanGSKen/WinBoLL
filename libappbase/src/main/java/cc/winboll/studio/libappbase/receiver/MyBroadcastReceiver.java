package cc.winboll.studio.libappbase.receiver;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/13 21:19:09
 * @Describe MyBroadcastReceiver
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.R;

public class MyBroadcastReceiver extends BroadcastReceiver {
    
    public static final String TAG = "MyBroadcastReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context.getString(R.string.action_sos).equals(intent.getAction())) {
            String message = intent.getStringExtra("message");
            String sosPackage = intent.getStringExtra("sosPackage");
            
            // 处理接收到的广播消息
            LogUtils.d(TAG, String.format("MyBroadcastReceiver action %s \n%s\n%s", intent.getAction(), sosPackage, message));
        }
    }
}

