package cc.winboll.studio.libappbase.sos;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/02 09:54:28
 * @Describe WinBoLL 系列应用通用管理类
 */
import android.content.Context;
import android.content.Intent;
import cc.winboll.studio.libappbase.LogUtils;

public class WinBoLL {

    public static final String TAG = "WinBoLL";

    public static final String ACTION_BIND = WinBoLL.class.getName() + ".ACTION_BIND";
    public static final String EXTRA_APPMODEL = "EXTRA_APPMODEL";

    public static void bindToAPPBase(Context context, String appMainService) {
        LogUtils.d(TAG, "bindToAPPBase(...)");
        String toPackage = "cc.winboll.studio.appbase";
        startBind(context, toPackage, appMainService);
    }

    public static void bindToAPPBaseBeta(Context context, String appMainService) {
        LogUtils.d(TAG, "bindToAPPBaseBeta(...)");
        String toPackage = "cc.winboll.studio.appbase.beta";
        startBind(context, toPackage, appMainService);
    }

    static void startBind(Context context, String toPackage, String appMainService) {
        Intent intent = new Intent(ACTION_BIND);
        intent.putExtra(EXTRA_APPMODEL, (new APPModel(toPackage, appMainService)).toString());
        intent.setPackage(toPackage);
        LogUtils.d(TAG, String.format("ACTION_BIND :\nTo Package : %s\nAPP Main Service : %s", toPackage, appMainService));
        context.sendBroadcast(intent);
    }

}
