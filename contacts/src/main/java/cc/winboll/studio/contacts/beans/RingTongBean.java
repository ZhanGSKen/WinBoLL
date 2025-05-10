package cc.winboll.studio.contacts.beans;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/02/24 18:47:11
 * @Describe 手机铃声设置参数类
 */
import cc.winboll.studio.libappbase.BaseBean;
import android.util.JsonWriter;
import java.io.IOException;
import android.media.AudioManager;
import android.util.JsonReader;

public class RingTongBean extends BaseBean {

    public static final String TAG = "AudioRingTongBean";

    // 铃声音量
    int streamVolume;
    
    public RingTongBean() {
        this.streamVolume = 100;
    }

    public RingTongBean(int streamVolume) {
        this.streamVolume = streamVolume;
    }

    public void setStreamVolume(int streamVolume) {
        this.streamVolume = streamVolume;
    }

    public int getStreamVolume() {
        return streamVolume;
    }

    @Override
    public String getName() {
        return RingTongBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        jsonWriter.name("streamVolume").value(getStreamVolume());

    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
            if (name.equals("streamVolume")) {
                setStreamVolume(jsonReader.nextInt());
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
