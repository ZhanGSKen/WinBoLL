package cc.winboll.studio.watchoutputinstaller.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;
import cc.winboll.studio.watchoutputinstaller.services.MainService;
import cc.winboll.studio.watchoutputinstaller.MainActivity;
import cc.winboll.studio.watchoutputinstaller.R;

public class NotificationUtil {

    static final String TAG = "NotificationUtil";
    static final String szServiceChannelID = "0";
    static int mNumSendForegroundNotification = 10000;

    public NotificationManager createServiceNotificationChannel(Context context) {
        //创建通知渠道ID
        String channelId = szServiceChannelID;
        //创建通知渠道名称
        String channelName = "Service Message";
        //创建通知渠道重要性
        int importance = NotificationManager.IMPORTANCE_MIN;
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setSound(null, null);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
            Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        return notificationManager;
    }

    // 创建通知
    //
    public void sendForegroundNotification(MainService service) {
        //创建Notification，传入Context和channelId
        Intent intent = new Intent();//这个intent会传给目标,可以使用getIntent来获取
        intent.setClass(service, MainActivity.class);

        //这里放一个count用来区分每一个通知
        //intent.putExtra("intent", "intent--->" + count);//这里设置一个数据,带过去

        //参数1:context 上下文对象
        //参数2:发送者私有的请求码(Private request code for the sender)
        //参数3:intent 意图对象
        //参数4:必须为FLAG_ONE_SHOT,FLAG_NO_CREATE,FLAG_CANCEL_CURRENT,FLAG_UPDATE_CURRENT,中的一个
        PendingIntent mForegroundPendingIntent = PendingIntent.getActivity(service, mNumSendForegroundNotification, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);

        Notification mForegroundNotification = new Notification.Builder(service, szServiceChannelID)
            .setAutoCancel(true)
            .setContentTitle(service.getString(R.string.app_name))
            .setContentText(service.TAG + " is started.")
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_launcher)
            //设置红色
            .setColor(Color.parseColor("#F00606"))
            .setLargeIcon(BitmapFactory.decodeResource(service.getResources(), R.drawable.ic_launcher))
            .setContentIntent(mForegroundPendingIntent)
            .build();


        /*RemoteViews mrvForegroundNotificationView = new RemoteViews(service.getPackageName(), R.layout.remoteview);
        mrvForegroundNotificationView.setTextViewText(R.id.remoteviewTextView1, notificationMessage.getTitle());
        mrvForegroundNotificationView.setTextViewText(R.id.remoteviewTextView2, notificationMessage.getContent());
        mrvForegroundNotificationView.setImageViewResource(R.id.remoteviewImageView1, R.drawable.ic_launcher);
        mForegroundNotification.contentView = mrvForegroundNotificationView;
        mForegroundNotification.bigContentView = mrvForegroundNotificationView;
        */

        service.startForeground(mNumSendForegroundNotification, mForegroundNotification);

    }
}


