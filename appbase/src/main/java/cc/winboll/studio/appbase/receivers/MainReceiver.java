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
import cc.winboll.studio.appbase.beans.WinBollNewsBean;
import cc.winboll.studio.appbase.services.MainService;
import cc.winboll.studio.appbase.widgets.WinBollNewsWidget;
import cc.winboll.studio.libappbase.AppUtils;
import cc.winboll.studio.libappbase.LogUtils;
import com.hjq.toast.ToastUtils;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import cc.winboll.studio.libappbase.sos.WinBoll;
import cc.winboll.studio.libappbase.sos.APPModel;
import cc.winboll.studio.libappbase.sos.SOS;
import cc.winboll.studio.libappbase.sos.SOSObject;

public class MainReceiver extends BroadcastReceiver {

    public static final String TAG = "MainReceiver";

    public static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    WeakReference<MainService> mwrService;

    public MainReceiver(MainService service) {
        mwrService = new WeakReference<MainService>(service);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String szAction = intent.getAction();
        if (szAction.equals(ACTION_BOOT_COMPLETED)) {
            ToastUtils.show("ACTION_BOOT_COMPLETED");
        } else if (szAction.equals(WinBoll.ACTION_BIND)) {
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
                        APPModel bean = APPModel.parseStringToBean(szAPPSOSBean, APPModel.class);
                        if (bean != null) {
                            String szAppPackageName = bean.getAppPackageName();
                            LogUtils.d(TAG, String.format("szAppPackageName %s", szAppPackageName));
                            String szAppMainServiveName = bean.getAppMainServiveName();
                            LogUtils.d(TAG, String.format("szNewsClassName %s", szAppMainServiveName));
                            mwrService.get().bindSOSConnection(bean);
                        }
                    } catch (IOException e) {
                        LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                    }
                }
            }
        } else if (intent.getAction().equals(SOS.ACTION_SOS)) {
            LogUtils.d(TAG, "ACTION_SOS");
            String sos = intent.getStringExtra(SOS.EXTRA_OBJECT);
            LogUtils.d(TAG, String.format("SOS %s", sos));
            if (sos != null && sos.equals("")) {
                SOSObject bean = SOS.parseSOSObject(sos);
                if (bean != null) {
                    String szObjectPackageName = bean.getObjectPackageName();
                    LogUtils.d(TAG, String.format("szObjectPackageName %s", szObjectPackageName));
                    String szObjectServiveName = bean.getObjectServiveName();
                    LogUtils.d(TAG, String.format("szObjectServiveName %s", szObjectServiveName));

                    Intent intentService = new Intent();
                    intentService.setComponent(new ComponentName(szObjectPackageName, szObjectServiveName));
                    context.startService(intentService);

                    String appName = AppUtils.getAppNameByPackageName(context, szObjectPackageName);
                    LogUtils.d(TAG, String.format("appName %s", appName));
                    WinBollNewsBean appWinBollNewsBean = new WinBollNewsBean(appName);
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    String currentTime = sdf.format(new Date());
                    StringBuilder sbLine = new StringBuilder();
                    sbLine.append("[");
                    sbLine.append(currentTime);
                    sbLine.append("] Power to ");
                    sbLine.append(appName);
                    appWinBollNewsBean.setMessage(sbLine.toString());

                    WinBollNewsWidget.addWinBollNewsBean(context, appWinBollNewsBean);

                    Intent intentWidget = new Intent(context, WinBollNewsWidget.class);
                    intentWidget.setAction(WinBollNewsWidget.ACTION_RELOAD_REPORT);
                    context.sendBroadcast(intentWidget);
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
        filter.addAction(WinBoll.ACTION_BIND);
        //filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        service.registerReceiver(this, filter);
    }
}
