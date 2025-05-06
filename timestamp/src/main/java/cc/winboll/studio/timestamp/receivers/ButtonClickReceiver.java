package cc.winboll.studio.timestamp.receivers;

/**
 * @Author ZhanGSKen
 * @Date 2025/05/05 11:35
 * @Describe ButtonClickReceiver
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import cc.winboll.studio.libappbase.LogUtils;

public class ButtonClickReceiver extends BroadcastReceiver {

    public static final String TAG = "ButtonClickReceiver";

    public static final String BUTTON_COPYTIMESTAMP_ACTION = "cc.winboll.studio.timestamp.receivers.ButtonClickReceiver.BUTTON_COPYTIMESTAMP_ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.d(TAG, "onReceive");
        //if (intent.getAction().equals(BUTTON_COPYTIMESTAMP_ACTION)) {
            // 在这里编写按钮点击后要执行的代码
            // 比如显示一个Toast
            Toast.makeText(context, "按钮被点击了", Toast.LENGTH_SHORT).show();
        //}
    }

}
