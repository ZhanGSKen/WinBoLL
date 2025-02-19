package cc.winboll.studio.appbase.widgets;
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
import cc.winboll.studio.appbase.R;
import cc.winboll.studio.appbase.beans.SOSReportBean;
import cc.winboll.studio.libappbase.AppUtils;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.bean.APPSOSBean;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SOSWidget extends AppWidgetProvider {

    public static final String TAG = "SOSWidget";

    public static final String ACTION_SOS = "cc.winboll.studio.appbase.widgets.SOSWidget.ACTION_SOS";
    public static final String ACTION_RELOAD_REPORT = "cc.winboll.studio.appbase.widgets.SOSWidget.ACTION_RELOAD_REPORT";


    volatile static ArrayList<SOSReportBean> _SOSReportBeanList;
    final static int _MAX_PAGES = 10;
    final static int _OnePageLinesCount = 5;
    volatile static int _CurrentPageIndex = 0;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        initAPPSOSReportBeanList(context);
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        initAPPSOSReportBeanList(context);
        if (intent.getAction().equals(ACTION_RELOAD_REPORT)) {
            LogUtils.d(TAG, "ACTION_RELOAD_REPORT");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, SOSWidget.class));
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }
    }

    //
    // 加入新报告信息
    //
    public synchronized static void addAPPSOSReportBean(Context context, SOSReportBean bean) {
        initAPPSOSReportBeanList(context);
        _SOSReportBeanList.add(0, bean);
        // 控制记录总数
        while (_SOSReportBeanList.size() > _MAX_PAGES * _OnePageLinesCount) {
            _SOSReportBeanList.remove(_SOSReportBeanList.size() - 1);
        }
        SOSReportBean.saveBeanList(context, _SOSReportBeanList, SOSReportBean.class);
    }

    synchronized static void initAPPSOSReportBeanList(Context context) {
        if (_SOSReportBeanList == null) {
            _SOSReportBeanList = new ArrayList<SOSReportBean>();
            SOSReportBean.loadBeanList(context, _SOSReportBeanList, SOSReportBean.class);
        }
        if (_SOSReportBeanList == null) {
            _SOSReportBeanList = new ArrayList<SOSReportBean>();
            SOSReportBean.saveBeanList(context, _SOSReportBeanList, SOSReportBean.class);
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        LogUtils.d(TAG, "updateAppWidget(...)");

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_sos);
        //设置按钮点击事件
        Intent intentPre = new Intent(context, SOSWidgetClickListener.class);
        intentPre.setAction(SOSWidgetClickListener.ACTION_PRE);
        PendingIntent pendingIntentPre = PendingIntent.getBroadcast(context, 0, intentPre, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_button_pre, pendingIntentPre);
        Intent intentNext = new Intent(context, SOSWidgetClickListener.class);
        intentNext.setAction(SOSWidgetClickListener.ACTION_NEXT);
        PendingIntent pendingIntentNext = PendingIntent.getBroadcast(context, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_button_next, pendingIntentNext);

        views.setTextViewText(R.id.infoTextView, getPageInfo());
        views.setTextViewText(R.id.sosReportTextView, getMessage());
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static String getMessage() {
        ArrayList<String> msgTemp = new ArrayList<String>();
        if (_SOSReportBeanList != null) {
            int start = _OnePageLinesCount * _CurrentPageIndex;
            start = _SOSReportBeanList.size() > start ? start : _SOSReportBeanList.size() - 1;
            for (int i = start, j = 0; i < _SOSReportBeanList.size() && j < _OnePageLinesCount && start > -1; i++, j++) {
                msgTemp.add(_SOSReportBeanList.get(i).getSosReport());
            }
            String message = String.join("\n", msgTemp);
            return message;
        }
        return "";
    }

    public static void prePage(Context context) {
        if (_SOSReportBeanList != null) {
            if (_CurrentPageIndex > 0) {
                _CurrentPageIndex = _CurrentPageIndex - 1;
            }
            Intent intentWidget = new Intent(context, SOSWidget.class);
            intentWidget.setAction(SOSWidget.ACTION_RELOAD_REPORT);
            context.sendBroadcast(intentWidget);
        }
    }

    public static void nextPage(Context context) {
        if (_SOSReportBeanList != null) {
            if ((_CurrentPageIndex + 1) * _OnePageLinesCount < _SOSReportBeanList.size()) {
                _CurrentPageIndex = _CurrentPageIndex + 1;
            }
            Intent intentWidget = new Intent(context, SOSWidget.class);
            intentWidget.setAction(SOSWidget.ACTION_RELOAD_REPORT);
            context.sendBroadcast(intentWidget);
        }
    }

    String getPageInfo() {
        if (_SOSReportBeanList == null) {
            return "0/0";
        }
        int leftCount = _SOSReportBeanList.size() % _OnePageLinesCount;
        int currentPageCount = _SOSReportBeanList.size() / _OnePageLinesCount + (leftCount == 0 ?0: 1);
        return String.format("%d/%d", _CurrentPageIndex + 1, currentPageCount);
    }
}
