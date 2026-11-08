package cc.winboll.studio.winboll.push;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.CookieManager;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * 推送网关客户端（自建 WSS 通道 B）。
 *
 * 按管理域《安卓端推送接口开发文档》实现：
 * 1. GET /push/ws-token 取连接令牌（携带 WebView 登录态 Cookie）；
 * 2. wss://<管理域>/push/ws?token= 握手；
 * 3. 接收 type=notify 的 JSON 文本帧，回调 {@link NotifyCallback}；
 * 4. 断线指数退避重连（1s→2s→4s→8s→16s，上限 30s），每次重连重新取令牌；
 * 5. 协议层 ping/pong 由 OkHttp 自动处理。
 *
 * 项目安卓端统一 Java 7 写法（无 lambda、无 try-with-resources）。
 *
 * @author WorkBuddy_MiMo
 */
public class PushHubClient {

	private static final String TAG = "PushHubClient";

	/** 重连退避指数上限：1s << 5 = 32s，再经 Math.min 钳到 30s。 */
	private static final int RETRY_SHIFT_MAX = 5;
	private static final long RETRY_MAX_MS = 30000L;

	/** 管理域基址（令牌签发与点击跳转的目标域）。 */
	public static final String BASE_URL = "https://management-develop.studio.winboll";

	/** 通知回调（Java 7 无 lambda，用接口 + 匿名内部类）。 */
	public interface NotifyCallback {
		void onNotify(NotifyFrame frame);
	}

	/** 一帧 notify 的字段。 */
	public static class NotifyFrame {
		public final String title;
		public final String body;
		public final String url;
		public final long ts;

		public NotifyFrame(String title, String body, String url, long ts) {
			this.title = title;
			this.body = body;
			this.url = url;
			this.ts = ts;
		}
	}

	private final OkHttpClient http;
	private final NotifyCallback callback;
	private final Handler main = new Handler(Looper.getMainLooper());
	private WebSocket ws;
	private int retry;
	private boolean stopped;
	/** 令牌请求在途标志：防止 reconnectNow 与首连并发开双连接。 */
	private boolean connecting;

	/** 命名 Runnable：断线重连任务可被 reconnectNow() 取消。 */
	private final Runnable reconnectTask = new Runnable() {
		@Override
		public void run() {
			connect();
		}
	};

	public PushHubClient(NotifyCallback callback) {
		this.http = new OkHttpClient.Builder().build();
		this.callback = callback;
	}

	public void start() {
		connect();
	}

	public void stop() {
		stopped = true;
		main.removeCallbacks(reconnectTask);
		if (ws != null) {
			try {
				ws.close(1000, "client stop");
			} catch (Exception ignored) {
			}
			ws = null;
		}
	}

	/**
	 * 立即重连（网络由断到通、App 回前台时调用，不必等退避结束）。
	 * 已连接或已停止时不做任何事。
	 */
	public void reconnectNow() {
		if (stopped || ws != null || connecting) {
			return;
		}
		main.removeCallbacks(reconnectTask);
		retry = 0;
		connect();
	}

	private String wsBase() {
		return BASE_URL.replaceFirst("https://", "wss://").replaceFirst("http://", "ws://");
	}

	private void connect() {
		if (stopped || ws != null || connecting) {
			return;
		}
		connecting = true;
		// 每次连接都重新取令牌（旧 token 复用大概率 401）
		final Request.Builder rb = new Request.Builder()
			.url(BASE_URL + "/push/ws-token")
			.get();
		// 复用 WebView 已登录会话（Auth::requireAuth 的 PHPSESSID）
		try {
			String cookie = CookieManager.getInstance().getCookie(BASE_URL);
			if (cookie != null && cookie.length() > 0) {
				rb.addHeader("Cookie", cookie);
			}
		} catch (Exception ignored) {
		}
		http.newCall(rb.build()).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				connecting = false;
				scheduleReconnect();
			}

			@Override
			public void onResponse(Call call, Response response) {
				connecting = false;
				String token = readToken(response);
				if (token.length() == 0) {
					Log.w(TAG, "ws-token failed, code=" + response.code() + ", retry later");
					scheduleReconnect();
				} else {
					Log.d(TAG, "ws-token ok, connecting ws");
					open(token);
				}
			}
		});
	}

	/** 读令牌响应体并释放连接（Java 7 不使用 try-with-resources）。 */
	private String readToken(Response response) {
		try {
			String body = response.body() == null ? "" : response.body().string();
			return new JSONObject(body).optString("token", "");
		} catch (Exception e) {
			return "";
		} finally {
			response.close();
		}
	}

	private void open(String token) {
		final Request request = new Request.Builder()
			.url(wsBase() + "/push/ws?token=" + token)
			.build();
		ws = http.newWebSocket(request, new WebSocketListener() {
			@Override
			public void onOpen(WebSocket webSocket, Response response) {
				retry = 0;
				Log.i(TAG, "ws connected");
			}

			@Override
			public void onMessage(WebSocket webSocket, String text) {
				final NotifyFrame frame = parseFrame(text);
				if (frame == null) {
					return;
				}
				Log.i(TAG, "notify frame: title=" + frame.title + " url=" + frame.url);
				main.post(new Runnable() {
					@Override
					public void run() {
						callback.onNotify(frame);
					}
				});
			}

			@Override
			public void onFailure(WebSocket webSocket, Throwable t, Response response) {
				Log.w(TAG, "ws failure: " + t);
				if (ws == webSocket) {
					ws = null;
				}
				scheduleReconnect();
			}

			@Override
			public void onClosed(WebSocket webSocket, int code, String reason) {
				if (ws == webSocket) {
					ws = null;
				}
				scheduleReconnect();
			}
		});
	}

	private NotifyFrame parseFrame(String text) {
		try {
			JSONObject o = new JSONObject(text);
			if (!"notify".equals(o.optString("type", ""))) {
				// 未知 type 忽略（文档 §五）
				return null;
			}
			return new NotifyFrame(
				o.optString("title", "新消息"),
				o.optString("body", ""),
				o.optString("url", "/"),
				o.optLong("ts", 0L));
		} catch (Exception e) {
			return null;
		}
	}

	private void scheduleReconnect() {
		if (stopped) {
			return;
		}
		main.removeCallbacks(reconnectTask);
		long delay = Math.min(RETRY_MAX_MS, 1000L << Math.min(retry, RETRY_SHIFT_MAX));
		retry++;
		main.postDelayed(reconnectTask, delay);
	}
}
