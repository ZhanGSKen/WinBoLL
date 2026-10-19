package cc.winboll.studio.positions.utils;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2024/07/19 14:30:57
 * @Describe 应用服务组件工具类
 */
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.positions.services.AssistantService;
import cc.winboll.studio.positions.services.MainService;
import java.util.List;

public class ServiceUtil {
    public final static String TAG = "ServiceUtil";

    public static boolean isServiceAlive(Context context, String szServiceName) {
        // 获取Activity管理者对象
        ActivityManager manager = (ActivityManager) context
            .getSystemService(Context.ACTIVITY_SERVICE);
        // 获取正在运行的服务（此处设置最多取1000个）
        List<ActivityManager.RunningServiceInfo> runningServices = manager
            .getRunningServices(1000);
        if (runningServices.size() <= 0) {
            return false;
        }
        // 遍历，若存在名字和传入的serviceName的一致则说明存在
        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServices) {
            if (runningServiceInfo.service.getClassName().equals(szServiceName)) {
                return true;
            }
        }

        return false;
    }

	public static void stopAutoService(Context context) {
		AppConfigsUtil appConfigsUtil = AppConfigsUtil.getInstance(context);
		appConfigsUtil.setIsEnableMainService(false);
		appConfigsUtil.saveConfigs();
		// 关闭并设置主服务
		Intent intent1 = new Intent(context, MainService.class);
		intent1.putExtra(MainService.EXTRA_IS_SETTING_TO_ENABLE, false);
		context.stopService(intent1); // 先停止旧服务
		context.startService(intent1); // 传入新的启动标志位，返回给系统
		// 关闭并设置主服务守护进程
		Intent intent2 = new Intent(context, AssistantService.class);
		intent2.putExtra(AssistantService.EXTRA_IS_SETTING_TO_ENABLE, false);
		context.stopService(intent2); // 先停止旧服务
		context.startService(intent2); // 传入新的启动标志位，返回给系统
		// 再次关闭所有服务
		context.stopService(intent1);
		context.stopService(intent2);

		LogUtils.d(TAG, "stopAutoService");
	}

	public static void startAutoService(Context context) {
		AppConfigsUtil appConfigsUtil = AppConfigsUtil.getInstance(context);
		appConfigsUtil.setIsEnableMainService(true);
		appConfigsUtil.saveConfigs();
		Intent intent = new Intent(context, MainService.class);
		intent.putExtra(MainService.EXTRA_IS_SETTING_TO_ENABLE, true);
		context.startService(intent);
		LogUtils.d(TAG, "startAutoService");
	}
}
