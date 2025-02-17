package cc.winboll.studio.libappbase;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/13 21:09:36
 * @Describe SOS 组件
 */
import android.content.Context;
import android.content.Intent;
import cc.winboll.studio.libappbase.bean.APPSOSBean;

public class SOS {

    public static final String TAG = "SOS";
    public static final String ACTION_SOS = "cc.winboll.studio.libappbase.WinBoll.ACTION_SOS";
    
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

        LogUtils.d(TAG, String.format("SOS Send To WinBoll. (szToPackage : %s)", szToPackage));
        //ToastUtils.show("SOS Send To WinBoll");
    }

}
