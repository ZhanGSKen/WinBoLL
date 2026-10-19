package cc.winboll.studio.winboll.models;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/06/05 11:26
 */

public class ResponseData {
    
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";
    
    private String status;
    private String message;
    private UserInfoModel data;
    
    public ResponseData() {
        this.status = "";
        this.message = "";
        this.data = new UserInfoModel();
    }
    
    public ResponseData(String status, String message, UserInfoModel data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setData(UserInfoModel data) {
        this.data = data;
    }

    public UserInfoModel getData() {
        return data;
    }
}

