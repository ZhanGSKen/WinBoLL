package cc.winboll.studio.libappbase.widgets;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/02/17 20:32:12
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
import cc.winboll.studio.libappbase.utils.ServiceUtils;
import android.content.ServiceConnection;
import android.os.IBinder;
import cc.winboll.studio.libappbase.utils.ToastUtils;

public class StatusWidget extends AppWidgetProvider {

    public static final String TAG = "StatusWidget";

    public static final String ACTION_STATUS_UPDATE = "cc.winboll.studio.libappbase.widgets.APPWidget.ACTION_STATUS_UPDATE";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_STATUS_UPDATE)) {
            ToastUtils.show("Test");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, StatusWidget.class));
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_status);
        //设置按钮点击事件
        Intent intentAppButton = new Intent(context, StatusWidgetClickListener.class);
        intentAppButton.setAction(StatusWidgetClickListener.ACTION_IVAPP);
        PendingIntent pendingIntentAppButton = PendingIntent.getBroadcast(context, 0, intentAppButton, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.ivapp, pendingIntentAppButton);

//        boolean isActive = ServiceUtils.isServiceRunning(context, TestService.class.getName());
//        if (isActive) {
//            views.setImageViewResource(R.id.ivapp, cc.winboll.studio.libappbase.R.drawable.ic_launcher);
//        } else {
//            views.setImageViewResource(R.id.ivapp, cc.winboll.studio.libappbase.R.drawable.ic_launcher_disable);
//        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
