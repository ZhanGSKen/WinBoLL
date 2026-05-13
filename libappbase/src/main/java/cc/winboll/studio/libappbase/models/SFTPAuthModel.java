package cc.winboll.studio.libappbase.models;

/**
 * SFTP登录验证信息实体类
 * 封装SFTP登录所需的所有配置信息：服务端地址、端口、账号密码、秘钥信息、编码
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/30 19:08:00
 * @LastEditTime 2026/01/31 22:45:00
 */
public class SFTPAuthModel {
    public static final String TAG = "SFTPAuthModel";

    // SFTP服务器地址（必填，如192.168.1.100、sftp.xxx.com）
    private String ftpServer;
    // SFTP服务器端口（必填，默认22）
    private int ftpPort = 22;
    // SFTP登录用户名（匿名登录传null/空）
    private String ftpUsername;
    // SFTP登录密码（匿名登录传null/空）
    private String ftpPassword;
    // SFTP登录秘钥路径（秘钥登录时使用，本地绝对路径，如/sdcard/sftp/key.pem，账号密码登录传null/空）
    private String ftpKeyPath;
    // SFTP登录秘钥密码（秘钥有密码时填写，无密码传null/空）
    private String ftpKeyPwd;
    // SFTP编码（默认UTF-8，解决中文文件名乱码）
    private String ftpCharset = "UTF-8";

    // 空参构造（JavaBean规范）
    public SFTPAuthModel() {
    }

    // 全参构造（快速初始化）
    public SFTPAuthModel(String ftpServer, int ftpPort, String ftpUsername, String ftpPassword,
						 String ftpKeyPath, String ftpKeyPwd, String ftpCharset) {
        this.ftpServer = ftpServer;
        this.ftpPort = ftpPort;
        this.ftpUsername = ftpUsername;
        this.ftpPassword = ftpPassword;
        this.ftpKeyPath = ftpKeyPath;
        this.ftpKeyPwd = ftpKeyPwd;
        this.ftpCharset = ftpCharset;
    }

    // ==================== Get/Set 方法 ====================
    public String getFtpServer() {
        return ftpServer;
    }

    public void setFtpServer(String ftpServer) {
        this.ftpServer = ftpServer;
    }

    public int getFtpPort() {
        return ftpPort;
    }

    public void setFtpPort(int ftpPort) {
        this.ftpPort = ftpPort;
    }

    public String getFtpUsername() {
        return ftpUsername;
    }

    public void setFtpUsername(String ftpUsername) {
        this.ftpUsername = ftpUsername;
    }

    public String getFtpPassword() {
        return ftpPassword;
    }

    public void setFtpPassword(String ftpPassword) {
        this.ftpPassword = ftpPassword;
    }

    public String getFtpKeyPath() {
        return ftpKeyPath;
    }

    public void setFtpKeyPath(String ftpKeyPath) {
        this.ftpKeyPath = ftpKeyPath;
    }

    public String getFtpKeyPwd() {
        return ftpKeyPwd;
    }

    public void setFtpKeyPwd(String ftpKeyPwd) {
        this.ftpKeyPwd = ftpKeyPwd;
    }

    public String getFtpCharset() {
        return ftpCharset;
    }

    public void setFtpCharset(String ftpCharset) {
        this.ftpCharset = ftpCharset;
    }
}

