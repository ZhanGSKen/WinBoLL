package cc.winboll.studio.gitsion;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libgitsion.manager.GpsSubscribeManager;
import cc.winboll.studio.libgitsion.manager.SubscribeLocationManager;
import cc.winboll.studio.libgitsion.model.GpsSubscribeMsg;

import java.util.Map;

/**
 * WinBoLL Studio
 * GPS定位核心前台服务
 * 负责GPS持续监听、订阅者步长判断、基准坐标刷新、前台常驻通知
 * Java7 | API26~30
 * 新增：实时同步最新GPS到MainActivity静态坐标
 */
public final class MainService extends Service {

    //日志标签
    public static final String TAG = "MainService";

    //前台通知常量
    private static final String CHANNEL_ID = "gps_relay_channel";
    private static final int NOTIFICATION_ID = 1;

    //SP配置常量
    static final String PREF_NAME = "gps_relay_service_prefs";
    static final String KEY_SERVICE_ENABLED = "service_enabled";

    //系统定位 & 通知控件
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;

    //运行状态 & 计数
    private boolean mIsRunning = false;
    private int mGpsLocationCount = 0;

    //订阅管理器
    private GpsSubscribeManager mSubscribeManager;
    private SubscribeLocationManager mLocationRuleManager;


    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "Service onCreate");

        initManager();
        initNotificationConfig();

        //上次开启状态则自动重启GPS监听
        if (checkServiceEnableStatus()) {
            LogUtils.d(TAG, "历史服务已启用，自动启动GPS监听");
            startGpsLocationListen();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "Service onStartCommand");
        saveServiceEnableStatus(true);
        startGpsLocationListen();
        return START_STICKY;
    }

    /**
     * 初始化订阅规则管理器
     */
    private void initManager() {
        mSubscribeManager = GpsSubscribeManager.getInstance();
        mLocationRuleManager = SubscribeLocationManager.getInstance();
    }

    /**
     * 初始化通知渠道与管理类
     */
    private void initNotificationConfig() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createSystemNotificationChannel();
    }

    /**
     * 读取服务启用状态
     */
    private boolean checkServiceEnableStatus() {
        return getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
			.getBoolean(KEY_SERVICE_ENABLED, false);
    }

    /**
     * 保存服务启用状态
     */
    private void saveServiceEnableStatus(boolean enabled) {
        getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
			.edit()
			.putBoolean(KEY_SERVICE_ENABLED, enabled)
			.apply();
        LogUtils.d(TAG, "服务启用状态已设置：" + enabled);
    }

    /**
     * 启动GPS定位监听核心逻辑
     */
    private void startGpsLocationListen() {
        if (mIsRunning) {
            LogUtils.d(TAG, "GPS监听已正在运行，无需重复启动");
            return;
        }

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        initLocationListener();

        try {
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                //定位间隔：1000毫秒 / 最小位移1米
                mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER,
					1000,
					1,
					mLocationListener
                );
                mIsRunning = true;
                startServiceForegroundNotification();
                LogUtils.d(TAG, "GPS定位监听已成功注册");
            }
        } catch (SecurityException e) {
            LogUtils.e(TAG, "定位权限缺失，监听启动失败：" + e.getMessage());
        }
    }

    /**
     * 初始化定位监听回调
     */
    private void initLocationListener() {
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                handleLocationUpdate(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                LogUtils.d(TAG, "GPS状态变更 -> 提供者：" + provider + " 状态：" + status);
            }

            @Override
            public void onProviderEnabled(String provider) {
                LogUtils.d(TAG, "GPS提供者已启用：" + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                LogUtils.d(TAG, "GPS提供者已禁用：" + provider);
            }
        };
    }

    /**
     * 处理每次定位刷新｜核心：步长判断 + 基准坐标更新
     * 新增：同步最新坐标到MainActivity静态变量
     */
    private void handleLocationUpdate(Location location) {
        mGpsLocationCount ++;
        String locationInfo = "纬度：" + location.getLatitude() + " , 经度：" + location.getLongitude();
        LogUtils.d(TAG, "定位刷新 -> " + locationInfo);

        //========== 新增关键代码：实时同步最新真实GPS坐标 ==========
        MainActivity.lastLat = location.getLatitude();
        MainActivity.lastLng = location.getLongitude();
        //==========================================================

        //更新前台通知文案
        updateForegroundNotification(locationInfo);

        //遍历全部订阅者进行推送规则判断
        Map<String, GpsSubscribeMsg> subscribeAllMap = mSubscribeManager.getSubscribeMap();
        for (Map.Entry<String, GpsSubscribeMsg> entry : subscribeAllMap.entrySet()) {
            final String subscribeSid = entry.getKey();
            final GpsSubscribeMsg subscribeConfig = entry.getValue();

            double currentLat = location.getLatitude();
            double currentLng = location.getLongitude();

            //判断是否满足推送条件(全订阅/步长阈值)
            boolean allowPush = mLocationRuleManager.isNeedPush(subscribeSid, currentLat, currentLng);
            if (allowPush) {
                //推送成功后刷新该订阅者基准定点坐标
                mLocationRuleManager.updateSubscriberPoint(subscribeSid, currentLat, currentLng);
            }
        }
    }

    /**
     * 创建系统通知渠道
     */
    private void createSystemNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
				CHANNEL_ID,
				"GPS Relay Service",
				NotificationManager.IMPORTANCE_LOW
            );
            notificationChannel.setDescription("GPSRelaySentinel 后台常驻服务通知");
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }

    /**
     * 开启前台常驻通知
     */
    private void startServiceForegroundNotification() {
        mNotificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
			.setContentTitle("GPS 中继服务")
			.setContentText("等待GPS定位数据...")
			.setSmallIcon(android.R.drawable.ic_menu_mylocation)
			.setOngoing(true);

        Notification notification = mNotificationBuilder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

    /**
     * 动态更新通知内容
     */
    private void updateForegroundNotification(String locationText) {
        if (mNotificationBuilder != null) {
            mNotificationBuilder.setContentText(locationText + " | 定位次数：" + mGpsLocationCount);
            mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //注销定位监听
        if (mLocationManager != null && mLocationListener != null) {
            try {
                mLocationManager.removeUpdates(mLocationListener);
            } catch (SecurityException e) {
                LogUtils.e(TAG, "移除定位监听权限异常：" + e.getMessage());
            }
        }
        mIsRunning = false;
        LogUtils.d(TAG, "MainService 已销毁，GPS监听已停止");
    }
}

