package cc.winboll.studio.libjc;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/01/07 13:51:14
 * @Describe 错误输出流进程
 */
public class JCErrorThread extends Thread {

    public static final String TAG = "JCErrorThread";

    JCMainThread.OnMessageListener mOnMessageListener;

    public JCErrorThread(JCMainThread.OnMessageListener listener) {
        mOnMessageListener = listener;
    }
    @Override
    public void run() {
        super.run();
        JCErrorStream.start(mOnMessageListener);
    }
}
