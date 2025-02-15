package cc.winboll.studio.libappbase;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/13 21:09:36
 * @Describe SOS 组件
 */
import android.content.Context;
import android.content.Intent;
import com.hjq.toast.ToastUtils;

public class SOS {

    public static final String TAG = "SOS";

    public static void sosToWinBoll(Context context) {
        Intent intent = new Intent(WinBoll.ACTION_SOS);
        intent.putExtra("sosPackage", context.getPackageName());
        intent.putExtra("message", "SOS");
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
