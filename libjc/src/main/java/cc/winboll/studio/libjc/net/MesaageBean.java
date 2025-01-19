package cc.winboll.studio.libjc.net;
import android.util.JsonReader;
import android.util.JsonWriter;
import java.io.IOException;
import main.java.cc.winboll.studio.libjc.bean.BaseBean;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/09 20:07:06
 * @Describe 消息类
 */
public class MesaageBean extends BaseBean {

    public static final String TAG = "MesaageBean";

    public static enum MESAAGE_TYPE {
        UNKNOWN,
        CONNECTION_STATUS,
        MESSAGE
        ;
    }

    MESAAGE_TYPE mMESAAGE_TYPE = MESAAGE_TYPE.UNKNOWN;
    String messageContents;


    public void setMESAAGE_TYPE(MESAAGE_TYPE type) {
        this.mMESAAGE_TYPE = type;
    }

    public MESAAGE_TYPE getMESAAGE_TYPE() {
        return mMESAAGE_TYPE;
    }

    public void setMessageContents(String messageContents) {
        this.messageContents = messageContents;
    }

    public String getMessageContents() {
        return messageContents;
    }

    @Override
    public String getName() {
        return MesaageBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        MesaageBean bean = this;
        jsonWriter.name("MESAAGE_TYPE").value(bean.mMESAAGE_TYPE.ordinal());
        jsonWriter.name("messageContents").value(bean.getMessageContents());

    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
            if (name.equals("MESAAGE_TYPE")) {
                setMESAAGE_TYPE(MESAAGE_TYPE.values()[jsonReader.nextInt()]);
            } else if (name.equals("messageContents")) {
                setMessageContents(jsonReader.nextString());
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
    
    public MesaageBean parseString() {
        return null;
    }
}
