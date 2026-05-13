package cc.winboll.studio.powerbell.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonWriter;
import java.io.IOException;
import java.io.Serializable;
import cc.winboll.studio.libappbase.BaseBean;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/12/17 15:55
 * @Describe 服务控制参数模型
 * 适配 API30，管理服务启用状态，支持 Serializable 持久化、Parcelable 组件传递、JSON 序列化解析
 */
public class ControlCenterServiceBean extends BaseBean implements Parcelable, Serializable {
    // ====================== 静态常量（置顶统一管理，避免魔法值） ======================
    //private static final long serialVersionUID = 1L; // Serializable 必备，保障反序列化兼容
    private static final String TAG = "ControlCenterServiceBean";
    private static final String JSON_FIELD_IS_ENABLE_SERVICE = "isEnableService"; // JSON 字段常量，避免硬编码

    // ====================== 核心成员变量（私有封装，规范命名） ======================
    private boolean isEnableService = false; // 服务启用状态：true=启用，false=禁用

    // ====================== Parcelable 静态创建器（必须 public static final，适配 API30 组件传递） ======================
    public static final Parcelable.Creator<ControlCenterServiceBean> CREATOR = new Parcelable.Creator<ControlCenterServiceBean>() {
        @Override
        public ControlCenterServiceBean createFromParcel(Parcel source) {
            boolean isEnable = source.readByte() != 0;
            ControlCenterServiceBean bean = new ControlCenterServiceBean(isEnable);
            LogUtils.d(TAG, String.format("createFromParcel: 反序列化完成，isEnableService=%b", isEnable));
            return bean;
        }

        @Override
        public ControlCenterServiceBean[] newArray(int size) {
            LogUtils.d(TAG, String.format("newArray: 创建数组，长度=%d", size));
            return new ControlCenterServiceBean[size];
        }
    };

    // ====================== 构造方法（无参+有参，满足不同初始化场景） ======================
    /**
     * 无参构造（JSON解析、反射创建必备）
     */
    public ControlCenterServiceBean() {
        this.isEnableService = false;
        LogUtils.d(TAG, "无参构造：初始化服务状态为禁用（false）");
    }

    /**
     * 有参构造（指定服务启用状态）
     * @param isEnableService 服务启用状态
     */
    public ControlCenterServiceBean(boolean isEnableService) {
        this.isEnableService = isEnableService;
        LogUtils.d(TAG, String.format("有参构造：初始化服务状态，isEnableService=%b", isEnableService));
    }

    // ====================== Getter/Setter 方法（封装成员变量，控制访问） ======================
    public boolean isEnableService() {
        LogUtils.d(TAG, String.format("isEnableService: 当前状态=%b", isEnableService));
        return isEnableService;
    }

    public void setIsEnableService(boolean isEnableService) {
        LogUtils.d(TAG, String.format("setIsEnableService: 旧状态=%b，新状态=%b", this.isEnableService, isEnableService));
        this.isEnableService = isEnableService;
    }

    // ====================== 父类 BaseBean 方法重写（核心业务逻辑：JSON 序列化/反序列化） ======================
    @Override
    public String getName() {
        String className = ControlCenterServiceBean.class.getName();
        LogUtils.d(TAG, String.format("getName: 返回类名=%s", className));
        return className;
    }

    /**
     * 序列化对象到 JSON（适配数据持久化/网络传输）
     */
    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        jsonWriter.name(JSON_FIELD_IS_ENABLE_SERVICE).value(this.isEnableService);
        LogUtils.d(TAG, String.format("writeThisToJsonWriter: 序列化完成，%s=%b", JSON_FIELD_IS_ENABLE_SERVICE, this.isEnableService));
    }

    /**
     * 从 JSON 反序列化创建对象（适配数据恢复）
     */
    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        ControlCenterServiceBean bean = new ControlCenterServiceBean();
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String fieldName = jsonReader.nextName();
            if (JSON_FIELD_IS_ENABLE_SERVICE.equals(fieldName)) {
                boolean isEnable = jsonReader.nextBoolean();
                bean.setIsEnableService(isEnable);
                LogUtils.d(TAG, String.format("readBeanFromJsonReader: 读取字段，%s=%b", fieldName, isEnable));
            } else {
                jsonReader.skipValue();
                LogUtils.w(TAG, String.format("readBeanFromJsonReader: 跳过未知字段=%s", fieldName));
            }
        }
        jsonReader.endObject();
        LogUtils.d(TAG, "readBeanFromJsonReader: 反序列化完成");
        return bean;
    }

    // ====================== Parcelable 接口方法实现（适配 Intent 组件间传递，Java7 适配） ======================
    @Override
    public int describeContents() {
        LogUtils.d(TAG, "describeContents: 返回内容描述符=0");
        return 0; // 无特殊内容（如文件描述符），返回0即可（API30 标准实现）
    }

    /**
     * 序列化对象到 Parcel（Intent 传递必备，Java7 适配：用 byte 存储 boolean）
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        byte flag = (byte) (this.isEnableService ? 1 : 0);
        dest.writeByte(flag);
        LogUtils.d(TAG, String.format("writeToParcel: 序列化完成，isEnableService=%b（存储为byte=%d）", this.isEnableService, flag));
    }
}

