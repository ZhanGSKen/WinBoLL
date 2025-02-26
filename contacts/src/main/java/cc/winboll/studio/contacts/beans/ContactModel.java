package cc.winboll.studio.contacts.beans;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/26 13:37:00
 * @Describe ContactModel
 */
public class ContactModel {

    public static final String TAG = "ContactModel";

    private String name;
    private String number;

    public ContactModel(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }
}

