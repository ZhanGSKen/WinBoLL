package cc.winboll.studio.powerbell.utils;

import android.content.Context;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.App;
import cc.winboll.studio.powerbell.models.AppConfigBean;
import cc.winboll.studio.powerbell.models.ControlCenterServiceBean;
import cc.winboll.studio.powerbell.threads.RemindThread;

/**
 * 应用配置工具类：管理应用核心配置（服务开关、电池提醒阈值、背景设置等）
 * 适配：Java7 | API30 | 小米手机，单例模式，线程安全，配置持久化
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Describe 应用配置全量管理工具，支持配置持久化、自动校准、线程安全访问
 */
public class AppConfigUtils {
    // ======================== 静态常量区（魔法值统一管理）========================
    public static final String TAG = "AppConfigUtils";
    public static final String BACKGROUND_DIR = "Background"; // 背景图片存储目录
    private static final int MIN_REMINDER_VALUE = 0;         // 提醒阈值最小值
    private static final int MAX_REMINDER_VALUE = 100;       // 提醒阈值最大值
    private static final int MIN_INTERVAL_TIME = 1000;       // 最小提醒间隔（ms）
    private static final int MIN_DETECT_INTERVAL = 500;      // 最小电量检测间隔（ms）

    // ======================== 静态成员区（单例实例）========================
    private static volatile AppConfigUtils sInstance; // 单例实例（volatile保障双重校验锁有效性）

    // ======================== 成员变量区（按依赖优先级排序，final/volatile保障线程安全）========================
    private final Context mContext;          // 应用上下文（ApplicationContext，避免内存泄漏）
    private final App mApplication;          // 应用Application实例（final保障不可变）
    public volatile AppConfigBean mAppConfigBean;              // 应用配置Bean（持久化核心，volatile保障线程安全）
    private volatile boolean mIsServiceEnabled = false;        // 服务开关缓存状态（减少Bean读取次数）

    // ======================== 单例相关方法区（双重校验锁+构造方法）========================
    /**
     * 双重校验锁单例获取方法，线程安全
     * @param context 上下文（不可为null）
     * @return 单例实例
     */
    public static AppConfigUtils getInstance(Context context) {
        String contextType = context != null ? context.getClass().getSimpleName() : "null";
        LogUtils.d(TAG, String.format("getInstance() 调用 | 传入Context类型=%s", contextType));

        if (context == null) {
            LogUtils.e(TAG, "getInstance() 失败：Context不能为空");
            throw new IllegalArgumentException("Context cannot be null");
        }

        if (sInstance == null) {
            synchronized (AppConfigUtils.class) {
                if (sInstance == null) {
                    sInstance = new AppConfigUtils(context);
                    LogUtils.d(TAG, "getInstance()：单例实例创建成功");
                }
            }
        }

        LogUtils.d(TAG, "getInstance()：单例实例获取成功");
        return sInstance;
    }

    /**
     * 私有构造方法，禁止外部实例化
     * @param context 上下文（内部转换为ApplicationContext）
     */
    private AppConfigUtils(Context context) {
        LogUtils.d(TAG, "AppConfigUtils() 构造方法调用");
        this.mContext = context.getApplicationContext();
        this.mApplication = (App) context.getApplicationContext();
        mAppConfigBean = new AppConfigBean();
        loadAppConfig(); // 加载持久化配置
        LogUtils.d(TAG, "AppConfigUtils() 构造完成，配置初始化成功");
    }

    // ======================== 核心配置持久化方法区（加载+保存）========================
    /**
     * 加载应用配置（初始化/重载通用入口）
     * @return 加载后的应用配置Bean
     */
    public AppConfigBean loadAppConfig() {
        LogUtils.d(TAG, "loadAppConfig() 调用 | 开始加载应用配置");
        AppConfigBean savedAppBean = (AppConfigBean) AppConfigBean.loadBean(mContext, AppConfigBean.class);

        if (savedAppBean != null) {
            mAppConfigBean = savedAppBean;
            LogUtils.d(TAG, String.format("loadAppConfig() 成功 | 充电阈值=%d%% | 耗电阈值=%d%%",
                                          mAppConfigBean.getChargeReminderValue(), mAppConfigBean.getUsageReminderValue()));
        } else {
            mAppConfigBean = new AppConfigBean();
            AppConfigBean.saveBean(mContext, mAppConfigBean);
            LogUtils.d(TAG, "loadAppConfig()：无已保存配置，使用默认值并持久化");
        }

        return mAppConfigBean;
    }

    /**
     * 保存应用配置（内部核心方法，直接持久化）
     */
    public void saveAppConfig() {
        AppConfigBean.saveBean(mContext, mAppConfigBean);
        LogUtils.d(TAG, "saveAppConfig()：应用配置保存成功");
    }

    // ======================== 充电提醒配置方法区（开关+阈值）========================
    /**
     * 设置充电提醒开关状态
     * @param isEnabled 目标状态（true=开启，false=关闭）
     */
    public void setChargeReminderEnabled(final boolean isEnabled) {
        LogUtils.d(TAG, String.format("setChargeReminderEnabled() 调用 | 传入状态=%b", isEnabled));

        if (isEnabled == mAppConfigBean.isEnableChargeReminder()) {
            LogUtils.d(TAG, "setChargeReminderEnabled()：充电提醒状态无变化，无需操作");
            return;
        }

        mAppConfigBean.setEnableChargeReminder(isEnabled);
        saveAppConfig();
        LogUtils.d(TAG, String.format("setChargeReminderEnabled() 成功 | 充电提醒状态=%s", isEnabled ? "开启" : "关闭"));
    }

    /**
     * 获取充电提醒开关状态
     * @return 充电提醒状态（true=开启，false=关闭）
     */
    public boolean isChargeReminderEnabled() {
        boolean isEnabled = mAppConfigBean.isEnableChargeReminder();
        LogUtils.d(TAG, String.format("isChargeReminderEnabled()：获取充电提醒状态=%s", isEnabled ? "开启" : "关闭"));
        return isEnabled;
    }

    /**
     * 设置充电提醒阈值（自动校准0-100）
     * @param value 目标阈值
     */
    public void setChargeReminderValue(final int value) {
        LogUtils.d(TAG, String.format("setChargeReminderValue() 调用 | 传入阈值=%d", value));
        final int calibratedValue = Math.min(Math.max(value, MIN_REMINDER_VALUE), MAX_REMINDER_VALUE);

        if (calibratedValue == mAppConfigBean.getChargeReminderValue()) {
            LogUtils.d(TAG, "setChargeReminderValue()：充电提醒阈值无变化，无需操作");
            return;
        }

        mAppConfigBean.setChargeReminderValue(calibratedValue);
        saveAppConfig();
        LogUtils.d(TAG, String.format("setChargeReminderValue() 成功 | 充电提醒阈值=%d%%", calibratedValue));
    }

    /**
     * 获取充电提醒阈值
     * @return 充电提醒阈值（0-100）
     */
    public int getChargeReminderValue() {
        int value = mAppConfigBean.getChargeReminderValue();
        LogUtils.d(TAG, String.format("getChargeReminderValue()：获取充电提醒阈值=%d%%", value));
        return value;
    }

    // ======================== 耗电提醒配置方法区（开关+阈值）========================
    /**
     * 设置耗电提醒开关状态
     * @param isEnabled 目标状态（true=开启，false=关闭）
     */
    public void setUsageReminderEnabled(final boolean isEnabled) {
        LogUtils.d(TAG, String.format("setUsageReminderEnabled() 调用 | 传入状态=%b", isEnabled));

        if (isEnabled == mAppConfigBean.isEnableUsageReminder()) {
            LogUtils.d(TAG, "setUsageReminderEnabled()：耗电提醒状态无变化，无需操作");
            return;
        }

        mAppConfigBean.setEnableUsageReminder(isEnabled);
        saveAppConfig();
        LogUtils.d(TAG, String.format("setUsageReminderEnabled() 成功 | 耗电提醒状态=%s", isEnabled ? "开启" : "关闭"));
    }

    /**
     * 获取耗电提醒开关状态
     * @return 耗电提醒状态（true=开启，false=关闭）
     */
    public boolean isUsageReminderEnabled() {
        boolean isEnabled = mAppConfigBean.isEnableUsageReminder();
        LogUtils.d(TAG, String.format("isUsageReminderEnabled()：获取耗电提醒状态=%s", isEnabled ? "开启" : "关闭"));
        return isEnabled;
    }

    /**
     * 设置耗电提醒阈值（自动校准0-100）
     * @param value 目标阈值
     */
    public void setUsageReminderValue(final int value) {
        LogUtils.d(TAG, String.format("setUsageReminderValue() 调用 | 传入阈值=%d", value));
        final int calibratedValue = Math.min(Math.max(value, MIN_REMINDER_VALUE), MAX_REMINDER_VALUE);

        if (calibratedValue == mAppConfigBean.getUsageReminderValue()) {
            LogUtils.d(TAG, "setUsageReminderValue()：耗电提醒阈值无变化，无需操作");
            return;
        }

        mAppConfigBean.setUsageReminderValue(calibratedValue);
        saveAppConfig();
        LogUtils.d(TAG, String.format("setUsageReminderValue() 成功 | 耗电提醒阈值=%d%%", calibratedValue));
    }

    /**
     * 获取耗电提醒阈值
     * @return 耗电提醒阈值（0-100）
     */
    public int getUsageReminderValue() {
        int value = mAppConfigBean.getUsageReminderValue();
        LogUtils.d(TAG, String.format("getUsageReminderValue()：获取耗电提醒阈值=%d%%", value));
        return value;
    }

    // ======================== 实时电池状态配置方法区（内存缓存，不持久化）========================
    /**
     * 设置当前充电状态（仅内存缓存）
     * @param isCharging 充电状态（true=充电中，false=未充电）
     */
    public void setCharging(boolean isCharging) {
        LogUtils.d(TAG, String.format("setCharging() 调用 | 传入状态=%b", isCharging));

        if (isCharging == mAppConfigBean.isCharging()) {
            LogUtils.d(TAG, "setCharging()：充电状态无变化，无需操作");
            return;
        }

        mAppConfigBean.setIsCharging(isCharging);
        LogUtils.d(TAG, String.format("setCharging() 成功 | 充电状态=%s", isCharging ? "充电中" : "未充电"));
    }

    /**
     * 获取当前充电状态
     * @return 充电状态（true=充电中，false=未充电）
     */
    public boolean isCharging() {
        boolean isCharging = mAppConfigBean.isCharging();
        LogUtils.d(TAG, String.format("isCharging()：获取充电状态=%s", isCharging ? "充电中" : "未充电"));
        return isCharging;
    }

    /**
     * 设置当前电池电量（仅内存缓存，自动校准0-100）
     * @param value 当前电量
     */
    public void setCurrentBatteryValue(int value) {
        LogUtils.d(TAG, String.format("setCurrentBatteryValue() 调用 | 传入电量=%d", value));
        int calibratedValue = Math.min(Math.max(value, MIN_REMINDER_VALUE), MAX_REMINDER_VALUE);

        if (calibratedValue == App.sQuantityOfElectricity) {
            LogUtils.d(TAG, "setCurrentBatteryValue()：电池电量无变化，无需操作");
            return;
        }

        App.sQuantityOfElectricity = calibratedValue;
        LogUtils.d(TAG, String.format("setCurrentBatteryValue() 成功 | 电池电量=%d%%", calibratedValue));
    }

    /**
     * 获取当前电池电量
     * @return 当前电池电量（0-100）
     */
    public int getCurrentBatteryValue() {
        int value = App.sQuantityOfElectricity;
        LogUtils.d(TAG, String.format("getCurrentBatteryValue()：获取电池电量=%d%%", value));
        return value;
    }

    // ======================== 间隔配置方法区（持久化）========================
    /**
     * 设置提醒间隔时间（自动校准最小1000ms）
     * @param interval 目标间隔（单位：ms）
     */
    public void setReminderIntervalTime(final int interval) {
        LogUtils.d(TAG, String.format("setReminderIntervalTime() 调用 | 传入间隔=%dms", interval));
        final int calibratedInterval = Math.max(interval, MIN_INTERVAL_TIME);

        if (calibratedInterval == mAppConfigBean.getReminderIntervalTime()) {
            LogUtils.d(TAG, "setReminderIntervalTime()：提醒间隔无变化，无需操作");
            return;
        }

        mAppConfigBean.setReminderIntervalTime(calibratedInterval);
        saveAppConfig();
        LogUtils.d(TAG, String.format("setReminderIntervalTime() 成功 | 提醒间隔=%dms", calibratedInterval));
    }

    /**
     * 获取提醒间隔时间
     * @return 提醒间隔（单位：ms）
     */
    public int getReminderIntervalTime() {
        int interval = mAppConfigBean.getReminderIntervalTime();
        LogUtils.d(TAG, String.format("getReminderIntervalTime()：获取提醒间隔=%dms", interval));
        return interval;
    }

    /**
     * 设置电量检测间隔（自动校准最小500ms）
     * @param interval 目标间隔（单位：ms）
     */
    public void setBatteryDetectInterval(final int interval) {
        LogUtils.d(TAG, String.format("setBatteryDetectInterval() 调用 | 传入间隔=%dms", interval));
        final int calibratedInterval = Math.max(interval, MIN_DETECT_INTERVAL);

        if (calibratedInterval == mAppConfigBean.getBatteryDetectInterval()) {
            LogUtils.d(TAG, "setBatteryDetectInterval()：检测间隔无变化，无需操作");
            return;
        }

        mAppConfigBean.setBatteryDetectInterval(calibratedInterval);
        saveAppConfig();
        LogUtils.d(TAG, String.format("setBatteryDetectInterval() 成功 | 电量检测间隔=%dms", calibratedInterval));
    }

    /**
     * 获取电量检测间隔
     * @return 电量检测间隔（单位：ms）
     */
    public int getBatteryDetectInterval() {
        int interval = mAppConfigBean.getBatteryDetectInterval();
        LogUtils.d(TAG, String.format("getBatteryDetectInterval()：获取电量检测间隔=%dms", interval));
        return interval;
    }

    // ======================== 服务开关配置方法区（独立Bean）========================
    /**
     * 获取服务开关状态
     * @return 服务开关状态（true=开启，false=关闭）
     */
    public boolean isServiceEnabled() {
        LogUtils.d(TAG, "isServiceEnabled() 调用 | 开始获取服务开关状态");
        ControlCenterServiceBean savedServiceBean = (ControlCenterServiceBean) ControlCenterServiceBean.loadBean(mContext, ControlCenterServiceBean.class);

        if (savedServiceBean != null) {
            boolean isEnabled = savedServiceBean.isEnableService();
            LogUtils.d(TAG, String.format("isServiceEnabled()：服务开关状态=%b", isEnabled));
            return isEnabled;
        } else {
            ControlCenterServiceBean.saveBean(mContext, new ControlCenterServiceBean(false));
            LogUtils.d(TAG, "isServiceEnabled()：无已保存服务配置，默认关闭并持久化");
            return false;
        }
    }

    /**
     * 设置服务开关状态
     * @param isServiceEnabled 目标状态（true=开启，false=关闭）
     */
    public void setIsServiceEnabled(boolean isServiceEnabled) {
        LogUtils.d(TAG, String.format("setIsServiceEnabled() 调用 | 传入状态=%b", isServiceEnabled));
        ControlCenterServiceBean.saveBean(mContext, new ControlCenterServiceBean(isServiceEnabled));
        LogUtils.d(TAG, String.format("setIsServiceEnabled() 成功 | 服务开关状态=%b", isServiceEnabled));
    }
}

