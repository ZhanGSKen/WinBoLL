package cc.winboll.studio.timestamp.utils;

/**
 * @Author ZhanGSKen
 * @Date 2025/05/05 14:00
 * @Describe AppConfigsUtil
 */
import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import cc.winboll.studio.timestamp.models.AppConfigsModel;
import java.io.IOException;

public class AppConfigsUtil {
    
    public static final String TAG = "AppConfigsUtil";
    
    volatile static AppConfigsUtil _AppConfigsUtil;
    Context mContext;
    AppConfigsModel mAppConfigsModel;
    
    AppConfigsUtil(Context context) {
        this.mContext = context;
    }
    
    public synchronized static AppConfigsUtil getInstance(Context context){
        if(_AppConfigsUtil == null) {
            _AppConfigsUtil = new AppConfigsUtil(context);
            _AppConfigsUtil.loadAppConfigs();
        }
        return _AppConfigsUtil;
    }
    
    public AppConfigsModel getAppConfigsModel() {
        return mAppConfigsModel;
    }

    public AppConfigsModel loadAppConfigs() {
        AppConfigsModel appConfigsModel = null;
        appConfigsModel = AppConfigsModel.loadBean(mContext, AppConfigsModel.class);
        if (appConfigsModel != null) {
            mAppConfigsModel = appConfigsModel;
        } else {
            saveAppConfigs(new AppConfigsModel());
            _AppConfigsUtil = this;
        }
        return mAppConfigsModel;
    }

    public void saveAppConfigs(AppConfigsModel appConfigsModel) {
        AppConfigsModel.saveBean(mContext, appConfigsModel);
    }
    
    public void saveAppConfigs() {
        AppConfigsModel.saveBean(mContext, mAppConfigsModel);
    }
}
