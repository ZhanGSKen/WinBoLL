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
import cc.winboll.studio.libappbase.bean.APPSOSBean;
import java.util.ArrayList;

public class SOS {

    public static final String TAG = "SOS";
    
    public static final String ACTION_SOS = SOS.class.getName() + ".ACTION_SOS";
    public static final String ACTION_BIND = SOS.class.getName() + ".ACTION_BIND";
    public static final String ACTION_SERVICE_ENABLE = SOS.class.getName() + ".ACTION_SERVICE_ENABLE";
    public static final String ACTION_SERVICE_DISABLE = SOS.class.getName() + ".ACTION_SERVICE_DISENABLE";
    
    public static void sosWinBollService(Context context, APPSOSBean bean) {
        Intent intent = new Intent(ACTION_SOS);
        intent.putExtra("SOS", "Service");
        intent.putExtra("APPSOSBean", bean.toString());
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

    public static void bindToAPPService(Context context, APPSOSBean bean) {
        Intent intent = new Intent(ACTION_BIND);
        intent.putExtra("SOS", "Service");
        intent.putExtra("APPSOSBean", bean.toString());
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
