package cc.winboll.studio.contacts.phonecallui;

/**
 * 监听电话通信状态的服务，实现该类的同时必须提供电话管理的 UI
 *
 * @author aJIEw
 * @see PhoneCallActivity
 * @see android.telecom.InCallService
 */
import android.media.AudioManager;
import android.os.Build;
import android.telecom.Call;
import android.telecom.InCallService;
import androidx.annotation.RequiresApi;
import cc.winboll.studio.contacts.ActivityStack;
import cc.winboll.studio.contacts.dun.Rules;

@RequiresApi(api = Build.VERSION_CODES.M)
public class PhoneCallService extends InCallService {
    
    private int originalRingVolume;
    
    private final Call.Callback callback = new Call.Callback() {
        @Override
        public void onStateChanged(Call call, int state) {
            super.onStateChanged(call, state);
            switch (state) {
                case Call.STATE_ACTIVE: {
                        break;
                    }

                case Call.STATE_DISCONNECTED: {
                        ActivityStack.getInstance().finishActivity(PhoneCallActivity.class);
                        break;
                    }

            }
        }
    };

    @Override
    public void onCallAdded(Call call) {
        super.onCallAdded(call);
        
        call.registerCallback(callback);
        PhoneCallManager.call = call;
        CallType callType = null;

        if (call.getState() == Call.STATE_RINGING) {
            callType = CallType.CALL_IN;
        } else if (call.getState() == Call.STATE_CONNECTING) {
            callType = CallType.CALL_OUT;
        }

        if (callType != null) {
            Call.Details details = call.getDetails();
            String phoneNumber = details.getHandle().getSchemeSpecificPart();

            // 记录原始铃声音量
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            originalRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
            if (!Rules.isAllowed(phoneNumber)) {
                // 预先静音
                audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
                call.disconnect();
                return;
            }
            
            PhoneCallActivity.actionStart(this, phoneNumber, callType);
        }
    }

    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);

        call.unregisterCallback(callback);
        PhoneCallManager.call = null;
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        // 恢复铃声音量
        audioManager.setStreamVolume(AudioManager.STREAM_RING, originalRingVolume, 0);
    }

    public enum CallType {
        CALL_IN,
        CALL_OUT,
    }
}
