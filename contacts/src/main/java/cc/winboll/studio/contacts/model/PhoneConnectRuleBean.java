package cc.winboll.studio.contacts.model;

import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import cc.winboll.studio.libappbase.LogUtils;
import java.io.IOException;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/02/21 09:52:10
 * @Describe 电话黑名单规则实体类，支持JSON序列化与反序列化
 */
public class PhoneConnectRuleBean extends BaseBean {
    // ====================== 常量定义区 ======================
    public static final String TAG = "PhoneConnectRuleModel";
    // JSON字段名常量，避免硬编码错误
    private static final String JSON_KEY_RULE_TEXT = "ruleText";
    private static final String JSON_KEY_ALLOW_CONNECTION = "isAllowConnection";
    private static final String JSON_KEY_IS_ENABLE = "isEnable";

    // ====================== 成员变量区 ======================
    private String ruleText;
    private boolean isAllowConnection;
    private boolean isEnable;
    private boolean isSimpleView;

    // ====================== 构造函数区 ======================
    /**
     * 默认构造，初始化默认值
     */
    public PhoneConnectRuleBean() {
        this.ruleText = "";
        this.isAllowConnection = false;
        this.isEnable = false;
        this.isSimpleView = true;
        LogUtils.d(TAG, "PhoneConnectRuleModel: 默认构造初始化完成 | 规则文本空串，默认禁用状态");
    }

    /**
     * 带参构造，初始化核心规则参数
     */
    public PhoneConnectRuleBean(String ruleText, boolean isAllowConnection, boolean isEnable) {
        this.ruleText = ruleText == null ? "" : ruleText;
        this.isAllowConnection = isAllowConnection;
        this.isEnable = isEnable;
        this.isSimpleView = true;
        LogUtils.d(TAG, "PhoneConnectRuleModel: 带参构造初始化完成 | 规则文本=" + this.ruleText
				   + " | 允许连接=" + this.isAllowConnection + " | 规则启用=" + this.isEnable);
    }

    // ====================== Getter & Setter 方法区 ======================
    public String getRuleText() {
        return ruleText;
    }

    public void setRuleText(String ruleText) {
        String oldValue = this.ruleText;
        this.ruleText = ruleText == null ? "" : ruleText;
        LogUtils.d(TAG, "setRuleText: 规则文本更新 | 旧值=" + oldValue + " | 新值=" + this.ruleText);
    }

    public boolean isAllowConnection() {
        return isAllowConnection;
    }

    public void setIsAllowConnection(boolean isAllowConnection) {
        LogUtils.d(TAG, "setIsAllowConnection: 允许连接状态更新为" + isAllowConnection);
        this.isAllowConnection = isAllowConnection;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setIsEnable(boolean isEnable) {
        LogUtils.d(TAG, "setIsEnable: 规则启用状态更新为" + isEnable);
        this.isEnable = isEnable;
    }

    public boolean isSimpleView() {
        return isSimpleView;
    }

    public void setIsSimpleView(boolean isSimpleView) {
        LogUtils.d(TAG, "setIsSimpleView: 视图模式更新 | 简洁模式=" + isSimpleView);
        this.isSimpleView = isSimpleView;
    }

    // ====================== 重写 BaseBean 抽象方法区 ======================
    @Override
    public String getName() {
        String className = PhoneConnectRuleBean.class.getName();
        LogUtils.v(TAG, "getName: 获取当前类名=" + className);
        return className;
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        LogUtils.d(TAG, "writeThisToJsonWriter: 开始JSON序列化规则数据");
        super.writeThisToJsonWriter(jsonWriter);
        // 序列化核心字段
        jsonWriter.name(JSON_KEY_RULE_TEXT).value(getRuleText());
        jsonWriter.name(JSON_KEY_ALLOW_CONNECTION).value(isAllowConnection());
        jsonWriter.name(JSON_KEY_IS_ENABLE).value(isEnable());
        LogUtils.d(TAG, "writeThisToJsonWriter: JSON序列化完成");
    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        // 优先让父类处理通用字段
        if (super.initObjectsFromJsonReader(jsonReader, name)) {
            LogUtils.v(TAG, "initObjectsFromJsonReader: 父类已处理字段=" + name);
            return true;
        }

        // 处理当前类专属字段
        if (JSON_KEY_RULE_TEXT.equals(name)) {
            setRuleText(jsonReader.nextString());
        } else if (JSON_KEY_ALLOW_CONNECTION.equals(name)) {
            setIsAllowConnection(jsonReader.nextBoolean());
        } else if (JSON_KEY_IS_ENABLE.equals(name)) {
            setIsEnable(jsonReader.nextBoolean());
        } else {
            LogUtils.w(TAG, "initObjectsFromJsonReader: 未识别的JSON字段=" + name);
            return false;
        }
        LogUtils.v(TAG, "initObjectsFromJsonReader: 成功解析字段=" + name);
        return true;
    }

    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        LogUtils.d(TAG, "readBeanFromJsonReader: 开始从JSON解析规则数据");
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String fieldName = jsonReader.nextName();
            if (!initObjectsFromJsonReader(jsonReader, fieldName)) {
                LogUtils.w(TAG, "readBeanFromJsonReader: 跳过未识别字段=" + fieldName);
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        LogUtils.d(TAG, "readBeanFromJsonReader: JSON解析完成 | 解析后规则=" + getRuleText());
        return this;
    }
}

