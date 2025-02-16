package cc.winboll.studio.libappbase.widgets;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/15 17:20:46
 * @Describe WidgetButtonClickListener
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cc.winboll.studio.libappbase.LogUtils;

public class WidgetButtonClickListener extends BroadcastReceiver {

    public static final String TAG = "WidgetButtonClickListener";
    public static final String ACTION_PRE = "cc.winboll.studio.libappbase.widgets.WidgetButtonClickListener.ACTION_PRE";
    public static final String ACTION_NEXT = "cc.winboll.studio.libappbase.widgets.WidgetButtonClickListener.ACTION_NEXT";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            LogUtils.d(TAG, String.format("action %s", action));
            return;
        }
        if (action.equals(ACTION_PRE)) {
            LogUtils.d(TAG, "ACTION_PRE");
            APPSOSReportWidget.prePage(context);
        } else if (action.equals(ACTION_NEXT)) {
            LogUtils.d(TAG, "ACTION_NEXT");
            APPSOSReportWidget.nextPage(context);
        } else {
            LogUtils.d(TAG, String.format("action %s", action));
        }
    }
}
