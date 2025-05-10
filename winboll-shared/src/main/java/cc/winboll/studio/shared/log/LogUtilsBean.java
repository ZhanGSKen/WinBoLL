package cc.winboll.studio.shared.log;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/08/23 15:39:07
 * @Describe LogUtils 数据配置类。
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.shared.app.BaseBean;
import java.io.IOException;
public class LogUtilsBean extends BaseBean {

    public static final String TAG = "LogUtilsBean";

    LogUtils.LOG_LEVEL logLevel;

    public LogUtilsBean() {
        this.logLevel = LogUtils.LOG_LEVEL.Off;
    }

    public LogUtilsBean(LogUtils.LOG_LEVEL logLevel) {
        this.logLevel = logLevel;
    }

    public void setLogLevel(LogUtils.LOG_LEVEL logLevel) {
        this.logLevel = logLevel;
    }

    public LogUtils.LOG_LEVEL getLogLevel() {
        return logLevel;
    }

    @Override
    public String getName() {
        return LogUtilsBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        LogUtilsBean bean = this;
        jsonWriter.name("logLevel").value(bean.getLogLevel().ordinal());
    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
            if (name.equals("logLevel")) {
                setLogLevel(LogUtils.LOG_LEVEL.values()[jsonReader.nextInt()]);
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
