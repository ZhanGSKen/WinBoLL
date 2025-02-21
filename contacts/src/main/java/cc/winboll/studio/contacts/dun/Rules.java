package cc.winboll.studio.contacts.dun;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/21 06:15:10
 * @Describe 云盾防御规则
 */
import java.util.regex.Pattern;

public class Rules {

    public static final String TAG = "Rules";

    public static boolean isValidPhoneNumber(String phoneNumber) {
        // 中国手机号码正则表达式，以1开头，第二位可以是3、4、5、6、7、8、9，后面跟9位数字
        String regex = "^1[3-9]\\d{9}$";
        return Pattern.matches(regex, phoneNumber);
    }

    public static boolean isAllowed(String phoneNumber) {
        return isValidPhoneNumber(phoneNumber);
    }
}
