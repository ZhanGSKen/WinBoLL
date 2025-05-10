package cc.winboll.studio.libappbase;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/08/12 14:43:50
 * @Describe 日志视图线程类
 */
import android.os.FileObserver;
import cc.winboll.studio.libappbase.LogUtils;
import java.lang.ref.WeakReference;

public class LogViewThread extends Thread {

    public static final String TAG = "LogViewThread";

    // 线程退出标志
    volatile boolean isExist = false;
    // 应用日志文件监听实例
    LogListener mLogListener;
    // 日志视图弱引用
    WeakReference<LogView> mwrLogView;

    //
    // 构造函数
    // @logView : 日志显示输出视图类
    public LogViewThread(LogView logView) {
        mwrLogView = new WeakReference<LogView>(logView);

    }

    public void setIsExist(boolean isExist) {
        this.isExist = isExist;
    }

    public boolean isExist() {
        return isExist;
    }

    @Override
    public void run() {
        String szLogDir = LogUtils.getLogCacheDir().getPath();
        mLogListener = new LogListener(szLogDir);
        mLogListener.startWatching();
        while (isExist() == false) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }
        mLogListener.stopWatching();
    }


    //
    // 日志文件监听类
    //
    class LogListener extends FileObserver {
        public LogListener(String path) {
            super(path);
        }

        @Override
        public void onEvent(int event, String path) {
            int e = event & FileObserver.ALL_EVENTS;
            switch (e) {
                case FileObserver.CLOSE_WRITE:{
                        if (mwrLogView.get() != null) {
                            mwrLogView.get().updateLogView();
                        }
                        break;
                    }
                case FileObserver.DELETE:{
                        if (mwrLogView.get() != null) {
                            mwrLogView.get().updateLogView();
                        }
                        break;
                    }
            }
        }
    }
}
