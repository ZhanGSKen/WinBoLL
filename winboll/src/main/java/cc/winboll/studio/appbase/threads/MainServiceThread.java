package cc.winboll.studio.appbase.threads;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/14 03:46:44
 */
import android.content.Context;
import cc.winboll.studio.appbase.handlers.MainServiceHandler;
import cc.winboll.studio.libappbase.LogUtils;
import java.lang.ref.WeakReference;

public class MainServiceThread extends Thread {

    public static final String TAG = "MainServiceThread";

    Context mContext;

    // 控制线程是否退出的标志
    volatile boolean isExist = false;

    // 服务Handler, 用于线程发送消息使用
    WeakReference<MainServiceHandler> mwrMainServiceHandler;

    public void setIsExist(boolean isExist) {
        this.isExist = isExist;
    }

    public boolean isExist() {
        return isExist;
    }

    public MainServiceThread(Context context, MainServiceHandler handler) {
        mContext = context;
        mwrMainServiceHandler = new WeakReference<MainServiceHandler>(handler);
    }

    @Override
    public void run() {
        LogUtils.d(TAG, "run()");

        while (!isExist()) {
            //ToastUtils.show("run()");
            //LogUtils.d(TAG, "run()");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
            }
        }
        LogUtils.d(TAG, "run() exit.");
    }

}
