package cc.winboll.studio.contacts.beans;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/21 09:52:10
 * @Describe 电话黑名单规则
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;

public class PhoneBlackRuleBean extends BaseBean {
    
    public static final String TAG = "PhoneBlackRuleBean";
    
    String ruleText;
    boolean isEnable;
    
    public PhoneBlackRuleBean() {
        this.ruleText = "";
        this.isEnable = false;
    }

    public PhoneBlackRuleBean(String ruleText, boolean isEnable) {
        this.ruleText = ruleText;
        this.isEnable = isEnable;
    }

    public void setRuleText(String ruleText) {
        this.ruleText = ruleText;
    }

    public String getRuleText() {
        return ruleText;
    }

    public void setIsEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    public boolean isEnable() {
        return isEnable;
    }

    @Override
    public String getName() {
        return PhoneBlackRuleBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        jsonWriter.name("ruleText").value(getRuleText());
        jsonWriter.name("isEnable").value(isEnable());

    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
            if (name.equals("ruleText")) {
                setRuleText(jsonReader.nextString());
            } else if (name.equals("isEnable")) {
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
    
    
}
