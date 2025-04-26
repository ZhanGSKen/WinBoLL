package cc.winboll.studio.libaes.winboll;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/28 19:13:20
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
