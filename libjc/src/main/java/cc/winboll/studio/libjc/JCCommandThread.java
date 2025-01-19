package cc.winboll.studio.libjc;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/07 17:34:56
 * @Describe JC命令行执行类
 */
import cc.winboll.studio.libjc.util.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class JCCommandThread extends Thread {
    //static volatile JCCommandThread _JCCommandThread;
    public JCCommandThread() {}
//    public static synchronized JCCommandThread getInstance() {
//        if(_JCCommandThread == null) {
//            _JCCommandThreadRunning = false;
//            _JCCommandThread = new JCCommandThread();
//        }
//        return _JCCommandThread;
//    }
//    
    static volatile String _CMD;
    static volatile boolean _Exit;
    static volatile boolean _JCCommandThreadRunning;
    static Process _Process;

    public static final String TAG = "JCCommand";

    public synchronized void exec(String cmd) {
        if (cmd.equals("/bye")) {
            _Exit = true;
            return;
        } else if (_CMD.equals("") && !cmd.equals("")) {
            _CMD = cmd + "\n";
            OutputStream outputStream = _Process.getOutputStream();
            try {
                System.out.println("$ " + _CMD);
                //Log.d(TAG, "_CMD : " + _CMD);
                outputStream.write(_CMD.getBytes());
                outputStream.flush();
                _CMD = "";
                //Log.d(TAG, "OutputStream done.");
            } catch (IOException e) {
                LogUtils.e(TAG, e, Thread.currentThread().getStackTrace());
            }
        }
    }

    @Override
    public void run() {
        super.run();
        if(!_JCCommandThreadRunning) {
            _JCCommandThreadRunning = true;
            init();
        }
    }
    
    public void init() {
        _Exit = false;
        _CMD = "";
        try {
            LogUtils.d(TAG, "start()");
            _Process = Runtime.getRuntime().exec("/system/xbin/busybox ash");

            // 创建线程读取标准输出流
            OutputThread outputThread = new OutputThread();
            //outputThread.initProcess(_Process);

            // 创建线程读取标准错误流（通常命令执行出错的信息会在这里输出）
            ErrorThread errorThread = new ErrorThread();
            //outputThread.initProcess(_Process);

            LogUtils.d(TAG, "Process init ok.");
            outputThread.start();
            errorThread.start();
            LogUtils.d(TAG, "Thread start.");

//            while (!_Exit) {
//                exec();
//            }

            // 等待线程执行完毕
//            try {
//                outputThread.join();
//                errorThread.join();
//                Log.d(TAG, "Thread join.");
//            } catch (InterruptedException e) {
//                Log.e(TAG, e, Thread.currentThread().getStackTrace());
//            }

            // 等待命令执行结束并获取退出值
            LogUtils.d(TAG, "_Process.waitFor()");
            int exitValue = _Process.waitFor();
            LogUtils.d(TAG, "Process exited with value: " + exitValue);

            _Exit = true;

        } catch (IOException | InterruptedException e) {
            LogUtils.e(TAG, e, Thread.currentThread().getStackTrace());
        }
    }

    public static class OutputThread extends Thread {
//        java.lang.Process mProcess;
//        public void initProcess(java.lang.Process process) {
//            mProcess = process;
//        }
        @Override
        public void run() {
            super.run();
            try {
                InputStream inputStream = _Process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    //Log.d(TAG, line);
                }
                reader.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e(TAG, e, Thread.currentThread().getStackTrace());
            }
        }

    }

    public static class ErrorThread extends Thread {
//        java.lang.Process mProcess;
//        public void initProcess(java.lang.Process process) {
//            mProcess = process;
//        }

        @Override
        public void run() {
            super.run();
            try {
                InputStream errorStream = _Process.getErrorStream();
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
                String line;
                while ((line = errorReader.readLine()) != null) {
                    System.out.println("Error: " + line);
                    //Log.d(TAG, "Error: " + line);
                }
                errorReader.close();
                errorStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e(TAG, e, Thread.currentThread().getStackTrace());
            }
        }

    }
}

