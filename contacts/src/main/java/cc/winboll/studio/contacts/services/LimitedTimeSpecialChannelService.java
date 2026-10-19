package cc.winboll.studio.contacts.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/04/18 15:00:00 (GMT+8)
 * @LastEditTime 2026/04/22 17:20:00 (GMT+8)
 * @Describe 限时特殊通道服务
 * 提供安全的服务启动、令牌校验及循环任务管理
 * 新增功能：
 * 1. 计时过程中实时输出剩余秒数
 * 2. 服务销毁前，发送本地广播通知应用内其他组件，并携带剩余秒数信息
 * 3. 每秒发送一次倒计时状态的本地广播
 */
public class LimitedTimeSpecialChannelService extends Service {

    // ========================= 常量定义 =========================
    public static final String TAG = "LimitedTimeSpecialChannelService";
    public static final String EXTRA_DELAY_MILLIS = "EXTRA_DELAY_MILLIS";
    private static final String EXTRA_SECURITY_TOKEN = "EXTRA_SECURITY_TOKEN";

    // 本地广播 Action 常量
    public static final String ACTION_SERVICE_DESTROYED = "cc.winboll.studio.contacts.services.ACTION_SERVICE_DESTROYED";

    // 倒计时心跳广播 Action
    public static final String ACTION_COUNTDOWN_TICK = "cc.winboll.studio.contacts.services.ACTION_COUNTDOWN_TICK";
    // 广播携带的额外参数：剩余秒数
    public static final String EXTRA_REMAINING_SECONDS = "EXTRA_REMAINING_SECONDS";
    // 广播携带的额外参数：总定时秒数
    public static final String EXTRA_TOTAL_SECONDS = "EXTRA_TOTAL_SECONDS";

    // ========================= 静态变量 =========================
    /**
     * 公共静态私有字段：安全校验令牌
     * 确保全局可访问且值不可变更
     */
    public static final String mValidToken = "VALID_TOKEN_" + System.currentTimeMillis();

    private static volatile LimitedTimeSpecialChannelService sInstance = null;
    private static volatile boolean sIsServiceRunning = false;

    // ========================= 成员变量 =========================
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private long mTotalMillis = 0;      // 总定时时长
    private long mRemainingMillis = 0;  // 剩余时长
    private LocalBroadcastManager mLocalBroadcastManager; // 本地广播管理器实例

    // ========================= 公共静态方法 =========================
    /**
     * 公共静态方法：启动服务
     * @param context 上下文
     * @param delayMillis 定时时长（毫秒）
     */
    public static void startService(Context context, long delayMillis) {
        LogUtils.i(TAG, "调用静态入口方法 startService");
        LogUtils.i(TAG, "入参 - context: " + context + ", delayMillis: " + delayMillis);

        if (context == null) {
            LogUtils.w(TAG, "启动失败，上下文为null");
            return;
        }
        if (isServiceRunning()) {
            LogUtils.i(TAG, "服务已运行，忽略重复启动请求");
            return;
        }

        // 构建Intent传递参数
        Intent intent = new Intent(context, LimitedTimeSpecialChannelService.class);
        intent.putExtra(EXTRA_SECURITY_TOKEN, mValidToken);
        intent.putExtra(EXTRA_DELAY_MILLIS, delayMillis);

        context.startService(intent);
        LogUtils.i(TAG, "服务启动命令已发出");
    }

    /**
     * 公共静态方法：停止服务
     * @param context 上下文
     */
    public static void stopService(Context context) {
        LogUtils.i(TAG, "调用静态入口方法 stopService");
        LogUtils.i(TAG, "入参 - context: " + context);

        if (context == null) {
            LogUtils.w(TAG, "停止失败，上下文为null");
            return;
        }
        Intent intent = new Intent(context, LimitedTimeSpecialChannelService.class);
        context.stopService(intent);
        LogUtils.i(TAG, "服务停止命令已发出");
    }

    /**
     * 公共静态方法：查询服务运行状态
     * @return true if running
     */
    public static boolean isServiceRunning() {
        return sIsServiceRunning;
    }

    /**
     * 【核心单元测试方法】
     * 执行完整的单元测试流程
     * @param context 上下文
     */
    public static void unitTest(Context context) {
        LogUtils.i(TAG, "=== 开始执行单元测试 ===");

        // 测试1: 初始状态应为未运行
        boolean initialState = isServiceRunning();
        LogUtils.i(TAG, "测试1 - 初始状态检查: " + (initialState ? "失败(不应运行)" : "成功(未运行)"));

        // 启动服务，设置5秒定时
        startService(context, 5000L);

        // 核心修复：同步等待服务启动完成
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            LogUtils.e(TAG, "单元测试等待被中断", e);
        }

        // 测试3: 验证服务已启动
        boolean runningState = isServiceRunning();
        LogUtils.i(TAG, "测试3 - 运行状态检查: " + (runningState ? "成功(运行中)" : "失败(未运行)"));

        // 测试4: 尝试重复启动
        LogUtils.i(TAG, "测试4 - 尝试重复启动(预期忽略)");
        startService(context, 5000L);

        // 测试5: 等待服务自动停止
        LogUtils.i(TAG, "测试5 - 等待服务自动停止(5秒)...");
        try {
            // 等待超过服务的5秒运行时间，确保能观测到销毁状态
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            LogUtils.e(TAG, "测试等待被中断", e);
        }

        // 验证最终状态
        boolean finalState = isServiceRunning();
        LogUtils.i(TAG, "测试5 - 最终状态检查: " + (!finalState ? "成功(已销毁)" : "失败(仍运行)"));
        LogUtils.i(TAG, "=== 单元测试执行完成 ===");
    }

    // ========================= 生命周期 =========================
    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.i(TAG, "服务 onCreate，创建实例");
        sInstance = this;
        sIsServiceRunning = true;

        // 初始化本地广播管理器
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        LogUtils.i(TAG, "本地广播管理器初始化完成");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.i(TAG, "服务 onStartCommand，处理启动请求");
        LogUtils.i(TAG, "入参 - intent: " + intent + ", flags: " + flags + ", startId: " + startId);

        if (!isValidToken(intent)) {
            LogUtils.w(TAG, "安全校验失败，拒绝启动服务");
            stopSelf();
            return START_NOT_STICKY;
        }

        long delayMillis = intent.getLongExtra(EXTRA_DELAY_MILLIS, 0);
        if (delayMillis <= 0) {
            LogUtils.w(TAG, "无效的定时时长: " + delayMillis + "，服务将退出");
            stopSelf();
            return START_NOT_STICKY;
        }

        // 初始化总时长和剩余时长
        mTotalMillis = delayMillis;
        mRemainingMillis = delayMillis;
        LogUtils.i(TAG, "初始化定时时长: " + (mTotalMillis / 1000) + " 秒");

        startLoopTask();
        // 设置自动停止定时器
        mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					LogUtils.i(TAG, "定时时长结束，准备停止服务");
					stopSelf();
				}
			}, delayMillis);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        LogUtils.i(TAG, "服务 onDestroy，准备销毁");

        // 发送本地广播，通知应用内其他组件服务即将销毁
        // 此处携带剩余秒数信息，方便接收端知晓具体的结束状态
        sendServiceDestroyedBroadcast();

        // 重置运行状态
        sIsServiceRunning = false;
        sInstance = null;

        // 清理所有回调
        mHandler.removeCallbacksAndMessages(null);

        // 执行父类销毁
        super.onDestroy();

        LogUtils.i(TAG, "服务销毁流程完成");
    }

    /**
     * 发送服务销毁的本地广播
     * 确保应用内所有注册了该广播接收器的组件都能收到通知
     * [新增] 携带当前剩余秒数信息
     */
    private void sendServiceDestroyedBroadcast() {
        LogUtils.i(TAG, "准备发送服务销毁本地广播");
        try {
            Intent intent = new Intent(ACTION_SERVICE_DESTROYED);
            // 新增：将当前剩余秒数放入广播附加数据中
            intent.putExtra(EXTRA_REMAINING_SECONDS, mRemainingMillis / 1000);

            mLocalBroadcastManager.sendBroadcast(intent);
            LogUtils.i(TAG, "服务销毁广播发送成功，Action: " + ACTION_SERVICE_DESTROYED);
            LogUtils.i(TAG, "广播附加数据 - 剩余秒数: " + (mRemainingMillis / 1000));
        } catch (Exception e) {
            LogUtils.e(TAG, "发送服务销毁广播失败", e);
        }
    }

    /**
     * 发送倒计时心跳的本地广播
     * 每秒执行一次，携带当前剩余秒数和总时长
     */
    private void sendCountdownTickBroadcast() {
        try {
            Intent intent = new Intent(ACTION_COUNTDOWN_TICK);
            intent.putExtra(EXTRA_REMAINING_SECONDS, mRemainingMillis / 1000);
            intent.putExtra(EXTRA_TOTAL_SECONDS, mTotalMillis / 1000);

            mLocalBroadcastManager.sendBroadcast(intent);
            // 日志已精简，只在关键节点打印
        } catch (Exception e) {
            LogUtils.e(TAG, "发送倒计时广播失败", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.i(TAG, "服务 onBind，不支持绑定操作");
        // 不支持绑定
        return null;
    }

    // ========================= 私有辅助方法 =========================
    /**
     * 校验令牌有效性
     */
    private boolean isValidToken(Intent intent) {
        LogUtils.i(TAG, "调用 isValidToken 方法进行安全校验");
        LogUtils.i(TAG, "入参 - intent: " + intent);

        if (intent == null) {
            return false;
        }
        String incomingToken = intent.getStringExtra(EXTRA_SECURITY_TOKEN);
        LogUtils.i(TAG, "接收到外部传入令牌: " + incomingToken);
        LogUtils.i(TAG, "本地校验令牌: " + mValidToken);
        return mValidToken.equals(incomingToken);
    }

    /**
     * 启动循环任务
     */
    private void startLoopTask() {
        LogUtils.i(TAG, "启动倒计时循环任务");
        mHandler.removeCallbacks(mLoopTaskRunnable);
        mHandler.postDelayed(mLoopTaskRunnable, 1000);
    }

    /**
     * 循环任务心跳
     * 核心逻辑：先递减时长，再发送广播，确保0秒能被正确发送
     */
    private final Runnable mLoopTaskRunnable = new Runnable() {
        @Override
        public void run() {
            if (sIsServiceRunning) {
                // 核心修复：先递减剩余时长
                if (mRemainingMillis >= 1000) {
                    mRemainingMillis -= 1000;
                } else {
                    // 兜底：防止负数，直接置为0
                    mRemainingMillis = 0;
                }

                // 计算并发送当前剩余秒数
                long remainingSeconds = mRemainingMillis / 1000;
                // 日志已精简，只在非0状态打印，避免日志刷屏
                if (remainingSeconds > 0) {
                    LogUtils.i(TAG, "循环心跳：剩余 " + remainingSeconds + " 秒");
                }

                // 发送倒计时广播
                sendCountdownTickBroadcast();

                // 递归调用，形成循环
                startLoopTask();
            }
        }
    };

}

