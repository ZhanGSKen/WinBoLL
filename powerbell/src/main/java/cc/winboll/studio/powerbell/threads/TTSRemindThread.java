package cc.winboll.studio.powerbell.threads;

import android.content.Context;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.powerbell.models.ThoughtfulServiceBean;
import cc.winboll.studio.powerbell.services.TTSPlayService;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/02/28 20:41
 * @Describe TTS 语音通知线程（单例 + 继承 Thread）
 */
public class TTSRemindThread extends Thread {

    public static final String TAG = "TTSRemindThread";

    // 单例实例
    private static TTSRemindThread sInstance;

    // 运行标志
    private volatile boolean mIsRunning = false;
	private volatile int mBattery;
	private volatile boolean mIsCharging;

    private Context mContext;

    /**
     * 私有构造，单例禁用外部 new
     */
    private TTSRemindThread(Context context) {
        this.mContext = context.getApplicationContext();
    }

    /**
     * 获取单例（DCL 双重校验）
     */
    public static TTSRemindThread getInstance(Context context) {
        if (sInstance == null) {
            synchronized (TTSRemindThread.class) {
                if (sInstance == null) {
                    sInstance = new TTSRemindThread(context);
                }
            }
        }
        return sInstance;
    }

    /**
     * 对外静态启动入口
     */
    public static void start(Context context, int battery, boolean isCharging) {
        TTSRemindThread instance = getInstance(context);
		instance.mBattery = battery;
		instance.mIsCharging = isCharging;
        if (!instance.mIsRunning) {
            LogUtils.d(TAG, "start() TTS 提醒线程启动");
            instance.mIsRunning = true;
            instance.start(); // 启动线程
        }
    }

    /**
     * 对外静态停止入口
     */
    public static void stopTTS() {
        if (sInstance != null) {
            LogUtils.d(TAG, "stopTTS() TTS 提醒线程停止");
            sInstance.mIsRunning = false;
			sInstance = null;
        }
    }

    /**
     * 线程主逻辑
     */
    @Override
    public void run() {
        super.run();
        LogUtils.d(TAG, "run() TTS 线程已开始循环");

        while (mIsRunning) {
            try {
                // ======================
                // 在这里写你的循环 TTS 逻辑
                // ======================

				// TTS 语音通知模块
				// 读取 TTS 语音通知配置
				ThoughtfulServiceBean ttsBean = ThoughtfulServiceBean.loadBean(mContext, ThoughtfulServiceBean.class);
				if (ttsBean == null) {
					ttsBean = new ThoughtfulServiceBean();
				}
				if (ttsBean.isEnableTtsWhenNotifyBattery()) {
					//ToastUtils.show("Test");
					// 启动
					//ToastUtils.show(String.format("mIsCharging %s, mBattery %d", mIsCharging, mBattery));
					String text = mIsCharging ?"充电": "用电";
					text += String.format("已达预定值，现在电量为百分之%d", mBattery);
					TTSPlayService.startPlayTTS(mContext, text);
				}

                // 防止死循环疯狂跑，加一点休眠
                sleep(6000);
            } catch (InterruptedException e) {
                LogUtils.e(TAG, "TTS 线程被中断", e);
                break;
            }
        }

        mIsRunning = false;
        LogUtils.d(TAG, "run() TTS 线程已退出");
    }

    /**
     * 是否正在运行
     */
    public boolean isRunning() {
        return mIsRunning;
    }
}

