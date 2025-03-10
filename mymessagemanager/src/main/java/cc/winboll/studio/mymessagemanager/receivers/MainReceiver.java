package cc.winboll.studio.mymessagemanager.receivers;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/05/28 20:22:12
 * @Describe 在文件 AndroidManifest.xml 注册监听的广播接收类，
 *           用于接收系统启动完毕的广播消息。
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import cc.winboll.studio.mymessagemanager.services.MainService;
import cc.winboll.studio.mymessagemanager.utils.AppConfigUtil;
import cc.winboll.studio.shared.log.LogUtils;

public class MainReceiver extends BroadcastReceiver {

    public static String TAG = "ManagerReceiver";

    static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    AppConfigUtil mConfigUtil;

    @Override
    public void onReceive(Context context, Intent intent) {
        String szAction = intent.getAction();
        if (szAction.equals(ACTION_BOOT_COMPLETED)) {
            mConfigUtil = AppConfigUtil.getInstance(context);
            if (mConfigUtil.mAppConfigBean.isEnableService()) {
                Intent intentService = new Intent(context, MainService.class);
                if (Build.VERSION.SDK_INT >= 26) {
                    context.startForegroundService(intentService);
                } else {
                    context.startService(intentService);
                }
                LogUtils.i(TAG, "System Boot And Start ManagerService Completed!");
            }
        }

    }

}
