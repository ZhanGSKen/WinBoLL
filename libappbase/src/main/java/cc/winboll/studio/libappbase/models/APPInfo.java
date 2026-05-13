package cc.winboll.studio.libappbase.models;

import cc.winboll.studio.libappbase.R;
import cc.winboll.studio.libappbase.LogUtils;
import java.io.Serializable;

/**
 * @Describe 应用信息实体类，存储应用核心配置信息，实现序列化接口
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/11 12:29:00
 * @LastEditTime 2026/01/12 00:15:00
 */
public class APPInfo implements Serializable {
    // 常量定义区
    public static final String TAG = "APPInfo";

    // 成员属性区（按功能归类排序，统一私有访问权限，Java7 兼容，注释清晰）
    private String appName;             // 应用名称
    private int appIcon;                // 应用图标资源ID
    private String appDescription;      // 应用描述文案
    private String appGitName;          // 应用Git仓库名称
    private String appGitOwner;         // 应用Git仓库拥有者账号
    private String appGitAPPBranch;     // 应用Git仓库对应分支
    private String appGitAPPSubProjectFolder;  // 应用Git仓库内子项目文件夹路径
    private String appHomePage;         // 应用官方主页地址
    private String appAPKName;          // 应用安装包名称
    private String appAPKFolderName;    // 应用安装包存储文件夹名称
    private boolean isAddDebugTools;    // 是否启用应用调试工具功能

    // 构造方法区（按 参数从少到多 排序，逻辑清晰，便于调用选型）
    /**
     * 无参构造方法，默认初始化WinBoLL应用基础配置
     */
    public APPInfo() {
        LogUtils.d(TAG, "APPInfo() 无参构造方法调用，执行默认配置初始化");
        String szBranchName = "winboll";
        this.appName = "WinBoLL";
        this.appIcon = R.drawable.ic_winboll;
        this.appDescription = "Hello, WinBoLl!";
        this.appGitName = "WinBoLL";
        this.appGitOwner = "Studio";
        this.appGitAPPBranch = szBranchName;
        this.appGitAPPSubProjectFolder = szBranchName;
        this.appHomePage = "https://www.winboll.cc/apks/index.php?project=WinBoLL";
        this.appAPKName = "WinBoLL";
        this.appAPKFolderName = "WinBoLL";
        this.isAddDebugTools = false;
        LogUtils.d(TAG, "APPInfo() 无参构造初始化完成，默认应用名称：" + this.appName);
    }

    /**
     * 多参构造方法（不含调试工具配置，默认关闭调试功能）
     * @param appName 应用名称
     * @param appIcon 应用图标资源ID
     * @param appDescription 应用描述
     * @param appGitName Git仓库名称
     * @param appGitOwner Git仓库拥有者
     * @param appGitAPPBranch Git仓库分支
     * @param appGitAPPSubProjectFolder Git子项目文件夹
     * @param appHomePage 应用主页
     * @param appAPKName 应用包名
     * @param appAPKFolderName 应用包存储文件夹名
     */
    public APPInfo(String appName, int appIcon, String appDescription, String appGitName, String appGitOwner, 
                   String appGitAPPBranch, String appGitAPPSubProjectFolder, String appHomePage, 
                   String appAPKName, String appAPKFolderName) {
        LogUtils.d(TAG, "APPInfo(多参无调试) 构造调用，入参应用名：" + appName + " | Git仓库名：" + appGitName);
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
        this.isAddDebugTools = false;
        LogUtils.d(TAG, "APPInfo(多参无调试) 构造初始化完成");
    }

    /**
     * 全参构造方法（包含调试工具配置，支持自定义调试开关）
     * @param appName 应用名称
     * @param appIcon 应用图标资源ID
     * @param appDescription 应用描述
     * @param appGitName Git仓库名称
     * @param appGitOwner Git仓库拥有者
     * @param appGitAPPBranch Git仓库分支
     * @param appGitAPPSubProjectFolder Git子项目文件夹
     * @param appHomePage 应用主页
     * @param appAPKName 应用包名
     * @param appAPKFolderName 应用包存储文件夹名
     * @param isAddDebugTools 是否开启调试工具
     */
    public APPInfo(String appName, int appIcon, String appDescription, String appGitName, String appGitOwner, 
                   String appGitAPPBranch, String appGitAPPSubProjectFolder, String appHomePage, 
                   String appAPKName, String appAPKFolderName, boolean isAddDebugTools) {
        LogUtils.d(TAG, "APPInfo(全参带调试) 构造调用，入参应用名：" + appName + " | 调试开关：" + isAddDebugTools);
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
        this.isAddDebugTools = isAddDebugTools;
        LogUtils.d(TAG, "APPInfo(全参带调试) 构造初始化完成");
    }

    // Getter/Setter 方法区（严格跟随成员属性定义顺序，易查找维护，仅Setter加调试日志）
    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        LogUtils.d(TAG, "setAppName() 调用，传入应用名称：" + appName);
        this.appName = appName;
    }

    public int getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(int appIcon) {
        LogUtils.d(TAG, "setAppIcon() 调用，传入图标资源ID：" + appIcon);
        this.appIcon = appIcon;
    }

    public String getAppDescription() {
        return appDescription;
    }

    public void setAppDescription(String appDescription) {
        LogUtils.d(TAG, "setAppDescription() 调用，传入描述文案：" + appDescription);
        this.appDescription = appDescription;
    }

    public String getAppGitName() {
        return appGitName;
    }

    public void setAppGitName(String appGitName) {
        LogUtils.d(TAG, "setAppGitName() 调用，传入Git仓库名：" + appGitName);
        this.appGitName = appGitName;
    }

    public String getAppGitOwner() {
        return appGitOwner;
    }

    public void setAppGitOwner(String appGitOwner) {
        LogUtils.d(TAG, "setAppGitOwner() 调用，传入Git拥有者：" + appGitOwner);
        this.appGitOwner = appGitOwner;
    }

    public String getAppGitAPPBranch() {
        return appGitAPPBranch;
    }

    public void setAppGitAPPBranch(String appGitAPPBranch) {
        LogUtils.d(TAG, "setAppGitAPPBranch() 调用，传入Git分支：" + appGitAPPBranch);
        this.appGitAPPBranch = appGitAPPBranch;
    }

    public String getAppGitAPPSubProjectFolder() {
        return appGitAPPSubProjectFolder;
    }

    public void setAppGitAPPSubProjectFolder(String appGitAPPSubProjectFolder) {
        LogUtils.d(TAG, "setAppGitAPPSubProjectFolder() 调用，传入Git子项目文件夹：" + appGitAPPSubProjectFolder);
        this.appGitAPPSubProjectFolder = appGitAPPSubProjectFolder;
    }

    public String getAppHomePage() {
        return appHomePage;
    }

    public void setAppHomePage(String appHomePage) {
        LogUtils.d(TAG, "setAppHomePage() 调用，传入应用主页地址：" + appHomePage);
        this.appHomePage = appHomePage;
    }

    public String getAppAPKName() {
        return appAPKName;
    }

    public void setAppAPKName(String appAPKName) {
        LogUtils.d(TAG, "setAppAPKName() 调用，传入应用包名：" + appAPKName);
        this.appAPKName = appAPKName;
    }

    public String getAppAPKFolderName() {
        return appAPKFolderName;
    }

    public void setAppAPKFolderName(String appAPKFolderName) {
        LogUtils.d(TAG, "setAppAPKFolderName() 调用，传入包存储文件夹名：" + appAPKFolderName);
        this.appAPKFolderName = appAPKFolderName;
    }

    public boolean isAddDebugTools() {
        return isAddDebugTools;
    }

    public void setIsAddDebugTools(boolean isAddDebugTools) {
        LogUtils.d(TAG, "setIsAddDebugTools() 调用，传入调试开关状态：" + isAddDebugTools);
        this.isAddDebugTools = isAddDebugTools;
    }
}

