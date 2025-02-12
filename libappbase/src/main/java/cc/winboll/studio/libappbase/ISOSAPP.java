package cc.winboll.studio.libappbase;
import android.content.Intent;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/13 03:49:35
 * @Describe 简单 SOS 接口
 */
public interface ISOSAPP {
    
    public static final String TAG = "ISOS";
    public static final String EXTRA_PACKAGE = "EXTRA_PACKAGE";
    public static final String EXTRA_SERVICE = "EXTRA_SERVICE";
    
    public void helpISOSService(Intent intent);
    
}
