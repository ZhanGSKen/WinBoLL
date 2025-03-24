package cc.winboll.studio.powerbell.beans;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/18 11:52:28
 * @Describe 应用背景图片数据类
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;

public class BackgroundPictureBean extends BaseBean {

    public static final String TAG = "BackgroundPictureBean";

    int backgroundWidth = 100;
    int backgroundHeight = 100;
    boolean isUseBackgroundFile = false;

    public BackgroundPictureBean() {
    }

    public BackgroundPictureBean(String recivedFileName, boolean isUseBackgroundFile) {
        this.isUseBackgroundFile = isUseBackgroundFile;
    }

    public void setBackgroundWidth(int backgroundWidth) {
        this.backgroundWidth = backgroundWidth;
    }

    public int getBackgroundWidth() {
        return backgroundWidth;
    }

    public void setBackgroundHeight(int backgroundHeight) {
        this.backgroundHeight = backgroundHeight;
    }

    public int getBackgroundHeight() {
        return backgroundHeight;
    }

    public void setIsUseBackgroundFile(boolean isUseBackgroundFile) {
        this.isUseBackgroundFile = isUseBackgroundFile;
    }

    public boolean isUseBackgroundFile() {
        return isUseBackgroundFile;
    }

    @Override
    public String getName() {
        return BackgroundPictureBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        BackgroundPictureBean bean = this;
        jsonWriter.name("backgroundWidth").value(bean.getBackgroundWidth());
        jsonWriter.name("backgroundHeight").value(bean.getBackgroundHeight());
        jsonWriter.name("isUseBackgroundFile").value(bean.isUseBackgroundFile());
    }

    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        BackgroundPictureBean bean = new BackgroundPictureBean();
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (name.equals("backgroundWidth")) {
                bean.setBackgroundWidth(jsonReader.nextInt());
            } else if (name.equals("backgroundHeight")) {
                bean.setBackgroundHeight(jsonReader.nextInt());
            } else if (name.equals("isUseBackgroundFile")) {
                bean.setIsUseBackgroundFile(jsonReader.nextBoolean());
            } else {
                jsonReader.skipValue();
            }
        }
        // 结束 JSON 对象
        jsonReader.endObject();
        return bean;
    }
}
