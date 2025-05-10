package cc.winboll.studio.shared.service;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/12/09 08:19:06
 * @Describe WinBoll 邮件服务
 */
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class WinBollMail extends Service {
    
    public static final String TAG = "WinBollMail";
    
    @Override
    public IBinder onBind(Intent intent) {
        
        return null;
    }
    
}
