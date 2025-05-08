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
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.RemoteViews;
import cc.winboll.studio.timestamp.App;
import cc.winboll.studio.timestamp.MainActivity;
import cc.winboll.studio.timestamp.MainService;
import cc.winboll.studio.timestamp.R;
import cc.winboll.studio.timestamp.receivers.ButtonClickReceiver;

public class NotificationHelper {

    public static final String TAG = "NotificationUtil";
    public static final int ID_MSG_SERVICE = 10000;

    static final String szSMSChannelID = "1";

    static final String szServiceChannelID = "0";


    //static int mNumSendForegroundNotification = 10000;
    //static int mNumSendSMSNotification = 20000;
    Context mContext;


    public NotificationManager createServiceNotificationChannel(Context context) {
        mContext = context;
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

    public NotificationManager createSMSNotificationChannel(Context context) {
        //创建通知渠道ID
        String channelId = szSMSChannelID;
        //创建通知渠道名称
        String channelName = "SMS Message";
        //创建通知渠道重要性
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), Notification.AUDIO_ATTRIBUTES_DEFAULT);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
            Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        return notificationManager;
    }

    // 创建通知
    //
    public void sendForegroundNotification(MainService service, String message) {
        //创建Notification，传入Context和channelId
        Intent intent = new Intent();//这个intent会传给目标,可以使用getIntent来获取
        intent.setClass(mContext, MainActivity.class);

        //这里放一个count用来区分每一个通知
        //intent.putExtra("intent", "intent--->" + count);//这里设置一个数据,带过去

        //参数1:context 上下文对象
        //参数2:发送者私有的请求码(Private request code for the sender)
        //参数3:intent 意图对象
        //参数4:必须为FLAG_ONE_SHOT,FLAG_NO_CREATE,FLAG_CANCEL_CURRENT,FLAG_UPDATE_CURRENT,中的一个
        PendingIntent mForegroundPendingIntent = PendingIntent.getActivity(service, ID_MSG_SERVICE, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);

        Notification mForegroundNotification = new Notification.Builder(service, szServiceChannelID)
            .setAutoCancel(true)
            //.setContentTitle(nessageNotificationBean.getTitle())
            //.setContentText(nessageNotificationBean.getContent())
            //.setContent(remoteviews)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(App.getAPPIcon())
            //设置红色
            .setColor(Color.parseColor("#F00606"))
            .setLargeIcon(BitmapFactory.decodeResource(service.getResources(), App.getAPPIcon()))
            .setContentIntent(mForegroundPendingIntent)
            .build();

        // 创建 RemoteViews 对象，加载布局
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.custom_notification_layout);

        // 自定义 TextView 的文本
        remoteViews.setTextViewText(R.id.tv_timestamp, message);
        // 自定义 TextView 的文本颜色
        remoteViews.setTextColor(R.id.tv_timestamp, mContext.getResources().getColor(R.color.colorAccent, null));
        // 这里虽然不能直接设置字体大小，但可以通过反射等方式尝试（不推荐，且有兼容性问题）

        // 设置按钮图片
        remoteViews.setImageViewResource(R.id.iv_copytimestamp, App.getAPPIcon());
        // 创建点击通知后的意图
        Intent intentMain = new Intent(mContext, MainActivity.class);
        PendingIntent pendingMainIntent = PendingIntent.getActivity(mContext, 0, intentMain, PendingIntent.FLAG_UPDATE_CURRENT);
        // 设置通知的点击事件
        remoteViews.setOnClickPendingIntent(R.id.tv_timestamp, pendingMainIntent);

        // 创建点击按钮后要发送的广播 Intent
        Intent broadcastIntent = new Intent(ButtonClickReceiver.BUTTON_COPYTIMESTAMP_ACTION);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
            mContext,
            0,
            broadcastIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT
        );

        // 为按钮设置点击事件
        remoteViews.setOnClickPendingIntent(R.id.iv_copytimestamp, pendingIntent);

        mForegroundNotification.contentView = remoteViews;
        mForegroundNotification.bigContentView = remoteViews;

        service.startForeground(ID_MSG_SERVICE, mForegroundNotification);

        // 播放默认短信铃声
        Uri defaultSmsRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        AudioPlayerUriUtil.playAudio(service, defaultSmsRingtoneUri);

        // 播放应用铃声
        // 获取MP3文件的Uri
        Uri soundUri = Uri.parse("android.resource://" + service.getPackageName() + "/" + R.raw.diweiyi);
        AudioPlayerUriUtil.playAudio(service, soundUri);
    }
    
    

//    public void sendSMSNotification(Context context, MessageNotificationBean messageNotificationBean) {
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
//            Context.NOTIFICATION_SERVICE);
//        /*NotificationManager notificationManager = createSMSNotificationChannel(context);
//         if (notificationManager == null) {
//         LogUtils.d(TAG, "createSMSNotificationChannel failed.");
//         return;
//         }*/
//
//        //创建Notification，传入Context和channelId
//        Intent intent = new Intent(context, SMSActivity.class);
//        intent.putExtra(SMSActivity.EXTRA_PHONE, messageNotificationBean.getPhone());
//        LogUtils.d(TAG, "sendSMSNotification(...) message.getPhone() is : " + messageNotificationBean.getPhone());
//        //Intent intent = new Intent();//这个intent会传给目标,可以使用getIntent来获取
//        //intent.setClass(context, MainActivity.class);
//        //这里放一个count用来区分每一个通知
//        //intent.putExtra("intent", "intent--->" + count);//这里设置一个数据,带过去
//
//        //参数1:context 上下文对象
//        //参数2:发送者私有的请求码(Private request code for the sender)
//        //参数3:intent 意图对象
//        //参数4:必须为FLAG_ONE_SHOT,FLAG_NO_CREATE,FLAG_CANCEL_CURRENT,FLAG_UPDATE_CURRENT,中的一个
//        PendingIntent mRemindPendingIntent = PendingIntent.getActivity(context, messageNotificationBean.getMessageId(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
//        Notification mSMSNotification = new Notification.Builder(context, szSMSChannelID)
//            .setAutoCancel(true)
//            .setContentTitle(messageNotificationBean.getTitle())
//            .setContentText(messageNotificationBean.getContent())
//            .setWhen(System.currentTimeMillis())
//            .setSmallIcon(R.drawable.ic_launcher)
//            //设置红色
//            .setColor(Color.parseColor("#F00606"))
//            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
//            .setContentIntent(mRemindPendingIntent)
//            .build();
//
//        RemoteViews mrvSMSNotificationView = new RemoteViews(context.getPackageName(), R.layout.remoteview);
//        mrvSMSNotificationView.setTextViewText(R.id.remoteviewTextView1, messageNotificationBean.getTitle());
//        mrvSMSNotificationView.setTextViewText(R.id.remoteviewTextView2, messageNotificationBean.getContent());
//        mrvSMSNotificationView.setImageViewResource(R.id.remoteviewImageView1, R.drawable.ic_launcher);
//        mSMSNotification.contentView = mrvSMSNotificationView;
//        mSMSNotification.bigContentView = mrvSMSNotificationView;
//        notificationManager.notify(messageNotificationBean.getMessageId(), mSMSNotification);
//        LogUtils.d(TAG, "getMessageId is : " + Integer.toString(messageNotificationBean.getMessageId()));
//
//    }

//    public void sendSMSReceivedMessage(Context context, int nMessageId, String szPhone, String szBody) {
//        String szTitle = context.getString(R.string.text_smsfrom)  + "<" + szPhone + ">";
//        String szContent = "[ " + szBody + " ]";
//        sendSMSNotification(context, new MessageNotificationBean(nMessageId, szPhone, szTitle, szContent));
//    }

//    public static void cancelNotification(Context context, int notificationId) {
//        // 获取 NotificationManager 实例
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        // 撤回指定 ID 的通知栏消息
//        notificationManager.cancel(notificationId);
//    }
}


