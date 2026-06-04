package cc.winboll.studio.gitsion;

import android.content.Intent;

import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libgitsion.model.GpsSubscribeMsg;
import cc.winboll.studio.libgitsion.model.LocationPoint;
import cc.winboll.studio.libgitsion.service.GpsSubscribeReceiverService;

public final class GpsReceiverChildService2 extends GpsSubscribeReceiverService {

    public static final String TAG = "GpsReceiverChildService2";

    @Override
    public void onReceiveGpsData(LocationPoint point, GpsSubscribeMsg config) {
        super.onReceiveGpsData(point, config);
        LogUtils.d(TAG,"独立接收服务2 成功收到GPS消息");
        LogUtils.d(TAG,"纬度:"+point.getLatitude()+" 经度:"+point.getLongitude());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }
}

