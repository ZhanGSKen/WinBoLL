package cc.winboll.studio.positions.listeners;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/04/03 14:01:18
 * @Describe 手机 GPS 实时位置监听类
 */
import android.location.Location;

public interface OnGPSRTLocationListener {
    
    public static final String TAG = "OnGPSRTLocationListener";
    
    void onGPSRTLocation(Location location);
    
    
}
