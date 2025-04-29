package cc.winboll.studio.libaes.winboll;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/28 19:13:20
 * @Describe WinBoLL 邮件服务
 */
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class WinBoLLMail extends Service {

    public static final String TAG = "WinBoLLMail";

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

}
