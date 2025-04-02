package cc.winboll.studio.autoinstaller.models;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/04/02 20:50:29
 * @Describe 监控的 APK 安装文件对应的应用信息数据模型
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;

public class APKModel extends BaseBean {

    public static final String TAG = "APPModel";

    // 每次更新的 APK 文件对应的应用包名称
    String apkPackageName;

    public APKModel() {
        this.apkPackageName = "";
    }

    public APKModel(String apkPackageName) {
        this.apkPackageName = apkPackageName;
    }

    public void setApkPackageName(String apkPackageName) {
        this.apkPackageName = apkPackageName;
    }

    public String getApkPackageName() {
        return apkPackageName;
    }



    @Override
    public String getName() {
        return APKModel.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        jsonWriter.name("appPackageName").value(getApkPackageName());

    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
            if (name.equals("appPackageName")) {
                setApkPackageName(jsonReader.nextString());
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
