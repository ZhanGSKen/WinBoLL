package cc.winboll.studio.contacts.utils;

import cc.winboll.studio.libappbase.LogUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2024/12/09 19:00:21
 * @Describe 正则前置校验工具类（RegexPPi）：检验文本是否满足基础正则匹配要求
 */
public class RegexPPiUtils {
    // ====================== 常量定义区 ======================
    public static final String TAG = "RegexPPiUtils";
    // 基础匹配正则：匹配任意文本（包括空字符串）
    private static final String BASE_REGEX = ".*";
    // 预编译正则 Pattern，提升重复调用效率
    private static final Pattern BASE_PATTERN = Pattern.compile(BASE_REGEX);

    // ====================== 核心校验方法区 ======================
    /**
     * 检验文本是否满足基础正则表达式模式（.*）匹配要求
     * @param text 待校验的文本内容
     * @return 匹配结果，文本为null时返回false
     */
    public static boolean isPPiOK(String text) {
        // 空值校验，避免空指针异常
        if (text == null) {
            LogUtils.w(TAG, "isPPiOK: 待校验文本为 null，返回 false");
            return false;
        }

        // 执行正则匹配
        Matcher matcher = BASE_PATTERN.matcher(text);
        boolean isMatch = matcher.matches();

        // 打印调试日志，记录校验结果
        LogUtils.d(TAG, String.format("isPPiOK: 文本=[%s]，匹配结果=%b", text, isMatch));
        return isMatch;
    }
}

