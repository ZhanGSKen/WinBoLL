package cc.winboll.studio.contacts.utils;

import android.widget.EditText;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/04/13 00:59:13
 * @Describe Int类型数字输入框工具集：安全读取 EditText 中的整数内容
 */
public class EditTextIntUtils {
    // ====================== 常量定义区 ======================
    public static final String TAG = "EditTextIntUtils";
    // 默认返回值：读取失败时返回
    private static final int DEFAULT_INT_VALUE = 0;

    // ====================== 工具方法区 ======================
    /**
     * 从 EditText 中安全读取整数
     * @param editText 目标输入框
     * @return 输入框中的整数，读取失败返回 0
     */
    public static int getIntFromEditText(EditText editText) {
        // 空值校验：防止 EditText 为 null 导致空指针
        if (editText == null) {
            LogUtils.w(TAG, "getIntFromEditText: EditText 实例为 null，返回默认值 " + DEFAULT_INT_VALUE);
            return DEFAULT_INT_VALUE;
        }

        // 获取并去除首尾空格
        String inputStr = editText.getText().toString().trim();
        LogUtils.d(TAG, "getIntFromEditText: 输入框原始内容 | " + inputStr);

        // 校验空字符串
        if (inputStr.isEmpty()) {
            LogUtils.w(TAG, "getIntFromEditText: 输入框内容为空，返回默认值 " + DEFAULT_INT_VALUE);
            return DEFAULT_INT_VALUE;
        }

        // 安全转换整数，捕获格式异常
        try {
            int result = Integer.parseInt(inputStr);
            LogUtils.d(TAG, "getIntFromEditText: 转换成功 | 结果=" + result);
            return result;
        } catch (NumberFormatException e) {
            LogUtils.e(TAG, "getIntFromEditText: 内容不是有效整数 | 输入内容=" + inputStr, e);
            return DEFAULT_INT_VALUE;
        }
    }
}

