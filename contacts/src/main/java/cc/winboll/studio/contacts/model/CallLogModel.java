package cc.winboll.studio.contacts.model;

import cc.winboll.studio.libappbase.LogUtils;
import java.util.Date;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/02/26 13:10:57
 * @Describe 通话记录数据模型
 */
public class CallLogModel {
    // ====================== 常量定义区 ======================
    public static final String TAG = "CallLogModel";

    // ====================== 成员变量区 ======================
    private String phoneNumber;
    private String callStatus;
    private Date callDate;

    // ====================== 构造函数区 ======================
    public CallLogModel(String phoneNumber, String callStatus, Date callDate) {
        // 去除号码中的空格并初始化
        this.phoneNumber = phoneNumber.replaceAll("\\s", "");
        this.callStatus = callStatus;
        this.callDate = callDate;

        LogUtils.d(TAG, "CallLogModel: 初始化通话记录模型 | 号码=" + this.phoneNumber 
				   + " | 状态=" + this.callStatus + " | 时间=" + this.callDate);
    }

    // ====================== Getter 方法区 ======================
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCallStatus() {
        return callStatus;
    }

    public Date getCallDate() {
        return callDate;
    }
}
