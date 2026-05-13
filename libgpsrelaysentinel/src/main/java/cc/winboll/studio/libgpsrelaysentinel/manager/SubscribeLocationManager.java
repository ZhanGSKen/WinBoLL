package cc.winboll.studio.libgpsrelaysentinel.manager;

import cc.winboll.studio.libgpsrelaysentinel.model.GpsSubscribeConst;
import cc.winboll.studio.libgpsrelaysentinel.model.GpsSubscribeMsg;
import cc.winboll.studio.libgpsrelaysentinel.model.LocationPoint;

import java.util.HashMap;
import java.util.Map;

public final class SubscribeLocationManager {

    private static SubscribeLocationManager instance;

    //订阅配置
    private final Map<String,GpsSubscribeMsg> subscribeConfigMap;
    //基准定点坐标
    private final Map<String,LocationPoint> subscriberPointMap;
    //真实推送计数(精准统计)
    private final Map<String,Integer> subscriberPushCountMap;

    private SubscribeLocationManager(){
        subscribeConfigMap = new HashMap<String, GpsSubscribeMsg>();
        subscriberPointMap = new HashMap<String, LocationPoint>();
        subscriberPushCountMap = new HashMap<String, Integer>();
    }

    public static SubscribeLocationManager getInstance(){
        if(instance == null){
            instance = new SubscribeLocationManager();
        }
        return instance;
    }

    //========= 订阅配置 =========
    public void putSubscribeConfig(String sid,GpsSubscribeMsg msg){
        subscribeConfigMap.put(sid,msg);
    }

    public GpsSubscribeMsg getSubscribeConfig(String sid){
        return subscribeConfigMap.get(sid);
    }

    //========= 基准定点坐标 =========
    public void initSubscriberPoint(String sid,double lat,double lng){
        subscriberPointMap.put(sid,new LocationPoint(lat,lng,System.currentTimeMillis()));
    }

    public void updateSubscriberPoint(String sid,double lat,double lng){
        subscriberPointMap.put(sid,new LocationPoint(lat,lng,System.currentTimeMillis()));
    }

    public LocationPoint getLastPoint(String sid){
        return subscriberPointMap.get(sid);
    }

    //========= 精准推送计数 =========
    public void addPushCount(String sid){
        int current = subscriberPushCountMap.get(sid) == null ? 0 : subscriberPushCountMap.get(sid);
        subscriberPushCountMap.put(sid,current + 1);
    }

    public int getPushCount(String sid){
        return subscriberPushCountMap.get(sid) == null ? 0 : subscriberPushCountMap.get(sid);
    }

    public void clearPushCount(String sid){
        subscriberPushCountMap.put(sid,0);
    }

    //========= 步长规则判断 =========
    public boolean isNeedPush(String sid,double nowLat,double nowLng){
        GpsSubscribeMsg config = getSubscribeConfig(sid);
        if(config == null){
            return false;
        }

        //全量订阅直接放行
        if(config.getSubscribeMode() == GpsSubscribeConst.SUB_TYPE_ALL){
            return true;
        }

        //无初始定点 → 先建立第一个基准点
        LocationPoint lastPoint = getLastPoint(sid);
        if(lastPoint == null){
            return true;
        }

        //计算实际移动距离
        double distance = calculateDistance(
			lastPoint.getLatitude(),lastPoint.getLongitude(),
			nowLat,nowLng
        );

        return distance >= config.getStepDistanceM();
    }

    //两点经纬度距离计算(米)
    private double calculateDistance(double lat1,double lng1,double lat2,double lng2){
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double radLng1 = Math.toRadians(lng1);
        double radLng2 = Math.toRadians(lng2);

        double latDiff = radLat1 - radLat2;
        double lngDiff = radLng1 - radLng2;

        double value = 2 * Math.asin(Math.sqrt(
										 Math.pow(Math.sin(latDiff / 2),2)
										 + Math.cos(radLat1) * Math.cos(radLat2)
										 * Math.pow(Math.sin(lngDiff / 2),2)
									 ));
        return value * 6378137;
    }

    //========= 移除 & 清空 =========
    public void removeSubscribe(String sid){
        subscribeConfigMap.remove(sid);
        subscriberPointMap.remove(sid);
        subscriberPushCountMap.remove(sid);
    }

    public void clearAll(){
        subscribeConfigMap.clear();
        subscriberPointMap.clear();
        subscriberPushCountMap.clear();
    }
}

