package cc.winboll.studio.appbase.widgets;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/15 14:41:25
 * @Describe TimeWidget
 */
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import cc.winboll.studio.appbase.R;
import cc.winboll.studio.libappbase.LogUtils;
import com.hjq.toast.ToastUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;

public class TimeWidget extends AppWidgetProvider {

    public static final String TAG = "TimeWidget";

    public static final String UPDATE_TIME_ACTION = "com.example.android.UPDATE_TIME";

    volatile static ArrayList<String> _Message;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(UPDATE_TIME_ACTION)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, TimeWidget.class));
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        if (_Message == null) {
            _Message = new ArrayList<String>();
        }
        LogUtils.d(TAG, "updateAppWidget(...)");

        StringBuilder sbLine = new StringBuilder();
//        for (int appWidgetId : appWidgetIds) {
//            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
//

//            appWidgetManager.updateAppWidget(appWidgetId, views);
//        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        //设置按钮点击事件
        Intent intent = new Intent(context, WidgetButtonClickListener.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);

        // 获取当前时间并设置到TextView
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String currentTime = sdf.format(new Date());
        sbLine.append("[");
        sbLine.append(currentTime);
        sbLine.append("]");
        _Message.add(0, sbLine.toString());
        while(_Message.size() > 6) { // 控制显示在6行
            _Message.remove(_Message.size() - 1);
        }
        String message = String.join("\n", _Message);
        views.setTextViewText(R.id.timeTextView, message);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

