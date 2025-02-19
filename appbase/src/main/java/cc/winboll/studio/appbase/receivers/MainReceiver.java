package cc.winboll.studio.appbase.receivers;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/13 06:58:04
 * @Describe 主要广播接收器
 */
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import cc.winboll.studio.appbase.beans.SOSReportBean;
import cc.winboll.studio.appbase.services.MainService;
import cc.winboll.studio.appbase.widgets.SOSWidget;
import cc.winboll.studio.libappbase.AppUtils;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.SOS;
import cc.winboll.studio.libappbase.bean.APPSOSBean;
import com.hjq.toast.ToastUtils;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainReceiver extends BroadcastReceiver {

    public static final String TAG = "MainReceiver";
    public static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    WeakReference<MainService> mwrService;
    // 存储电量指示值，
    // 用于校验电量消息时的电量变化
    static volatile int _mnTheQuantityOfElectricityOld = -1;
    static volatile boolean _mIsCharging = false;

    public MainReceiver(MainService service) {
        mwrService = new WeakReference<MainService>(service);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String szAction = intent.getAction();
        if (szAction.equals(ACTION_BOOT_COMPLETED)) {
            ToastUtils.show("ACTION_BOOT_COMPLETED");
        } else if (szAction.equals(SOS.ACTION_BIND)) {
            LogUtils.d(TAG, "ACTION_BIND");
            LogUtils.d(TAG, String.format("context.getPackageName() %s", context.getPackageName()));
            LogUtils.d(TAG, String.format("intent.getAction() %s", intent.getAction()));
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
                            mwrService.get().bindSOSConnection(bean);
                        }
                    } catch (IOException e) {
                        LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                    }
                }
            }
        } else if (intent.getAction().equals(SOS.ACTION_SOS)) {
            LogUtils.d(TAG, "ACTION_SOS");
            LogUtils.d(TAG, String.format("context.getPackageName() %s", context.getPackageName()));
            LogUtils.d(TAG, String.format("intent.getAction() %s", intent.getAction()));
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
                            SOSReportBean appSOSReportBean = new SOSReportBean(appName);
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                            String currentTime = sdf.format(new Date());
                            StringBuilder sbLine = new StringBuilder();
                            sbLine.append("[");
                            sbLine.append(currentTime);
                            sbLine.append("] Power to ");
                            sbLine.append(appName);
                            appSOSReportBean.setSosReport(sbLine.toString());
                            
                            SOSWidget.addAPPSOSReportBean(context, appSOSReportBean);
                            Intent intentWidget = new Intent(context, SOSWidget.class);
                            intentWidget.setAction(SOSWidget.ACTION_RELOAD_REPORT);
                            context.sendBroadcast(intentWidget);
                        }
                    } catch (IOException e) {
                        LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                    }
                }
            }
        } else {
            ToastUtils.show(szAction);
        }
    }

    // 注册 Receiver
    //
    public void registerAction(MainService service) {
        IntentFilter filter=new IntentFilter();
        filter.addAction(ACTION_BOOT_COMPLETED);
        filter.addAction(SOS.ACTION_SOS);
        filter.addAction(SOS.ACTION_BIND);
        filter.addAction(SOS.ACTION_SERVICE_ENABLE);
        filter.addAction(SOS.ACTION_SERVICE_DISABLE);
        //filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        service.registerReceiver(this, filter);
    }
}
