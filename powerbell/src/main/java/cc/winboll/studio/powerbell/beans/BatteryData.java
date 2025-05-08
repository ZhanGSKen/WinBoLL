package cc.winboll.studio.powerbell.beans;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/22 14:30:51
 * @Describe 电池报告数据模型
 */
public class BatteryData {

    public static final String TAG = "BatteryData";

    private int currentLevel;
    private String dischargeTime;
    private String chargeTime;

    public BatteryData(int currentLevel, String dischargeTime, String chargeTime) {
        this.currentLevel = currentLevel;
        this.dischargeTime = dischargeTime;
        this.chargeTime = chargeTime;
    }

    public int getCurrentLevel() { return currentLevel; }
    public String getDischargeTime() { return dischargeTime; }
    public String getChargeTime() { return chargeTime; }
}

