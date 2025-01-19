package cc.winboll.studio.shared.log;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/04 14:17:02
 * @Describe 日志类class TAG 标签数据类
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.shared.app.BaseBean;
import java.io.IOException;

public class LogUtilsClassTAGBean extends BaseBean {

    public static final String TAG = "LogUtilsClassTAGBean";

    // 标签名
    String tag;
    // 是否启用
    Boolean enable;

    public LogUtilsClassTAGBean() {
        this.tag = TAG;
        this.enable = true;
    }

    public LogUtilsClassTAGBean(String tag, Boolean enable) {
        this.tag = tag;
        this.enable = enable;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Boolean getEnable() {
        return enable;
    }

    @Override
    public String getName() {
        return LogUtilsClassTAGBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        LogUtilsClassTAGBean bean = this;
        jsonWriter.name("tag").value(bean.getTag());
        jsonWriter.name("enable").value(bean.getEnable());
    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
            if (name.equals("tag")) {
                setTag(jsonReader.nextString());
            } else if (name.equals("enable")) {
                setEnable(jsonReader.nextBoolean());
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (!initObjectsFromJsonReader(jsonReader, name)) {
                jsonReader.skipValue();
            }
        }
        // 结束 JSON 对象
        jsonReader.endObject();
        return this;
    }
}
