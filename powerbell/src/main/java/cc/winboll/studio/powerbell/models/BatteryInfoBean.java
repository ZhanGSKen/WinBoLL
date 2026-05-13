package cc.winboll.studio.powerbell.models;

import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import cc.winboll.studio.libappbase.LogUtils;
import java.io.IOException;
import java.io.Serializable;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Describe 电池信息数据模型
 * 适配 API30，存储电量时间戳与电量值，支持 JSON 序列化/反序列化
 * 修复字段拼写错误，补充数据校验与调试日志
 */
public class BatteryInfoBean extends BaseBean implements Serializable {
    // ====================== 静态常量（首屏可见，统一管理） ======================
    public static final String TAG = "BatteryInfoBean";
    // 字段校验常量（避免硬编码，统一管理）
    private static final int BATTERY_MIN = 0;
    private static final int BATTERY_MAX = 100;
    private static final long DEFAULT_TIMESTAMP = 0L;
    private static final int DEFAULT_BATTERY_VALUE = 0;

    // ====================== 成员变量（修复拼写错误：battetyValue → batteryValue） ======================
    private long timeStamp;    // 记录电量的时间戳
    private int batteryValue;  // 电量值（0-100）

    // ====================== 构造方法（按参数重载排序，补充校验与日志） ======================
    /**
     * 无参构造器（JSON 反序列化、反射实例化必备）
     */
    public BatteryInfoBean() {
        this.timeStamp = DEFAULT_TIMESTAMP;
        this.batteryValue = DEFAULT_BATTERY_VALUE;
        LogUtils.d(TAG, "BatteryInfoBean: 无参构造初始化完成，默认时间戳：" + timeStamp + "，默认电量：" + batteryValue);
    }

    /**
     * 带参构造器（核心构造，初始化所有字段）
     * @param timeStamp 电量记录时间戳
     * @param batteryValue 电量值（0-100）
     */
    public BatteryInfoBean(long timeStamp, int batteryValue) {
        this.timeStamp = timeStamp;
        // 电量范围校验（0-100，异常值置为默认值）
        this.batteryValue = batteryValue >= BATTERY_MIN && batteryValue <= BATTERY_MAX
			? batteryValue : DEFAULT_BATTERY_VALUE;
        LogUtils.d(TAG, String.format("BatteryInfoBean: 带参构造初始化完成 | 时间戳：%d | 电量：%d（输入：%d）",
									  this.timeStamp, this.batteryValue, batteryValue));
    }

    // ====================== Setter/Getter 方法（按成员变量顺序排列，修复拼写错误，补充日志） ======================
    /**
     * 设置电量记录时间戳
     * @param timeStamp 时间戳
     */
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
        LogUtils.d(TAG, "setTimeStamp: 时间戳设置为 " + timeStamp);
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * 设置电量值（修复拼写错误：battetyValue → batteryValue）
     * @param batteryValue 电量值（0-100）
     */
    public void setBatteryValue(int batteryValue) {
        this.batteryValue = batteryValue >= BATTERY_MIN && batteryValue <= BATTERY_MAX
			? batteryValue : DEFAULT_BATTERY_VALUE;
        LogUtils.d(TAG, String.format("setBatteryValue: 电量设置为 %d（输入：%d）",
									  this.batteryValue, batteryValue));
    }

    public int getBatteryValue() {
        return batteryValue;
    }

    // ====================== JSON 序列化/反序列化方法（修复字段拼写错误，补充调试日志） ======================
    @Override
    public String getName() {
        String className = BatteryInfoBean.class.getName();
        LogUtils.d(TAG, "getName: 类名标识为 " + className);
        return className;
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        BatteryInfoBean bean = this;
        jsonWriter.name("timeStamp").value(bean.getTimeStamp());
        // 修复 JSON 字段名拼写错误：battetyValue → batteryValue
        jsonWriter.name("batteryValue").value(bean.getBatteryValue());
        LogUtils.d(TAG, "writeThisToJsonWriter: JSON 序列化完成 | 时间戳：" + bean.getTimeStamp() + "，电量：" + bean.getBatteryValue());
    }

    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        BatteryInfoBean bean = new BatteryInfoBean();
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case "timeStamp":
                    bean.setTimeStamp(jsonReader.nextLong());
                    break;
                case "batteryValue":
                    bean.setBatteryValue(jsonReader.nextInt());
                    break;
					// 兼容旧字段名（battetyValue），避免旧配置解析失败
                case "battetyValue":
                    int oldBatteryValue = jsonReader.nextInt();
                    bean.setBatteryValue(oldBatteryValue);
                    LogUtils.w(TAG, "readBeanFromJsonReader: 读取旧字段 battetyValue，已兼容为 batteryValue，值：" + oldBatteryValue);
                    break;
                default:
                    jsonReader.skipValue();
                    LogUtils.w(TAG, "readBeanFromJsonReader: 跳过未知字段 " + name);
                    break;
            }
        }
        jsonReader.endObject();
        LogUtils.d(TAG, "readBeanFromJsonReader: JSON 反序列化完成 | 时间戳：" + bean.getTimeStamp() + "，电量：" + bean.getBatteryValue());
        return bean;
    }
}

