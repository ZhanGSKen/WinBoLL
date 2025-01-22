package cc.winboll.studio.libapputils.bean;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/20 14:19:02
 * @Describe 应用信息类
 */
import cc.winboll.studio.libapputils.R;
import java.io.Serializable;

public class APPInfo implements Serializable {

    public static final String TAG = "APPInfo";

    // 应用名称
    String appName;
    // 应用图标
    int appIcon;
    // 应用描述
    String appDescription;
    // 应用Git仓库地址
    String appGitName;
    // 应用Git仓库拥有者
    String appGitOwner;
    // 应用Git仓库分支
    String appGitAPPBranch;
    // 应用Git仓库子项目文件夹
    String appGitAPPSubProjectFolder;
    // 应用主页
    String appHomePage;
    // 应用包名称
    String appAPKName;
    // 应用包存储文件夹名称
    String appAPKFolderName;

    public APPInfo(String appName, int appIcon, String appDescription, String appGitName, String appGitOwner, String appGitAPPBranch, String appGitAPPSubProjectFolder, String appHomePage, String appAPKName, String appAPKFolderName) {
        this.appName = appName;
        this.appIcon = appIcon;
        this.appDescription = appDescription;
        this.appGitName = appGitName;
        this.appGitOwner = appGitOwner;
        this.appGitAPPBranch = appGitAPPBranch;
        this.appGitAPPSubProjectFolder = appGitAPPSubProjectFolder;
        this.appHomePage = appHomePage;
        this.appAPKName = appAPKName;
        this.appAPKFolderName = appAPKFolderName;
    }

    public APPInfo() {
        this.appName = "WinBoll-APP";
        this.appIcon = R.drawable.ic_launcher;
        this.appDescription = "WinBoll APP";
        this.appGitName = "APP";
        this.appGitOwner = "Studio";
        this.appGitAPPBranch = "app";
        this.appGitAPPSubProjectFolder = "app";
        this.appHomePage = "https://www.winboll.cc/studio/details.php?app=APP";
        this.appAPKName = "APP";
        this.appAPKFolderName = "APP";
    }

    public void setAppGitOwner(String appGitOwner) {
        this.appGitOwner = appGitOwner;
    }

    public String getAppGitOwner() {
        return appGitOwner;
    }

    public void setAppGitAPPBranch(String appGitAPPBranch) {
        this.appGitAPPBranch = appGitAPPBranch;
    }

    public String getAppGitAPPBranch() {
        return appGitAPPBranch;
    }

    public void setAppGitAPPSubProjectFolder(String appGitAPPSubProjectFolder) {
        this.appGitAPPSubProjectFolder = appGitAPPSubProjectFolder;
    }

    public String getAppGitAPPSubProjectFolder() {
        return appGitAPPSubProjectFolder;
    }

    public void setAppIcon(int appIcon) {
        this.appIcon = appIcon;
    }

    public int getAppIcon() {
        return appIcon;
    }

    public void setAppDescription(String appDescription) {
        this.appDescription = appDescription;
    }

    public String getAppDescription() {
        return appDescription;
    }

    public void setAppAPKFolderName(String appAPKFolderName) {
        this.appAPKFolderName = appAPKFolderName;
    }

    public String getAppAPKFolderName() {
        return appAPKFolderName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppGitName(String appGitName) {
        this.appGitName = appGitName;
    }

    public String getAppGitName() {
        return appGitName;
    }

    public void setAppHomePage(String appHomePage) {
        this.appHomePage = appHomePage;
    }

    public String getAppHomePage() {
        return appHomePage;
    }

    public void setAppAPKName(String appAPKName) {
        this.appAPKName = appAPKName;
    }

    public String getAppAPKName() {
        return appAPKName;
    }
}

