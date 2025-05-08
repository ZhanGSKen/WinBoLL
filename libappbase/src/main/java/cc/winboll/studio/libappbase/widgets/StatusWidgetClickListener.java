package cc.winboll.studio.libappbase.widgets;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/17 20:33:53
 * @Describe APPWidgetClickListener
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.utils.ToastUtils;

public class StatusWidgetClickListener extends BroadcastReceiver {
    
    public static final String TAG = "APPWidgetClickListener";

    public static final String ACTION_IVAPP = "cc.winboll.studio.libappbase.widgets.StatusWidgetClickListener.ACTION_IVAPP";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            LogUtils.d(TAG, String.format("action %s", action));
            return;
        }
        if (action.equals(ACTION_IVAPP)) {
            ToastUtils.show("ACTION_LAUNCHER");
        } else {
            LogUtils.d(TAG, String.format("action %s", action));
        }
    }
}
