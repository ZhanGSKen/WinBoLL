package cc.winboll.studio.positions.models;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/10/01 04:50
 * @Describe AppConfigsModel
 */
 import cc.winboll.studio.libappbase.BaseBean;
import android.util.JsonWriter;
import android.util.JsonReader;
import java.io.IOException;

public class AppConfigsModel extends BaseBean {
    
    public static final String TAG = "AppConfigsModel";
	
	boolean isEnableMainService;

	public AppConfigsModel(boolean isEnableMainService) {
		this.isEnableMainService = isEnableMainService;
	}
	
	public AppConfigsModel() {
		this.isEnableMainService = false;
	}

	public void setIsEnableMainService(boolean isEnableMainService) {
		this.isEnableMainService = isEnableMainService;
	}

	public boolean isEnableMainService() {
		return isEnableMainService;
	}
    
    @Override
	public String getName() {
		return AppConfigsModel.class.getName();
	}

	// JSON序列化（保存位置数据）
	@Override
	public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
		super.writeThisToJsonWriter(jsonWriter);
		jsonWriter.name("isEnableDistanceRefreshService").value(isEnableMainService());
	}

	// JSON反序列化（加载位置数据，校验字段）
	@Override
	public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
		if (super.initObjectsFromJsonReader(jsonReader, name)) { 
			return true; 
		} else {
			if (name.equals("isEnableDistanceRefreshService")) {
				setIsEnableMainService(jsonReader.nextBoolean());
			} else {
				return false;
			}
		}
		return true;
	}

	// 从JSON读取位置数据
	@Override
	public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
		jsonReader.beginObject();
		while (jsonReader.hasNext()) {
			String name = jsonReader.nextName();
			if (!initObjectsFromJsonReader(jsonReader, name)) {
				jsonReader.skipValue(); // 跳过未知字段
			}
		}
		jsonReader.endObject();
		return this;
	}
}
