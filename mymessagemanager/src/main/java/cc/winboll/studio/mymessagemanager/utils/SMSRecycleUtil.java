package cc.winboll.studio.mymessagemanager.utils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/19 19:29:59
 * @Describe 短信回收站工具类
 */
import android.content.Context;
import cc.winboll.studio.mymessagemanager.App;
import cc.winboll.studio.mymessagemanager.beans.SMSBean;
import cc.winboll.studio.mymessagemanager.beans.SMSRecycleBean;
import java.util.ArrayList;

public class SMSRecycleUtil {
    
    public static final String TAG = "SMSRecycleUtil";
    
    public static String getSMSRecycleListDataPath(Context context) {
        if(cc.winboll.studio.mymessagemanager.BuildConfig.DEBUG) {
            return context.getExternalFilesDir(TAG) + "/mSMSRecycleList.json";
        } else {
            return context.getDataDir() + "/home/" + TAG + "/mSMSRecycleList.json";
        }
    }
    
    public static void addSMSRecycleItem(Context context, SMSBean bean) {
        ArrayList<SMSRecycleBean> list = new ArrayList<SMSRecycleBean>();
        SMSRecycleBean.loadBeanListFromFile(getSMSRecycleListDataPath(context), list, SMSRecycleBean.class);
        SMSRecycleBean smsRecycleBean = new SMSRecycleBean(bean, System.currentTimeMillis());
        list.add(smsRecycleBean);
        SMSRecycleBean.saveBeanListToFile(getSMSRecycleListDataPath(context), list);
    }
}
