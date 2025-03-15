package cc.winboll.studio.autoinstaller.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import cc.winboll.studio.autoinstaller.beans.AppConfigs;
import cc.winboll.studio.autoinstaller.services.MainService;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/06/06 13:19:38
 * @Describe 应用消息接收类
 */
public class MainReceiver extends BroadcastReceiver {

    public static final String TAG = "MainReceiver";

    static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        String szAction = intent.getAction();
        if (szAction.equals(ACTION_BOOT_COMPLETED)) {
            AppConfigs appConfigs = AppConfigs.loadAppConfigs(context);
            if (appConfigs.isEnableService()) {
                Intent intentService = new Intent(context, MainService.class);
                //intentService.putExtra(MainService.EXTRA_APKFILEPATH, appConfigs.getWatchingFilePath());
                if (Build.VERSION.SDK_INT >= 26) {
                    context.startForegroundService(intentService);
                } else {
                    context.startService(intentService);
                }
                LogUtils.i(TAG, "System Boot And Start MainService Completed!");
            }
        }
    }

}
