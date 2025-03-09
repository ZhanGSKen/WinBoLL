package cc.winboll.studio.mymessagemanager.utils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/19 14:30:57
 * @Describe 应用通知栏工具类
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
import android.widget.RemoteViews;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.activitys.MainActivity;
import cc.winboll.studio.mymessagemanager.activitys.SMSActivity;
import cc.winboll.studio.mymessagemanager.beans.MessageNotificationBean;
import cc.winboll.studio.mymessagemanager.services.MainService;
import cc.winboll.studio.shared.log.LogUtils;

public class NotificationUtil {

    public static final String TAG = "NotificationUtil";
    public static final int ID_MSG_SERVICE = 10000;

    static final String szSMSChannelID = "1";

    static final String szServiceChannelID = "0";


    //static int mNumSendForegroundNotification = 10000;
    //static int mNumSendSMSNotification = 20000;

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
    public void sendForegroundNotification(MainService service, MessageNotificationBean nessageNotificationBean) {
		//创建Notification，传入Context和channelId
		Intent intent = new Intent();//这个intent会传给目标,可以使用getIntent来获取
		intent.setClass(service, MainActivity.class);

		//这里放一个count用来区分每一个通知
		//intent.putExtra("intent", "intent--->" + count);//这里设置一个数据,带过去

		//参数1:context 上下文对象
		//参数2:发送者私有的请求码(Private request code for the sender)
		//参数3:intent 意图对象
		//参数4:必须为FLAG_ONE_SHOT,FLAG_NO_CREATE,FLAG_CANCEL_CURRENT,FLAG_UPDATE_CURRENT,中的一个
		PendingIntent mForegroundPendingIntent = PendingIntent.getActivity(service, nessageNotificationBean.getMessageId(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);

		Notification mForegroundNotification = new Notification.Builder(service, szServiceChannelID)
			.setAutoCancel(true)
			.setContentTitle(nessageNotificationBean.getTitle())
			.setContentText(nessageNotificationBean.getContent())
			.setWhen(System.currentTimeMillis())
			.setSmallIcon(R.drawable.ic_launcher)
			//设置红色
			.setColor(Color.parseColor("#F00606"))
			.setLargeIcon(BitmapFactory.decodeResource(service.getResources(), R.drawable.ic_launcher))
			.setContentIntent(mForegroundPendingIntent)
			.build();


		RemoteViews mrvForegroundNotificationView = new RemoteViews(service.getPackageName(), R.layout.remoteview);
		mrvForegroundNotificationView.setTextViewText(R.id.remoteviewTextView1, nessageNotificationBean.getTitle());
		mrvForegroundNotificationView.setTextViewText(R.id.remoteviewTextView2, nessageNotificationBean.getContent());
		mrvForegroundNotificationView.setImageViewResource(R.id.remoteviewImageView1, R.drawable.ic_launcher);
		mForegroundNotification.contentView = mrvForegroundNotificationView;
		mForegroundNotification.bigContentView = mrvForegroundNotificationView;

		service.startForeground(nessageNotificationBean.getMessageId(), mForegroundNotification);

    }

    public void sendSMSNotification(Context context, MessageNotificationBean messageNotificationBean) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
			Context.NOTIFICATION_SERVICE);
		/*NotificationManager notificationManager = createSMSNotificationChannel(context);
		 if (notificationManager == null) {
		 LogUtils.d(TAG, "createSMSNotificationChannel failed.");
		 return;
		 }*/

		//创建Notification，传入Context和channelId
		Intent intent = new Intent(context, SMSActivity.class);
		intent.putExtra(SMSActivity.EXTRA_PHONE, messageNotificationBean.getPhone());
		LogUtils.d(TAG, "sendSMSNotification(...) message.getPhone() is : " + messageNotificationBean.getPhone());
		//Intent intent = new Intent();//这个intent会传给目标,可以使用getIntent来获取
		//intent.setClass(context, MainActivity.class);
		//这里放一个count用来区分每一个通知
		//intent.putExtra("intent", "intent--->" + count);//这里设置一个数据,带过去

		//参数1:context 上下文对象
		//参数2:发送者私有的请求码(Private request code for the sender)
		//参数3:intent 意图对象
		//参数4:必须为FLAG_ONE_SHOT,FLAG_NO_CREATE,FLAG_CANCEL_CURRENT,FLAG_UPDATE_CURRENT,中的一个
		PendingIntent mRemindPendingIntent = PendingIntent.getActivity(context, messageNotificationBean.getMessageId(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
		Notification mSMSNotification = new Notification.Builder(context, szSMSChannelID)
			.setAutoCancel(true)
			.setContentTitle(messageNotificationBean.getTitle())
			.setContentText(messageNotificationBean.getContent())
			.setWhen(System.currentTimeMillis())
			.setSmallIcon(R.drawable.ic_launcher)
			//设置红色
			.setColor(Color.parseColor("#F00606"))
			.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
			.setContentIntent(mRemindPendingIntent)
			.build();

		RemoteViews mrvSMSNotificationView = new RemoteViews(context.getPackageName(), R.layout.remoteview);
		mrvSMSNotificationView.setTextViewText(R.id.remoteviewTextView1, messageNotificationBean.getTitle());
		mrvSMSNotificationView.setTextViewText(R.id.remoteviewTextView2, messageNotificationBean.getContent());
		mrvSMSNotificationView.setImageViewResource(R.id.remoteviewImageView1, R.drawable.ic_launcher);
		mSMSNotification.contentView = mrvSMSNotificationView;
		mSMSNotification.bigContentView = mrvSMSNotificationView;
		notificationManager.notify(messageNotificationBean.getMessageId(), mSMSNotification);
        LogUtils.d(TAG, "getMessageId is : " + Integer.toString(messageNotificationBean.getMessageId()));

    }

    public void sendSMSReceivedMessage(Context context, int nMessageId, String szPhone, String szBody) {
        String szTitle = context.getString(R.string.text_smsfrom)  + "<" + szPhone + ">";
        String szContent = "[ " + szBody + " ]";
        sendSMSNotification(context, new MessageNotificationBean(nMessageId, szPhone, szTitle, szContent));
    }

    public static void cancelNotification(Context context, int notificationId) {
        // 获取 NotificationManager 实例
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // 撤回指定 ID 的通知栏消息
        notificationManager.cancel(notificationId);
    }
}


