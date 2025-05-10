package cc.winboll.studio.contacts.utils;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/04/13 01:16:28
 * @Describe Int数字操作工具集
 */
import cc.winboll.studio.libappbase.LogUtils;

public class IntUtils {

    public static final String TAG = "IntUtils";

    public static int getIntInRange(int origin, int range_a, int range_b) {
        int min = Math.min(range_a, range_b);
        int max = Math.max(range_a, range_b);
        int res = Math.min(origin, max);
        res = Math.max(res, min);
        return res;
    }

    public static void unittest_getIntInRange() {
        LogUtils.d(TAG, String.format("getIntInRange(-100, 5, 10); %d", getIntInRange(-100, 5, 10)));
        LogUtils.d(TAG, String.format("getIntInRange(8, 5, 10); %d", getIntInRange(8, 5, 10)));
        LogUtils.d(TAG, String.format("getIntInRange(200, 5, 10); %d", getIntInRange(200, 5, 10)));
        LogUtils.d(TAG, String.format("getIntInRange(-100, -5, 10); %d", getIntInRange(-100, -5, 10)));
        LogUtils.d(TAG, String.format("getIntInRange(9, -5, 10); %d", getIntInRange(9, -5, 10)));
        LogUtils.d(TAG, String.format("getIntInRange(100, -5, 10); %d", getIntInRange(100, -5, 10)));

        LogUtils.d(TAG, String.format("getIntInRange(500, 5, -10); %d", getIntInRange(500, 5, -10)));
        LogUtils.d(TAG, String.format("getIntInRange(4, 5, -10); %d", getIntInRange(4, 5, -10)));
        LogUtils.d(TAG, String.format("getIntInRange(-20, 5, -10); %d", getIntInRange(-20, 5, -10)));
        LogUtils.d(TAG, String.format("getIntInRange(500, 50, 10); %d", getIntInRange(500, 50, 10)));
        LogUtils.d(TAG, String.format("getIntInRange(30, 50, 10); %d", getIntInRange(30, 50, 10)));
        LogUtils.d(TAG, String.format("getIntInRange(6, 50, 10); %d", getIntInRange(6, 50, 10)));
    }
}
