package cc.winboll.studio.contacts.receivers;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/13 06:58:04
 * @Describe 主要广播接收器
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import cc.winboll.studio.contacts.services.MainService;
import com.hjq.toast.ToastUtils;
import java.lang.ref.WeakReference;
import cc.winboll.studio.libappbase.LogUtils;

public class MainReceiver extends BroadcastReceiver {

    public static final String TAG = "MainReceiver";
    public static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    WeakReference<MainService> mwrService;

    public MainReceiver(MainService service) {
        mwrService = new WeakReference<MainService>(service);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String szAction = intent.getAction();
        if (szAction.equals(ACTION_BOOT_COMPLETED)) {
            ToastUtils.show("ACTION_BOOT_COMPLETED");
            MainService.startMainService(context);
        } else {
            ToastUtils.show(szAction);
        }
    }

    // 注册 Receiver
    //
    public void registerAction(Context context) {
        IntentFilter filter=new IntentFilter();
        filter.addAction(ACTION_BOOT_COMPLETED);
        //filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(this, filter);
    }
}
