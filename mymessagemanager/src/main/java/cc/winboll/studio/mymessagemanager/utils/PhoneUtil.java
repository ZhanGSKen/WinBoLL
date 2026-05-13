package cc.winboll.studio.mymessagemanager.utils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/08/30 14:32
 * @Describe 手机联系人工具类
 */
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.mymessagemanager.beans.PhoneBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

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
    public List<PhoneBean> getPhoneList() {
        List<PhoneBean> listPhoneBean = new ArrayList<>();
        ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = cr.query(mUriPhoneContent, new String[]{NUMBER, DISPLAY_NAME}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                // 去除号码中的空格
                String phone = cursor.getString(0).replaceAll("\\s", "");
                String name = cursor.getString(1);
                PhoneBean phoneBean = new PhoneBean(name, phone);
                listPhoneBean.add(phoneBean);
            }
            cursor.close();
        }

        // 按电话号码排序
        Collections.sort(listPhoneBean, new Comparator<PhoneBean>() {
				@Override
				public int compare(PhoneBean o1, PhoneBean o2) {
					return o1.getTelPhone().compareTo(o2.getTelPhone());
				}
			});

        return listPhoneBean;
    }

    /**
     * 根据联系人名称查询号码（兼容拼音查询）
     * @param keyword 搜索关键词（支持汉字、拼音、拼音首字母）
     * @return 匹配的联系人列表（包含姓名和号码）
     */
    public List<PhoneBean> getPhonesByName(String keyword) {
        List<PhoneBean> result = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return result; // 关键词为空，返回空列表
        }

        // 获取所有联系人
        List<PhoneBean> allContacts = getPhoneList();
        // 统一转为小写，忽略大小写
        String keywordLower = keyword.trim().toLowerCase();

        for (PhoneBean contact : allContacts) {
            String name = contact.getName();
            if (name == null || name.isEmpty()) {
                continue;
            }

            // 1. 直接匹配姓名（包含关键词）
            if (name.toLowerCase().contains(keywordLower)) {
                result.add(contact);
                continue;
            }

            // 2. 匹配姓名的全拼（包含关键词）
            String namePinyin = getPinyin(name).toLowerCase();
            if (namePinyin.contains(keywordLower)) {
                result.add(contact);
                continue;
            }

            // 3. 匹配姓名的拼音首字母（包含关键词）
            String namePinyinFirstLetter = getPinyinFirstLetter(name).toLowerCase();
            if (namePinyinFirstLetter.contains(keywordLower)) {
                result.add(contact);
                continue;
            }
        }

        return result;
    }

    /**
     * 将汉字转为全拼（不带声调，小写）
     * 例如："张三" → "zhangsan"
     */
    private String getPinyin(String chinese) {
        StringBuilder pinyin = new StringBuilder();
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE); // 小写
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE); // 不带声调

        char[] chars = chinese.toCharArray();
        for (char c : chars) {
            // 如果是汉字，转换为拼音；否则直接拼接（如字母、数字、符号）
            if (Character.toString(c).matches("[\\u4e00-\\u9fa5]")) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        pinyin.append(pinyinArray[0]); // 取第一个拼音（多音字默认取第一个）
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    LogUtils.e(TAG, "拼音转换失败：" + e.getMessage());
                }
            } else {
                pinyin.append(c);
            }
        }
        return pinyin.toString();
    }

    /**
     * 将汉字转为拼音首字母（小写）
     * 例如："张三" → "zs"
     */
    private String getPinyinFirstLetter(String chinese) {
        StringBuilder firstLetters = new StringBuilder();
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        char[] chars = chinese.toCharArray();
        for (char c : chars) {
            if (Character.toString(c).matches("[\\u4e00-\\u9fa5]")) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        // 取拼音首字母（如"zhang" → "z"）
                        firstLetters.append(pinyinArray[0].charAt(0));
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    LogUtils.e(TAG, "拼音首字母转换失败：" + e.getMessage());
                }
            } else {
                // 非汉字直接拼接首字符（如"李3" → "l3"）
                firstLetters.append(c);
            }
        }
        return firstLetters.toString();
    }

    public boolean isPhoneInContacts(String szPhone) {
        List<PhoneBean> listPhoneDto = getPhoneList();
        LogUtils.d(TAG, String.format("isPhoneInContacts(...) listPhoneDto.size() %d", listPhoneDto.size()));
        for (int i = 0; i < listPhoneDto.size(); i++) {
            if (isTheSamePhoneNumber(listPhoneDto.get(i).getTelPhone(), szPhone)) {
                return true;
            }
        }
        return false;
    }

    public String getNameByPhone(String szPhone) {
        if (szPhone == null || szPhone.equals("")) {
            return "";
        }

        List<PhoneBean> listPhoneDto = getPhoneList();
        LogUtils.d(TAG, String.format("getNameByPhone(...) listPhoneDto.size() %d", listPhoneDto.size()));
        for (int i = 0; i < listPhoneDto.size(); i++) {
            if (isTheSamePhoneNumber(listPhoneDto.get(i).getTelPhone(), szPhone)) {
                return listPhoneDto.get(i).getName();
            }
        }
        return "";
    }

    public boolean isTheSamePhoneNumber(String szNum1, String szNum2) {
        if (szNum1.equals(szNum2)) {
            LogUtils.d(TAG, "szNum1.equals(szNum2)");
            return true;
        }

        if (UnitAreaUtils.getInstance(mContext).isCurrentUnitAreaNumber(szNum1)) {
            if (szNum1.equals(UnitAreaUtils.getInstance(mContext).genCurrentUnitAreaNumber(szNum2))) {
                LogUtils.d(TAG, "szNum1.equals(UnitAreaUtils.genCurrentUnitAreaNumber(szNum2))");
                return true;
            }
        }

        if (UnitAreaUtils.getInstance(mContext).isCurrentUnitAreaNumber(szNum2)) {
            if (szNum2.equals(UnitAreaUtils.getInstance(mContext).genCurrentUnitAreaNumber(szNum1))) {
                LogUtils.d(TAG, "szNum2.equals(UnitAreaUtils.genCurrentUnitAreaNumber(szNum1))");
                return true;
            }
        }

        LogUtils.d(TAG, "isTheSamePhoneNumber(...) return false;");
        return false;
    }

    // 检验电话号码是否是数字
    public static boolean isPhoneByDigit(String szPhone) {
        if (!RegexPPiUtils.isPPiOK(szPhone)) {
            return false;
        }
        String regex = "[+]?\\d+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(szPhone);
        LogUtils.d(TAG, String.format("matcher.matches() : %s", matcher.matches()));
        return matcher.matches();
    }
}

