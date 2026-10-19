package cc.winboll.studio.contacts.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import cc.winboll.studio.libappbase.LogUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/08/30 14:32
 * @Describe 联系人工具集：提供联系人查询、添加、编辑、号码格式化等功能，适配主流机型
 */
public class ContactUtils {
    // ====================== 常量定义区 ======================
    public static final String TAG = "ContactUtils";
    // 手机号正则（11位中国大陆手机号）
    private static final String REGEX_CHINA_MOBILE = "^1[0-9]{10}$";

    // ====================== 单例与成员变量区 ======================
    // 单例实例（volatile 保证多线程可见性）
    private static volatile ContactUtils sInstance;
    // 上下文（弱引用避免内存泄漏，Java7 兼容）
    private final Context mContext;
    // 缓存联系人：key=纯数字号码，value=联系人姓名
    private final Map<String, String> mContactMap = new HashMap<>();

    // ====================== 单例构造区 ======================
    /**
     * 私有构造器：初始化上下文并加载联系人
     */
    private ContactUtils(Context context) {
        // 传入应用上下文，避免Activity上下文泄漏
        this.mContext = context.getApplicationContext();
        LogUtils.d(TAG, "ContactUtils 初始化，开始加载联系人");
        reloadContacts();
    }

    /**
     * 获取单例实例（双重校验锁，Java7 安全）
     */
    public static ContactUtils getInstance(Context context) {
        if (context == null) {
            LogUtils.e(TAG, "getInstance: 上下文为null，无法创建实例");
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (sInstance == null) {
            synchronized (ContactUtils.class) {
                if (sInstance == null) {
                    sInstance = new ContactUtils(context);
                }
            }
        }
        return sInstance;
    }

    // ====================== 联系人缓存与查询区 ======================
    /**
     * 重新加载联系人到缓存
     */
    public void reloadContacts() {
        LogUtils.d(TAG, "reloadContacts: 开始刷新联系人缓存");
        mContactMap.clear();
        readContactsFromSystem();
        LogUtils.d(TAG, "reloadContacts: 联系人缓存刷新完成，共缓存 " + mContactMap.size() + " 个联系人");
    }

    /**
     * 从系统通讯录读取所有联系人（核心方法）
     */
    private void readContactsFromSystem() {
        ContentResolver resolver = mContext.getContentResolver();
        // 只查询姓名和号码字段，减少IO开销
        String[] projection = {
			ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
			ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        Cursor cursor = null;
        try {
            cursor = resolver.query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				projection,
				null,
				null,
				null
            );

            if (cursor == null) {
                LogUtils.w(TAG, "readContactsFromSystem: 通讯录查询Cursor为null，可能缺少权限");
                return;
            }

            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                if (phone != null) {
                    String simplePhone = formatToSimplePhoneNumber(phone);
                    mContactMap.put(simplePhone, name != null ? name : "[UnknownName]");
                    LogUtils.v(TAG, "readContactsFromSystem: 缓存联系人 - 号码：" + simplePhone + "，姓名：" + name);
                }
            }
        } catch (SecurityException e) {
            LogUtils.e(TAG, "readContactsFromSystem: 读取通讯录失败，缺少 READ_CONTACTS 权限", e);
        } catch (Exception e) {
            LogUtils.e(TAG, "readContactsFromSystem: 读取通讯录异常", e);
        } finally {
            if (cursor != null) {
                cursor.close(); // 确保游标关闭，避免内存泄漏
            }
        }
    }

    /**
     * 从缓存中获取联系人姓名
     */
    public String getContactName(String phone) {
        if (phone == null) {
            LogUtils.w(TAG, "getContactName: 输入号码为null");
            return "[NotInContacts]";
        }
        String simplePhone = formatToSimplePhoneNumber(phone);
        String name = mContactMap.get(simplePhone);
        LogUtils.d(TAG, "getContactName: 查询号码 " + simplePhone + "，姓名：" + (name == null ? "[NotInContacts]" : name));
        return name == null ? "[NotInContacts]" : name;
    }

    // ====================== 号码格式化工具区 ======================
    /**
     * 格式化号码为纯数字（去除所有非数字字符）
     */
    public static String formatToSimplePhoneNumber(String number) {
        if (number == null || number.isEmpty()) {
            LogUtils.w(TAG, "formatToSimplePhoneNumber: 输入号码为空");
            return "";
        }
        String simpleNumber = number.replaceAll("[^0-9]", "");
        LogUtils.v(TAG, "formatToSimplePhoneNumber: 原号码 " + number + " → 纯数字号码 " + simpleNumber);
        return simpleNumber;
    }

    /**
     * 格式化11位手机号为带空格格式（如：138 0000 1234）
     */
    public static String formatToSpacePhoneNumber(String simpleNumber) {
        if (simpleNumber == null || !simpleNumber.matches(REGEX_CHINA_MOBILE)) {
            LogUtils.v(TAG, "formatToSpacePhoneNumber: 号码不符合11位手机号格式，无需格式化");
            return simpleNumber;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(simpleNumber.substring(0, 3))
			.append(" ")
			.append(simpleNumber.substring(3, 7))
			.append(" ")
			.append(simpleNumber.substring(7, 11));

        String formatted = sb.toString();
        LogUtils.v(TAG, "formatToSpacePhoneNumber: 纯数字号码 " + simpleNumber + " → 带空格号码 " + formatted);
        return formatted;
    }

    // ====================== 联系人查询（直接查系统，不走缓存）区 ======================
    /**
     * 直接查询系统通讯录获取联系人姓名（按原始号码匹配）
     */
    public static String getDisplayNameByPhone(Context context, String phoneNumber) {
        if (context == null || phoneNumber == null) {
            LogUtils.w(TAG, "getDisplayNameByPhone: 上下文或号码为空");
            return null;
        }

        ContentResolver resolver = context.getContentResolver();
        String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
        Cursor cursor = null;
        String displayName = null;

        try {
            cursor = resolver.query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				projection,
				ContactsContract.CommonDataKinds.Phone.NUMBER + "=?",
				new String[]{phoneNumber},
				null
            );

            if (cursor != null && cursor.moveToFirst()) {
                displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            }
            LogUtils.d(TAG, "getDisplayNameByPhone: 按原始号码 " + phoneNumber + " 查询，姓名：" + displayName);
        } catch (SecurityException e) {
            LogUtils.e(TAG, "getDisplayNameByPhone: 缺少 READ_CONTACTS 权限", e);
        } catch (Exception e) {
            LogUtils.e(TAG, "getDisplayNameByPhone: 查询异常", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return displayName;
    }

    /**
     * 直接查询系统通讯录获取联系人姓名（按纯数字号码匹配）
     */
    public static String getDisplayNameByPhoneSimple(Context context, String phoneNumber) {
        if (phoneNumber == null) {
            LogUtils.w(TAG, "getDisplayNameByPhoneSimple: 输入号码为null");
            return null;
        }
        String simplePhone = formatToSimplePhoneNumber(phoneNumber);
        LogUtils.d(TAG, "getDisplayNameByPhoneSimple: 按纯数字号码 " + simplePhone + " 查询");
        return getDisplayNameByPhone(context, simplePhone);
    }

    /**
     * 判断号码是否在系统通讯录中
     */
    public static boolean isPhoneInContacts(Context context, String phoneNumber) {
        if (context == null || phoneNumber == null) {
            LogUtils.w(TAG, "isPhoneInContacts: 上下文或号码为空");
            return false;
        }

        String simplePhone = formatToSimplePhoneNumber(phoneNumber);
        String displayName = getDisplayNameByPhone(context, simplePhone);

        if (displayName == null) {
            LogUtils.d(TAG, "isPhoneInContacts: 号码 " + simplePhone + " 未找到联系人（纯数字匹配）");
            String spacePhone = formatToSpacePhoneNumber(simplePhone);
            displayName = getDisplayNameByPhone(context, spacePhone);
            if (displayName == null) {
                LogUtils.d(TAG, "isPhoneInContacts: 号码 " + spacePhone + " 未找到联系人（带空格匹配）");
                return false;
            }
        }

        LogUtils.d(TAG, "isPhoneInContacts: 号码 " + simplePhone + " 已在联系人中，姓名：" + displayName);
        return true;
    }

    /**
     * 通过电话号码查询联系人ID（适配定制机型）
     */
    public static Long getContactIdByPhone(Context context, String phoneNumber) {
        if (context == null || phoneNumber == null || phoneNumber.isEmpty()) {
            LogUtils.w(TAG, "getContactIdByPhone: 上下文或号码为空");
            return -1L;
        }

        ContentResolver resolver = context.getContentResolver();
        Uri queryUri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] projection = {ContactsContract.CommonDataKinds.Phone.CONTACT_ID};
        Cursor cursor = null;
        Long contactId = -1L;

        try {
            cursor = resolver.query(queryUri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
            }
            LogUtils.d(TAG, "getContactIdByPhone: 号码 " + phoneNumber + " 对应的联系人ID：" + contactId);
        } catch (SecurityException e) {
            LogUtils.e(TAG, "getContactIdByPhone: 缺少 READ_CONTACTS 权限", e);
        } catch (Exception e) {
            LogUtils.e(TAG, "getContactIdByPhone: 查询异常", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return contactId;
    }

    // ====================== 联系人跳转工具区 ======================
    /**
     * 跳转至系统添加联系人界面
     * @param context 上下文
     * @param phoneNumber 预填号码（可为null）
     */
    public static void jumpToAddContact(Context context, String phoneNumber) {
        if (context == null) {
            LogUtils.e(TAG, "jumpToAddContact: 上下文为null");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType("vnd.android.cursor.dir/person");
        if (phoneNumber != null) {
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
            LogUtils.d(TAG, "jumpToAddContact: 跳转添加联系人，预填号码：" + phoneNumber);
        } else {
            LogUtils.d(TAG, "jumpToAddContact: 跳转添加联系人，无预填号码");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 支持非Activity上下文调用
        context.startActivity(intent);
    }

    /**
     * 跳转至系统编辑联系人界面（适配小米等定制机型）
     * @param context 上下文
     * @param phoneNumber 待编辑号码（必传）
     * @param contactId 联系人ID（可选，优先使用）
     */
    public static void jumpToEditContact(Context context, String phoneNumber, Long contactId) {
        if (context == null) {
            LogUtils.e(TAG, "jumpToEditContact: 上下文为null");
            return;
        }

        // 校验必要参数
        if (contactId == null || contactId <= 0) {
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                LogUtils.e(TAG, "jumpToEditContact: 联系人ID和号码均为空，无法编辑");
                return;
            }
        }

        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // 优先通过ID定位（精准）
        if (contactId != null && contactId > 0) {
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            intent.setData(contactUri);
            LogUtils.d(TAG, "jumpToEditContact: 通过ID " + contactId + " 定位联系人，准备编辑");
        } else {
            // 通过号码定位
            Uri phoneUri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            intent.setData(phoneUri);
            LogUtils.d(TAG, "jumpToEditContact: 通过号码 " + phoneNumber + " 定位联系人，准备编辑");
        }

        // 预填最新号码
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            intent.putExtra(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber);
            intent.putExtra(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        }

        context.startActivity(intent);
    }
}

