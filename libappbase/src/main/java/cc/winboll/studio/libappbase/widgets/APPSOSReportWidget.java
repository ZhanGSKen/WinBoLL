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
import cc.winboll.studio.libappbase.bean.APPSOSReportBean;
import java.io.IOException;

public class APPSOSReportWidget extends AppWidgetProvider {

    public static final String TAG = "APPSOSReportWidget";

    public static final String ACTION_ADD_SOS_REPORT = "cc.winboll.studio.libappbase.widgets.APPSOSReportWidget.ACTION_ADD_SOS_REPORT";
    public static final String ACTION_RELOAD_SOS_REPORT = "cc.winboll.studio.libappbase.widgets.APPSOSReportWidget.ACTION_RELOAD_SOS_REPORT";

    volatile static ArrayList<APPSOSReportBean> _APPSOSReportBeanList;
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
        if (intent.getAction().equals(ACTION_ADD_SOS_REPORT)) {
            LogUtils.d(TAG, "ACTION_ADD_SOS_REPORT");
            String szAPPSOSReportBean = intent.getStringExtra("APPSOSReportBean");
            LogUtils.d(TAG, String.format("szAPPSOSBean %s", szAPPSOSReportBean));
            if (szAPPSOSReportBean != null && !szAPPSOSReportBean.equals("")) {
                try {
                    APPSOSReportBean bean = APPSOSReportBean.parseStringToBean(szAPPSOSReportBean, APPSOSReportBean.class);
                    if (bean != null) {
                        addAPPSOSReportBean(context, bean);
                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, APPSOSReportWidget.class));
                        for (int appWidgetId : appWidgetIds) {
                            updateAppWidget(context, appWidgetManager, appWidgetId);
                        }
                    }
                } catch (IOException e) {
                    LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                }
            }
        } else if (intent.getAction().equals(ACTION_RELOAD_SOS_REPORT)) {
            LogUtils.d(TAG, "ACTION_RELOAD_SOS_REPORT");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, APPSOSReportWidget.class));
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }

    }

    //
    // 加入新报告信息
    //
    void addAPPSOSReportBean(Context context, APPSOSReportBean bean) {
        initAPPSOSReportBeanList(context);
        _APPSOSReportBeanList.add(0, bean);
        // 控制记录总数
        while (_APPSOSReportBeanList.size() > _MAX_PAGES * _OnePageLinesCount) {
            _APPSOSReportBeanList.remove(_APPSOSReportBeanList.size() - 1);
        }
        APPSOSReportBean.saveBeanList(context, _APPSOSReportBeanList, APPSOSReportBean.class);
    }

    void initAPPSOSReportBeanList(Context context) {
        if (_APPSOSReportBeanList == null) {
            _APPSOSReportBeanList = new ArrayList<APPSOSReportBean>();
            APPSOSReportBean.loadBeanList(context, _APPSOSReportBeanList, APPSOSReportBean.class);
        }
        if (_APPSOSReportBeanList == null) {
            _APPSOSReportBeanList = new ArrayList<APPSOSReportBean>();
            APPSOSReportBean.saveBeanList(context, _APPSOSReportBeanList, APPSOSReportBean.class);
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
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

        views.setTextViewText(R.id.infoTextView, getPageInfo());
        views.setTextViewText(R.id.sosReportTextView, getMessage());
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static String getMessage() {
        ArrayList<String> msgTemp = new ArrayList<String>();
        if (_APPSOSReportBeanList != null) {
            int start = _OnePageLinesCount * _CurrentPageIndex;
            start = _APPSOSReportBeanList.size() > start ? start : _APPSOSReportBeanList.size() - 1;
            for (int i = start, j = 0; i < _APPSOSReportBeanList.size() && j < _OnePageLinesCount; i++, j++) {
                msgTemp.add(_APPSOSReportBeanList.get(i).getSosReport());
            }
            String message = String.join("\n", msgTemp);
            return message;
        }
        return "";
    }

    public static void prePage(Context context) {
        if (_APPSOSReportBeanList != null) {
            if (_CurrentPageIndex > 0) {
                _CurrentPageIndex = _CurrentPageIndex - 1;
            }
            Intent intentAPPSOSReportWidget = new Intent(context, APPSOSReportWidget.class);
            intentAPPSOSReportWidget.setAction(APPSOSReportWidget.ACTION_RELOAD_SOS_REPORT);
            context.sendBroadcast(intentAPPSOSReportWidget);
        }
    }

    public static void nextPage(Context context) {
        if (_APPSOSReportBeanList != null) {
            if ((_CurrentPageIndex + 1) * _OnePageLinesCount < _APPSOSReportBeanList.size()) {
                _CurrentPageIndex = _CurrentPageIndex + 1;
            }
            Intent intentAPPSOSReportWidget = new Intent(context, APPSOSReportWidget.class);
            intentAPPSOSReportWidget.setAction(APPSOSReportWidget.ACTION_RELOAD_SOS_REPORT);
            context.sendBroadcast(intentAPPSOSReportWidget);
        }
    }

    String getPageInfo() {
        if (_APPSOSReportBeanList == null) {
            return "0/0";
        }
        int leftCount = _APPSOSReportBeanList.size() % _OnePageLinesCount;
        int currentPageCount = _APPSOSReportBeanList.size() / _OnePageLinesCount + (leftCount == 0 ?0: 1);
        return String.format("%d/%d", _CurrentPageIndex + 1, currentPageCount);
    }
}
