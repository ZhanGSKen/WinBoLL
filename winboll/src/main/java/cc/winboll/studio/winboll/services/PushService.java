package cc.winboll.studio.winboll.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.IBinder;

import cc.winboll.studio.winboll.R;
import cc.winboll.studio.winboll.push.PushHubClient;
import cc.winboll.studio.winboll.push.PushNotifier;

/**
 * 消息推送前台服务（自建 WSS 通道 B，文档 §七 保活要求）。
 *
 * - 前台服务 + 常驻低优先级通知，降低被 Doze/厂商策略杀掉的概率；
 * * 内嵌 {@link PushHubClient} 长连接收帧，收到 notify 转 {@link PushNotifier} 发系统通知；
 * - 监听网络变化（断网/切网后立即重建连接）；App 回前台时也可触发立即重连；
 * - START_STICKY：被杀后系统重建，服务内重新拉起连接。
 *
 * @author WorkBuddy_MiMo
 */
public class PushService extends Service {

	private static final String NOTIFICATION_CHANNEL_ID = "push_service";
	private static final int NOTIFICATION_ID = 0x5055; // "PU"

	/** App 回前台时触发立即重连（不必等退避结束，文档 §五）。 */
	public static final String ACTION_RECONNECT = "cc.winboll.studio.winboll.action.PUSH_RECONNECT";

	private PushHubClient mClient;
	private ConnectivityManager.NetworkCallback mNetCallback;

	/** 启动推送服务（幂等，已在运行则只触发 onStartCommand）。 */
	public static void start(Context context) {
		Intent intent = new Intent(context, PushService.class);
		try {
			context.startService(intent);
		} catch (IllegalStateException e) {
			// 后台启动受限时走前台服务通道
			context.startForegroundService(intent);
		}
	}

	/** App 回前台等场景触发立即重连（已连接时为空操作）。 */
	public static void reconnect(Context context) {
		Intent intent = new Intent(context, PushService.class);
		intent.setAction(ACTION_RECONNECT);
		try {
			context.startService(intent);
		} catch (IllegalStateException e) {
			context.startForegroundService(intent);
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		createNotificationChannel();
		// 前台服务 + 常驻通知（文档 §七）
		startForeground(NOTIFICATION_ID, buildOngoingNotification());
		mClient = new PushHubClient(new PushHubClient.NotifyCallback() {
			@Override
			public void onNotify(PushHubClient.NotifyFrame frame) {
				PushNotifier.show(PushService.this, frame);
			}
		});
		mClient.start();
		registerNetworkCallback();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && ACTION_RECONNECT.equals(intent.getAction())) {
			if (mClient != null) {
				mClient.reconnectNow();
			}
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		unregisterNetworkCallback();
		if (mClient != null) {
			mClient.stop();
			mClient = null;
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/** 网络由断到通立即重连（文档 §五 重连策略）。 */
	private void registerNetworkCallback() {
		ConnectivityManager cm =
			(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
			return;
		}
		mNetCallback = new ConnectivityManager.NetworkCallback() {
			@Override
			public void onAvailable(Network network) {
				if (mClient != null) {
					mClient.reconnectNow();
				}
			}
		};
		try {
			cm.registerDefaultNetworkCallback(mNetCallback);
		} catch (Exception ignored) {
			mNetCallback = null;
		}
	}

	private void unregisterNetworkCallback() {
		if (mNetCallback == null) {
			return;
		}
		ConnectivityManager cm =
			(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			try {
				cm.unregisterNetworkCallback(mNetCallback);
			} catch (Exception ignored) {
			}
		}
		mNetCallback = null;
	}

	private Notification buildOngoingNotification() {
		Notification.Builder builder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_winboll_logo)
			.setContentTitle("WinBoLL+ 消息推送")
			.setContentText("推送服务运行中")
			.setOngoing(true)
			.setShowWhen(false);
		return builder.build();
	}

	private void createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= 26) {
			NotificationManager nm =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			if (nm != null && nm.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
				NotificationChannel channel = new NotificationChannel(
					NOTIFICATION_CHANNEL_ID,
					"推送服务",
					NotificationManager.IMPORTANCE_LOW);
				channel.setDescription("消息推送长连接前台服务");
				channel.setShowBadge(false);
				nm.createNotificationChannel(channel);
			}
		}
	}
}
