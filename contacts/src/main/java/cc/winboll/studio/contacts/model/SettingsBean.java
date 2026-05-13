package cc.winboll.studio.contacts.model;

import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;

import cc.winboll.studio.contacts.utils.IntUtils;
import cc.winboll.studio.libappbase.BaseBean;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/03/02 19:51:40
 * @Describe 应用设置数据模型，支持云盾防御配置与JSON序列化
 */
public class SettingsBean extends BaseBean {

    // ====================== 常量定义区 ======================
    public static final String TAG = "SettingsModel";
    // 数值范围常量
    public static final int MAX_INTRANGE = 666666;
    public static final int MIN_INTRANGE = 1;
    // JSON字段名常量，消除硬编码
    private static final String JSON_KEY_DUN_TOTAL = "dunTotalCount";
    private static final String JSON_KEY_DUN_CURRENT = "dunCurrentCount";
    private static final String JSON_KEY_DUN_RESUME_SECOND = "dunResumeSecondCount";
    private static final String JSON_KEY_DUN_RESUME_COUNT = "dunResumeCount";
    private static final String JSON_KEY_DUN_ENABLE = "isEnableDun";
    private static final String JSON_KEY_URL = "szBoBullToon_URL";

    // ====================== 成员变量区 ======================
    // 云盾防御层数量
    private int dunTotalCount;
    // 当前云盾防御层
    private int dunCurrentCount;
    // 防御层恢复时间间隔(秒钟)
    private int dunResumeSecondCount;
    // 每次恢复防御层数
    private int dunResumeCount;
    // 是否启用云盾
    private boolean isEnableDun;
    // BoBullToon 应用模块数据请求地址
    private String szBoBullToon_URL;

    // ====================== 构造函数区 ======================
    /**
     * 默认构造，初始化默认配置
     */
    public SettingsBean() {
        this.dunTotalCount = 6;
        this.dunCurrentCount = 6;
        this.dunResumeSecondCount = 60;
        this.dunResumeCount = 1;
        this.isEnableDun = false;
        this.szBoBullToon_URL = "";
        LogUtils.d(TAG, "SettingsModel: 默认构造初始化完成 | 云盾默认配置加载完毕");
    }

    /**
     * 带参构造，初始化自定义配置并校验数值范围
     */
    public SettingsBean(int dunTotalCount, int dunCurrentCount, int dunResumeSecondCount,
                         int dunResumeCount, boolean isEnableDun, String szBoBullToon_URL) {
        this.dunTotalCount = getSettingsModelRangeInt(dunTotalCount);
        this.dunCurrentCount = getSettingsModelRangeInt(dunCurrentCount);
        this.dunResumeSecondCount = getSettingsModelRangeInt(dunResumeSecondCount);
        this.dunResumeCount = getSettingsModelRangeInt(dunResumeCount);
        this.isEnableDun = isEnableDun;
        this.szBoBullToon_URL = szBoBullToon_URL == null ? "" : szBoBullToon_URL;

        LogUtils.d(TAG, "SettingsModel: 带参构造初始化完成 | 总层数=" + this.dunTotalCount
				   + " | 当前层数=" + this.dunCurrentCount + " | 恢复间隔=" + this.dunResumeSecondCount
				   + " | 恢复层数=" + this.dunResumeCount + " | 云盾启用=" + this.isEnableDun);
    }

    // ====================== 私有工具方法区 ======================
    /**
     * 数值范围校验，确保参数在 MIN~MAX 区间内
     */
    private int getSettingsModelRangeInt(int origin) {
        int result = IntUtils.getIntInRange(origin, MIN_INTRANGE, MAX_INTRANGE);
        if (result != origin) {
            LogUtils.w(TAG, "getSettingsModelRangeInt: 数值校正 | 原始值=" + origin + " | 校正后=" + result);
        }
        return result;
    }

    // ====================== Getter & Setter 方法区 ======================
    public int getDunTotalCount() {
        return dunTotalCount;
    }

    public void setDunTotalCount(int dunTotalCount) {
        int oldValue = this.dunTotalCount;
        this.dunTotalCount = getSettingsModelRangeInt(dunTotalCount);
        LogUtils.d(TAG, "setDunTotalCount: 总防御层数更新 | 旧值=" + oldValue + " | 新值=" + this.dunTotalCount);
    }

    public int getDunCurrentCount() {
        return dunCurrentCount;
    }

    public void setDunCurrentCount(int dunCurrentCount) {
        int oldValue = this.dunCurrentCount;
        this.dunCurrentCount = getSettingsModelRangeInt(dunCurrentCount);
        LogUtils.d(TAG, "setDunCurrentCount: 当前防御层数更新 | 旧值=" + oldValue + " | 新值=" + this.dunCurrentCount);
    }

    public int getDunResumeSecondCount() {
        return dunResumeSecondCount;
    }

    public void setDunResumeSecondCount(int dunResumeSecondCount) {
        int oldValue = this.dunResumeSecondCount;
        this.dunResumeSecondCount = getSettingsModelRangeInt(dunResumeSecondCount);
        LogUtils.d(TAG, "setDunResumeSecondCount: 恢复间隔更新 | 旧值=" + oldValue + " | 新值=" + this.dunResumeSecondCount);
    }

    public int getDunResumeCount() {
        return dunResumeCount;
    }

    public void setDunResumeCount(int dunResumeCount) {
        int oldValue = this.dunResumeCount;
        this.dunResumeCount = getSettingsModelRangeInt(dunResumeCount);
        LogUtils.d(TAG, "setDunResumeCount: 恢复层数更新 | 旧值=" + oldValue + " | 新值=" + this.dunResumeCount);
    }

    public boolean isEnableDun() {
        return isEnableDun;
    }

    public void setIsEnableDun(boolean isEnableDun) {
        LogUtils.d(TAG, "setIsEnableDun: 云盾启用状态更新为" + isEnableDun);
        this.isEnableDun = isEnableDun;
    }

    public String getBoBullToon_URL() {
        return szBoBullToon_URL;
    }

    public void setBoBullToon_URL(String boBullToon_URL) {
        String oldValue = this.szBoBullToon_URL;
        this.szBoBullToon_URL = boBullToon_URL == null ? "" : boBullToon_URL;
        LogUtils.d(TAG, "setBoBullToon_URL: 请求地址更新 | 旧值=" + oldValue + " | 新值=" + this.szBoBullToon_URL);
    }

    // ====================== 重写 BaseBean 抽象方法区 ======================
    @Override
    public String getName() {
        String className = SettingsBean.class.getName();
        LogUtils.v(TAG, "getName: 获取当前类名=" + className);
        return className;
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        LogUtils.d(TAG, "writeThisToJsonWriter: 开始JSON序列化设置数据");
        super.writeThisToJsonWriter(jsonWriter);
        // 写入所有配置字段
        jsonWriter.name(JSON_KEY_DUN_TOTAL).value(getDunTotalCount());
        jsonWriter.name(JSON_KEY_DUN_CURRENT).value(getDunCurrentCount());
        jsonWriter.name(JSON_KEY_DUN_RESUME_SECOND).value(getDunResumeSecondCount());
        jsonWriter.name(JSON_KEY_DUN_RESUME_COUNT).value(getDunResumeCount());
        jsonWriter.name(JSON_KEY_DUN_ENABLE).value(isEnableDun());
        jsonWriter.name(JSON_KEY_URL).value(getBoBullToon_URL());
        LogUtils.d(TAG, "writeThisToJsonWriter: JSON序列化完成");
    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        // 优先调用父类处理通用字段
        if (super.initObjectsFromJsonReader(jsonReader, name)) {
            LogUtils.v(TAG, "initObjectsFromJsonReader: 父类已处理字段=" + name);
            return true;
        }

        // 处理当前类专属配置字段
        if (JSON_KEY_DUN_TOTAL.equals(name)) {
            setDunTotalCount(getSettingsModelRangeInt(jsonReader.nextInt()));
        } else if (JSON_KEY_DUN_CURRENT.equals(name)) {
            setDunCurrentCount(getSettingsModelRangeInt(jsonReader.nextInt()));
        } else if (JSON_KEY_DUN_RESUME_SECOND.equals(name)) {
            setDunResumeSecondCount(getSettingsModelRangeInt(jsonReader.nextInt()));
        } else if (JSON_KEY_DUN_RESUME_COUNT.equals(name)) {
            setDunResumeCount(getSettingsModelRangeInt(jsonReader.nextInt()));
        } else if (JSON_KEY_DUN_ENABLE.equals(name)) {
            setIsEnableDun(jsonReader.nextBoolean());
        } else if (JSON_KEY_URL.equals(name)) {
            setBoBullToon_URL(jsonReader.nextString());
        } else {
            LogUtils.w(TAG, "initObjectsFromJsonReader: 未识别的JSON字段=" + name);
            return false;
        }
        LogUtils.v(TAG, "initObjectsFromJsonReader: 成功解析字段=" + name);
        return true;
    }

    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        LogUtils.d(TAG, "readBeanFromJsonReader: 开始从JSON解析设置数据");
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String fieldName = jsonReader.nextName();
            if (!initObjectsFromJsonReader(jsonReader, fieldName)) {
                LogUtils.w(TAG, "readBeanFromJsonReader: 跳过未识别字段=" + fieldName);
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        LogUtils.d(TAG, "readBeanFromJsonReader: JSON解析完成 | 云盾配置加载完毕");
        return this;
    }
}

