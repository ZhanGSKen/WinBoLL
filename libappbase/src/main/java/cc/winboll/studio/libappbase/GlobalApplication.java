package cc.winboll.studio.libappbase;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/11 19:56
 * @Describe 全局 Application 类，用于初始化应用核心组件、管理全局状态（如调试模式）
 * 需在 AndroidManifest.xml 中配置 android:name=".GlobalApplication" 使其生效
 */
public class GlobalApplication extends Application {

    /** 日志标签 */
    public static final String TAG = "GlobalApplication";

    /** 全局 Application 单例实例（volatile 保证多线程可见性，避免指令重排） */
    private static volatile GlobalApplication sInstance;

    /**
     * 应用调试模式标记（volatile 保证多线程可见性）
     * true：调试模式（开启日志、调试功能）；false：正式模式（关闭调试相关功能）
     */
    private static volatile boolean isDebugging = false;

    // 新增：WinBoLL 服务器主机地址（volatile 保证多线程可见性）
    private static volatile String winbollHost = null;
    // 新增：SP 存储相关常量（私有存储，仅当前应用可访问）
    private static final String SP_NAME = "WinBoLL_SP_CONFIG";
    private static final String SP_KEY_WINBOLL_HOST = "winboll_host";

    /**
     * 获取全局 Application 单例实例（外部可通过此方法获取上下文）
     * @return GlobalApplication 单例（未初始化时返回 null，需确保配置 AndroidManifest）
     */
    public static GlobalApplication getInstance() {
        return sInstance;
    }

    /**
     * 设置应用调试模式
     * @param debugging 调试模式状态（true/false）
     */
    public static void setIsDebugging(boolean debugging) {
        isDebugging = debugging;
    }

    /**
     * 保存调试模式状态到本地文件（持久化存储，重启应用后生效）
     * @param application 全局 Application 实例（通过 getInstance() 获取更规范）
     */
    public static void saveDebugStatus(GlobalApplication application) {
        if (application == null) {
            LogUtils.e(TAG, "saveDebugStatus: Application 实例为空，保存失败");
            return;
        }
        // 将调试状态封装为 APPModel 并保存到文件
        APPModel.saveBeanToFile(
            getAppModelFilePath(application),
            new APPModel(isDebugging)
        );
    }

    /**
     * 获取 APPModel 配置文件的存储路径
     * 路径：应用私有数据目录 / APPModel.json（仅当前应用可访问，安全）
     * @param application 全局 Application 实例
     * @return 配置文件绝对路径
     */
    private static String getAppModelFilePath(GlobalApplication application) {
        return application.getDataDir().getPath() + "/APPModel.json";
    }

    /**
     * 获取当前应用调试模式状态
     * @return true：调试模式；false：正式模式
     */
    public static boolean isDebugging() {
        return isDebugging;
    }

	// 新增：设置 WinBoLL 服务器主机地址（同时保存到 SP 持久化）
	public static void setWinbollHost(String host) {
		if (sInstance == null) {
			LogUtils.e(TAG, "setWinbollHost: 应用未初始化，设置失败");
			return;
		}
		// 检查并补全末尾 /  核心改动
		if (host != null && !host.isEmpty() && !host.endsWith("/")) {
			host += "/";
		}
		// 更新内存中的字段
		winbollHost = host;
		// 保存到 SP 持久化（私有模式，安全）
		SharedPreferences sp = sInstance.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
		sp.edit().putString(SP_KEY_WINBOLL_HOST, host).apply();
		LogUtils.d(TAG, "setWinbollHost: 服务器地址已设置并持久化，host=" + host);
	}


    // 新增：获取 WinBoLL 服务器主机地址（优先内存，内存为空则从 SP 读取）
    public static String getWinbollHost() {
        if (winbollHost != null) {
            // 内存中存在，直接返回（提高效率）
            return winbollHost;
        }
        if (sInstance == null) {
            LogUtils.e(TAG, "getWinbollHost: 应用未初始化，获取失败");
            return null;
        }
        // 内存中不存在，从 SP 读取并更新到内存
        SharedPreferences sp = sInstance.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        winbollHost = sp.getString(SP_KEY_WINBOLL_HOST, "https://console.winboll.cc/");
        LogUtils.d(TAG, "getWinbollHost: 从 SP 读取服务器地址，host=" + winbollHost);
        return winbollHost;
    }

    /**
     * 应用启动时初始化（仅执行一次）
     * 初始化核心框架、恢复调试状态、配置全局异常处理等
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化单例实例（确保在所有初始化操作前完成）
        sInstance = this;

        restoreDebugStatus();
        // 初始化基础组件（日志、崩溃处理、Toast）
        initCoreComponents();
        // 初始化服务器地址（从 SP 读取到内存，提高后续访问效率）
        initWinbollHost();

        LogUtils.d(TAG, "GlobalApplication 初始化完成，单例实例已创建");
    }

    /**
     * 初始化应用核心组件（日志、崩溃处理、Toast 框架）
     */
    private void initCoreComponents() {
        // 初始化日志工具（传入 Application 上下文）
		
		// 调试状态下初始化日志工具
		if (GlobalApplication.isDebugging()) {
			LogUtils.init(this);
		}
        // 初始化全局异常处理器（捕获应用崩溃信息，用于调试或上报）
        CrashHandler.init(this);
        // 初始化 Toast 工具（统一 Toast 样式、避免内存泄漏等）
        ToastUtils.init(this);
    }

    /**
     * 恢复调试模式状态（从本地配置文件读取）
     * 1. 读取本地 APPModel.json 文件
     * 2. 读取成功：使用保存的调试状态；读取失败（文件不存在）：默认关闭调试并创建配置文件
     */
    private void restoreDebugStatus() {
        // 从文件加载 APPModel 实例（存储调试状态的模型类）
        APPModel appModel = APPModel.loadBeanFromFile(
            getAppModelFilePath(this),
            APPModel.class
        );

        if (appModel == null) {
            // 配置文件不存在，默认关闭调试模式并创建文件
            setIsDebugging(false);
            saveDebugStatus(this);
            LogUtils.d(TAG, "调试配置文件不存在，默认关闭调试模式并创建配置文件");
        } else {
            // 配置文件存在，使用保存的调试状态
            setIsDebugging(appModel.isDebugging());
            LogUtils.d(TAG, "从配置文件恢复调试模式：" + isDebugging);
        }
    }

    // 新增：初始化服务器地址（应用启动时从 SP 读取到内存）
    private void initWinbollHost() {
        getWinbollHost(); // 触发从 SP 读取并更新内存
    }

    /**
     * 获取应用名称（从 AndroidManifest.xml 的 android:label 读取）
     * @param context 上下文（建议传入 Application 上下文，避免内存泄漏）
     * @return 应用名称（读取失败返回 null）
     */
    public static String getAppName(Context context) {
        if (context == null) {
            LogUtils.w(TAG, "getAppName: 上下文为空，返回 null");
            return null;
        }
        PackageManager packageManager = context.getPackageManager();
        try {
            // 获取应用信息（包含应用名称、图标等）
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(
                context.getPackageName(),  // 当前应用包名
                0  // 额外标志（0 表示默认获取基本信息）
            );
            // 从应用信息中获取应用名称（支持多语言）
            String appName = (String) packageManager.getApplicationLabel(applicationInfo);
            LogUtils.d(TAG, "获取应用名称成功：" + appName);
            return appName;
        } catch (NameNotFoundException e) {
            // 包名不存在（理论上不会发生，捕获异常避免崩溃）
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
            //LogUtils.e(TAG, "获取应用名称失败：包名不存在", e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 应用终止时调用（仅用于释放全局资源）
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
        // 释放单例引用（可选，避免内存泄漏风险）
        sInstance = null;
        LogUtils.d(TAG, "GlobalApplication 终止，单例实例已释放");
    }
}

