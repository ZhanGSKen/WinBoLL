package cc.winboll.studio.libappbase.widgets;
/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/15 14:41:25
 * @Describe TimeWidget
 */
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TimeWidget extends AppWidgetProvider {

    public static final String TAG = "TimeWidget";

    public static final String UPDATE_TIME_ACTION = "com.example.android.UPDATE_TIME";

    volatile static ArrayList<String> _Message;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, "");
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(UPDATE_TIME_ACTION)) {
            
            String sosAppName = intent.getStringExtra("appName");
            LogUtils.d(TAG, String.format("sosAppName %s", sosAppName));
            
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, TimeWidget.class));
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, sosAppName);
            }
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, String sosAppName) {

        LogUtils.d(TAG, "updateAppWidget(...)");

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        //设置按钮点击事件
        Intent intent = new Intent(context, WidgetButtonClickListener.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
        if (_Message == null) {
            _Message = new ArrayList<String>();
        }
        // 获取当前时间并设置到TextView
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String currentTime = sdf.format(new Date());
        StringBuilder sbLine = new StringBuilder();
        sbLine.append("[");
        sbLine.append(currentTime);
        sbLine.append("] Power to ");
        sbLine.append(sosAppName);
        _Message.add(0, sbLine.toString());
        while (_Message.size() > 6) { // 控制显示在6行
            _Message.remove(_Message.size() - 1);
        }
        if (_Message != null) {
            String message = String.join("\n", _Message);
            views.setTextViewText(R.id.timeTextView, message);
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


}
