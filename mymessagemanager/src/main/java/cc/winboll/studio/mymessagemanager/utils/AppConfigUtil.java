package cc.winboll.studio.mymessagemanager.utils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/06/02 21:43:52
 * @Describe 应用配置工具类
 */
import android.content.Context;
import cc.winboll.studio.mymessagemanager.beans.AppConfigBean;

public class AppConfigUtil {
    
    public static final String TAG = "AppConfigUtil";
    
    static AppConfigUtil _mConfigUtil;
    Context mContext;
    public AppConfigBean mAppConfigBean;

    AppConfigUtil(Context context) {
        mContext = context;
        mAppConfigBean = AppConfigBean.loadBean(context, AppConfigBean.class);
        if(mAppConfigBean == null) {
            mAppConfigBean = new AppConfigBean();
            AppConfigBean.saveBean(context, mAppConfigBean);
        }
    }

    public static AppConfigUtil getInstance(Context context) {
        if (_mConfigUtil == null) {
            _mConfigUtil = new AppConfigUtil(context);
        }
        return _mConfigUtil;
    }
    
    public void reLoadConfig() {
        mAppConfigBean = AppConfigBean.loadBean(mContext, AppConfigBean.class);
    }
    
    public void saveConfig() {
        AppConfigBean.saveBean(mContext, mAppConfigBean);
    }
    
    public String getPhoneReplaceString() {
        //String phoneNumber = "+86 123 4567 8901"; // 带有国家代码和空格的手机号码
        //String filteredNumber = phoneNumber.replaceAll("^\\+86|\\s", ""); // 过滤国家代码和空格
        //LogUtils.d(TAG, filteredNumber);

        String szReplace = "\\s";
        if (mAppConfigBean.isMergeCountryCodePrefix()) {
            szReplace = "^\\+" + mAppConfigBean.getCountryCode() + "|\\s";
        }
        //LogUtils.d(TAG, "szReplace is : " + szReplace);
        return szReplace;
    }
}
