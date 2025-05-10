package cc.winboll.studio.libappbase.winboll;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/05/10 10:13
 * @Describe WinBoLL 系列应用通用管理类
 */
import android.content.Context;
import android.content.Intent;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.models.WinBoLLModel;

public class WinBoLL {

    public static final String TAG = "WinBoLL";

    public static final String ACTION_BIND = WinBoLL.class.getName() + ".ACTION_BIND";
    public static final String EXTRA_WINBOLLMODEL = "EXTRA_WINBOLLMODEL";

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
        intent.putExtra(EXTRA_WINBOLLMODEL, (new WinBoLLModel(toPackage, appMainService)).toString());
        intent.setPackage(toPackage);
        LogUtils.d(TAG, String.format("ACTION_BIND :\nTo Package : %s\nAPP Main Service : %s", toPackage, appMainService));
        context.sendBroadcast(intent);
    }

}
