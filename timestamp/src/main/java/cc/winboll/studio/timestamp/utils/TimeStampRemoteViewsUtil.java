package cc.winboll.studio.timestamp.utils;

/**
 * @Author ZhanGSKen
 * @Date 2025/05/05 21:10
 * @Describe TimeStampRemoteViewsUtil
 */
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;
import androidx.core.app.NotificationCompat;
import cc.winboll.studio.timestamp.MainActivity;
import cc.winboll.studio.timestamp.R;

public class TimeStampRemoteViewsUtil {

    public static final String TAG = "TimeStampRemoteViewsUtil";

    public static final String CHANNEL_ID = "TimeStampChannel";

    static volatile TimeStampRemoteViewsUtil _TimeStampRemoteViewsUtil;
    Context mContext;
    RemoteViews mRemoteViews;

    TimeStampRemoteViewsUtil(Context context) {
        mContext = context;
        createNotificationChannel();
    }

    public static synchronized TimeStampRemoteViewsUtil getInstance(Context context) {
        if (_TimeStampRemoteViewsUtil == null) {
            _TimeStampRemoteViewsUtil = new TimeStampRemoteViewsUtil(context);
        }
        return _TimeStampRemoteViewsUtil;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "自定义视图通知通道";
            String description = "用于展示自定义视图的通知通道";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showNotification(String msg) {
        if (mRemoteViews == null) {
            // 创建 RemoteViews 对象，加载布局
            mRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.custom_notification_layout);
            // 自定义 TextView 的文本颜色
            mRemoteViews.setTextColor(R.id.custom_text_view, mContext.getResources().getColor(R.color.colorAccent, null));
            // 这里虽然不能直接设置字体大小，但可以通过反射等方式尝试（不推荐，且有兼容性问题）

            // 创建点击通知后的意图
            Intent intent = new Intent(mContext, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // 设置通知的点击事件
            mRemoteViews.setOnClickPendingIntent(R.id.custom_text_view, pendingIntent);

            // 构建通知
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContent(mRemoteViews)
                .setAutoCancel(true);

            // 显示通知
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            notificationManager.notify(1, builder.build());
        }
        // 自定义 TextView 的文本
        mRemoteViews.setTextViewText(R.id.custom_text_view, msg);
    }
}
