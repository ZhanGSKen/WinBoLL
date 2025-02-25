package cc.winboll.studio.positions.threads;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/14 03:46:44
 */
import android.content.Context;
import cc.winboll.studio.positions.handlers.MainServiceHandler;
import cc.winboll.studio.positions.services.MainService;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.SOS;
import cc.winboll.studio.libappbase.bean.APPSOSBean;
import com.hjq.toast.ToastUtils;
import java.lang.ref.WeakReference;

public class MainServiceThread extends Thread {

    public static final String TAG = "MainServiceThread";
    
    volatile static MainServiceThread _MainServiceThread;
    // 控制线程是否退出的标志
    volatile boolean isExit = false;
    volatile boolean isStarted = false;
    Context mContext;
    // 服务Handler, 用于线程发送消息使用
    WeakReference<MainServiceHandler> mwrMainServiceHandler;

    MainServiceThread(Context context, MainServiceHandler handler) {
        mContext = context;
        mwrMainServiceHandler = new WeakReference<MainServiceHandler>(handler);
    }

    public void setIsExit(boolean isExit) {
        this.isExit = isExit;
    }

    public boolean isExit() {
        return isExit;
    }

    public void setIsStarted(boolean isStarted) {
        this.isStarted = isStarted;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public static MainServiceThread getInstance(Context context, MainServiceHandler handler) {
        if (_MainServiceThread != null) {
            _MainServiceThread.setIsExit(true);
        }
        _MainServiceThread = new MainServiceThread(context, handler);
        return _MainServiceThread;
    }

    @Override
    public void run() {
        if (isStarted == false) {
            isStarted = true;
            LogUtils.d(TAG, "run()");
           
            while (!isExit()) {
                //ToastUtils.show("run");
                //LogUtils.d(TAG, "run()");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                }
            }
            _MainServiceThread = null;
            LogUtils.d(TAG, "run() exit");
        }
    }

}
