package cc.winboll.studio.libappbase.sos;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/02 09:36:29
 * @Describe WinBoll 应用 SOS 机理保护类
 */
import android.content.Context;
import android.content.Intent;
import cc.winboll.studio.libappbase.LogUtils;
import java.io.IOException;

public class SOS {

    public static final String TAG = "SOS";

    public static final String ACTION_SOS = SOS.class.getName() + ".ACTION_SOS";
    public static final String EXTRA_OBJECT = "EXTRA_OBJECT";

    public static void sosToAppBase(Context context, String sosService) {
        LogUtils.d(TAG, "sosToAppBase()");
        String szToPackage = "cc.winboll.studio.appbase";
        sos(context, szToPackage, sosService);

    }

    public static void sosToAppBaseBeta(Context context, String sosService) {
        LogUtils.d(TAG, "sosToAppBaseBeta()");
        String szToPackage = "cc.winboll.studio.appbase.beta";
        sos(context, szToPackage, sosService);

    }

    static void sos(Context context, String szToPackage, String sosService) {
        LogUtils.d(TAG, "sos(...)");
        Intent intent = new Intent(ACTION_SOS);
        intent.putExtra(EXTRA_OBJECT, genSOSObjectString(context.getPackageName(), sosService));
        intent.setPackage(szToPackage);
        LogUtils.d(TAG, String.format("ACTION_SOS :\nTo Package : %sSOS Service : %s\n", szToPackage, sosService));
        context.sendBroadcast(intent);
    }

    static SOSObject stringToSOSObject(String szSOSObject) {
        try {
            return SOSObject.parseStringToBean(szSOSObject, SOSObject.class);
        } catch (IOException e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
        return null;
    }

    static String sosObjectToString(SOSObject object) {
        return object.toString();
    }

    static String genSOSObjectString(String objectPackageName, String objectServiveName) {
        return (new SOSObject(objectPackageName, objectServiveName)).toString();
    }
}
