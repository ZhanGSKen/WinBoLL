package cc.winboll.studio.libaes.winboll;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @Author ZhanGSKen
 * @Date 2025/05/03 19:28
 */
public class WinBoLLClientService extends Service {
    
    public static final String TAG = "WinBoLLClientService";
    
    @Override
    public IBinder onBind(Intent intent) {
        
        return null;
    }
    
}