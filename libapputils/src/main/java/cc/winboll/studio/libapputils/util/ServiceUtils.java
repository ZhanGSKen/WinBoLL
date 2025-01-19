package cc.winboll.studio.libapputils.util;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/12/08 20:09:02
 * @Describe  应用服务组件工具类
 */
import android.app.ActivityManager;
import android.content.Context;
import java.util.List;

public class ServiceUtils {
    
    public static final String TAG = "ServiceUtils";
    
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
    
}
