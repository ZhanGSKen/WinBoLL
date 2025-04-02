package cc.winboll.studio.libaes.winboll;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/28 19:05:15
 * @Describe WinBollService 运行参数配置
 */
import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;

public class WinBollClientServiceBean extends BaseBean {

    public static final String TAG = "WinBollClientServiceBean";

    volatile boolean isEnable;

    public WinBollClientServiceBean() {
        isEnable = false;
    }

    public void setIsEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    public boolean isEnable() {
        return isEnable;
    }

    @Override
    public String getName() {
        return WinBollClientServiceBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        WinBollClientServiceBean bean = this;
        jsonWriter.name("isEnable").value(bean.isEnable());
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

    public static WinBollClientServiceBean loadWinBollClientServiceBean(Context context) {
        WinBollClientServiceBean bean = WinBollClientServiceBean.loadBean(context, WinBollClientServiceBean.class);
        return bean == null ? new WinBollClientServiceBean() : bean;
    }

    public static boolean saveWinBollServiceBean(WinBollClientService service, WinBollClientServiceBean bean) {
        return WinBollClientServiceBean.saveBean(service, bean);
    }
}
