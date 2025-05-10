package cc.winboll.studio.mymessagemanager.utils;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/07/19 14:30:57
 * @Describe 手机联系人工具类
 */
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import cc.winboll.studio.mymessagemanager.beans.PhoneBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PhoneUtil {

    public static String TAG = "PhoneUtil";

    // 号码
    public final static String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    // 联系人姓名
    public final static String DISPLAY_NAME = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;

    // 上下文对象
    Context mContext;
    // 联系人提供者的Uri
    Uri mUriPhoneContent = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

    public PhoneUtil(Context context) {
        mContext = context;
    }

    // 读取所有联系人
    //
    public List<PhoneBean> getPhoneList() {
        List<PhoneBean> listPhoneBean = new ArrayList<>();
        ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = cr.query(mUriPhoneContent, new String[]{NUMBER, DISPLAY_NAME}, null, null, null);
        while (cursor.moveToNext()) {
			PhoneBean phoneBean = new PhoneBean(cursor.getString(1), cursor.getString(0).replaceAll("\\s", ""));
			listPhoneBean.add(phoneBean);

        }
		cursor.close();

        Collections.sort(listPhoneBean, new Comparator<PhoneBean>() {
                @Override
                public int compare(PhoneBean o1, PhoneBean o2) {
                    return o1.getTelPhone().compareTo(o2.getTelPhone());
                }
            });

        return listPhoneBean;
    }

    public boolean isPhoneInContacts(String szPhone) {
        List<PhoneBean> listPhoneDto = getPhoneList();
        for (int i = 0; i < listPhoneDto.size(); i++) {
            if (listPhoneDto.get(i).getTelPhone().equals(szPhone)) {
                return true;
            }
        }
        return false;
    }

}
