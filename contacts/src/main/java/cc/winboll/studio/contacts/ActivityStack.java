package cc.winboll.studio.contacts;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import cc.winboll.studio.libappbase.LogUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/02/13 06:58:04
 * @Describe Activity 栈管理工具，统一管理应用内 Activity 生命周期
 * 适配：Java7 + Android API29-30 + 小米机型，优化并发安全与通话场景稳定性
 */
public class ActivityStack {
    // 常量定义（核心标识+版本兼容常量）
    public static final String TAG = "ActivityStack";
    private static final int API_VERSION_O = 26; // Android 8.0 API26（isDestroyed适配用）

    // 单例与核心成员变量（按优先级排序）
    private static final ActivityStack INSTANCE = new ActivityStack();
    // 替换为ArrayList+同步锁：解决CopyOnWriteArrayList迭代器不能删除的崩溃，兼顾并发安全
    private final List<Activity> mActivityList = new ArrayList<Activity>();
    private final Handler mMainHandler = new Handler(Looper.getMainLooper()); // 复用主线程Handler，避免内存泄漏

    // 单例对外暴露方法
    public static ActivityStack getInstance() {
        return INSTANCE;
    }

    // 私有构造，禁止外部实例化
    private ActivityStack() {
        LogUtils.d(TAG, "ActivityStack 初始化完成");
    }

    // ====================== 栈基础操作（添加/移除） ======================
    /**
     * 添加Activity到栈中，避免重复入栈
     * @param activity 待添加的Activity
     */
    public void addActivity(Activity activity) {
        if (activity == null) {
            LogUtils.w(TAG, "addActivity: activity is null, skip");
            return;
        }
        // 同步锁：解决多线程并发添加冲突（小米机型多线程场景适配）
        synchronized (mActivityList) {
            if (!mActivityList.contains(activity)) {
                mActivityList.add(activity);
                LogUtils.d(TAG, "addActivity: " + activity.getClass().getSimpleName() + ", stack size: " + mActivityList.size());
            }
        }
    }

    /**
     * 移除Activity（不销毁，用于正常退出场景）
     * @param activity 待移除的Activity
     */
    public void removeActivity(Activity activity) {
        if (activity == null) {
            LogUtils.w(TAG, "removeActivity: activity is null, skip");
            return;
        }
        synchronized (mActivityList) {
            if (mActivityList.remove(activity)) {
                LogUtils.d(TAG, "removeActivity: " + activity.getClass().getSimpleName() + ", stack size: " + mActivityList.size());
            }
        }
    }

    // ====================== Activity状态查询（获取/判断存活） ======================
    /**
     * 获取栈顶有效Activity（迭代遍历替代递归，避免栈溢出，适配小米多页面场景）
     * @return 栈顶有效Activity，无则返回null
     */
    public Activity getTopActivity() {
        synchronized (mActivityList) {
            if (mActivityList.isEmpty()) {
                LogUtils.w(TAG, "getTopActivity: stack is empty, return null");
                return null;
            }

            Activity validTopActivity = null;
            // 倒序遍历，优先取最顶层有效Activity，同时清理无效残留
            for (int i = mActivityList.size() - 1; i >= 0; i--) {
                Activity activity = mActivityList.get(i);
                // 版本兼容校验：API26+才支持isDestroyed
                if (activity != null && !activity.isFinishing() && (getSdkVersion() < API_VERSION_O || !activity.isDestroyed())) {
                    validTopActivity = activity;
                    break;
                } else {
                    mActivityList.remove(i);
                    String className = (activity != null) ? activity.getClass().getSimpleName() : "null";
                    LogUtils.w(TAG, "getTopActivity: remove invalid activity: " + className);
                }
            }

            if (validTopActivity != null) {
                LogUtils.d(TAG, "getTopActivity: top activity: " + validTopActivity.getClass().getSimpleName());
            }
            return validTopActivity;
        }
    }

    /**
     * 获取指定类的有效Activity实例（通话场景核心方法，判断页面是否存活）
     * @param activityClass 目标Activity类
     * @return 有效实例，无则返回null
     */
    public Activity getActivity(Class<?> activityClass) {
        if (activityClass == null) {
            LogUtils.w(TAG, "getActivity: activityClass is null, return null");
            return null;
        }
        synchronized (mActivityList) {
            if (mActivityList.isEmpty()) {
                LogUtils.w(TAG, "getActivity: stack empty, return null");
                return null;
            }

            for (Activity activity : mActivityList) {
                if (activity != null && activity.getClass().equals(activityClass) && !activity.isFinishing() && (getSdkVersion() < API_VERSION_O || !activity.isDestroyed())) {
                    LogUtils.d(TAG, "getActivity: find valid activity: " + activityClass.getSimpleName());
                    return activity;
                }
            }
            LogUtils.w(TAG, "getActivity: no valid activity: " + activityClass.getSimpleName());
            return null;
        }
    }

    /**
     * 判断指定Activity是否存活（简化通话场景调用，避免重复判空）
     * @param activityClass 目标Activity类
     * @return true：存活，false：未存活
     */
    public boolean isActivityAlive(Class<?> activityClass) {
        boolean isAlive = getActivity(activityClass) != null;
        LogUtils.d(TAG, "isActivityAlive: " + activityClass.getSimpleName() + ", result: " + isAlive);
        return isAlive;
    }

    // ====================== Activity销毁操作（单/批量/全部） ======================
    /**
     * 销毁栈顶Activity（主线程执行，适配小米机型线程限制）
     */
    public void finishTopActivity() {
        runOnMainThread(new Runnable() {
				@Override
				public void run() {
					synchronized (mActivityList) {
						if (mActivityList.isEmpty()) {
							LogUtils.w(TAG, "finishTopActivity: stack is empty, skip");
							return;
						}

						// 先移除再校验，避免并发冲突（小米多线程场景适配）
						Activity topActivity = mActivityList.remove(mActivityList.size() - 1);
						if (topActivity == null) {
							LogUtils.w(TAG, "finishTopActivity: top activity is null, skip");
							return;
						}

						if (!topActivity.isFinishing() && (getSdkVersion() < API_VERSION_O || !topActivity.isDestroyed())) {
							topActivity.finish();
							LogUtils.d(TAG, "finishTopActivity: destroy top activity: " + topActivity.getClass().getSimpleName() + ", stack size: " + mActivityList.size());
						}
					}
				}
			});
    }

    /**
     * 销毁指定Activity（主线程执行，避免跨线程异常）
     * @param activity 待销毁的Activity
     */
    public void finishActivity(final Activity activity) {
        runOnMainThread(new Runnable() {
				@Override
				public void run() {
					if (activity == null) {
						LogUtils.w(TAG, "finishActivity: activity is null, skip");
						return;
					}
					synchronized (mActivityList) {
						if (mActivityList.contains(activity) && !activity.isFinishing() && (getSdkVersion() < API_VERSION_O || !activity.isDestroyed())) {
							mActivityList.remove(activity);
							activity.finish();
							LogUtils.d(TAG, "finishActivity: destroy activity: " + activity.getClass().getSimpleName() + ", stack size: " + mActivityList.size());
						}
					}
				}
			});
    }

    /**
     * 销毁指定类的所有Activity（核心修复：迭代器删除崩溃，通话场景核心）
     * @param activityClass 目标Activity类
     */
    public void finishActivity(final Class<?> activityClass) {
        runOnMainThread(new Runnable() {
				@Override
				public void run() {
					if (activityClass == null) {
						LogUtils.w(TAG, "finishActivity: activityClass is null, skip");
						return;
					}
					synchronized (mActivityList) {
						if (mActivityList.isEmpty()) {
							LogUtils.w(TAG, "finishActivity: stack empty, skip");
							return;
						}

						// 核心修复：用索引遍历+倒序删除，替代迭代器删除（避免UnsupportedOperationException）
						for (int i = mActivityList.size() - 1; i >= 0; i--) {
							Activity activity = mActivityList.get(i);
							if (activity != null && activity.getClass().equals(activityClass)) {
								if (!activity.isFinishing() && (getSdkVersion() < API_VERSION_O || !activity.isDestroyed())) {
									mActivityList.remove(i); // 索引删除，支持ArrayList
									activity.finish();
									LogUtils.d(TAG, "finishActivity: destroy class activity: " + activityClass.getSimpleName() + ", stack size: " + mActivityList.size());
								} else {
									mActivityList.remove(i); // 清理无效残留
								}
							}
						}
					}
				}
			});
    }

    /**
     * 销毁栈中所有Activity（退出应用/清空栈场景用）
     */
    public void finishAllActivity() {
        runOnMainThread(new Runnable() {
				@Override
				public void run() {
					synchronized (mActivityList) {
						if (mActivityList.isEmpty()) {
							LogUtils.w(TAG, "finishAllActivity: stack is empty, skip");
							return;
						}

						// 遍历销毁所有有效Activity，逐个状态校验（小米机型稳定性适配）
						for (Activity activity : mActivityList) {
							if (activity != null && !activity.isFinishing() && (getSdkVersion() < API_VERSION_O || !activity.isDestroyed())) {
								activity.finish();
								LogUtils.d(TAG, "finishAllActivity: destroy activity: " + activity.getClass().getSimpleName());
							}
						}
						mActivityList.clear();
						LogUtils.d(TAG, "finishAllActivity: all activity destroyed, stack cleared");
					}
				}
			});
    }

    // ====================== 栈优化与工具方法 ======================
    /**
     * 清理栈中所有无效Activity（null/已销毁/已结束），优化小米机型内存占用
     */
    public void clearInvalidActivities() {
        runOnMainThread(new Runnable() {
				@Override
				public void run() {
					synchronized (mActivityList) {
						if (mActivityList.isEmpty()) {
							return;
						}

						// 倒序索引删除，避免遍历过程中索引错乱
						for (int i = mActivityList.size() - 1; i >= 0; i--) {
							Activity activity = mActivityList.get(i);
							if (activity == null || activity.isFinishing() || (getSdkVersion() >= API_VERSION_O && activity.isDestroyed())) {
								mActivityList.remove(i);
								String className = (activity != null) ? activity.getClass().getSimpleName() : "null";
								LogUtils.d(TAG, "clearInvalidActivities: remove invalid activity: " + className);
							}
						}
						LogUtils.d(TAG, "clearInvalidActivities: done, stack size: " + mActivityList.size());
					}
				}
			});
    }

    /**
     * 确保任务在主线程执行（Activity操作必须主线程，小米机型严格限制）
     * @param runnable 待执行任务
     */
    private void runOnMainThread(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        // 避免不必要的线程切换，优化性能（小米机型流畅度适配）
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            mMainHandler.post(runnable);
            LogUtils.d(TAG, "runOnMainThread: post task to main thread");
        }
    }

    /**
     * 辅助方法：获取当前系统SDK版本（简化版本判断逻辑，统一调用）
     * @return SDK版本号
     */
    private int getSdkVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }
}

