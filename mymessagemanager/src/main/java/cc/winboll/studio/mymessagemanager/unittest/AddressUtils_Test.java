package cc.winboll.studio.mymessagemanager.unittest;
import android.content.Context;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.mymessagemanager.utils.AddressUtils;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/01 13:07:32
 * @Describe AddressUtils Test
 */
public class AddressUtils_Test {

    public static final String TAG = "AddressUtils_Test";

    public static void main(Context context) {
        String szSmsBody = "无影无迹";
        String szSmsAddress = "无名小辈";
        LogUtils.d(TAG, String.format("szSmsAddress %s\n getFormattedAddress : %s", szSmsAddress, AddressUtils.getFormattedAddress(szSmsAddress)));
        szSmsAddress = "13172887736";
        LogUtils.d(TAG, String.format("szSmsAddress %s\n getFormattedAddress : %s", szSmsAddress, AddressUtils.getFormattedAddress(szSmsAddress)));
        szSmsAddress = "+8613172887736";
        LogUtils.d(TAG, String.format("szSmsAddress %s\n getFormattedAddress : %s", szSmsAddress, AddressUtils.getFormattedAddress(szSmsAddress)));
        szSmsAddress = "8613172887736";
        LogUtils.d(TAG, String.format("szSmsAddress %s\n getFormattedAddress : %s", szSmsAddress, AddressUtils.getFormattedAddress(szSmsAddress)));
        
    }

}
