package cc.winboll.studio.positions.utils;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/11 09:36:52
 * @Describe 定位数据融合类
 */
import cc.winboll.studio.positions.App;
import java.io.File;

public class LocationFusion {

    public static final String TAG = "LocationFusion";
    
    static final String FILE_NAME = "LocationFusionModel.json";

    // 融合定位数据的方法
    public static double[] fuseLocationData(double latitudeGPSLock, double longitudeGPSLock,
                                            double latitudeWifiLock, double longitudeWifiLock, double gpsWeight, double wifiWeight) {
        if (gpsWeight + wifiWeight != 1) {
            throw new IllegalArgumentException("GPS权重和Wi-Fi权重之和必须为1");
        }
        double lat = latitudeGPSLock * gpsWeight + latitudeWifiLock * wifiWeight;
        double lon = longitudeGPSLock * gpsWeight + longitudeWifiLock * wifiWeight;
        return new double[]{lat, lon};
    }

//    public static void main(String[] args) {
//        double[] gpsLocation = {30.5, 120.5};
//        double[] wifiLocation = {30.6, 120.6};
//        double gpsWeight = 0.6;
//        double wifiWeight = 0.4;
//        double[] fusedLocation = fuseLocationData(gpsLocation, wifiLocation, gpsWeight, wifiWeight);
//        System.out.println("融合后的纬度: " + fusedLocation[0] + ", 经度: " + fusedLocation[1]);
//    }
    
    static String getDataPath() {
        return App.szDataFolder + File.separator + FILE_NAME;
    }
}
