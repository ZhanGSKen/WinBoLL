package cc.winboll.studio.libappbase.sos;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/02 09:49:45
 * @Describe SOSCenterServiceModel
 *           Simple Operate Signal Service Model.
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;

public class SOSCenterServiceModel extends BaseBean {

    public static final String TAG = "SOSCenterServiceModel";

    boolean isEnable;

    public SOSCenterServiceModel() {
        this.isEnable = false;
    }

    public void setIsEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    public boolean isEnable() {
        return isEnable;
    }

    @Override
    public String getName() {
        return SOSCenterServiceModel.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        jsonWriter.name("isEnable").value(isEnable());

    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
            if (name.equals("isEnable")) {
                setIsEnable(jsonReader.nextBoolean());
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
