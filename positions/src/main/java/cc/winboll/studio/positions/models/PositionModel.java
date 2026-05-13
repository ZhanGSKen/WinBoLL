package cc.winboll.studio.positions.models;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/09/29 18:57
 * @Describe 位置数据模型
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;
import java.util.UUID;

public class PositionModel extends BaseBean {

    public static final String TAG = "PositionModel";
	// 位置唯一标识符（与任务的positionId绑定）
	String positionId;
	// 经度（范围：-180~180）
	double longitude;
	// 纬度（范围：-90~90）
	double latitude;
	// 位置备注（空值时显示“无备注”）
	String memo;
	// 定位点与指定点实时距离长度
	double realPositionDistance;
	// 是否启用实时距离计算
	boolean isEnableRealPositionDistance;
	// 是否显示简单视图（true=简单视图，false=编辑视图）
	boolean isSimpleView = true;

	// 带参构造（强制初始化位置ID和经纬度）
	public PositionModel(String positionId, double longitude, double latitude, String memo, boolean isEnableRealPositionDistance) {
		this.positionId = (positionId == null || positionId.trim().isEmpty()) ? genPositionId() : positionId;
		this.longitude = Math.max(-180, Math.min(180, longitude)); // 经度范围限制
		this.latitude = Math.max(-90, Math.min(90, latitude)); // 纬度范围限制
		this.memo = (memo == null || memo.trim().isEmpty()) ? "无备注" : memo;
		this.isEnableRealPositionDistance = isEnableRealPositionDistance;
	}

	// 无参构造（默认值初始化，避免空指针）
	public PositionModel() {
		this.positionId = genPositionId();
		this.longitude = 0.0;
		this.latitude = 0.0;
		this.memo = "无备注";
		this.isEnableRealPositionDistance = false;
	}

	public void setRealPositionDistance(double realPositionDistance) {
		this.realPositionDistance = realPositionDistance;
	}

	public double getRealPositionDistance() {
		return realPositionDistance;
	}

	// ---------------------- Getter/Setter（确保字段有效性） ----------------------
	public void setPositionId(String positionId) {
		this.positionId = (positionId == null || positionId.trim().isEmpty()) ? genPositionId() : positionId;
	}

	public String getPositionId() {
		return positionId;
	}

	public void setIsEnableRealPositionDistance(boolean isEnableRealPositionDistance) {
		this.isEnableRealPositionDistance = isEnableRealPositionDistance;
	}

	public boolean isEnableRealPositionDistance() {
		return isEnableRealPositionDistance;
	}

	public void setIsSimpleView(boolean isSimpleView) {
		this.isSimpleView = isSimpleView;
	}

	public boolean isSimpleView() {
		return isSimpleView;
	}

	public void setMemo(String memo) {
		this.memo = (memo == null || memo.trim().isEmpty()) ? "无备注" : memo;
	}

	public String getMemo() {
		return memo;
	}

	public void setLongitude(double longitude) {
		this.longitude = Math.max(-180, Math.min(180, longitude)); // 限制经度范围
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = Math.max(-90, Math.min(90, latitude)); // 限制纬度范围
	}

	public double getLatitude() {
		return latitude;
	}

	// ---------------------- 父类方法重写 ----------------------
	@Override
	public String getName() {
		return PositionModel.class.getName();
	}

	// 生成唯一位置ID（与任务ID格式一致，确保关联匹配）
	public static String genPositionId() {
        UUID uniqueUuid = UUID.randomUUID();
        return uniqueUuid.toString(); // 36位标准UUID（含横杠，确保与任务ID格式统一）
    }

	// JSON序列化（保存位置数据）
	@Override
	public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
		super.writeThisToJsonWriter(jsonWriter);
		jsonWriter.name("positionId").value(getPositionId());
		jsonWriter.name("longitude").value(getLongitude());
		jsonWriter.name("latitude").value(getLatitude());
		jsonWriter.name("memo").value(getMemo());
		jsonWriter.name("isEnableRealPositionDistance").value(isEnableRealPositionDistance());
	}

	// JSON反序列化（加载位置数据，校验字段）
	@Override
	public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
		if (super.initObjectsFromJsonReader(jsonReader, name)) { 
			return true; 
		} else {
			if (name.equals("positionId")) {
				setPositionId(jsonReader.nextString());
			} else if (name.equals("longitude")) {
				setLongitude(jsonReader.nextDouble());
			} else if (name.equals("latitude")) {
				setLatitude(jsonReader.nextDouble());
			} else if (name.equals("memo")) {
				setMemo(jsonReader.nextString());
			} else if (name.equals("isEnableRealPositionDistance")) {
				setIsEnableRealPositionDistance(jsonReader.nextBoolean());
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

	// ---------------------- 核心工具方法：计算两点距离（Haversine公式，确保精度） ----------------------
	/**
	 * 计算两个位置之间的直线距离（地球表面最短距离）
	 * @param position1 第一个位置（非null）
	 * @param position2 第二个位置（非null）
	 * @param isKilometer 是否返回千米单位：true→千米，false→米
	 * @return 距离（保留1位小数，符合显示需求）
	 * @throws IllegalArgumentException 位置为null或经纬度无效时抛出
	 */
	public static double calculatePositionDistance(PositionModel position1, PositionModel position2, boolean isKilometer) {
		// 1. 校验参数有效性（避免计算异常）
		if (position1 == null || position2 == null) {
			throw new IllegalArgumentException("位置对象不能为null");
		}
		double lon1 = position1.getLongitude();
		double lat1 = position1.getLatitude();
		double lon2 = position2.getLongitude();
		double lat2 = position2.getLatitude();
		// 经纬度范围二次校验（确保有效）
		if (lat1 < -90 || lat1 > 90 || lat2 < -90 || lat2 > 90 
			|| lon1 < -180 || lon1 > 180 || lon2 < -180 || lon2 > 180) {
			throw new IllegalArgumentException("经纬度值无效（纬度：-90~90，经度：-180~180）");
		}

		// 2. Haversine公式计算（地球半径取6371km，行业标准）
		final double EARTH_RADIUS_KM = 6371;
		double radLat1 = Math.toRadians(lat1); // 角度转弧度
		double radLat2 = Math.toRadians(lat2);
		double radLon1 = Math.toRadians(lon1);
		double radLon2 = Math.toRadians(lon2);

		double deltaLat = radLat2 - radLat1; // 纬度差
		double deltaLon = radLon2 - radLon1; // 经度差

		// 核心公式
		double a = Math.pow(Math.sin(deltaLat / 2), 2) 
			+ Math.cos(radLat1) * Math.cos(radLat2) 
			* Math.pow(Math.sin(deltaLon / 2), 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distanceKm = EARTH_RADIUS_KM * c; // 距离（千米）

		// 3. 单位转换+精度处理（保留1位小数，符合显示需求）
		double distance;
		if (isKilometer) {
			distance = Math.round(distanceKm * 10.0) / 10.0; // 千米→1位小数
		} else {
			distance = Math.round(distanceKm * 1000 * 10.0) / 10.0; // 米→1位小数
		}

		return distance;
	}
}

