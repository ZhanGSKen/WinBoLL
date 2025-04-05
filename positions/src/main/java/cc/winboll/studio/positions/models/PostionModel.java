package cc.winboll.studio.positions.models;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/25 02:58:33
 * @Describe LocationJson
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;
import android.location.Location;
import java.util.UUID;

public class PostionModel extends BaseBean {

    public static final String TAG = "PostionModel";
    
    // UUID 唯一位置标识
    private String uuid;
    // 纬度
    private double latitude;
    // 经度
    private double longitude;
    // 标记时间
    private long timestamp;
    // 精确度
    private double accuracy;
    private String provider;
    private String comments;
    private boolean isEnable;
    private boolean isSimpleView;
    
    public PostionModel() {
        this.uuid = UUID.randomUUID().toString();
        this.latitude = 0.0f;
        this.longitude = 0.0f;
        this.timestamp = 0L;
        this.accuracy = 0.0f;
        this.provider = "";
        this.comments = "";
        this.isEnable = false;
        this.isSimpleView = true;
    }

    public PostionModel(Location location) {
        this.uuid = UUID.randomUUID().toString();
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.timestamp = location.getTime();
        this.accuracy = location.getAccuracy();
        this.provider = location.getProvider();
        this.comments = "";
        this.isEnable = false;
        this.isSimpleView = true;
    }

    public void setIsEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getComments() {
        return comments;
    }

    public void setIsSimpleView(boolean isSimpleView) {
        this.isSimpleView = isSimpleView;
    }

    public boolean isSimpleView() {
        return isSimpleView;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public String getUuid() {
        return uuid;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }


    @Override
    public String getName() {
        return PostionModel.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        jsonWriter.name("uuid").value(getUuid());
        jsonWriter.name("latitude").value(getLatitude());
        jsonWriter.name("longitude").value(getLongitude());
        jsonWriter.name("timestamp").value(getTimestamp());
        jsonWriter.name("accuracy").value(getAccuracy());
        jsonWriter.name("provider").value(getProvider());
        jsonWriter.name("comments").value(getComments());
        jsonWriter.name("isEnable").value(isEnable());
        jsonWriter.name("isSimpleView").value(isSimpleView());

    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
            if (name.equals("uuid")) {
                setUuid(jsonReader.nextString());
            } else if (name.equals("latitude")) {
                setLatitude(jsonReader.nextDouble());
            } else if (name.equals("longitude")) {
                setLongitude(jsonReader.nextDouble());
            } else if (name.equals("timestamp")) {
                setTimestamp(jsonReader.nextLong());
            } else if (name.equals("accuracy")) {
                setAccuracy(jsonReader.nextDouble());
            } else if (name.equals("provider")) {
                setProvider(jsonReader.nextString());
            } else if (name.equals("comments")) {
                setComments(jsonReader.nextString());
            } else if (name.equals("isEnable")) {
                setIsEnable(jsonReader.nextBoolean());
            } else if (name.equals("isSimpleView")) {
                setIsSimpleView(jsonReader.nextBoolean());
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

    public Location toLocation() {
        Location location = new Location(getProvider());
        location.setLatitude(getLatitude());
        location.setLongitude(getLongitude());
        location.setTime(getTimestamp());
        location.setAccuracy((float)getAccuracy());
        return location;
    }

}
