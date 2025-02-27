package cc.winboll.studio.libappbase;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/13 21:09:36
 * @Describe SOS 组件
 */
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import cc.winboll.studio.libappbase.bean.APPNewsBean;
import java.util.ArrayList;

public class WinBoll {

    public static final String TAG = "WinBoll";
    
    public static final String ACTION_SOS = WinBoll.class.getName() + ".ACTION_SOS";
    public static final String ACTION_BIND = WinBoll.class.getName() + ".ACTION_BIND";
    public static final String ACTION_SERVICE_ENABLE = WinBoll.class.getName() + ".ACTION_SERVICE_ENABLE";
    public static final String ACTION_SERVICE_DISABLE = WinBoll.class.getName() + ".ACTION_SERVICE_DISENABLE";
    public static final String EXTRA_SOS = "EXTRA_SOS";
    public static final String EXTRA_APPNEWSBEAN = "EXTRA_APPNEWSBEAN";
    
    public static void sosService(Context context, APPNewsBean bean) {
        Intent intent = new Intent(ACTION_SOS);
        intent.putExtra(EXTRA_SOS, "Service");
        intent.putExtra(EXTRA_APPNEWSBEAN, bean.toString());
        String szToPackage = "";
        if (GlobalApplication.isDebuging()) {
            szToPackage = "cc.winboll.studio.appbase.beta";
        } else {
            szToPackage = "cc.winboll.studio.appbase";
        }
        intent.setPackage(szToPackage);
        context.sendBroadcast(intent);

        LogUtils.d(TAG, String.format("Send ACTION_SOS To WinBoll. (szToPackage : %s)", szToPackage));
        //ToastUtils.show("SOS Send To WinBoll");
    }

    public static void bindToAPPBase(Context context, APPNewsBean bean) {
        Intent intent = new Intent(ACTION_BIND);
        intent.putExtra(EXTRA_SOS, "Service");
        intent.putExtra(EXTRA_APPNEWSBEAN, bean.toString());
        String szToPackage = "";
        if (GlobalApplication.isDebuging()) {
            szToPackage = "cc.winboll.studio.appbase.beta";
        } else {
            szToPackage = "cc.winboll.studio.appbase";
        }
        intent.setPackage(szToPackage);
        context.sendBroadcast(intent);
        LogUtils.d(TAG, String.format("Send ACTION_BIND To WinBoll. (szToPackage : %s)", szToPackage));
    }
    
}
