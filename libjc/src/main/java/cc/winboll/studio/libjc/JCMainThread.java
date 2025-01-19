package cc.winboll.studio.libjc;

import cc.winboll.studio.libjc.JCMainThread;
import cc.winboll.studio.libjc.util.ConsoleUtils;
import cc.winboll.studio.libjc.util.LogUtils;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JCMainThread extends Thread {

    public static String TAG = "JCMainThread";

    static volatile JCMainThread _JCMainThread;
    static volatile boolean _Exit;
    static OnMessageListener _OnLogListener;
    JCCommandThread mJCCommand;

    JCMainThread(String logName) {
		ConsoleUtils.Debug.printFuncInfo(TAG);
		
        _Exit = false;
        _OnLogListener = null;
		
        LogUtils.init((logName == null || logName.equals("")) ?JCMainThread.class.getName(): logName);
        System.out.println("JCMainThread()");
        LogUtils.d(TAG, "JCMainThread()");

        Main.setRunningMode(Main.JAR_RUNNING_MODE.CONSOLE);
    }

    public static synchronized JCMainThread getInstance(String logName) {
        if (_JCMainThread == null) {
            _JCMainThread = new JCMainThread(logName);
        }
        return _JCMainThread;
    }
    
    

    public void exeInit(String cmd) {
        //System.out.println("cmd " + cmd);
        mJCCommand.exec(cmd);
    }

    public void exeBashCommand(String cmd) {
        //System.out.println("cmd " + cmd);
        mJCCommand.exec(cmd);
    }

    @Override
    public void run() {
        super.run();
		System.out.println("JCMainThread run()");
        LogUtils.d(TAG, "run()");
        if (_OnLogListener != null) {
            (new JCErrorThread(_OnLogListener)).start();
            (new JCOutputThread(_OnLogListener)).start();
        }
        mJCCommand = new JCCommandThread();
        mJCCommand.start();

        while (!_Exit) {

            //Log.d(TAG, "!_Exit");
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = sdf.format(date);
            String msg = String.format("run() %s", formattedDate);
            //System.out.println(msg);
            //System.err.println("err : " + msg);
            try {
                Thread.sleep(5 * 1000);

                // 这里模拟一下运行出错
//                String[] as = new String[2];
//                String b = as[3];
//                System.out.println(b);
//
            } catch (Exception e) {
                LogUtils.e(TAG, e, Thread.currentThread().getStackTrace());
            }
        }
		System.out.println("JCMainThread run()");
        // 内存清理
        _OnLogListener = null;
    }

    public static OnMessageListener getOnLogListener() {
        return _OnLogListener;
    }

    public static void setOnLogListener(OnMessageListener listener) {
        _OnLogListener = listener;
    }

    public interface OnMessageListener {
        //void log(String message);
        void outPrint(String message);
        void errPrint(String message);
    }
}
