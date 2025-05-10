package cc.winboll.studio.libappbase.models;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/03/02 10:28:08
 * @Describe 应用调试模型
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;

public class APPModel extends BaseBean {

    public static final String TAG = "APPModel";

    // 应用是否处于正在调试状态
    //
    boolean isDebuging = false;

    public APPModel() {
        this.isDebuging = false;
    }

    public APPModel(boolean isDebuging) {
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
        return APPModel.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        jsonWriter.name("isDebuging").value(isDebuging());
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
