package cc.winboll.studio.positions.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import androidx.core.app.ActivityCompat;
import cc.winboll.studio.positions.listeners.OnGPSRTLocationListener;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/04/03 12:13:23
 * @Describe 获取实时 GPS 数据的服务
 */
public class GPSService extends Service {

    public static final String TAG = "GPSService";

    OnGPSRTLocationListener mOnGPSRTLocationListener;
    LocationManager locationManager;
    Location mLocationPhoneGPS;
    Location mLocationNetwork;

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    // 用于返回服务实例的Binder
    public class MyBinder extends Binder {
        public GPSService getService() {
            return GPSService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // 请求GPS定位
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, phoneGPSLocationListener);
        // 请求基站（网络）定位
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkLocationListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.removeUpdates(phoneGPSLocationListener);
            locationManager.removeUpdates(networkLocationListener);
        }
    }
    
    public void setOnGPSRTLocationListener(OnGPSRTLocationListener listener) {
        mOnGPSRTLocationListener = listener;
    }

    private LocationListener phoneGPSLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // 处理GPS定位结果
            if (location != null && mOnGPSRTLocationListener != null) {
                mLocationPhoneGPS = location;
                mOnGPSRTLocationListener.onGPSRTLocation(mLocationPhoneGPS);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    private LocationListener networkLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // 处理基站（网络）定位结果
            if (location != null) {
                mLocationNetwork = location;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };
}
