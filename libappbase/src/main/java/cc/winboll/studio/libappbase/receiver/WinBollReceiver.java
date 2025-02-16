package cc.winboll.studio.libappbase.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.WinBoll;
import cc.winboll.studio.libappbase.AppUtils;
import cc.winboll.studio.libappbase.widgets.APPSOSReportWidget;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/15 12:17:32
 * @Describe WinBollReceiver
 */
public class WinBollReceiver extends BroadcastReceiver {

    public static final String TAG = "WinBollReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(WinBoll.ACTION_SOS)) {
            LogUtils.d(TAG, String.format("context.getPackageName() %s", context.getPackageName()));
            LogUtils.d(TAG, String.format("action %s", action));
            String sos = intent.getStringExtra("sos");
            LogUtils.d(TAG, String.format("sos %s", sos));
            if (sos != null && sos.equals("SOS")) {
                String sosCalssType = intent.getStringExtra("sosCalssType");
                LogUtils.d(TAG, String.format("sosCalssType %s", sosCalssType));
                if (sosCalssType != null && sosCalssType.equals("Service")) {
                    String sosPackage = intent.getStringExtra("sosPackage");
                    LogUtils.d(TAG, String.format("sosPackage %s", sosPackage));

                    String sosClassName = intent.getStringExtra("sosClassName");
                    LogUtils.d(TAG, String.format("sosClassName %s", sosClassName));

                    Intent intentService = new Intent();
                    intentService.setComponent(new ComponentName(sosPackage, sosClassName));
                    context.startService(intentService);
                    LogUtils.d(TAG, String.format("context.startService(intentService);"));
                    
                    String appName = AppUtils.getAppNameByPackageName(context, sosPackage);
                    LogUtils.d(TAG, String.format("appName %s", appName));
                    Intent intentAPPSOSReportWidget = new Intent(context, APPSOSReportWidget.class);
                    intentAPPSOSReportWidget.setAction(APPSOSReportWidget.ACTION_ADD_SOS_REPORT);
                    intentAPPSOSReportWidget.putExtra("appName", appName);
                    context.sendBroadcast(intentAPPSOSReportWidget);
                    
                }  
            }

        } else {
            LogUtils.d(TAG, String.format("action %s", action));
        }
    }

}
