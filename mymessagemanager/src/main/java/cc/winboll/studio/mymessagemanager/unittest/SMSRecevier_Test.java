package cc.winboll.studio.mymessagemanager.unittest;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/25 19:02:15
 * @Describe SMSRecevier 测试类
 */
import cc.winboll.studio.mymessagemanager.utils.SMSReceiveRuleUtil;
import android.content.Context;
import cc.winboll.studio.shared.log.LogUtils;
import cc.winboll.studio.mymessagemanager.receivers.SMSRecevier;

public class SMSRecevier_Test {

    public static final String TAG = "SMSRecevier_Test";

    public static void main(Context context) {
        String szSmsBody = "无影无迹";
        String szSmsAddress = "无名小辈";
        test1(context, szSmsBody, szSmsAddress);
        
        szSmsBody = "无影无迹";
        szSmsAddress = "1?0";
        test1(context, szSmsBody, szSmsAddress);
        
        szSmsBody = "无影无迹";
        szSmsAddress = "10000";
        test1(context, szSmsBody, szSmsAddress);
        
        szSmsBody = "【UC】无影无迹";
        szSmsAddress = "无名小辈";
        test1(context, szSmsBody, szSmsAddress);
        
        szSmsBody = "【UC】无影无迹";
        szSmsAddress = "10000";
        test1(context, szSmsBody, szSmsAddress);
        
    }
    
    public static void test1(Context context, String szSmsBody, String szSmsAddress) {
        
        boolean isSMSOK = SMSRecevier.checkIsSMSOK(context, szSmsBody, szSmsAddress);
        LogUtils.d(TAG, String.format("szSmsBody : %s\nszSmsAddress : %s\nisSMSOK : %s", szSmsBody, szSmsAddress, isSMSOK));
    }
}
