package cc.winboll.studio.appbase.widgets;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/15 17:20:46
 * @Describe WidgetButtonClickListener
 */
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;
import cc.winboll.studio.appbase.R;

public class WidgetButtonClickListener extends BroadcastReceiver {
    
    public static final String TAG = "WidgetButtonClickListener";
    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, TimeWidget.class));

        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setTextViewText(R.id.timeTextView, "文本已更新");
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        Toast.makeText(context, "按钮被点击", Toast.LENGTH_SHORT).show();
    }
}

