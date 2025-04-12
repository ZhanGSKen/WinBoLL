package cc.winboll.studio.contacts.beans;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/02 19:51:40
 * @Describe SettingsModel
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;

public class SettingsModel extends BaseBean {

    public static final String TAG = "SettingsModel";

    // 云盾防御层数量
    int dunTotalCount;
    // 当前云盾防御层
    int dunCurrentCount;
    // 防御层恢复时间间隔(秒钟)
    int dunResumeSecondCount;
    // 每次恢复防御层数
    int dunResumeCount;
    // 是否启用云盾
    boolean isEnableDun;

    public SettingsModel() {
        this.dunTotalCount = 6;
        this.dunCurrentCount = 6;
        this.dunResumeSecondCount = 60;
        this.dunResumeCount = 1;
        this.isEnableDun = false;
    }

    public SettingsModel(int dunTotalCount, int dunCurrentCount, int dunResumeSecondCount, int dunResumeCount, boolean isEnableDun) {
        this.dunTotalCount = dunTotalCount;
        this.dunCurrentCount = dunCurrentCount;
        this.dunResumeSecondCount = dunResumeSecondCount;
        this.dunResumeCount = dunResumeCount;
        this.isEnableDun = isEnableDun;
    }

    public void setDunTotalCount(int dunTotalCount) {
        this.dunTotalCount = dunTotalCount;
    }

    public int getDunTotalCount() {
        return dunTotalCount;
    }

    public void setDunCurrentCount(int dunCurrentCount) {
        this.dunCurrentCount = dunCurrentCount;
    }

    public int getDunCurrentCount() {
        return dunCurrentCount;
    }

    public void setDunResumeSecondCount(int dunResumeSecondCount) {
        this.dunResumeSecondCount = dunResumeSecondCount;
    }

    public int getDunResumeSecondCount() {
        return dunResumeSecondCount;
    }

    public void setDunResumeCount(int dunResumeCount) {
        this.dunResumeCount = dunResumeCount;
    }

    public int getDunResumeCount() {
        return dunResumeCount;
    }

    public void setIsEnableDun(boolean isEnableDun) {
        this.isEnableDun = isEnableDun;
    }

    public boolean isEnableDun() {
        return isEnableDun;
    }



    @Override
    public String getName() {
        return SettingsModel.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        jsonWriter.name("dunTotalCount").value(getDunTotalCount());
        jsonWriter.name("dunCurrentCount").value(getDunCurrentCount());
        jsonWriter.name("dunResumeSecondCount").value(getDunResumeSecondCount());
        jsonWriter.name("dunResumeCount").value(getDunResumeCount());
        jsonWriter.name("isEnableDun").value(isEnableDun());

    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
            if (name.equals("dunTotalCount")) {
                setDunTotalCount(jsonReader.nextInt());
            } else if (name.equals("dunCurrentCount")) {
                setDunCurrentCount(jsonReader.nextInt());
            } else if (name.equals("dunResumeSecondCount")) {
                setDunResumeSecondCount(jsonReader.nextInt());
            } else if (name.equals("dunResumeCount")) {
                setDunResumeCount(jsonReader.nextInt());
            } else if (name.equals("isEnableDun")) {
                setIsEnableDun(jsonReader.nextBoolean());
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
