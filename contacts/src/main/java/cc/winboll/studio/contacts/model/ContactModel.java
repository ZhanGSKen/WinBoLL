package cc.winboll.studio.contacts.model;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/08/30 14:32
 * @Describe 联系人信息数据模型，支持姓名转全拼和拼音首字母
 */
public class ContactModel {
    // ====================== 常量定义区 ======================
    public static final String TAG = "ContactModel";
    // 汉字匹配正则常量，避免重复创建
    private static final String CHINESE_CHAR_REGEX = "[\\u4e00-\\u9fa5]";

    // ====================== 成员变量区 ======================
    private String name;
    private String number;
    private String pinyin;
    private String pinyinFirstLetter;

    // ====================== 构造函数区 ======================
    public ContactModel(String name, String number) {
        LogUtils.d(TAG, "ContactModel: 开始初始化联系人模型");
        this.name = name == null ? "" : name;
        // 去除号码空格，空值处理为""
        this.number = number == null ? "" : number.replaceAll("\\s", "");
        // 初始化拼音和拼音首字母
        this.pinyin = convertToPinyin(this.name);
        this.pinyinFirstLetter = convertToPinyinFirstLetter(this.name);

        LogUtils.d(TAG, "ContactModel: 联系人初始化完成 | 姓名=" + this.name 
				   + " | 号码=" + this.number + " | 全拼=" + this.pinyin 
				   + " | 拼音首字母=" + this.pinyinFirstLetter);
    }

    // ====================== 拼音转换工具方法区 ======================
    /**
     * 姓名转为全拼（多音字默认取首个拼音）
     */
    private String convertToPinyin(String chinese) {
        LogUtils.d(TAG, "convertToPinyin: 开始转换姓名为全拼，姓名=" + chinese);
        HanyuPinyinOutputFormat format = getPinyinOutputFormat();
        StringBuilder pinyinSb = new StringBuilder();

        for (int i = 0; i < chinese.length(); i++) {
            char ch = chinese.charAt(i);
            // 仅处理汉字
            if (Character.toString(ch).matches(CHINESE_CHAR_REGEX)) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(ch, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        pinyinSb.append(pinyinArray[0]);
                        LogUtils.v(TAG, "convertToPinyin: 字符[" + ch + "]转为拼音[" + pinyinArray[0] + "]");
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    LogUtils.e(TAG, "convertToPinyin: 拼音转换异常，字符=" + ch, e);
                }
            } else {
                pinyinSb.append(ch);
                LogUtils.v(TAG, "convertToPinyin: 非汉字字符直接拼接，字符=" + ch);
            }
        }

        String result = pinyinSb.toString();
        LogUtils.d(TAG, "convertToPinyin: 全拼转换完成，结果=" + result);
        return result;
    }

    /**
     * 姓名转为拼音首字母（多音字默认取首个拼音首字母）
     */
    private String convertToPinyinFirstLetter(String chinese) {
        LogUtils.d(TAG, "convertToPinyinFirstLetter: 开始转换姓名为拼音首字母，姓名=" + chinese);
        HanyuPinyinOutputFormat format = getPinyinOutputFormat();
        StringBuilder firstLetterSb = new StringBuilder();

        for (int i = 0; i < chinese.length(); i++) {
            char ch = chinese.charAt(i);
            if (Character.toString(ch).matches(CHINESE_CHAR_REGEX)) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(ch, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        char firstChar = pinyinArray[0].charAt(0);
                        firstLetterSb.append(firstChar);
                        LogUtils.v(TAG, "convertToPinyinFirstLetter: 字符[" + ch + "]转为首字母[" + firstChar + "]");
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    LogUtils.e(TAG, "convertToPinyinFirstLetter: 拼音首字母转换异常，字符=" + ch, e);
                }
            } else {
                firstLetterSb.append(ch);
                LogUtils.v(TAG, "convertToPinyinFirstLetter: 非汉字字符直接拼接，字符=" + ch);
            }
        }

        String result = firstLetterSb.toString();
        LogUtils.d(TAG, "convertToPinyinFirstLetter: 拼音首字母转换完成，结果=" + result);
        return result;
    }

    /**
     * 获取统一的拼音输出格式（小写、无音调）
     * 抽离为公共方法，避免重复创建对象
     */
    private HanyuPinyinOutputFormat getPinyinOutputFormat() {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        return format;
    }

    // ====================== Getter 方法区 ======================
    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getPinyin() {
        return pinyin;
    }

    public String getPinyinFirstLetter() {
        return pinyinFirstLetter;
    }
}

