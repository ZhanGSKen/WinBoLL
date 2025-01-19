package cc.winboll.studio.powerbell.receivers;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/06/06 15:01:39
 * @Describe 应用广播消息接收类
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import cc.winboll.studio.powerbell.GlobalApplication;
import cc.winboll.studio.powerbell.services.ControlCenterService;
import cc.winboll.studio.powerbell.utils.ServiceUtils;
import cc.winboll.studio.shared.log.LogUtils;

public class MainReceiver extends BroadcastReceiver {

    public static final String TAG = "MainReceiver";

    static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    // 存储电量指示值，
    // 用于校验电量消息时的电量变化
    static volatile int _mnTheQuantityOfElectricityOld = -1;

    @Override
    public void onReceive(Context context, Intent intent) {
        String szAction = intent.getAction();
        if (szAction.equals(ACTION_BOOT_COMPLETED)) {
            boolean isEnableService = GlobalApplication.getAppConfigUtils(context).getIsEnableService();
            if (isEnableService) {
                if (ServiceUtils.isServiceAlive(context.getApplicationContext(), ControlCenterService.class.getName()) == false) {
                    LogUtils.d(TAG, "wakeupAndBindMain() Wakeup... ControlCenterService");
                    if (Build.VERSION.SDK_INT >= 26) {
                        context.startForegroundService(new Intent(context, ControlCenterService.class));
                    } else {
                        context.startService(new Intent(context, ControlCenterService.class));
                    }
                }
            }
        }
    }
}
