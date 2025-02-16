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

public class APPSOSReportWidget extends AppWidgetProvider {

    public static final String TAG = "APPSOSReportWidget";

    public static final String ACTION_ADD_SOS_REPORT = "cc.winboll.studio.libappbase.widgets.APPSOSReportWidget.ACTION_ADD_SOS_REPORT";
    public static final String ACTION_RELOAD_SOS_REPORT = "cc.winboll.studio.libappbase.widgets.APPSOSReportWidget.ACTION_RELOAD_SOS_REPORT";

    volatile static ArrayList<String> _Message;
    final static int _MAX_PAGES = 10;
    final static int _OnePageLinesCount = 5;
    volatile static int _CurrentPageIndex = 0;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, "");
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_ADD_SOS_REPORT)) {
            LogUtils.d(TAG, "ACTION_ADD_SOS_REPORT");
            String sosAppName = intent.getStringExtra("appName");
            LogUtils.d(TAG, String.format("sosAppName %s", sosAppName));

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, APPSOSReportWidget.class));
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, sosAppName);
            }
        } else if (intent.getAction().equals(ACTION_RELOAD_SOS_REPORT)) {
            LogUtils.d(TAG, "ACTION_RELOAD_SOS_REPORT");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, APPSOSReportWidget.class));
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, "");
            }
        }

    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, String sosAppName) {
        LogUtils.d(TAG, "updateAppWidget(...)");

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        //设置按钮点击事件
        Intent intentPre = new Intent(context, WidgetButtonClickListener.class);
        intentPre.setAction(WidgetButtonClickListener.ACTION_PRE);
        PendingIntent pendingIntentPre = PendingIntent.getBroadcast(context, 0, intentPre, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_button_pre, pendingIntentPre);
        Intent intentNext = new Intent(context, WidgetButtonClickListener.class);
        intentNext.setAction(WidgetButtonClickListener.ACTION_NEXT);
        PendingIntent pendingIntentNext = PendingIntent.getBroadcast(context, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_button_next, pendingIntentNext);

        // 加入新消息
        if (sosAppName != null && !sosAppName.equals("")) {
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
            while (_Message.size() > _MAX_PAGES * _OnePageLinesCount) { // 控制显示在6行
                _Message.remove(_Message.size() - 1);
            }
        }
        views.setTextViewText(R.id.infoTextView, getPageInfo());
        views.setTextViewText(R.id.sosReportTextView, getMessage());
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static String getMessage() {
        ArrayList<String> msgTemp = new ArrayList<String>();
        if (_Message != null) {
            int start = _OnePageLinesCount * _CurrentPageIndex;
            start = _Message.size() > start ? start : _Message.size() - 1;
            for (int i = start, j = 0; i < _Message.size() && j < _OnePageLinesCount; i++, j++) {
                msgTemp.add(_Message.get(i));
            }
            String message = String.join("\n", msgTemp);
            return message;
        }
        return "";
    }

    public static void prePage(Context context) {
        if (_Message != null) {
            if (_CurrentPageIndex > 0) {
                _CurrentPageIndex = _CurrentPageIndex - 1;
            }
            Intent intentAPPSOSReportWidget = new Intent(context, APPSOSReportWidget.class);
            intentAPPSOSReportWidget.setAction(APPSOSReportWidget.ACTION_RELOAD_SOS_REPORT);
            context.sendBroadcast(intentAPPSOSReportWidget);
        }
    }

    public static void nextPage(Context context) {
        if (_Message != null) {
            if ((_CurrentPageIndex + 1) * _OnePageLinesCount < _Message.size()) {
                _CurrentPageIndex = _CurrentPageIndex + 1;
            }
            Intent intentAPPSOSReportWidget = new Intent(context, APPSOSReportWidget.class);
            intentAPPSOSReportWidget.setAction(APPSOSReportWidget.ACTION_RELOAD_SOS_REPORT);
            context.sendBroadcast(intentAPPSOSReportWidget);
        }
    }

    String getPageInfo() {
        if (_Message == null) {
            return "0/0";
        }
        int leftCount = _Message.size() % _OnePageLinesCount;
        int currentPageCount = _Message.size() / _OnePageLinesCount + (leftCount == 0 ?0: 1);
        return String.format("%d/%d", _CurrentPageIndex + 1, currentPageCount);
    }
}
