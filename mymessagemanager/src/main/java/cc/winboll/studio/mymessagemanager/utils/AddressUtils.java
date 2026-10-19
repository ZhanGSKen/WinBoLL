package cc.winboll.studio.mymessagemanager.utils;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/01 13:03:16
 * @Describe 通信录地址工具
 */
public class AddressUtils {
    
    public static final String TAG = "AddressUtils";
    
    public static String getFormattedAddress(String address) {
        if (address != null && address.matches("[+]?\\d+")) {
            return address;
        } else {
            return "【" + address + "】";
        }
    }
    
}
