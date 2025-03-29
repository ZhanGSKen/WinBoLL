package cc.winboll.studio.mymessagemanager.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.mymessagemanager.App;
import cc.winboll.studio.mymessagemanager.activitys.SMSActivity;
import cc.winboll.studio.mymessagemanager.beans.SMSBean;
import cc.winboll.studio.mymessagemanager.utils.AppConfigUtil;
import cc.winboll.studio.mymessagemanager.utils.NotificationUtil;
import cc.winboll.studio.mymessagemanager.utils.PhoneUtil;
import cc.winboll.studio.mymessagemanager.utils.SMSReceiveRuleUtil;
import cc.winboll.studio.mymessagemanager.utils.SMSRecycleUtil;
import cc.winboll.studio.mymessagemanager.utils.SMSUtil;
import cc.winboll.studio.mymessagemanager.utils.TTSPlayRuleUtil;

public class SMSRecevier extends BroadcastReceiver {

    public static String TAG = "SMSRecevier";

    public static String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    public SMSRecevier() {
        super();
        //LogUtils.d(TAG, "SMSRecevier()");
    }

    /*public void init(ManagerService.SMSListener smsListener) {
     mSMSListener = smsListener;
     }*/

    @Override
    public void onReceive(Context context, Intent intent) {

        String szAction = intent.getAction();
        if (szAction.equals(ACTION_SMS_RECEIVED)) {
            //LogUtils.d(TAG, "ACTION_SMS_RECEIVED");
            String szSmsBody = SMSUtil.getSmsBody(intent);
            String szSmsAddress = SMSUtil.getSmsAddress(intent);
            AppConfigUtil configUtil = AppConfigUtil.getInstance(context);
            boolean isEnableTTS = configUtil.mAppConfigBean.isEnableTTS();
            boolean isEnableTTSAnalyzeMode = configUtil.mAppConfigBean.isEnableTTSRuleMode();

            if (checkIsSMSOK(context, szSmsBody, szSmsAddress)) {
                int nResultId = SMSUtil.saveReceiveSms(context, szSmsAddress, szSmsBody, "0", System.currentTimeMillis(), "inbox");
                if (nResultId >= 0) {
                    NotificationUtil nu = new NotificationUtil();
                    nu.sendSMSReceivedMessage(context, nResultId, szSmsAddress, szSmsBody);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(SMSActivity.ACTION_NOTIFY_SMS_CHANGED));
                    LogUtils.d(TAG, "<" + szSmsAddress + "> : ( " + szSmsBody + " ) [SAVED]");
                    if (isEnableTTS) {
                        if (isEnableTTSAnalyzeMode) {
                            TTSPlayRuleUtil ttsPlayRuleUtil = TTSPlayRuleUtil.getInstance(context);
                            ttsPlayRuleUtil.speakTTSAnalyzeModeText(szSmsBody, configUtil.mAppConfigBean.getTtsPlayDelayTimes());
                        } else {
                            TTSPlayRuleUtil.speakText(context, szSmsBody, configUtil.mAppConfigBean.getTtsPlayDelayTimes(), 0);
                        }

                    }
                }

                abortBroadcast();
            } else {
                SMSBean bean = new SMSBean(-1, szSmsAddress, szSmsBody, System.currentTimeMillis(), SMSBean.Type.INBOX, SMSBean.ReadStatus.UNREAD, 0);
                SMSRecycleUtil.addSMSRecycleItem(context, bean);
            }
        }
    }

    //
    // 检查短信是否在接收设定规则内
    //
    public static boolean checkIsSMSOK(Context context, String szSmsBody, String szSmsAddress) {
        PhoneUtil phoneUtil = new PhoneUtil(context);
        boolean isPhoneInContacts = phoneUtil.isPhoneInContacts(szSmsAddress);
        LogUtils.d(TAG, String.format("isPhoneInContacts %s", isPhoneInContacts));

        boolean isPhoneByDigit = phoneUtil.isPhoneByDigit(szSmsAddress);
        LogUtils.d(TAG, String.format("isPhoneByDigit %s", isPhoneByDigit));

        AppConfigUtil configUtil = AppConfigUtil.getInstance(context);
        boolean isOnlyReceiveContacts = configUtil.mAppConfigBean.isEnableOnlyReceiveContacts();
        LogUtils.d(TAG, String.format("isOnlyReceiveContacts %s", isOnlyReceiveContacts));

        boolean isInSMSAcceptRule = SMSReceiveRuleUtil.getInstance(context, false).checkIsSMSAcceptInRule(context, szSmsBody);
        LogUtils.d(TAG, String.format("isInSMSAcceptRule %s", isInSMSAcceptRule));

        // 启用了只接受通讯录，通讯录里有记录
        if (isOnlyReceiveContacts && isPhoneInContacts) {
            return true;
        }
        // 如果不是数字通讯地址，但是在通讯录内
        if (!isPhoneByDigit && isPhoneInContacts) {
            return true;
        }
        // 通讯地址是数字，并且在短信接收规则内。
        if (isPhoneByDigit && isInSMSAcceptRule) {
            return true;
        }
        
        return false;
    }
}
    
    
    

