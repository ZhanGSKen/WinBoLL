package cc.winboll.studio.libappbase;

import android.os.FileObserver;
import java.lang.ref.WeakReference;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2024/08/12 14:43:50
 * @Describe 日志视图线程类
 * 独立线程监听日志文件目录变化（如写入、删除），触发日志视图更新，避免阻塞主线程
 */
public class LogViewThread extends Thread {

    /** 日志标签（用于调试输出） */
    public static final String TAG = "LogViewThread";

    /** 线程退出标志（volatile 保证多线程可见性，控制循环退出） */
    private volatile boolean isExit = false;
    /** 日志文件目录监听实例（监听文件写入、删除事件） */
    private LogListener mLogListener;
    /** 日志视图弱引用（避免持有 LogView 强引用导致内存泄漏） */
    private final WeakReference<LogView> mLogViewWeakRef;

    /**
     * 构造函数
     * @param logView 日志显示视图实例（需通过弱引用持有，避免内存泄漏）
     */
    public LogViewThread(LogView logView) {
        // 使用弱引用包装 LogView，当视图销毁时可被 GC 回收
        mLogViewWeakRef = new WeakReference<>(logView);
    }

    /**
     * 设置线程退出标志（触发线程停止监听并退出）
     * @param exit true：退出线程；false：继续运行（默认）
     */
    public void setExit(boolean exit) {
        this.isExit = exit;
    }

    /**
     * 获取当前线程退出状态
     * @return true：已标记退出；false：运行中
     */
    public boolean isExit() {
        return isExit;
    }

    /**
     * 线程核心逻辑：初始化文件监听并启动循环，直到收到退出标志
     */
    @Override
    public void run() {
		// 调试状态进行日志输出任务
		if (GlobalApplication.isDebugging()) {
			// 获取日志缓存目录路径（从 LogUtils 统一获取，确保路径一致性）
			String logDirPath = LogUtils.getLogCacheDir().getPath();
			LogUtils.d(TAG, "启动日志文件监听，监听目录：" + logDirPath);

			// 初始化日志文件监听器（监听目标目录的文件事件）
			mLogListener = new LogListener(logDirPath);
			// 开始监听文件事件（非阻塞，内部通过 Native 层实现）
			mLogListener.startWatching();

			// 循环等待退出标志（每 1 秒检查一次，降低 CPU 占用）
			while (!isExit()) {
				try {
					Thread.sleep(1000); // 休眠 1 秒，避免忙等
				} catch (InterruptedException e) {
					// 线程被中断时，恢复中断标志并退出循环（避免无限阻塞）
					Thread.currentThread().interrupt();
					LogUtils.d(TAG, "日志监听线程被中断，准备退出。" + e);
					break;
				}
			}

			// 收到退出标志，停止监听并释放资源
			mLogListener.stopWatching();
			LogUtils.d(TAG, "日志文件监听已停止，线程退出");
		}
    }

    /**
     * 日志文件监听内部类（继承 FileObserver，监听目录下文件变化）
     * 仅关注文件写入完成（CLOSE_WRITE）和文件删除（DELETE）事件
     */
    private class LogListener extends FileObserver {

        /**
         * 构造函数
         * @param path 监听的目录路径（此处为日志缓存目录）
         */
        public LogListener(String path) {
            // 父类构造：监听指定目录的所有事件（通过位掩码 ALL_EVENTS 指定）
            super(path);
        }

        /**
         * 文件事件回调（运行在系统私有线程，非主线程）
         * @param event 事件类型（通过位掩码表示，需与 ALL_EVENTS 按位与解析）
         * @param path 发生事件的文件名（相对监听目录的路径）
         */
        @Override
        public void onEvent(int event, String path) {
            // 解析事件类型（排除无关事件，只处理目标事件）
            int eventType = event & FileObserver.ALL_EVENTS;

            switch (eventType) {
					// 事件：文件写入完成（如日志写入结束并关闭文件）
                case FileObserver.CLOSE_WRITE:
                    // 触发日志视图更新（需先判断 LogView 是否未被回收）
                    updateLogView();
                    break;

					// 事件：文件被删除（如日志清理操作）
                case FileObserver.DELETE:
                    LogUtils.d(TAG, "日志文件被删除，文件名：" + (path != null ? path : "未知"));
                    // 触发日志视图更新（刷新视图显示空状态）
                    updateLogView();
                    break;

                default:
                    // 忽略其他无关事件（如文件创建、访问等）
                    break;
            }
        }

        /**
         * 触发日志视图更新（通过弱引用获取 LogView，避免内存泄漏）
         */
        private void updateLogView() {
            // 从弱引用中获取 LogView 实例（若视图已销毁，get() 返回 null）
            LogView logView = mLogViewWeakRef.get();
            if (logView != null) {
                // 调用 LogView 的更新方法（需确保 updateLogView 内部处理主线程切换）
                logView.updateLogView();
            } else {
                LogUtils.w(TAG, "LogView 已被回收，无法更新日志视图");
            }
        }
    }
}

