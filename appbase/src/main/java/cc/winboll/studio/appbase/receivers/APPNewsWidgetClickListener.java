package cc.winboll.studio.appbase.receivers;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/03/24 07:11:44
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cc.winboll.studio.appbase.widgets.APPNewsWidget;
import cc.winboll.studio.libappbase.LogUtils;

public class APPNewsWidgetClickListener extends BroadcastReceiver {

    public static final String TAG = "APPNewsWidgetClickListener";
    public static final String ACTION_PRE = APPNewsWidgetClickListener.class.getName() + ".ACTION_PRE";
    public static final String ACTION_NEXT = APPNewsWidgetClickListener.class.getName() + ".ACTION_NEXT";

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
