package cc.winboll.studio.powerbell.utils;

import android.app.Activity;
import android.content.Context;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.App;
import cc.winboll.studio.powerbell.MainActivity;
import cc.winboll.studio.powerbell.beans.AppConfigBean;
import cc.winboll.studio.powerbell.beans.ControlCenterServiceBean;
import cc.winboll.studio.powerbell.dialogs.YesNoAlertDialog;
import cc.winboll.studio.powerbell.fragments.MainViewFragment;
import cc.winboll.studio.powerbell.services.ControlCenterService;
import java.io.File;

// 应用配置工具类
//
public class AppConfigUtils {
    public static final String TAG = "AppConfigUtils";

    public static final String BACKGROUND_DIR = "Background";

    // 保存唯一配置实例
    static AppConfigUtils _mAppConfigUtils;
    // 应用环境上下文
    Context mContext;

    // 是否启动铃声提醒服务
    volatile boolean mIsEnableService = false;

    public volatile AppConfigBean mAppConfigBean;
    
    // 电池充电提醒值。
    // Battery charge reminder value.
    volatile int mnChargeReminderValue = -1;
    volatile boolean mIsEnableChargeReminder = false;
    // 电池耗电量提醒值。
    // Battery power usege reminder value.
    volatile int mnUsegeReminderValue = -1;
    volatile boolean mIsEnableUsegeReminder = false;

    volatile boolean mIsUseBackgroundFile = false;
    volatile String mszBackgroundFileName = "";

    // 保存应用实例
    App mApplication;

    AppConfigUtils(Context context) {
        mContext = context;
        String szExternalFilesDir = mContext.getExternalFilesDir(TAG) + File.separator;
        //mlistAppConfigBean = new ArrayList<AppConfigBean>();
        mAppConfigBean = new AppConfigBean();
        loadAppConfigBean();
    }

    // 返回唯一实例
    //
    public static AppConfigUtils getInstance(Context context) {
        if (_mAppConfigUtils == null) {
            _mAppConfigUtils = new AppConfigUtils(context);
        }
        return _mAppConfigUtils;
    }
    
    public void setIsEnableService(Activity activity, final boolean isEnableService) {
        YesNoAlertDialog.show(activity, "应用设置信息", "是否保存应用配置？", new YesNoAlertDialog.OnDialogResultListener(){
                @Override
                public void onYes() {
                    mIsEnableService = isEnableService;
                    ControlCenterServiceBean bean = new ControlCenterServiceBean(isEnableService);
                    ControlCenterServiceBean.saveBean(mContext, bean);
                    if (mIsEnableService) {
                        LogUtils.d(TAG, "startControlCenterService");
                        ControlCenterService.startControlCenterService(mContext);
                    } else {
                        LogUtils.d(TAG, "stopControlCenterService");
                        ControlCenterService.stopControlCenterService(mContext);
                    }
                }

                @Override
                public void onNo() {
                    MainViewFragment.relaodAppConfigs();
                }
            });
    }

    public boolean getIsEnableService() {
        ControlCenterServiceBean bean = ControlCenterServiceBean.loadBean(mContext, ControlCenterServiceBean.class);
        if (bean == null) {
            ControlCenterServiceBean.saveBean(mContext, new ControlCenterServiceBean(false));
            return false;
        }
        return bean.isEnableService();
    }

    public void setIsEnableChargeReminder(boolean isEnableChargeReminder) {
        mAppConfigBean.setIsEnableChargeReminder(isEnableChargeReminder);
        saveConfigData(MainActivity._mMainActivity);
    }

    public boolean getIsEnableChargeReminder() {
        return mAppConfigBean.isEnableChargeReminder();
    }

    public void setIsEnableUsegeReminder(boolean isEnableUsegeReminder) {
        mAppConfigBean.setIsEnableUsegeReminder(isEnableUsegeReminder);
        saveConfigData(MainActivity._mMainActivity);
    }

    public boolean getIsEnableUsegeReminder() {
        return mAppConfigBean.isEnableUsegeReminder();
    }

    public void setChargeReminderValue(int value) {
        mAppConfigBean.setChargeReminderValue(value);
        saveConfigData(MainActivity._mMainActivity);
    }

    public int getChargeReminderValue() {
        return mAppConfigBean.getChargeReminderValue();
    }

    public void setUsegeReminderValue(int value) {
        mAppConfigBean.setUsegeReminderValue(value);
        saveConfigData(MainActivity._mMainActivity);
    }

    public int getUsegeReminderValue() {
        return mAppConfigBean.getUsegeReminderValue();
    }

    public void setIsCharging(boolean isCharging) {
        mAppConfigBean.setIsCharging(isCharging);
    }

    public boolean isCharging() {
        return mAppConfigBean.isCharging();
    }

    public void setCurrentValue(int nCurrentValue) {
        mAppConfigBean.setCurrentValue(nCurrentValue);
    }

    public int getCurrentValue() {
        return mAppConfigBean.getCurrentValue();
    }

    public void setReminderIntervalTime(int reminderIntervalTime) {
        mAppConfigBean.setReminderIntervalTime(reminderIntervalTime);
    }

    public int getReminderIntervalTime() {
        return mAppConfigBean.getReminderIntervalTime();
    }

    //
    // 加载电池提醒配置数据
    //
    public void loadAppConfigBean() {
        AppConfigBean bean = AppConfigBean.loadBean(mContext, AppConfigBean.class);
        if (bean == null) {
            bean = new AppConfigBean();
            AppConfigBean.saveBean(mContext, mAppConfigBean);
        }
        mAppConfigBean.setIsEnableUsegeReminder(bean.isEnableUsegeReminder());
        mAppConfigBean.setUsegeReminderValue(bean.getUsegeReminderValue());
        mAppConfigBean.setIsEnableChargeReminder(bean.isEnableChargeReminder());
        mAppConfigBean.setChargeReminderValue(bean.getChargeReminderValue());
    }
    
    public void saveConfigData(final MainActivity activity) {
        if (MainActivity._mMainActivity == null) {
            return;
        }

        YesNoAlertDialog.show(activity, "应用设置信息", "是否保存应用配置？", new YesNoAlertDialog.OnDialogResultListener(){

                @Override
                public void onYes() {
                    saveConfigData();
                }

                @Override
                public void onNo() {
                    AppConfigUtils.getInstance(activity).loadAppConfigBean();
                    MainViewFragment.relaodAppConfigs();
                }
            });
    }

    //
    // 保存应用配置数据
    //
    void saveConfigData() {
        // 更新配置先取消一下旧的的提醒消息
        //NotificationHelper.cancelRemindNotification(mContext);
        
        AppConfigBean.saveBean(mContext, mAppConfigBean);
        // 通知活动窗口和服务配置已更新
        ControlCenterService.updateStatus(mContext, mAppConfigBean);
        MainViewFragment.relaodAppConfigs();
    }
}
