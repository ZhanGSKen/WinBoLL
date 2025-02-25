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
import cc.winboll.studio.contacts.beans.RingTongBean;
import cc.winboll.studio.contacts.dun.Rules;
import cc.winboll.studio.libappbase.LogUtils;

@RequiresApi(api = Build.VERSION_CODES.M)
public class PhoneCallService extends InCallService {

    public static final String TAG = "PhoneCallService";

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
            //
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

            // 恢复铃声音量，预防其他意外条件导致的音量变化问题
            //

            // 读取应用配置，未配置就初始化配置文件
            RingTongBean bean = RingTongBean.loadBean(this, RingTongBean.class);
            if (bean == null) {
                // 初始化配置
                bean = new RingTongBean();
                RingTongBean.saveBean(this, bean);
            }
            // 如果当前音量和应用保存的不一致就恢复为应用设定值
            // 恢复铃声音量
            try {
                audioManager.setStreamVolume(AudioManager.STREAM_RING, bean.getStreamVolume(), 0);
                //audioManager.setMode(AudioManager.RINGER_MODE_NORMAL);
            } catch (java.lang.SecurityException e) {
                LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
            }

            // 检查电话接收规则
            if (!Rules.getInstance(this).isAllowed(phoneNumber)) {
                // 调低音量
                try {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
                    //audioManager.setMode(AudioManager.RINGER_MODE_SILENT);
                } catch (java.lang.SecurityException e) {
                    LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                }
                // 断开电话
                call.disconnect();
                // 停顿1秒，预防第一声铃声响动
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    LogUtils.d(TAG, "");
                }
                // 恢复铃声音量
                try {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, bean.getStreamVolume(), 0);
                    //audioManager.setMode(AudioManager.RINGER_MODE_NORMAL);
                } catch (java.lang.SecurityException e) {
                    LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                }
                // 屏蔽电话结束
                return;
            }

            // 正常接听电话
            PhoneCallActivity.actionStart(this, phoneNumber, callType);
        }
    }

    void resumeStreamVolume(AudioManager audioManager, int originalRingVolume) {
        // 如果当前音量和应用保存的不一致就恢复为应用设定值
        RingTongBean bean = RingTongBean.loadBean(this, RingTongBean.class);
        if (bean == null) {
            bean = new RingTongBean();
        }
        if (originalRingVolume != bean.getStreamVolume()) {
            // 恢复铃声音量
            audioManager.setStreamVolume(AudioManager.STREAM_RING, bean.getStreamVolume(), 0);
        }
    }

    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);
        call.unregisterCallback(callback);
        PhoneCallManager.call = null;
    }

    public enum CallType {
        CALL_IN,
        CALL_OUT,
    }
}
