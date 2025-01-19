package cc.winboll.studio.powerbell.handlers;

import android.os.Handler;
import android.os.Message;
import cc.winboll.studio.powerbell.services.ControlCenterService;
import java.lang.ref.WeakReference;

public class ControlCenterServiceHandler extends Handler {
    public static final String TAG = ControlCenterServiceHandler.class.getSimpleName();

    public static final int MSG_REMIND_TEXT = 0;

    WeakReference<ControlCenterService> serviceWeakReference;
    public ControlCenterServiceHandler(ControlCenterService service) {
        serviceWeakReference = new WeakReference<ControlCenterService>(service);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_REMIND_TEXT: // 处理下载完成消息，更新UI
                {
                    // 显示提醒消息
                    //
                    //LogUtils.d(TAG, "显示提醒消息");
                    ControlCenterService controlCenterService = serviceWeakReference.get();
                    if (controlCenterService != null) {
                        //LogUtils.d(TAG, ((NotificationMessage)msg.obj).getTitle());
                        //LogUtils.d(TAG, ((NotificationMessage)msg.obj).getContent());
                        controlCenterService.appenRemindMSG((String)msg.obj);
                    }
                    break;
                }
        }
    }
}
