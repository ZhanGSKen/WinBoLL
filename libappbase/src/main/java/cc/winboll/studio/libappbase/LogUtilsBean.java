package cc.winboll.studio.libappbase;

import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.models.libs1520000.BaseBean;
import java.io.IOException;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2024/08/23 15:39:07
 * @Describe LogUtils 配置数据模型（继承 BaseBean，实现 JSON 序列化/反序列化）
 * 封装 LogUtils 的核心配置参数（当前仅日志级别），用于配置的持久化存储与读取
 */
public class LogUtilsBean extends BaseBean {

    /** 当前类的日志 TAG（用于调试输出） */
    public static final String TAG = "LogUtilsBean";

    /**
     * 全局日志级别（默认值：Off，即不输出任何日志）
     * 关联 LogUtils.LOG_LEVEL 枚举，存储日志输出的级别阈值
     */
    private LogUtils.LOG_LEVEL logLevel;

    /**
     * 无参构造方法（默认初始化日志级别为 Off）
     * 用于 JSON 反序列化时的实例创建
     */
    public LogUtilsBean() {
        this.logLevel = LogUtils.LOG_LEVEL.Off;
    }

    /**
     * 有参构造方法（指定初始日志级别）
     * @param logLevel 初始日志级别（如 LogUtils.LOG_LEVEL.Debug）
     */
    public LogUtilsBean(LogUtils.LOG_LEVEL logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * 设置日志级别（更新配置时使用）
     * @param logLevel 目标日志级别
     */
    public void setLogLevel(LogUtils.LOG_LEVEL logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * 获取当前日志级别（读取配置时使用）
     * @return 当前配置的日志级别
     */
    public LogUtils.LOG_LEVEL getLogLevel() {
        return logLevel;
    }

    /**
     * 重写父类方法：获取当前类的全限定名（用于 BaseBean 反射识别）
     * @return 类全限定名（如 "cc.winboll.studio.libappbase.LogUtilsBean"）
     */
    @Override
    public String getName() {
        return LogUtilsBean.class.getName();
    }

    /**
     * 重写父类方法：将当前配置对象序列化为 JSON（持久化存储时调用）
     * 序列化字段：logLevel（存储枚举的 ordinal 值，确保反序列化一致性）
     * @param jsonWriter JSON 写入器（用于输出 JSON 数据）
     * @throws IOException JSON 写入异常（如流关闭、格式错误）
     */
    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        // 调用父类序列化逻辑（若 BaseBean 有公共字段，需优先处理）
        super.writeThisToJsonWriter(jsonWriter);
        // 序列化日志级别：存储枚举的索引值（如 Off=0、Error=1...），比存储名称更高效
        jsonWriter.name("logLevel").value(this.getLogLevel().ordinal());
    }

    /**
     * 重写父类方法：从 JSON 字段初始化当前对象（读取配置时调用）
     * 解析字段：logLevel（通过索引值恢复 LogUtils.LOG_LEVEL 枚举）
     * @param jsonReader JSON 读取器（用于读取 JSON 数据）
     * @param name JSON 字段名（当前解析的字段）
     * @return true：字段解析成功；false：字段不匹配（需父类处理或跳过）
     * @throws IOException JSON 读取异常（如字段类型不匹配、流中断）
     */
    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        // 先让父类处理公共字段，处理成功则直接返回
        if (super.initObjectsFromJsonReader(jsonReader, name)) {
            return true;
        }
        // 解析当前类专属字段
        if ("logLevel".equals(name)) {
            // 通过枚举索引值恢复枚举实例（确保与序列化时的 ordinal 对应）
            int levelOrdinal = jsonReader.nextInt();
            this.setLogLevel(LogUtils.LOG_LEVEL.values()[levelOrdinal]);
        } else {
            // 字段不匹配，返回 false 表示需要跳过该字段
            return false;
        }
        // 字段解析成功
        return true;
    }

    /**
     * 重写父类方法：从 JSON 读取器完整解析配置对象（入口方法）
     * 负责 JSON 对象的开始/结束解析，遍历所有字段并调用 initObjectsFromJsonReader 处理
     * @param jsonReader JSON 读取器（传入待解析的 JSON 流）
     * @return 解析后的当前 LogUtilsBean 实例（支持链式调用）
     * @throws IOException JSON 解析异常（如格式错误、字段缺失）
     */
    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        // 开始解析 JSON 对象（必须与 writeThisToJsonWriter 中的结构对应）
        jsonReader.beginObject();
        // 遍历 JSON 中的所有字段
        while (jsonReader.hasNext()) {
            String fieldName = jsonReader.nextName();
            // 解析字段，若字段不匹配则跳过该值（避免解析失败）
            if (!this.initObjectsFromJsonReader(jsonReader, fieldName)) {
                jsonReader.skipValue();
            }
        }
        // 结束 JSON 对象解析（必须调用，否则会导致流异常）
        jsonReader.endObject();
        // 返回当前实例，支持链式调用（如 new LogUtilsBean().readBeanFromJsonReader(reader)）
        return this;
    }
}

