package cc.winboll.studio.winboll.utils;

import cc.winboll.studio.libappbase.LogUtils;

/**
 * 日志工具类（适配项目规范）
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/07
 */
public class SpecUtil {

    private static final boolean isDebug = true;

    public static void WWSpecLogInfo(String tag, String msg) {
        if (isDebug) {
            LogUtils.i(tag, msg);
        }
    }

    public static void WWSpecLogError(String tag, String msg) {
        if (isDebug) {
            LogUtils.e(tag, msg);
        }
    }
}

