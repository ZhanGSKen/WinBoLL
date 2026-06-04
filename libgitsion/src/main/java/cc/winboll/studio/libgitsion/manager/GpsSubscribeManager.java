package cc.winboll.studio.libgitsion.manager;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/05/07 10:25
 */

import android.content.Context;
import android.content.Intent;
import java.util.HashMap;
import java.util.Map;

import cc.winboll.studio.libgitsion.model.GpsSubscribeConst;
import cc.winboll.studio.libgitsion.model.GpsSubscribeMsg;
import cc.winboll.studio.libgitsion.model.GpsSubscribeResult;

public final class GpsSubscribeManager {

    private static GpsSubscribeManager instance;
    private final Map<String,GpsSubscribeMsg> subscribeMap;
    private Context appContext;

    private GpsSubscribeManager(){
        subscribeMap = new HashMap<String, GpsSubscribeMsg>();
    }

    public static GpsSubscribeManager getInstance(){
        if(instance == null){
            instance = new GpsSubscribeManager();
        }
        return instance;
    }

    public void initContext(final Context context){
        this.appContext = context.getApplicationContext();
    }

    public void addSubscribe(final GpsSubscribeMsg subscribeMsg){
        if(subscribeMsg == null){
            return;
        }
        subscribeMap.put(subscribeMsg.getSubscribeUniqueId(),subscribeMsg);
    }

    public void removeSubscribe(final String sid){
        if(sid == null){
            return;
        }
        subscribeMap.remove(sid);
        SubscribeLocationManager.getInstance().removeSubscribe(sid);
    }

    public boolean isSubscribeExist(final String sid){
        return subscribeMap.containsKey(sid);
    }

    public void sendSubscribeResult(final GpsSubscribeResult result){
        if(appContext == null || result == null){
            return;
        }
        Intent intent = new Intent(GpsSubscribeConst.ACTION_SUBSCRIBE_CALLBACK);
        intent.putExtra(GpsSubscribeConst.EXTRA_SUBSCRIBE_RESULT, result);
        appContext.sendBroadcast(intent);
    }

    public void clearAllSubscribe(){
        subscribeMap.clear();
        SubscribeLocationManager.getInstance().clearAll();
    }

    public Map<String, GpsSubscribeMsg> getSubscribeMap() {
        return subscribeMap;
    }
}

