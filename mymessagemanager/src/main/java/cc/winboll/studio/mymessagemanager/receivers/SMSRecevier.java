package cc.winboll.studio.mymessagemanager.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import cc.winboll.studio.mymessagemanager.GlobalApplication;
import cc.winboll.studio.mymessagemanager.activitys.SMSActivity;
import cc.winboll.studio.mymessagemanager.beans.SMSBean;
import cc.winboll.studio.mymessagemanager.utils.AppConfigUtil;
import cc.winboll.studio.mymessagemanager.utils.NotificationUtil;
import cc.winboll.studio.mymessagemanager.utils.PhoneUtil;
import cc.winboll.studio.mymessagemanager.utils.SMSReceiveRuleUtil;
import cc.winboll.studio.mymessagemanager.utils.SMSRecycleUtil;
import cc.winboll.studio.mymessagemanager.utils.SMSUtil;
import cc.winboll.studio.mymessagemanager.utils.TTSPlayRuleUtil;
import cc.winboll.studio.mymessagemanager.utils.RegexPPiUtils;

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
            PhoneUtil phoneUtil = new PhoneUtil(context);
            boolean isPhoneInContacts = phoneUtil.isPhoneInContacts(szSmsAddress);
            AppConfigUtil configUtil = AppConfigUtil.getInstance(context);
            boolean isOnlyReceiveContacts = configUtil.mAppConfigBean.isEnableOnlyReceiveContacts();
            boolean isEnableTTS = configUtil.mAppConfigBean.isEnableTTS();
            boolean isEnableTTSAnalyzeMode = configUtil.mAppConfigBean.isEnableTTSRuleMode();
            boolean isInSMSAcceptRule = SMSReceiveRuleUtil.getInstance(context, false).checkIsSMSAcceptInRule(context, szSmsBody);
            //LogUtils.d(TAG, "isInSMSAcceptRule is : " + Boolean.toString(isInSMSAcceptRule));

            if (!isPhoneInContacts) {
                GlobalApplication.showApplicationMessage(" The phone number " + szSmsAddress + " is not in contacts.");
                if (isOnlyReceiveContacts) {
                    GlobalApplication.showApplicationMessage("Close the \"Only Receive Contacts\" switch will be receive The " + szSmsAddress + "'s message in future.");
                }
            }

            if ((!isOnlyReceiveContacts)
                || isPhoneInContacts
                || isInSMSAcceptRule) {
                int nResultId = SMSUtil.saveReceiveSms(context, szSmsAddress, szSmsBody, "0", System.currentTimeMillis(), "inbox");
                if (nResultId >= 0) {
                    NotificationUtil nu = new NotificationUtil();
                    nu.sendSMSReceivedMessage(context, nResultId, szSmsAddress, szSmsBody);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(SMSActivity.ACTION_NOTIFY_SMS_CHANGED));
                    GlobalApplication.showApplicationMessage("<" + szSmsAddress + "> : ( " + szSmsBody + " ) [SAVED]");
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


}
    
    
    

