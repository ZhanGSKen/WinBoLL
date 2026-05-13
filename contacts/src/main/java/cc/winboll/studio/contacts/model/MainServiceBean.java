package cc.winboll.studio.contacts.model;

import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import cc.winboll.studio.libappbase.LogUtils;
import java.io.IOException;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/02/13 07:06:13
 * @Describe 主服务配置实体类，支持JSON序列化与反序列化
 */
public class MainServiceBean extends BaseBean {

    // ====================== 常量定义区 ======================
    public static final String TAG = "MainServiceBean";
    private static final String JSON_KEY_IS_ENABLE = "isEnable";

    // ====================== 成员变量区 ======================
    private boolean isEnable;

    // ====================== 构造函数区 ======================
    public MainServiceBean() {
        this.isEnable = false;
        LogUtils.d(TAG, "MainServiceBean: 初始化实体类，默认状态为禁用");
    }

    // ====================== Getter & Setter 方法区 ======================
    public void setIsEnable(boolean isEnable) {
        LogUtils.d(TAG, "setIsEnable: 服务状态设置为" + isEnable);
        this.isEnable = isEnable;
    }

    public boolean isEnable() {
        return isEnable;
    }

    // ====================== 重写 BaseBean 抽象方法区 ======================
    @Override
    public String getName() {
        String className = MainServiceBean.class.getName();
        LogUtils.v(TAG, "getName: 获取类名=" + className);
        return className;
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        LogUtils.d(TAG, "writeThisToJsonWriter: 开始将实体类写入JSON");
        super.writeThisToJsonWriter(jsonWriter);
        // 写入服务启用状态字段
        jsonWriter.name(JSON_KEY_IS_ENABLE).value(this.isEnable);
        LogUtils.d(TAG, "writeThisToJsonWriter: JSON写入完成，isEnable=" + this.isEnable);
    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        // 优先调用父类方法处理通用字段
        if (super.initObjectsFromJsonReader(jsonReader, name)) {
            LogUtils.v(TAG, "initObjectsFromJsonReader: 父类已处理字段=" + name);
            return true;
        }

        // 处理当前类专属字段
        if (JSON_KEY_IS_ENABLE.equals(name)) {
            this.isEnable = jsonReader.nextBoolean();
            LogUtils.d(TAG, "initObjectsFromJsonReader: 读取字段[" + name + "]值=" + this.isEnable);
            return true;
        }

        LogUtils.w(TAG, "initObjectsFromJsonReader: 未识别字段=" + name);
        return false;
    }

    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        LogUtils.d(TAG, "readBeanFromJsonReader: 开始从JSON读取实体类数据");
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (!initObjectsFromJsonReader(jsonReader, name)) {
                LogUtils.w(TAG, "readBeanFromJsonReader: 跳过未识别字段=" + name);
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        LogUtils.d(TAG, "readBeanFromJsonReader: JSON读取完成，当前实体状态=" + this.isEnable);
        return this;
    }
}

