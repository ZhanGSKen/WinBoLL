package cc.winboll.studio.contacts.phonecallui;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.telecom.Call;
import android.telecom.VideoProfile;
import androidx.annotation.RequiresApi;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/12/15 20:11
 * @Describe 通话核心管理类
 * 功能：接听/挂断通话、免提控制、资源释放，适配API29-30及小米机型
 */
@RequiresApi(api = Build.VERSION_CODES.Q) // 匹配目标适配区间API29
public class PhoneCallManager {
    // 常量定义区
    public static final String TAG = "PhoneCallManager";
    private static final String MI_ADAPT_TAG = "MiDeviceAdapt"; // 小米适配标识
    private static final int VIDEO_PROFILE_AUDIO_ONLY = VideoProfile.STATE_AUDIO_ONLY;
    private static final int AUDIO_MODE_BACKUP = -1; // 音频模式备份默认值

    // 成员属性区（按依赖优先级排序，移除静态call避免跨组件冲突）
    private Context mContext;
    private AudioManager mAudioManager;
    private int mAudioModeBackup; // 备份原始音频模式，避免影响其他应用
    private boolean mIsSpeakerOpened; // 免提状态标记，防止重复切换

    // 构造方法（单例化改造，避免多实例冲突）
    private static volatile PhoneCallManager sInstance;
    public static PhoneCallManager getInstance(Context context) {
        if (context == null) {
            LogUtils.e(TAG, "getInstance: 上下文为空，初始化失败");
            return null;
        }
        if (sInstance == null) {
            synchronized (PhoneCallManager.class) {
                if (sInstance == null) {
                    sInstance = new PhoneCallManager(context.getApplicationContext()); // 用应用上下文，避免内存泄漏
                }
            }
        }
        return sInstance;
    }

    // 私有构造，禁止外部实例化
    private PhoneCallManager(Context context) {
        LogUtils.d(TAG, MI_ADAPT_TAG + " 初始化通话管理类");
        this.mContext = context;
        this.mAudioModeBackup = AUDIO_MODE_BACKUP;
        this.mIsSpeakerOpened = false;
        initAudioManager();
        LogUtils.d(TAG, MI_ADAPT_TAG + " 通话管理类初始化完成");
    }

    // 初始化辅助方法
    private void initAudioManager() {
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManager != null) {
            // 备份原始音频模式（小米机型切换后需恢复，避免外放异常）
            mAudioModeBackup = mAudioManager.getMode();
            LogUtils.d(TAG, "音频管理器初始化成功，原始模式备份：" + mAudioModeBackup);
        } else {
            LogUtils.e(TAG, "音频管理器初始化失败，将影响通话音频控制");
        }
    }

    // 核心业务方法（按使用场景排序，强化小米适配+容错）
    /**
     * 接听电话，默认音频通话模式
     */
    public void answer() {
        LogUtils.d(TAG, "执行接听通话操作");
        // 从PhoneCallService的静态管理器获取通话对象，统一数据源
        Call currentCall = PhoneCallService.PhoneCallManager.call;
        if (currentCall == null) {
            LogUtils.e(TAG, "接听失败：通话对象为空");
            return;
        }

        // 校验通话状态，避免重复接听（小米机型状态变更延迟）
        if (currentCall.getState() != Call.STATE_RINGING) {
            LogUtils.w(TAG, MI_ADAPT_TAG + " 非响铃状态，无需接听，当前状态：" + currentCall.getState());
            return;
        }

        try {
            currentCall.answer(VIDEO_PROFILE_AUDIO_ONLY);
            openSpeaker(); // 接听后自动开免提
            LogUtils.d(TAG, "通话接听成功，自动开启免提");
        } catch (SecurityException e) {
            LogUtils.e(TAG, MI_ADAPT_TAG + " 接听权限不足（需android.permission.ANSWER_PHONE_CALLS）", e);
        } catch (IllegalStateException e) {
            LogUtils.e(TAG, MI_ADAPT_TAG + " 通话状态异常，无法接听", e);
        } catch (Exception e) {
            LogUtils.e(TAG, "接听通话异常", e);
        }
    }

    /**
     * 断开通话（支持来电拒接、通话中挂断）
     */
    public void disconnect() {
        LogUtils.d(TAG, "执行断开通话操作");
        Call currentCall = PhoneCallService.PhoneCallManager.call;
        if (currentCall == null) {
            LogUtils.e(TAG, "挂断失败：通话对象为空");
            return;
        }

        // 校验通话状态，避免重复挂断
        if (currentCall.getState() == Call.STATE_DISCONNECTED) {
            LogUtils.w(TAG, MI_ADAPT_TAG + " 通话已断开，无需重复操作");
            return;
        }

        try {
            currentCall.disconnect();
            closeSpeaker(); // 挂断后关闭免提+恢复音频模式
            LogUtils.d(TAG, "通话断开成功");
        } catch (SecurityException e) {
            LogUtils.e(TAG, MI_ADAPT_TAG + " 挂断权限不足（需android.permission.CALL_PHONE）", e);
        } catch (IllegalStateException e) {
            LogUtils.e(TAG, MI_ADAPT_TAG + " 通话状态异常，无法挂断", e);
        } catch (Exception e) {
            LogUtils.e(TAG, "断开通话异常", e);
        }
    }

    /**
     * 打开免提，适配小米机型音频通道切换（解决MIUI音频混乱）
     */
    public void openSpeaker() {
        LogUtils.d(TAG, "执行打开免提操作");
        if (mAudioManager == null) {
            LogUtils.e(TAG, "打开免提失败：音频管理器未初始化");
            return;
        }
        if (mIsSpeakerOpened) {
            LogUtils.w(TAG, "免提已开启，无需重复操作");
            return;
        }

        try {
            // 小米机型适配步骤：1. 设置通话模式 2. 关闭静音 3. 开启免提（固定顺序）
            mAudioManager.setMode(AudioManager.MODE_IN_CALL);
            mAudioManager.setStreamMute(AudioManager.STREAM_VOICE_CALL, false); // 确保通话音频不静音
            mAudioManager.setSpeakerphoneOn(true);

            mIsSpeakerOpened = true;
            LogUtils.d(TAG, MI_ADAPT_TAG + " 免提开启成功，当前模式：" + mAudioManager.getMode());
        } catch (SecurityException e) {
            LogUtils.e(TAG, MI_ADAPT_TAG + " 音频控制权限不足", e);
        } catch (Exception e) {
            LogUtils.e(TAG, "打开免提异常", e);
        }
    }

    /**
     * 新增：关闭免提（挂断/切换场景调用，修复小米音频残留）
     */
    public void closeSpeaker() {
        LogUtils.d(TAG, "执行关闭免提操作");
        if (mAudioManager == null || !mIsSpeakerOpened) {
            LogUtils.w(TAG, "免提未开启或音频管理器为空，无需操作");
            return;
        }

        try {
            mAudioManager.setSpeakerphoneOn(false);
            // 恢复原始音频模式（关键：小米机型不恢复会导致其他应用外放异常）
            if (mAudioModeBackup != AUDIO_MODE_BACKUP) {
                mAudioManager.setMode(mAudioModeBackup);
                LogUtils.d(TAG, MI_ADAPT_TAG + " 恢复原始音频模式：" + mAudioModeBackup);
            }
            mIsSpeakerOpened = false;
            LogUtils.d(TAG, "免提关闭成功");
        } catch (Exception e) {
            LogUtils.e(TAG, MI_ADAPT_TAG + " 关闭免提异常", e);
        }
    }

    /**
     * 销毁资源，避免内存泄漏+音频残留（适配小米内存管理）
     */
    public void destroy() {
        LogUtils.d(TAG, "开始销毁通话管理资源");
        closeSpeaker(); // 销毁前强制关闭免提+恢复音频模式
        // 释放资源（应用上下文无需主动置空，避免空指针）
        mAudioManager = null;
        sInstance = null; // 单例置空，下次重新初始化
        LogUtils.d(TAG, MI_ADAPT_TAG + " 通话管理资源销毁完成");
    }

    /**
     * 新增：获取当前免提状态（供UI层同步显示）
     */
    public boolean isSpeakerOpened() {
        return mIsSpeakerOpened;
    }
}

