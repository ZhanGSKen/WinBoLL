package cc.winboll.studio.libaes.beans;

/**
 * @Author ZhanGSKen
 * @Date 2025/05/03 19:16
 */
import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;

public class WinBoLLClientServiceBean extends BaseBean {

    public static final String TAG = "WinBoLLClientServiceBean";
    
    // 服务是否正在使用中
    boolean isEnable;
    
    public WinBoLLClientServiceBean() {
        this.isEnable = false;
    }

    public WinBoLLClientServiceBean(boolean isEnable) {
        this.isEnable = isEnable;
    }

    public void setIsEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    public boolean isEnable() {
        return isEnable;
    }


    @Override
    public String getName() {
        return WinBoLLClientServiceBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        WinBoLLClientServiceBean bean = this;
        //jsonWriter.name("logLevel").value(bean.getLogLevel().ordinal());
    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
//            if (name.equals("logLevel")) {
//                setLogLevel(LogUtils.LOG_LEVEL.values()[jsonReader.nextInt()]);
//            } else {
//                return false;
//            }
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

    public static WinBoLLClientServiceBean loadWinBoLLClientServiceBean(Context context) {
        return new WinBoLLClientServiceBean();
    }
}
