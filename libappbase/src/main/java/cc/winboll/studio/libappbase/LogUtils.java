package cc.winboll.studio.libappbase;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import cc.winboll.studio.libappbase.GlobalApplication;
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

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @CreateTime 2026-05-09 15:46:28
 * @EditTime 2026-05-11 15:36:12
 * @Describe 应用日志工具类
 * 补全多级别日志重载、自动日志文件裁剪、应用内TAG自动扫描管理；
 * 支持日志本地持久化、异常堆栈格式化、TAG开关配置、线程与集合打印工具；
 * 完全兼容Java7语法，严格遵循变量final编码规范；
 * 重要说明：本类内部调试打印必须使用 android.util.Log，禁止使用LogUtils自身方法，
 * 避免递归嵌套调用、逻辑漩涡与无限循环调用问题。
 */
public class LogUtils {

    // ====================== 常量与枚举 ======================
    public static final String TAG = "LogUtils";

    /**
     * 日志级别枚举
     */
    public static enum LOG_LEVEL {
        Off,
        Error,
        Warn,
        Info,
        Debug,
        Verbose
		}

    // ====================== 全局静态成员 ======================
    private static volatile boolean _IsInited = false;
    private static Context _mContext;
    private static final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("[yyyyMMdd_HHmmss_SSS]", Locale.getDefault());
    private static File _mfLogCacheDir;
    private static File _mfLogDataDir;
    private static File _mfLogCatchFile;
    private static File _mfLogUtilsBeanFile;
    private static LogUtilsBean _mLogUtilsBean;
    public static final Map<String, Boolean> mapTAGList = new HashMap<String, Boolean>();

    // ====================== 初始化入口 ======================
    public static void init(final Context context) {
		init(context, LOG_LEVEL.Off);
    }

    public static void init(final Context context, final LOG_LEVEL logLevel) {
		Log.d(TAG, "init 执行日志工具初始化");
		_mContext = context;

		initLogUtilsDir();

		initLogConfigBean();
		addClassTAGList();
		loadTAGBeanSettings();
		checkAndTrimLogFileSize();

		_IsInited = true;
		Log.d(TAG, "init 执行日志工具初始化完成");
    }

    // ====================== 目录初始化 ======================
    private static void initLogUtilsDir() {
        final Context appContext = _mContext.getApplicationContext();
        _mfLogCacheDir = new File(appContext.getCacheDir(), TAG);
        if (!_mfLogCacheDir.exists()) {
            _mfLogCacheDir.mkdirs();
        }
        _mfLogCatchFile = new File(_mfLogCacheDir, "log.txt");

        _mfLogDataDir = appContext.getExternalFilesDir(TAG);
        if (!_mfLogDataDir.exists()) {
            _mfLogDataDir.mkdirs();
        }
        _mfLogUtilsBeanFile = new File(_mfLogDataDir, TAG + ".json");
    }

    private static void initLogConfigBean() {
        _mLogUtilsBean = LogUtilsBean.loadBeanFromFile(_mfLogUtilsBeanFile.getPath(), LogUtilsBean.class);
        if (_mLogUtilsBean == null) {
            _mLogUtilsBean = new LogUtilsBean();
            _mLogUtilsBean.saveBeanToFile(_mfLogUtilsBeanFile.getPath(), _mLogUtilsBean);
            Log.d(TAG, "initLogConfigBean 自动创建默认日志配置文件");
        }
    }

    // ====================== 日志文件裁剪 ======================
    private static void checkAndTrimLogFileSize() {
        if (_mfLogCatchFile == null || !_mfLogCatchFile.exists()) {
            return;
        }

		final long KEEP_FILE_SIZE = 25000L; // ~25KB 确保剪贴板可完整复制
        final long MAX_FILE_SIZE = 2 * KEEP_FILE_SIZE;
        final long fileSize = _mfLogCatchFile.length();

        if (fileSize <= MAX_FILE_SIZE) {
            return;
        }

        final long needSkip = fileSize - KEEP_FILE_SIZE;
        final String lineBreak = System.lineSeparator();

        FileInputStream fis = null;
        BufferedReader reader = null;
        FileOutputStream fos = null;
        BufferedWriter writer = null;

        try {
            fis = new FileInputStream(_mfLogCatchFile);
            reader = new BufferedReader(new InputStreamReader(fis));
            fos = new FileOutputStream(_mfLogCatchFile);
            writer = new BufferedWriter(new OutputStreamWriter(fos));

            final StringBuilder sb = new StringBuilder();
            String line;
            long skippedTotal = 0;

            while ((line = reader.readLine()) != null) {
                final byte[] lineBytes = line.getBytes();
                skippedTotal += lineBytes.length + lineBreak.getBytes().length;
                if (skippedTotal > needSkip) {
                    sb.append(line).append(lineBreak);
                }
            }
            writer.write(sb.toString());
            Log.d(TAG, "checkAndTrimLogFileSize 日志文件裁剪完成");
        } catch (IOException e) {
            Log.e(TAG, "checkAndTrimLogFileSize 日志文件裁剪失败", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {}
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {}
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {}
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {}
        }
    }

    // ====================== TAG 配置管理 ======================
    public static Map<String, Boolean> getMapTAGList() {
        return mapTAGList;
    }

    private static void loadTAGBeanSettings() {
        final ArrayList<LogUtilsClassTAGBean> list = new ArrayList<LogUtilsClassTAGBean>();
        LogUtilsClassTAGBean.loadBeanList(_mContext, list, LogUtilsClassTAGBean.class);

        for (final LogUtilsClassTAGBean beanSetting : list) {
            final String tag = beanSetting.getTag();
            if (mapTAGList.containsKey(tag)) {
                mapTAGList.put(tag, beanSetting.getEnable());
            }
        }
    }

    private static void saveTAGBeanSettings() {
        final ArrayList<LogUtilsClassTAGBean> list = new ArrayList<LogUtilsClassTAGBean>();
        for (final Map.Entry<String, Boolean> entry : mapTAGList.entrySet()) {
            list.add(new LogUtilsClassTAGBean(entry.getKey(), entry.getValue()));
        }
        LogUtilsClassTAGBean.saveBeanList(_mContext, list, LogUtilsClassTAGBean.class);
    }

    private static void addClassTAGList() {
        try {
            final String packageNamePrefix = "cc.winboll.studio";
            final List<String> classNames = new ArrayList<String>();
            final String apkPath = _mContext.getPackageCodePath();

            final DexFile dexfile = new DexFile(apkPath);
            Enumeration<String> entries = dexfile.entries();

            while (entries.hasMoreElements()) {
                final String className = entries.nextElement();
                if (className.startsWith(packageNamePrefix)) {
                    classNames.add(className);
                }
            }

            for (final String className : classNames) {
                try {
                    final Class<?> clazz = Class.forName(className);
                    final Field[] fields = clazz.getDeclaredFields();

                    for (final Field field : fields) {
                        if (Modifier.isStatic(field.getModifiers())
							&& Modifier.isPublic(field.getModifiers())
							&& field.getType() == String.class
							&& "TAG".equals(field.getName())) {

                            final String tagValue = (String) field.get(null);
                            mapTAGList.put(tagValue, false);
                        }
                    }
                } catch (NoClassDefFoundError e) {
                    Log.w(TAG, "addClassTAGList 解析类TAG失败 NoClassDefFoundError");
                } catch (ClassNotFoundException e) {
                    Log.w(TAG, "addClassTAGList 解析类TAG失败 ClassNotFoundException");
                } catch (IllegalAccessException e) {
                    Log.w(TAG, "addClassTAGList 解析类TAG失败 IllegalAccessException");
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "addClassTAGList 扫描Dex异常", e);
        }
    }

    public static void setTAGListEnable(final String tag, final boolean isEnable) {
        final Iterator<Map.Entry<String, Boolean>> iterator = mapTAGList.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, Boolean> entry = iterator.next();
            if (tag.equals(entry.getKey())) {
                entry.setValue(isEnable);
                break;
            }
        }
        saveTAGBeanSettings();
        Log.d(TAG, "setTAGListEnable 更新TAG开关配置");
    }

    public static void setALlTAGListEnable(final boolean isEnable) {
        for (final Map.Entry<String, Boolean> entry : mapTAGList.entrySet()) {
            entry.setValue(isEnable);
        }
        saveTAGBeanSettings();
        Log.d(TAG, "setALlTAGListEnable 全量设置TAG开关");
    }

    // ====================== 日志级别控制 ======================
    public static void setLogLevel(final LOG_LEVEL logLevel) {
        _mLogUtilsBean.setLogLevel(logLevel);
        _mLogUtilsBean.saveBeanToFile(_mfLogUtilsBeanFile.getPath(), _mLogUtilsBean);
    }

    public static LOG_LEVEL getLogLevel() {
        return _mLogUtilsBean == null ?LOG_LEVEL.Off: _mLogUtilsBean.getLogLevel();
    }

    private static boolean isLoggable(final String tag, final LOG_LEVEL logLevel) {
		if (!GlobalApplication.isDebugging()) {
			return false;
		}
        if (!_IsInited) {
            return false;
        }
        if (mapTAGList.get(tag) == null || !mapTAGList.get(tag)) {
            return false;
        }
        return isInTheLevel(logLevel);
    }

    private static boolean isInTheLevel(final LOG_LEVEL logLevel) {
        return _mLogUtilsBean.getLogLevel().ordinal() >= logLevel.ordinal();
    }

    // ====================== 基础对外方法 ======================
    public static File getLogCacheDir() {
        return _mfLogCacheDir;
    }

    public static boolean isInited() {
        return _IsInited;
    }

    // ====================== Error 日志重载（补齐缺失方法） ======================
    public static void e(final String szTAG, final String szMessage) {
        if (isLoggable(szTAG, LOG_LEVEL.Error)) {
            saveLog(szTAG, LOG_LEVEL.Error, szMessage);
        }
    }

    public static void e(final String szTAG, final String szMessage, final Exception e) {
        if (isLoggable(szTAG, LOG_LEVEL.Error)) {
            final StringBuilder sb = new StringBuilder(szMessage);
            sb.append("\n【异常信息】: ").append(getExceptionInfo(e));
            saveLog(szTAG, LOG_LEVEL.Error, sb.toString());
        }
    }

    public static void e(final String szTAG, final Exception e) {
        if (isLoggable(szTAG, LOG_LEVEL.Error)) {
            final String message = "【异常信息】: " + getExceptionInfo(e);
            saveLog(szTAG, LOG_LEVEL.Error, message);
        }
    }

    public static void e(final String szTAG, final String szMessage, final Exception e, final StackTraceElement[] listStackTrace) {
        if (isLoggable(szTAG, LOG_LEVEL.Error)) {
            final StringBuilder sb = new StringBuilder(szMessage);
            sb.append("\n【异常信息】: ").append(getExceptionInfo(e));
            sb.append("\n【堆栈信息】: ").append(getStackTraceInfo(listStackTrace));
            saveLog(szTAG, LOG_LEVEL.Error, sb.toString());
        }
    }

    /**
     * 补齐你需要的：LogUtils.e(TAG, "uncaughtException: 崩溃处理异常", e)
     */
    public static void e(final String szTAG, final String szMessage, final Throwable e) {
        if (isLoggable(szTAG, LOG_LEVEL.Error)) {
            final StringBuilder sb = new StringBuilder(szMessage);
            sb.append("\n【异常信息】: ").append(getThrowableInfo(e));
            saveLog(szTAG, LOG_LEVEL.Error, sb.toString());
        }
    }

    // ====================== Warn 日志重载 ======================
    public static void w(final String szTAG, final String szMessage) {
        if (isLoggable(szTAG, LOG_LEVEL.Warn)) {
            saveLog(szTAG, LOG_LEVEL.Warn, szMessage);
        }
    }

    public static void w(final String szTAG, final String szMessage, final Exception e) {
        if (isLoggable(szTAG, LOG_LEVEL.Warn)) {
            final StringBuilder sb = new StringBuilder(szMessage);
            sb.append("\n【异常信息】: ").append(getExceptionInfo(e));
            saveLog(szTAG, LOG_LEVEL.Warn, sb.toString());
        }
    }

    public static void w(final String szTAG, final Exception e) {
        if (isLoggable(szTAG, LOG_LEVEL.Warn)) {
            final String message = "【异常信息】: " + getExceptionInfo(e);
            saveLog(szTAG, LOG_LEVEL.Warn, message);
        }
    }

    // ====================== Info 日志重载 ======================
    public static void i(final String szTAG, final String szMessage) {
        if (isLoggable(szTAG, LOG_LEVEL.Info)) {
            saveLog(szTAG, LOG_LEVEL.Info, szMessage);
        }
    }

    public static void i(final String szTAG, final String szMessage, final Object obj) {
        if (isLoggable(szTAG, LOG_LEVEL.Info)) {
            final String objStr = obj == null ? "null" : obj.toString();
            final StringBuilder sb = new StringBuilder(szMessage);
            sb.append("\n【数据对象】: ").append(objStr);
            saveLog(szTAG, LOG_LEVEL.Info, sb.toString());
        }
    }

    // ====================== Debug 日志重载 ======================
    public static void d(final String szTAG, final String szMessage) {
        if (isLoggable(szTAG, LOG_LEVEL.Debug)) {
            saveLog(szTAG, LOG_LEVEL.Debug, szMessage);
        }
    }

    public static void d(final String szTAG, final String szMessage, final StackTraceElement[] listStackTrace) {
        if (isLoggable(szTAG, LOG_LEVEL.Debug)) {
            final StringBuilder sbMessage = new StringBuilder(szMessage);
            sbMessage.append("\n【调用堆栈】: ").append(getStackTraceInfo(listStackTrace));
            saveLog(szTAG, LOG_LEVEL.Debug, sbMessage.toString());
        }
    }

    public static void d(final String szTAG, final String szMessage, final Exception e) {
        if (isLoggable(szTAG, LOG_LEVEL.Debug)) {
            final StringBuilder sb = new StringBuilder(szMessage);
            sb.append("\n【异常信息】: ").append(getExceptionInfo(e));
            saveLog(szTAG, LOG_LEVEL.Debug, sb.toString());
        }
    }

    public static void d(final String szTAG, final Exception e, final StackTraceElement[] listStackTrace) {
        if (isLoggable(szTAG, LOG_LEVEL.Debug)) {
            final StringBuilder sbMessage = new StringBuilder();
            sbMessage.append("【异常信息】: ").append(getExceptionInfo(e));
            sbMessage.append("\n【调用堆栈】: ").append(getStackTraceInfo(listStackTrace));
            saveLog(szTAG, LOG_LEVEL.Debug, sbMessage.toString());
        }
    }

    public static void d(final String szTAG, final String szMessage, final Object obj) {
        if (isLoggable(szTAG, LOG_LEVEL.Debug)) {
            final String objStr = obj == null ? "null" : obj.toString();
            final StringBuilder sb = new StringBuilder(szMessage);
            sb.append("\n【数据对象】: ").append(objStr);
            saveLog(szTAG, LOG_LEVEL.Debug, sb.toString());
        }
    }

    public static void d(final String szTAG, final Object obj) {
        if (isLoggable(szTAG, LOG_LEVEL.Debug)) {
            final String objStr = obj == null ? "null" : obj.toString();
            final String message = "【数据对象】: " + objStr;
            saveLog(szTAG, LOG_LEVEL.Debug, message);
        }
    }

    // ====================== Verbose 日志重载 ======================
    public static void v(final String szTAG, final String szMessage) {
        if (isLoggable(szTAG, LOG_LEVEL.Verbose)) {
            saveLog(szTAG, LOG_LEVEL.Verbose, szMessage);
        }
    }

    public static void v(final String szTAG, final String szMessage, final Object obj) {
        if (isLoggable(szTAG, LOG_LEVEL.Verbose)) {
            final String objStr = obj == null ? "null" : obj.toString();
            final StringBuilder sb = new StringBuilder(szMessage);
            sb.append("\n【数据对象】: ").append(objStr);
            saveLog(szTAG, LOG_LEVEL.Verbose, sb.toString());
        }
    }

    public static void v(final String szTAG, final Object obj) {
        if (isLoggable(szTAG, LOG_LEVEL.Verbose)) {
            final String objStr = obj == null ? "null" : obj.toString();
            final String message = "【数据对象】: " + objStr;
            saveLog(szTAG, LOG_LEVEL.Verbose, message);
        }
    }

    // ====================== 扩展工具打印 ======================
    public static void printThreadInfo(final String szTAG, final String szMessage) {
        if (isLoggable(szTAG, LOG_LEVEL.Debug)) {
            final Thread currentThread = Thread.currentThread();
            final StringBuilder sb = new StringBuilder(szMessage);
            sb.append("\n【线程信息】: ")
				.append("线程名=").append(currentThread.getName())
				.append(", 线程ID=").append(currentThread.getId())
				.append(", 线程状态=").append(currentThread.getState().name());
            saveLog(szTAG, LOG_LEVEL.Debug, sb.toString());
        }
    }

    public static <K, V> void printMap(final String szTAG, final String szMessage, final Map<K, V> map) {
        if (isLoggable(szTAG, LOG_LEVEL.Debug) && map != null) {
            final StringBuilder sb = new StringBuilder(szMessage);
            sb.append("\n【Map 数据】size=").append(map.size());
            for (final Map.Entry<K, V> entry : map.entrySet()) {
                final String keyStr = entry.getKey() == null ? "null" : entry.getKey().toString();
                final String valueStr = entry.getValue() == null ? "null" : entry.getValue().toString();
                sb.append("\n  ").append(keyStr).append(" = ").append(valueStr);
            }
            saveLog(szTAG, LOG_LEVEL.Debug, sb.toString());
        }
    }

    public static <T> void printList(final String szTAG, final String szMessage, final List<T> list) {
        if (isLoggable(szTAG, LOG_LEVEL.Debug) && list != null) {
            final StringBuilder sb = new StringBuilder(szMessage);
            sb.append("\n【List 数据】size=").append(list.size());
            for (int i = 0; i < list.size(); i++) {
                final T item = list.get(i);
                final String itemStr = item == null ? "null" : item.toString();
                sb.append("\n  索引").append(i).append(" = ").append(itemStr);
            }
            saveLog(szTAG, LOG_LEVEL.Debug, sb.toString());
        }
    }

    // ====================== 私有格式化工具 ======================
    private static String getExceptionInfo(final Exception e) {
        if (e == null) {
            return "异常对象为null";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getSimpleName()).append(" : ")
			.append(e.getMessage() == null ? "无异常消息" : e.getMessage());

        final StackTraceElement[] stackTrace = e.getStackTrace();
        if (stackTrace != null && stackTrace.length > 0) {
            final int limit = Math.min(stackTrace.length, 5);
            for (int i = 0; i < limit; i++) {
                sb.append("\n    ").append(stackTrace[i].toString());
            }
        }
        return sb.toString();
    }

    private static String getThrowableInfo(final Throwable e) {
        if (e == null) {
            return "异常对象为null";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getSimpleName()).append(" : ")
			.append(e.getMessage() == null ? "无异常消息" : e.getMessage());

        final StackTraceElement[] stackTrace = e.getStackTrace();
        if (stackTrace != null && stackTrace.length > 0) {
            final int limit = Math.min(stackTrace.length, 5);
            for (int i = 0; i < limit; i++) {
                sb.append("\n    ").append(stackTrace[i].toString());
            }
        }
        return sb.toString();
    }

    private static String getStackTraceInfo(final StackTraceElement[] stackTrace) {
        if (stackTrace == null || stackTrace.length == 0) {
            return "堆栈信息为空";
        }
        final StringBuilder sb = new StringBuilder();
        int count = 0;
        for (final StackTraceElement element : stackTrace) {
            if (element.getClassName().contains("cc.winboll.studio.libappbase.LogUtils")) {
                continue;
            }
            sb.append("\n    ").append(element.getClassName()).append(".")
				.append(element.getMethodName()).append("(")
				.append(element.getFileName()).append(":").append(element.getLineNumber()).append(")");
            count++;
            if (count >= 8) {
                break;
            }
        }
        return sb.toString();
    }

    // ====================== 日志持久化读写 ======================
    private static void saveLog(final String szTAG, final LogUtils.LOG_LEVEL logLevel, final String szMessage) {
        BufferedWriter out = null;
        try {
            if (!_mfLogCatchFile.exists()) {
                final File parentDir = _mfLogCatchFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                _mfLogCatchFile.createNewFile();
            }
            out = new BufferedWriter(new OutputStreamWriter(
										 new FileOutputStream(_mfLogCatchFile, true), "UTF-8"));
            final String logLine = "[" + logLevel + "]  "
				+ mSimpleDateFormat.format(System.currentTimeMillis())
				+ "  [" + szTAG + "]\n"
				+ szMessage + "\n\n";
            out.write(logLine);
            out.flush();
        } catch (IOException e) {
            Log.e(TAG, "saveLog 日志写入失败", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, "saveLog 流关闭失败", e);
                }
            }
        }
    }

    public static String loadLog() {
        if (_mfLogCatchFile == null || !_mfLogCatchFile.exists()) {
            return "日志文件不存在";
        }
        final StringBuffer sb = new StringBuffer();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
										new FileInputStream(_mfLogCatchFile), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            sb.append("日志加载失败");
            Log.e(TAG, "loadLog 读取日志异常", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "loadLog 读取流关闭异常", e);
                }
            }
        }
        return sb.toString();
    }

    public static void cleanLog() {
        if (_mfLogCatchFile == null) {
            Log.d(TAG, "cleanLog 日志文件未初始化");
            return;
        }
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
										 new FileOutputStream(_mfLogCatchFile, false), "UTF-8"));
            out.write("");
            out.flush();
            Log.d(TAG, "cleanLog 日志已清空");
        } catch (IOException e) {
            Log.e(TAG, "cleanLog 清空日志失败", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, "cleanLog 流关闭异常", e);
                }
            }
        }
    }

    public static boolean checkLogFileSize(final int maxSizeMB) {
        if (_mfLogCatchFile == null || !_mfLogCatchFile.exists()) {
            return false;
        }
        final long maxSizeByte = 1048576L * maxSizeMB;
        final long fileSize = _mfLogCatchFile.length();
        return fileSize > maxSizeByte;
    }

    public static void showShortToast(final Context context, final String message) {
        if (context == null || message == null) {
            return;
        }
        if (Thread.currentThread().getId() == android.os.Process.myTid()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        } else {
            final Context uiContext = context;
            ((android.app.Activity) uiContext).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(uiContext, message, Toast.LENGTH_SHORT).show();
					}
				});
        }
    }
}

