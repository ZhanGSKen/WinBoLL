package cc.winboll.studio.libappbase.bean;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/17 00:29:29
 * @Describe APPSOSReportBean
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;
import java.io.Serializable;

public class APPSOSBean extends BaseBean {

    public static final String TAG = "APPSOSBean";

    protected String sosPackage;
    protected String sosClassName;

    public APPSOSBean() {
        this.sosPackage = "";
        this.sosClassName = "";
    }

    public APPSOSBean(String sosPackage, String sosClassName) {
        this.sosPackage = sosPackage;
        this.sosClassName = sosClassName;
    }

    public void setSosPackage(String sosPackage) {
        this.sosPackage = sosPackage;
    }

    public String getSosPackage() {
        return sosPackage;
    }

    public void setSosClassName(String sosClassName) {
        this.sosClassName = sosClassName;
    }

    public String getSosClassName() {
        return sosClassName;
    }

    @Override
    public String getName() {
        return APPSOSBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        jsonWriter.name("sosPackage").value(getSosPackage());
        jsonWriter.name("sosClassName").value(getSosClassName());

    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
            if (name.equals("sosPackage")) {
                setSosPackage(jsonReader.nextString());
            } else if (name.equals("sosClassName")) {
                setSosClassName(jsonReader.nextString());
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
