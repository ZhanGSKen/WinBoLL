package cc.winboll.studio.powerbell.beans;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/07/18 07:06:07
 * @Describe 服务控制参数
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;

public class ControlCenterServiceBean extends BaseBean {

    public static final String TAG = "ControlCenterServiceBean";

    boolean isEnableService = false;
    
    public ControlCenterServiceBean() {
        this.isEnableService = false;
    }

    public ControlCenterServiceBean(boolean isEnableService) {
        this.isEnableService = isEnableService;
    }

    public void setIsEnableService(boolean isEnableService) {
        this.isEnableService = isEnableService;
    }

    public boolean isEnableService() {
        return isEnableService;
    }

    @Override
    public String getName() {
        return ControlCenterServiceBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        ControlCenterServiceBean bean = this;
        jsonWriter.name("isEnableService").value(bean.isEnableService());
    }

    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        ControlCenterServiceBean bean = new ControlCenterServiceBean();
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (name.equals("isEnableService")) {
                bean.setIsEnableService(jsonReader.nextBoolean());
            } else {
                jsonReader.skipValue();
            }
        }
        // 结束 JSON 对象
        jsonReader.endObject();
        return bean;
    }
}
