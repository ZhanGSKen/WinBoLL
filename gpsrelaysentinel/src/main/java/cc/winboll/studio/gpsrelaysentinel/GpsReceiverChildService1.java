package cc.winboll.studio.gpsrelaysentinel;

import android.content.Intent;

import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libgpsrelaysentinel.model.GpsSubscribeMsg;
import cc.winboll.studio.libgpsrelaysentinel.model.LocationPoint;
import cc.winboll.studio.libgpsrelaysentinel.service.GpsSubscribeReceiverService;

public final class GpsReceiverChildService1 extends GpsSubscribeReceiverService {

    public static final String TAG = "GpsReceiverChildService1";

    @Override
    public void onReceiveGpsData(LocationPoint point, GpsSubscribeMsg config) {
        super.onReceiveGpsData(point, config);
        //当前独立接收日志
        LogUtils.d(TAG,"独立接收服务1 成功收到GPS消息");
        LogUtils.d(TAG,"纬度:"+point.getLatitude()+" 经度:"+point.getLongitude());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }
}

