package cc.winboll.studio.libappbase;

import android.app.Activity;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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

import cc.winboll.studio.libappbase.utils.CrashHandleNotifyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 应用全局崩溃处理类（单例逻辑）
 * 核心功能：捕获应用未捕获异常，记录崩溃日志到文件，启动崩溃报告页面，
 * 并通过「崩溃保险丝」机制防止重复崩溃，保障基础功能可用
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @CreateTime 2025/11/11 20:14:00
 * @EditTime 2026/05/11 15:36:45
 */
public final class CrashHandler {

    // ====================== 常量定义 ======================
    /** 日志标签 */
    public static final String TAG = "CrashHandler";
    /** 崩溃报告页面标题 */
    public static final String TITTLE = "CrashReport";
    /** Intent 传递崩溃信息键 */
    public static final String EXTRA_CRASH_LOG = "crashInfo";
    /** SharedPreferences 存储键 */
    static final String PREFS = CrashHandler.class.getName() + "PREFS";
    /** 标记是否发生崩溃键 */
    static final String PREFS_CRASHHANDLER_ISCRASHHAPPEN = "PREFS_CRASHHANDLER_ISCRASHHAPPEN";

    // ====================== 成员变量 ======================
    /** 崩溃保险丝状态文件路径 */
    public static String _CrashCountFilePath;
    /** 系统默认异常处理器兜底 */
    public static final UncaughtExceptionHandler DEFAULT_UNCAUGHT_EXCEPTION_HANDLER
	= Thread.getDefaultUncaughtExceptionHandler();

    // ====================== 对外初始化方法 ======================
    /**
     * 初始化崩溃处理器（默认存储路径）
     * @param app 全局Application实例
     */
    public static void init(final Application app) {
        _CrashCountFilePath = app.getExternalFilesDir("CrashHandler") + "/IsCrashHandlerCrashHappen.dat";
        LogUtils.d(TAG, "init _CrashCountFilePath = " + _CrashCountFilePath);
        init(app, null);
    }

    /**
     * 初始化崩溃处理器（自定义日志目录）
     * @param app 全局Application实例
     * @param crashDir 自定义崩溃日志目录，传null使用默认
     */
    public static void init(final Application app, final String crashDir) {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(final Thread thread, final Throwable throwable) {
					try {
						tryUncaughtException(thread, throwable, crashDir, app);
					} catch (Throwable e) {
						LogUtils.e(TAG, "uncaughtException error", e);
						if (DEFAULT_UNCAUGHT_EXCEPTION_HANDLER != null) {
							DEFAULT_UNCAUGHT_EXCEPTION_HANDLER.uncaughtException(thread, throwable);
						}
					}
				}
			});
    }

    // ====================== 内部崩溃处理核心 ======================
    /**
     * 执行崩溃信息收集、日志写入、跳转崩溃页面
     */
    private static void tryUncaughtException(final Thread thread,
											 final Throwable throwable,
											 final String crashDir,
											 final Application app) {
        // 触发崩溃保险丝
        AppCrashSafetyWire.getInstance().burnSafetyWire();

        // 格式化时间
        final String time = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss",
												 Locale.getDefault()).format(new Date());

        // 创建日志文件
        File logParent = TextUtils.isEmpty(crashDir)
			? new File(app.getExternalFilesDir(null), "crash")
			: new File(crashDir);
        final File crashFile = new File(logParent, "crash_" + time + ".txt");

        // 获取应用版本信息
        String versionName = "unknown";
        long versionCode = 0;
        try {
            final PackageInfo packageInfo = app.getPackageManager()
				.getPackageInfo(app.getPackageName(), 0);
            versionName = packageInfo.versionName;
            if (Build.VERSION.SDK_INT >= 28) {
                versionCode = packageInfo.getLongVersionCode();
            } else {
                versionCode = packageInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(TAG, "get package info fail");
        }

        // 抓取异常堆栈
        String fullStackTrace;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        fullStackTrace = sw.toString();
        pw.close();

        // 拼接崩溃头部信息
        StringBuilder sb = new StringBuilder();
        sb.append("************* Crash Head ****************\n");
        sb.append("Time Of Crash                        : ").append(time).append("\n");
        sb.append("Device Manufacturer                  : ").append(Build.MANUFACTURER).append("\n");
        sb.append("Device Model                         : ").append(Build.MODEL).append("\n");
        sb.append("Android Version                      : ").append(Build.VERSION.RELEASE).append("\n");
        sb.append("Android SDK                          : ").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("App VersionName                      : ").append(versionName).append("\n");
        sb.append("App VersionCode                      : ").append(versionCode).append("\n");
        sb.append("************* Crash Head ****************\n");
        sb.append("\n").append(fullStackTrace);

        final String errorLog = sb.toString();

        // 写入日志文件
        try {
            writeFile(crashFile, errorLog);
        } catch (IOException e) {
            LogUtils.e(TAG, "write crash log file fail");
        }

        // 跳转崩溃页面
        gotoCrashActivity(errorLog, app);
    }

    /**
     * 写入文本到文件
     */
    private static void writeFile(final File file, final String content) throws IOException {
        final File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content.getBytes());
        fos.close();
    }

    /**
     * 根据保险丝状态跳转对应崩溃页面
     */
    private static void gotoCrashActivity(final String errorLog, final Application app) {
        final Intent intent = new Intent();
        if (AppCrashSafetyWire.getInstance().isAppCrashSafetyWireOK()) {
            intent.setClass(app, GlobalCrashActivity.class);
        } else {
            intent.setClass(app, CrashActivity.class);
        }
        intent.putExtra(EXTRA_CRASH_LOG, errorLog);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_CLEAR_TASK);

        try {
            if (GlobalApplication.isDebugging()) {
                app.startActivity(intent);
            } else {
                CrashHandleNotifyUtils.handleUncaughtException(app, intent, GlobalCrashActivity.class);
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } catch (ActivityNotFoundException e) {
            LogUtils.e(TAG, "CrashActivity not found");
            if (DEFAULT_UNCAUGHT_EXCEPTION_HANDLER != null) {
                DEFAULT_UNCAUGHT_EXCEPTION_HANDLER.uncaughtException(Thread.currentThread(), e);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "start CrashActivity error");
            if (DEFAULT_UNCAUGHT_EXCEPTION_HANDLER != null) {
                DEFAULT_UNCAUGHT_EXCEPTION_HANDLER.uncaughtException(Thread.currentThread(), e);
            }
        }
    }

    // ====================== 内部Activity页面 ======================
    /**
     * 基础极简崩溃页面
     * 保险丝熔断时启动，避免复杂布局二次崩溃
     */
    public static final class CrashActivity extends Activity implements MenuItem.OnMenuItemClickListener {
        private static final int MENUITEM_COPY = 0;
        private static final int MENUITEM_RESTART = 1;

        private String mLog;

        @Override
        protected void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            AppCrashSafetyWire.getInstance().postResumeCrashSafetyWireHandler(getApplicationContext());
            mLog = getIntent().getStringExtra(EXTRA_CRASH_LOG);
            setTheme(android.R.style.Theme_DeviceDefault_Light_DarkActionBar);
            initLayout();
        }

        /**
         * 动态初始化布局
         */
        private void initLayout() {
            ScrollView contentView = new ScrollView(this);
            contentView.setFillViewport(true);

            HorizontalScrollView hw = new HorizontalScrollView(this);
            hw.setBackgroundColor(0xFFF5F5F5);

            TextView message = new TextView(this);
            final int padding = dp2px(16);
            message.setPadding(padding, padding, padding, padding);
            message.setText(mLog);
            message.setTextColor(0xFF000000);
            message.setTextIsSelectable(true);

            hw.addView(message);
            contentView.addView(hw, ViewGroup.LayoutParams.MATCH_PARENT,
								ViewGroup.LayoutParams.MATCH_PARENT);
            setContentView(contentView);

            getActionBar().setTitle(TITTLE);
            getActionBar().setSubtitle(GlobalApplication.class.getSimpleName() + " Error");
        }

        @Override
        public void onBackPressed() {
            restartApp();
        }

        /**
         * 重启应用
         */
        private void restartApp() {
            final Intent intent = getPackageManager()
				.getLaunchIntentForPackage(getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
								| Intent.FLAG_ACTIVITY_CLEAR_TOP
								| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }

        /**
         * dp转px
         */
        private int dp2px(final float dpValue) {
            final float scale = Resources.getSystem().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        }

        @Override
        public boolean onCreateOptionsMenu(final Menu menu) {
            menu.add(0, MENUITEM_COPY, 0, "Copy")
				.setOnMenuItemClickListener(this)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            menu.add(0, MENUITEM_RESTART, 0, "Restart")
				.setOnMenuItemClickListener(this)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            return true;
        }

        @Override
        public boolean onMenuItemClick(final MenuItem item) {
            switch (item.getItemId()) {
                case MENUITEM_COPY:
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText(getPackageName(), mLog));
                    Toast.makeText(getApplication(), "The text is copied.", Toast.LENGTH_SHORT).show();
                    break;
                case MENUITEM_RESTART:
                    AppCrashSafetyWire.getInstance().resumeToMaximumImmediately();
                    restartApp();
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}

