package cc.winboll.studio.powerbell.utils;

import android.text.TextUtils;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.models.BatteryInfoBean;
import java.util.ArrayList;
import java.util.Locale;

/**
 * 字符串格式化工具类
 * 功能：电量使用时间列表格式化、时间跨度计算
 * 适配：Java 7 + Android API 30
 * 核心逻辑：将电池信息列表转换为指定格式字符串，计算时间戳之间的跨度并格式化
 */
public class StringUtils {
    // ================================== 静态常量区（置顶归类，消除魔法值）=================================
    public static final String TAG = StringUtils.class.getSimpleName();
    // 时间跨度单位符号
    private static final String UNIT_DAY = "☀";
    private static final String UNIT_HOUR = "★";
    private static final String UNIT_MINUTE = "✰";
    private static final String UNIT_SECOND_DEFAULT = "☆}";
    // 时间计算常量
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;
    private static final long MILLIS_PER_HOUR = 60 * 60 * 1000L;
    private static final long MILLIS_PER_MINUTE = 60 * 1000L;
    private static final long MILLIS_PER_SECOND = 1000L;
    // 空字符串常量（替代 TextUtils.EMPTY，保证 Java 7 兼容）
    private static final String EMPTY_STRING = "";

    // ================================== 核心格式化方法（电量列表格式化）=================================
    /**
     * 格式化电量使用时间列表为单行字符串
     * @param batteryInfoList 电池信息列表（非空）
     * @return 格式化后的单行字符串，格式："电量% 时间跨度 电量% 时间跨度 ..."
     */
    public static String formatPCMListString(ArrayList<BatteryInfoBean> batteryInfoList) {
        LogUtils.d(TAG, "【formatPCMListString】调用开始 | 列表大小=" + (batteryInfoList != null ? batteryInfoList.size() : null));
        // 1. 参数校验
        if (batteryInfoList == null || batteryInfoList.size() < 2) {
            LogUtils.e(TAG, "【formatPCMListString】参数异常：列表为空或长度不足2");
            return EMPTY_STRING;
        }

        String result = EMPTY_STRING;
        // 2. 遍历列表，拼接字符串（倒序拼接）
        for (int i = 0; i < batteryInfoList.size() - 1; i++) {
            BatteryInfoBean currentBean = batteryInfoList.get(i);
            BatteryInfoBean nextBean = batteryInfoList.get(i + 1);
            // 空指针防护
            if (currentBean == null || nextBean == null) {
                LogUtils.w(TAG, "【formatPCMListString】列表项为空，跳过当前索引：" + i);
                continue;
            }
            // 获取电量和时间跨度
            int batteryValue = currentBean.getBatteryValue();
            String timeSpan = getTimespanDifference(currentBean.getTimeStamp(), nextBean.getTimeStamp());
            // 倒序拼接
            result = batteryValue + "% " + timeSpan + " " + result;
            LogUtils.d(TAG, "【formatPCMListString】循环拼接 | 索引=" + i + " | 电量=" + batteryValue + "% | 时间跨度=" + timeSpan);
        }

        LogUtils.d(TAG, "【formatPCMListString】格式化完成 | 结果长度=" + result.length());
        return result;
    }

    /**
     * 格式化电量使用时间列表为带换行的字符串
     * @param batteryInfoList 电池信息列表（非空）
     * @return 格式化后的带换行字符串，每行一个电量和时间跨度
     */
    public static String formatPCMListStringWithEnter(ArrayList<BatteryInfoBean> batteryInfoList) {
        LogUtils.d(TAG, "【formatPCMListStringWithEnter】调用开始 | 列表大小=" + (batteryInfoList != null ? batteryInfoList.size() : null));
        // 1. 参数校验
        if (batteryInfoList == null || batteryInfoList.size() < 2) {
            LogUtils.e(TAG, "【formatPCMListStringWithEnter】参数异常：列表为空或长度不足2");
            return EMPTY_STRING;
        }

        String result = EMPTY_STRING;
        // 2. 遍历列表，拼接字符串（倒序拼接，带换行）
        for (int i = 0; i < batteryInfoList.size() - 1; i++) {
            BatteryInfoBean currentBean = batteryInfoList.get(i);
            BatteryInfoBean nextBean = batteryInfoList.get(i + 1);
            // 空指针防护
            if (currentBean == null || nextBean == null) {
                LogUtils.w(TAG, "【formatPCMListStringWithEnter】列表项为空，跳过当前索引：" + i);
                continue;
            }
            // 获取电量和时间跨度
            int batteryValue = currentBean.getBatteryValue();
            String timeSpan = getTimespanDifference(currentBean.getTimeStamp(), nextBean.getTimeStamp());
            // 倒序拼接（带换行）
            result = "\n" + batteryValue + "%\n        " + timeSpan + " " + result;
            LogUtils.d(TAG, "【formatPCMListStringWithEnter】循环拼接 | 索引=" + i + " | 电量=" + batteryValue + "% | 时间跨度=" + timeSpan);
        }

        LogUtils.d(TAG, "【formatPCMListStringWithEnter】格式化完成 | 结果长度=" + result.length());
        return result;
    }

    // ================================== 时间跨度计算方法（核心工具方法）=================================
    /**
     * 计算两个时间戳之间的跨度并格式化为指定字符串
     * @param start 开始时间戳（毫秒）
     * @param end 结束时间戳（毫秒）
     * @return 格式化的时间跨度字符串，格式：{天☀时★分✰秒} 或 {☆}（当时间差为0时）
     */
    public static String getTimespanDifference(long start, long end) {
        LogUtils.d(TAG, "【getTimespanDifference】调用开始 | 开始时间戳=" + start + " | 结束时间戳=" + end);
        long between = end - start;
        LogUtils.d(TAG, "【getTimespanDifference】时间差（毫秒）=" + between);

        // 计算天、时、分、秒
        long day = between / MILLIS_PER_DAY;
        long hour = (between % MILLIS_PER_DAY) / MILLIS_PER_HOUR;
        long min = (between % MILLIS_PER_HOUR) / MILLIS_PER_MINUTE;
        long sec = (between % MILLIS_PER_MINUTE) / MILLIS_PER_SECOND;

        // 拼接结果字符串
        StringBuilder result = new StringBuilder("{");
        boolean hasHigherUnit = false;

        // 拼接天
        if (day > 0) {
            result.append(String.format(Locale.getDefault(), "%d%s", day, UNIT_DAY));
            hasHigherUnit = true;
        }
        // 拼接时（当天>0或后续有单位时）
        if (hour > 0 || hasHigherUnit) {
            result.append(String.format(Locale.getDefault(), "%d%s", hour, UNIT_HOUR));
            hasHigherUnit = true;
        }
        // 拼接分（当时>0或后续有单位时）
        if (min > 0 || hasHigherUnit) {
            result.append(String.format(Locale.getDefault(), "%d%s", min, UNIT_MINUTE));
            hasHigherUnit = true;
        }
        // 拼接秒或默认值
        if (hasHigherUnit) {
            result.append(String.format(Locale.getDefault(), "%d}", sec));
        } else {
            result.append(UNIT_SECOND_DEFAULT);
        }

        String timeSpan = result.toString();
        LogUtils.d(TAG, "【getTimespanDifference】计算完成 | 时间跨度=" + timeSpan);
        return timeSpan;
    }
}

