package cc.winboll.studio.libapputils.log;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/08/12 13:44:06
 * @Describe LogUtils
 * @Describe 应用日志类
 */
import android.content.Context;
import cc.winboll.studio.libapputils.app.WinBollApplication;
import cc.winboll.studio.libapputils.util.FileUtils;
import dalvik.system.DexFile;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class LogUtils {

    public static final String TAG = "LogUtils";

    public static enum LOG_LEVEL { Off, Error, Warn, Info, Debug, Verbose }

    static volatile boolean _IsInited = false;
    static Context _mContext;
    // 日志显示时间格式
    static SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("[yyyyMMdd_HHmmSS]", Locale.getDefault());
    // 应用日志文件夹
    static File _mfLogCacheDir;
    static File _mfLogDataDir;
    // 应用日志文件
    static File _mfLogCatchFile;
    static File _mfLogUtilsBeanFile;
    static LogUtilsBean _mLogUtilsBean;
    public static Map<String, Boolean> mapTAGList = new HashMap<String, Boolean>();

    //
    // 初始化函数
    //
    public static void init(Context context) {
        _mContext = context;
        init(context, LOG_LEVEL.Off);
    }

    //
    // 初始化函数
    //
    public static void init(Context context, LOG_LEVEL logLevel) {
        if (WinBollApplication.isDebug()) {
            // 初始化日志缓存文件路径
            _mfLogCacheDir = new File(context.getApplicationContext().getExternalCacheDir(), TAG);
            if (!_mfLogCacheDir.exists()) {
                _mfLogCacheDir.mkdirs();
            }
            _mfLogCatchFile = new File(_mfLogCacheDir, "log.txt");

            // 初始化日志配置文件路径
            _mfLogDataDir = context.getApplicationContext().getExternalFilesDir(TAG);
            if (!_mfLogDataDir.exists()) {
                _mfLogDataDir.mkdirs();
            }
            _mfLogUtilsBeanFile = new File(_mfLogDataDir, TAG + ".json");
        } else {
            // 初始化日志缓存文件路径
            _mfLogCacheDir = new File(context.getApplicationContext().getCacheDir(), TAG);
            if (!_mfLogCacheDir.exists()) {
                _mfLogCacheDir.mkdirs();
            }
            _mfLogCatchFile = new File(_mfLogCacheDir, "log.txt");

            // 初始化日志配置文件路径
            _mfLogDataDir = new File(context.getApplicationContext().getFilesDir(), TAG);
            if (!_mfLogDataDir.exists()) {
                _mfLogDataDir.mkdirs();
            }
            _mfLogUtilsBeanFile = new File(_mfLogDataDir, TAG + ".json");
        }

//        Toast.makeText(context,
//                       "_mfLogUtilsBeanFile : " + _mfLogUtilsBeanFile
//                       + "\n_mfLogCatchFile : " + _mfLogCatchFile,
//                       Toast.LENGTH_SHORT).show();
//
        _mLogUtilsBean = LogUtilsBean.loadBeanFromFile(_mfLogUtilsBeanFile.getPath(), LogUtilsBean.class);
        if (_mLogUtilsBean == null) {
            _mLogUtilsBean = new LogUtilsBean();
            _mLogUtilsBean.saveBeanToFile(_mfLogUtilsBeanFile.getPath(), _mLogUtilsBean);
        }

        // 加载当前应用下的所有类的 TAG
        addClassTAGList();
        loadTAGBeanSettings();
        _IsInited = true;
        LogUtils.d(TAG, String.format("mapTAGList : %s", mapTAGList.toString()));
    }

    public static Map<String, Boolean> getMapTAGList() {
        return mapTAGList;
    }

    static void loadTAGBeanSettings() {
        ArrayList<LogUtilsClassTAGBean> list = new ArrayList<LogUtilsClassTAGBean>();
        LogUtilsClassTAGBean.loadBeanList(_mContext, list, LogUtilsClassTAGBean.class);
        for (int i = 0; i < list.size(); i++) {
            LogUtilsClassTAGBean beanSetting = list.get(i);
            for (Map.Entry<String, Boolean> entry : mapTAGList.entrySet()) {
                if (entry.getKey().equals(beanSetting.getTag())) {
                    entry.setValue(beanSetting.getEnable());
                }
            }

        }
    }

    static void saveTAGBeanSettings() {
        ArrayList<LogUtilsClassTAGBean> list = new ArrayList<LogUtilsClassTAGBean>();
        for (Map.Entry<String, Boolean> entry : mapTAGList.entrySet()) {
            list.add(new LogUtilsClassTAGBean(entry.getKey(), entry.getValue()));
        }
        LogUtilsClassTAGBean.saveBeanList(_mContext, list, LogUtilsClassTAGBean.class);
    }

    static void addClassTAGList() {
        //ClassLoader classLoader = getClass().getClassLoader();
        try {
            //String packageName = context.getPackageName();
            String packageNamePrefix = "cc.winboll.studio";
            List<String> classNames = new ArrayList<>();
            String apkPath = _mContext.getPackageCodePath();
            //Log.d("APK_PATH", "The APK path is: " + apkPath);
            LogUtils.d(TAG, String.format("apkPath : %s", apkPath));
            //String apkPath = "/data/app/" + packageName + "-";

            //DexFile dexfile = new DexFile(apkPath + "1/base.apk");
            DexFile dexfile = new DexFile(apkPath);

            int countTemp = 0;
            Enumeration<String> entries = dexfile.entries();
            while (entries.hasMoreElements()) {
                countTemp++;
                String className = entries.nextElement();
                if (className.startsWith(packageNamePrefix)) {
                    classNames.add(className);
                }
            }

            LogUtils.d(TAG, String.format("countTemp : %d\nClassNames size : %d", countTemp, classNames.size()));

            for (String className : classNames) {
                try {
                    Class<?> clazz = Class.forName(className);
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()) && field.getType() == String.class && "TAG".equals(field.getName())) {
                            String tagValue = (String) field.get(null);
                            //Log.d("TAG_INFO", "Class: " + className + ", TAG value: " + tagValue);
                            //LogUtils.d(TAG, String.format("Tag Value : %s", tagValue));
                            //mapTAGList.put(tagValue, true);
                            mapTAGList.put(tagValue, false);
                        }
                    }
                } catch (ClassNotFoundException | IllegalAccessException e) {
                    LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                    //Toast.makeText(context, TAG + " : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (IOException e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
            //Toast.makeText(context, TAG + " : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void setTAGListEnable(String tag, boolean isEnable) {
        Iterator<Map.Entry<String, Boolean>> iterator = mapTAGList.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Boolean> entry = iterator.next();
            if (tag.equals(entry.getKey())) {
                entry.setValue(isEnable);
                //System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
                break;
            }
        }
        saveTAGBeanSettings();
        LogUtils.d(TAG, String.format("mapTAGList : %s", mapTAGList.toString()));
    }

    public static void setALlTAGListEnable(boolean isEnable) {
        Iterator<Map.Entry<String, Boolean>> iterator = mapTAGList.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Boolean> entry = iterator.next();
            entry.setValue(isEnable);
            //System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
        saveTAGBeanSettings();
        LogUtils.d(TAG, String.format("mapTAGList : %s", mapTAGList.toString()));
    }

    public static void setLogLevel(LOG_LEVEL logLevel) {
        LogUtils._mLogUtilsBean.setLogLevel(logLevel);
        _mLogUtilsBean.saveBeanToFile(_mfLogUtilsBeanFile.getPath(), _mLogUtilsBean);
    }

    public static LOG_LEVEL getLogLevel() {
        return LogUtils._mLogUtilsBean.getLogLevel();
    }

    static boolean isLoggable(String tag, LOG_LEVEL logLevel) {
        return _IsInited && mapTAGList.get(tag) && isInTheLevel(logLevel);
    }

    static boolean isInTheLevel(LOG_LEVEL logLevel) {
        return (LogUtils._mLogUtilsBean.getLogLevel().ordinal() == logLevel.ordinal()
            || LogUtils._mLogUtilsBean.getLogLevel().ordinal() > logLevel.ordinal());
    }

    //
    // 获取应用日志文件夹
    //
    public static File getLogCacheDir() {
        return _mfLogCacheDir;
    }

    //
    // 调试日志写入函数
    //
    public static void e(String szTAG, String szMessage) {
        if (isLoggable(szTAG, LogUtils.LOG_LEVEL.Error)) {
            saveLog(szTAG, LogUtils.LOG_LEVEL.Error, szMessage);
        }
    }

    //
    // 调试日志写入函数
    //
    public static void w(String szTAG, String szMessage) {
        if (isLoggable(szTAG, LogUtils.LOG_LEVEL.Warn)) {
            saveLog(szTAG, LogUtils.LOG_LEVEL.Warn, szMessage);
        }
    }

    //
    // 调试日志写入函数
    //
    public static void i(String szTAG, String szMessage) {
        if (isLoggable(szTAG, LogUtils.LOG_LEVEL.Info)) {
            saveLog(szTAG, LogUtils.LOG_LEVEL.Info, szMessage);
        }
    }

    //
    // 调试日志写入函数
    //
    public static void d(String szTAG, String szMessage) {
        if (isLoggable(szTAG, LogUtils.LOG_LEVEL.Debug)) {
            saveLog(szTAG, LogUtils.LOG_LEVEL.Debug, szMessage);
        }
    }

    //
    // 调试日志写入函数
    // 包含线程调试堆栈信息
    //
    public static void d(String szTAG, String szMessage, StackTraceElement[] listStackTrace) {
        if (isLoggable(szTAG, LogUtils.LOG_LEVEL.Debug)) {
            StringBuilder sbMessage = new StringBuilder(szMessage);
            sbMessage.append(" \nAt ");
            sbMessage.append(listStackTrace[2].getMethodName());
            sbMessage.append(" (");
            sbMessage.append(listStackTrace[2].getFileName());
            sbMessage.append(":");
            sbMessage.append(listStackTrace[2].getLineNumber());
            sbMessage.append(")");
            saveLog(szTAG, LogUtils.LOG_LEVEL.Debug, sbMessage.toString());
        }
    }

    //
    // 调试日志写入函数
    // 包含异常信息和线程调试堆栈信息
    //
    public static void d(String szTAG, Exception e, StackTraceElement[] listStackTrace) {
        if (isLoggable(szTAG, LogUtils.LOG_LEVEL.Debug)) {
            StringBuilder sbMessage = new StringBuilder(e.getClass().toGenericString());
            sbMessage.append(" : ");
            sbMessage.append(e.getMessage());
            sbMessage.append(" \nAt ");
            sbMessage.append(listStackTrace[2].getMethodName());
            sbMessage.append(" (");
            sbMessage.append(listStackTrace[2].getFileName());
            sbMessage.append(":");
            sbMessage.append(listStackTrace[2].getLineNumber());
            sbMessage.append(")");
            saveLog(szTAG, LogUtils.LOG_LEVEL.Debug, sbMessage.toString());
        }
    }

    //
    // 调试日志写入函数
    //
    public static void v(String szTAG, String szMessage) {
        if (isLoggable(szTAG, LogUtils.LOG_LEVEL.Verbose)) {
            saveLog(szTAG, LogUtils.LOG_LEVEL.Verbose, szMessage);
        }
    }

    //
    // 日志文件保存函数
    //
    static void saveLog(String szTAG, LogUtils.LOG_LEVEL logLevel, String szMessage) {
        try {
            BufferedWriter out = null;
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(_mfLogCatchFile, true), "UTF-8"));
            out.write("[" + logLevel + "]  " + mSimpleDateFormat.format(System.currentTimeMillis()) + "  [" + szTAG + "]\n" + szMessage + "\n");
            out.close();
        } catch (IOException e) {
            LogUtils.d(TAG, "IOException : " + e.getMessage());
        }
    }

    //
    // 历史日志加载函数
    //
    public static String loadLog() {
        if (_mfLogCatchFile.exists()) {
            StringBuffer sb = new StringBuffer();
            try {
                BufferedReader in = null;
                in = new BufferedReader(new InputStreamReader(new FileInputStream(_mfLogCatchFile), "UTF-8"));
                String line = "";
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
            } catch (IOException e) {
                LogUtils.d(TAG, "IOException : " + e.getMessage());
            } 
            return sb.toString();
        }
        return "";
    }

    //
    // 清理日志函数
    //
    public static void cleanLog() {
        if (_mfLogCatchFile.exists()) {
            try {
                FileUtils.writeStringToFile(_mfLogCatchFile.getPath(), "");
                //LogUtils.d(TAG, "cleanLog");
            } catch (IOException e) {
                LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
            }
        }
    }
}
