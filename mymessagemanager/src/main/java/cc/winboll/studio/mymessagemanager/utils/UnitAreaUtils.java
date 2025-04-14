package cc.winboll.studio.mymessagemanager.utils;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/04/14 15:55:36
 * @Describe 电话号码区域管理辅助类
 */
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.mymessagemanager.beans.AppConfigBean;
import android.content.Context;

public class UnitAreaUtils {
    
    public static final String TAG = "UnitAreaUtils";
    
    static UnitAreaUtils _UnitAreaUtils;
    Context mContext;

    UnitAreaUtils(Context context) {
        mContext = context;
    }

    public static UnitAreaUtils getInstance(Context context) {
        if (_UnitAreaUtils == null) {
            _UnitAreaUtils = new UnitAreaUtils(context);
        }
        return _UnitAreaUtils;
    }
    
    public boolean isCurrentUnitAreaNumber(String szPhoneNumer) {
        String szUnitArea = getUnitArea();
        LogUtils.d(TAG, String.format("szPhoneNumer.substring(1,3) %s", szPhoneNumer.substring(1,3)));
        return szPhoneNumer.substring(1,3).equals(szUnitArea);
    }
    
    public String genCurrentUnitAreaNumber(String szPhoneNumer) {
        String szUnitArea = getUnitArea();
        LogUtils.d(TAG, String.format("szUnitArea %s", szUnitArea));
        return "+" + szUnitArea + szPhoneNumer;
    }
    
    String getUnitArea() {
        String szUnitArea = AppConfigUtil.getInstance(mContext).mAppConfigBean.getCountryCode();
        LogUtils.d(TAG, String.format("szUnitArea %s", szUnitArea));
        return szUnitArea;
    }
}
