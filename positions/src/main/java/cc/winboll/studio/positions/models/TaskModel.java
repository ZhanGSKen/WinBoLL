package cc.winboll.studio.positions.models;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/04/07 13:30:41
 * @Describe 提醒任务单一任务模型
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;
import android.location.Location;
import java.util.UUID;

public class TaskModel extends BaseBean {

    public static final String TAG = "TaskModel";

    enum AROUND_CONDITIONAL { WITHIN, BEYOND };

    // UUID 唯一任务标识
    private String uuid;
    // 距离任务中心点的方圆半径(米)
    private int aroundMeters;
    // 方圆半径区域选择条件
    private AROUND_CONDITIONAL aroundConditional;
    // 任务生效时间戳
    private long taskEnableTimestamp;
    // 任务过期时间戳
    private long taskDisableTimestamp;
    // 任务描述
    private String comments;
    // 是否启用
    private boolean isEnable;
    // 在UI列表中是否显示简单视图
    private boolean isSimpleView;

    public TaskModel() {
        this.uuid = UUID.randomUUID().toString();
        this.aroundMeters = 0;
        this.aroundConditional = AROUND_CONDITIONAL.WITHIN;
        this.taskEnableTimestamp = System.currentTimeMillis();
        this.taskDisableTimestamp = System.currentTimeMillis();
        this.comments = "";
        this.isEnable = false;
        this.isSimpleView = true;
    }

    public TaskModel(String uuid, int aroundMeters, AROUND_CONDITIONAL aroundConditional, long taskEnableTimestamp, long taskDisableTimestamp, String comments, boolean isEnable, boolean isSimpleView) {
        this.uuid = uuid;
        this.aroundMeters = aroundMeters;
        this.aroundConditional = aroundConditional;
        this.taskEnableTimestamp = taskEnableTimestamp;
        this.taskDisableTimestamp = taskDisableTimestamp;
        this.comments = comments;
        this.isEnable = isEnable;
        this.isSimpleView = isSimpleView;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setAroundMeters(int aroundMeters) {
        this.aroundMeters = aroundMeters;
    }

    public int getAroundMeters() {
        return aroundMeters;
    }

    public void setAroundConditional(AROUND_CONDITIONAL aroundConditional) {
        this.aroundConditional = aroundConditional;
    }

    public AROUND_CONDITIONAL getAroundConditional() {
        return aroundConditional;
    }

    public void setTaskEnableTimestamp(long taskEnableTimestamp) {
        this.taskEnableTimestamp = taskEnableTimestamp;
    }

    public long getTaskEnableTimestamp() {
        return taskEnableTimestamp;
    }

    public void setTaskDisableTimestamp(long taskDisableTimestamp) {
        this.taskDisableTimestamp = taskDisableTimestamp;
    }

    public long getTaskDisableTimestamp() {
        return taskDisableTimestamp;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getComments() {
        return comments;
    }

    public void setIsEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setIsSimpleView(boolean isSimpleView) {
        this.isSimpleView = isSimpleView;
    }

    public boolean isSimpleView() {
        return isSimpleView;
    }

    @Override
    public String getName() {
        return TaskModel.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        jsonWriter.name("uuid").value(getUuid());
        jsonWriter.name("aroundMeters").value(getUuid());
        jsonWriter.name("aroundConditional").value(getComments());
        jsonWriter.name("taskEnableTimestamp").value(isEnable());
        jsonWriter.name("taskDisableTimestamp").value(isSimpleView());
        jsonWriter.name("comments").value(getComments());
        jsonWriter.name("isEnable").value(isEnable());
        jsonWriter.name("isSimpleView").value(isSimpleView());
    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
            if (name.equals("uuid")) {
                setUuid(jsonReader.nextString());
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
}
