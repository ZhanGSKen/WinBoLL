package cc.winboll.studio.timestamp.models;

/**
 * @Author ZhanGSKen
 * @Date 2025/05/05 09:51
 * @Describe 应用配置数据模型
 */
import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import cc.winboll.studio.timestamp.models.AppConfigsModel;
import java.io.IOException;

public class AppConfigsModel extends BaseBean {

    public static final String TAG = "AppConfigs";

    // 是否启动服务
    boolean isEnableService;
    // 时间戳显示格式
    String timeStampFormatString;
    // 时间戳拷贝格式
    String timeStampCopyFormatString;

    public AppConfigsModel() {
        this.isEnableService = false;
        this.timeStampFormatString = "yyyy-MM-dd HH:mm:ss";
        this.timeStampCopyFormatString = "yyyy_MM_dd-HH_mm_ss";
    }

    public void setTimeStampCopyFormatString(String timeStampCopyFormatString) {
        this.timeStampCopyFormatString = timeStampCopyFormatString;
    }

    public String getTimeStampCopyFormatString() {
        return timeStampCopyFormatString;
    }
    
    public void setTimeStampFormatString(String timeStampFormatString) {
        this.timeStampFormatString = timeStampFormatString;
    }

    public String getTimeStampFormatString() {
        return timeStampFormatString;
    }

    public void setIsEnableService(boolean isEnableService) {
        this.isEnableService = isEnableService;
    }

    public boolean isEnableService() {
        return isEnableService;
    }

    @Override
    public String getName() {
        return AppConfigsModel.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        jsonWriter.name("isEnableService").value(isEnableService());
        jsonWriter.name("timeStampFormatString").value(getTimeStampFormatString());
        jsonWriter.name("timeStampCopyFormatString").value(getTimeStampCopyFormatString());
    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
            if (name.equals("isEnableService")) {
                setIsEnableService(jsonReader.nextBoolean());
            } else if (name.equals("timeStampFormatString")) {
                setTimeStampFormatString(jsonReader.nextString());
            } else if (name.equals("timeStampCopyFormatString")) {
                setTimeStampCopyFormatString(jsonReader.nextString());
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
