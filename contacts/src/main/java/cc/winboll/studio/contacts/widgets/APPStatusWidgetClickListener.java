package cc.winboll.studio.contacts.widgets;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/17 14:59:55
 * @Describe WidgetButtonClickListener
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import cc.winboll.studio.libappbase.LogUtils;

public class APPStatusWidgetClickListener extends BroadcastReceiver {

    public static final String TAG = "APPStatusWidgetClickListener";

    public static final String ACTION_APPICON_CLICK = "cc.winboll.studio.contacts.widgets.APPStatusWidgetClickListener.ACTION_APPICON_CLICK";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            LogUtils.d(TAG, String.format("action %s", action));
            return;
        }
        if (action.equals(ACTION_APPICON_CLICK)) {
            LogUtils.d(TAG, "ACTION_APPICON_CLICK");
            Toast.makeText(context, "ACTION_APPICON_CLICK", Toast.LENGTH_SHORT).show();
        }
    }
}
