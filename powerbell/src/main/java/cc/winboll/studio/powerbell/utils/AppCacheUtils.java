package cc.winboll.studio.powerbell.utils;

import android.content.Context;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.models.BatteryInfoBean;
import java.util.ArrayList;

/**
 * 应用缓存工具类（适配Android API 30，基于Java 7编写）
 * 负责电池信息的缓存、持久化与管理
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Describe 电池信息缓存工具：实现电量变化记录、持久化存储与缓存限制
 */
public class AppCacheUtils {
    // ===================== 静态常量区（置顶归类，消除魔法值） =====================
    public static final String TAG = "AppCacheUtils";
    private static final int MAX_BATTERY_RECORD_COUNT = 180; // 电池记录最大条数限制

    // ===================== 静态成员区（单例相关） =====================
    private static AppCacheUtils sInstance;

    // ===================== 成员变量区（按功能分层） =====================
    private Context mContext; // ApplicationContext，避免内存泄漏
    private ArrayList<BatteryInfoBean> mBatteryInfoList; // 电池信息缓存列表

    // ===================== 单例方法区（线程安全） =====================
    /**
     * 获取单例实例
     * @param context 上下文（内部会转换为ApplicationContext）
     * @return 唯一AppCacheUtils实例
     */
    public static synchronized AppCacheUtils getInstance(Context context) {
        String contextType = context != null ? context.getClass().getSimpleName() : "null";
        LogUtils.d(TAG, String.format("getInstance调用 | 传入Context类型=%s", contextType));

        if (sInstance == null) {
            if (context == null) {
                LogUtils.e(TAG, "getInstance失败：传入Context为null");
                throw new IllegalArgumentException("Context cannot be null");
            }
            sInstance = new AppCacheUtils(context.getApplicationContext());
            LogUtils.d(TAG, "getInstance：单例实例初始化完成");
        }
        return sInstance;
    }

    // ===================== 私有构造方法区（禁止外部实例化） =====================
    /**
     * 私有构造方法，初始化缓存列表并加载持久化数据
     * @param context ApplicationContext
     */
    private AppCacheUtils(Context context) {
        LogUtils.d(TAG, "AppCacheUtils构造方法调用");
        mContext = context;
        mBatteryInfoList = new ArrayList<BatteryInfoBean>();
        loadAppCacheData();
        LogUtils.d(TAG, String.format("AppCacheUtils构造完成 | 初始电池信息数量=%d", mBatteryInfoList.size()));
    }

    // ===================== 公共业务方法区（对外暴露接口） =====================
    /**
     * 添加电池电量变化记录（仅当电量变化时添加）
     * @param batteryValue 电池电量值
     */
    public void addChangingTime(int batteryValue) {
        LogUtils.d(TAG, String.format("addChangingTime调用 | 传入电量值=%d", batteryValue));

        if (mBatteryInfoList.isEmpty()) {
            addChangingTimeToList(batteryValue);
            LogUtils.d(TAG, "addChangingTime：缓存列表为空，直接添加记录");
            return;
        }

        // 对比最后一条记录的电量值，避免重复添加
        int lastBatteryValue = mBatteryInfoList.get(mBatteryInfoList.size() - 1).getBatteryValue();
        if (lastBatteryValue != batteryValue) {
            addChangingTimeToList(batteryValue);
            LogUtils.d(TAG, String.format("addChangingTime：电量变化，添加新记录 | 原电量=%d | 新电量=%d", lastBatteryValue, batteryValue));
        } else {
            LogUtils.d(TAG, "addChangingTime：电量未变化，跳过添加");
        }
    }

    /**
     * 获取电池信息缓存列表
     * @return 完整的电池信息列表
     */
    public ArrayList<BatteryInfoBean> getArrayListBatteryInfo() {
        LogUtils.d(TAG, String.format("getArrayListBatteryInfo调用 | 当前缓存数量=%d", mBatteryInfoList.size()));
        loadAppCacheData();
        return mBatteryInfoList;
    }

    /**
     * 清除所有电池历史记录
     */
    public void clearBatteryHistory() {
        LogUtils.d(TAG, String.format("clearBatteryHistory调用 | 清除前缓存数量=%d", mBatteryInfoList.size()));
        mBatteryInfoList.clear();
        saveAppCacheData();
        LogUtils.d(TAG, "clearBatteryHistory完成 | 缓存已清空");
    }

    // ===================== 私有辅助方法区（内部业务逻辑） =====================
    /**
     * 内部方法：添加电量记录到列表并持久化
     * @param batteryValue 电池电量值
     */
    private void addChangingTimeToList(int batteryValue) {
        LogUtils.d(TAG, String.format("addChangingTimeToList调用 | 传入电量值=%d", batteryValue));

        // 限制列表最大长度，避免内存溢出
        if (mBatteryInfoList.size() >= MAX_BATTERY_RECORD_COUNT) {
            mBatteryInfoList.remove(0);
            LogUtils.d(TAG, String.format("addChangingTimeToList：列表超过%d条，移除最旧记录", MAX_BATTERY_RECORD_COUNT));
        }

        BatteryInfoBean batteryInfo = new BatteryInfoBean(System.currentTimeMillis(), batteryValue);
        mBatteryInfoList.add(batteryInfo);
        LogUtils.d(TAG, String.format("addChangingTimeToList：添加新记录 | 电量=%d | 时间戳=%d", batteryInfo.getBatteryValue(), batteryInfo.getTimeStamp()));
        saveAppCacheData();
    }

    /**
     * 从文件加载缓存数据
     */
    private void loadAppCacheData() {
        LogUtils.d(TAG, "loadAppCacheData调用 | 开始加载持久化数据");
        mBatteryInfoList.clear();
        BatteryInfoBean.loadBeanList(mContext, mBatteryInfoList, BatteryInfoBean.class);
        LogUtils.d(TAG, String.format("loadAppCacheData完成 | 加载数据数量=%d", mBatteryInfoList.size()));
    }

    /**
     * 保存缓存数据到文件
     */
    private void saveAppCacheData() {
        LogUtils.d(TAG, String.format("saveAppCacheData调用 | 保存数据数量=%d", mBatteryInfoList.size()));
        BatteryInfoBean.saveBeanList(mContext, mBatteryInfoList, BatteryInfoBean.class);
        LogUtils.d(TAG, "saveAppCacheData完成 | 数据已持久化");
    }
}

