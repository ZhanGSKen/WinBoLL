package cc.winboll.studio.winboll.models;

import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.models.libs1520000.BaseBean;
import java.io.IOException;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/04/30 10:47
 */
public class TermuxButtonModel extends BaseBean {

    public static final String TAG = "TermuxButtonModel";

    String buttonName;
    String exeCommand;
    String workDir;
    String iconPath;

    // 已修改：isCommit 改为规范过去式命名 isCommitted
    boolean isCommitted;
    String commitTitle;
    String commitInfo;

    public TermuxButtonModel() {
        this.buttonName = "";
        this.exeCommand = "";
        this.workDir = "";
        this.iconPath = "";
        // 默认初始化
        this.isCommitted = false;
        this.commitTitle = "";
        this.commitInfo = "";
    }

    public void setButtonName(String buttonName) {
        this.buttonName = buttonName;
    }

    public String getButtonName() {
        return buttonName;
    }

    public void setExeCommand(String exeCommand) {
        this.exeCommand = exeCommand;
    }

    public String getExeCommand() {
        return exeCommand;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public String getWorkDir() {
        return workDir;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getIconPath() {
        return iconPath;
    }

    // ========== 已修改 对应 isCommitted 完整 Get & Set ==========
    public boolean isCommitted() {
        return isCommitted;
    }

    public void setCommitted(boolean committed) {
        isCommitted = committed;
    }

    public String getCommitTitle() {
        return commitTitle;
    }

    public void setCommitTitle(String commitTitle) {
        this.commitTitle = commitTitle;
    }

    public String getCommitInfo() {
        return commitInfo;
    }

    public void setCommitInfo(String commitInfo) {
        this.commitInfo = commitInfo;
    }

    @Override
    public String getName() {
        return TermuxButtonModel.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        jsonWriter.name("buttonName").value(getButtonName());
        jsonWriter.name("exeCommand").value(getExeCommand());
        jsonWriter.name("workDir").value(getWorkDir());
        jsonWriter.name("iconPath").value(getIconPath() != null ? getIconPath() : "");

        // JSON写入同步修改
        jsonWriter.name("isCommitted").value(isCommitted());
        jsonWriter.name("commitTitle").value(getCommitTitle());
        jsonWriter.name("commitInfo").value(getCommitInfo());
    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) {
            return true;
        } else {
            if (name.equals("buttonName")) {
                setButtonName(jsonReader.nextString());
            } else if (name.equals("exeCommand")) {
                setExeCommand(jsonReader.nextString());
            } else if (name.equals("workDir")) {
                setWorkDir(jsonReader.nextString());
            } else if (name.equals("iconPath")) {
                setIconPath(jsonReader.nextString());
            }
            // JSON解析字段同步修改
            else if (name.equals("isCommitted")) {
                setCommitted(jsonReader.nextBoolean());
            } else if (name.equals("commitTitle")) {
                setCommitTitle(jsonReader.nextString());
            } else if (name.equals("commitInfo")) {
                setCommitInfo(jsonReader.nextString());
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
        jsonReader.endObject();
        return this;
    }

}

