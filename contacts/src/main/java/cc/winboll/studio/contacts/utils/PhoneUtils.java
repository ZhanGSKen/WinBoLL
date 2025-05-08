package cc.winboll.studio.contacts.utils;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/26 15:21:48
 * @Describe PhoneUtils
 */
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

public class PhoneUtils {
    
    public static final String TAG = "PhoneUtils";
    
    public static void call(Context context, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(android.net.Uri.parse("tel:" + phoneNumber));
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        context.startActivity(intent);
    }
    
}
