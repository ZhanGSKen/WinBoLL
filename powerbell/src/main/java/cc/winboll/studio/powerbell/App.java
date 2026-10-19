package cc.winboll.studio.powerbell;

import android.content.Context;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.powerbell.models.NotificationMessage;
import cc.winboll.studio.powerbell.receivers.GlobalApplicationReceiver;
import cc.winboll.studio.powerbell.utils.AppCacheUtils;
import cc.winboll.studio.powerbell.utils.AppConfigUtils;
import cc.winboll.studio.powerbell.utils.BackgroundSourceUtils;
import cc.winboll.studio.powerbell.utils.BitmapCacheUtils;
import cc.winboll.studio.powerbell.utils.NotificationManagerUtils;
import cc.winboll.studio.powerbell.views.MemoryCachedBackgroundView;

/**
 * 应用全局入口类
 * 适配：Java7 语法规范 | Android API30 系统版本
 * 核心策略：极致强制缓存 - 无论内存紧张程度，永不自动清理任何缓存（Bitmap/视图控件/路径记录）
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2025-12-29 15:30:00
 * @LastModified 2026-01-02 19:01:00
 */
public class App extends GlobalApplication {

    // ====================================== 常量区 - 置顶排序 (按功能归类) ======================================
    // 基础日志TAG
    private static final String TAG = "App";
    // 缓存保护专用TAG
    private static final String CACHE_PROTECT_TAG = "FORCE_CACHE_PROTECT";
    // 电池无效值常量（修复拼写错误：INVALID_BATTERY_VALUE）
    private static final int INVALID_BATTERY_VALUE = -1;

    // 组件跳转常量
    public static final String COMPONENT_EN1 = "cc.winboll.studio.powerbell.MainActivityEN1";
    public static final String COMPONENT_CN1 = "cc.winboll.studio.powerbell.MainActivityCN1";
    public static final String COMPONENT_CN2 = "cc.winboll.studio.powerbell.MainActivityCN2";

    // 动作跳转常量
    public static final String ACTION_SWITCHTO_EN1 = "cc.winboll.studio.powerbell.App.ACTION_SWITCHTO_EN1";
    public static final String ACTION_SWITCHTO_CN1 = "cc.winboll.studio.powerbell.App.ACTION_SWITCHTO_CN1";
    public static final String ACTION_SWITCHTO_CN2 = "cc.winboll.studio.powerbell.App.ACTION_SWITCHTO_CN2";

    // ====================================== 静态属性区 - 全局单例/状态 (按核心程度排序) ======================================
    // 应用单例
    private static App sApp;

    // 配置与缓存工具 (全局单例)
    public static AppConfigUtils sAppConfigUtils;
    private static AppCacheUtils sAppCacheUtils;

    // 资源与视图缓存 (强制驻留，极致缓存核心)
    public static BackgroundSourceUtils sBackgroundSourceUtils;
    public static BitmapCacheUtils sBitmapCacheUtils;
    private static MemoryCachedBackgroundView sMemoryCachedBackgroundView;

    // 系统状态 (电池电量)
    public static volatile int sQuantityOfElectricity = INVALID_BATTERY_VALUE;

    // 系统工具 (通知管理器)
    private static NotificationManagerUtils sNotificationManagerUtils;

    // ====================================== 成员属性区 - 非静态成员 (广播接收器) ======================================
    private GlobalApplicationReceiver mGlobalReceiver;

    // ====================================== 公共静态方法 - 单例/工具获取 (对外入口) ======================================
    /**
     * 获取应用全局单例实例
     * @return 应用单例App实例
     */
    public static App getInstance() {
        LogUtils.d(TAG, "【getInstance】应用单例获取方法调用 | 当前实例：" + sApp);
        return sApp;
    }

    /**
     * 获取配置工具类单例实例
     * @param context 上下文对象
     * @return 配置工具类AppConfigUtils实例
     */
    public static AppConfigUtils getAppConfigUtils(Context context) {
        String contextClass = context != null ? context.getClass().getSimpleName() : "null";
        LogUtils.d(TAG, "【getAppConfigUtils】配置工具获取方法调用 | 入参Context类型：" + contextClass);

        if (sAppConfigUtils == null) {
            sAppConfigUtils = AppConfigUtils.getInstance(context);
            LogUtils.d(TAG, "【getAppConfigUtils】配置工具实例为空，已初始化新实例");
        }
        return sAppConfigUtils;
    }

    /**
     * 获取缓存工具类单例实例
     * @param context 上下文对象
     * @return 缓存工具类AppCacheUtils实例
     */
    public static AppCacheUtils getAppCacheUtils(Context context) {
        String contextClass = context != null ? context.getClass().getSimpleName() : "null";
        LogUtils.d(TAG, "【getAppCacheUtils】缓存工具获取方法调用 | 入参Context类型：" + contextClass);

        if (sAppCacheUtils == null) {
            sAppCacheUtils = AppCacheUtils.getInstance(context);
            LogUtils.d(TAG, "【getAppCacheUtils】缓存工具实例为空，已初始化新实例");
        }
        return sAppCacheUtils;
    }

    // ====================================== 公共成员方法 - 业务逻辑 (实例方法) ======================================
    /**
     * 清除电池历史数据
     */
    public void clearBatteryHistory() {
        LogUtils.d(TAG, "【clearBatteryHistory】清除电池历史数据方法调用");
        if (sAppCacheUtils != null) {
            sAppCacheUtils.clearBatteryHistory();
            LogUtils.d(TAG, "【clearBatteryHistory】电池历史数据清除成功");
        } else {
            LogUtils.w(TAG, "【clearBatteryHistory】电池历史数据清除失败 | 缓存工具实例sAppCacheUtils为空");
        }
    }

    /**
     * 获取视图缓存实例
     * @return 视图缓存MemoryCachedBackgroundView实例
     */
    public MemoryCachedBackgroundView getMemoryCachedBackgroundView() {
        LogUtils.d(TAG, "【getMemoryCachedBackgroundView】视图缓存获取方法调用 | 当前实例：" + sMemoryCachedBackgroundView);
        return sMemoryCachedBackgroundView;
    }

    // ====================================== 公共静态方法 - 业务逻辑 (全局工具方法) ======================================
    /**
     * 手动清理所有缓存（仅主动调用生效，符合极致缓存策略）
     */
    public static void manualClearAllCache() {
        LogUtils.w(CACHE_PROTECT_TAG, "【manualClearAllCache】手动清理缓存方法调用 | 仅主动触发生效");

        // 清理Bitmap缓存
        if (sBitmapCacheUtils != null) {
            sBitmapCacheUtils.clearAllCache();
            LogUtils.d(CACHE_PROTECT_TAG, "【manualClearAllCache】Bitmap缓存已清理");
        }

        // 仅置空视图缓存引用，不销毁实例（极致缓存策略）
        if (sMemoryCachedBackgroundView != null) {
            LogUtils.d(CACHE_PROTECT_TAG, "【manualClearAllCache】视图缓存引用已置空 | 实例保留");
            sMemoryCachedBackgroundView = null;
        }

        LogUtils.w(CACHE_PROTECT_TAG, "【manualClearAllCache】手动清理缓存操作完成");
    }

    /**
     * 发送通知消息（仅调试模式下生效）
     * @param title 通知标题
     * @param content 通知内容
     */
    public static void notifyMessage(String title, String content) {
        LogUtils.d(TAG, "【notifyMessage】发送通知消息方法调用 | 标题：" + title + " | 内容：" + content);

        boolean canSend = isDebugging() && sApp != null && sNotificationManagerUtils != null;
        if (canSend) {
            NotificationMessage message = new NotificationMessage(title, content, "");
            sNotificationManagerUtils.showMessageNotification(sApp, message);
            LogUtils.d(TAG, "【notifyMessage】通知消息发送成功");
        } else {
            LogUtils.d(TAG, "【notifyMessage】通知消息发送失败 | 条件不满足：调试模式=" + isDebugging() + " | 应用实例=" + (sApp != null) + " | 通知工具=" + (sNotificationManagerUtils != null));
        }
    }

    // ====================================== 生命周期方法 - 应用全局生命周期 (重写父类方法) ======================================
    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "【onCreate】应用启动生命周期方法调用 | 开始初始化应用...");

        // 初始化应用单例与调试模式
        sApp = this;
        setIsDebugging(BuildConfig.DEBUG);
        LogUtils.d(TAG, "【onCreate】应用单例已初始化 | 调试模式：" + BuildConfig.DEBUG);

        // 初始化核心组件
        initBaseTools();
        initUtils();
        initReceiver();

        LogUtils.d(TAG, "【onCreate】应用初始化完成 | 极致强制缓存策略已激活");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LogUtils.d(TAG, "【onTerminate】应用终止生命周期方法调用 | 开始释放非缓存资源...");

        // 释放非缓存资源
        ToastUtils.release();
        releaseNotificationManager();
        releaseReceiver();

        // 核心策略：不清理任何缓存
        LogUtils.w(CACHE_PROTECT_TAG, "【onTerminate】极致缓存策略生效 | 所有缓存将保留在内存中");
        LogUtils.d(TAG, "【onTerminate】非缓存资源释放完成");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        LogUtils.w(CACHE_PROTECT_TAG, "【onTrimMemory】系统内存修剪回调 | 内存等级：" + level + " | 忽略修剪，缓存强制保护");
        logDetailedCacheStatus();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        LogUtils.w(CACHE_PROTECT_TAG, "【onLowMemory】系统低内存回调 | 极致缓存策略生效 | 不执行任何缓存清理操作");
        logDetailedCacheStatus();
    }

    // ====================================== 私有初始化方法 - 组件初始化 (按依赖顺序排序) ======================================
    /**
     * 初始化基础工具类（Activity管理、Toast、通知管理器）
     */
    private void initBaseTools() {
        LogUtils.d(TAG, "【initBaseTools】基础工具类初始化开始...");
        WinBoLLActivityManager.init(this);
        ToastUtils.init(this);
        sNotificationManagerUtils = new NotificationManagerUtils(this);
        LogUtils.d(TAG, "【initBaseTools】基础工具类初始化完成");
    }

    /**
     * 初始化核心工具与缓存（极致强制驻留，缓存核心）
     */
    private void initUtils() {
        LogUtils.d(TAG, "【initUtils】核心工具与缓存初始化开始 | 极致缓存策略激活");

        // 1. 配置与基础缓存工具初始化
        sAppConfigUtils = getAppConfigUtils(this);
        sAppCacheUtils = getAppCacheUtils(this);

        // 2. 资源与Bitmap缓存工具初始化（永久驻留）
        sBackgroundSourceUtils = BackgroundSourceUtils.getInstance(this);
        sBackgroundSourceUtils.loadSettings();
        sBitmapCacheUtils = BitmapCacheUtils.getInstance();
        LogUtils.d(TAG, "【initUtils】资源与Bitmap缓存工具初始化完成 | 永久驻留内存");

        // 3. 视图缓存初始化（永久驻留，无实例则创建）
        sMemoryCachedBackgroundView = MemoryCachedBackgroundView.getLastInstance(this);
        if (sMemoryCachedBackgroundView == null) {
            sMemoryCachedBackgroundView = MemoryCachedBackgroundView.getInstance(this, sBackgroundSourceUtils.getCurrentBackgroundBean(), true);
            LogUtils.d(TAG, "【initUtils】视图缓存无现有实例，已创建新实例");
        }
        LogUtils.d(TAG, "【initUtils】视图缓存初始化完成 | 永久驻留内存");
    }

    /**
     * 注册全局广播接收器
     */
    private void initReceiver() {
        LogUtils.d(TAG, "【initReceiver】全局广播接收器注册开始...");
        mGlobalReceiver = new GlobalApplicationReceiver(this);
        mGlobalReceiver.registerAction();
        LogUtils.d(TAG, "【initReceiver】全局广播接收器注册完成");
    }

    // ====================================== 私有释放方法 - 资源释放 (按创建逆序排序) ======================================
    /**
     * 释放全局广播接收器
     */
    private void releaseReceiver() {
        LogUtils.d(TAG, "【releaseReceiver】全局广播接收器释放开始...");
        if (mGlobalReceiver != null) {
            mGlobalReceiver.unregisterAction();
            mGlobalReceiver = null;
            LogUtils.d(TAG, "【releaseReceiver】全局广播接收器释放完成");
        }
    }

    /**
     * 释放通知管理器资源
     */
    private void releaseNotificationManager() {
        LogUtils.d(TAG, "【releaseNotificationManager】通知管理器资源释放开始...");
        if (sNotificationManagerUtils != null) {
            sNotificationManagerUtils.release();
            sNotificationManagerUtils = null;
            LogUtils.d(TAG, "【releaseNotificationManager】通知管理器资源释放完成");
        }
    }

    // ====================================== 私有辅助方法 - 日志/工具 (辅助功能) ======================================
    /**
     * 记录当前缓存详细状态（用于调试监控，极致缓存策略监控）
     */
    private void logDetailedCacheStatus() {
        LogUtils.d(TAG, "【logDetailedCacheStatus】缓存状态监控日志开始...");

        // Bitmap缓存状态
        if (sBitmapCacheUtils != null) {
            LogUtils.d(CACHE_PROTECT_TAG, "【缓存状态】BitmapCache - 有效");
            try {
                LogUtils.d(CACHE_PROTECT_TAG, "【缓存详情】Bitmap缓存数量：" + sBitmapCacheUtils.getCacheCount());
            } catch (Exception e) {
                LogUtils.e(CACHE_PROTECT_TAG, "【缓存详情】获取Bitmap缓存数量失败", e);
            }
        } else {
            LogUtils.d(CACHE_PROTECT_TAG, "【缓存状态】BitmapCache - 未初始化");
        }

        // 视图缓存状态
        if (sMemoryCachedBackgroundView != null) {
            LogUtils.d(CACHE_PROTECT_TAG, "【缓存状态】ViewCache - 有效");
            LogUtils.d(CACHE_PROTECT_TAG, "【缓存详情】视图实例数量：" + MemoryCachedBackgroundView.getInstanceCount());
        } else {
            LogUtils.d(CACHE_PROTECT_TAG, "【缓存状态】ViewCache - 引用已置空（实例可能保留）");
        }
    }
}

