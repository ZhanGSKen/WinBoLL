package cc.winboll.studio.contacts.beans;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/26 13:10:57
 * @Describe CallLogModel
 */

import java.util.Date;

public class CallLogModel {
    public static final String TAG = "CallLogModel";

    private String phoneNumber;
    private String callStatus;
    private Date callDate;

    public CallLogModel(String phoneNumber, String callStatus, Date callDate) {
        this.phoneNumber = phoneNumber;
        this.callStatus = callStatus;
        this.callDate = callDate;
    }

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

