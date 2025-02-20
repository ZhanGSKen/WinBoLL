package cc.winboll.studio.contacts.widgets;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/17 14:49:31
 * @Describe APPStatusWidget
 */
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.contacts.threads.MainServiceThread;
import cc.winboll.studio.libappbase.LogUtils;
import com.hjq.toast.ToastUtils;

public class APPStatusWidget extends AppWidgetProvider {

    public static final String TAG = "APPSOSReportWidget";

    public static final String ACTION_STATUS_ACTIVE = "cc.winboll.studio.contacts.widgets.APPStatusWidget.ACTION_STATUS_ACTIVE";
    public static final String ACTION_STATUS_NOACTIVE = "cc.winboll.studio.contacts.widgets.APPStatusWidget.ACTION_STATUS_NOACTIVE";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_STATUS_ACTIVE)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, APPStatusWidget.class));
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        } else if (intent.getAction().equals(ACTION_STATUS_NOACTIVE)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, APPStatusWidget.class));
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }

    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        LogUtils.d(TAG, "updateAppWidget(...)");

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        //设置按钮点击事件
        Intent intentAppButton = new Intent(context, APPStatusWidgetClickListener.class);
        intentAppButton.setAction(APPStatusWidgetClickListener.ACTION_APPICON_CLICK);
        PendingIntent pendingIntentAppButton = PendingIntent.getBroadcast(context, 0, intentAppButton, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widgetlayoutImageView1, pendingIntentAppButton);

//        boolean isActive = !MainServiceThread.isExist();
//        if (isActive) {
//            views.setImageViewResource(R.id.widgetlayoutImageView1, R.drawable.ic_launcher);
//        } else {
//            views.setImageViewResource(R.id.widgetlayoutImageView1, R.drawable.ic_launcher_disable);
//
//        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void onAPPStatusWidgetClick(Context context) {
        ToastUtils.show("onAPPStatusWidgetClick");
    }
}
