package cc.winboll.studio.powerbell.models;

import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import cc.winboll.studio.libappbase.LogUtils;
import java.io.IOException;
import java.io.Serializable;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2024/07/18 11:52:28
 * @Describe 应用背景图片数据类
 * 适配 API30，支持 Serializable 持久化、JSON 序列化/反序列化
 * 存储正式/预览背景配置，包含原图、压缩图、裁剪比例、像素颜色等核心字段
 */
public class BackgroundBean extends BaseBean implements Serializable {
    // ====================== 静态常量（首屏可见，统一管理） ======================
    // 日志标签（全局统一，替换 Log 为 LogUtils）
    public static final String TAG = "BackgroundBean";
    // 兼容旧字段常量（统一管理，避免硬编码）
    private static final String OLD_FIELD_USE_SCALED_COMPRESS = "isUseScaledCompress";
    // 字段默认值常量（统一管理，避免魔法值）
    private static final int DEFAULT_DIMENSION = 100; // 默认宽高
    private static final int MIN_DIMENSION = 1;       // 最小宽高

    // ====================== 成员变量（按功能分类：原图配置→压缩图配置→控制字段→裁剪配置→像素颜色） ======================
    // 原图配置
    private String backgroundFileName = "";          // 背景图片文件名
    private String backgroundFilePath = "";          // 背景图片完整路径
    private String backgroundFileInfo = "";          // 图片信息（Uri、网络地址等）
    // 压缩图配置
    private String backgroundScaledCompressFileName = ""; // 压缩后背景图片文件名
    private String backgroundScaledCompressFilePath = ""; // 压缩后背景图片完整路径
    // 控制字段
    private boolean isUseBackgroundFile = false;             // 是否启用背景图片
    private boolean isUseBackgroundScaledCompressFile = false; // 是否启用压缩背景图（重命名：原isUseScaledCompress）
    // 裁剪配置
    private int backgroundWidth = DEFAULT_DIMENSION;  // 背景图宽度
    private int backgroundHeight = DEFAULT_DIMENSION; // 背景图高度
    // 像素颜色
    private int pixelColor = 0xFFFFFFFF;                       // 拾取的像素颜色（纯色背景用）

    // ====================== 构造方法（无参构造，JSON反序列化必备） ======================
    /**
     * 无参构造器（必须，JSON反序列化时需默认构造器）
     */
    public BackgroundBean() {
        LogUtils.d(TAG, "BackgroundBean: 无参构造初始化完成");
    }

    // ====================== Getter/Setter 方法（按功能分类，补充调试日志，强化校验） ======================
    // --------------- 原图配置相关 ---------------
    public String getBackgroundFileName() {
        return backgroundFileName;
    }

    public void setBackgroundFileName(String backgroundFileName) {
        this.backgroundFileName = backgroundFileName == null ? "" : backgroundFileName;
        LogUtils.d(TAG, String.format("setBackgroundFileName: 背景文件名设置为 %s", this.backgroundFileName));
    }

    public String getBackgroundFilePath() {
        return backgroundFilePath;
    }

    public void setBackgroundFilePath(String backgroundFilePath) {
        this.backgroundFilePath = backgroundFilePath == null ? "" : backgroundFilePath;
        LogUtils.d(TAG, String.format("setBackgroundFilePath: 背景文件路径设置为 %s", this.backgroundFilePath));
    }

    public String getBackgroundFileInfo() {
        return backgroundFileInfo;
    }

    public void setBackgroundFileInfo(String backgroundFileInfo) {
        this.backgroundFileInfo = backgroundFileInfo == null ? "" : backgroundFileInfo;
        LogUtils.d(TAG, String.format("setBackgroundFileInfo: 背景文件信息设置为 %s", this.backgroundFileInfo));
    }

    // --------------- 控制字段相关 ---------------
    public boolean isUseBackgroundFile() {
        return isUseBackgroundFile;
    }

    public void setIsUseBackgroundFile(boolean isUseBackgroundFile) {
        this.isUseBackgroundFile = isUseBackgroundFile;
        LogUtils.d(TAG, String.format("setIsUseBackgroundFile: 是否启用背景图设置为 %b", isUseBackgroundFile));
    }

    // --------------- 压缩图配置相关 ---------------
    public String getBackgroundScaledCompressFileName() {
        return backgroundScaledCompressFileName;
    }

    public void setBackgroundScaledCompressFileName(String backgroundScaledCompressFileName) {
        this.backgroundScaledCompressFileName = backgroundScaledCompressFileName == null ? "" : backgroundScaledCompressFileName;
        LogUtils.d(TAG, String.format("setBackgroundScaledCompressFileName: 压缩背景文件名设置为 %s", this.backgroundScaledCompressFileName));
    }

    public String getBackgroundScaledCompressFilePath() {
        return backgroundScaledCompressFilePath;
    }

    public void setBackgroundScaledCompressFilePath(String backgroundScaledCompressFilePath) {
        this.backgroundScaledCompressFilePath = backgroundScaledCompressFilePath == null ? "" : backgroundScaledCompressFilePath;
        LogUtils.d(TAG, String.format("setBackgroundScaledCompressFilePath: 压缩背景文件路径设置为 %s", this.backgroundScaledCompressFilePath));
    }

    /**
     * 重命名：原isUseScaledCompress → 新isUseBackgroundScaledCompressFile（Getter/Setter同步修改）
     * 语义：明确表示“是否启用背景压缩图文件”，避免与其他压缩逻辑混淆
     */
    public boolean isUseBackgroundScaledCompressFile() {
        return isUseBackgroundScaledCompressFile;
    }

    public void setIsUseBackgroundScaledCompressFile(boolean isUseBackgroundScaledCompressFile) {
        this.isUseBackgroundScaledCompressFile = isUseBackgroundScaledCompressFile;
        LogUtils.d(TAG, String.format("setIsUseBackgroundScaledCompressFile: 是否启用压缩背景图设置为 %b", isUseBackgroundScaledCompressFile));
    }

    // --------------- 裁剪配置相关 ---------------
    public int getBackgroundWidth() {
        return backgroundWidth;
    }

    public void setBackgroundWidth(int backgroundWidth) {
        this.backgroundWidth = backgroundWidth < MIN_DIMENSION ? DEFAULT_DIMENSION : backgroundWidth;
        LogUtils.d(TAG, String.format("setBackgroundWidth: 背景宽度设置为 %d（输入值：%d）", this.backgroundWidth, backgroundWidth));
    }

    public int getBackgroundHeight() {
        return backgroundHeight;
    }

    public void setBackgroundHeight(int backgroundHeight) {
        this.backgroundHeight = backgroundHeight < MIN_DIMENSION ? DEFAULT_DIMENSION : backgroundHeight;
        LogUtils.d(TAG, String.format("setBackgroundHeight: 背景高度设置为 %d（输入值：%d）", this.backgroundHeight, backgroundHeight));
    }

    // --------------- 像素颜色相关 ---------------
    public int getPixelColor() {
        return pixelColor;
    }

    public void setPixelColor(int pixelColor) {
        this.pixelColor = pixelColor;
        LogUtils.d(TAG, String.format("setPixelColor: 像素颜色设置为 0x%08X", pixelColor));
    }

    // ====================== 序列化/反序列化方法（适配重命名字段，兼容旧版本，补充调试日志） ======================
    @Override
    public String getName() {
        String className = BackgroundBean.class.getName();
        LogUtils.d(TAG, String.format("getName: 类名标识为 %s", className));
        return className;
    }

    /**
     * 序列化：同步重命名字段（原isUseScaledCompress → 新isUseBackgroundScaledCompressFile）
     * 确保新字段能正常持久化，同时兼容旧版本JSON（保留旧字段写入，避免旧版本读取异常）
     */
    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        BackgroundBean bean = this;
        // 原图配置序列化
        jsonWriter.name("backgroundFileName").value(bean.getBackgroundFileName());
        jsonWriter.name("backgroundFilePath").value(bean.getBackgroundFilePath());
        jsonWriter.name("backgroundFileInfo").value(bean.getBackgroundFileInfo());
        // 控制字段序列化
        jsonWriter.name("isUseBackgroundFile").value(bean.isUseBackgroundFile());
        // 压缩图配置序列化
        jsonWriter.name("backgroundScaledCompressFileName").value(bean.getBackgroundScaledCompressFileName());
        jsonWriter.name("backgroundScaledCompressFilePath").value(bean.getBackgroundScaledCompressFilePath());
        // 关键：新字段序列化（核心）
        jsonWriter.name("isUseBackgroundScaledCompressFile").value(bean.isUseBackgroundScaledCompressFile());
        // 兼容旧版本：保留旧字段名写入（避免旧版本Bean读取时缺失字段）
        jsonWriter.name(OLD_FIELD_USE_SCALED_COMPRESS).value(bean.isUseBackgroundScaledCompressFile());
        // 裁剪配置与像素颜色序列化
        jsonWriter.name("backgroundWidth").value(bean.getBackgroundWidth());
        jsonWriter.name("backgroundHeight").value(bean.getBackgroundHeight());
        jsonWriter.name("pixelColor").value(bean.getPixelColor());
        LogUtils.d(TAG, "writeThisToJsonWriter: JSON 序列化完成，已兼容旧字段");
    }

    /**
     * 反序列化：同步处理重命名字段（兼容旧版本JSON，新旧字段都能读取）
     * 逻辑：优先读取新字段，若新字段不存在则读取旧字段（确保升级后旧配置仍有效）
     */
    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        BackgroundBean bean = new BackgroundBean();
        jsonReader.beginObject();
        // 临时变量：存储旧字段值（用于兼容）
        boolean tempUseScaledCompress = false;
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case "backgroundFileName":
                    bean.setBackgroundFileName(jsonReader.nextString());
                    break;
                case "backgroundFilePath":
                    bean.setBackgroundFilePath(jsonReader.nextString());
                    break;
                case "backgroundFileInfo":
                    bean.setBackgroundFileInfo(jsonReader.nextString());
                    break;
                case "isUseBackgroundFile":
                    bean.setIsUseBackgroundFile(jsonReader.nextBoolean());
                    break;
                case "backgroundScaledCompressFileName":
                    bean.setBackgroundScaledCompressFileName(jsonReader.nextString());
                    break;
                case "backgroundScaledCompressFilePath":
                    bean.setBackgroundScaledCompressFilePath(jsonReader.nextString());
                    break;
                case "isUseBackgroundScaledCompressFile":
                    // 关键：读取新字段（优先）
                    bean.setIsUseBackgroundScaledCompressFile(jsonReader.nextBoolean());
                    LogUtils.d(TAG, "readBeanFromJsonReader: 读取新字段 isUseBackgroundScaledCompressFile 完成");
                    break;
                case OLD_FIELD_USE_SCALED_COMPRESS:
                    // 兼容旧版本：读取旧字段（若新字段未读取，则用旧字段值）
                    tempUseScaledCompress = jsonReader.nextBoolean();
                    LogUtils.d(TAG, "readBeanFromJsonReader: 读取旧字段 isUseScaledCompress 完成");
                    break;
                case "backgroundWidth":
                    bean.setBackgroundWidth(jsonReader.nextInt());
                    break;
                case "backgroundHeight":
                    bean.setBackgroundHeight(jsonReader.nextInt());
                    break;
                case "pixelColor":
                    bean.setPixelColor(jsonReader.nextInt());
                    break;
                default:
                    jsonReader.skipValue();
                    LogUtils.w(TAG, String.format("readBeanFromJsonReader: 跳过未知字段 %s", name));
                    break;
            }
        }
        jsonReader.endObject();
        // 兼容逻辑：若新字段未被赋值（旧版本JSON无此字段），则用旧字段值填充
        if (!bean.isUseBackgroundScaledCompressFile()) {
            bean.setIsUseBackgroundScaledCompressFile(tempUseScaledCompress);
            LogUtils.d(TAG, "readBeanFromJsonReader: 旧字段值已填充到新字段");
        }
        LogUtils.d(TAG, "readBeanFromJsonReader: JSON 反序列化完成");
        return bean;
    }

    // ====================== 辅助方法（重置配置、配置校验，补充调试日志） ======================
    /**
     * 重置背景配置（适配“取消背景”功能，同步重置重命名字段）
     */
    public void resetBackgroundConfig() {
        this.backgroundFileName = "";
        this.backgroundFilePath = "";
        this.backgroundScaledCompressFileName = "";
        this.backgroundScaledCompressFilePath = "";
        this.backgroundFileInfo = "";
        this.isUseBackgroundFile = false;
        this.isUseBackgroundScaledCompressFile = false;
        this.backgroundWidth = DEFAULT_DIMENSION;
        this.backgroundHeight = DEFAULT_DIMENSION;
        LogUtils.d(TAG, "resetBackgroundConfig: 背景配置已重置为默认值");
    }

    /**
     * 检查背景配置是否有效（适配BackgroundSettingsActivity的预览/保存校验）
     * 同步使用重命名字段判断压缩图是否启用
     * @return true-配置有效（可显示背景图），false-配置无效
     */
    public boolean isBackgroundConfigValid() {
        // 启用背景图时，需确保：原图路径/文件名 或 压缩图路径/文件名 非空
        if (!isUseBackgroundFile) {
            LogUtils.d(TAG, "isBackgroundConfigValid: 未启用背景图，配置无效");
            return false;
        }
        // 原图校验：路径非空 或 文件名非空
        boolean isOriginalValid = !backgroundFilePath.isEmpty() || !backgroundFileName.isEmpty();
        // 压缩图校验：启用压缩图时，路径/文件名需非空
        boolean isCompressValid = true;
        if (isUseBackgroundScaledCompressFile()) {
            isCompressValid = !backgroundScaledCompressFilePath.isEmpty() || !backgroundScaledCompressFileName.isEmpty();
        }
        // 逻辑：启用压缩图则需压缩图有效；不启用压缩图则需原图有效
        boolean isValid = isUseBackgroundScaledCompressFile() ? isCompressValid : isOriginalValid;
        LogUtils.d(TAG, String.format("isBackgroundConfigValid: 背景配置有效性为 %b（启用压缩图：%b，原图有效：%b，压缩图有效：%b）",
									  isValid, isUseBackgroundScaledCompressFile(), isOriginalValid, isCompressValid));
        return isValid;
    }
}

