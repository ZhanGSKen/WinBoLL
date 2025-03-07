package cc.winboll.studio.appbase.widgets;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/15 17:20:46
 * @Describe WidgetButtonClickListener
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cc.winboll.studio.libappbase.LogUtils;

public class WinBollNewsWidgetClickListener extends BroadcastReceiver {

    public static final String TAG = "WinBollNewsWidgetClickListener";
    public static final String ACTION_PRE = "cc.winboll.studio.appbase.widgets.WinBollNewsWidgetClickListener.ACTION_PRE";
    public static final String ACTION_NEXT = "cc.winboll.studio.appbase.widgets.WinBollNewsWidgetClickListener.ACTION_NEXT";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            LogUtils.d(TAG, String.format("action %s", action));
            return;
        }
        if (action.equals(ACTION_PRE)) {
            LogUtils.d(TAG, "ACTION_PRE");
            APPNewsWidget.prePage(context);
        } else if (action.equals(ACTION_NEXT)) {
            LogUtils.d(TAG, "ACTION_NEXT");
            APPNewsWidget.nextPage(context);
        } else {
            LogUtils.d(TAG, String.format("action %s", action));
        }
    }
}
