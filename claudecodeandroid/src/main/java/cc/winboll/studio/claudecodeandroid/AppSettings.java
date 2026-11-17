package cc.winboll.studio.claudecodeandroid;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 应用配置存取。
 *
 * <p>保存桥接服务地址、访问令牌、默认工作目录，供设置页与聊天页共用。</p>
 *
 * @author 豆包&amp;Big Pickle&amp;MiMo&amp;ZhanGSKen
 * @since 1.0.0
 */
public final class AppSettings {

    private static final String PREF_NAME = "claude_bridge_prefs";
    private static final String KEY_SERVER_URL = "server_url";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_CWD = "cwd";

    /** 局域网默认地址：本机 WINBOT 的桥接端口 */
    public static final String DEFAULT_SERVER_URL = "http://192.168.123.202:8787";
    public static final String DEFAULT_CWD = "C:\\0WINBOT";

    private AppSettings() {
        // 工具类禁止实例化
    }

    private static SharedPreferences prefs(final Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String getServerUrl(final Context context) {
        String value = prefs(context).getString(KEY_SERVER_URL, "");
        if (value == null || value.length() == 0) {
            return DEFAULT_SERVER_URL;
        }
        return value;
    }

    public static void setServerUrl(final Context context, final String url) {
        prefs(context).edit().putString(KEY_SERVER_URL, url).apply();
    }

    public static String getToken(final Context context) {
        String value = prefs(context).getString(KEY_TOKEN, "");
        return value == null ? "" : value;
    }

    public static void setToken(final Context context, final String token) {
        prefs(context).edit().putString(KEY_TOKEN, token).apply();
    }

    public static String getCwd(final Context context) {
        String value = prefs(context).getString(KEY_CWD, "");
        if (value == null || value.length() == 0) {
            return DEFAULT_CWD;
        }
        return value;
    }

    public static void setCwd(final Context context, final String cwd) {
        prefs(context).edit().putString(KEY_CWD, cwd).apply();
    }

    /** 去掉地址尾部的斜杠，避免拼接出双斜杠 */
    public static String normalizeBaseUrl(final String url) {
        String result = url == null ? "" : url.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
