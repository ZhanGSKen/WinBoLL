package cc.winboll.studio.powerbell.utils;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/22 04:39:40
 * @Describe 通知工具类
 */
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.widget.RemoteViews;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import cc.winboll.studio.powerbell.R;

public class NotificationHelper {
    public static final String TAG = "NotificationHelper";

    // 渠道ID和名称
    private static final String CHANNEL_ID_FOREGROUND = "foreground_channel";
    private static final String CHANNEL_NAME_FOREGROUND = "Foreground Service";
    private static final String CHANNEL_ID_TEMPORARY = "temporary_channel";
    private static final String CHANNEL_NAME_TEMPORARY = "Temporary Notifications";

    // 通知ID
    public static final int FOREGROUND_NOTIFICATION_ID = 1001;
    public static final int TEMPORARY_NOTIFICATION_ID = 2001;

    private final Context mContext;
    private final NotificationManager mNotificationManager;

    public NotificationHelper(Context context) {
        mContext = context;
        mNotificationManager = context.getSystemService(NotificationManager.class);
        createNotificationChannels();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createForegroundChannel();
            createTemporaryChannel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createForegroundChannel() {
        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID_FOREGROUND,
            CHANNEL_NAME_FOREGROUND,
            NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Persistent service notifications");
        channel.setSound(null, null);
        channel.enableVibration(false);
        mNotificationManager.createNotificationChannel(channel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createTemporaryChannel() {
        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID_TEMPORARY,
            CHANNEL_NAME_TEMPORARY,
            NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Temporary alert notifications");
        channel.setSound(null, null);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{100, 200, 300, 400});
        channel.setBypassDnd(true);
        mNotificationManager.createNotificationChannel(channel);
    }

    // 显示常驻通知（通常用于前台服务）
    public Notification showForegroundNotification(Intent intent, String title, String content) {
        PendingIntent pendingIntent = createPendingIntent(intent);

        Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID_FOREGROUND)
            .setSmallIcon(R.drawable.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher))
            .setContentTitle(title + "\n" + content)
            //.setContentText(content)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build();

        mNotificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification);
        return notification;
    }

    // 显示临时通知（自动消失）
    public void showTemporaryNotification(Intent intent, String title, String content) {
        PendingIntent pendingIntent = createPendingIntent(intent);

        Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID_TEMPORARY)
            .setSmallIcon(R.drawable.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher))
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(new long[]{100, 200, 300, 400})
            .build();

        mNotificationManager.notify(TEMPORARY_NOTIFICATION_ID, notification);
    }

    // 创建自定义布局通知（可扩展）
    public void showCustomNotification(Intent intent, RemoteViews contentView, RemoteViews bigContentView) {
        PendingIntent pendingIntent = createPendingIntent(intent);

        Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID_TEMPORARY)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentIntent(pendingIntent)
            .setContent(contentView)
            .setCustomBigContentView(bigContentView)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build();

        mNotificationManager.notify(TEMPORARY_NOTIFICATION_ID + 1, notification);
    }

    // 取消所有通知
    public void cancelAllNotifications() {
        mNotificationManager.cancelAll();
    }

    // 创建PendingIntent（兼容不同API版本）
    private PendingIntent createPendingIntent(Intent intent) {
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            flags |= PendingIntent.FLAG_IMMUTABLE;
//        }
        return PendingIntent.getActivity(
            mContext,
            0,
            intent,
            flags
        );
    }
}

