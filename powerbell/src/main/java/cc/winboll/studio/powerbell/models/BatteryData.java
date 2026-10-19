package cc.winboll.studio.powerbell.models;

import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/03/22 14:30:51
 * @Describe 电池报告数据模型
 * 适配 API30，存储当前电量、放电时间、充电时间核心数据
 * 支持参数校验与调试日志输出
 */
public class BatteryData {
    // ====================== 静态常量（首屏可见，统一管理） ======================
    public static final String TAG = "BatteryData";
    // 字段校验常量（避免硬编码，统一管理）
    private static final int BATTERY_MIN = 0;
    private static final int BATTERY_MAX = 100;
    private static final String EMPTY_TIME = "00:00:00";

    // ====================== 成员变量（按功能分类：电量→时间） ======================
    private int currentLevel;     // 当前电池电量（0-100）
    private String dischargeTime; // 放电时间
    private String chargeTime;    // 充电时间

    // ====================== 构造方法（按参数重载排序，补充校验与日志） ======================
    /**
     * 无参构造器（适配 JSON 反序列化、反射实例化场景）
     */
    public BatteryData() {
        this.currentLevel = BATTERY_MIN;
        this.dischargeTime = EMPTY_TIME;
        this.chargeTime = EMPTY_TIME;
        LogUtils.d(TAG, "BatteryData: 无参构造初始化完成，默认值已设置");
    }

    /**
     * 带参构造器（核心构造，初始化所有字段）
     * @param currentLevel 当前电量（0-100）
     * @param dischargeTime 放电时间
     * @param chargeTime 充电时间
     */
    public BatteryData(int currentLevel, String dischargeTime, String chargeTime) {
        // 电量范围校验（0-100，异常值置为0）
        this.currentLevel = currentLevel >= BATTERY_MIN && currentLevel <= BATTERY_MAX 
			? currentLevel : BATTERY_MIN;
        // 时间字段防 null（空值置为默认空时间）
        this.dischargeTime = dischargeTime == null ? EMPTY_TIME : dischargeTime;
        this.chargeTime = chargeTime == null ? EMPTY_TIME : chargeTime;

        // 调试日志：输出入参与最终赋值结果
        LogUtils.d(TAG, String.format("BatteryData: 带参构造初始化完成 | 当前电量：%d（输入：%d）| 放电时间：%s（输入：%s）| 充电时间：%s（输入：%s）",
									  this.currentLevel, currentLevel,
									  this.dischargeTime, dischargeTime,
									  this.chargeTime, chargeTime));
    }

    // ====================== Getter 方法（按成员变量顺序排列，补充日志可选） ======================
    /**
     * 获取当前电池电量
     * @return 当前电量（0-100）
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * 获取放电时间
     * @return 放电时间
     */
    public String getDischargeTime() {
        return dischargeTime;
    }

    /**
     * 获取充电时间
     * @return 充电时间
     */
    public String getChargeTime() {
        return chargeTime;
    }
}

