package cc.winboll.studio.powerbell.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import cc.winboll.studio.powerbell.GlobalApplication;
import cc.winboll.studio.powerbell.fragments.MainViewFragment;
import cc.winboll.studio.powerbell.utils.AppConfigUtils;
import cc.winboll.studio.powerbell.utils.BatteryUtils;
import cc.winboll.studio.powerbell.utils.NotificationHelper;

public class GlobalApplicationReceiver extends BroadcastReceiver {

    public static final String TAG = "GlobalApplicationReceiver";

    AppConfigUtils mAppConfigUtils;
    GlobalApplication mGlobalApplication;
    // 存储电量指示值，
    // 用于校验电量消息时的电量变化
    static volatile int _mnTheQuantityOfElectricityOld = -1;
    static volatile boolean _mIsCharging = false;
    // 保存当前实例，
    // 便利封装 registerAction() 函数
    GlobalApplicationReceiver mReceiver;

    public GlobalApplicationReceiver(GlobalApplication globalApplication) {
        mReceiver = this;
        mGlobalApplication = globalApplication;
        mAppConfigUtils = GlobalApplication.getAppConfigUtils(mGlobalApplication);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
            // 先设置好新电池状态标志
            boolean isCharging = BatteryUtils.isCharging(intent);
            if (_mIsCharging != isCharging) {
                mAppConfigUtils.setIsCharging(isCharging);
            }
            int nTheQuantityOfElectricity = BatteryUtils.getTheQuantityOfElectricity(intent);
            if (_mnTheQuantityOfElectricityOld != nTheQuantityOfElectricity) {
                mAppConfigUtils.setCurrentValue(nTheQuantityOfElectricity);
            }
            // 新电池状态标志某一个有变化就更新显示信息
            if (_mIsCharging != isCharging || _mnTheQuantityOfElectricityOld != nTheQuantityOfElectricity) {
                // 电池状态改变先取消旧的提醒消息
                //NotificationHelper.cancelRemindNotification(context);
                
                GlobalApplication.getAppCacheUtils(context).addChangingTime(nTheQuantityOfElectricity);
                MainViewFragment.sendMsgCurrentValueBattery(nTheQuantityOfElectricity);
                // 保存好新的电池状态标志
                _mIsCharging = isCharging;
                _mnTheQuantityOfElectricityOld = nTheQuantityOfElectricity;
            }
        }
    }

    // 注册 Receiver
    //
    public void registerAction() {
        IntentFilter filter=new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mGlobalApplication.registerReceiver(mReceiver, filter);
    }
}
