package cc.winboll.studio.libgitsion.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libgitsion.manager.SubscribeLocationManager;
import cc.winboll.studio.libgitsion.model.GpsSubscribeConst;
import cc.winboll.studio.libgitsion.model.GpsSubscribeMsg;
import cc.winboll.studio.libgitsion.model.GpsSubscribeResult;
import cc.winboll.studio.libgitsion.model.LocationPoint;

/**
 * 全局消息接收父类服务
 * 所有应用内接收服务全部继承此类
 */
public abstract class GpsSubscribeReceiverService extends Service {

    public static final String TAG_PARENT = "GpsSubscribeReceiverService";

    //当前绑定的视图订阅SID
    protected String bindViewSid;

    private BroadcastReceiver mCallbackReceiver;

    public void bindControlSid(String sid){
        this.bindViewSid = sid;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG_PARENT, "Service onCreate, 注册广播接收器");
        mCallbackReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (GpsSubscribeConst.ACTION_SUBSCRIBE_CALLBACK.equals(intent.getAction())) {
                    GpsSubscribeResult result = intent.getParcelableExtra(GpsSubscribeConst.EXTRA_SUBSCRIBE_RESULT);
                    if (result != null && bindViewSid != null
                        && bindViewSid.equals(result.getSubscribeUniqueId())) {
                        LocationPoint point = new LocationPoint(
                            result.getLatitude(),
                            result.getLongitude(),
                            result.getLocationTime()
                        );
                        GpsSubscribeMsg config = SubscribeLocationManager.getInstance()
                            .getSubscribeConfig(bindViewSid);
                        LogUtils.d(TAG_PARENT, "收到GPS推送，转发至 onReceiveGpsData，SID：" + bindViewSid);
                        onReceiveGpsData(point, config);
                    }
                }
            }
        };
        registerReceiver(mCallbackReceiver, new IntentFilter(GpsSubscribeConst.ACTION_SUBSCRIBE_CALLBACK));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra(GpsSubscribeConst.EXTRA_SUBSCRIBE_SID)) {
            String sid = intent.getStringExtra(GpsSubscribeConst.EXTRA_SUBSCRIBE_SID);
            bindControlSid(sid);
            LogUtils.d(TAG_PARENT, "绑定SID：" + sid);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCallbackReceiver != null) {
            unregisterReceiver(mCallbackReceiver);
            mCallbackReceiver = null;
            LogUtils.d(TAG_PARENT, "广播接收器已注销");
        }
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

