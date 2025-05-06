package cc.winboll.studio.timestamp.utils;

/**
 * @Author ZhanGSKen
 * @Date 2025/05/05 10:36
 * @Describe 应用通知工具类
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
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.timestamp.R;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // 示例：维护当前使用的渠道ID列表
    // 键：渠道ID，值：渠道重要性级别
    Map<String, Integer> activeChannelConfigs = new HashMap<>();

    public NotificationHelper(Context context) {
        mContext = context;
        mNotificationManager = context.getSystemService(NotificationManager.class);

        // 初始化配置
        activeChannelConfigs.put(
            CHANNEL_ID_FOREGROUND,
            NotificationManager.IMPORTANCE_HIGH
        );
        activeChannelConfigs.put(
            CHANNEL_ID_TEMPORARY,
            NotificationManager.IMPORTANCE_DEFAULT
        );

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
            //.setContentTitle(title)
            .setContentTitle(content)
            //.setContentText(content)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build();

        mNotificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification);
        return notification;
    }
    

    // 显示常驻通知（通常用于前台服务）
    public Notification showCustomForegroundNotification(Intent intent, RemoteViews contentView, RemoteViews bigContentView) {
        PendingIntent pendingIntent = createPendingIntent(intent);
        
        
        Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID_FOREGROUND)
            .setSmallIcon(R.drawable.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher))
            //.setContentTitle(title)
            .setContentIntent(pendingIntent)
            .setContent(contentView)
            .setCustomBigContentView(bigContentView)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setOngoing(true)
            .build();

        mNotificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification);
        return notification;
    }

    // 显示临时通知（自动消失）
    public void showTemporaryNotification(Intent intent, String title, String content) {
        showTemporaryNotification(intent, TEMPORARY_NOTIFICATION_ID, title, content);
    }

    // 显示临时通知（自动消失）
    public void showTemporaryNotification(Intent intent, int notificationID, String title, String content) {
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

        mNotificationManager.notify(notificationID, notification);
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

    // 取消指定通知
    public void cancelNotification(int notificationID) {
        mNotificationManager.cancel(notificationID);
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

//    public void sendSMSReceivedMessage(int notificationID, String szPhone, String szBody) {
//        Intent intent = new Intent(mContext, SMSActivity.class);
//        intent.putExtra(SMSActivity.EXTRA_PHONE, szPhone);
//        String szTitle = mContext.getString(R.string.text_smsfrom)  + "<" + szPhone + ">";
//        String szContent = "[ " + szBody + " ]";
//        showTemporaryNotification(intent, notificationID, szTitle, szContent);
//    }

    public void cleanOldChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            List<NotificationChannel> allChannels = mNotificationManager.getNotificationChannels();
            for (NotificationChannel channel : allChannels) {
                LogUtils.d(TAG, "Clean channel : " + channel.getId());
                if (!activeChannelConfigs.containsKey(channel.getId())) {
                    // 安全删除渠道
                    mNotificationManager.deleteNotificationChannel(channel.getId());
                    LogUtils.d(TAG, String.format("Deleted Channel %s", channel.getId()));
                }
            }
        }
    }
}
