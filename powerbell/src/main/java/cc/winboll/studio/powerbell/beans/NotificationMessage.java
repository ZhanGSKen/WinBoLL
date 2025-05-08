package cc.winboll.studio.powerbell.beans;

// 应用消息结构
//
public class NotificationMessage {
    
    String Title;
    String Content;
    String RemindMSG;

    public NotificationMessage(String title, String content) {
        Title = title;
        Content = content;
    }

    public void setRemindMSG(String remindMSG) {
        RemindMSG = remindMSG;
    }

    public String getRemindMSG() {
        return RemindMSG;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getTitle() {
        return Title;
    }

    public void setContent(String content) {
        Content = content;
    }

    public String getContent() {
        return Content;
    }
    
}
