package cc.winboll.studio.contacts.threads;

import android.content.Context;
import cc.winboll.studio.contacts.handlers.MainServiceHandler;
import cc.winboll.studio.libappbase.LogUtils;
import java.lang.ref.WeakReference;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/02/14 03:46:44
 * @Describe 主服务后台工作线程，负责定时轮询与消息调度
 */
public class MainServiceThread extends Thread {
    // ====================== 常量定义区 ======================
    public static final String TAG = "MainServiceThread";
    // 线程休眠周期（1秒）
    private static final long THREAD_SLEEP_INTERVAL = 1000L;

    // ====================== 静态成员变量区 ======================
    private static volatile MainServiceThread sInstance;

    // ====================== 成员变量区 ======================
    // 线程运行控制标记
    private volatile boolean mIsExit;
    private volatile boolean mIsStarted;
    // 弱引用持有上下文和Handler，避免内存泄漏
    private WeakReference<Context> mContextWeakRef;
    private WeakReference<MainServiceHandler> mHandlerWeakRef;

    // ====================== 私有构造函数 ======================
    private MainServiceThread(Context context, MainServiceHandler handler) {
        this.mContextWeakRef = new WeakReference<>(context);
        this.mHandlerWeakRef = new WeakReference<>(handler);
        this.mIsExit = false;
        this.mIsStarted = false;
        LogUtils.d(TAG, "MainServiceThread: 线程实例初始化完成");
    }

    // ====================== 单例获取方法 ======================
    public static MainServiceThread getInstance(Context context, MainServiceHandler handler) {
        // 若已有实例，先标记退出并销毁旧实例
        if (sInstance != null) {
            LogUtils.d(TAG, "getInstance: 存在旧线程实例，标记退出");
            sInstance.setIsExit(true);
            sInstance = null;
        }
        // 创建新线程实例
        sInstance = new MainServiceThread(context, handler);
        LogUtils.d(TAG, "getInstance: 新线程实例已创建");
        return sInstance;
    }

    // ====================== 运行状态控制方法 ======================
    public void setIsExit(boolean isExit) {
        this.mIsExit = isExit;
        LogUtils.d(TAG, "setIsExit: 线程退出标记已更新 | " + isExit);
    }

    public boolean isExit() {
        return mIsExit;
    }

    public void setIsStarted(boolean isStarted) {
        this.mIsStarted = isStarted;
    }

    public boolean isStarted() {
        return mIsStarted;
    }

    // ====================== 线程核心执行方法 ======================
    @Override
    public void run() {
        // 防止重复启动
        if (mIsStarted) {
            LogUtils.w(TAG, "run: 线程已启动，避免重复执行");
            return;
        }

        // 标记线程启动状态
        mIsStarted = true;
        LogUtils.i(TAG, "run: 线程开始运行");

        // 线程主循环
        while (!mIsExit) {
            try {
                // 此处可添加业务逻辑（如定时任务、消息分发）
                Thread.sleep(THREAD_SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                LogUtils.e(TAG, "run: 线程休眠被中断", e);
                // 恢复线程中断状态
                Thread.currentThread().interrupt();
            }
        }

        // 线程退出清理
        mIsStarted = false;
        mContextWeakRef.clear();
        mHandlerWeakRef.clear();
        sInstance = null;
        LogUtils.i(TAG, "run: 线程正常退出");
    }
}

