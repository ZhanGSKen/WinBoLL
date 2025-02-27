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

public class APPNewsBean extends BaseBean {

    public static final String TAG = "APPNewsBean";

    protected String newsPackageName;
    protected String newsClassName;

    public APPNewsBean() {
        this.newsPackageName = "";
        this.newsClassName = "";
    }

    public APPNewsBean(String newsPackageName, String newsClassName) {
        this.newsPackageName = newsPackageName;
        this.newsClassName = newsClassName;
    }

    public void setNewsPackageName(String newsPackageName) {
        this.newsPackageName = newsPackageName;
    }

    public String getNewsPackageName() {
        return newsPackageName;
    }

    public void setNewsClassName(String sosClassName) {
        this.newsClassName = sosClassName;
    }

    public String getNewsClassName() {
        return newsClassName;
    }

    @Override
    public String getName() {
        return APPNewsBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        jsonWriter.name("newsPackageName").value(getNewsPackageName());
        jsonWriter.name("newsClassName").value(getNewsClassName());

    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
            if (name.equals("newsPackageName")) {
                setNewsPackageName(jsonReader.nextString());
            } else if (name.equals("newsClassName")) {
                setNewsClassName(jsonReader.nextString());
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
