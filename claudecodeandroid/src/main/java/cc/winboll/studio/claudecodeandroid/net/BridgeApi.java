package cc.winboll.studio.claudecodeandroid.net;

import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * 桥接服务 HTTP 客户端（HttpURLConnection 实现，无第三方依赖）。
 *
 * <p>所有方法均为阻塞调用，必须放在工作线程执行。</p>
 *
 * @author 豆包&amp;Big Pickle&amp;MiMo&amp;ZhanGSKen
 * @since 1.0.0
 */
public final class BridgeApi {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final int CONNECT_TIMEOUT_MS = 10000;
    private static final int READ_TIMEOUT_MS = 30000;

    private BridgeApi() {
        // 工具类禁止实例化
    }

    private static HttpURLConnection open(final String url, final String token, final String method)
            throws IOException {
        final HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Accept", "application/json");
        if (Build.VERSION.SDK_INT <= 19) {
            // 兼容旧版本 keep-alive 缺陷
            conn.setRequestProperty("Connection", "close");
        }
        return conn;
    }

    private static String readAll(final InputStream input) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF8));
        final StringBuilder sb = new StringBuilder();
        try {
            String line = reader.readLine();
            while (line != null) {
                sb.append(line);
                line = reader.readLine();
            }
        } finally {
            reader.close();
        }
        return sb.toString();
    }

    private static JSONObject parseResponse(final HttpURLConnection conn) throws IOException {
        final int code = conn.getResponseCode();
        InputStream input = null;
        try {
            input = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
            final String body = input == null ? "" : readAll(input);
            final JSONObject result = body.length() == 0 ? new JSONObject() : new JSONObject(body);
            result.put("_httpCode", code);
            return result;
        } catch (org.json.JSONException e) {
            throw new IOException("返回内容不是合法 JSON", e);
        } finally {
            if (input != null) {
                input.close();
            }
            conn.disconnect();
        }
    }

    private static JSONObject requestJson(final String baseUrl, final String token, final String method,
                                          final String path, final JSONObject body) throws IOException {
        final HttpURLConnection conn = open(baseUrl + path, token, method);
        try {
            if (body != null && !"GET".equals(method)) {
                conn.setDoOutput(true);
                final OutputStream out = conn.getOutputStream();
                try {
                    out.write(body.toString().getBytes(UTF8));
                    out.flush();
                } finally {
                    out.close();
                }
            }
            return parseResponse(conn);
        } finally {
            // parseResponse 内部已 disconnect，这里仅兜底
        }
    }

    /** 健康检查 */
    public static JSONObject ping(final String baseUrl, final String token) throws IOException {
        return requestJson(baseUrl, token, "GET", "/api/ping", null);
    }

    /** 会话列表 */
    public static JSONArray listSessions(final String baseUrl, final String token) throws IOException {
        final JSONObject obj = requestJson(baseUrl, token, "GET", "/api/sessions", null);
        return obj.optJSONArray("sessions");
    }

    /** 新建会话 */
    public static JSONObject createSession(final String baseUrl, final String token,
                                           final String title, final String cwd) throws IOException {
        final JSONObject body = new JSONObject();
        try {
            body.put("title", title);
            body.put("cwd", cwd);
        } catch (org.json.JSONException e) {
            throw new IOException("构造请求失败", e);
        }
        return requestJson(baseUrl, token, "POST", "/api/sessions", body);
    }

    /** 删除会话 */
    public static boolean deleteSession(final String baseUrl, final String token,
                                        final String sessionId) throws IOException {
        final JSONObject obj = requestJson(baseUrl, token, "DELETE",
                "/api/sessions/" + sessionId, null);
        return obj.optInt("_httpCode", 0) == 200;
    }

    /** 发送一轮用户输入 */
    public static int sendMessage(final String baseUrl, final String token,
                                  final String sessionId, final String text) throws IOException {
        final JSONObject body = new JSONObject();
        try {
            body.put("text", text);
        } catch (org.json.JSONException e) {
            throw new IOException("构造请求失败", e);
        }
        final JSONObject obj = requestJson(baseUrl, token, "POST",
                "/api/sessions/" + sessionId + "/messages", body);
        return obj.optInt("_httpCode", 0);
    }

    /** 权限审批作答 */
    public static boolean answerPermission(final String baseUrl, final String token,
                                           final String requestId, final boolean allow,
                                           final boolean remember, final String toolName) throws IOException {
        final JSONObject body = new JSONObject();
        try {
            body.put("allow", allow);
            body.put("remember", remember);
            body.put("toolName", toolName == null ? "" : toolName);
        } catch (org.json.JSONException e) {
            throw new IOException("构造请求失败", e);
        }
        final JSONObject obj = requestJson(baseUrl, token, "POST",
                "/api/permissions/" + requestId, body);
        return obj.optInt("_httpCode", 0) == 200;
    }

    /** 中断当前轮次 */
    public static boolean interrupt(final String baseUrl, final String token,
                                    final String sessionId) throws IOException {
        final JSONObject obj = requestJson(baseUrl, token, "POST",
                "/api/sessions/" + sessionId + "/interrupt", new JSONObject());
        return obj.optInt("_httpCode", 0) == 200;
    }

    /** 打开 SSE 事件流（长连接，readTimeout 放宽） */
    public static HttpURLConnection openEventStream(final String baseUrl, final String token,
                                                    final String sessionId, final long afterSeq,
                                                    final int readTimeoutMs) throws IOException {
        final String url = baseUrl + "/api/sessions/" + sessionId + "/events?afterSeq=" + afterSeq;
        final HttpURLConnection conn = open(url, token, "GET");
        conn.setReadTimeout(readTimeoutMs);
        conn.setRequestProperty("Accept", "text/event-stream");
        conn.setRequestProperty("Cache-Control", "no-cache");
        return conn;
    }

    /** 读取一行 SSE 内容 */
    public static String readLine(final BufferedReader reader) throws IOException {
        return reader.readLine();
    }

    /** 构造 UTF-8 的 BufferedReader */
    public static BufferedReader newReader(final InputStream input) {
        return new BufferedReader(new InputStreamReader(input, UTF8));
    }
}
