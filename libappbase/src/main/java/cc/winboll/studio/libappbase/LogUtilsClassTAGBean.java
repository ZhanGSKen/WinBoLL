package cc.winboll.studio.libappbase;

import android.util.JsonReader;
import android.util.JsonWriter;
import java.io.IOException;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/01/04 14:17:02
 * @Describe 日志 TAG 过滤配置模型（继承 BaseBean，实现 JSON 序列化/反序列化）
 * 封装单个日志 TAG 的名称及其启用状态，用于 LogUtils 的 TAG 过滤规则持久化存储与读取
 */
public class LogUtilsClassTAGBean extends BaseBean {

    /** 当前类的日志 TAG（用于调试输出） */
    public static final String TAG = "LogUtilsClassTAGBean";

    /**
     * 日志 TAG 名称（如 "LogViewThread"、"ToastUtils"）
     * 与 LogUtils 中扫描的应用内 TAG 一一对应
     */
    private String tag;

    /**
     * TAG 启用状态（控制该 TAG 的日志是否输出）
     * true：启用（输出该 TAG 的日志）；false：禁用（不输出该 TAG 的日志）
     */
    private Boolean enable;

    /**
     * 无参构造方法（默认初始化：TAG 为当前类 TAG，启用状态为 true）
     * 用于 JSON 反序列化时的实例创建，或默认配置生成
     */
    public LogUtilsClassTAGBean() {
        this.tag = TAG;       // 默认 TAG 为当前类的 TAG
        this.enable = true;   // 默认启用该 TAG 的日志输出
    }

    /**
     * 有参构造方法（指定 TAG 名称和启用状态）
     * 用于主动创建 TAG 过滤配置实例
     * @param tag     日志 TAG 名称
     * @param enable  TAG 启用状态（true/false）
     */
    public LogUtilsClassTAGBean(String tag, Boolean enable) {
        this.tag = tag;
        this.enable = enable;
    }

    /**
     * 设置日志 TAG 名称
     * @param tag 目标 TAG 名称
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * 获取日志 TAG 名称
     * @return 当前配置的 TAG 名称
     */
    public String getTag() {
        return tag;
    }

    /**
     * 设置 TAG 启用状态
     * @param enable 目标启用状态（true：启用；false：禁用）
     */
    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    /**
     * 获取 TAG 启用状态
     * @return 当前 TAG 的启用状态
     */
    public Boolean getEnable() {
        return enable;
    }

    /**
     * 重写父类方法：获取当前类的全限定名（用于 BaseBean 反射识别）
     * @return 类全限定名（如 "cc.winboll.studio.libappbase.LogUtilsClassTAGBean"）
     */
    @Override
    public String getName() {
        return LogUtilsClassTAGBean.class.getName();
    }

    /**
     * 重写父类方法：将当前 TAG 配置对象序列化为 JSON（持久化存储时调用）
     * 序列化字段：tag（TAG 名称）、enable（启用状态）
     * @param jsonWriter JSON 写入器（用于输出 JSON 数据）
     * @throws IOException JSON 写入异常（如流关闭、格式错误）
     */
    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        // 调用父类序列化逻辑（若 BaseBean 有公共字段，需优先处理）
        super.writeThisToJsonWriter(jsonWriter);
        // 序列化 TAG 名称
        jsonWriter.name("tag").value(this.getTag());
        // 序列化启用状态
        jsonWriter.name("enable").value(this.getEnable());
    }

    /**
     * 重写父类方法：从 JSON 字段初始化当前对象（读取配置时调用）
     * 解析字段：tag（TAG 名称）、enable（启用状态）
     * @param jsonReader JSON 读取器（用于读取 JSON 数据）
     * @param name       JSON 字段名（当前解析的字段）
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
        if ("tag".equals(name)) {
            // 读取 TAG 名称并设置
            this.setTag(jsonReader.nextString());
        } else if ("enable".equals(name)) {
            // 读取启用状态并设置
            this.setEnable(jsonReader.nextBoolean());
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
     * @return 解析后的当前 LogUtilsClassTAGBean 实例（支持链式调用）
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
        // 返回当前实例，支持链式调用（如 new LogUtilsClassTAGBean().readBeanFromJsonReader(reader)）
        return this;
    }
}

