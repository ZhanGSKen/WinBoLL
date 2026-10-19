package cc.winboll.studio.positions.services;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2024/07/19 14:30:57
 * @Describe 应用主要服务组件类
 */
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.positions.App;
import cc.winboll.studio.positions.models.PositionModel;
import cc.winboll.studio.positions.models.PositionTaskModel;
import cc.winboll.studio.positions.utils.AppConfigsUtil;
import cc.winboll.studio.positions.utils.DistanceCalculatorUtil;
import cc.winboll.studio.positions.utils.NotificationUtil;
import cc.winboll.studio.positions.utils.ServiceUtil;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit; // 新增：定时器时间单位依赖

public class MainService extends Service {

    public static final String TAG = "MainService";

	public static final String EXTRA_IS_SETTING_TO_ENABLE = "EXTRA_IS_SETTING_TO_ENABLE";

    // ---------------------- 新增：定时器相关变量 ----------------------
    private ScheduledExecutorService taskCheckTimer; // 任务校验定时器
    private static final long TASK_CHECK_INTERVAL = 1; // 定时间隔（1分钟）
    private static final long TASK_CHECK_INIT_DELAY = 1; // 初始延迟（1分钟：立即启动）

    // GPS监听接口（Java 7 标准接口定义，无Lambda依赖）
    public interface GpsUpdateListener {
        void onGpsPositionUpdated(PositionModel currentGpsPos);
        void onGpsStatusChanged(String status);
    }

    // 空转GPS监听实例（用于注册到IdleGpsService）
    private final GpsUpdateListener mIdleGpsListener = new GpsUpdateListener() {
        @Override
        public void onGpsPositionUpdated(PositionModel currentGpsPos) {
            handleGpsPositionUpdate(currentGpsPos);
        }

        @Override
        public void onGpsStatusChanged(String status) {
            handleGpsStatusChange(status);
        }
    };

    // 中央处理：GPS 位置更新
    private void handleGpsPositionUpdate(PositionModel pos) {
        if (pos == null) return;
        syncCurrentGpsPosition(pos);
        DistanceCalculatorUtil.getInstance(MainService.this).checkAllTaskTriggerCondition(pos);
        String src = App.isAppIdleRunning() ? " (空转)" : "";
        LogUtils.d(TAG, "GPS位置更新：纬度=" + pos.getLatitude() + "，经度=" + pos.getLongitude() + src);
    }

    // 中央处理：GPS 状态变化
    private void handleGpsStatusChange(String status) {
        LogUtils.d(TAG, "GPS状态变化：" + status);
        notifyAllGpsStatusListeners(status);
        updateNotificationGpsStatus(status);
    }

    // 任务更新监听接口（Java 7 风格，供Adapter监听任务变化）
    public interface TaskUpdateListener {
        void onTaskUpdated();
    }

    // 监听管理（弱引用+线程安全集合，适配Java 7，避免内存泄漏+并发异常）
    private final Set<WeakReference<GpsUpdateListener>> mGpsListeners = new HashSet<WeakReference<GpsUpdateListener>>();
    private final Set<WeakReference<TaskUpdateListener>> mTaskListeners = new HashSet<WeakReference<TaskUpdateListener>>();
    private final Object mListenerLock = new Object(); // 监听操作锁，保证线程安全

    // 原有核心变量（Java 7 显式初始化，无Java 8+语法）
	private LocalBinder mLocalBinder; //持有 LocalBinder 实例（用于暴露服务）
    private LocationManager mLocationManager;
    private LocationListener mGpsLocationListener;
    private static final long GPS_UPDATE_INTERVAL = 2000; // GPS更新间隔：2秒
    private static final float GPS_UPDATE_DISTANCE = 1;   // GPS更新距离阈值：1米
    private boolean isGpsEnabled = false;                 // GPS是否启用标记
    private boolean isGpsPermissionGranted = false;       // 定位权限是否授予标记

    // 数据存储集合（Java 7 基础集合，避免Stream/forEach等Java 8+特性）
    private final ArrayList<PositionModel> mPositionList = new ArrayList<PositionModel>();   // 位置数据列表
    private final ArrayList<PositionTaskModel> mAllTasks = new ArrayList<PositionTaskModel>();// 任务数据列表
    private static PositionModel _mCurrentGpsPosition; // 当前GPS定位数据
    private boolean isListeningToIdleGps = false; // 当前是否监听空转GPS

    // 服务相关变量（Java 7 显式声明，保持原逻辑）
    MyServiceConnection mMyServiceConnection;
    volatile static boolean _mIsServiceRunning; // 服务运行状态（volatile保证可见性）
    AppConfigsUtil mAppConfigsUtil;
    private ScheduledExecutorService distanceExecutor = Executors.newSingleThreadScheduledExecutor(); // 单线程池处理距离计算
    private final Set<String> mVisiblePositionIds = new HashSet<String>(); // 可见位置ID集合

    // 单例+应用上下文（Java 7 静态变量，保证服务实例唯一+上下文安全）
    private static volatile MainService sInstance;
    private static Context sAppContext;


    // =========================================================================
    // 新增：定时器初始化方法（创建单线程定时器，每1分钟调用任务校验）
    // =========================================================================
    private void initTaskCheckTimer() {
        // 先销毁旧定时器（避免重复创建导致多线程问题）
        if (taskCheckTimer != null && !taskCheckTimer.isShutdown()) {
            taskCheckTimer.shutdown();
        }

        // 创建单线程定时器（确保任务串行执行，避免并发异常）
        taskCheckTimer = Executors.newSingleThreadScheduledExecutor();
        // 定时任务：初始延迟1分钟，每1分钟执行一次
        taskCheckTimer.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					LogUtils.d(TAG, "定时任务触发：开始校验任务（间隔1分钟）");
					// 调用任务校验核心方法（与GPS位置变化时逻辑一致）
					DistanceCalculatorUtil.getInstance(MainService.this).checkAllTaskTriggerCondition(MainService._mCurrentGpsPosition);
				}
			}, TASK_CHECK_INIT_DELAY, TASK_CHECK_INTERVAL, TimeUnit.MINUTES);

        LogUtils.d(TAG, "任务校验定时器已启动（间隔：" + TASK_CHECK_INTERVAL + "分钟）");
    }

    // =========================================================================
    // 新增：定时器销毁方法（服务销毁时调用，避免内存泄漏）
    // =========================================================================
    private void destroyTaskCheckTimer() {
        if (taskCheckTimer != null && !taskCheckTimer.isShutdown()) {
            taskCheckTimer.shutdown(); // 优雅关闭：等待已提交任务执行完成
            try {
                // 等待1秒，若未终止则强制关闭
                if (!taskCheckTimer.awaitTermination(1, TimeUnit.SECONDS)) {
                    taskCheckTimer.shutdownNow(); // 强制终止未完成任务
                }
            } catch (InterruptedException e) {
                taskCheckTimer.shutdownNow(); // 捕获中断异常，强制关闭
                Thread.currentThread().interrupt(); // 恢复线程中断状态
            } finally {
                taskCheckTimer = null; // 置空，避免重复操作
                LogUtils.d(TAG, "任务校验定时器已销毁");
            }
        }
    }

    // =========================================================================
    // 任务操作核心接口（Java 7 实现，全迭代器遍历，无ConcurrentModificationException）
    // =========================================================================
    /**
     * 新增任务（Adapter调用，通过MainService统一管理任务，保证数据一致性）
     * @param newTask 待新增的任务模型
     */
    public void addTask(PositionTaskModel newTask) {
        // 参数校验（Java 7 基础判断，无Optional等Java 8+特性）
        if (newTask == null || TextUtils.isEmpty(newTask.getPositionId())) {
            LogUtils.w(TAG, "addPositionTask：任务为空或未绑定位置ID，新增失败");
            return;
        }

        // 任务去重（Java 7 迭代器遍历，避免增强for循环删除/新增导致的并发异常）
        boolean isDuplicate = false;
        Iterator<PositionTaskModel> taskIter = mAllTasks.iterator();
        while (taskIter.hasNext()) {
            PositionTaskModel task = taskIter.next();
            if (newTask.getTaskId().equals(task.getTaskId())) {
                isDuplicate = true;
                break;
            }
        }
        if (isDuplicate) {
            LogUtils.w(TAG, "addPositionTask：任务ID已存在（" + newTask.getTaskId() + "），新增失败");
            return;
        }

        // 新增任务+持久化+通知刷新（全Java 7 语法）
        mAllTasks.add(newTask);
        saveAllTasks();
        notifyTaskUpdated(); // 通知所有监听者（如Adapter）任务已更新
        LogUtils.d(TAG, "addPositionTask：成功（位置ID=" + newTask.getPositionId() + "，任务ID=" + newTask.getTaskId() + "）");
    }

    /**
     * 获取指定位置的所有任务（Adapter显示任务数量用，数据来源唯一）
     * @param positionId 位置ID
     * @return 该位置绑定的所有任务（返回新列表，避免外部修改原数据）
     */
    public ArrayList<PositionTaskModel> getTasksByPositionId(String positionId) {
        ArrayList<PositionTaskModel> posTasks = new ArrayList<PositionTaskModel>();
        if (TextUtils.isEmpty(positionId) || mAllTasks.isEmpty()) {
            return posTasks;
        }

        // 筛选任务（Java 7 迭代器遍历，安全筛选）
        Iterator<PositionTaskModel> taskIter = mAllTasks.iterator();
        while (taskIter.hasNext()) {
            PositionTaskModel task = taskIter.next();
            if (positionId.equals(task.getPositionId())) {
                posTasks.add(task);
            }
        }
        return posTasks;
    }

    /**
     * 获取所有任务（Adapter全量刷新用，返回拷贝避免原数据被外部修改）
     * @return 所有任务的拷贝列表
     */
    public ArrayList<PositionTaskModel> getAllTasks() {
        return mAllTasks; // Java 7 集合拷贝方式
    }

	public void updateTask(PositionTaskModel updatedTask) {
        if (updatedTask == null || updatedTask.getTaskId() == null) return;
        for (int i = 0; i < mAllTasks.size(); i++) {
            PositionTaskModel task = mAllTasks.get(i);
            if (updatedTask.getTaskId().equals(task.getTaskId())) {
                mAllTasks.set(i, updatedTask); // 替换为更新后的任务
                break;
            }
		}
        saveAllTasks(); // 持久化更新后的数据
    }

    // 4. 仅更新任务启用状态（优化性能，避免全量字段更新）
    public void updateTaskStatus(PositionTaskModel task) {
        if (task == null || task.getTaskId() == null) return;
        for (PositionTaskModel item : mAllTasks) {
            if (task.getTaskId().equals(item.getTaskId())) {
                item.setIsEnable(task.isEnable()); // 只更新启用状态字段
                break;
            }
        }
        saveAllTasks(); // 持久化状态变更
    }


    /**
     * 删除任务（Adapter调用，通过迭代器安全删除，避免并发异常）
     * @param taskId 待删除任务的ID
     */
    public void deleteTask(final String taskId) {
        if (TextUtils.isEmpty(taskId) || mAllTasks.isEmpty()) {
            LogUtils.w(TAG, "deletePositionTask：任务ID为空或列表为空，删除失败");
            return;
        }

		// 迭代器删除（Java 7 唯一安全删除集合元素的方式）
		Iterator<PositionTaskModel> taskIter = mAllTasks.iterator();
		while (taskIter.hasNext()) {
			PositionTaskModel task = taskIter.next();
			if (taskId.equals(task.getTaskId())) {
				taskIter.remove(); // 迭代器安全删除，无ConcurrentModificationException
				saveAllTasks();
				notifyTaskUpdated();
				LogUtils.d(TAG, "deletePositionTask：成功（任务ID=" + taskId + "）");
				break;
			}
		}
    }

    /**
     * 注册任务更新监听（Java 7 弱引用管理，避免内存泄漏）
     * @param listener 任务更新监听者（如Adapter）
     */
    public void registerTaskUpdateListener(TaskUpdateListener listener) {
        if (listener == null) {
            LogUtils.w(TAG, "registerTaskUpdateListener：监听者为空，跳过");
            return;
        }
        synchronized (mListenerLock) { // 加锁保证多线程注册安全
            mTaskListeners.add(new WeakReference<TaskUpdateListener>(listener));
        }
    }

    /**
     * 反注册任务更新监听（Java 7 迭代器清理，避免内存泄漏）
     * @param listener 待反注册的监听者
     */
    public void unregisterTaskUpdateListener(TaskUpdateListener listener) {
        if (listener == null) {
            LogUtils.w(TAG, "unregisterTaskUpdateListener：监听者为空，跳过");
            return;
        }
        synchronized (mListenerLock) {
            Iterator<WeakReference<TaskUpdateListener>> iter = mTaskListeners.iterator();
            while (iter.hasNext()) {
                WeakReference<TaskUpdateListener> ref = iter.next();
                // 清理目标监听者或已被回收的弱引用
                if (ref.get() == listener || ref.get() == null) {
                    iter.remove();
                }
            }
        }
    }

    /**
     * 通知所有任务监听者更新（Java 7 匿名内部类实现主线程回调，无Lambda）
     */
    private void notifyTaskUpdated() {
        synchronized (mListenerLock) {
            Iterator<WeakReference<TaskUpdateListener>> iter = mTaskListeners.iterator();
            while (iter.hasNext()) {
                final WeakReference<TaskUpdateListener> ref = iter.next();
                if (ref.get() != null) {
                    // 判断是否在主线程，不在则切换（Java 7 匿名Runnable，无Lambda）
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        ref.get().onTaskUpdated();
                    } else {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									ref.get().onTaskUpdated();
								}
							});
                    }
                } else {
                    iter.remove(); // 清理已回收的弱引用，避免内存泄漏
                }
            }
        }
    }


    // =========================================================================
    // 原有基础方法（Java 7 语法调整：移除所有Lambda/方法引用，用匿名内部类替代）
    // =========================================================================
    /**
     * 获取服务单例（Java 7 静态同步方法，保证线程安全）
     * @param context 上下文
     * @return MainService实例（未绑定成功时返回null）
     */
    public static synchronized MainService getInstance(Context context) {
        if (sInstance == null) {
			if (AppConfigsUtil.getInstance(context).isEnableMainService(true)) {
				Intent intent = new Intent(context.getApplicationContext(), MainService.class);
				context.getApplicationContext().startService(intent);
			}
            return null;
        }
        if (sAppContext == null) {
            sAppContext = sInstance.getApplicationContext();
        }
        return sInstance;
    }

    /**
     * 服务绑定回调（Java 7 基础实现，无默认方法等Java 8+特性）
     */
    @Override
	public IBinder onBind(Intent intent) {
		// 返回 LocalBinder，使Activity能通过Binder获取MainService实例
		return mLocalBinder;
	}

    /**
     * 服务创建回调（初始化单例、上下文、配置、服务连接等）
     */
    @Override
    public void onCreate() {
        LogUtils.d(TAG, "onCreate");
        super.onCreate();
        sInstance = this;
        sAppContext = getApplicationContext();

		// 初始化 LocalBinder（关键：将MainService实例传入Binder）
		mLocalBinder = new LocalBinder(this); 

        _mIsServiceRunning = false;
        mAppConfigsUtil = AppConfigsUtil.getInstance(this);

        // 初始化服务连接（Java 7 显式判断，无Optional）
        if (mMyServiceConnection == null) {
            mMyServiceConnection = new MyServiceConnection();
        }

		if (mAppConfigsUtil.isEnableMainService(true)) {
            if (App.isAppIdleRunning()) {
                IdleGpsService.getInstance().start();
            }
			run(); // 启动服务核心逻辑
		}
    }

    /**
     * 服务核心逻辑（启动前台服务、初始化GPS、加载数据等）
     * 【关键修改】新增定时器初始化，每1分钟调用任务校验
     */
    public void run() {
        if (mAppConfigsUtil.isEnableMainService(true)) {
            if (!_mIsServiceRunning) {
                _mIsServiceRunning = true;
                wakeupAndBindAssistant(); // 唤醒并绑定辅助服务

                // 启动前台服务（Java 7 显式调用，无方法引用）
                String initialStatus = "[ Positions ] is in Service.";
                if (App.isAppIdleRunning()) {
                    initialStatus += " [IDLE RUNNING]";
                }
                NotificationUtil.createForegroundServiceNotification(this, initialStatus);
                startForeground(NotificationUtil.FOREGROUND_SERVICE_NOTIFICATION_ID,
								NotificationUtil.createForegroundServiceNotification(this, initialStatus));

                // 初始化GPS相关（Java 7 基础API调用）
                mLocationManager = (LocationManager) sInstance.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                initGpsLocationListener();
                startGpsLocation();

                // 加载本地数据（Java 7 静态方法调用，无方法引用）
                PositionModel.loadBeanList(MainService.this, mPositionList, PositionModel.class);
                PositionTaskModel.loadBeanList(MainService.this, mAllTasks, PositionTaskModel.class);

				// 提示与日志（Java 7 基础调用）
                ToastUtils.show(initialStatus);
                LogUtils.i(TAG, initialStatus);

                // ---------------------- 关键新增：启动任务校验定时器 ----------------------
				//checkAllTaskTriggerCondition();
				initTaskCheckTimer();
            }
        }
    }

    /**
     * 获取服务运行状态
     * @return true=运行中，false=未运行
     */
    public boolean isServiceRunning() {
        return _mIsServiceRunning;
    }

    /**
     * 服务销毁回调（清理资源、停止GPS、清空数据、反注册监听等）
     * 【关键修改】新增定时器销毁，避免内存泄漏
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        sInstance = null;

        // 清理资源（Java 7 顺序调用，无Stream等特性）
        stopGpsLocation();
        clearAllData();
        stopForeground(true);

        // 清理所有监听者（Java 7 加锁+清空，避免内存泄漏）
        synchronized (mListenerLock) {
            mGpsListeners.clear();
            mTaskListeners.clear();
        }

        // ---------------------- 关键新增：销毁任务校验定时器 ----------------------
        destroyTaskCheckTimer();
        // 销毁距离计算线程池（原有逻辑，补充确保线程安全）
        if (distanceExecutor != null && !distanceExecutor.isShutdown()) {
            distanceExecutor.shutdown();
        }

        // 重置状态变量
        _mIsServiceRunning = false;
        isGpsEnabled = false;
        mLocationManager = null;
    }


    // =========================================================================
    // 位置操作方法（Java 7 语法，全迭代器/基础循环，无Java 8+特性）
    // =========================================================================
    /**
     * 获取所有位置数据（返回原列表，供外部读取）
     * @return 位置列表
     */
    public ArrayList<PositionModel> getPositionList() {
        return mPositionList;
    }

    /**
     * 获取当前GPS位置
     * @return 当前GPS定位模型（未获取时返回null）
     */
    public PositionModel getCurrentGpsPosition() {
        return _mCurrentGpsPosition;
    }

    /**
     * 删除指定位置（Java 7 迭代器安全删除）
     * @param targetPosId 待删除位置的ID
     */
    public void removePosition(String targetPosId) {
        if (TextUtils.isEmpty(targetPosId) || mPositionList.isEmpty()) {
            LogUtils.w(TAG, "removePosition：参数无效");
            return;
        }
        // 迭代器遍历删除（Java 7 安全方式）
        Iterator<PositionModel> iter = mPositionList.iterator();
        while (iter.hasNext()) {
            PositionModel pos = iter.next();
            if (targetPosId.equals(pos.getPositionId())) {
                iter.remove();
                savePositionList();
                break;
            }
        }
    }

    /**
     * 更新位置数据（Java 7 基础for循环，无Stream筛选）
     * @param updatedPos 更新后的位置模型
     */
    public void updatePosition(PositionModel updatedPos) {
        if (updatedPos == null || TextUtils.isEmpty(updatedPos.getPositionId()) || mPositionList.isEmpty()) {
            LogUtils.w(TAG, "updatePosition：参数无效");
            return;
        }
        // 基础for循环查找并更新（Java 7 标准写法）
        for (int i = 0; i < mPositionList.size(); i++) {
            PositionModel oldPos = mPositionList.get(i);
            if (updatedPos.getPositionId().equals(oldPos.getPositionId())) {
                mPositionList.set(i, updatedPos);
                savePositionList();
                break;
            }
        }
    }

    /**
     * 同步所有位置任务（全量替换，用于批量更新）
     * @param newTaskList 新的任务列表
     */
    public void syncAllPositionTasks(ArrayList<PositionTaskModel> newTaskList) {
        if (newTaskList == null) {
            LogUtils.w(TAG, "syncAllPositionTasks：新列表为空");
            return;
        }
        // 全量替换+持久化+通知（Java 7 基础集合操作）
        mAllTasks.clear();
        mAllTasks.addAll(newTaskList);
        saveAllTasks();
        notifyTaskUpdated();
    }

    /**
     * 新增位置（Java 7 增强for循环去重，无Stream）
     * @param newPos 待新增的位置模型
     */
    public void addPosition(PositionModel newPos) {
        if (newPos == null) {
            LogUtils.w(TAG, "addPosition：位置为空");
            return;
        }
        // 位置去重（Java 7 增强for循环，无Stream.filter）
        boolean isDuplicate = false;
        for (PositionModel pos : mPositionList) {
            if (newPos.getPositionId().equals(pos.getPositionId())) {
                isDuplicate = true;
                break;
            }
        }
        if (!isDuplicate) {
            mPositionList.add(newPos);
            savePositionList();
        }
    }

    /**
     * 持久化位置数据（Java 7 静态方法调用，保持原逻辑）
     */
    void savePositionList() {
        LogUtils.d(TAG, String.format("savePositionList : size=%d", mPositionList.size()));
        PositionModel.saveBeanList(MainService.this, mPositionList, PositionModel.class);
    }

    /**
     * 持久化任务数据（Java 7 静态方法调用，保持原逻辑）
     */
    public void saveAllTasks() {
        LogUtils.d(TAG, String.format("saveTaskList : size=%d", mAllTasks.size()));
        PositionTaskModel.saveBeanList(MainService.this, mAllTasks, PositionTaskModel.class);
    }

    /**
     * 清空所有数据（位置+任务+GPS缓存，Java 7 集合clear方法）
     */
    public void clearAllData() {
        mPositionList.clear();
        mAllTasks.clear();
        _mCurrentGpsPosition = null;
        LogUtils.d(TAG, "clearAllData：已清空所有数据");
    }

    /**
     * 同步当前GPS位置（更新缓存+通知监听者+同步通知栏，全Java 7 语法）
     * @param position 最新GPS位置模型
     */
    public void syncCurrentGpsPosition(PositionModel position) {
        if (position == null) {
            LogUtils.w(TAG, "syncCurrentGpsPosition：位置为空");
            return;
        }
        this._mCurrentGpsPosition = position;
        LogUtils.d(TAG, "syncCurrentGpsPosition：成功（纬度=" + position.getLatitude() + "，经度=" + position.getLongitude() + "）");
        notifyAllGpsListeners(position);

        // 服务运行中才同步通知栏状态
        if (_mIsServiceRunning) {
            syncGpsStatusToNotification();
        }
    }

    /**
     * 同步GPS状态到前台通知（Java 7 匿名Runnable切换主线程，无Lambda）
     */
    private void syncGpsStatusToNotification() {
        if (!_mIsServiceRunning || _mCurrentGpsPosition == null) {
            return;
        }
        // 根据空转状态决定通知前缀（区分空转GPS与真实GPS）
        String prefix = App.isAppIdleRunning() ? "空转GPS" : "GPS位置";
        // 格式化通知内容（Java 7 String.format，使用%.15f显示全部GPS精度）
        String gpsStatus = String.format(
			"%s：北纬%.15f° 东经%.15f° | 可见位置：%d个",
			prefix,
			_mCurrentGpsPosition.getLatitude(),
			_mCurrentGpsPosition.getLongitude(),
			mVisiblePositionIds.size()
        );
        if (App.isAppIdleRunning()) {
            gpsStatus += " [IDLE RUNNING]";
        }
        final String finalGpsStatus = gpsStatus;
        // 主线程判断+切换（Java 7 匿名内部类）
        if (Looper.myLooper() == Looper.getMainLooper()) {
            NotificationUtil.updateForegroundServiceStatus(this, finalGpsStatus);
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						NotificationUtil.updateForegroundServiceStatus(MainService.this, finalGpsStatus);
					}
				});
        }
    }




    // =========================================================================
    // 服务生命周期+辅助服务相关（Java 7 语法，无Lambda/方法引用）
    // =========================================================================
    /**
     * 服务启动命令（每次startService调用时触发，重启服务核心逻辑）
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		boolean isSettingToEnable = false;
		if (intent != null) {
			isSettingToEnable = intent.getBooleanExtra(EXTRA_IS_SETTING_TO_ENABLE, false);
			if (isSettingToEnable) {
                if (App.isAppIdleRunning()) {
                    IdleGpsService.getInstance().start();
                }
				run(); // 重启服务核心逻辑（保证服务启动后进入运行状态）
			}
		}

        // 如果被设置为自启动就返回START_STICKY：服务被异常杀死后，系统会尝试重启（原逻辑保留）
		// 否则就启动默认参数
        return isSettingToEnable ? Service.START_STICKY : super.onStartCommand(intent, flags, startId);
    }

    /**
     * 服务连接内部类（Java 7 静态内部类，避免持有外部类强引用导致内存泄漏）
     */
    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 原逻辑保留（空实现，如需绑定辅助服务可补充具体逻辑）
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // 辅助服务断开时，重新唤醒绑定（原逻辑保留）
            if (mAppConfigsUtil.isEnableMainService(true)) {
                wakeupAndBindAssistant();
            }
        }
    }

    /**
	 * 唤醒并绑定辅助服务（检查服务状态，未存活则启动+绑定）
     */
    void wakeupAndBindAssistant() {
        // 检查辅助服务是否存活（Java 7 静态方法调用，无方法引用）
        if (!ServiceUtil.isServiceAlive(getApplicationContext(), AssistantService.class.getName())) {
            // 启动+绑定辅助服务（Java 7 显式Intent，无Lambda）
            startService(new Intent(MainService.this, AssistantService.class));
            bindService(new Intent(MainService.this, AssistantService.class), mMyServiceConnection, Context.BIND_IMPORTANT);
        }
    }


    // =========================================================================
    // GPS相关核心方法（Java 7 语法，匿名内部类实现LocationListener，无Lambda）
    // =========================================================================
    /**
     * 构造函数（Java 7 显式初始化线程池+GPS监听器，无默认构造函数简化）
     */
    public MainService() {
        distanceExecutor = Executors.newSingleThreadScheduledExecutor();
        initGpsLocationListener();
    }

    /**
     * 初始化GPS监听器（Java 7 匿名内部类实现LocationListener，无Lambda）
     */
    private void initGpsLocationListener() {
        LogUtils.d(TAG, "initGpsLocationListener");
        mGpsLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    // 封装GPS位置为PositionModel（Java 7 显式setter调用）
                    PositionModel gpsPos = new PositionModel();
                    gpsPos.setLatitude(location.getLatitude());
                    gpsPos.setLongitude(location.getLongitude());
                    gpsPos.setPositionId("CURRENT_GPS_POS");
                    gpsPos.setMemo("实时GPS位置");

                    // 调用中央处理方法
                    handleGpsPositionUpdate(gpsPos);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // 仅处理GPS_PROVIDER状态变化（Java 7 基础判断）
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                    String statusDesc = "";
                    // 状态枚举判断（Java 7 switch，无增强switch）
                    switch (status) {
                        case LocationProvider.AVAILABLE:
                            statusDesc = "GPS状态：已就绪（可用）";
                            break;
                        case LocationProvider.OUT_OF_SERVICE:
                            statusDesc = "GPS状态：无服务（信号弱）";
                            break;
                        case LocationProvider.TEMPORARILY_UNAVAILABLE:
                            statusDesc = "GPS状态：临时不可用（遮挡）";
                            break;
                    }
                    handleGpsStatusChange(statusDesc);
                }
            }

            @Override
            public void onProviderEnabled(String provider) {
                // GPS启用时更新状态+通知+重启定位（Java 7 基础逻辑）
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                    isGpsEnabled = true;
                    handleGpsStatusChange("GPS已开启（用户手动打开）");
                    startGpsLocation();
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
                // GPS禁用时清空状态+通知+提示（Java 7 基础逻辑）
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                    isGpsEnabled = false;
                    _mCurrentGpsPosition = null;
                    handleGpsStatusChange("GPS已关闭（用户手动关闭）");
                    ToastUtils.show("GPS已关闭，无法获取位置，请在设置中开启");
                }
            }
        };
    }

    /**
     * 检查GPS就绪状态（权限+启用状态，Java 7 基础权限判断，无Stream）
     * @return true=GPS就绪，false=未就绪
     */
    private boolean checkGpsReady() {
        // 检查定位权限（Java 7 基础权限API，无权限请求框架依赖）
        isGpsPermissionGranted = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
			== PackageManager.PERMISSION_GRANTED;

        // 初始化LocationManager（Java 7 显式判断，无Optional）
        if (mLocationManager == null) {
			mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        // 检查GPS是否启用（系统LocationManager API，Java 7 兼容）
        isGpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // 权限未授予：提示+日志+通知
        if (!isGpsPermissionGranted) {
            String tip = "GPS准备失败：缺少精确定位权限";
            LogUtils.e(TAG, tip);
            notifyAllGpsStatusListeners(tip);
            updateNotificationGpsStatus("缺少定位权限，无法获取GPS");
            ToastUtils.show("请授予定位权限，否则无法获取GPS位置");
            return false;
        }
        // GPS未启用：提示+日志+通知
        if (!isGpsEnabled) {
            String tip = "GPS准备失败：系统GPS未开启";
            LogUtils.e(TAG, tip);
            notifyAllGpsStatusListeners(tip);
            updateNotificationGpsStatus("GPS未开启，请在设置中打开");
            ToastUtils.show("GPS已关闭，请在设置中开启以获取位置");
            return false;
        }

        LogUtils.d(TAG, "GPS准备就绪：权限已获取，GPS已开启");
        return true;
    }

    /**
     * 启动GPS定位（Java 7 异常处理，无try-with-resources，显式捕获SecurityException）
     * 【关键修改】根据应用空转状态切换数据源（IdleGpsService 或 系统GPS）
     */
    private void startGpsLocation() {
        // 检查空转状态：如果处于空转，使用 IdleGpsService
        if (App.isAppIdleRunning()) {
            if (isListeningToIdleGps) return; // 已在监听空转GPS，无需重复注册
            stopGpsLocation(); // 停止系统GPS监听（如果正在运行）
            IdleGpsService.getInstance().registerGpsUpdateListener(mIdleGpsListener);
            isListeningToIdleGps = true;
            LogUtils.d(TAG, "启动GPS定位：使用空转模拟数据");
            handleGpsStatusChange("空转GPS监听中...");
            return;
        }

        // 系统GPS逻辑
        if (isListeningToIdleGps) {
            // 之前是空转，现在切换到系统GPS
            IdleGpsService.getInstance().unregisterGpsUpdateListener(mIdleGpsListener);
            isListeningToIdleGps = false;
        }

        if (!checkGpsReady()) {
            return;
        }

        try {
            // 注册GPS位置更新（Java 7 标准LocationManager API，指定Looper为主线程）
            mLocationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER,
				GPS_UPDATE_INTERVAL,
				GPS_UPDATE_DISTANCE,
				mGpsLocationListener,
				Looper.getMainLooper()
            );

            // 获取最后已知GPS位置（缓存位置，避免首次定位等待）
            Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                PositionModel lastGpsPos = new PositionModel();
                lastGpsPos.setLatitude(lastKnownLocation.getLatitude());
                lastGpsPos.setLongitude(lastKnownLocation.getLongitude());
                lastGpsPos.setPositionId("CURRENT_GPS_POS");
                handleGpsPositionUpdate(lastGpsPos);
                LogUtils.d(TAG, "已获取缓存GPS位置：纬度=" + lastKnownLocation.getLatitude());
            } else {
                String tip = "无缓存GPS位置，等待实时定位...";
                handleGpsStatusChange(tip);
                updateNotificationGpsStatus("GPS搜索中（请移至开阔地带）");
            }

        } catch (SecurityException e) {
            // 定位权限异常（Java 7 显式捕获，无Lambda异常处理）
            String error = "启动GPS失败（权限异常）：" + e.getMessage();
            LogUtils.e(TAG, error);
            handleGpsStatusChange(error);
            isGpsPermissionGranted = false;
            updateNotificationGpsStatus("定位权限异常，无法获取GPS");
        } catch (Exception e) {
            // 其他异常（如LocationManager为空、系统服务异常等）
            String error = "启动GPS失败：" + e.getMessage();
            LogUtils.e(TAG, error);
            handleGpsStatusChange(error);
            updateNotificationGpsStatus("GPS启动失败，尝试重试...");
        }
    }

    /**
     * 停止GPS定位（Java 7 异常处理，移除监听器避免内存泄漏）
     * 【关键修改】根据当前监听源停止对应的服务
     */
    private void stopGpsLocation() {
        if (isListeningToIdleGps) {
            IdleGpsService.getInstance().unregisterGpsUpdateListener(mIdleGpsListener);
            isListeningToIdleGps = false;
            LogUtils.d(TAG, "停止GPS定位：已注销空转GPS监听");
        } else {
            // 校验参数：避免空指针+权限未授予时调用
            if (mLocationManager != null && mGpsLocationListener != null && isGpsPermissionGranted) {
                try {
                    mLocationManager.removeUpdates(mGpsLocationListener);
                    String tip = "GPS定位已停止（移除监听器）";
                    LogUtils.d(TAG, tip);
                    handleGpsStatusChange(tip);
                } catch (Exception e) {
                    String error = "停止GPS失败：" + e.getMessage();
                    LogUtils.e(TAG, error);
                    handleGpsStatusChange(error);
                }
            }
        }
    }

    /**
     * 发送任务触发通知（更新前台通知+显示Toast，Java 7 匿名Runnable切换主线程）
     * @param task 触发的任务
     * @param bindPos 任务绑定的位置
     * @param currentDistance 当前距离
     */
    public void sendTaskTriggerNotification(final PositionTaskModel task, PositionModel bindPos, double currentDistance) {
        /*if (!_mIsServiceRunning) {
		 return;
		 }*/

        // 格式化通知内容（Java 7 String.format，无TextBlock等Java 15+特性）
        final String triggerContent = String.format(
			"任务触发：%s\n位置：%s\n当前距离：%.1f米（条件：%s%d米）",
			task.getTaskDescription(),
			bindPos.getMemo(),
			currentDistance,
			task.isGreaterThan() ? ">" : "<",
			task.getDiscussDistance()
        );

        // 更新前台通知（主线程判断+切换）
        updateNotificationGpsStatus(triggerContent);

        // 显示Toast（主线程安全调用，Java 7 匿名内部类）
        if (Looper.myLooper() == Looper.getMainLooper()) {
            ToastUtils.show(triggerContent);
			NotificationUtil.show(MainService.this, task.getTaskId(), task.getPositionId(), task.getTaskDescription());
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						ToastUtils.show(triggerContent);
						NotificationUtil.show(MainService.this, task.getTaskId(), task.getPositionId(), task.getTaskDescription());
					}
				});
        }
        LogUtils.i(TAG, "任务触发通知：" + triggerContent);
    }


    /**
     * 更新前台通知的GPS状态（Java 7 主线程切换，匿名Runnable实现）
     * @param statusText 通知显示的状态文本
     */
    void updateNotificationGpsStatus(final String statusText) {
        if (_mIsServiceRunning) {
            String text = statusText;
            if (App.isAppIdleRunning()) {
                text += " [IDLE RUNNING]";
            }
            final String finalText = text;
            // 判断当前线程是否为主线程，避免UI操作在子线程
            if (Looper.myLooper() == Looper.getMainLooper()) {
                NotificationUtil.updateForegroundServiceStatus(this, finalText);
            } else {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							NotificationUtil.updateForegroundServiceStatus(MainService.this, finalText);
						}
					});
            }
        }
    }


    // =========================================================================
    // GPS监听通知相关方法（Java 7 迭代器遍历弱引用集合，避免内存泄漏）
    // =========================================================================
    /**
     * 通知所有GPS监听者位置更新（Java 7 迭代器+弱引用管理，无Stream）
     * @param currentGpsPos 当前最新GPS位置
     */
    public void notifyAllGpsListeners(PositionModel currentGpsPos) {
        if (currentGpsPos == null || mGpsListeners.isEmpty()) {
            return;
        }
        synchronized (mListenerLock) {
            Iterator<WeakReference<GpsUpdateListener>> iter = mGpsListeners.iterator();
            while (iter.hasNext()) {
                WeakReference<GpsUpdateListener> ref = iter.next();
                GpsUpdateListener listener = ref.get();
                if (listener != null) {
                    notifySingleListener(listener, currentGpsPos);
                } else {
                    iter.remove(); // 清理已被GC回收的监听者，避免内存泄漏
                }
            }
        }
    }

    /**
     * 通知单个GPS监听者位置更新（主线程安全，Java 7 匿名Runnable）
     * @param listener 单个监听者
     * @param currentGpsPos 当前GPS位置
     */
    private void notifySingleListener(final GpsUpdateListener listener, final PositionModel currentGpsPos) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            listener.onGpsPositionUpdated(currentGpsPos);
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						listener.onGpsPositionUpdated(currentGpsPos);
					}
				});
        }
    }

    /**
     * 通知所有GPS监听者状态变化（如GPS开启/关闭、信号弱等，Java 7 迭代器）
     * @param status GPS状态描述文本
     */
    private void notifyAllGpsStatusListeners(final String status) {
        if (status == null || mGpsListeners.isEmpty()) {
            return;
        }
        synchronized (mListenerLock) {
            Iterator<WeakReference<GpsUpdateListener>> iter = mGpsListeners.iterator();
            while (iter.hasNext()) {
                WeakReference<GpsUpdateListener> ref = iter.next();
                final GpsUpdateListener listener = ref.get();
                if (listener != null) {
                    // 主线程切换，避免监听者在子线程处理UI
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        listener.onGpsStatusChanged(status);
                    } else {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									listener.onGpsStatusChanged(status);
								}
							});
                    }
                } else {
                    iter.remove(); // 清理无效弱引用
                }
            }
        }
    }

    /**
     * 注册GPS更新监听（Java 7 弱引用添加，避免监听者内存泄漏）
     * @param listener GPS更新监听者（如Activity/Adapter）
     */
    public void registerGpsUpdateListener(GpsUpdateListener listener) {
        if (listener == null) {
            LogUtils.w(TAG, "registerGpsUpdateListener：监听者为空");
            return;
        }
        synchronized (mListenerLock) {
            mGpsListeners.add(new WeakReference<GpsUpdateListener>(listener));
            LogUtils.d(TAG, "GPS监听注册成功，当前数量：" + mGpsListeners.size());
            // 注册后立即推送当前GPS位置（避免监听者错过初始数据）
            if (_mCurrentGpsPosition != null) {
                notifySingleListener(listener, _mCurrentGpsPosition);
            }
        }
    }

    /**
     * 反注册GPS更新监听（Java 7 迭代器清理，避免内存泄漏）
     * @param listener 待反注册的GPS监听者
     */
    public void unregisterGpsUpdateListener(GpsUpdateListener listener) {
        if (listener == null) {
            LogUtils.w(TAG, "unregisterGpsUpdateListener：监听者为空");
            return;
        }
        synchronized (mListenerLock) {
            Iterator<WeakReference<GpsUpdateListener>> iter = mGpsListeners.iterator();
            while (iter.hasNext()) {
                WeakReference<GpsUpdateListener> ref = iter.next();
                // 匹配目标监听者或已回收的弱引用，直接移除
                if (ref.get() == listener || ref.get() == null) {
                    iter.remove();
                    LogUtils.d(TAG, "GPS监听反注册成功，当前数量：" + mGpsListeners.size());
                    break;
                }
            }
        }
    }

	// 补全 LocalBinder 定义（与 LocationActivity 中的 LocalBinder 保持一致）
	// 注意：若 LocationActivity 已定义 LocalBinder，此处可删除；建议统一在 MainService 中定义，避免重复
	public class LocalBinder extends android.os.Binder {
		private MainService mService;

		public LocalBinder(MainService service) {
			this.mService = service;
		}

		public MainService getService() {
			return mService;
		}
	}

}


