package cc.winboll.studio.contacts.model;

import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import cc.winboll.studio.libappbase.LogUtils;
import java.io.IOException;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/02/24 18:47:11
 * @Describe 手机铃声设置参数类，支持JSON序列化与反序列化
 */
public class RingTongBean extends BaseBean {

    // ====================== 常量定义区 ======================
    public static final String TAG = "AudioRingTongBean";
    private static final String JSON_KEY_STREAM_VOLUME = "streamVolume";
    // 铃声音量范围常量（参考AudioManager标准）
    private static final int VOLUME_MIN = 0;
    private static final int VOLUME_MAX = 100;

    // ====================== 成员变量区 ======================
    private int streamVolume;

    // ====================== 构造函数区 ======================
    /**
     * 默认构造，铃声音量初始化为最大值
     */
    public RingTongBean() {
        this.streamVolume = VOLUME_MAX;
        LogUtils.d(TAG, "RingTongBean: 默认构造初始化 | 铃声音量=" + this.streamVolume);
    }

    /**
     * 带参构造，初始化指定铃声音量
     */
    public RingTongBean(int streamVolume) {
        // 音量值范围校验，避免非法值
        this.streamVolume = Math.max(VOLUME_MIN, Math.min(VOLUME_MAX, streamVolume));
        LogUtils.d(TAG, "RingTongBean: 带参构造初始化 | 原始音量=" + streamVolume + " | 校正后=" + this.streamVolume);
    }

    // ====================== Getter & Setter 方法区 ======================
    public int getStreamVolume() {
        return streamVolume;
    }

    public void setStreamVolume(int streamVolume) {
        int oldVolume = this.streamVolume;
        // 音量值范围校验
        this.streamVolume = Math.max(VOLUME_MIN, Math.min(VOLUME_MAX, streamVolume));
        LogUtils.d(TAG, "setStreamVolume: 铃声音量更新 | 旧值=" + oldVolume + " | 新值=" + this.streamVolume);
    }

    // ====================== 重写 BaseBean 抽象方法区 ======================
    @Override
    public String getName() {
        String className = RingTongBean.class.getName();
        LogUtils.v(TAG, "getName: 获取当前类名=" + className);
        return className;
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        LogUtils.d(TAG, "writeThisToJsonWriter: 开始JSON序列化铃声音量参数");
        super.writeThisToJsonWriter(jsonWriter);
        jsonWriter.name(JSON_KEY_STREAM_VOLUME).value(getStreamVolume());
        LogUtils.d(TAG, "writeThisToJsonWriter: JSON序列化完成 | 音量值=" + getStreamVolume());
    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        // 优先调用父类处理通用字段
        if (super.initObjectsFromJsonReader(jsonReader, name)) {
            LogUtils.v(TAG, "initObjectsFromJsonReader: 父类已处理字段=" + name);
            return true;
        }

        // 处理当前类专属字段
        if (JSON_KEY_STREAM_VOLUME.equals(name)) {
            setStreamVolume(jsonReader.nextInt());
            LogUtils.v(TAG, "initObjectsFromJsonReader: 解析字段[" + name + "]值=" + this.streamVolume);
            return true;
        }

        LogUtils.w(TAG, "initObjectsFromJsonReader: 未识别的JSON字段=" + name);
        return false;
    }

    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        LogUtils.d(TAG, "readBeanFromJsonReader: 开始从JSON解析铃声音量参数");
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String fieldName = jsonReader.nextName();
            if (!initObjectsFromJsonReader(jsonReader, fieldName)) {
                LogUtils.w(TAG, "readBeanFromJsonReader: 跳过未识别字段=" + fieldName);
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        LogUtils.d(TAG, "readBeanFromJsonReader: JSON解析完成 | 最终音量值=" + this.streamVolume);
        return this;
    }
}

