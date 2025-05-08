package cc.winboll.studio.mymessagemanager.beans;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2023/06/30 23:21:27
 * @Describe 应用配置数据类，V1 旧版。
 */
import cc.winboll.studio.mymessagemanager.utils.ThemeUtil;

public class AppConfigBean_V1 {

    // 当前国家代码(如+8612345678901代码就是86.)
    String countryCode = "86";
    // 是否合并的手机号码前缀
    boolean isMergeCountryCodePrefix = true;
    // TT语音延时播放毫秒数
    int ttsPlayDelayTimes = 3000;
    boolean enableService = false;
    boolean enableOnlyReceiveContacts = false;
    boolean enableTTS = false;
    boolean enableTTSRuleMode = false;
    //int appThemeID = ThemeUtil.getThemeID(ThemeUtil.BaseTheme.DEFAULT);

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

    public void setEnableService(boolean enableService) {
        this.enableService = enableService;
    }

    public boolean isEnableService() {
        return enableService;
    }

    public void setEnableOnlyReceiveContacts(boolean enableOnlyReceiveContacts) {
        this.enableOnlyReceiveContacts = enableOnlyReceiveContacts;
    }

    public boolean isEnableOnlyReceiveContacts() {
        return enableOnlyReceiveContacts;
    }

    public void setEnableTTS(boolean enableTTS) {
        this.enableTTS = enableTTS;
    }

    public boolean isEnableTTS() {
        return enableTTS;
    }

    public void setEnableTTSRuleMode(boolean enableTTSRuleMode) {
        this.enableTTSRuleMode = enableTTSRuleMode;
    }

    public boolean isEnableTTSRuleMode() {
        return enableTTSRuleMode;
    }

    /*public void setAppThemeID(int appThemeID) {
        this.appThemeID = appThemeID;
    }

    public int getAppThemeID() {
        return appThemeID;
    }*/

}
