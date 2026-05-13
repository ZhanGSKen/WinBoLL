package cc.winboll.studio.mymessagemanager.beans;

public class ContractsBean {

    private String mszName;
    private String mszTelPhone;

    public ContractsBean(String szName, String szTelPhone) {
        this.mszName = szName;
        this.mszTelPhone = szTelPhone;
    }

    public void setName(String szName) {
        this.mszName = szName;
    }

    public String getName() {
        return mszName;
    }

    public void setTelPhone(String szTelPhone) {
        this.mszTelPhone = szTelPhone;
    }

    public String getTelPhone() {
        return mszTelPhone;
    }

}
