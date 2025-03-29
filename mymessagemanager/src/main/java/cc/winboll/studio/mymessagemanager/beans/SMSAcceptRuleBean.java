package cc.winboll.studio.mymessagemanager.beans;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/05/30 10:57:14
 * @Describe 短信接收规则类
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;

public class SMSAcceptRuleBean extends BaseBean {

    public static final String TAG = "SMSAcceptRuleBean";

    // 规则类型枚举
    public enum RuleType { ACCEPT, REFUSE, REGEXPPIUTILS_ISPPIOK_FALSE }
    
    // 用户ID
    int userId = -1;
    // 规则数据
    String ruleData = "";
    // 是否启用
    boolean isEnable = false;
    // 规则类型
    RuleType ruleType = RuleType.REFUSE;
    // 是否简单视图
    boolean isSimpleView = false;

    public SMSAcceptRuleBean() {}

    public SMSAcceptRuleBean(int userId, String ruleData, boolean isEnable, RuleType ruleType, boolean isSimpleView) {
        this.userId = userId;
        this.ruleData = ruleData;
        this.isEnable = isEnable;
        this.ruleType = ruleType;
        this.isSimpleView = isSimpleView;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    public RuleType getRuleType() {
        return ruleType;
    }
    
    public void setIsSimpleView(boolean isSimpleView) {
        this.isSimpleView = isSimpleView;
    }

    public boolean isSimpleView() {
        return isSimpleView;
    }

    public void setUserId(int userID) {
        this.userId = userID;
    }

    public int getUserId() {
        return userId;
    }

    public void setRuleData(String ruleData) {
        this.ruleData = ruleData;
    }

    public String getRuleData() {
        return ruleData;
    }

    public void setIsEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    public boolean isEnable() {
        return isEnable;
    }

    @Override
    public String getName() {
        return SMSAcceptRuleBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        SMSAcceptRuleBean bean = this;
        jsonWriter.name("userId").value(bean.getUserId());
        jsonWriter.name("ruleData").value(bean.getRuleData());
        jsonWriter.name("isEnable").value(bean.isEnable());
        jsonWriter.name("ruleType").value(bean.getRuleType().ordinal());
        jsonWriter.name("isSimpleView").value(bean.isSimpleView());
    }

    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        SMSAcceptRuleBean bean = new SMSAcceptRuleBean();
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (name.equals("userId")) {
                bean.setUserId(jsonReader.nextInt());
            } else if (name.equals("ruleData")) {
                bean.setRuleData(jsonReader.nextString());
            } else if (name.equals("isEnable")) {
                bean.setIsEnable(jsonReader.nextBoolean());
            } else if (name.equals("ruleType")) {
                bean.setRuleType(RuleType.values()[jsonReader.nextInt()]);
            } else if (name.equals("isSimpleView")) {
                bean.setIsSimpleView(jsonReader.nextBoolean());
            } else {
                jsonReader.skipValue();
            }
        }
        // 结束 JSON 对象
        jsonReader.endObject();
        return bean;
    }
}
