package cc.winboll.studio.contacts.phonecallui;

/**
 * 监听电话通信状态的服务，实现该类的同时必须提供电话管理的 UI
 *
 * @author aJIEw
 * @see PhoneCallActivity
 * @see android.telecom.InCallService
 */
import android.content.ContentResolver;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;
import android.telecom.Call;
import android.telecom.InCallService;
import android.telephony.TelephonyManager;
import androidx.annotation.RequiresApi;
import cc.winboll.studio.contacts.ActivityStack;
import cc.winboll.studio.contacts.beans.RingTongBean;
import cc.winboll.studio.contacts.dun.Rules;
import cc.winboll.studio.contacts.fragments.CallLogFragment;
import cc.winboll.studio.libappbase.LogUtils;
import java.io.File;
import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.M)
public class PhoneCallService extends InCallService {

    public static final String TAG = "PhoneCallService";

    MediaRecorder mediaRecorder;

    private final Call.Callback callback = new Call.Callback() {
        @Override
        public void onStateChanged(Call call, int state) {
            super.onStateChanged(call, state);
            switch (state) {
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    {
                        long callId = getCurrentCallId();
                        if (callId != -1) {
                            // 在这里可以对获取到的通话记录ID进行处理
                            //System.out.println("当前通话记录ID: " + callId);

                            // 电话接通，开始录音
                            startRecording(callId);
                        }
                        break;
                    }
                case TelephonyManager.CALL_STATE_IDLE:
                    // 电话挂断，停止录音
                    stopRecording();
                    break;
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
            int ringerVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
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
                if (ringerVolume != bean.getStreamVolume()) {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, bean.getStreamVolume(), 0);
                    //audioManager.setMode(AudioManager.RINGER_MODE_NORMAL);
                }
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

    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);
        call.unregisterCallback(callback);
        PhoneCallManager.call = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CallLogFragment.updateCallLogFragment();
    }

    public enum CallType {
        CALL_IN,
        CALL_OUT,
    }


    private void startRecording(long callId) {
        LogUtils.d(TAG, "startRecording(...)");
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(getOutputFilePath(callId));
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
    }

    private String getOutputFilePath(long callId) {
        LogUtils.d(TAG, "getOutputFilePath(...)");
        // 设置录音文件的保存路径
        File file = new File(getExternalFilesDir(TAG), String.format("call_%d.mp4", callId));
        return file.getAbsolutePath();
    }

    private void stopRecording() {
        LogUtils.d(TAG, "stopRecording()");
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private long getCurrentCallId() {
        LogUtils.d(TAG, "getCurrentCallId()");
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        Uri callLogUri = Uri.parse("content://call_log/calls");
        String[] projection = {"_id", "number", "call_type", "date"};
        String selection = "call_type = " + CallLog.Calls.OUTGOING_TYPE + " OR call_type = " + CallLog.Calls.INCOMING_TYPE;
        String sortOrder = "date DESC";

        try {
            Cursor cursor = contentResolver.query(callLogUri, projection, selection, null, sortOrder);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndex("_id"));
            }
        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }

        return -1;
    }
}
