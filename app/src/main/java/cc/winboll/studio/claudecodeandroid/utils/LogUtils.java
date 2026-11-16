package cc.winboll.studio.claudecodeandroid.utils;

import android.util.Log;

/**
 * 统一日志工具类。
 *
 * <p>所有模块必须通过本类输出日志，禁止直接调用 {@link Log}。
 * 可通过 {@link #DEBUG} 开关统一控制日志输出。</p>
 *
 * @author 豆包&amp;Big Pickle&amp;MiMo&amp;ZhanGSKen
 * @since 1.0.0
 */
public final class LogUtils {

    /** 全局日志开关，release 构建可置为 false */
    public static final boolean DEBUG = true;

    /** 统一日志前缀，便于过滤 */
    private static final String PREFIX = "WINBOT/";

    private LogUtils() {
        // 工具类禁止实例化
    }

    public static void v(final String tag, final String msg) {
        if (DEBUG) {
            Log.v(PREFIX + tag, msg);
        }
    }

    public static void d(final String tag, final String msg) {
        if (DEBUG) {
            Log.d(PREFIX + tag, msg);
        }
    }

    public static void i(final String tag, final String msg) {
        if (DEBUG) {
            Log.i(PREFIX + tag, msg);
        }
    }

    public static void w(final String tag, final String msg) {
        if (DEBUG) {
            Log.w(PREFIX + tag, msg);
        }
    }

    public static void e(final String tag, final String msg) {
        if (DEBUG) {
            Log.e(PREFIX + tag, msg);
        }
    }

    public static void e(final String tag, final String msg, final Throwable tr) {
        if (DEBUG) {
            Log.e(PREFIX + tag, msg, tr);
        }
    }
}
