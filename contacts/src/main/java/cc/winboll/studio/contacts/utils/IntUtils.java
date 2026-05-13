package cc.winboll.studio.contacts.utils;

import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/04/13 01:16:28
 * @Describe Int数字操作工具集：提供整数范围限制、数值边界校准功能
 */
public class IntUtils {
    // ====================== 常量定义区 ======================
    public static final String TAG = "IntUtils";

    // ====================== 核心工具方法区 ======================
    /**
     * 将整数限制在指定区间内，自动校准超出边界的数值
     * @param origin  原始整数
     * @param range_a 区间端点1（无需区分大小）
     * @param range_b 区间端点2（无需区分大小）
     * @return 校准后的整数，结果始终在 [min(range_a,range_b), max(range_a,range_b)] 内
     */
    public static int getIntInRange(int origin, int range_a, int range_b) {
        int min = Math.min(range_a, range_b);
        int max = Math.max(range_a, range_b);
        int res = Math.min(origin, max);
        res = Math.max(res, min);

        // 打印调试日志，记录参数与计算结果
        LogUtils.d(TAG, String.format("getIntInRange: 原始值=%d, 区间=[%d,%d], 校准后=%d",
									  origin, min, max, res));
        return res;
    }

    // ====================== 单元测试方法区 ======================
    /**
     * 单元测试：验证 getIntInRange 方法在不同场景下的正确性
     */
    public static void unittest_getIntInRange() {
        LogUtils.i(TAG, "unittest_getIntInRange: 开始执行单元测试");

        // 正数区间测试
        LogUtils.d(TAG, String.format("测试1: getIntInRange(-100, 5, 10) = %d", getIntInRange(-100, 5, 10)));
        LogUtils.d(TAG, String.format("测试2: getIntInRange(8, 5, 10) = %d", getIntInRange(8, 5, 10)));
        LogUtils.d(TAG, String.format("测试3: getIntInRange(200, 5, 10) = %d", getIntInRange(200, 5, 10)));

        // 跨正负区间测试
        LogUtils.d(TAG, String.format("测试4: getIntInRange(-100, -5, 10) = %d", getIntInRange(-100, -5, 10)));
        LogUtils.d(TAG, String.format("测试5: getIntInRange(9, -5, 10) = %d", getIntInRange(9, -5, 10)));
        LogUtils.d(TAG, String.format("测试6: getIntInRange(100, -5, 10) = %d", getIntInRange(100, -5, 10)));

        // 端点顺序颠倒测试
        LogUtils.d(TAG, String.format("测试7: getIntInRange(500, 5, -10) = %d", getIntInRange(500, 5, -10)));
        LogUtils.d(TAG, String.format("测试8: getIntInRange(4, 5, -10) = %d", getIntInRange(4, 5, -10)));
        LogUtils.d(TAG, String.format("测试9: getIntInRange(-20, 5, -10) = %d", getIntInRange(-20, 5, -10)));

        // 大数区间测试
        LogUtils.d(TAG, String.format("测试10: getIntInRange(500, 50, 10) = %d", getIntInRange(500, 50, 10)));
        LogUtils.d(TAG, String.format("测试11: getIntInRange(30, 50, 10) = %d", getIntInRange(30, 50, 10)));
        LogUtils.d(TAG, String.format("测试12: getIntInRange(6, 50, 10) = %d", getIntInRange(6, 50, 10)));

        LogUtils.i(TAG, "unittest_getIntInRange: 单元测试执行完毕");
    }
}

