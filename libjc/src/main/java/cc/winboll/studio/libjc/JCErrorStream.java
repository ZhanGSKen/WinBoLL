package cc.winboll.studio.libjc;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/07 13:46:40
 * @Describe JC 错误输出流
 */
import cc.winboll.studio.libjc.util.LogUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class JCErrorStream extends OutputStream {

    public static final String TAG = "JCErrorStream";
    private StringBuilder buffer = new StringBuilder();

    static volatile JCErrorStream _JCErrorStream;
    static volatile boolean _IsInitOK;
    JCMainThread.OnMessageListener mOnMessageListener;

    @Override
    public void write(int b) {
        buffer.append((char) b);
        err(String.format("%i", b));
    }

    void err(String message) {
        mOnMessageListener.errPrint(message);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        buffer.append(new String(b, off, len));
        err(new String(b, off, len));
    }

    public String getOutput() {
        return buffer.toString();
    }
    JCErrorStream(JCMainThread.OnMessageListener listener) {
        mOnMessageListener = listener;
    }

    public static void start(JCMainThread.OnMessageListener listener) {
        LogUtils.d(TAG, String.format("%s init()\n", TAG));

        if (_JCErrorStream == null) {
            _JCErrorStream = new JCErrorStream(listener);
            _IsInitOK = false;

            // 保存原始的System.out
            PrintStream originalOut = System.err;
            try {
                // 创建自定义输出流实例并将System.out重定向到它
                //JCOutputStream jcOutputStream = new JCOutputStream();
                System.setErr(new PrintStream(_JCErrorStream));
                System.err.println("Test err stream.\n");
                while (!JCMainThread._Exit) {
                    
                }
                // 这里原本输出到控制台的内容会被重定向到自定义输出流
                System.out.println("这是被接管后输出的内容");
                System.out.println("另一行内容");

                // 获取自定义输出流收集到的内容
                String output = _JCErrorStream.getOutput();
                System.out.println("收集到的内容: " + output);

                // 恢复System.out为原始的输出流
                System.setOut(originalOut);


            } finally {
                try {
                    _JCErrorStream.close();
                } catch (IOException e) {
                    LogUtils.e(TAG, e, Thread.currentThread().getStackTrace());
                }
            }
        }
    }
}

