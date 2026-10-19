package cc.winboll.studio.libgpsrelaysentinel.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/05/07 10:27
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cc.winboll.studio.libgpsrelaysentinel.model.GpsSubscribeConst;
import cc.winboll.studio.libgpsrelaysentinel.model.GpsSubscribeResult;

public final class GpsSubscribeObserverReceiver extends BroadcastReceiver {

    private OnSubscribeResultListener listener;

    public void setOnSubscribeResultListener(OnSubscribeResultListener listener){
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(GpsSubscribeConst.ACTION_SUBSCRIBE_CALLBACK.equals(action)){
            GpsSubscribeResult result = intent.getParcelableExtra("data");
            if(listener != null && result != null){
                listener.onResultBack(result);
            }
        }
    }

    public interface OnSubscribeResultListener{
        void onResultBack(GpsSubscribeResult result);
    }
}

