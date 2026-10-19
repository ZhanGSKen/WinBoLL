package cc.winboll.studio.positions.models;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/09/30 02:48
 * @Describe 位置任务数据模型
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;
import java.util.UUID;

public class PositionTaskModel extends BaseBean {
    
    public static final String TAG = "PositionTaskModel";
    // 任务标识符（唯一）
	String taskId;
	// 绑定的位置标识符（与PositionModel的positionId一一对应）
	String positionId;
	// 任务描述
	String taskDescription;
	// 任务距离条件：是否大于设定距离
	boolean isGreaterThan;
	// 任务距离条件：是否小于设定距离（与isGreaterThan互斥）
	boolean isLessThan;
	// 任务条件距离（单位：米）
	int discussDistance;
	// 任务开始启用时间
	long startTime;
	// 任务是否已触发
	boolean isBingo = false;
	// 是否启用任务
	boolean isEnable;

	// 带参构造（强制传入positionId，确保任务与位置绑定）
	public PositionTaskModel(String taskId, String positionId, String taskDescription, boolean isGreaterThan, int discussDistance, long startTime, boolean isEnable) {
		this.taskId = (taskId == null || taskId.trim().isEmpty()) ? genTaskId() : taskId; // 空ID自动生成
		this.positionId = positionId; // 强制绑定位置ID
		this.taskDescription = (taskDescription == null || taskDescription.trim().isEmpty()) ? "新任务" : taskDescription;
		this.isGreaterThan = isGreaterThan;
		this.isLessThan = !isGreaterThan; // 确保互斥
		this.discussDistance = Math.max(discussDistance, 1); // 距离最小1米，避免无效值
		this.startTime = startTime;
		this.isEnable = isEnable;
	}
	
	// 无参构造（初始化默认值，positionId需后续设置）
	public PositionTaskModel() {
		this.taskId = genTaskId();
		this.positionId = "";
		this.taskDescription = "新任务";
		this.isGreaterThan = true;
		this.isLessThan = false; // 初始互斥
		this.discussDistance = 100; // 默认100米
		this.startTime = System.currentTimeMillis();
		this.isEnable = true;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setIsBingo(boolean isBingo) {
		this.isBingo = isBingo;
	}

	public boolean isBingo() {
		return isBingo;
	}

	// ---------------------- Getter/Setter（确保positionId不为空，距离有效） ----------------------
	public void setTaskId(String taskId) {
		this.taskId = (taskId == null || taskId.trim().isEmpty()) ? genTaskId() : taskId;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setPositionId(String positionId) {
		this.positionId = (positionId == null || positionId.trim().isEmpty()) ? "" : positionId; // 空值防护
	}

	public String getPositionId() {
		return positionId;
	}

	public void setTaskDescription(String taskDescription) {
		this.taskDescription = (taskDescription == null || taskDescription.trim().isEmpty()) ? "新任务" : taskDescription;
	}

	public String getTaskDescription() {
		return taskDescription;
	}

	// 修复：确保isGreaterThan和isLessThan互斥
	public void setIsGreaterThan(boolean isGreaterThan) {
		this.isGreaterThan = isGreaterThan;
		this.isLessThan = !isGreaterThan; // 关键：小于 = 非大于
	}

	public boolean isGreaterThan() {
		return isGreaterThan;
	}

	// 修复：确保isLessThan和isGreaterThan互斥
	public void setIsLessThan(boolean isLessThan) {
		this.isLessThan = isLessThan;
		this.isGreaterThan = !isLessThan; // 关键：大于 = 非小于
	}

	public boolean isLessThan() {
		return isLessThan;
	}

	public void setDiscussDistance(int discussDistance) {
		this.discussDistance = Math.max(discussDistance, 1); // 距离最小1米，避免0或负数
	}

	public int getDiscussDistance() {
		return discussDistance;
	}

	public void setIsEnable(boolean isEnable) {
		this.isEnable = isEnable;
	}

	public boolean isEnable() {
		return isEnable;
	}

	// ---------------------- 父类方法重写 ----------------------
	@Override
	public String getName() {
		return PositionTaskModel.class.getName();
	}
	
	// 生成唯一任务ID（与PositionModel保持一致格式）
	public static String genTaskId() {
        UUID uniqueUuid = UUID.randomUUID();
        return uniqueUuid.toString(); // 36位标准UUID（含横杠，确保唯一）
    }

	// JSON序列化（保存任务数据，包含所有字段）
	@Override
	public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
		super.writeThisToJsonWriter(jsonWriter);
		jsonWriter.name("taskId").value(getTaskId());
		jsonWriter.name("positionId").value(getPositionId());
		jsonWriter.name("taskDescription").value(getTaskDescription());
		jsonWriter.name("isGreaterThan").value(isGreaterThan());
		jsonWriter.name("isLessThan").value(isLessThan());
		jsonWriter.name("discussDistance").value(getDiscussDistance());
		jsonWriter.name("startTime").value(getStartTime());
		jsonWriter.name("isEnable").value(isEnable());
	}

	// JSON反序列化（加载任务数据，校验字段有效性）
	@Override
	public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
		if (super.initObjectsFromJsonReader(jsonReader, name)) { 
			return true; 
		} else {
			if (name.equals("taskId")) {
				setTaskId(jsonReader.nextString());
			} else if (name.equals("positionId")) {
				setPositionId(jsonReader.nextString());
			} else if (name.equals("taskDescription")) {
				setTaskDescription(jsonReader.nextString());
			} else if (name.equals("isGreaterThan")) {
				setIsGreaterThan(jsonReader.nextBoolean());
			} else if (name.equals("isLessThan")) {
				setIsLessThan(jsonReader.nextBoolean());
			} else if (name.equals("discussDistance")) {
				setDiscussDistance(jsonReader.nextInt());
			} else if (name.equals("startTime")) {
				setStartTime(jsonReader.nextLong());
			} else if (name.equals("isEnable")) {
				setIsEnable(jsonReader.nextBoolean());
			} else {
				return false;
			}
		}
		return true;
	}

	// 从JSON读取任务数据（确保反序列化完整）
	@Override
	public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
		jsonReader.beginObject();
		while (jsonReader.hasNext()) {
			String name = jsonReader.nextName();
			if (!initObjectsFromJsonReader(jsonReader, name)) {
				jsonReader.skipValue(); // 跳过未知字段，避免崩溃
			}
		}
		jsonReader.endObject();
		return this;
	}
    
}

