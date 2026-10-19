package cc.winboll.studio.libappbase;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/11 20:51
 * @Describe 吐司工具类（单例模式）
 * 简化 Android 吐司的创建与展示，通过独立线程 + Handler 处理消息，最终切换到主线程显示吐司，避免内存泄漏
 */
public class ToastUtils {

    /** 工具类日志 TAG（用于调试输出） */
    public static final String TAG = "ToastUtils";
    /** 消息标识：显示短时长吐司 */
    private static final int MSG_SHOW_SHORT_TOAST = 1001;

    /** 单例实例（volatile 保证多线程下可见性，避免指令重排） */
    private static volatile ToastUtils sInstance;
    /** 全局上下文（volatile 保证多线程可见性，避免空指针） */
    private volatile Context mContext;
    /** 独立线程的 Handler（volatile 保证可见性） */
    private volatile Handler mWorkerHandler;
    /** 主线程 Handler（volatile 保证可见性） */
    private volatile Handler mMainHandler;
    /** 消息处理独立线程 */
    private Thread mWorkerThread;
    /** 资源释放标记（volatile 避免多线程误操作） */
    private volatile boolean isReleased = false;

    /**
     * 私有构造方法（禁止外部直接创建实例，确保单例）
     * 1. 初始化主线程 Handler；
     * 2. 创建并启动独立消息处理线程。
     */
    private ToastUtils() {
        initMainHandler(); // 优先初始化主线程 Handler
        startWorkerThread(); // 启动独立消息处理线程
    }

    /**
     * 初始化主线程 Handler
     */
    private void initMainHandler() {
        if (Looper.getMainLooper() == null) {
            LogUtils.e(TAG, "主线程 Looper 为空，无法初始化 mMainHandler");
            throw new IllegalStateException("主线程 Looper 未初始化，无法创建 ToastUtils");
        }
        mMainHandler = new Handler(Looper.getMainLooper());
        LogUtils.d(TAG, "主线程 Handler 初始化完成，线程ID：" + Looper.getMainLooper().getThread().getId());
    }

    /**
     * 启动独立消息处理线程
     */
    private void startWorkerThread() {
        mWorkerThread = new Thread(new Runnable() {
				@Override
				public void run() {
					LogUtils.d(TAG, "消息处理线程启动，线程ID：" + Thread.currentThread().getId());
					Looper.prepare();

					mWorkerHandler = new Handler(Looper.myLooper()) {
						@Override
						public void handleMessage(Message msg) {
							super.handleMessage(msg);
							// 若已释放，直接返回，不处理消息
							if (isReleased) {
								LogUtils.w(TAG, "资源已释放，忽略消息处理");
								return;
							}
							LogUtils.d(TAG, "WorkerHandler 接收消息，当前线程ID：" + Thread.currentThread().getId());
							if (msg.what == MSG_SHOW_SHORT_TOAST && msg.obj != null) {
								String message = (String) msg.obj;
								postToMainThreadShowToast(message);
							}
						}
					};

					Looper.loop();
					LogUtils.d(TAG, "消息处理线程退出");
				}
			}, "ToastWorkerThread");
        mWorkerThread.start();
    }

    /**
     * 获取单例实例（双重检查锁定）
     * @return ToastUtils 单例对象
     */
    private static ToastUtils getInstance() {
        if (sInstance == null) {
            synchronized (ToastUtils.class) {
                if (sInstance == null) {
                    sInstance = new ToastUtils();
                }
            }
        }
        return sInstance;
    }

    /**
     * 初始化工具类（必须在 Application 启动时调用）
     * @param context 全局上下文（推荐 Application 上下文）
     */
    public static void init(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("初始化上下文不能为 null！");
        }
        ToastUtils instance = getInstance();
        // 若已释放，重置释放标记
        if (instance.isReleased) {
            instance.isReleased = false;
            instance.startWorkerThread(); // 重新启动线程
        }
        instance.mContext = context.getApplicationContext();
        LogUtils.d(TAG, "ToastUtils 初始化完成，上下文已设置");
    }

    // ===================================== 新增：isInited() 方法 =====================================
    /**
     * 判断 ToastUtils 是否已初始化（供外部调用，如 CrashHandleNotifyUtils 中的复制提示）
     * @return true：已初始化（可正常显示吐司）；false：未初始化/已释放（无法正常显示）
     */
    public static boolean isInited() {
        ToastUtils instance = getInstance();
        // 双重校验：1. 未释放 2. 上下文已设置（确保初始化完成）
        return !instance.isReleased && instance.mContext != null;
    }
    // ===================================== 新增结束 =====================================

    /**
     * 外部接口：显示短时长吐司
     * @param message 吐司内容
     */
    public static void show(String message) {
        LogUtils.d(TAG, "外部调用 show()，当前线程ID：" + Thread.currentThread().getId());
        if (message == null || message.isEmpty()) {
            return;
        }

        ToastUtils instance = getInstance();
        // 校验资源是否已释放
        if (instance.isReleased) {
            LogUtils.w(TAG, "ToastUtils 已释放，无法显示吐司：" + message);
            return;
        }
        // 校验上下文是否初始化
        if (instance.mContext == null) {
            LogUtils.e(TAG, "ToastUtils 未初始化！请先调用 init(Context) 方法");
            // 不抛出异常，避免崩溃，改为日志提示
            return;
        }

        instance.sendToastMessage(message);
    }

    /**
     * 发送吐司消息到 WorkerHandler
     * @param message 吐司内容
     */
    private void sendToastMessage(String message) {
        LogUtils.d(TAG, "发送消息到 WorkerHandler");
        // 校验 WorkerHandler 是否就绪
        if (mWorkerHandler == null) {
            LogUtils.w(TAG, "WorkerHandler 未就绪，直接主线程显示");
            postToMainThreadShowToast(message);
            return;
        }
        // 发送消息
        Message msg = mWorkerHandler.obtainMessage(MSG_SHOW_SHORT_TOAST);
        msg.obj = message;
        mWorkerHandler.sendMessage(msg);
    }

    /**
     * 切换到主线程显示吐司
     * @param message 吐司内容
     */
    private void postToMainThreadShowToast(final String message) {
        LogUtils.d(TAG, "切换到主线程显示吐司，当前线程ID：" + Thread.currentThread().getId());
        // 校验资源是否已释放
        if (isReleased) {
            LogUtils.w(TAG, "资源已释放，取消显示吐司");
            return;
        }
        // 校验并初始化 mMainHandler
        if (mMainHandler == null) {
            LogUtils.e(TAG, "mMainHandler 为空，尝试重新初始化");
            initMainHandler();
            if (mMainHandler == null) {
                LogUtils.e(TAG, "mMainHandler 初始化失败，无法显示吐司：" + message);
                return;
            }
        }
        // 主线程显示
        mMainHandler.post(new Runnable() {
				@Override
				public void run() {
					if (isReleased) return; // 释放后取消执行
					showToastInternal(message);
				}
			});
    }

    /**
     * 实际显示吐司（主线程）
     * @param message 吐司内容
     */
    private void showToastInternal(String message) {
        LogUtils.d(TAG, "执行 showToastInternal()");
        // 最终校验上下文
        if (mContext == null) {
            LogUtils.w(TAG, "上下文为空，无法显示吐司：" + message);
            // 尝试重新获取 Application 上下文（降级策略）
            Context appContext = GlobalApplication.getInstance();
            if (appContext != null) {
                mContext = appContext;
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                LogUtils.d(TAG, "通过 GlobalApplication 获取上下文，成功显示吐司");
            }
            return;
        }
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 释放资源（仅在应用退出时调用）
     */
    public static void release() {
        LogUtils.d(TAG, "开始释放 ToastUtils 资源");
        ToastUtils instance = getInstance();
        // 标记为已释放，阻止后续消息处理
        instance.isReleased = true;

        // 停止 Worker 线程
        if (instance.mWorkerHandler != null && instance.mWorkerHandler.getLooper() != null) {
            instance.mWorkerHandler.getLooper().quit();
            instance.mWorkerHandler = null;
        }

        // 清理主线程 Handler
        if (instance.mMainHandler != null) {
            instance.mMainHandler.removeCallbacksAndMessages(null);
            instance.mMainHandler = null;
        }

        // 等待线程退出
        if (instance.mWorkerThread != null && instance.mWorkerThread.isAlive()) {
            try {
                instance.mWorkerThread.join(1000);
            } catch (InterruptedException e) {
				LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                Thread.currentThread().interrupt();
            }
            instance.mWorkerThread = null;
        }

        // 清空上下文（避免内存泄漏）
        instance.mContext = null;
        LogUtils.d(TAG, "ToastUtils 资源释放完成");
    }
}

