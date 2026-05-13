package cc.winboll.studio.libgpsrelaysentinel.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libgpsrelaysentinel.model.GpsSubscribeMsg;
import cc.winboll.studio.libgpsrelaysentinel.model.LocationPoint;

/**
 * 全局消息接收父类服务
 * 所有应用内接收服务全部继承此类
 */
public abstract class GpsSubscribeReceiverService extends Service {

    public static final String TAG_PARENT = "GpsSubscribeReceiverService";

    //当前绑定的视图订阅SID
    protected String bindViewSid;

    public void bindControlSid(String sid){
        this.bindViewSid = sid;
    }

    /**
     * 统一接收GPS推送入口
     */
    public void onReceiveGpsData(LocationPoint point, GpsSubscribeMsg config){
        //父类统一日志溯源
        LogUtils.d(TAG_PARENT,"【消息溯源】接收视图SID：" + bindViewSid);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

