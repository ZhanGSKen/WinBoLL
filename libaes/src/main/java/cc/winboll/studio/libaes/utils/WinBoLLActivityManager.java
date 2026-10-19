package cc.winboll.studio.libaes.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogActivity;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/05/10 10:02
 * @Describe 应用活动窗口管理器（改进版）
 * 核心能力：多任务窗口管理、Activity栈维护、任务前台恢复、批量关闭、前后Activity切换
 * 参考 ：android 类似微信小程序多任务窗口 及 设置 TaskDescription 修改 icon 和 label
 */
public class WinBoLLActivityManager {

    public static final String TAG = "WinBoLLActivityManager";
    public static final String EXTRA_TAG = "EXTRA_TAG";

    public enum WinBoLLUI_TYPE { APPLICATION, SERVICE } // 规范命名 大写开头

    private GlobalApplication mGlobalApplication;
    private static volatile WinBoLLActivityManager sInstance; // 单例命名规范
    private final Map<String, IWinBoLLActivity> mActivityListMap; // 私有不可变
    private static volatile WinBoLLUI_TYPE sWinBoLLUI_TYPE = WinBoLLUI_TYPE.SERVICE;

    // 私有构造 杜绝外部实例化
    private WinBoLLActivityManager(@NonNull GlobalApplication application) {
        mGlobalApplication = application;
        mActivityListMap = new HashMap<>(); // 菱形泛型简化
    }

    /**
     * 初始化管理器（必须在Application onCreate中调用）
     */
    public static <T extends GlobalApplication> void init(@NonNull T application) {
        if (sInstance == null) {
            synchronized (WinBoLLActivityManager.class) {
                if (sInstance == null) {
                    sInstance = new WinBoLLActivityManager(application);
                }
            }
        }
    }

    /**
     * 获取单例（需先调用init初始化，否则抛异常）
     */
    @NonNull
    public static WinBoLLActivityManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("WinBoLLActivityManager 未初始化，请先在Application中调用 init()");
        }
        return sInstance;
    }

    // ===================== 基础配置 =====================
    public static void setWinBoLLUI_TYPE(@NonNull WinBoLLUI_TYPE winBoLLUI_TYPE) {
        sWinBoLLUI_TYPE = winBoLLUI_TYPE;
    }

    @NonNull
    public static WinBoLLUI_TYPE getWinBoLLUI_TYPE() {
        return sWinBoLLUI_TYPE;
    }

    // ===================== Activity 增删查 =====================
    /**
     * 把Activity添加到管理中（自动去重）
     */
    public <T extends IWinBoLLActivity> void add(@NonNull T activity) {
        String tag = activity.getTag();
        if (isActivityActive(tag)) {
            LogUtils.d(TAG, String.format("Activity[%s] 已处于活跃状态，无需重复添加", tag));
            return;
        }
        mActivityListMap.put(tag, activity);
        LogUtils.d(TAG, String.format("添加Activity：%s，当前管理数量：%d", tag, mActivityListMap.size()));
    }

    /**
     * 判断指定Tag的Activity是否活跃
     */
    public boolean isActivityActive(@NonNull String tag) {
        return mActivityListMap.containsKey(tag) && mActivityListMap.get(tag) != null;
    }

    /**
     * 根据Tag获取Activity（空安全）
     */
    @Nullable
    public Activity getActivityByTag(@NonNull String tag) {
        IWinBoLLActivity winBoLLActivity = mActivityListMap.get(tag);
        if (winBoLLActivity == null) return null;
        Activity activity = winBoLLActivity.getActivity();
        // 过滤已销毁/已结束的Activity
        if (activity == null || activity.isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed())) {
            registeRemove(winBoLLActivity);
            return null;
        }
        return activity;
    }

    /**
     * 移除指定Activity（销毁时调用）
     */
    public <T extends IWinBoLLActivity> boolean registeRemove(@NonNull T iWinBoLLActivity) {
        String tag = iWinBoLLActivity.getTag();
        if (mActivityListMap.containsKey(tag)) {
            mActivityListMap.remove(tag);
            LogUtils.d(TAG, String.format("移除Activity：%s，剩余管理数量：%d", tag, mActivityListMap.size()));
            return true;
        }
        return false;
    }

    // ===================== Activity 启动 =====================
    /**
     * 启动WinBoLLActivity（存在则前台恢复，不存在则新建多任务窗口）
     */
    public <T extends IWinBoLLActivity> void startWinBoLLActivity(@NonNull Context context, @NonNull Class<T> clazz) {
        if (!resumeActivity(clazz)) {
            Intent intent = new Intent(context, clazz);
            setMultiTaskFlags(intent);
            context.startActivity(intent);
        }
    }

    /**
     * 带Intent参数启动WinBoLLActivity
     */
    public <T extends IWinBoLLActivity> void startWinBoLLActivity(@NonNull Context context, @NonNull Intent intent, @NonNull Class<T> clazz) {
        if (!resumeActivity(clazz)) {
            setMultiTaskFlags(intent);
            context.startActivity(intent);
        }
    }

    /**
     * 启动日志页面（固定多任务模式）
     */
    public void startLogActivity(@NonNull Context context) {
        Intent intent = new Intent(context, LogActivity.class);
        setMultiTaskFlags(intent);
        intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT); // 分屏相关
        context.startActivity(intent);
    }

    /**
     * 设置多任务窗口通用Flags
     */
    private void setMultiTaskFlags(@NonNull Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
    }

    // ===================== Activity 前台恢复 =====================
    /**
     * 根据Activity类 恢复前台（反射获取Tag，需保证无参构造）
     */
    public <T extends IWinBoLLActivity> boolean resumeActivity(@NonNull Class<T> clazz) {
        try {
            T instance = clazz.newInstance();
            return resumeActivity(instance.getTag());
        } catch (InstantiationException | IllegalAccessException e) {
            LogUtils.e(TAG, "恢复Activity失败，类需提供无参构造", e);
        }
        return false;
    }

    /**
     * 根据Tag 恢复Activity前台
     */
    public boolean resumeActivity(@NonNull String tag) {
        Activity activity = getActivityByTag(tag);
        return activity != null && resumeActivity(activity);
    }

    /**
     * 恢复指定Activity到前台（适配高版本权限）
     */
    @SuppressWarnings("deprecation")
    public boolean resumeActivity(@NonNull Activity activity) {
        if (activity.isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed())) {
            return false;
        }
        try {
            ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
            if (am == null) {
                LogUtils.w(TAG, "获取ActivityManager失败，无法恢复前台");
                return false;
            }
            // Android 11+ 限制，低版本正常使用
            am.moveTaskToFront(activity.getTaskId(), ActivityManager.MOVE_TASK_NO_USER_ACTION);
			//ToastUtils.show(String.format("Activity[%s] 已恢复到前台", activity.getClass().getSimpleName()));
            LogUtils.d(TAG, String.format("Activity[%s] 已恢复到前台", activity.getClass().getSimpleName()));
            return true;
        } catch (SecurityException e) {
			//ToastUtils.show("恢复Activity前台失败，缺少权限或系统限制 ：" + e.getMessage());
            LogUtils.e(TAG, "恢复Activity前台失败，缺少权限或系统限制", e);
            //ToastUtils.show("窗口恢复失败，请手动打开");
            return false;
        }
    }

    // ===================== Activity 关闭 =====================
    /**
     * 结束所有管理的Activity（按UI类型选择关闭策略）
     */
    public void finishAll() {
        if (mActivityListMap.isEmpty()) {
            LogUtils.d(TAG, "当前无管理的Activity，无需结束");
            return;
        }
        LogUtils.d(TAG, String.format("开始结束所有Activity，共%d个", mActivityListMap.size()));
        Iterator<Map.Entry<String, IWinBoLLActivity>> iterator = mActivityListMap.entrySet().iterator();
        while (iterator.hasNext()) {
            IWinBoLLActivity winBoLLActivity = iterator.next().getValue();
            Activity activity = winBoLLActivity.getActivity();
            if (activity == null) {
                iterator.remove();
                continue;
            }
            // 安全关闭，避免重复操作
            if (!activity.isFinishing() && !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed())) {
                if (sWinBoLLUI_TYPE == WinBoLLUI_TYPE.SERVICE) {
                    activity.finishAndRemoveTask(); // 结束+移除最近任务
                } else if (sWinBoLLUI_TYPE == WinBoLLUI_TYPE.APPLICATION) {
                    activity.finish(); // 仅结束页面
                }
            }
            iterator.remove(); // 移除已处理的项
        }
        LogUtils.d(TAG, "所有Activity结束完成");
    }

    /**
     * 结束指定Activity，自动恢复上一个Activity前台
     */
    public <T extends IWinBoLLActivity> void finish(@NonNull T iWinBoLLActivity) {
        Activity currentActivity = iWinBoLLActivity.getActivity();
        if (currentActivity == null || currentActivity.isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && currentActivity.isDestroyed())) {
            registeRemove(iWinBoLLActivity);
            return;
        }

        // 先获取上一个Activity，再关闭当前
        Activity preActivity = getPreActivity(iWinBoLLActivity);
        currentActivity.finish();
        registeRemove(iWinBoLLActivity); // 关闭后移除管理

        // 恢复上一个Activity前台
        if (preActivity != null) {
            resumeActivity(preActivity);
        }
    }

    /**
     * 获取当前Activity的上一个栈内Activity（修复原遍历逻辑错误）
     */
    @Nullable
    private Activity getPreActivity(@NonNull IWinBoLLActivity currentActivity) {
        String currentTag = currentActivity.getTag();
        IWinBoLLActivity preWinBoLLActivity = null;
        for (Map.Entry<String, IWinBoLLActivity> entry : mActivityListMap.entrySet()) {
            String tag = entry.getKey();
            if (Objects.equals(tag, currentTag)) {
                break; // 找到当前Activity，循环终止，pre即为上一个
            }
            preWinBoLLActivity = entry.getValue();
        }
        return preWinBoLLActivity != null ? preWinBoLLActivity.getActivity() : null;
    }

    // ===================== 调试辅助 =====================
    /**
     * 打印所有管理的Activity信息（调试用）
     */
    public void printActivityListInfo() {
        if (mActivityListMap.isEmpty()) {
            LogUtils.d(TAG, "当前管理的Activity列表为空");
            return;
        }
        StringBuilder sb = new StringBuilder(String.format("Activity管理列表（总数：%d）\n", mActivityListMap.size()));
        for (Map.Entry<String, IWinBoLLActivity> entry : mActivityListMap.entrySet()) {
            sb.append("Tag: ").append(entry.getKey())
				.append(" | Activity: ").append(entry.getValue().getActivity().getClass().getSimpleName())
				.append("\n");
        }
        LogUtils.d(TAG, sb.toString());
    }
}

