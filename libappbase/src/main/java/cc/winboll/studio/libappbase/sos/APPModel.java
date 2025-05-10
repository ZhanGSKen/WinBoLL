package cc.winboll.studio.libappbase.sos;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/03/02 10:28:08
 * @Describe APPModel
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;

public class APPModel extends BaseBean {

    public static final String TAG = "APPModel";

    String appPackageName;
    String appMainServiveName;

    public APPModel() {
        this.appPackageName = "";
        this.appMainServiveName = "";
    }

    public APPModel(String appPackageName, String appMainServiveName) {
        this.appPackageName = appPackageName;
        this.appMainServiveName = appMainServiveName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppMainServiveName(String appMainServiveName) {
        this.appMainServiveName = appMainServiveName;
    }

    public String getAppMainServiveName() {
        return appMainServiveName;
    }

    @Override
    public String getName() {
        return APPModel.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        jsonWriter.name("appPackageName").value(getAppPackageName());
        jsonWriter.name("appMainServiveName").value(getAppMainServiveName());

    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
            if (name.equals("appPackageName")) {
                setAppPackageName(jsonReader.nextString());
            } else if (name.equals("appMainServiveName")) {
                setAppMainServiveName(jsonReader.nextString());
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
