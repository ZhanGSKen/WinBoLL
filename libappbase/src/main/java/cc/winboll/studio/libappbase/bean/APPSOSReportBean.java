package cc.winboll.studio.libappbase.bean;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/17 10:05:09
 * @Describe APPSOSReportBean
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;

public class APPSOSReportBean extends BaseBean {
    
    public static final String TAG = "APPSOSReportBean";
    
    protected String sosReport;
    
    public APPSOSReportBean() {
        this.sosReport = "";
    }

    public APPSOSReportBean(String sosReport) {
        this.sosReport = sosReport;
    }

    public void setSosReport(String sosReport) {
        this.sosReport = sosReport;
    }

    public String getSosReport() {
        return sosReport;
    }

    @Override
    public String getName() {
        return APPSOSReportBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        jsonWriter.name("sosReport").value(getSosReport());

    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
            if (name.equals("sosReport")) {
                setSosReport(jsonReader.nextString());
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
