package cc.winboll.studio.mymessagemanager.beans;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/05/30 10:57:14
 * @Describe 短信接收规则类，V1 旧版。
 */
public class SMSAcceptRuleBean_V1 {

    public static final String TAG = "SMSAcceptRuleBean_V1";

    // 用户ID
    String userID = "";
    // 规则数据
    String ruleData = "";
    // 是否启用
    boolean enable = false;

    public SMSAcceptRuleBean_V1() {}

    public SMSAcceptRuleBean_V1(String userID, String ruleData, boolean enable) {
        this.userID = userID;
        this.ruleData = ruleData;
        this.enable = enable;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public void setRuleData(String ruleData) {
        this.ruleData = ruleData;
    }

    public String getRuleData() {
        return ruleData;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnable() {
        return enable;
    }
}
