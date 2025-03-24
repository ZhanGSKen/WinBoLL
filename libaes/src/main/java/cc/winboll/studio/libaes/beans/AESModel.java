package cc.winboll.studio.libaes.beans;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/24 15:27:16
 * @Describe AES数据实例化模型
 */
import cc.winboll.studio.libappbase.BaseBean;
import android.util.JsonWriter;
import java.io.IOException;
import android.util.JsonReader;

public class AESModel extends BaseBean {

    public static final String TAG = "AESModel";

    boolean isInDebugMode;

    public AESModel() {
        this.isInDebugMode = false;
    }

    public AESModel(boolean isInDebugMode) {
        this.isInDebugMode = isInDebugMode;
    }

    public void setIsInDebugMode(boolean isInDebugMode) {
        this.isInDebugMode = isInDebugMode;
    }

    public boolean isInDebugMode() {
        return isInDebugMode;
    }

    @Override
    public String getName() {
        return AESModel.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        jsonWriter.name("isInDebugMode").value(isInDebugMode());
    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
            if (name.equals("isInDebugMode")) {
                setIsInDebugMode(jsonReader.nextBoolean());
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
