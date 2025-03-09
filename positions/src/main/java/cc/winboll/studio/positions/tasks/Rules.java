package cc.winboll.studio.positions.tasks;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/25 01:15:08
 * @Describe 定位规则类
 */
import com.tencent.map.geolocation.TencentLocation;
import android.location.Location;

public class Rules {
    
    public static final String TAG = "Rules";
    
    public static String getEffectInfo(Location locationA) {
        //Location locationB = new Location(22.0f, 111.0f);
        // 腾讯SDK返回的坐标点（注意坐标系需统一）
        //TencentLocation locationA = ...; // 第一个点
        //TencentLocation locationB = ...; // 第二个点

//        float[] results = new float[1];
//        Location.distanceBetween(
//            locationA.getLatitude(),  // 纬度
//            locationA.getLongitude(), // 经度
//            locationB.getLatitude(),
//            locationB.getLongitude(),
//            results
//        );
//
//        return "两点距离：" + results[0] + "米";
        return "";
    }
    
}
