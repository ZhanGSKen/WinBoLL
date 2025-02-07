package cc.winboll.studio.libappbase;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/08/12 13:22:12
 * @Describe 异常处理类
 */
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
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import cc.winboll.studio.libappbase.GlobalApplication;
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

public final class CrashHandler {
    public static final String TAG = "CrashHandler";
    
    final static String PREFS = CrashHandler.class.getName() + "PREFS";
    final static String PREFS_CRASHHANDLER_ISCRASHHAPPEN = "PREFS_CRASHHANDLER_ISCRASHHAPPEN";

    public static String _CrashCountFilePath;

    public static final UncaughtExceptionHandler DEFAULT_UNCAUGHT_EXCEPTION_HANDLER = Thread.getDefaultUncaughtExceptionHandler();




    public static void init(Application app) {
        _CrashCountFilePath = app.getExternalFilesDir("CrashHandler") + "/IsCrashHandlerCrashHappen.dat";
        init(app, null);
        LogUtils.d(TAG, "init");
    }

    public static void init(final Application app, final String crashDir) {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(){

                @Override
                public void uncaughtException(Thread thread, Throwable throwable) {
                    try {
                        tryUncaughtException(thread, throwable);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        if (DEFAULT_UNCAUGHT_EXCEPTION_HANDLER != null)
                            DEFAULT_UNCAUGHT_EXCEPTION_HANDLER.uncaughtException(thread, throwable);
                    }
                }

                private void tryUncaughtException(Thread thread, Throwable throwable) {
                    final String time = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss", Locale.getDefault()).format(new Date());
                    File crashFile = new File(TextUtils.isEmpty(crashDir) ? new File(app.getExternalFilesDir(null), "crash")
                                              : new File(crashDir), "crash_" + time + ".txt");

                    String versionName = "unknown";
                    long versionCode = 0;
                    try { 
                        PackageInfo packageInfo = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
                        versionName = packageInfo.versionName;
                        versionCode = Build.VERSION.SDK_INT >= 28 ? packageInfo.getLongVersionCode()
                            : packageInfo.versionCode;
                    } catch (PackageManager.NameNotFoundException ignored) {}

                    String fullStackTrace; {
                        StringWriter sw = new StringWriter(); 
                        PrintWriter pw = new PrintWriter(sw);
                        throwable.printStackTrace(pw);
                        fullStackTrace = sw.toString();
                        pw.close();
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append("************* Crash Head ****************\n");
                    sb.append("Time Of Crash                        : ").append(time).append("\n");
                    sb.append("Device Manufacturer                  : ").append(Build.MANUFACTURER).append("\n");
                    sb.append("Device Model                         : ").append(Build.MODEL).append("\n");
                    sb.append("Android Version                      : ").append(Build.VERSION.RELEASE).append("\n");
                    sb.append("Android SDK                          : ").append(Build.VERSION.SDK_INT).append("\n");
                    sb.append("App VersionName                      : ").append(versionName).append("\n");
                    sb.append("App VersionCode                      : ").append(versionCode).append("\n");
                    sb.append("AppBase GlobalApplication Debug Mode : ").append(GlobalApplication.isDebuging).append("\n");
                    sb.append("CrashHandler CurrentSafeLevel        : ").append(String.format("%d", AppCrashSafetyWire.getInstance().getCurrentSafetyLevel())).append("\n");
                    sb.append("************* Crash Head ****************\n");
                    sb.append("\n").append(fullStackTrace);

                    String errorLog = sb.toString();

                    try {
                        writeFile(crashFile, errorLog);
                    } catch (IOException ignored) {}

                    gotoCrashActiviy: {
                        Intent intent = new Intent();

                        if (AppCrashSafetyWire.getInstance().isAppCrashSafetyWireOK() && AppCrashSafetyWire.getInstance().postCrashSafetyWire(app)) {
                            AppCrashSafetyWire.getInstance().reset();
                            intent.setClass(app, GlobalCrashActiviy.class);
                            intent.putExtra(GlobalCrashActiviy.EXTRA_CRASH_INFO, errorLog);
                            // 如果发生了 CrashHandler 内部崩溃， 就调用基础的应用崩溃显示类
//                            intent.setClass(app, GlobalCrashActiviy.class);
//                            intent.putExtra(GlobalCrashActiviy.EXTRA_CRASH_INFO, errorLog);
                        } else {
                            AppCrashSafetyWire.getInstance().reset();
                            // 正常状态调用进阶的应用崩溃显示页
                            intent.setClass(app, CrashActiviy.class);
                            intent.putExtra(CrashActiviy.EXTRA_CRASH_INFO, errorLog);
                        } 

//                        intent.setClass(app, CrashActiviy.class);
//                        intent.putExtra(CrashActiviy.EXTRA_CRASH_INFO, errorLog);


                        intent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        );

                        try {
                            app.startActivity(intent);
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(0);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                            if (DEFAULT_UNCAUGHT_EXCEPTION_HANDLER != null)
                                DEFAULT_UNCAUGHT_EXCEPTION_HANDLER.uncaughtException(thread, throwable);
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (DEFAULT_UNCAUGHT_EXCEPTION_HANDLER != null)
                                DEFAULT_UNCAUGHT_EXCEPTION_HANDLER.uncaughtException(thread, throwable);
                        }
                    }

                }

                private void writeFile(File file, String content) throws IOException {
                    File parentFile = file.getParentFile();
                    if (parentFile != null && !parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    file.createNewFile();
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(content.getBytes());
                    try {
                        fos.close();
                    } catch (IOException e) {}
                }

            });
    }

    //
    // 应用崩溃保险丝
    //
    public static final class AppCrashSafetyWire {

        volatile static AppCrashSafetyWire _AppCrashSafetyWire;

        volatile int currentSafetyLevel; // 熔断值，为 0 表示熔断了。
        private static final int _MINI = 1;
        private static final int _MAX = 5;

        AppCrashSafetyWire() {
            currentSafetyLevel = loadCurrentSafetyLevel();
        }

        public static synchronized AppCrashSafetyWire getInstance() {
            if (_AppCrashSafetyWire == null) {
                _AppCrashSafetyWire = new AppCrashSafetyWire();
            }
            return _AppCrashSafetyWire;
        }

        public void setCurrentSafetyLevel(int currentSafetyLevel) {
            this.currentSafetyLevel = currentSafetyLevel;
        }

        public int getCurrentSafetyLevel() {
            return currentSafetyLevel;
        }

        public void saveCurrentSafetyLevel(int currentSafetyLevel) {
            this.currentSafetyLevel = currentSafetyLevel;
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(_CrashCountFilePath));
                oos.writeInt(currentSafetyLevel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public int loadCurrentSafetyLevel() {
            try {
                File f = new File(_CrashCountFilePath);
                if (f.exists()) {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(_CrashCountFilePath));
                    currentSafetyLevel = ois.readInt();
                } else {
                    currentSafetyLevel = _MAX;
                    saveCurrentSafetyLevel(currentSafetyLevel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return currentSafetyLevel;
        }

        boolean putOnSafetyWire() {
            // 崩溃计数进入崩溃保险值
            int safeLevel = loadCurrentSafetyLevel();
            if (isSafetyWireOK(safeLevel)) {
                // 如果保险丝未熔断, 就减少一次熔断值
                saveCurrentSafetyLevel(safeLevel - 1);
                return true;
            }


            return false;
        }

        boolean isSafetyWireOK(int safetyLevel) {
            if (safetyLevel >= _MINI && safetyLevel <= _MAX) {
                // 如果在保险值之内
                return true;
            }
            return false;
        }

        void reset() {
            saveCurrentSafetyLevel(_MAX);
        }

        void off() {
            saveCurrentSafetyLevel(_MINI);
        }

        boolean isAppCrashSafetyWireOK() {
            return false;
        }

        // 调用函数以启用持续崩溃保险，从而调用 CrashHandler 内部崩溃处理窗口
        boolean postCrashSafetyWire(final Context context) {
            if (AppCrashSafetyWire.getInstance().putOnSafetyWire()) {
                // 设置内部崩溃处理模块失效
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                        @Override
                        public void run() {
                            // 进程持续运行时重置保险丝
                            //AppCrashSafetyWire.getInstance().reset();
                        }
                    }, 1000);
                return true;
            }
            return false;
        }

    }

    public static final class CrashActiviy extends Activity implements MenuItem.OnMenuItemClickListener {

        private static final String EXTRA_CRASH_INFO = "crashInfo";

        private static final int MENUITEM_COPY = 0;
        private static final int MENUITEM_RESTART = 1;

        private String mLog;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mLog = getIntent().getStringExtra(EXTRA_CRASH_INFO);
            setTheme(android.R.style.Theme_DeviceDefault_Light_DarkActionBar);
            setContentView: {
                ScrollView contentView = new ScrollView(this);
                contentView.setFillViewport(true);

                HorizontalScrollView hw = new HorizontalScrollView(this);
                hw.setBackgroundColor(Color.GRAY);
                TextView message = new TextView(this); {
                    int padding = dp2px(16);
                    message.setPadding(padding, padding, padding, padding);
                    message.setText(mLog);
                    message.setTextIsSelectable(true);
                }
                hw.addView(message);

                contentView.addView(hw, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                setContentView(contentView);
            }
        }

        @Override
        public void onBackPressed() {
            restart();
        }

        private void restart() {
            PackageManager pm = getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(getPackageName());
            if (intent != null) {
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                );
                startActivity(intent);
            }
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }

        private int dp2px(final float dpValue) {
            final float scale = Resources.getSystem().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case MENUITEM_COPY: 
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText(getPackageName(), mLog));
                    Toast.makeText(getApplication(), "The text is copied.", Toast.LENGTH_SHORT).show();
                    break;
                case MENUITEM_RESTART: 
                    restart();
                    break;
            }
            return false;
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            menu.add(0, MENUITEM_COPY, 0, "Copy").setOnMenuItemClickListener(this)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menu.add(0, MENUITEM_RESTART, 0, "Restart").setOnMenuItemClickListener(this)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            return true;
        }
    }

    public static final class GlobalCrashActiviy extends Activity implements MenuItem.OnMenuItemClickListener {

        private static final String EXTRA_CRASH_INFO = "crashInfo";

        private static final int MENUITEM_COPY = 0;
        private static final int MENUITEM_RESTART = 1;

        private String mLog;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mLog = getIntent().getStringExtra(EXTRA_CRASH_INFO);
            setTheme(android.R.style.Theme_DeviceDefault_Light_DarkActionBar);
            setContentView: {
                ScrollView contentView = new ScrollView(this);
                contentView.setFillViewport(true);

                LinearLayout llTitle = new LinearLayout(this);
                TextView title = new TextView(this); {
                    int padding = dp2px(16);
                    title.setPadding(padding, padding, padding, padding);
                    title.setText("GlobalCrashActiviy");
                }
                llTitle.addView(title, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                contentView.addView(llTitle, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                HorizontalScrollView hw = new HorizontalScrollView(this);
                hw.setBackgroundColor(Color.GRAY);
                TextView message = new TextView(this); {
                    int padding = dp2px(16);
                    message.setPadding(padding, padding, padding, padding);
                    message.setText(mLog);
                    message.setTextIsSelectable(true);
                }
                hw.addView(message);

                contentView.addView(hw, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
                setContentView(contentView);
            }
        }

        @Override
        public void onBackPressed() {
            restart();
        }

        private void restart() {
            PackageManager pm = getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(getPackageName());
            if (intent != null) {
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                );
                startActivity(intent);
            }
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }

        private int dp2px(final float dpValue) {
            final float scale = Resources.getSystem().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case MENUITEM_COPY: 
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText(getPackageName(), mLog));
                    Toast.makeText(getApplication(), "The text is copied.", Toast.LENGTH_SHORT).show();
                    break;
                case MENUITEM_RESTART: 
                    restart();
                    break;
            }
            return false;
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            menu.add(0, MENUITEM_COPY, 0, "Copy").setOnMenuItemClickListener(this)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menu.add(0, MENUITEM_RESTART, 0, "Restart").setOnMenuItemClickListener(this)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            return true;
        }
    }


}

