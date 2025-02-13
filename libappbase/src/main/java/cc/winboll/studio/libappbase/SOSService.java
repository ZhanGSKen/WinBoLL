package cc.winboll.studio.libappbase;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/14 05:39:44
 */
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class SOSService extends Service {
    
    public static final String TAG = "SOSService";
    
    @Override
    public IBinder onBind(Intent intent) {
        
        return null;
    }
    
}
