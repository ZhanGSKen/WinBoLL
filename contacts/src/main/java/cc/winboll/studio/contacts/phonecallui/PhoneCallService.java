package cc.winboll.studio.contacts.phonecallui;

import android.media.AudioManager;
import android.telecom.Call;
import android.telecom.InCallService;
import android.telephony.TelephonyManager;
import androidx.annotation.RequiresApi;
import cc.winboll.studio.contacts.ActivityStack;
import cc.winboll.studio.contacts.dun.Rules;
import cc.winboll.studio.contacts.fragments.CallLogFragment;
import cc.winboll.studio.contacts.model.RingTongBean;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * 监听电话通信状态的服务，实现该类的同时必须提供电话管理的 UI
 * @author aJIEw, ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @see PhoneCallActivity
 * @see android.telecom.InCallService
 * 适配：Java7 语法 + Android API29 - 30 | 移除录音功能 | 强化小米设备稳定性与容错性
 */
@RequiresApi(api = 29)
public class PhoneCallService extends InCallService {
    // 常量定义区
    public static final String TAG = "PhoneCallService";
    // 小米设备适配标识，便于日志区分
    private static final String MI_DEVICE_TAG = "MiDeviceAdapt";

    // 成员属性区（按依赖顺序排列）
    private Call.Callback mCallCallback;
    private AudioManager mAudioManager;

    // 内部枚举类（通话类型定义）
    public enum CallType {
        CALL_IN,  // 来电
        CALL_OUT  // 去电
		}

    // Service生命周期方法区（按执行流程排序）
    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, MI_DEVICE_TAG + " 通话监听服务启动");
        initAudioManager();
        initCallCallback();
        LogUtils.d(TAG, MI_DEVICE_TAG + " 服务初始化完成");
    }

    @Override
    public void onCallAdded(Call call) {
        super.onCallAdded(call);
        LogUtils.d(TAG, "检测到新通话");
        if (call == null) {
            LogUtils.e(TAG, "通话对象为空，跳过处理");
            return;
        }

        // 双重校验回调，避免重复注册
        if (mCallCallback != null) {
            call.registerCallback(mCallCallback);
        }
        // 绑定通话对象到管理器，供UI层调用
        PhoneCallManager.call = call;
        LogUtils.d(TAG, MI_DEVICE_TAG + " 通话回调注册成功，对象绑定完成");

        CallType callType = judgeCallType(call);
        if (callType != null) {
            handleValidCall(call, callType);
        } else {
            LogUtils.w(TAG, "无法识别通话类型，状态码：" + call.getState());
        }
    }

    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);
        LogUtils.d(TAG, "通话结束，开始清理资源");
        if (call != null && mCallCallback != null) {
            call.unregisterCallback(mCallCallback);
            LogUtils.d(TAG, "通话回调已注销");
        }

        // 延迟置空通话对象，避免UI层挂断时对象已被释放（适配小米机型时序）
        new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						// 延迟200ms，确保PhoneCallActivity挂断逻辑执行完成
						Thread.sleep(200);
						PhoneCallManager.call = null;
					} catch (InterruptedException e) {
						LogUtils.e(TAG, MI_DEVICE_TAG + " 延迟置空通话对象异常", e);
					}
				}
			}).start();

        PhoneCallActivity.closePhoneCallActivity();
        LogUtils.d(TAG, MI_DEVICE_TAG + " 通话资源清理完成");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "服务开始销毁");
        CallLogFragment.updateCallLogFragment();
        // 释放资源，适配小米设备内存管理，避免内存泄漏
        mCallCallback = null;
        mAudioManager = null;
        LogUtils.d(TAG, MI_DEVICE_TAG + " 服务销毁完成");
    }

    // 初始化方法区
    private void initAudioManager() {
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (mAudioManager == null) {
            LogUtils.e(TAG, MI_DEVICE_TAG + " 获取音频管理器失败");
        } else {
            LogUtils.d(TAG, MI_DEVICE_TAG + " 音频管理器初始化成功");
        }
    }

    private void initCallCallback() {
        mCallCallback = new Call.Callback() {
            @Override
            public void onStateChanged(Call call, int state) {
                super.onStateChanged(call, state);
                if (call == null) {
                    LogUtils.e(TAG, "onStateChanged: 通话对象为空");
                    return;
                }
                String stateDesc = getCallStateDesc(state);
                LogUtils.d(TAG, "通话状态变更：" + stateDesc + "（状态码：" + state + "）");

                switch (state) {
                    case Call.STATE_DISCONNECTED:
                        // 双重校验，避免重复关闭页面
                        if (ActivityStack.getInstance().getActivity(PhoneCallActivity.class) != null) {
                            ActivityStack.getInstance().finishActivity(PhoneCallActivity.class);
                            LogUtils.d(TAG, "通话界面已关闭");
                        }
                        break;
                    case Call.STATE_ACTIVE:
                        LogUtils.d(TAG, MI_DEVICE_TAG + " 通话进入活跃状态，适配音频通道");
                        break;
                    default:
                        break;
                }
            }
        };
        LogUtils.d(TAG, "通话状态回调初始化完成");
    }

    // 核心业务处理方法区
    private CallType judgeCallType(Call call) {
        if (call == null) {
            LogUtils.e(TAG, "judgeCallType: 通话对象为空");
            return null;
        }
        int callState = call.getState();
        if (callState == Call.STATE_RINGING) {
            LogUtils.d(TAG, "识别为来电");
            return CallType.CALL_IN;
        } else if (callState == Call.STATE_CONNECTING) {
            LogUtils.d(TAG, "识别为去电");
            return CallType.CALL_OUT;
        }
        return null;
    }

    private boolean handleValidCall(Call call, CallType callType) {
        if (call == null || callType == null) {
            LogUtils.e(TAG, "handleValidCall: 通话对象或类型为空");
            return false;
        }

        Call.Details callDetails = call.getDetails();
        if (callDetails == null || callDetails.getHandle() == null) {
            LogUtils.e(TAG, "通话详情缺失，处理终止");
            return false;
        }

        String phoneNumber = callDetails.getHandle().getSchemeSpecificPart();
        LogUtils.d(TAG, "处理通话：号码=" + phoneNumber + "，类型=" + callType.name());

        if (mAudioManager == null) {
            LogUtils.e(TAG, "音频管理器未初始化");
            PhoneCallActivity.actionStart(this, phoneNumber, callType);
            return true;
        }

        if (checkRulesAndHandleRingerVolumeControl(phoneNumber, call)) {
            PhoneCallActivity.actionStart(this, phoneNumber, callType);
            LogUtils.d(TAG, MI_DEVICE_TAG + " 通话界面启动成功");
            return true;
        }
        return false;
    }

    private boolean checkRulesAndHandleRingerVolumeControl(String phoneNumber, Call call) {
        if (mAudioManager == null || phoneNumber == null || call == null) {
            LogUtils.e(TAG, "checkRulesAndHandleRingerVolumeControl: 入参为空");
            return false;
        }

        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        LogUtils.d(TAG, "当前铃声音量：" + currentVolume);

        RingTongBean ringTongBean = RingTongBean.loadBean(this, RingTongBean.class);
        if (ringTongBean == null) {
            ringTongBean = new RingTongBean();
            RingTongBean.saveBean(this, ringTongBean);
            LogUtils.d(TAG, "初始化默认铃音配置");
        }
        final int configVolume = ringTongBean.getStreamVolume();

        try {
            // 小米机型适配：调整音量时添加权限校验
            if (currentVolume != configVolume) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_RING, configVolume, 0);
                LogUtils.d(TAG, MI_DEVICE_TAG + " 铃声音量调整为配置值：" + configVolume);
            }
        } catch (SecurityException e) {
            LogUtils.e(TAG, "音量调整失败，权限不足", e);
            return false;
        }

        // 校验拦截规则
        if (!Rules.getInstance(this).isAllowed(phoneNumber)) {
            LogUtils.d(TAG, "号码" + phoneNumber + "命中拦截规则");
            try {
                // 拦截时静音并挂断
                mAudioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
                call.disconnect();
                LogUtils.d(TAG, MI_DEVICE_TAG + " 拦截通话已挂断并静音");

                // 延迟恢复音量，适配小米机型音频通道延迟
                new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								Thread.sleep(500);
								if (mAudioManager != null) {
									mAudioManager.setStreamVolume(AudioManager.STREAM_RING, configVolume, 0);
									LogUtils.d(TAG, MI_DEVICE_TAG + " 延迟恢复铃音配置");
								}
							} catch (InterruptedException e) {
								LogUtils.e(TAG, "恢复音量线程中断", e);
							}
						}
					}).start();
            } catch (SecurityException e) {
                LogUtils.e(TAG, "拦截静音失败", e);
                return false;
            }
            return false;
        }
        return true;
    }

    // 辅助工具方法区：解析通话状态描述
    private String getCallStateDesc(int state) {
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                return "响铃中";
            case TelephonyManager.CALL_STATE_OFFHOOK:
                return "通话中";
            case TelephonyManager.CALL_STATE_IDLE:
                return "空闲";
            case Call.STATE_ACTIVE:
                return "通话活跃";
            case Call.STATE_CONNECTING:
                return "连接中";
            case Call.STATE_DISCONNECTED:
                return "已断开";
            default:
                return "未知状态";
        }
    }

    // 静态内部类：统一管理通话对象，避免跨组件对象混乱
    public static class PhoneCallManager {
        public static Call call;
    }
}

