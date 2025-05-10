package cc.winboll.studio.mymessagemanager.utils;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/07/24 16:20:13
 * @Describe 用户视觉系统保护模式工具集
 */
import java.util.Random;

public class UserVisionSystemProtectModeUtil {

    public static final String TAG = "UserVisionSystemProtectModeUtil";

    public static final String PreviewShuffleSMS(String szSMSText, String szRefuseChars, String szReplaceChars) {
        String szPreview;
        // 将字符串转换为字符数组
        char[] charArray = szSMSText.toCharArray();
        // 打乱字符数组
        shuffleArray(charArray);
        // 构建新的字符串并打印
        szPreview = new String(charArray);
        szPreview = useProtectedCharsRule(szPreview, szRefuseChars, szReplaceChars);
        return szPreview;
    }

    //
    // 打乱字符数组的方法
    //
    // @param array 要被打乱的字符数组
    //
    public static void shuffleArray(char[] array) {
        // 创建随机数生成器
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            // 生成一个随机索引j，范围在0到i之间（包括i）
            int j = random.nextInt(i + 1);

            // 交换array[i]和array[j]的位置
            char temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    public static String useProtectedCharsRule(String szContent, String szRefuseChars, String szReplaceChars) {
        // 正则表达式模式 (寻找 szProtectChars 其中一个字符)
        String pattern = "[" + szRefuseChars + "]"; 

        // 替换模式后的字符串
        String szProtectedContent = szContent.replaceAll(pattern, szReplaceChars);
        return szProtectedContent;
    }
}
