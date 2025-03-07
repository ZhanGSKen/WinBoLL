package cc.winboll.studio.contacts.utils;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/06 21:08:16
 * @Describe ContactUtils
 */
public class ContactUtils {

    public static final String TAG = "ContactUtils";

    Map<String, String> contactMap = new HashMap<>();

    static volatile ContactUtils _ContactUtils;
    Context mContext;
    ContactUtils(Context context) {
        mContext = context;
        relaodContacts();
    }
    public synchronized static ContactUtils getInstance(Context context) {
        if (_ContactUtils == null) {
            _ContactUtils = new ContactUtils(context);
        }
        return _ContactUtils;
    }

    public void relaodContacts() {
        readContacts();
    }

    private void readContacts() {
        contactMap.clear();
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                              null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                //Map<String, String> contactMap = new HashMap<>();
                contactMap.put(formatToSimplePhoneNumber(phoneNumber), displayName);
            }
            cursor.close();
        }
        // 此时 contactList 就是存储联系人信息的 Map 列表
    }

    public String getContactsName(String phone) {
        String result = contactMap.get(formatToSimplePhoneNumber(phone));
        return result == null ? "[NoInContacts]" : result;
    }

//    static String getSimplePhone(String phone) {
//        return phone.replaceAll("[+\\s]", "");
//    }
    
    public static String formatToSimplePhoneNumber(String number) {
        // 去除所有空格和非数字字符
        return number.replaceAll("[^0-9]", "");
    }
    
    public static String getDisplayNameByPhone(Context context, String phoneNumber) {
        String displayName = null;
        ContentResolver resolver = context.getContentResolver();
        String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
        Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, ContactsContract.CommonDataKinds.Phone.NUMBER + "=?", new String[]{formatToSimplePhoneNumber(phoneNumber)}, null);
        if (cursor!= null && cursor.moveToFirst()) {
            displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            cursor.close();
        }
        return displayName;
    }
    
    public static String formatToSpacePhoneNumber(String simpleNumber) {
        // 去除所有空格和非数字字符
        StringBuilder sbSpaceNumber = new StringBuilder();
        String regex = "^1[0-9]{10}$"; 
        if(simpleNumber.matches(regex)) {
            sbSpaceNumber.append(simpleNumber.substring(0,2));
            sbSpaceNumber.append(" ");
            sbSpaceNumber.append(simpleNumber.substring(3,6));
            sbSpaceNumber.append(" ");
            sbSpaceNumber.append(simpleNumber.substring(7,10));
        }
        return sbSpaceNumber.toString();
    }
}
