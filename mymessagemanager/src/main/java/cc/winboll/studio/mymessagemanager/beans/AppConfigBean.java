package cc.winboll.studio.mymessagemanager.beans;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/06/02 20:07:44
 * @Describe 应用配置数据类
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.mymessagemanager.utils.ThemeUtil;
import cc.winboll.studio.shared.app.BaseBean;
import java.io.IOException;

public class AppConfigBean extends BaseBean {

    public static final String TAG = "AppConfigBean";

    // 当前国家代码(如+8612345678901代码就是86.)
    String countryCode = "86";
    // 是否合并的手机号码前缀
    boolean isMergeCountryCodePrefix = true;
    // TT语音延时播放毫秒数
    int ttsPlayDelayTimes = 3000;
    boolean isEnableService = false;
    boolean isEnableOnlyReceiveContacts = false;
    boolean isEnableTTS = false;
    boolean isEnableTTSRuleMode = false;
    boolean isSMSRecycleProtectMode = false;
    // 保护式预览拒绝显示的字符集
    String protectModerRefuseChars = "设定被和谐的字符";
    // 保护式预览拒绝显示的字符集的替代字符
    String protectModerReplaceChars = "当前替代显示字符";
    //int appThemeID = ThemeUtil.getThemeID(ThemeUtil.BaseTheme.DEFAULT);

    public void setProtectModerRefuseChars(String protectModerRefuseChars) {
        this.protectModerRefuseChars = protectModerRefuseChars;
    }

    public String getProtectModerRefuseChars() {
        return protectModerRefuseChars;
    }

    public void setProtectModerReplaceChars(String protectModerReplaceChars) {
        this.protectModerReplaceChars = protectModerReplaceChars;
    }

    public String getProtectModerReplaceChars() {
        return protectModerReplaceChars;
    }

    public void setIsSMSRecycleProtectMode(boolean isSMSRecycleProtectMode) {
        this.isSMSRecycleProtectMode = isSMSRecycleProtectMode;
    }

    public boolean isSMSRecycleProtectMode() {
        return isSMSRecycleProtectMode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setIsMergeCountryCodePrefix(boolean isMergeCountryCodePrefix) {
        this.isMergeCountryCodePrefix = isMergeCountryCodePrefix;
    }

    public boolean isMergeCountryCodePrefix() {
        return isMergeCountryCodePrefix;
    }

    public void setTtsPlayDelayTimes(int ttsPlayDelayTimes) {
        this.ttsPlayDelayTimes = ttsPlayDelayTimes;
    }

    public int getTtsPlayDelayTimes() {
        return ttsPlayDelayTimes;
    }

    public void setIsEnableService(boolean isEnableService) {
        this.isEnableService = isEnableService;
    }

    public boolean isEnableService() {
        return isEnableService;
    }

    public void setIsEnableOnlyReceiveContacts(boolean isEnableOnlyReceiveContacts) {
        this.isEnableOnlyReceiveContacts = isEnableOnlyReceiveContacts;
    }

    public boolean isEnableOnlyReceiveContacts() {
        return isEnableOnlyReceiveContacts;
    }

    public void setIsEnableTTS(boolean isEnableTTS) {
        this.isEnableTTS = isEnableTTS;
    }

    public boolean isEnableTTS() {
        return isEnableTTS;
    }

    public void setIsEnableTTSRuleMode(boolean isEnableTTSRuleMode) {
        this.isEnableTTSRuleMode = isEnableTTSRuleMode;
    }

    public boolean isEnableTTSRuleMode() {
        return isEnableTTSRuleMode;
    }

    /*public void setAppThemeID(int appThemeID) {
        this.appThemeID = appThemeID;
    }

    public int getAppThemeID() {
        return appThemeID;
    }

    public void setAppTheme(ThemeUtil.BaseTheme baseTheme) {
        setAppThemeID(ThemeUtil.getThemeID(baseTheme));
    }*/

    @Override
    public String getName() {
        return AppConfigBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        AppConfigBean bean = this;
        jsonWriter.name("countryCode").value(bean.getCountryCode());
        jsonWriter.name("isMergeCountryCodePrefix").value(bean.isMergeCountryCodePrefix());
        jsonWriter.name("ttsPlayDelayTimes").value(bean.getTtsPlayDelayTimes());
        jsonWriter.name("isEnableService").value(bean.isEnableService());
        jsonWriter.name("isEnableOnlyReceiveContacts").value(bean.isEnableOnlyReceiveContacts());
        jsonWriter.name("isEnableTTS").value(bean.isEnableTTS());
        jsonWriter.name("isEnableTTSRuleMode").value(bean.isEnableTTSRuleMode());
        jsonWriter.name("isSMSRecycleProtectMode").value(bean.isSMSRecycleProtectMode());
        jsonWriter.name("protectModerRefuseChars").value(bean.getProtectModerRefuseChars());
        jsonWriter.name("protectModerReplaceChars").value(bean.getProtectModerReplaceChars());
        //jsonWriter.name("appThemeID").value(bean.getAppThemeID());
    }

    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        AppConfigBean bean = new AppConfigBean();
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (name.equals("countryCode")) {
                bean.setCountryCode(jsonReader.nextString());
            } else if (name.equals("isMergeCountryCodePrefix")) {
                bean.setIsMergeCountryCodePrefix(jsonReader.nextBoolean());
            } else if (name.equals("ttsPlayDelayTimes")) {
                bean.setTtsPlayDelayTimes(jsonReader.nextInt());
            } else if (name.equals("isEnableService")) {
                bean.setIsEnableService(jsonReader.nextBoolean());
            } else if (name.equals("isEnableOnlyReceiveContacts")) {
                bean.setIsEnableOnlyReceiveContacts(jsonReader.nextBoolean());
            } else if (name.equals("isEnableTTS")) {
                bean.setIsEnableTTS(jsonReader.nextBoolean());
            } else if (name.equals("isEnableTTSRuleMode")) {
                bean.setIsEnableTTSRuleMode(jsonReader.nextBoolean());
            } else if (name.equals("isSMSRecycleProtectMode")) {
                bean.setIsSMSRecycleProtectMode(jsonReader.nextBoolean());
            } else if (name.equals("protectModerRefuseChars")) {
                bean.setProtectModerRefuseChars(jsonReader.nextString());
            } else if (name.equals("protectModerReplaceChars")) {
                bean.setProtectModerReplaceChars(jsonReader.nextString());
            } /*else if (name.equals("appThemeID")) {
                bean.setAppThemeID(jsonReader.nextInt());
            }*/ else {
                jsonReader.skipValue();
            }
        }
        // 结束 JSON 对象
        jsonReader.endObject();
        return bean;
    }
}
