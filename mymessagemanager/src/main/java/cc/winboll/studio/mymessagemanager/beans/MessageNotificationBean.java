package cc.winboll.studio.mymessagemanager.beans;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/07/19 13:10:44
 * @Describe 短信通知栏消息结构
 */
public class MessageNotificationBean {

    private int messageId;
    private String mszPhone;
    private String mszTitle;
    private String mszContent;

    public MessageNotificationBean(int messageId, String mszPhone, String mszTitle, String mszContent) {
        this.messageId = messageId;
        this.mszPhone = mszPhone;
        this.mszTitle = mszTitle;
        this.mszContent = mszContent;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setPhone(String szPhone) {
        this.mszPhone = szPhone;
    }

    public String getPhone() {
        return mszPhone;
    }

    public void setTitle(String szTitle) {
        this.mszTitle = szTitle;
    }

    public String getTitle() {
        return mszTitle;
    }

    public void setContent(String szContent) {
        this.mszContent = szContent;
    }

    public String getContent() {
        return mszContent;
    }
}
