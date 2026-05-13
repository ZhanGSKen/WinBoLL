package cc.winboll.studio.positions;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.positions.services.MainService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 全局Application应用入口类
 * 功能概括：
 * 1. 全局初始化管理、工具类框架初始化
 * 2. 应用全局空转状态统一管理
 * 3. IO读写通用工具封装
 * 4. 全局崩溃捕获、崩溃日志本地保存、崩溃弹窗页面
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @CreateTime 2026/05/03 12:23:00
 * @EditTime   2026/05/03 15:02:47
 */
public class App extends GlobalApplication {

    //===================== 全局静态常量与变量 =====================
    public static final String TAG = "App";
    private static Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static App sInstance;

    private static final String SP_NAME = "app_idle_config";
    private static final String SP_KEY_IDLE_RUNNING = "is_idle_running";

    // 应用全局空转状态标记
    private static boolean isAppIdleRunning = false;

    //===================== 空转状态对外方法 =====================
    /**
     * 获取当前应用空转运行状态
     * @return 是否处于空转状态
     */
    public static boolean isAppIdleRunning() {
        if (isAppIdleRunning) {
            LogUtils.d(TAG, "isAppIdleRunning -> 当前应用处于空转运行状态");
        }
        return isAppIdleRunning;
    }

    /**
     * 设置应用空转运行状态
     * @param idleRunning 空转开关状态
     */
    public static void setAppIdleRunning(boolean idleRunning) {
        LogUtils.d(TAG, "setAppIdleRunning -> 传入参数 idleRunning = " + idleRunning);
        if (isDebugging()) {
            isAppIdleRunning = idleRunning;
            saveIdleRunningToSp(idleRunning);
            LogUtils.i(TAG, "setAppIdleRunning -> 调试模式，空转状态设置生效并持久化");
            
            // 重启MainService服务以同步新的空转状态（stopService对未运行服务无效，故无需判断状态）
            if (sInstance != null) {
                LogUtils.i(TAG, "setAppIdleRunning -> 重启MainService服务以同步空转状态");
                Intent serviceIntent = new Intent(sInstance, MainService.class);
                sInstance.stopService(serviceIntent);
                sInstance.startService(serviceIntent);
            }
        } else {
            LogUtils.i(TAG, "setAppIdleRunning -> 非调试模式，空转设置无效");
            LogUtils.i(TAG, "Non-debug state, app idle setting is meaningless.");
        }
    }

    /**
     * 从SharedPreferences加载空转状态
     */
    private static void loadIdleRunningFromSp() {
        try {
            if (sInstance != null) {
                SharedPreferences sp = sInstance.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
                isAppIdleRunning = sp.getBoolean(SP_KEY_IDLE_RUNNING, false);
                LogUtils.i(TAG, "loadIdleRunningFromSp -> 从SP加载空转状态：" + isAppIdleRunning);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "loadIdleRunningFromSp -> 加载空转状态失败：" + e.getMessage());
            isAppIdleRunning = false;
        }
    }

    /**
     * 保存空转状态到SharedPreferences
     * @param idleRunning 空转状态
     */
    private static void saveIdleRunningToSp(boolean idleRunning) {
        try {
            if (sInstance != null) {
                SharedPreferences sp = sInstance.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
                sp.edit().putBoolean(SP_KEY_IDLE_RUNNING, idleRunning).apply();
                LogUtils.i(TAG, "saveIdleRunningToSp -> 空转状态已保存：" + idleRunning);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "saveIdleRunningToSp -> 保存空转状态失败：" + e.getMessage());
        }
    }

    //===================== 应用生命周期 =====================
    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        LogUtils.i(TAG, "onCreate -> 全局Application初始化开始");

        setIsDebugging(BuildConfig.DEBUG);
        WinBoLLActivityManager.init(this);
        ToastUtils.init(this);

        loadIdleRunningFromSp();

        LogUtils.i(TAG, "onCreate -> 全局组件初始化全部完成");
    }

    //===================== IO通用工具方法 =====================
    /**
     * 流式读写输入输出流
     * @param input 输入流
     * @param output 输出流
     * @throws IOException IO读写异常
     */
    public static void write(InputStream input, OutputStream output) throws IOException {
        byte[] buf = new byte[1024 * 8];
        int len;
        while ((len = input.read(buf)) != -1) {
            output.write(buf, 0, len);
        }
    }

    /**
     * 将字节数组写入指定文件
     * @param file 目标文件
     * @param data 字节数据
     * @throws IOException IO读写异常
     */
    public static void write(File file, byte[] data) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        ByteArrayInputStream input = new ByteArrayInputStream(data);
        FileOutputStream output = new FileOutputStream(file);
        try {
            write(input, output);
        } finally {
            closeIO(input, output);
        }
    }

    /**
     * 输入流转为UTF-8字符串
     * @param input 输入流
     * @return 转换后的文本
     * @throws IOException IO读写异常
     */
    public static String toString(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(input, output);
        try {
            return output.toString("UTF-8");
        } finally {
            closeIO(input, output);
        }
    }

    /**
     * 批量关闭IO流
     * @param closeables 可关闭对象
     */
    public static void closeIO(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            try {
                if (closeable != null) {
                    closeable.close();
                }
            } catch (IOException ignored) {

            }
        }
    }

    //===================== 全局崩溃捕获管理类 =====================
    public static class CrashHandler {

        public static final UncaughtExceptionHandler DEFAULT_UNCAUGHT_EXCEPTION_HANDLER = Thread.getDefaultUncaughtExceptionHandler();
        private static CrashHandler sInstance;
        private PartCrashHandler mPartCrashHandler;

        public static CrashHandler getInstance() {
            if (sInstance == null) {
                sInstance = new CrashHandler();
            }
            return sInstance;
        }

        /**
         * 注册全局崩溃捕获
         */
        public void registerGlobal(Context context) {
            registerGlobal(context, null);
        }

        /**
         * 注册全局崩溃并自定义崩溃日志保存目录
         * @param crashDir 崩溃日志路径
         */
        public void registerGlobal(Context context, String crashDir) {
            LogUtils.d(TAG, "CrashHandler -> 注册全局异常捕获器");
            Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandlerImpl(context.getApplicationContext(), crashDir));
        }

        /**
         * 解除全局崩溃捕获
         */
        public void unregister() {
            LogUtils.d(TAG, "CrashHandler -> 解除全局异常捕获");
            Thread.setDefaultUncaughtExceptionHandler(DEFAULT_UNCAUGHT_EXCEPTION_HANDLER);
        }

        /**
         * 注册局部前台异常捕获
         */
        public void registerPart(Context context) {
            unregisterPart(context);
            mPartCrashHandler = new PartCrashHandler(context.getApplicationContext());
            MAIN_HANDLER.postAtFrontOfQueue(mPartCrashHandler);
            LogUtils.d(TAG, "CrashHandler -> 局部异常监听已注册");
        }

        /**
         * 解除局部异常捕获
         */
        public void unregisterPart(Context context) {
            if (mPartCrashHandler != null) {
                mPartCrashHandler.isRunning.set(false);
                mPartCrashHandler = null;
                LogUtils.d(TAG, "CrashHandler -> 局部异常监听已销毁");
            }
        }

        /**
         * 局部前台Looper异常捕获
         */
        private static class PartCrashHandler implements Runnable {
            private final Context mContext;
            public AtomicBoolean isRunning = new AtomicBoolean(true);

            public PartCrashHandler(Context context) {
                this.mContext = context;
            }

            @Override
            public void run() {
                while (isRunning.get()) {
                    try {
                        Looper.loop();
                    } catch (final Throwable e) {
                        if (isRunning.get()) {
                            MAIN_HANDLER.post(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(mContext, e.toString(), Toast.LENGTH_LONG).show();
									}
								});
                        } else {
                            if (e instanceof RuntimeException) {
                                throw (RuntimeException) e;
                            } else {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        }

        /**
         * 全局未捕获异常处理实现
         */
        private static class UncaughtExceptionHandlerImpl implements UncaughtExceptionHandler {
            private static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
            private final Context mContext;
            private final File mCrashDir;

            public UncaughtExceptionHandlerImpl(Context context, String crashDir) {
                this.mContext = context;
                this.mCrashDir = TextUtils.isEmpty(crashDir) ? new File(mContext.getExternalCacheDir(), "crash") : new File(crashDir);
            }

            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                try {
                    String log = buildLog(throwable);
                    writeLog(log);

                    Intent intent = new Intent(mContext, CrashActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(Intent.EXTRA_TEXT, log);
                    mContext.startActivity(intent);

                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                } catch (Throwable e) {
                    if (DEFAULT_UNCAUGHT_EXCEPTION_HANDLER != null) {
                        DEFAULT_UNCAUGHT_EXCEPTION_HANDLER.uncaughtException(thread, throwable);
                    }
                }
            }

            /**
             * 组装崩溃详细日志信息
             */
            private String buildLog(Throwable throwable) {
                String time = DATE_FORMAT.format(new Date());
                String versionName = "unknown";
                long versionCode = 0;

                try {
                    PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                    versionName = packageInfo.versionName;
                    versionCode = Build.VERSION.SDK_INT >= 28 ? packageInfo.getLongVersionCode() : packageInfo.versionCode;
                } catch (Throwable ignored) {

                }

                LinkedHashMap<String, String> head = new LinkedHashMap<String, String>();
                head.put("Time Of Crash", time);
                head.put("Device", String.format("%s, %s", Build.MANUFACTURER, Build.MODEL));
                head.put("Android Version", String.format("%s (%d)", Build.VERSION.RELEASE, Build.VERSION.SDK_INT));
                head.put("App Version", String.format("%s (%d)", versionName, versionCode));
                head.put("Kernel", getKernel());
                head.put("Support Abis", Build.VERSION.SDK_INT >= 21 && Build.SUPPORTED_ABIS != null ? Arrays.toString(Build.SUPPORTED_ABIS) : "unknown");
                head.put("Fingerprint", Build.FINGERPRINT);

                StringBuilder builder = new StringBuilder();
                for (String key : head.keySet()) {
                    if (builder.length() != 0) {
                        builder.append("\n");
                    }
                    builder.append(key);
                    builder.append(" :    ");
                    builder.append(head.get(key));
                }
                builder.append("\n\n");
                builder.append(throwable.toString());

                return builder.toString();
            }

            /**
             * 写入崩溃日志到本地文件
             */
            private void writeLog(String log) {
                String time = DATE_FORMAT.format(new Date());
                File file = new File(mCrashDir, "crash_" + time + ".txt");
                try {
                    write(file, log.getBytes("UTF-8"));
                    LogUtils.d(TAG, "崩溃日志已保存至本地文件");
                } catch (Throwable e) {
                    LogUtils.e(TAG, "崩溃日志写入失败");
                }
            }

            /**
             * 获取系统内核版本信息
             */
            private static String getKernel() {
                try {
                    return App.toString(new FileInputStream("/proc/version")).trim();
                } catch (Throwable e) {
                    return e.getMessage();
                }
            }
        }
    }

    //===================== 崩溃展示页面 =====================
    public static final class CrashActivity extends Activity {

        private String mLog;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            LogUtils.i(TAG, "CrashActivity -> 崩溃展示页面已启动");

            setTheme(android.R.style.Theme_DeviceDefault);
            setTitle("App Crash");
            mLog = getIntent().getStringExtra(Intent.EXTRA_TEXT);

            ScrollView contentView = new ScrollView(this);
            contentView.setFillViewport(true);
            HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);

            TextView textView = new TextView(this);
            int padding = dp2px(16);
            textView.setPadding(padding, padding, padding, padding);
            textView.setText(mLog);
            textView.setTextIsSelectable(true);
            textView.setTypeface(Typeface.DEFAULT);
            textView.setLinksClickable(true);

            horizontalScrollView.addView(textView);
            contentView.addView(horizontalScrollView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            setContentView(contentView);
        }

        /**
         * 重启当前应用
         */
        private void restart() {
            Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }

        /**
         * dp转px通用方法
         */
        private static int dp2px(float dpValue) {
            final float scale = Resources.getSystem().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            menu.add(0, android.R.id.copy, 0, android.R.string.copy)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            return super.onCreateOptionsMenu(menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == android.R.id.copy) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText(getPackageName(), mLog));
                LogUtils.d(TAG, "CrashActivity -> 崩溃日志已复制到剪贴板");
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onBackPressed() {
            restart();
        }
    }
}

