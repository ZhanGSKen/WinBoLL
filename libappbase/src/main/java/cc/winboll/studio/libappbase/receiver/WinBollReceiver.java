package cc.winboll.studio.libappbase.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.WinBoll;
import cc.winboll.studio.libappbase.AppUtils;
import cc.winboll.studio.libappbase.widgets.APPSOSReportWidget;
import cc.winboll.studio.libappbase.bean.APPSOSBean;
import java.io.IOException;
import cc.winboll.studio.libappbase.bean.APPSOSReportBean;
import java.text.SimpleDateFormat;
import java.util.Date;

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
            String SOS = intent.getStringExtra("SOS");
            LogUtils.d(TAG, String.format("SOS %s", SOS));
            if (SOS != null && SOS.equals("Service")) {
                String szAPPSOSBean = intent.getStringExtra("APPSOSBean");
                LogUtils.d(TAG, String.format("szAPPSOSBean %s", szAPPSOSBean));
                if (szAPPSOSBean != null && !szAPPSOSBean.equals("")) {
                    try {
                        APPSOSBean bean = APPSOSBean.parseStringToBean(szAPPSOSBean, APPSOSBean.class);
                        if (bean != null) {
                            String sosPackage = bean.getSosPackage();
                            LogUtils.d(TAG, String.format("sosPackage %s", sosPackage));
                            String sosClassName = bean.getSosClassName();
                            LogUtils.d(TAG, String.format("sosClassName %s", sosClassName));

                            Intent intentService = new Intent();
                            intentService.setComponent(new ComponentName(sosPackage, sosClassName));
                            context.startService(intentService);

                            String appName = AppUtils.getAppNameByPackageName(context, sosPackage);
                            LogUtils.d(TAG, String.format("appName %s", appName));
                            APPSOSReportBean appSOSReportBean = new APPSOSReportBean(appName);
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                            String currentTime = sdf.format(new Date());
                            StringBuilder sbLine = new StringBuilder();
                            sbLine.append("[");
                            sbLine.append(currentTime);
                            sbLine.append("] Power to ");
                            sbLine.append(appName);
                            appSOSReportBean.setSosReport(sbLine.toString());

                            Intent intentAPPSOSReportWidget = new Intent(context, APPSOSReportWidget.class);
                            intentAPPSOSReportWidget.setAction(APPSOSReportWidget.ACTION_ADD_SOS_REPORT);
                            intentAPPSOSReportWidget.putExtra("APPSOSReportBean", appSOSReportBean.toString());
                            context.sendBroadcast(intentAPPSOSReportWidget);
                        }
                    } catch (IOException e) {
                        LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                    }
                }
            }

        } else {
            LogUtils.d(TAG, String.format("action %s", action));
        }
    }

}
