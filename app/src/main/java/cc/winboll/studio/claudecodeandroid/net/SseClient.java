package cc.winboll.studio.claudecodeandroid.net;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * SSE 事件流客户端。
 *
 * <p>维护一条到桥接服务的长连接，断线后按 lastSeq 自动重放重连，
 * 保证安卓端不会漏掉任何一条工具调用或权限审批事件。</p>
 *
 * @author 豆包&amp;Big Pickle&amp;MiMo&amp;ZhanGSKen
 * @since 1.0.0
 */
public final class SseClient {

    /** 事件与连接状态回调（在后台线程触发，UI 需自行切主线程） */
    public interface Listener {
        void onEvent(final JSONObject event);

        void onStatus(final String text, final boolean connected);
    }

    private static final int STREAM_READ_TIMEOUT_MS = 60000;
    private static final long RECONNECT_DELAY_MS = 3000L;

    private final String baseUrl;
    private final String token;
    private final String sessionId;
    private final Listener listener;

    private volatile boolean running = false;
    private volatile long lastSeq = 0L;
    private Thread worker;

    public SseClient(final String baseUrl, final String token, final String sessionId,
                     final Listener listener) {
        this.baseUrl = baseUrl;
        this.token = token;
        this.sessionId = sessionId;
        this.listener = listener;
    }

    public long getLastSeq() {
        return lastSeq;
    }

    public void start() {
        if (worker != null) {
            return;
        }
        running = true;
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                loop();
            }
        }, "sse-" + sessionId);
        worker.setDaemon(true);
        worker.start();
    }

    public void stop() {
        running = false;
        if (worker != null) {
            worker.interrupt();
            worker = null;
        }
    }

    private void loop() {
        while (running) {
            HttpURLConnection conn = null;
            try {
                conn = BridgeApi.openEventStream(baseUrl, token, sessionId, lastSeq,
                        STREAM_READ_TIMEOUT_MS);
                final int code = conn.getResponseCode();
                if (code != 200) {
                    listener.onStatus("连接被拒绝（HTTP " + code + "），请检查地址与令牌", false);
                    sleep(RECONNECT_DELAY_MS);
                    continue;
                }
                listener.onStatus("已连接", true);
                final BufferedReader reader = BridgeApi.newReader(conn.getInputStream());
                try {
                    readStream(reader);
                } finally {
                    reader.close();
                }
            } catch (IOException e) {
                if (!running) {
                    break;
                }
                listener.onStatus("连接中断：" + e.getMessage() + "，稍后重连", false);
                sleep(RECONNECT_DELAY_MS);
            } catch (RuntimeException e) {
                if (!running) {
                    break;
                }
                listener.onStatus("连接异常：" + e.getMessage() + "，稍后重连", false);
                sleep(RECONNECT_DELAY_MS);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    }

    /** 逐行读取 SSE，遇到空行派发事件 */
    private void readStream(final BufferedReader reader) throws IOException {
        final StringBuilder dataBuffer = new StringBuilder();
        String line = BridgeApi.readLine(reader);
        while (running && line != null) {
            if (line.length() == 0) {
                if (dataBuffer.length() > 0) {
                    dispatch(dataBuffer.toString());
                    dataBuffer.setLength(0);
                }
            } else if (line.startsWith("data:")) {
                String data = line.substring(5);
                if (data.startsWith(" ")) {
                    data = data.substring(1);
                }
                if (dataBuffer.length() > 0) {
                    dataBuffer.append('\n');
                } else {
                    // 相邻事件之间的 pending data 已被空行清空
                }
                dataBuffer.append(data);
            }
            // id: / : 注释 / event: 行忽略（seq 已包含在 JSON 里）
            line = BridgeApi.readLine(reader);
        }
    }

    private void dispatch(final String data) {
        try {
            final JSONObject event = new JSONObject(data);
            final long seq = event.optLong("seq", 0L);
            if (seq > lastSeq) {
                lastSeq = seq;
            }
            listener.onEvent(event);
        } catch (JSONException e) {
            // 单条脏数据忽略，不影响连接
        }
    }

    private void sleep(final long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
