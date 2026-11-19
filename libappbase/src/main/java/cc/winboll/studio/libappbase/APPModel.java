package cc.winboll.studio.libappbase;

import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.models.libs1520000.BaseBean;
import java.io.IOException;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/11 20:01
 * @Describe WinBoLL 应用全局数据模型类
 * 继承自 BaseBean，用于存储和管理应用的核心配置信息（如调试状态），
 * 支持 JSON 序列化/反序列化，便于数据持久化或跨组件传递
 */
public class APPModel extends BaseBean {

    /**
     * 日志打印标签，用于区分当前类的日志输出
     */
    public static final String TAG = "APPModel";

    /**
     * 应用调试状态标识
     * true：应用处于调试模式（可输出详细日志、启用调试功能等）
     * false：应用处于正式模式（关闭调试相关功能，优化性能）
     */
    private boolean isDebugging = false; // 修正拼写：原 isDebuging -> isDebugging（符合命名规范）

    /**
     * 无参构造方法
     * 初始化调试状态为默认值：false（正式模式）
     */
    public APPModel() {
        this.isDebugging = false;
    }

    /**
     * 带参构造方法
     * 可通过参数指定应用的初始调试状态
     * @param isDebugging 初始调试状态（true：调试模式；false：正式模式）
     */
    public APPModel(boolean isDebugging) {
        this.isDebugging = isDebugging;
    }

    /**
     * 设置应用调试状态
     * @param isDebugging 目标调试状态（true：开启调试；false：关闭调试）
     */
    public void setIsDebugging(boolean isDebugging) {
        this.isDebugging = isDebugging;
    }

    /**
     * 获取当前应用调试状态
     * @return 调试状态（true：调试中；false：非调试）
     */
    public boolean isDebugging() {
        return isDebugging;
    }

    /**
     * 重写父类方法，返回当前类的全限定名
     * 用于标识数据模型的类类型（可用于反射、序列化校验等场景）
     * @return 类的全限定名（如：cc.winboll.studio.libappbase.APPModel）
     */
    @Override
    public String getName() {
        return APPModel.class.getName();
    }

    /**
     * 重写父类方法，将当前模型的字段序列化到 JSON 中
     * 用于将调试状态等核心数据转换为 JSON 格式（如持久化到文件、网络传输）
     * @param jsonWriter JSON 写入器对象，用于输出 JSON 数据
     * @throws IOException 当 JSON 写入失败时抛出（如流关闭、格式错误）
     */
    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        // 先调用父类方法，序列化父类中的字段（若 BaseBean 有可序列化字段）
        super.writeThisToJsonWriter(jsonWriter);
        // 序列化当前类的调试状态字段：key 为 "isDebuging"（保持与原代码一致，避免兼容性问题），value 为当前状态
        jsonWriter.name("isDebuging").value(isDebugging());
    }

    /**
     * 重写父类方法，从 JSON 中解析字段并初始化当前对象
     * 用于将 JSON 格式的配置数据解析为 APPModel 实例（如从文件读取、网络接收后解析）
     * @param jsonReader JSON 读取器对象，用于读取 JSON 数据
     * @param name 当前解析的 JSON 字段名
     * @return true：字段解析成功；false：字段不属于当前类，需由调用者处理
     * @throws IOException 当 JSON 读取失败时抛出（如流关闭、数据格式错误）
     */
    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        // 先调用父类方法，解析父类中的字段（若 BaseBean 有可解析字段）
        if (super.initObjectsFromJsonReader(jsonReader, name)) {
            return true; // 父类已处理该字段，直接返回成功
        } else {
            // 解析当前类的字段
            if (name.equals("isDebuging")) {
                // 读取 JSON 中 "isDebuging" 字段的值，设置为当前对象的调试状态
                setIsDebugging(jsonReader.nextBoolean());
            } else {
                // 字段不属于当前类，返回 false 提示调用者跳过该字段
                return false;
            }
        }
        // 字段解析成功，返回 true
        return true;
    }

    /**
     * 重写父类方法，从 JSON 读取器中完整解析一个 APPModel 实例
     * 负责处理 JSON 对象的开始/结束标记，循环解析所有字段
     * @param jsonReader JSON 读取器对象，用于读取 JSON 数据
     * @return 解析完成的当前 APPModel 实例（支持链式调用）
     * @throws IOException 当 JSON 读取失败时抛出（如流关闭、数据格式错误）
     */
    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        // 开始解析 JSON 对象（对应 JSON 中的 '{'）
        jsonReader.beginObject();
        // 循环读取 JSON 中的所有字段（直到对象结束）
        while (jsonReader.hasNext()) {
            // 获取当前字段名
            String name = jsonReader.nextName();
            // 解析字段：若当前类无法处理该字段，则跳过（避免解析异常）
            if (!initObjectsFromJsonReader(jsonReader, name)) {
                jsonReader.skipValue();
            }
        }
        // 结束解析 JSON 对象（对应 JSON 中的 '}'）
        jsonReader.endObject();
        // 返回解析完成的实例（当前对象）
        return this;
    }
}

