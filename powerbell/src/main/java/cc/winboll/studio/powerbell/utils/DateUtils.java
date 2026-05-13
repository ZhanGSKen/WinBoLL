package cc.winboll.studio.powerbell.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/12/24
 * @Describe 日期时间工具类（Java 7 兼容 | API 30 适配）
 * 功能：提供当前时间的格式化字符串获取功能
 */
public class DateUtils {
    // ================================== 静态常量区（置顶归类，消除魔法值）=================================
    public static final String TAG = "DateUtils";
    private static final String DATE_FORMAT_PATTERN = "yyyyMMdd_HHmmssSSS"; // 修正年份格式为小写yyyy，毫秒为SSS
    private static final Locale DEFAULT_LOCALE = Locale.getDefault();

    // ================================== 工具方法（静态方法，无状态设计）=================================
    /**
     * 获取当前时间的格式化字符串
     * 格式：yyyyMMdd_HHmmssSSS（年-月-日_时-分-秒-毫秒）
     * @return 格式化后的当前时间字符串
     */
    public static String getDateNowString() {
        LogUtils.d(TAG, "【getDateNowString】调用开始");
        // 初始化日期格式化工具（Java 7 兼容，使用小写yyyy避免周基年问题）
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PATTERN, DEFAULT_LOCALE);
        // 读取当前时间戳
        long currentTime = System.currentTimeMillis();
        // 格式化时间
        String formattedTime = sdf.format(currentTime);
        LogUtils.d(TAG, "【getDateNowString】调用成功 | 格式化时间=" + formattedTime);
        return formattedTime;
    }
}

