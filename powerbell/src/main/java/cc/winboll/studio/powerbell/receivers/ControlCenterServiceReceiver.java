package cc.winboll.studio.powerbell.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import cc.winboll.studio.powerbell.beans.AppConfigBean;
import cc.winboll.studio.powerbell.beans.BatteryData;
import cc.winboll.studio.powerbell.services.ControlCenterService;
import cc.winboll.studio.powerbell.utils.AppConfigUtils;
import cc.winboll.studio.powerbell.utils.BatteryUtils;
import cc.winboll.studio.shared.log.LogUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ControlCenterServiceReceiver extends BroadcastReceiver {
    public static final String TAG = ControlCenterServiceReceiver.class.getSimpleName();

    public static final String ACTION_UPDATE_SERVICENOTIFICATION = ControlCenterServiceReceiver.class.getName() + ".ACTION_UPDATE_NOTIFICATION";
    public static final String ACTION_START_REMINDTHREAD = ControlCenterServiceReceiver.class.getName() + ".ACTION_UPDATE_REMINDTHREAD";

    WeakReference<ControlCenterService> mwrService;
    // 存储电量指示值，
    // 用于校验电量消息时的电量变化
    static volatile int _mnTheQuantityOfElectricityOld = -1;
    static volatile boolean _mIsCharging = false;

    public ControlCenterServiceReceiver(ControlCenterService service) {
        mwrService = new WeakReference<ControlCenterService>(service);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_UPDATE_SERVICENOTIFICATION)) {
            mwrService.get().updateServiceNotification();
        } else if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
            boolean isCharging = BatteryUtils.isCharging(intent);
            int nTheQuantityOfElectricity = BatteryUtils.getTheQuantityOfElectricity(intent);
            if (mwrService.get().getRemindThread() != null) {
                // 先设置提醒进程电池状态标志
                if (_mIsCharging != isCharging) {
                    mwrService.get().getRemindThread().setIsCharging(isCharging);
                }
                if (_mnTheQuantityOfElectricityOld != nTheQuantityOfElectricity) {
                    mwrService.get().getRemindThread().setQuantityOfElectricity(nTheQuantityOfElectricity);
                }
            }
            // 新电池状态标志某一个有变化就更新显示信息
            if (_mIsCharging != isCharging || _mnTheQuantityOfElectricityOld != nTheQuantityOfElectricity) {
                mwrService.get().updateServiceNotification();
                AppConfigUtils appConfigUtils = AppConfigUtils.getInstance(context);
                appConfigUtils.loadAppConfigBean();
                AppConfigBean appConfigBean = appConfigUtils.mAppConfigBean;
                appConfigBean.setCurrentValue(nTheQuantityOfElectricity);
                appConfigBean.setIsCharging(isCharging);
                mwrService.get().startRemindThread(appConfigBean);
                
                // 保存电池报告
                // 示例数据更新逻辑
//                List<BatteryData> newData = new ArrayList<>(adapter.getDataList());
//                newData.add(0, new BatteryData(percentage, "00:00:00", "00:00:00"));
//                adapter.updateData(newData);
                
                // 保存好新的电池状态标志
                _mIsCharging = isCharging;
                _mnTheQuantityOfElectricityOld = nTheQuantityOfElectricity;
            }
        } else if (intent.getAction().equals(ACTION_START_REMINDTHREAD)) {
            LogUtils.d(TAG, "ACTION_START_REMINDTHREAD");
            //AppConfigUtils appConfigUtils = AppConfigUtils.getInstance(context);
            //appConfigUtils.loadAppConfigBean();
            AppConfigBean appConfigBean = (AppConfigBean)intent.getSerializableExtra("appConfigBean");
            mwrService.get().startRemindThread(appConfigBean);
        }
    }

    // 注册 Receiver
    //
    public void registerAction(Context context) {
        IntentFilter filter=new IntentFilter();
        filter.addAction(ACTION_UPDATE_SERVICENOTIFICATION);
        filter.addAction(ACTION_START_REMINDTHREAD);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(this, filter);
    }
}
    
    
    
