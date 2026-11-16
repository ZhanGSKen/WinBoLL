package cc.winboll.studio.winboll.push;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import cc.winboll.studio.winboll.MainActivity;
import cc.winboll.studio.winboll.R;

/**
 * 推送消息通知展示（文档 §六）。
 *
 * 收到 notify 帧 → 建系统通知：
 * - channel：push_notify，IMPORTANCE_HIGH；
 * - 标题用 title（缺省 新消息），正文用 body；
 * - 通知 ID 用 url 的 hashCode，同一目标重复推送不叠加；
 * - 点击 → 应用内 WebView 打开 baseUrl + url（复用登录 Cookie，不用外部浏览器）。
 *
 * @author WorkBuddy_MiMo
 */
public class PushNotifier {

	/** 点击通知的动作与 extra。 */
	public static final String ACTION_PUSH_OPEN = "cc.winboll.studio.winboll.action.PUSH_OPEN";
	public static final String EXTRA_PUSH_URL = "push_url";

	private static final String CHANNEL_ID = "push_notify";
	private static final String CHANNEL_NAME = "推送通知";

	/** 收到一帧 notify，发系统通知。主线程调用。 */
	public static void show(Context context, PushHubClient.NotifyFrame frame) {
		if (context == null || frame == null) {
			return;
		}
		createChannel(context);

		String target = resolveUrl(frame.url);
		int id = frame.url != null ? frame.url.hashCode() : (int) frame.ts;

		Intent intent = new Intent(context, MainActivity.class);
		intent.setAction(ACTION_PUSH_OPEN);
		intent.putExtra(EXTRA_PUSH_URL, target);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent contentPi = PendingIntent.getActivity(
			context, id, intent,
			PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

		String title = frame.title != null && frame.title.length() > 0 ? frame.title : "新消息";
		Notification.Builder builder = new Notification.Builder(context, CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_winboll_logo)
			.setContentTitle(title)
			.setContentText(frame.body)
			.setContentIntent(contentPi)
			.setAutoCancel(true)
			.setShowWhen(true);
		if (frame.ts > 0) {
			builder.setWhen(frame.ts * 1000L);
		}

		NotificationManager nm = getNotificationManager(context);
		if (nm != null) {
			nm.notify(id, builder.build());
		}
	}

	/** frame.url 是站内相对路径，必须拼管理域；已是绝对地址则原样使用。 */
	public static String resolveUrl(String url) {
		if (url == null || url.length() == 0) {
			return PushHubClient.BASE_URL + "/";
		}
		if (url.startsWith("http://") || url.startsWith("https://")) {
			return url;
		}
		if (!url.startsWith("/")) {
			url = "/" + url;
		}
		return PushHubClient.BASE_URL + url;
	}

	private static void createChannel(Context context) {
		if (Build.VERSION.SDK_INT >= 26) {
			NotificationManager nm = getNotificationManager(context);
			if (nm != null && nm.getNotificationChannel(CHANNEL_ID) == null) {
				NotificationChannel channel = new NotificationChannel(
					CHANNEL_ID,
					CHANNEL_NAME,
					NotificationManager.IMPORTANCE_HIGH);
				channel.setDescription("站内消息推送通知");
				nm.createNotificationChannel(channel);
			}
		}
	}

	private static NotificationManager getNotificationManager(Context context) {
		return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
}
