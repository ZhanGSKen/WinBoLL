package cc.winboll.studio.libjc;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/07 13:27:26
 * @Describe JC输出流进程
 */
public class JCOutputThread extends Thread {
    
    public static final String TAG = "JCOutputThread";
    
    JCMainThread.OnMessageListener mOnMessageListener;

    public JCOutputThread(JCMainThread.OnMessageListener listener) {
        mOnMessageListener = listener;
    }
    @Override
    public void run() {
        super.run();
        JCOutputStream.start(mOnMessageListener);
    }
}
