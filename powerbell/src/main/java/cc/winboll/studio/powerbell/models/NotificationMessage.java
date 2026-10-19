package cc.winboll.studio.powerbell.models;

import cc.winboll.studio.libappbase.LogUtils;

/**
 * 通知数据模型
 * 适配 API30，统一存储通知标题、内容、标识信息，支持各组件数据传递
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Describe 通知数据模型：统一存储通知标题、内容等信息，适配各组件数据传递
 */
public class NotificationMessage {
    // ====================== 静态常量（统一管理） ======================
    private static final String TAG = "NotificationMessage";
    private static final String EMPTY_STRING = "";

    // ====================== 核心成员变量（按业务逻辑排序） ======================
    private String title;       // 通知标题
    private String content;     // 通知内容
    private String remindMSG;   // 通知标识（区分服务运行/充电/耗电）

    // ====================== 构造方法（无参+全参，满足不同初始化场景） ======================
    /**
     * 无参构造器（反射实例化、JSON反序列化必备）
     */
    public NotificationMessage() {
        this.title = EMPTY_STRING;
        this.content = EMPTY_STRING;
        this.remindMSG = EMPTY_STRING;
        LogUtils.d(TAG, "无参构造：初始化通知数据模型，默认值为空字符串");
    }

    /**
     * 全参构造器（直接传参创建实例，简化调用）
     * @param title 通知标题
     * @param content 通知内容
     * @param remindMSG 通知标识
     */
    public NotificationMessage(String title, String content, String remindMSG) {
        this.title = title == null ? EMPTY_STRING : title;
        this.content = content == null ? EMPTY_STRING : content;
        this.remindMSG = remindMSG == null ? EMPTY_STRING : remindMSG;
        LogUtils.d(TAG, String.format("全参构造：初始化完成 | 标题：%s | 内容：%s | 标识：%s",
									  this.title, this.content, this.remindMSG));
    }

    // ====================== Setter 方法（补充空值防护与调试日志） ======================
    public void setTitle(String title) {
        this.title = title == null ? EMPTY_STRING : title;
        LogUtils.d(TAG, String.format("setTitle：通知标题设置为「%s」", this.title));
    }

    public void setContent(String content) {
        this.content = content == null ? EMPTY_STRING : content;
        LogUtils.d(TAG, String.format("setContent：通知内容设置为「%s」", this.content));
    }

    public void setRemindMSG(String remindMSG) {
        this.remindMSG = remindMSG == null ? EMPTY_STRING : remindMSG;
        LogUtils.d(TAG, String.format("setRemindMSG：通知标识设置为「%s」", this.remindMSG));
    }

    // ====================== Getter 方法（按成员变量顺序排列） ======================
    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getRemindMSG() {
        return remindMSG;
    }
}

