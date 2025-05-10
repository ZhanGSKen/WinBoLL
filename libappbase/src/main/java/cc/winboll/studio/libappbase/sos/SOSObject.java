package cc.winboll.studio.libappbase.sos;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/02/27 14:12:05
 * @Describe SOSBean
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;

public class SOSObject extends BaseBean {

    public static final String TAG = "SOSObject";

    String objectPackageName;
    String objectServiveName;

    public SOSObject() {
        this.objectPackageName = "";
        this.objectServiveName = "";
    }

    public SOSObject(String objectPackageName, String objectServiveName) {
        this.objectPackageName = objectPackageName;
        this.objectServiveName = objectServiveName;
    }

    public void setObjectPackageName(String objectPackageName) {
        this.objectPackageName = objectPackageName;
    }

    public String getObjectPackageName() {
        return objectPackageName;
    }

    public void setObjectServiveName(String objectServiveName) {
        this.objectServiveName = objectServiveName;
    }

    public String getObjectServiveName() {
        return objectServiveName;
    }

    @Override
    public String getName() {
        return SOSObject.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        jsonWriter.name("objectPackageName").value(getObjectPackageName());
        jsonWriter.name("objectServiveName").value(getObjectServiveName());

    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
            if (name.equals("objectPackageName")) {
                setObjectPackageName(jsonReader.nextString());
            } else if (name.equals("objectServiveName")) {
                setObjectServiveName(jsonReader.nextString());
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
