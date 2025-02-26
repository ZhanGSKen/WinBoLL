package cc.winboll.studio.contacts.beans;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/26 13:37:00
 * @Describe ContactModel
 */
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class ContactModel {

    public static final String TAG = "ContactModel";

    private String name;
    private String number;
    private String pinyin;

    public ContactModel(String name, String number) {
        this.name = name;
        this.number = number;
        this.pinyin = convertToPinyin(name);
    }

    private String convertToPinyin(String chinese) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        StringBuilder pinyin = new StringBuilder();
        for (int i = 0; i < chinese.length(); i++) {
            char ch = chinese.charAt(i);
            if (Character.toString(ch).matches("[\\u4e00 - \\u9fa5]")) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(ch, format);
                    if (pinyinArray != null) {
                        pinyin.append(pinyinArray[0]);
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pinyin.append(ch);
            }
        }
        return pinyin.toString();
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getPinyin() {
        return pinyin;
    }
}

