package cc.winboll.studio.libjc.util;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/01/06 18:47:05
 * @Describe 日志类
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtils {

    public static final String TAG = "Log";

    static SimpleDateFormat _SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static File _fLog;
    static volatile LogUtils _Log;
    static volatile boolean _LogInitOK = false;

    public synchronized static void init(String logName) {
		ConsoleUtils.Debug.printFuncInfo(TAG);
		
        if (_Log == null) {
            _LogInitOK = false;
            _Log = new LogUtils();
            File fLogDir = new File("/sdcard/WinBollStudio/Logs");
            if (!fLogDir.exists()) {
                System.out.println("Log dir not exist : /sdcard/WinBollStudio/Logs");
                return;
            }
            _fLog = new File(fLogDir, ((logName == null || logName.equals("")) ?TAG: logName) + ".txt");
            ConsoleUtils.Debug.printObjectInfo("_LogInitOK", _LogInitOK);
            //System.out.println("Log Init OK!");
            _LogInitOK = true;
            ConsoleUtils.Debug.printObjectInfo("_LogInitOK", _LogInitOK);
        }
    }

    public static void d(String tag, String msg) {
        if (!_LogInitOK) {
            return;
        }

        try {
            Date date = new Date();
            String formattedDate = _SimpleDateFormat.format(date);
            FileOutputStream fos = new FileOutputStream(_fLog, true);
            fos.write(String.format("[ %s ]<%s> :\n%s\n", formattedDate, tag, msg).getBytes());
            fos.close();

//            JCMainThread.OnMessageListener listener = JCMainThread.getOnLogListener();
//            if (listener != null) {
//                listener.log(msg);
//            }
        } catch (IOException e) {
            System.err.println(String.format("%s Exception : %s", LogUtils.class.getName(), e.getMessage()));
        }
    }

    public static void e(String tag, Exception e, StackTraceElement[] listStackTrace) {
        if (!_LogInitOK) {
            return;
        }

        try {
            Date date = new Date();
            String formattedDate = _SimpleDateFormat.format(date);
            StringBuilder sbMessage = new StringBuilder(String.format("[ %s ]<%s> :\n%s -> \n", formattedDate, tag, e.getClass().getName()));
            FileOutputStream fos = new FileOutputStream(_fLog, true);
            sbMessage.append("At [ function : ");
            sbMessage.append(listStackTrace[2].getMethodName());
            sbMessage.append(" ]( ");
            sbMessage.append(listStackTrace[2].getFileName());
            sbMessage.append(" : ");
            sbMessage.append(listStackTrace[2].getLineNumber());
            sbMessage.append(" )");
            fos.write(sbMessage.toString().getBytes());
            fos.close();

            //System.err.println(sbMessage.toString());

//            JCMainThread.OnMessageListener listener = JCMainThread.getOnLogListener();
//            if (listener != null) {
//                listener.log(sbMessage.toString());
//            }
        } catch (IOException ex) {
            System.err.println(String.format("%s Exception : %s", LogUtils.class.getName(), ex.getMessage()));
        }
    }
}
