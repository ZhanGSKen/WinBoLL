package cc.winboll.studio.powerbell.utils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/18 04:32:46
 * @Describe 电池工具类
 */
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class BatteryUtils {

    public static final String TAG = "BatteryUtils";

    public static boolean isCharging(Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL;
        return isCharging;
    }

    public static int getTheQuantityOfElectricity(Intent intent) {
        int intLevel = intent.getIntExtra("level", 0);
        int intScale = intent.getIntExtra("scale", 100);
        return intLevel * 100 / intScale;
    }
}
