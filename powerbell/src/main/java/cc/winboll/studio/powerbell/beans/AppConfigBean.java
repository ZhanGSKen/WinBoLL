package cc.winboll.studio.powerbell.beans;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/04/29 17:24:53
 * @Describe 应用运行参数类
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;
import java.io.Serializable;

public class AppConfigBean extends BaseBean implements Serializable {

    transient public static final String TAG = "AppConfigBean";

    boolean isEnableUsegeReminder = false;
    int usegeReminderValue = 45;
    boolean isEnableChargeReminder = false;
    int chargeReminderValue = 100;
    // 铃声提醒间隔时间。.
    int reminderIntervalTime = 5000;
    // 电池是否正在充电。
    boolean isCharging = false;
    // 电池当前电量。.
    int currentValue = -1;

    public AppConfigBean() {
        setChargeReminderValue(100);
        setIsEnableChargeReminder(false);
        setUsegeReminderValue(10);
        setIsEnableUsegeReminder(false);
        setReminderIntervalTime(5000);
    }

    public void setReminderIntervalTime(int reminderIntervalTime) {
        this.reminderIntervalTime = reminderIntervalTime;
    }

    public int getReminderIntervalTime() {
        return reminderIntervalTime;
    }

    public void setIsCharging(boolean isCharging) {
        this.isCharging = isCharging;
    }

    public boolean isCharging() {
        return isCharging;
    }

    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public void setIsEnableUsegeReminder(boolean isEnableUsegeReminder) {
        this.isEnableUsegeReminder = isEnableUsegeReminder;
    }

    public boolean isEnableUsegeReminder() {
        return isEnableUsegeReminder;
    }

    public void setUsegeReminderValue(int usegeReminderValue) {
        this.usegeReminderValue = usegeReminderValue;
    }

    public int getUsegeReminderValue() {
        return usegeReminderValue;
    }

    public void setIsEnableChargeReminder(boolean isEnableChargeReminder) {
        this.isEnableChargeReminder = isEnableChargeReminder;
    }

    public boolean isEnableChargeReminder() {
        return isEnableChargeReminder;
    }

    public void setChargeReminderValue(int chargeReminderValue) {
        this.chargeReminderValue = chargeReminderValue;
    }

    public int getChargeReminderValue() {
        return chargeReminderValue;
    }

    @Override
    public String getName() {
        return AppConfigBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        AppConfigBean bean = this;
        jsonWriter.name("isEnableUsegeReminder").value(bean.isEnableUsegeReminder());
        jsonWriter.name("usegeReminderValue").value(bean.getUsegeReminderValue());
        jsonWriter.name("isEnableChargeReminder").value(bean.isEnableChargeReminder());
        jsonWriter.name("chargeReminderValue").value(bean.getChargeReminderValue());
    }

    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        AppConfigBean bean = new AppConfigBean();
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (name.equals("isEnableUsegeReminder")) {
                bean.setIsEnableUsegeReminder(jsonReader.nextBoolean());
            } else if (name.equals("usegeReminderValue")) {
                bean.setUsegeReminderValue(jsonReader.nextInt());
            } else if (name.equals("isEnableChargeReminder")) {
                bean.setIsEnableChargeReminder(jsonReader.nextBoolean());
            } else if (name.equals("chargeReminderValue")) {
                bean.setChargeReminderValue(jsonReader.nextInt());
            } else {
                jsonReader.skipValue();
            }
        }
        // 结束 JSON 对象
        jsonReader.endObject();
        return bean;
    }
}
