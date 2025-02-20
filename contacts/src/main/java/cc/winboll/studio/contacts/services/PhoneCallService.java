package cc.winboll.studio.contacts.services;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/20 19:58:02
 * @Describe MyPhoneCallService
 */
import android.telecom.Call;
import android.telecom.InCallService;
import cc.winboll.studio.contacts.PhoneCallManager;

public class PhoneCallService extends InCallService {

    public static final String TAG = "PhoneCallService";

    private Call.Callback callback = new Call.Callback() {
        @Override
        public void onStateChanged(Call call, int state) {
            super.onStateChanged(call, state);
            switch (state) {
                case Call.STATE_ACTIVE: {
                        break; // 通话中
                    }
                case Call.STATE_DISCONNECTED: {
                        break; // 通话结束
                    }
            }
        }
    };

//    @Override
//    public void onCallAdded(Call call) {
//        super.onCallAdded(call);
//
//        call.registerCallback(callback);
//    }
//
//    @Override
//    public void onCallRemoved(Call call) {
//        super.onCallRemoved(call);
//
//        call.unregisterCallback(callback);
//    }
    
    @Override
    public void onCallAdded(Call call) {
        super.onCallAdded(call);

        call.registerCallback(callback);
        PhoneCallManager.call = call; // 传入call

//        CallType callType = null;
//
//        if (call.getState() == Call.STATE_RINGING) {
//            callType = CallType.CALL_IN;
//        } else if (call.getState() == Call.STATE_CONNECTING) {
//            callType = CallType.CALL_OUT;
//        }
//
//        if (callType != null) {
//            Call.Details details = call.getDetails();
//            String phoneNumber = details.getHandle().toString().substring(4)
//                .replaceAll("%20", ""); // 去除拨出电话中的空格
//            PhoneCallActivity.actionStart(this, phoneNumber, callType);
//        }
    }

    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);

        call.unregisterCallback(callback);
        PhoneCallManager.call = null;
    }
}
