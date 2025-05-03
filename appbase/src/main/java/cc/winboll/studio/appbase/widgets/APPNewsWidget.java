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
import cc.winboll.studio.appbase.models.WinBoLLNewsBean;
import cc.winboll.studio.appbase.receivers.APPNewsWidgetClickListener;
import cc.winboll.studio.libappbase.AppUtils;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.sos.APPModel;
import cc.winboll.studio.libappbase.sos.WinBoLL;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class APPNewsWidget extends AppWidgetProvider {

    public static final String TAG = "APPNewsWidget";
    
    public static final String ACTION_WAKEUP_SERVICE = APPNewsWidget.class.getName() + ".ACTION_WAKEUP_SERVICE";
    public static final String ACTION_RELOAD_REPORT = APPNewsWidget.class.getName() + ".ACTION_RELOAD_REPORT";


    volatile static ArrayList<WinBoLLNewsBean> _WinBoLLNewsBeanList;
    final static int _MAX_PAGES = 10;
    final static int _OnePageLinesCount = 5;
    volatile static int _CurrentPageIndex = 0;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        initWinBoLLNewsBeanList(context);
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        initWinBoLLNewsBeanList(context);
        if (intent.getAction().equals(ACTION_RELOAD_REPORT)) {
            LogUtils.d(TAG, "ACTION_RELOAD_REPORT");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, APPNewsWidget.class));
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }else if (intent.getAction().equals(ACTION_WAKEUP_SERVICE)) {
            LogUtils.d(TAG, "ACTION_WAKEUP_SERVICE");
            String szAPPModel = intent.getStringExtra(WinBoLL.EXTRA_APPMODEL);
            LogUtils.d(TAG, String.format("szAPPModel %s", szAPPModel));
            if (szAPPModel != null && !szAPPModel.equals("")) {
                try {
                    APPModel bean = APPModel.parseStringToBean(szAPPModel, APPModel.class);
                    if (bean != null) {
                        String szAppPackageName = bean.getAppPackageName();
                        LogUtils.d(TAG, String.format("szAppPackageName %s", szAppPackageName));
                        String szAppMainServiveName = bean.getAppMainServiveName();
                        LogUtils.d(TAG, String.format("szAppMainServiveName %s", szAppMainServiveName));

                        
                        String appName = AppUtils.getAppNameByPackageName(context, szAppPackageName);
                        LogUtils.d(TAG, String.format("appName %s", appName));
                        WinBoLLNewsBean winBollNewsBean = new WinBoLLNewsBean(appName);
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        String currentTime = sdf.format(new Date());
                        StringBuilder sbLine = new StringBuilder();
                        sbLine.append("[");
                        sbLine.append(currentTime);
                        sbLine.append("] Wake up ");
                        sbLine.append(appName);
                        winBollNewsBean.setMessage(sbLine.toString());
                        
                        addWinBoLLNewsBean(context, winBollNewsBean);

                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, APPNewsWidget.class));
                        for (int appWidgetId : appWidgetIds) {
                            updateAppWidget(context, appWidgetManager, appWidgetId);
                        }
                    }
                } catch (IOException e) {
                    LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                }
            }
        }
    }

    //
    // 加入新报告信息
    //
    public synchronized static void addWinBoLLNewsBean(Context context, WinBoLLNewsBean bean) {
        initWinBoLLNewsBeanList(context);
        _WinBoLLNewsBeanList.add(0, bean);
        // 控制记录总数
        while (_WinBoLLNewsBeanList.size() > _MAX_PAGES * _OnePageLinesCount) {
            _WinBoLLNewsBeanList.remove(_WinBoLLNewsBeanList.size() - 1);
        }
        WinBoLLNewsBean.saveBeanList(context, _WinBoLLNewsBeanList, WinBoLLNewsBean.class);
    }

    synchronized static void initWinBoLLNewsBeanList(Context context) {
        if (_WinBoLLNewsBeanList == null) {
            _WinBoLLNewsBeanList = new ArrayList<WinBoLLNewsBean>();
            WinBoLLNewsBean.loadBeanList(context, _WinBoLLNewsBeanList, WinBoLLNewsBean.class);
        }
        if (_WinBoLLNewsBeanList == null) {
            _WinBoLLNewsBeanList = new ArrayList<WinBoLLNewsBean>();
            WinBoLLNewsBean.saveBeanList(context, _WinBoLLNewsBeanList, WinBoLLNewsBean.class);
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        LogUtils.d(TAG, "updateAppWidget(...)");

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_news);
        //设置按钮点击事件
        Intent intentPre = new Intent(context, APPNewsWidgetClickListener.class);
        intentPre.setAction(APPNewsWidgetClickListener.ACTION_PRE);
        PendingIntent pendingIntentPre = PendingIntent.getBroadcast(context, 0, intentPre, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_button_pre, pendingIntentPre);
        Intent intentNext = new Intent(context, APPNewsWidgetClickListener.class);
        intentNext.setAction(APPNewsWidgetClickListener.ACTION_NEXT);
        PendingIntent pendingIntentNext = PendingIntent.getBroadcast(context, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_button_next, pendingIntentNext);

        views.setTextViewText(R.id.tv_msg, getPageInfo());
        views.setTextViewText(R.id.tv_news, getMessage());
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static String getMessage() {
        ArrayList<String> msgTemp = new ArrayList<String>();
        if (_WinBoLLNewsBeanList != null) {
            int start = _OnePageLinesCount * _CurrentPageIndex;
            start = _WinBoLLNewsBeanList.size() > start ? start : _WinBoLLNewsBeanList.size() - 1;
            for (int i = start, j = 0; i < _WinBoLLNewsBeanList.size() && j < _OnePageLinesCount && start > -1; i++, j++) {
                msgTemp.add(_WinBoLLNewsBeanList.get(i).getMessage());
            }
            String message = String.join("\n", msgTemp);
            return message;
        }
        return "";
    }

    public static void prePage(Context context) {
        if (_WinBoLLNewsBeanList != null) {
            if (_CurrentPageIndex > 0) {
                _CurrentPageIndex = _CurrentPageIndex - 1;
            }
            Intent intentWidget = new Intent(context, APPNewsWidget.class);
            intentWidget.setAction(APPNewsWidget.ACTION_RELOAD_REPORT);
            context.sendBroadcast(intentWidget);
        }
    }

    public static void nextPage(Context context) {
        if (_WinBoLLNewsBeanList != null) {
            if ((_CurrentPageIndex + 1) * _OnePageLinesCount < _WinBoLLNewsBeanList.size()) {
                _CurrentPageIndex = _CurrentPageIndex + 1;
            }
            Intent intentWidget = new Intent(context, APPNewsWidget.class);
            intentWidget.setAction(APPNewsWidget.ACTION_RELOAD_REPORT);
            context.sendBroadcast(intentWidget);
        }
    }

    String getPageInfo() {
        if (_WinBoLLNewsBeanList == null) {
            return "0/0";
        }
        int leftCount = _WinBoLLNewsBeanList.size() % _OnePageLinesCount;
        int currentPageCount = _WinBoLLNewsBeanList.size() / _OnePageLinesCount + (leftCount == 0 ?0: 1);
        return String.format("%d/%d", _CurrentPageIndex + 1, currentPageCount);
    }
}
