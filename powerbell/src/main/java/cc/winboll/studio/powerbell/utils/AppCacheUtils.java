package cc.winboll.studio.powerbell.utils;

import android.content.Context;
import cc.winboll.studio.powerbell.beans.BatteryInfoBean;
import cc.winboll.studio.shared.log.LogUtils;
import java.util.ArrayList;

public class AppCacheUtils {
    public static final String TAG = "AppCacheUtils";

    // 保存唯一配置实例
    static AppCacheUtils _mAppCacheUtils;
    // 配置实例引用的上下文环境
    Context mContext;
    // 配置实例的数据的存储文件路径
    //volatile String mAppCacheDataFilePath = null;
    ArrayList<BatteryInfoBean> mlBatteryInfo;

    // 私有实例构造方法
    //
    AppCacheUtils(Context context) {
        mContext = context;
        //mAppCacheDataFilePath = context.getExternalFilesDir(TAG) + File.separator + "mlBatteryInfo.dat";
        mlBatteryInfo = new ArrayList<BatteryInfoBean>();
        loadAppCacheData();
    }

    // 返回唯一实例
    //
    public static synchronized AppCacheUtils getInstance(Context context) {
        if (_mAppCacheUtils == null) {
            _mAppCacheUtils = new AppCacheUtils(context);
        }
        return _mAppCacheUtils;
    }

    // 添加电量改变时间
    //
    public void addChangingTime(int nBattetyValue) {
        if (mlBatteryInfo.size() == 0) {
            addChangingTimeToList(nBattetyValue);
            //LogUtils.d(TAG, "nBattetyValue is "+Integer.toString(nBattetyValue));
            return;
        }
        if (mlBatteryInfo.get(mlBatteryInfo.size() - 1).getBattetyValue() != nBattetyValue) {
            addChangingTimeToList(nBattetyValue);
            //LogUtils.d(TAG, "nBattetyValue is "+Integer.toString(nBattetyValue));

        }
    }

    void addChangingTimeToList(int nBattetyValue) {
        if (mlBatteryInfo.size() > 180) {
            mlBatteryInfo.remove(0);
        }
        BatteryInfoBean batteryInfo = new BatteryInfoBean(System.currentTimeMillis(), nBattetyValue);
        LogUtils.d(TAG, "getBattetyValue is " + Integer.toString(batteryInfo.getBattetyValue()));
        LogUtils.d(TAG, "getTimeStamp is " + Long.toString(batteryInfo.getTimeStamp()));
        mlBatteryInfo.add(batteryInfo);
        saveAppCacheData();
    }

    public ArrayList<BatteryInfoBean> getArrayListBatteryInfo() {
        loadAppCacheData();
        return mlBatteryInfo;
    }

    // 读取文件存储的数据
    //
    void saveAppCacheData() {
        BatteryInfoBean.saveBeanList(mContext, mlBatteryInfo, BatteryInfoBean.class);
    }

    // 保存数据到文件
    //
    void loadAppCacheData() {
        mlBatteryInfo.clear();
        BatteryInfoBean.loadBeanList(mContext, mlBatteryInfo, BatteryInfoBean.class);
    }

    public void clearBatteryHistory() {
        mlBatteryInfo.clear();
        saveAppCacheData();
    }
}
