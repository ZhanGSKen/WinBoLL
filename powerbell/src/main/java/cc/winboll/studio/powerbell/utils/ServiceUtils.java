package cc.winboll.studio.powerbell.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;
import cc.winboll.studio.libappbase.LogUtils;
import java.util.List;

/**
 * 服务状态工具类
 * 功能：判断指定服务是否处于运行状态
 * 适配：Java 7 + Android API 30
 * 注意：Android 8.0+ 对后台服务限制严格，此方法仅适用于前台服务或兼容场景
 */
public class ServiceUtils {
    // ================================== 静态常量区（置顶归类）=================================
    public static final String TAG = ServiceUtils.class.getSimpleName();
    // 最大查询服务数量
    private static final int MAX_RUNNING_SERVICES = 1000;

    // ================================== 核心工具方法（判断服务是否运行）=================================
    /**
     * 判断指定服务是否处于运行状态
     * @param context 上下文（建议使用 Application 上下文避免内存泄漏）
     * @param serviceName 服务完整类名（如：com.example.app.service.DemoService）
     * @return true-服务运行中，false-服务未运行或查询失败
     */
    public static boolean isServiceAlive(Context context, String serviceName) {
        LogUtils.d(TAG, "【isServiceAlive】调用开始 | 服务名称=" + serviceName);
        // 1. 前置参数校验
        if (context == null) {
            LogUtils.e(TAG, "【isServiceAlive】参数异常：Context 为空");
            return false;
        }
        if (TextUtils.isEmpty(serviceName)) {
            LogUtils.e(TAG, "【isServiceAlive】参数异常：服务名称为空");
            return false;
        }

        // 2. 获取 ActivityManager
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            LogUtils.e(TAG, "【isServiceAlive】获取 ActivityManager 失败");
            return false;
        }

        // 3. 查询正在运行的服务
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(MAX_RUNNING_SERVICES);
        if (runningServices == null || runningServices.size() <= 0) {
            LogUtils.d(TAG, "【isServiceAlive】正在运行的服务列表为空");
            return false;
        }

        // 4. 遍历服务列表，匹配目标服务
        for (ActivityManager.RunningServiceInfo serviceInfo : runningServices) {
            if (serviceInfo.service == null) {
                continue;
            }
            String className = serviceInfo.service.getClassName();
            if (serviceName.equals(className)) {
                LogUtils.d(TAG, "【isServiceAlive】服务运行中 | 匹配成功：" + serviceName);
                return true;
            }
        }

        LogUtils.d(TAG, "【isServiceAlive】服务未运行 | 未匹配到：" + serviceName);
        return false;
    }
}

