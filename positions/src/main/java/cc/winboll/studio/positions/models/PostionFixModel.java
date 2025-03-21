package cc.winboll.studio.positions.models;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/21 10:11:36
 * @Describe 定位数据修正模型
 */
import android.location.Location;
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;
import android.content.Context;
import cc.winboll.studio.positions.App;
import java.io.File;

public class PostionFixModel extends BaseBean {

    public static final String TAG = "PostionFixModel";

    // 纬度校验数据模型
    private double latitudeFixModel;
    // 经度校验数据模型
    private double longitudeFixModel;

    public PostionFixModel() {
        this.latitudeFixModel = 0;
        this.longitudeFixModel = 0;
    }

    public PostionFixModel(double latitudeFixModel, double longitudeFixModel) {
        this.latitudeFixModel = latitudeFixModel;
        this.longitudeFixModel = longitudeFixModel;
    }

    public void setLatitudeFixModel(double latitudeFixModel) {
        this.latitudeFixModel = latitudeFixModel;
    }

    public double getLatitudeFixModel() {
        return latitudeFixModel;
    }

    public void setLongitudeFixModel(double longitudeFixModel) {
        this.longitudeFixModel = longitudeFixModel;
    }

    public double getLongitudeFixModel() {
        return longitudeFixModel;
    }

    @Override
    public String getName() {
        return PostionFixModel.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        jsonWriter.name("latitudeFixModel").value(getLatitudeFixModel());
        jsonWriter.name("longitudeFixModel").value(getLongitudeFixModel());

    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
            if (name.equals("latitudeFixModel")) {
                setLatitudeFixModel(jsonReader.nextDouble());
            } else if (name.equals("longitudeFixModel")) {
                setLongitudeFixModel(jsonReader.nextDouble());
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
    
    public static void savePostionFixModel(PostionFixModel model) {
        saveBeanToFile(getPostionFixModelDataPath(), model);
    }
    
    public static PostionFixModel loadPostionFixModel() {
        PostionFixModel model = loadBeanFromFile(getPostionFixModelDataPath(), PostionFixModel.class);
        if(model == null) {
            model = new PostionFixModel();
        }
        return model;
    }
    
    static String getPostionFixModelDataPath() {
        return App.szDataFolder + File.separator + TAG + ".data";
    }
}
