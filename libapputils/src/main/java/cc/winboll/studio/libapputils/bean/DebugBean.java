package cc.winboll.studio.libapputils.bean;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/12/25 12:38:07
 * @Describe 应用调试配置类
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import java.io.IOException;

public class DebugBean extends BaseBean {

    public static final String TAG = "DebugBean";

    // 应用是否处于正在调试状态
    //
    boolean isDebuging = false;
    
    public DebugBean() {
        this.isDebuging = false;
    }

    public DebugBean(boolean isDebuging) {
        this.isDebuging = isDebuging;
    }

    public void setIsDebuging(boolean isDebuging) {
        this.isDebuging = isDebuging;
    }

    public boolean isDebuging() {
        return isDebuging;
    }

    @Override
    public String getName() {
        return DebugBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        DebugBean bean = this;
        jsonWriter.name("isDebuging").value(bean.isDebuging());

    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
            if (name.equals("isDebuging")) {
                setIsDebuging(jsonReader.nextBoolean());
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
