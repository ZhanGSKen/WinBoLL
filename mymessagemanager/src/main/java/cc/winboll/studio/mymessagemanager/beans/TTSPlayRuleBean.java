package cc.winboll.studio.mymessagemanager.beans;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/05/28 20:22:12
 * @Describe TTS 语音播放规则类
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.shared.app.BaseBean;
import java.io.IOException;

public class TTSPlayRuleBean extends BaseBean {

    public static final String TAG = "TTSPlayRuleBean";

    // 用户ID
    int userId = -1;
    // TTS语音规则名称
    String ruleName = "";
    // 短信测试文本
    String demoSMSText = "";
    // 短信内容查询正则文本
    String patternText = "";
    // TTS语音播报正则文本
    String ttsRuleText = "";
    // 是否启用简单视图
    boolean isSimpleView = false;
    // 是否启用规则
    boolean isEnable = false;

    public TTSPlayRuleBean() {}

    public TTSPlayRuleBean(int userId, String ruleName, String demoSMSText, String patternText, String ttsRuleText, boolean isSimpleView, boolean isEnable) {
        this.userId = userId;
        this.ruleName = ruleName;
        this.demoSMSText = demoSMSText;
        this.patternText = patternText;
        this.ttsRuleText = ttsRuleText;
        this.isSimpleView = isSimpleView;
        this.isEnable = isEnable;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setDemoSMSText(String demoSMSText) {
        this.demoSMSText = demoSMSText;
    }

    public String getDemoSMSText() {
        return demoSMSText;
    }

    public void setPatternText(String patternText) {
        this.patternText = patternText;
    }

    public String getPatternText() {
        return patternText;
    }

    public void setTtsRuleText(String ttsRuleText) {
        this.ttsRuleText = ttsRuleText;
    }

    public String getTtsRuleText() {
        return ttsRuleText;
    }

    public void setIsSimpleView(boolean isSimpleView) {
        this.isSimpleView = isSimpleView;
    }

    public boolean isSimpleView() {
        return isSimpleView;
    }

    public void setIsEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    public boolean isEnable() {
        return isEnable;
    }
    

    @Override
    public String getName() {
        return TTSPlayRuleBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        TTSPlayRuleBean bean = this;
        jsonWriter.name("userId").value(bean.userId);
        jsonWriter.name("ruleName").value(bean.ruleName);
        jsonWriter.name("demoSMSText").value(bean.demoSMSText);
        jsonWriter.name("patternText").value(bean.patternText);
        jsonWriter.name("ttdRuleText").value(bean.ttsRuleText);
        jsonWriter.name("isSimpleView").value(bean.isSimpleView);
        jsonWriter.name("isEnable").value(bean.isEnable);
    }

    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        TTSPlayRuleBean bean = new TTSPlayRuleBean();
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (name.equals("userId")) {
                bean.setUserId(jsonReader.nextInt());
            } else if (name.equals("ruleName")) {
                bean.setRuleName(jsonReader.nextString());
            } else if (name.equals("demoSMSText")) {
                bean.setDemoSMSText(jsonReader.nextString());
            } else if (name.equals("patternText")) {
                bean.setPatternText(jsonReader.nextString());
            } else if (name.equals("ttdRuleText")) {
                bean.setTtsRuleText(jsonReader.nextString());
            } else if (name.equals("isSimpleView")) {
                bean.setIsSimpleView(jsonReader.nextBoolean());
            } else if (name.equals("isEnable")) {
                bean.setIsEnable(jsonReader.nextBoolean());
            } else {
                jsonReader.skipValue();
            }
        }
        // 结束 JSON 对象
        jsonReader.endObject();
        return bean;
    }
}
