package cc.winboll.studio.mymessagemanager.beans;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/19 13:10:44
 * @Describe 联系人信息类
 */
public class PhoneBean {

    //联系人姓名
    private String mszName;
    //电话号码
    private String mszTelPhone;

    public String getName() {
        return mszName;
    }

    public void setName(String szName) {
        this.mszName = szName;
    }

    public String getTelPhone() {
        return mszTelPhone;
    }

    public void setTelPhone(String szTelPhone) {
        this.mszTelPhone = szTelPhone;
    }

    public PhoneBean() {
    }

    public PhoneBean(String szName, String szTelPhone) {
        this.mszName = szName;
        this.mszTelPhone = szTelPhone;
    }

}
