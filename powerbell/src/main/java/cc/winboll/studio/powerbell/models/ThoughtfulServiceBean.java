package cc.winboll.studio.powerbell.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.io.Serializable;

import cc.winboll.studio.libappbase.BaseBean;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/12/29 20:59
 * @Describe 贴心服务配置实体类 (适配API30 / Java7)
 */
public class ThoughtfulServiceBean extends BaseBean implements Parcelable, Serializable {

    // ====================== 常量区 - 置顶统一管理 ======================
    public static final String TAG = ThoughtfulServiceBean.class.getSimpleName();
    private static final long serialVersionUID = 1L; // Serializable 序列化兼容必备
    // JSON序列化字段常量 杜绝硬编码
    public static final String JSON_FIELD_IS_ENABLE_CHARGE_TTS = "isEnableChargeTts";
    public static final String JSON_FIELD_IS_ENABLE_USE_POWER_TTS = "isEnableUsePowerTts";
    public static final String JSON_FIELD_IS_ENABLE_USAGE_TTS_WITH_BATTERY = "isEnableUseageTtsWithBattary";
    public static final String JSON_FIELD_IS_ENABLE_CHARGE_TTS_WITH_BATTERY = "isEnableChargeTtsWithBattary";
    // 👉 新增：通知电量消息时同时播放TTS
    public static final String JSON_FIELD_IS_ENABLE_TTS_WHEN_NOTIFY_BATTERY = "isEnableTtsWhenNotifyBattery";

    // ====================== 核心成员变量 - 私有封装 ======================
    private boolean isEnableChargeTts = false;                  // 是否启用 充电TTS贴心语音服务
    private boolean isEnableUsePowerTts = false;                // 是否启用 用电TTS贴心语音服务
    private boolean isEnableUseageTtsWithBattary = false;       // 用电TTS加入电量提醒
    private boolean isEnableChargeTtsWithBattary = false;        // 充电TTS加入电量提醒
    private boolean isEnableTtsWhenNotifyBattery = false;        // 👉 允许通知电量消息时同时播放TTS语音

    // ====================== Parcelable 静态创建器 ======================
    public static final Creator<ThoughtfulServiceBean> CREATOR = new Creator<ThoughtfulServiceBean>() {
        @Override
        public ThoughtfulServiceBean createFromParcel(Parcel source) {
            return new ThoughtfulServiceBean(source);
        }

        @Override
        public ThoughtfulServiceBean[] newArray(int size) {
            LogUtils.d(TAG, "newArray: 初始化数组，size = " + size);
            return new ThoughtfulServiceBean[size];
        }
    };

    // ====================== 构造方法区 ======================
    public ThoughtfulServiceBean() {
        LogUtils.d(TAG, "ThoughtfulServiceBean: 无参构造，初始化默认禁用所有TTS服务");
    }

    public ThoughtfulServiceBean(boolean isEnableChargeTts, boolean isEnableUsePowerTts,
                                 boolean isEnableUseageTtsWithBattary, boolean isEnableChargeTtsWithBattary,
                                 boolean isEnableTtsWhenNotifyBattery) {
        this.isEnableChargeTts = isEnableChargeTts;
        this.isEnableUsePowerTts = isEnableUsePowerTts;
        this.isEnableUseageTtsWithBattary = isEnableUseageTtsWithBattary;
        this.isEnableChargeTtsWithBattary = isEnableChargeTtsWithBattary;
        this.isEnableTtsWhenNotifyBattery = isEnableTtsWhenNotifyBattery;
        LogUtils.d(TAG, "ThoughtfulServiceBean: 全参构造 | 充电TTS=" + isEnableChargeTts + " | 用电TTS=" + isEnableUsePowerTts
				   + " | 用电TTS加电量=" + isEnableUseageTtsWithBattary + " | 充电TTS加电量=" + isEnableChargeTtsWithBattary
				   + " | 通知电量时播TTS=" + isEnableTtsWhenNotifyBattery);
    }

    private ThoughtfulServiceBean(Parcel in) {
        this.isEnableChargeTts = in.readByte() != 0;
        this.isEnableUsePowerTts = in.readByte() != 0;
        this.isEnableUseageTtsWithBattary = in.readByte() != 0;
        this.isEnableChargeTtsWithBattary = in.readByte() != 0;
        this.isEnableTtsWhenNotifyBattery = in.readByte() != 0; // 👉 新增反序列化
        LogUtils.d(TAG, "ThoughtfulServiceBean: Parcel构造解析完成");
    }

    // ====================== Getter/Setter 方法区 ======================
    public boolean isEnableChargeTts() {
        return isEnableChargeTts;
    }

    public void setIsEnableChargeTts(boolean isEnableChargeTts) {
        LogUtils.d(TAG, "setIsEnableChargeTts: 旧值=" + this.isEnableChargeTts + " ｜ 新值=" + isEnableChargeTts);
        this.isEnableChargeTts = isEnableChargeTts;
    }

    public boolean isEnableUsePowerTts() {
        return isEnableUsePowerTts;
    }

    public void setIsEnableUsePowerTts(boolean isEnableUsePowerTts) {
        LogUtils.d(TAG, "setIsEnableUsePowerTts: 旧值=" + this.isEnableUsePowerTts + " ｜ 新值=" + isEnableUsePowerTts);
        this.isEnableUsePowerTts = isEnableUsePowerTts;
    }

    public boolean isEnableUseageTtsWithBattary() {
        return isEnableUseageTtsWithBattary;
    }

    public void setIsEnableUseageTtsWithBattary(boolean isEnableUseageTtsWithBattary) {
        LogUtils.d(TAG, "setIsEnableUseageTtsWithBattary: 旧值=" + this.isEnableUseageTtsWithBattary + " ｜ 新值=" + isEnableUseageTtsWithBattary);
        this.isEnableUseageTtsWithBattary = isEnableUseageTtsWithBattary;
    }

    public boolean isEnableChargeTtsWithBattary() {
        return isEnableChargeTtsWithBattary;
    }

    public void setIsEnableChargeTtsWithBattary(boolean isEnableChargeTtsWithBattary) {
        LogUtils.d(TAG, "setIsEnableChargeTtsWithBattary: 旧值=" + this.isEnableChargeTtsWithBattary + " ｜ 新值=" + isEnableChargeTtsWithBattary);
        this.isEnableChargeTtsWithBattary = isEnableChargeTtsWithBattary;
    }

    // 👉 新增：通知电量时播放TTS
    public boolean isEnableTtsWhenNotifyBattery() {
        return isEnableTtsWhenNotifyBattery;
    }

    public void setIsEnableTtsWhenNotifyBattery(boolean isEnableTtsWhenNotifyBattery) {
        LogUtils.d(TAG, "setIsEnableTtsWhenNotifyBattery: 旧值=" + this.isEnableTtsWhenNotifyBattery + " ｜ 新值=" + isEnableTtsWhenNotifyBattery);
        this.isEnableTtsWhenNotifyBattery = isEnableTtsWhenNotifyBattery;
    }

    // ====================== 重写父类 BaseBean 核心方法 ======================
    @Override
    public String getName() {
        String className = ThoughtfulServiceBean.class.getName();
        LogUtils.d(TAG, "getName: 返回当前实体类名 = " + className);
        return className;
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        jsonWriter.name(JSON_FIELD_IS_ENABLE_CHARGE_TTS).value(this.isEnableChargeTts);
        jsonWriter.name(JSON_FIELD_IS_ENABLE_USE_POWER_TTS).value(this.isEnableUsePowerTts);
        jsonWriter.name(JSON_FIELD_IS_ENABLE_USAGE_TTS_WITH_BATTERY).value(this.isEnableUseageTtsWithBattary);
        jsonWriter.name(JSON_FIELD_IS_ENABLE_CHARGE_TTS_WITH_BATTERY).value(this.isEnableChargeTtsWithBattary);
        jsonWriter.name(JSON_FIELD_IS_ENABLE_TTS_WHEN_NOTIFY_BATTERY).value(this.isEnableTtsWhenNotifyBattery); // 👉 新增JSON写入
        LogUtils.d(TAG, "writeThisToJsonWriter: JSON序列化完成，所有TTS服务状态已写入");
    }

    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        ThoughtfulServiceBean bean = new ThoughtfulServiceBean();
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String fieldName = jsonReader.nextName();
            switch (fieldName) {
                case JSON_FIELD_IS_ENABLE_CHARGE_TTS:
                    bean.setIsEnableChargeTts(jsonReader.nextBoolean());
                    break;
                case JSON_FIELD_IS_ENABLE_USE_POWER_TTS:
                    bean.setIsEnableUsePowerTts(jsonReader.nextBoolean());
                    break;
                case JSON_FIELD_IS_ENABLE_USAGE_TTS_WITH_BATTERY:
                    bean.setIsEnableUseageTtsWithBattary(jsonReader.nextBoolean());
                    break;
                case JSON_FIELD_IS_ENABLE_CHARGE_TTS_WITH_BATTERY:
                    bean.setIsEnableChargeTtsWithBattary(jsonReader.nextBoolean());
                    break;
                case JSON_FIELD_IS_ENABLE_TTS_WHEN_NOTIFY_BATTERY:
                    bean.setIsEnableTtsWhenNotifyBattery(jsonReader.nextBoolean()); // 👉 新增JSON读取
                    break;
                default:
                    jsonReader.skipValue();
                    LogUtils.w(TAG, "readBeanFromJsonReader: 跳过未知JSON字段 = " + fieldName);
                    break;
            }
        }
        jsonReader.endObject();
        LogUtils.d(TAG, "readBeanFromJsonReader: JSON反序列化完成，生成实体对象");
        return bean;
    }

    // ====================== Parcelable 接口方法 ======================
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isEnableChargeTts ? 1 : 0));
        dest.writeByte((byte) (isEnableUsePowerTts ? 1 : 0));
        dest.writeByte((byte) (isEnableUseageTtsWithBattary ? 1 : 0));
        dest.writeByte((byte) (isEnableChargeTtsWithBattary ? 1 : 0));
        dest.writeByte((byte) (isEnableTtsWhenNotifyBattery ? 1 : 0)); // 👉 新增Parcel写入
        LogUtils.d(TAG, "writeToParcel: Parcel序列化完成，所有TTS服务状态已写入");
    }

}

