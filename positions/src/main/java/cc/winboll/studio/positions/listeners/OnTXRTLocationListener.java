package cc.winboll.studio.positions.listeners;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/04/03 13:06:13
 * @Describe 位置监听类
 */
import android.location.Location;

public interface OnTXRTLocationListener {
    
    public static final String TAG = "OnTXRTLocationListener";
    
    void onTXRTLocation(Location location);
    
}
