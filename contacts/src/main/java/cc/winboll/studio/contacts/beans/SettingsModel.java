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
import cc.winboll.studio.contacts.utils.IntUtils;

public class SettingsModel extends BaseBean {

    public static final String TAG = "SettingsModel";
    
    public static final int MAX_INTRANGE = 666666;
    public static final int MIN_INTRANGE = 1;
    
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
    // BoBullToon 应用模块数据请求地址
    String szBoBullToon_URL;

    public SettingsModel() {
        this.dunTotalCount = 6;
        this.dunCurrentCount = 6;
        this.dunResumeSecondCount = 60;
        this.dunResumeCount = 1;
        this.isEnableDun = false;
        this.szBoBullToon_URL = "";
    }

    public SettingsModel(int dunTotalCount, int dunCurrentCount, int dunResumeSecondCount, int dunResumeCount, boolean isEnableDun, String szBoBullToon_URL) {
        this.dunTotalCount = getSettingsModelRangeInt(dunTotalCount);
        this.dunCurrentCount = getSettingsModelRangeInt(dunCurrentCount);
        this.dunResumeSecondCount = getSettingsModelRangeInt(dunResumeSecondCount);
        this.dunResumeCount = getSettingsModelRangeInt(dunResumeCount);
        this.isEnableDun = isEnableDun;
        this.szBoBullToon_URL = szBoBullToon_URL;
    }

    public void setBoBullToon_URL(String boBullToon_URL) {
        this.szBoBullToon_URL = boBullToon_URL;
    }

    public String getBoBullToon_URL() {
        return szBoBullToon_URL;
    }

    public void setDunTotalCount(int dunTotalCount) {
        this.dunTotalCount = getSettingsModelRangeInt(dunTotalCount);
    }

    public int getDunTotalCount() {
        return dunTotalCount;
    }

    public void setDunCurrentCount(int dunCurrentCount) {
        this.dunCurrentCount = getSettingsModelRangeInt(dunCurrentCount);
    }

    public int getDunCurrentCount() {
        return dunCurrentCount;
    }

    public void setDunResumeSecondCount(int dunResumeSecondCount) {
        this.dunResumeSecondCount = getSettingsModelRangeInt(dunResumeSecondCount);
    }

    public int getDunResumeSecondCount() {
        return dunResumeSecondCount;
    }

    public void setDunResumeCount(int dunResumeCount) {
        this.dunResumeCount = getSettingsModelRangeInt(dunResumeCount);
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
    
    int getSettingsModelRangeInt(int origin) {
        return IntUtils.getIntInRange(origin, MIN_INTRANGE, MAX_INTRANGE);
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
        jsonWriter.name("szBoBullToon_URL").value(getBoBullToon_URL());
        
    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
            if (name.equals("dunTotalCount")) {
                setDunTotalCount(getSettingsModelRangeInt(jsonReader.nextInt()));
            } else if (name.equals("dunCurrentCount")) {
                setDunCurrentCount(getSettingsModelRangeInt(jsonReader.nextInt()));
            } else if (name.equals("dunResumeSecondCount")) {
                setDunResumeSecondCount(getSettingsModelRangeInt(jsonReader.nextInt()));
            } else if (name.equals("dunResumeCount")) {
                setDunResumeCount(getSettingsModelRangeInt(jsonReader.nextInt()));
            } else if (name.equals("isEnableDun")) {
                setIsEnableDun(jsonReader.nextBoolean());
            } else if (name.equals("szBoBullToon_URL")) {
                setBoBullToon_URL(jsonReader.nextString());
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
