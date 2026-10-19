//package cc.winboll.studio.positions.services;
//
///**
// * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
// * @Date 2025/09/30 19:53
// * @Describe 位置距离服务：管理数据+定时计算距离+适配Adapter（Java 7 兼容）+ GPS信号加载
// */
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.os.Binder;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.os.Looper;
//import android.widget.Toast;
//import cc.winboll.studio.libappbase.LogUtils;
//import cc.winboll.studio.positions.adapters.PositionAdapter;
//import cc.winboll.studio.positions.models.AppConfigsModel;
//import cc.winboll.studio.positions.models.PositionModel;
//import cc.winboll.studio.positions.models.PositionTaskModel;
//import cc.winboll.studio.positions.utils.NotificationUtil;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.Set;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
///**
// * 核心职责：
// * 1. 实现 PositionAdapter.DistanceServiceInterface 接口，解耦Adapter与服务
// * 2. 单例式管理位置/任务数据，提供安全增删改查接口
// * 3. 后台单线程定时计算可见位置距离，主线程回调更新UI
// * 4. 内置GPS信号加载（通过LocationManager实时获取位置，解决“等待GPS信号”问题）
// * 5. 服务启动时启动前台通知（保活后台GPS功能，符合系统规范）
// * 6. 严格Java 7语法：无Lambda/Stream，显式迭代器/匿名内部类
// */
//public class DistanceRefreshService extends Service {
//    public static final String TAG = "DistanceRefreshService";
//	
//	
//    // 服务状态与配置
//    private boolean isServiceRunning = false;
//    
//    private static final int REFRESH_INTERVAL = 3; // 距离刷新间隔（秒）
//    // 前台通知相关：记录是否已启动前台服务（避免重复调用startForeground）
//    private boolean isForegroundServiceStarted = false;
//
//   
//    
//    // 服务绑定与UI回调
//    private final IBinder mBinder = new DistanceBinder();
//
//    
//
//
//    /**
//     * 在主线程显示Toast（避免子线程无法显示Toast的问题）
//     */
//    private void showToastOnMainThread(final String message) {
//        if (Looper.myLooper() == Looper.getMainLooper()) {
//            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//        } else {
//            new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
//					@Override
//					public void run() {
//						Toast.makeText(DistanceRefreshService.this, message, Toast.LENGTH_SHORT).show();
//					}
//				});
//        }
//    }
//
//    // ---------------------- Binder 内部类（供外部绑定服务） ----------------------
//    public class DistanceBinder extends Binder {
//        /**
//         * 外部绑定后获取服务实例（安全暴露服务引用）
//         */
//        public DistanceRefreshService getService() {
//            return DistanceRefreshService.this;
//        }
//    }
//
//    
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//		
//        // 初始化GPS管理器（提前获取系统服务，避免启动时延迟）
//        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        LogUtils.d(TAG, "服务 onCreate：初始化完成，等待启动命令");
//        run();
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        run();
//        AppConfigsModel bean = AppConfigsModel.loadBean(DistanceRefreshService.this, AppConfigsModel.class);
//        boolean isEnableService = (bean == null) ? false : bean.isEnableMainService();
//        // 服务启用时返回START_STICKY（被杀死后尝试重启），禁用时返回默认值
//        return isEnableService ? Service.START_STICKY : super.onStartCommand(intent, flags, startId);
//    }
//
//    public void run() {
//        // 仅服务未运行时启动（避免重复启动）
//        if (!isServiceRunning) {
//            isServiceRunning = true;
//			
//			
//			
//            startDistanceRefreshTask(); // 启动定时距离计算
//            startForegroundNotification(); // 启动前台通知
//            
//            LogUtils.d(TAG, "服务 onStartCommand：启动成功，刷新间隔=" + REFRESH_INTERVAL + "秒，前台通知+GPS已启动");
//        } else {
//            LogUtils.w(TAG, "服务 onStartCommand：已在运行，无需重复启动（前台通知：" + (isForegroundServiceStarted ? "已启动" : "未启动") + " | GPS：" + (isGpsEnabled ? "已开启" : "未开启") + "）");
//            // 异常场景恢复：补全未启动的组件
//            if (!isForegroundServiceStarted) {
//                startForegroundNotification();
//                LogUtils.d(TAG, "服务 run：前台通知未启动，已恢复");
//            }
//            if (isServiceRunning && !isGpsEnabled) {
//                startGpsLocation();
//                LogUtils.d(TAG, "服务 run：GPS未启动，已恢复");
//            }
//        }
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//		return null; // 按你的业务逻辑返回，无绑定需求则保留null
//        //LogUtils.d(TAG, "服务 onBind：外部绑定成功（运行状态：" + (isServiceRunning ? "是" : "否") + " | GPS状态：" + (isGpsEnabled ? "可用" : "不可用") + "）");
//        //return mBinder; // 返回Binder实例，供外部获取服务
//    }
//
//    /*@Override
//	 public boolean onUnbind(Intent intent) {
//	 LogUtils.d(TAG, "服务 onUnbind：外部解绑，清理回调与可见位置");
//	 // 解绑后清理资源，避免内存泄漏
//	 mDistanceReceiver = null;
//	 mVisiblePositionIds.clear();
//	 // 解绑时不停止GPS（服务仍在后台运行，需持续获取位置）
//	 return super.onUnbind(intent);
//	 }*/
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//		
//        LogUtils.d(TAG, "服务 onDestroy：销毁完成，资源已释放（GPS+前台通知+线程池）");
//    }
//
//    // ---------------------- 前台服务通知管理（与GPS状态联动优化） ----------------------
//    /**
//     * 启动前台服务通知（调用NotificationUtils创建通知，确保仅启动一次）
//     */
//    private void startForegroundNotification() {
//        // 1. 校验：避免重复调用startForeground（系统不允许重复启动）
//        if (isForegroundServiceStarted) {
//            LogUtils.w(TAG, "startForegroundNotification：前台通知已启动，无需重复执行");
//            return;
//        }
//
//        try {
//// 2. 初始化通知状态文本（根据GPS初始状态动态显示，避免固定“等待”）
//			String initialStatus;
//			if (isGpsPermissionGranted && isGpsEnabled) {
//				initialStatus = "GPS已就绪，正在获取位置（刷新间隔" + REFRESH_INTERVAL + "秒）";
//			} else if (!isGpsPermissionGranted) {
//				initialStatus = "缺少定位权限，无法获取GPS位置";
//			} else {
//				initialStatus = "GPS未开启，请在设置中打开";
//			}
//
//
//// 5. 标记前台服务已启动
//			isForegroundServiceStarted = true;
//			LogUtils.d(TAG, "startForegroundNotification：前台服务通知启动成功，初始状态：" + initialStatus);
//
//		} catch (Exception e) {
//// 捕获异常（如上下文失效、通知渠道未创建）
//			isForegroundServiceStarted = false;
//			LogUtils.d(TAG, "startForegroundNotification：前台通知启动失败" + e);
//		}
//	}
//
//
//
//	/**
//
//	 - 主线程回调Adapter更新UI（避免跨线程操作UI异常）
//	 */
//	/*private void notifyDistanceUpdateToUI(final String positionId) {
//	 if (Looper.myLooper() == Looper.getMainLooper()) {
//	 if (mDistanceReceiver != null) {
//	 mDistanceReceiver.onDistanceUpdate(positionId);
//	 }
//	 } else {
//	 new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
//	 @Override
//	 public void run() {
//	 if (mDistanceReceiver != null) {
//	 mDistanceReceiver.onDistanceUpdate(positionId);
//	 }
//	 }
//	 });
//	 }
//	 }*/
//
//
//
//
//
//// ---------------------- 实现 PositionAdapter.DistanceServiceInterface 接口 ----------------------
//
//	public ArrayList getPositionList() {
//		if (!isServiceRunning) {
//			LogUtils.w(TAG, "getPositionList：服务未运行，返回空列表");
//			return new ArrayList();
//		}
//		return new ArrayList(mPositionList);
//	}
//
//
//	public ArrayList getPositionTasksList() {
//		if (!isServiceRunning) {
//			LogUtils.w(TAG, "getPositionTasksList：服务未运行，返回空列表");
//			return new ArrayList();
//		}
//		return new ArrayList(mTaskList);
//	}
//
//	
//	
//
//	/*public void setOnDistanceUpdateReceiver(PositionAdapter.OnDistanceUpdateReceiver receiver) {
//		this.mDistanceReceiver = receiver;
//		LogUtils.d(TAG, "setOnDistanceUpdateReceiver：回调接收器已设置（" + (receiver != null ? "有效" : "无效") + "）");
//	}*/
//
//	public void addVisibleDistanceView(String positionId) {
//		if (!isServiceRunning || positionId == null) {
//			LogUtils.w(TAG, "addVisibleDistanceView：服务未运行/位置ID无效，添加失败");
//			return;
//		}
//		if (mVisiblePositionIds.add(positionId)) {
//			LogUtils.d(TAG, "addVisibleDistanceView：添加成功（位置ID=" + positionId + "，当前可见数=" + mVisiblePositionIds.size() + "）");
//// 新增：添加可见位置后，立即更新通知（显示最新可见数量）
//			if (isForegroundServiceStarted && mCurrentGpsPosition != null) {
//				syncGpsStatusToNotification();
//			}
//		}
//	}
//
//	public void removeVisibleDistanceView(String positionId) {
//		if (positionId == null) {
//			LogUtils.w(TAG, "removeVisibleDistanceView：位置ID为空，移除失败");
//			return;
//		}
//		if (mVisiblePositionIds.remove(positionId)) {
//			int remainingCount = mVisiblePositionIds.size();
//			LogUtils.d(TAG, "removeVisibleDistanceView：移除成功（位置ID=" + positionId + "，当前可见数=" + remainingCount + "）");
//// 新增：移除可见位置后，更新通知（同步数量变化）
//			if (isForegroundServiceStarted && mCurrentGpsPosition != null) {
//				syncGpsStatusToNotification();
//			}
//		}
//	}
//
//	public void clearVisibleDistanceViews() {
//		mVisiblePositionIds.clear();
//		LogUtils.d(TAG, "clearVisibleDistanceViews：所有可见位置已清空");
//// 新增：清空可见位置后，更新通知（提示计算暂停）
//		if (isForegroundServiceStarted) {
//			updateNotificationGpsStatus("无可见位置，距离计算暂停");
//		}
//	}
//
//// ---------------------- 数据管理接口（修复原有语法错误+优化逻辑） ----------------------
//	/**
//
//	 - 获取服务运行状态
//	 */
//	public boolean isServiceRunning() {
//		return isServiceRunning;
//	}
//
//	/**
//
//	 - 添加位置（修复迭代器泛型缺失问题）
//	 */
//	public void addPosition(PositionModel position) {
//		if (!isServiceRunning || position == null || position.getPositionId() == null) {
//			LogUtils.w(TAG, "addPosition：服务未运行/数据无效，添加失败");
//			return;
//		}// 修复：显式声明PositionModel泛型，避免类型转换警告
//		boolean isDuplicate = false;
//		Iterator posIter = mPositionList.iterator();
//		while (posIter.hasNext()) {
//			PositionModel existingPos = (PositionModel)posIter.next();
//			if (position.getPositionId().equals(existingPos.getPositionId())) {
//				isDuplicate = true;
//				break;
//			}
//		}if (!isDuplicate) {
//			mPositionList.add(position);
//			LogUtils.d(TAG, "addPosition：添加成功（位置ID=" + position.getPositionId() + "，总数=" + mPositionList.size() + "）");
//		} else {
//			LogUtils.w(TAG, "addPosition：位置ID=" + position.getPositionId() + "已存在，添加失败");
//		}
//	}
//
//	/**
//
//	 - 删除位置（修复任务删除时的类型转换错误）
//	 */
//	public void removePosition(String positionId) {
//		if (!isServiceRunning || positionId == null) {
//			LogUtils.w(TAG, "removePosition：服务未运行/位置ID无效，删除失败");
//			return;
//		}// 1. 删除位置
//		boolean isRemoved = false;
//		Iterator posIter = mPositionList.iterator();
//		while (posIter.hasNext()) {
//			PositionModel pos = (PositionModel)posIter.next();
//			if (positionId.equals(pos.getPositionId())) {
//				posIter.remove();
//				isRemoved = true;
//				break;
//			}
//		}if (isRemoved) {
//// 修复：任务列表迭代时用PositionTaskModel泛型（原错误用PositionModel导致转换失败）
//			Iterator taskIter = mTaskList.iterator();
//			while (taskIter.hasNext()) {
//				PositionTaskModel task = (PositionTaskModel)taskIter.next();
//				if (positionId.equals(task.getPositionId())) {
//					taskIter.remove();
//				}
//			}// 3. 移除可见位置
//			mVisiblePositionIds.remove(positionId);
//			LogUtils.d(TAG, "removePosition：删除成功（位置ID=" + positionId + "，剩余位置数=" + mPositionList.size() + "，剩余任务数=" + mTaskList.size() + "）");
//		} else {
//			LogUtils.w(TAG, "removePosition：位置ID=" + positionId + "不存在，删除失败");
//		}
//	}
//
//	/**
//
//	 - 更新位置信息（修复代码格式+迭代器泛型）
//	 */
//	public void updatePosition(PositionModel updatedPosition) {
//		if (!isServiceRunning || updatedPosition == null || updatedPosition.getPositionId() == null) {
//			LogUtils.w(TAG, "updatePosition：服务未运行/数据无效，更新失败");
//			return;
//		}boolean isUpdated = false;
//		Iterator posIter = mPositionList.iterator();
//		while (posIter.hasNext()) {
//			PositionModel pos = (PositionModel)posIter.next();
//			if (updatedPosition.getPositionId().equals(pos.getPositionId())) {
//				pos.setMemo(updatedPosition.getMemo());
//				pos.setIsEnableRealPositionDistance(updatedPosition.isEnableRealPositionDistance());
//				if (!updatedPosition.isEnableRealPositionDistance()) {
//					pos.setRealPositionDistance(-1);
//					//notifyDistanceUpdateToUI(pos.getPositionId());
//				}
//				isUpdated = true;
//				break;
//			}
//		}if (isUpdated) {
//			LogUtils.d(TAG, "updatePosition：更新成功（位置ID=" + updatedPosition.getPositionId() + "）");
//		} else {
//			LogUtils.w(TAG, "updatePosition：位置ID=" + updatedPosition.getPositionId() + "不存在，更新失败");
//		}
//	}
//
//	/**
//
//	 - 同步任务列表（修复泛型缺失+代码格式）
//	 */
//	public void syncAllPositionTasks(ArrayList tasks) {
//		if (!isServiceRunning || tasks == null) {
//			LogUtils.w(TAG, "syncAllPositionTasks：服务未运行/任务列表为空，同步失败");
//			return;
//		}// 1. 清空旧任务
//		mTaskList.clear();
//// 2. 添加新任务（修复泛型+去重逻辑）
//		Set taskIdSet = new HashSet();
//		Iterator taskIter = tasks.iterator();
//		while (taskIter.hasNext()) {
//			PositionTaskModel task = (PositionTaskModel)taskIter.next();
//			if (task != null && task.getTaskId() != null && !taskIdSet.contains(task.getTaskId())) {
//				taskIdSet.add(task.getTaskId());
//				mTaskList.add(task);
//			}
//		}LogUtils.d(TAG, "syncAllPositionTasks：同步成功（接收任务数=" + tasks.size() + "，去重后=" + mTaskList.size() + "）");
//	}
//
//
//
//
//// ---------------------- 补充：修复LocationProvider引用缺失问题（避免编译错误） ----------------------
//// 注：原代码中onStatusChanged使用LocationProvider枚举，需补充静态导入或显式声明
//// 此处通过内部静态类定义，解决系统API引用问题（兼容Java 7语法）
//	private static class LocationProvider {
//		public static final int AVAILABLE = 2;
//		public static final int OUT_OF_SERVICE = 0;
//		public static final int TEMPORARILY_UNAVAILABLE = 1;
//	}
//
//// ---------------------- 补充：Context引用工具（避免服务销毁后Context失效） ----------------------
//	/*private Context getSafeContext() {
//	 // 服务未销毁时返回自身Context，已销毁时返回应用Context（避免内存泄漏）
//	 if (isDestroyed()) {
//	 return getApplicationContext();
//	 }
//	 return this;
//	 }*/
//
//// 注：isDestroyed()为API 17+方法，若需兼容更低版本，可添加版本判断
//	/*private boolean isDestroyed() {
//	 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//	 return super.isDestroyed();
//	 }
//	 // 低版本通过状态标记间接判断（服务销毁时会置为false）
//	 return !isServiceRunning && !isForegroundServiceStarted;
//	 }*/
//}
