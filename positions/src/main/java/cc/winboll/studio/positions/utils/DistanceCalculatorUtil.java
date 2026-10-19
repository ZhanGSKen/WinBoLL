package cc.winboll.studio.positions.utils;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.positions.models.PositionModel;
import cc.winboll.studio.positions.models.PositionTaskModel;
import cc.winboll.studio.positions.services.MainService;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/10/27 18:40
 * @Describe 距离计算工具集（单例模式）
 */
public class DistanceCalculatorUtil {

    public static final String TAG = "DistanceCalculatorUtil";

    // 1. 私有静态 volatile 实例：保证多线程下实例可见性，避免指令重排序导致的空指针
    private static volatile DistanceCalculatorUtil sInstance;
	Context mContext;
	ArrayList<PositionModel> mPositionList;   // 位置数据列表
    ArrayList<PositionTaskModel> mAllTasks;// 任务数据列表
	PositionModel mGpsPositionCalculated;
	long mLastCalculatedTime = 0;
	long mMinCalculatedTimeBettween = 30000; // GPS数据更新时，两次计算之间的最小时间间隔
	double mMinjumpDistance = 10.0f; // GPS数据更新时，能跳跃距离最小有效值，达到有效值时，两次计算之间的最小时间间隔阀值将被忽略。

    // 2. 私有构造器：禁止外部通过 new 关键字创建实例，确保单例唯一性
    private DistanceCalculatorUtil(Context context) {
        // 可选：初始化工具类依赖的资源（如配置参数、缓存等）
		mContext = context;

        LogUtils.d(TAG, "DistanceCalculatorUtil 单例实例初始化");
    }

    // 3. 公开静态方法：双重校验锁获取单例，兼顾线程安全与性能
    public static DistanceCalculatorUtil getInstance(Context context) {
        // 第一重校验：避免已创建实例时的频繁加锁，提升性能
        if (sInstance == null) {
            // 加锁：确保多线程下仅一个线程进入实例创建逻辑
            synchronized (DistanceCalculatorUtil.class) {
                // 第二重校验：防止多线程并发时重复创建实例
                if (sInstance == null) {
                    sInstance = new DistanceCalculatorUtil(context);
                }
            }
        }
        return sInstance;
    }

    // ---------------------- 以下可补充距离计算相关工具方法 ----------------------
    /**
     * 示例：Haversine 公式计算两点间距离（单位：米）
     * @param lat1 第一点纬度
     * @param lon1 第一点经度
     * @param lat2 第二点纬度
     * @param lon2 第二点经度
     * @return 两点间直线距离（米），计算失败返回 -1
     */
    public static double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        try {
            final double EARTH_RADIUS = 6371000; // 地球半径（米）
            // 角度转弧度
            double latDiff = Math.toRadians(lat2 - lat1);
            double lonDiff = Math.toRadians(lon2 - lon1);

            // Haversine 核心公式
            double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2)
				+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
				* Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            return EARTH_RADIUS * c; // 返回距离（米）
        } catch (Exception e) {
            LogUtils.d(TAG, "Haversine 距离计算失败：" + e.getMessage());
            return -1; // 标记计算失败
        }
    }


    /**
     * 计算两点间距离（Haversine公式，纯Java 7 基础API，无数学工具类依赖）
     * @param gpsLat GPS纬度
     * @param gpsLon GPS经度
     * @param posLat 目标位置纬度
     * @param posLon 目标位置经度
     * @return 两点间距离（单位：米）
     */
    /*private double calculateHaversineDistance(double gpsLat, double gpsLon, double posLat, double posLon) {
	 final double EARTH_RADIUS = 6371000; // 地球半径（米）
	 double latDiff = Math.toRadians(posLat - gpsLat);
	 double lonDiff = Math.toRadians(posLon - gpsLon);
	 // Haversine公式核心计算（Java 7 基础数学方法）
	 double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2)
	 + Math.cos(Math.toRadians(gpsLat)) * Math.cos(Math.toRadians(posLat))
	 * Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2);
	 double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	 return EARTH_RADIUS * c;
	 }*/

	/**
     * 校验所有任务触发条件（距离达标则触发任务通知）
     */
    public void checkAllTaskTriggerCondition(PositionModel currentGpsPosition) {
		if (currentGpsPosition == null) {
			LogUtils.d(TAG, "传入坐标参数为空，退出函数。");
			return;
		}

		// 计算频率控制模块
		//
		// 计算与最近一次GPS计算的时间间隔
		long nCalculatedTimeBettween = System.currentTimeMillis() - mLastCalculatedTime;
		// 计算跳跃距离
		double gpsPositionCalculatedLatitude = mGpsPositionCalculated == null ?0.0f: mGpsPositionCalculated.getLatitude();
		double gpsPositionCalculatedLongitude = mGpsPositionCalculated == null ?0.0f: mGpsPositionCalculated.getLongitude();
		double jumpDistance = calculateHaversineDistance(gpsPositionCalculatedLatitude, gpsPositionCalculatedLongitude, currentGpsPosition.getLatitude(), currentGpsPosition.getLongitude());
		if (jumpDistance < mMinjumpDistance) {
			LogUtils.d(TAG, String.format("checkAllTaskTriggerCondition：跳跃距离%f，小于50米。", jumpDistance));
			// 跳跃距离小于最小有效跳跃值
			if (nCalculatedTimeBettween < mMinCalculatedTimeBettween) {
				//间隔小于最小时间间隔设定
				LogUtils.d(TAG, String.format("checkAllTaskTriggerCondition：与最近一次计算间隔时间%d，坐标变化忽略。", nCalculatedTimeBettween));
				return;
			}
		}

		if (mGpsPositionCalculated == null) {
			mGpsPositionCalculated = currentGpsPosition;
			LogUtils.d(TAG, "最后计算位置记录为空，现在使用新坐标为初始化。");
		}

		LogUtils.d(TAG, String.format("checkAllTaskTriggerCondition：跳跃距离%f，与上次计算间隔%d，现在启动任务数据计算。", jumpDistance, nCalculatedTimeBettween));

		// 获取位置任务基础数据
		MainService mainService = MainService.getInstance(mContext);
		mPositionList = mainService.getPositionList();
		mAllTasks = mainService.getAllTasks();

		// 位置数据为空，跳过校验。
        if (mPositionList.isEmpty()) {
            LogUtils.d(TAG, "checkAllTaskTriggerCondition：位置数据为空，跳过距离计算。");
            return;
        }

		// 更新所有位置点的位置距离数据
		refreshRealPositionDistance(currentGpsPosition);

		// 任务数据为空，跳过校验。
        if (mAllTasks.isEmpty()) {
            LogUtils.d(TAG, "checkAllTaskTriggerCondition：任务数据为空，跳过任务提醒检查计算。");
            return;
        }

        // 迭代器遍历任务（Java 7 安全遍历，避免并发修改异常）
        Iterator<PositionTaskModel> taskIter = mAllTasks.iterator();
        while (taskIter.hasNext()) {
            PositionTaskModel task = taskIter.next();
            // 仅校验“已启用”且“绑定有效位置”的任务
            if (!task.isEnable() || TextUtils.isEmpty(task.getPositionId())) {
                continue;
            }

            // 查找任务绑定的位置（Java 7 迭代器遍历位置列表）
            PositionModel bindPos = null;
            Iterator<PositionModel> posIter = mPositionList.iterator();
            while (posIter.hasNext()) {
                PositionModel pos = posIter.next();
                if (task.getPositionId().equals(pos.getPositionId())) {
                    bindPos = pos;
                    break;
                }
            }
            if (bindPos == null) {
                LogUtils.w(TAG, "任务ID=" + task.getTaskId() + "：绑定位置不存在，跳过");
                task.setIsBingo(false);
                continue;
            }

			// 校验任务开始时间
			if (task.getStartTime() > System.currentTimeMillis()) {
				continue;
			}

            // 校验距离条件（判断是否满足任务触发阈值）
            double currentDistance = bindPos.getRealPositionDistance();
            if (currentDistance < 0) {
                LogUtils.w(TAG, "任务ID=" + task.getTaskId() + "：距离计算失败，跳过");
                task.setIsBingo(false);
                continue;
            }

            boolean isTriggered = false;
            int taskDistance = task.getDiscussDistance();
            // 任务触发条件：大于/小于指定距离（Java 7 基础判断，无三元运算符嵌套）
            if (task.isGreaterThan()) {
                isTriggered = currentDistance > taskDistance;
            } else if (task.isLessThan()) {
                isTriggered = currentDistance < taskDistance;
            }

            // 更新任务触发状态+发送通知（状态变化时才处理）
            if (task.isBingo() != isTriggered) {
                task.setIsBingo(isTriggered);
                if (isTriggered) {
                    MainService.getInstance(mContext).sendTaskTriggerNotification(task, bindPos, currentDistance);
                }
            }
        }

        MainService.getInstance(mContext).saveAllTasks(); // 持久化更新后的任务状态
		// 记录最后坐标更新点
		mGpsPositionCalculated = currentGpsPosition;
		// 记录数据计算时间
		mLastCalculatedTime = System.currentTimeMillis();
    }


    /**
     * 强制刷新所有位置距离（GPS更新后调用，计算距离+校验任务触发条件）
     */
    public void refreshRealPositionDistance(PositionModel currentGpsPosition) {
        // 遍历所有位置计算距离（Java 7 增强for循环，无Stream）
        for (PositionModel pos : mPositionList) {
            if (pos.isEnableRealPositionDistance()) {
				double distance = DistanceCalculatorUtil.calculateHaversineDistance(
					currentGpsPosition.getLatitude(),
					currentGpsPosition.getLongitude(),
					pos.getLatitude(),
					pos.getLongitude()
				);
				pos.setRealPositionDistance(distance);
            } else {
                pos.setRealPositionDistance(-1); // 未启用距离计算，标记为无效
            }
        }

        // 距离刷新后通知GPS监听者
        MainService.getInstance(mContext).notifyAllGpsListeners(currentGpsPosition);
    }
}
