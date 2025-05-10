package cc.winboll.studio.contacts.handlers;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/02/14 03:51:40
 */
import android.os.Handler;
import android.os.Message;
import cc.winboll.studio.contacts.services.MainService;
import java.lang.ref.WeakReference;

public class MainServiceHandler extends Handler {
    public static final String TAG = "MainServiceHandler";

    public static final int MSG_REMINDTHREAD = 0;

    WeakReference<MainService> serviceWeakReference;
    public MainServiceHandler(MainService service) {
        serviceWeakReference = new WeakReference<MainService>(service);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_REMINDTHREAD: // 处理下载完成消息，更新UI
                {
                    // 显示提醒消息
                    //
                    //LogUtils.d(TAG, "显示提醒消息");
                    MainService mainService = serviceWeakReference.get();
                    if (mainService != null) {
                        mainService.appenMessage((String)msg.obj);
                    }
                    break;
                }
        }
    }
}
