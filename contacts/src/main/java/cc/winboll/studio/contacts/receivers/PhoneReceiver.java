package cc.winboll.studio.contacts.receivers;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/20 17:54:55
 * @Describe PhoneCallReceiver
 */
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.telephony.TelephonyManager;
//import android.util.Log;
//import cc.winboll.studio.contacts.services.PhoneCallService;
//
//public class PhoneReceiver extends BroadcastReceiver {
//
//    public static final String TAG = "PhoneReceiver";
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
//            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
//            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
//                String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
//                Log.d(TAG, "Incoming call from: " + phoneNumber);
//
//                // 启动服务来处理电话接听
//                Intent serviceIntent = new Intent(context, PhoneCallService.class);
//                serviceIntent.putExtra("phoneNumber", phoneNumber);
//                context.startService(serviceIntent);
//            }
//        }
//    }
//}
