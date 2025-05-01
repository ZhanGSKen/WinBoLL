package cc.winboll.studio.powerbell.beans;

import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;
import java.io.Serializable;

public class BatteryInfoBean extends BaseBean implements Serializable {
    
    public static final String TAG = "BatteryInfoBean";

    // 记录电量的时间戳
    long timeStamp;
    // 电量值
    int battetyValue;
    
    public BatteryInfoBean() {
        this.timeStamp = 0;
        this.battetyValue = 0;
    }

    public BatteryInfoBean(long timeStamp, int battetyValue) {
        this.timeStamp = timeStamp;
        this.battetyValue = battetyValue;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setBattetyValue(int battetyValue) {
        this.battetyValue = battetyValue;
    }

    public int getBattetyValue() {
        return battetyValue;
    }

    @Override
    public String getName() {
        return BatteryInfoBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        BatteryInfoBean bean = this;
        jsonWriter.name("timeStamp").value(bean.getTimeStamp());
        jsonWriter.name("battetyValue").value(bean.getBattetyValue());
    }

    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        BatteryInfoBean bean = new BatteryInfoBean();
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (name.equals("timeStamp")) {
                bean.setTimeStamp(jsonReader.nextLong());
            } else if (name.equals("battetyValue")) {
                bean.setBattetyValue(jsonReader.nextInt());
            } else {
                jsonReader.skipValue();
            }
        }
        // 结束 JSON 对象
        jsonReader.endObject();
        return bean;
    }
}
