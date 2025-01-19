package cc.winboll.studio.powerbell.utils;

/*
 * 参考：
 * https://blog.csdn.net/qq_35507234/article/details/90676587
 * https://blog.csdn.net/qq_16628781/article/details/51548324
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
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;
import cc.winboll.studio.powerbell.MainActivity;
import cc.winboll.studio.powerbell.R;
import cc.winboll.studio.powerbell.beans.NotificationMessage;
import cc.winboll.studio.powerbell.services.ControlCenterService;

public class NotificationUtils {

    public static final String TAG = NotificationUtils.class.getSimpleName();

    Context mContext;
    NotificationManager mNotificationManager;

    Notification mForegroundNotification;
    PendingIntent mForegroundPendingIntent;
    Notification mRemindNotification;
    PendingIntent mRemindPendingIntent;
    RemoteViews mrvServiceNotificationView;
    RemoteViews mrvRemindNotificationView;

    static enum NotificationType { MIN, MAX };
    private static int _mnServiceNotificationID = 1;
    private static int _mnRemindNotificationID = 2;
    private static String _mszChannelIDService = "1";
    private static String _mszChannelNameService = "Service";
    private static String _mszChannelIDRemind = "2";
    private static String _mszChannelNameRemind = "Remind";

    public NotificationUtils(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) context.getSystemService(
            Context.NOTIFICATION_SERVICE);
    }

    public void createNotificationChannel() {
        NotificationChannel channel1 = new NotificationChannel(_mszChannelIDService, _mszChannelNameService, NotificationManager.IMPORTANCE_DEFAULT);
        channel1.setSound(null, null);
        mNotificationManager.createNotificationChannel(channel1);
        NotificationChannel channel2 = new NotificationChannel(_mszChannelIDRemind, _mszChannelNameRemind, NotificationManager.IMPORTANCE_HIGH);
        channel2.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), Notification.AUDIO_ATTRIBUTES_DEFAULT);
        mNotificationManager.createNotificationChannel(channel2);
    }

    // 创建并发送服务通知
    //
    public void createForegroundNotification(ControlCenterService service, NotificationMessage notificationMessage) {
        //创建Notification，传入Context和channelId
        Intent intent = new Intent();//这个intent会传给目标,可以使用getIntent来获取
        intent.setPackage(service.getPackageName());
        //LogUtils.d(TAG, "mService.getPackageName() : " + service.getPackageName());
        intent.setClass(service, MainActivity.class);
        //LogUtils.d(TAG, "MainActivity.class.getName() : " + MainActivity.class.getName());
        //这里放一个count用来区分每一个通知
        //intent.putExtra("intent", "intent--->" + count);//这里设置一个数据,带过去

        //参数1:context 上下文对象
        //参数2:发送者私有的请求码(Private request code for the sender)
        //参数3:intent 意图对象
        //参数4:必须为FLAG_ONE_SHOT,FLAG_NO_CREATE,FLAG_CANCEL_CURRENT,FLAG_UPDATE_CURRENT,中的一个
        //mForegroundPendingIntent = PendingIntent.getActivity(mService, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			mForegroundPendingIntent = PendingIntent.getActivity(service,
																 1, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
		} else {
			mForegroundPendingIntent = PendingIntent.getActivity(service,
																 1, intent, PendingIntent.FLAG_IMMUTABLE);
		}

        mForegroundNotification = new Notification.Builder(service, _mszChannelIDService)
            .setAutoCancel(true)
            .setContentTitle(notificationMessage.getTitle())
            .setContentText(notificationMessage.getContent())
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_launcher)
            //设置红色
            .setColor(Color.parseColor("#F00606"))
            .setLargeIcon(BitmapFactory.decodeResource(service.getResources(), R.drawable.ic_launcher))
            .setContentIntent(mForegroundPendingIntent)
            .build();

        setForegroundNotificationRemoteViews(service, notificationMessage);
        service.startForeground(_mnServiceNotificationID, mForegroundNotification);
    }

    void initmrvRemindNotificationView(ControlCenterService service, NotificationMessage notificationMessage) {
        mrvRemindNotificationView = new RemoteViews(service.getPackageName(), R.layout.view_remindnotification);
        mrvRemindNotificationView.setTextViewText(R.id.viewremindnotificationTextView1, notificationMessage.getTitle());
        String szRemindMSG = notificationMessage.getRemindMSG();
        //LogUtils.d(TAG, "szRemindMSG : " + szRemindMSG);
        //mrvRemindNotificationView.setTextViewText(R.id.remoteviewTextView2, szRemindMSG);
        if (szRemindMSG != null) {
            if (szRemindMSG.trim().equals("-")) {
                //LogUtils.d(TAG, "-");
                mrvRemindNotificationView.setViewVisibility(R.id.remoteviewCharge, View.GONE);
                mrvRemindNotificationView.setViewVisibility(R.id.remoteviewUsege, View.VISIBLE);
            } else if (szRemindMSG.trim().equals("+")) {
                //LogUtils.d(TAG, "+");
                mrvRemindNotificationView.setViewVisibility(R.id.remoteviewUsege, View.GONE);
                mrvRemindNotificationView.setViewVisibility(R.id.remoteviewCharge, View.VISIBLE);
            }
            mrvRemindNotificationView.setImageViewResource(R.id.remoteviewImageView1, R.drawable.ic_launcher);
            //给我remoteViews上的控件tv_content添加监听事件
            //remoteViews.setOnClickPendingIntent(R.id.remoteviewLinearLayout1, pi);
            //return mrvServiceNotificationView;
        }
    }

    void initmrvServiceNotificationView(ControlCenterService service, NotificationMessage notificationMessage) {
        mrvServiceNotificationView = new RemoteViews(service.getPackageName(), R.layout.view_servicenotification);
        mrvServiceNotificationView.setTextViewText(R.id.remoteviewTextView1, notificationMessage.getTitle());
        //String szRemindMSG = notificationMessage.getRemindMSG();
        //mrvServiceNotificationView.setTextViewText(R.id.remoteviewTextView2, szRemindMSG);
        //rvServiceNotificationView.setTextViewText(R.id.remoteviewTextView3, notificationMessage.getContent() + Integer.toString(nTest));
        mrvServiceNotificationView.setTextViewText(R.id.remoteviewTextView3, notificationMessage.getContent());
        mrvServiceNotificationView.setImageViewResource(R.id.remoteviewImageView1, R.drawable.ic_launcher);
        //给我remoteViews上的控件tv_content添加监听事件
        //remoteViews.setOnClickPendingIntent(R.id.remoteviewLinearLayout1, pi);
        //return mrvServiceNotificationView;
    }

    void setForegroundNotificationRemoteViews(ControlCenterService service, NotificationMessage notificationMessage) {
        initmrvServiceNotificationView(service, notificationMessage);
        mForegroundNotification.contentView = mrvServiceNotificationView;
        mForegroundNotification.bigContentView = mrvServiceNotificationView;
    }

    void setRemindNotificationRemoteViews(ControlCenterService service, NotificationMessage notificationMessage) {
        initmrvRemindNotificationView(service, notificationMessage);
        mRemindNotification.contentView = mrvRemindNotificationView;
        mRemindNotification.bigContentView = mrvRemindNotificationView;
    }

    // 更新服务通知
    //
    public void updateForegroundNotification(ControlCenterService service, NotificationMessage notificationMessage) {
        setForegroundNotificationRemoteViews(service, notificationMessage);
        mNotificationManager.notify(_mnServiceNotificationID, mForegroundNotification);

    }

    // 创建并发送电量提醒通知
    //
    public void updateRemindNotification(ControlCenterService service, NotificationMessage notificationMessage) {
        //LogUtils.d(TAG, "updateRemindNotification : " + notificationMessage.getRemindMSG());
        setRemindNotificationRemoteViews(service, notificationMessage);
        mNotificationManager.notify(_mnRemindNotificationID, mRemindNotification);
    }

    public void createRemindNotification(ControlCenterService service, NotificationMessage notificationMessage) {
        //LogUtils.d(TAG, "notificationMessage : " + notificationMessage.getRemindMSG());
        //创建Notification，传入Context和channelId
        Intent intent = new Intent();//这个intent会传给目标,可以使用getIntent来获取
        intent.setPackage(service.getPackageName());
        intent.setClass(service, MainActivity.class);
        //这里放一个count用来区分每一个通知
        //intent.putExtra("intent", "intent--->" + count);//这里设置一个数据,带过去

        //参数1:context 上下文对象
        //参数2:发送者私有的请求码(Private request code for the sender)
        //参数3:intent 意图对象
        //参数4:必须为FLAG_ONE_SHOT,FLAG_NO_CREATE,FLAG_CANCEL_CURRENT,FLAG_UPDATE_CURRENT,中的一个
        //mRemindPendingIntent = PendingIntent.getActivity(mService, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			mRemindPendingIntent = PendingIntent.getActivity(service,
                                                             1, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
		} else {
			mRemindPendingIntent = PendingIntent.getActivity(service,
                                                             1, intent, PendingIntent.FLAG_IMMUTABLE);
		}

        mRemindNotification = new Notification.Builder(service, _mszChannelIDRemind)
            .setAutoCancel(true)
            .setContentTitle(notificationMessage.getTitle())
            .setContentText(notificationMessage.getContent())
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_launcher)
            //设置红色
            .setColor(Color.parseColor("#F00606"))
            .setLargeIcon(BitmapFactory.decodeResource(service.getResources(), R.drawable.ic_launcher))
            .setContentIntent(mRemindPendingIntent)
            .build();
        setRemindNotificationRemoteViews(service, notificationMessage);
    }
    
    public static void cancelRemindNotification(Context context){
        // 获取 NotificationManager 实例
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // 撤回指定 ID 的通知栏消息
        notificationManager.cancel(_mnRemindNotificationID);
    }
}


