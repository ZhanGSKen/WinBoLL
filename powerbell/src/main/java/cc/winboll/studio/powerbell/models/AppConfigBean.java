package cc.winboll.studio.powerbell.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import cc.winboll.studio.libappbase.LogUtils;
import java.io.IOException;
import java.io.Serializable;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2024/04/29 17:24:53
 * @Describe 应用运行参数类
 * 适配 API30，支持 Serializable 持久化、Parcelable Intent 传递、JSON 序列化/反序列化
 * 包含耗电提醒、充电提醒、电量检测、铃声提醒、相框尺寸等核心配置
 */
public class AppConfigBean extends BaseBean implements Serializable, Parcelable {
    // ====================== 静态常量区（首屏可见，统一管理） ======================
    // 序列化版本号（Serializable 必备，避免反序列化失败）
    private static final long serialVersionUID = 1L;
    // 日志标签（全局统一）
    transient public static final String TAG = "AppConfigBean";
    // 字段校验常量（统一阈值，避免硬编码）
    private static final int MIN_INTERVAL = 500;        // 最小检测间隔（ms）
    private static final int MIN_REMIND_INTERVAL = 1000;// 最小提醒间隔（ms）
    private static final int BATTERY_MIN = 0;           // 电量最小值
    private static final int BATTERY_MAX = 100;         // 电量最大值
    private static final int INVALID_BATTERY = -1;      // 无效电量标识
    private static final int DEFAULT_FRAME_WIDTH = 500; // 默认相框宽度（px）
    private static final int DEFAULT_FRAME_HEIGHT = 500;// 默认相框高度（px）

    // ====================== 成员变量区（按功能分类：提醒配置→电量状态→检测配置→相框配置） ======================
    // 耗电提醒配置
    boolean isEnableUsageReminder = false; // 耗电提醒开关
    int usageReminderValue = 45;           // 耗电提醒阈值（0-100）
    // 充电提醒配置
    boolean isEnableChargeReminder = false;// 充电提醒开关
    int chargeReminderValue = 100;         // 充电提醒阈值（0-100）
    // 铃声提醒配置
    int reminderIntervalTime = 5000;       // 铃声提醒间隔（ms）
    // 电量状态
    boolean isCharging = false;            // 是否充电
    // 电量检测配置
    int batteryDetectInterval = 2000;      // 电量检测间隔（ms，适配 RemindThread）
    // 相框配置
    int defaultFrameWidth = DEFAULT_FRAME_WIDTH;  // 默认相框宽度（px）
    int defaultFrameHeight = DEFAULT_FRAME_HEIGHT;// 默认相框高度（px）

    // ====================== 构造方法（初始化默认配置，强化默认值校验） ======================
    public AppConfigBean() {
        setChargeReminderValue(100);
        setEnableChargeReminder(false);
        setUsageReminderValue(10);
        setEnableUsageReminder(false);
        setReminderIntervalTime(5000);
        setBatteryDetectInterval(1000);
        setDefaultFrameWidth(DEFAULT_FRAME_WIDTH);
        setDefaultFrameHeight(DEFAULT_FRAME_HEIGHT);
        LogUtils.d(TAG, "AppConfigBean() 构造器执行 | 默认配置初始化完成");
    }

    // ====================== 核心业务方法（Setter/Getter，按字段功能分类，补充调试日志） ======================
    // --------------- 充电状态相关 ---------------
    public void setIsCharging(boolean isCharging) {
        this.isCharging = isCharging;
        LogUtils.d(TAG, String.format("setIsCharging() 执行 | 充电状态=%b", isCharging));
    }

    public boolean isCharging() {
        return isCharging;
    }

    // --------------- 耗电提醒配置相关 ---------------
    public void setEnableUsageReminder(boolean isEnableUsageReminder) {
        this.isEnableUsageReminder = isEnableUsageReminder;
        LogUtils.d(TAG, String.format("setEnableUsageReminder() 执行 | 耗电提醒开关=%b", isEnableUsageReminder));
    }

    public boolean isEnableUsageReminder() {
        return isEnableUsageReminder;
    }

    public void setUsageReminderValue(int usageReminderValue) {
        this.usageReminderValue = Math.min(Math.max(usageReminderValue, BATTERY_MIN), BATTERY_MAX);
        LogUtils.d(TAG, String.format("setUsageReminderValue() 执行 | 最终阈值=%d | 输入值=%d", this.usageReminderValue, usageReminderValue));
    }

    public int getUsageReminderValue() {
        return usageReminderValue;
    }

    // --------------- 充电提醒配置相关 ---------------
    public void setEnableChargeReminder(boolean isEnableChargeReminder) {
        this.isEnableChargeReminder = isEnableChargeReminder;
        LogUtils.d(TAG, String.format("setEnableChargeReminder() 执行 | 充电提醒开关=%b", isEnableChargeReminder));
    }

    public boolean isEnableChargeReminder() {
        return isEnableChargeReminder;
    }

    public void setChargeReminderValue(int chargeReminderValue) {
        this.chargeReminderValue = Math.min(Math.max(chargeReminderValue, BATTERY_MIN), BATTERY_MAX);
        LogUtils.d(TAG, String.format("setChargeReminderValue() 执行 | 最终阈值=%d | 输入值=%d", this.chargeReminderValue, chargeReminderValue));
    }

    public int getChargeReminderValue() {
        return chargeReminderValue;
    }

    // --------------- 铃声提醒配置相关 ---------------
    public void setReminderIntervalTime(int reminderIntervalTime) {
        this.reminderIntervalTime = Math.max(reminderIntervalTime, MIN_REMIND_INTERVAL);
        LogUtils.d(TAG, String.format("setReminderIntervalTime() 执行 | 最终间隔=%dms | 输入值=%dms", this.reminderIntervalTime, reminderIntervalTime));
    }

    public int getReminderIntervalTime() {
        return reminderIntervalTime;
    }

    // --------------- 电量检测配置相关 ---------------
    public void setBatteryDetectInterval(int batteryDetectInterval) {
        this.batteryDetectInterval = Math.max(batteryDetectInterval, MIN_INTERVAL);
        LogUtils.d(TAG, String.format("setBatteryDetectInterval() 执行 | 最终间隔=%dms | 输入值=%dms", this.batteryDetectInterval, batteryDetectInterval));
    }

    public int getBatteryDetectInterval() {
        return batteryDetectInterval;
    }

    // --------------- 相框配置相关 ---------------
    public void setDefaultFrameWidth(int defaultFrameWidth) {
        this.defaultFrameWidth = defaultFrameWidth;
        LogUtils.d(TAG, String.format("setDefaultFrameWidth() 执行 | 最终宽度=%dpx | 输入值=%dpx", this.defaultFrameWidth, defaultFrameWidth));
    }

    public int getDefaultFrameWidth() {
        return defaultFrameWidth;
    }

    public void setDefaultFrameHeight(int defaultFrameHeight) {
        this.defaultFrameHeight = defaultFrameHeight;
        LogUtils.d(TAG, String.format("setDefaultFrameHeight() 执行 | 最终高度=%dpx | 输入值=%dpx", this.defaultFrameHeight, defaultFrameHeight));
    }

    public int getDefaultFrameHeight() {
        return defaultFrameHeight;
    }

    // ====================== 父类重写方法（JSON 序列化/反序列化，兼容旧配置） ======================
    @Override
    public String getName() {
        return AppConfigBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        LogUtils.d(TAG, "writeThisToJsonWriter() 执行 | 开始JSON序列化");

        // 原有字段序列化
        jsonWriter.name("isEnableUsageReminder").value(isEnableUsageReminder());
        jsonWriter.name("usageReminderValue").value(getUsageReminderValue());
        jsonWriter.name("isEnableChargeReminder").value(isEnableChargeReminder());
        jsonWriter.name("chargeReminderValue").value(getChargeReminderValue());
        jsonWriter.name("reminderIntervalTime").value(getReminderIntervalTime());
        jsonWriter.name("isCharging").value(isCharging());
        // 新增字段序列化（检测配置）
        jsonWriter.name("batteryDetectInterval").value(getBatteryDetectInterval());
        // 新增字段序列化（相框配置）
        jsonWriter.name("defaultFrameWidth").value(getDefaultFrameWidth());
        jsonWriter.name("defaultFrameHeight").value(getDefaultFrameHeight());

        LogUtils.d(TAG, "writeThisToJsonWriter() 完成 | JSON序列化成功");
    }

    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        LogUtils.d(TAG, "readBeanFromJsonReader() 执行 | 开始JSON反序列化");
        AppConfigBean bean = new AppConfigBean();
        jsonReader.beginObject();

        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            // 兼容拼写错误字段（isEnableUsegeReminder → isEnableUsageReminder）
            if (name.equals("isEnableUsageReminder") || name.equals("isEnableUsegeReminder")) {
                bean.setEnableUsageReminder(jsonReader.nextBoolean());
                LogUtils.d(TAG, String.format("readBeanFromJsonReader() 读取字段 | %s=%b", name, bean.isEnableUsageReminder()));
            } else if (name.equals("usageReminderValue") || name.equals("usegeReminderValue")) {
                bean.setUsageReminderValue(jsonReader.nextInt());
                LogUtils.d(TAG, String.format("readBeanFromJsonReader() 读取字段 | %s=%d", name, bean.getUsageReminderValue()));
            } else if (name.equals("isEnableChargeReminder")) {
                bean.setEnableChargeReminder(jsonReader.nextBoolean());
                LogUtils.d(TAG, String.format("readBeanFromJsonReader() 读取字段 | %s=%b", name, bean.isEnableChargeReminder()));
            } else if (name.equals("chargeReminderValue")) {
                bean.setChargeReminderValue(jsonReader.nextInt());
                LogUtils.d(TAG, String.format("readBeanFromJsonReader() 读取字段 | %s=%d", name, bean.getChargeReminderValue()));
            } else if (name.equals("reminderIntervalTime")) {
                bean.setReminderIntervalTime(jsonReader.nextInt());
                LogUtils.d(TAG, String.format("readBeanFromJsonReader() 读取字段 | %s=%d", name, bean.getReminderIntervalTime()));
            } else if (name.equals("isCharging")) {
                bean.setIsCharging(jsonReader.nextBoolean());
                LogUtils.d(TAG, String.format("readBeanFromJsonReader() 读取字段 | %s=%b", name, bean.isCharging()));
            } else if (name.equals("batteryDetectInterval")) {
                bean.setBatteryDetectInterval(jsonReader.nextInt());
                LogUtils.d(TAG, String.format("readBeanFromJsonReader() 读取字段 | %s=%d", name, bean.getBatteryDetectInterval()));
            } else if (name.equals("defaultFrameWidth")) {
                bean.setDefaultFrameWidth(jsonReader.nextInt());
                LogUtils.d(TAG, String.format("readBeanFromJsonReader() 读取字段 | %s=%d", name, bean.getDefaultFrameWidth()));
            } else if (name.equals("defaultFrameHeight")) {
                bean.setDefaultFrameHeight(jsonReader.nextInt());
                LogUtils.d(TAG, String.format("readBeanFromJsonReader() 读取字段 | %s=%d", name, bean.getDefaultFrameHeight()));
            } else {
                jsonReader.skipValue();
                LogUtils.w(TAG, String.format("readBeanFromJsonReader() 跳过未知字段 | %s", name));
            }
        }

        jsonReader.endObject();
        LogUtils.d(TAG, "readBeanFromJsonReader() 完成 | JSON反序列化成功");
        return bean;
    }

    // ====================== Parcelable 接口实现（API30 Intent 传递必备） ======================
    @Override
    public int describeContents() {
        return 0; // 无特殊内容描述，固定返回0
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        LogUtils.d(TAG, "writeToParcel() 执行 | 开始Parcel序列化");
        // 按成员变量顺序写入，boolean 转 byte 存储
        dest.writeByte((byte) (isEnableUsageReminder ? 1 : 0));
        dest.writeInt(usageReminderValue);
        dest.writeByte((byte) (isEnableChargeReminder ? 1 : 0));
        dest.writeInt(chargeReminderValue);
        dest.writeInt(reminderIntervalTime);
        dest.writeByte((byte) (isCharging ? 1 : 0));
        dest.writeInt(batteryDetectInterval);
        dest.writeInt(defaultFrameWidth);
        dest.writeInt(defaultFrameHeight);
        LogUtils.d(TAG, "writeToParcel() 完成 | Parcel序列化成功");
    }

    // 反序列化 Creator（必须 public static final 修饰，Java7 适配）
    public static final Parcelable.Creator<AppConfigBean> CREATOR = new Parcelable.Creator<AppConfigBean>() {
        @Override
        public AppConfigBean createFromParcel(Parcel source) {
            LogUtils.d(TAG, "createFromParcel() 执行 | 开始Parcel反序列化");
            AppConfigBean bean = new AppConfigBean();
            // 按 writeToParcel 顺序读取
            bean.isEnableUsageReminder = source.readByte() != 0;
            bean.usageReminderValue = source.readInt();
            bean.isEnableChargeReminder = source.readByte() != 0;
            bean.chargeReminderValue = source.readInt();
            bean.reminderIntervalTime = source.readInt();
            bean.isCharging = source.readByte() != 0;
            bean.batteryDetectInterval = source.readInt();
            bean.defaultFrameWidth = source.readInt();
            bean.defaultFrameHeight = source.readInt();
            LogUtils.d(TAG, "createFromParcel() 完成 | Parcel反序列化成功");
            return bean;
        }

        @Override
        public AppConfigBean[] newArray(int size) {
            return new AppConfigBean[size];
        }
    };
}

