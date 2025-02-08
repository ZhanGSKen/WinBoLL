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
        LogUtils.d(TAG, String.format("_CrashCountFilePath %s", _CrashCountFilePath));
        init(app, null);
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
                    // 每到这里就燃烧一次保险丝
                    AppCrashSafetyWire.getInstance().burnSafetyWire();
                    
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
                    sb.append("************* Crash Head ****************\n");
                    sb.append("\n").append(fullStackTrace);

                    String errorLog = sb.toString();

                    try {
                        writeFile(crashFile, errorLog);
                    } catch (IOException ignored) {}

                    gotoCrashActiviy: {
                        Intent intent = new Intent();
                        LogUtils.d(TAG, "gotoCrashActiviy: ");
                        if (AppCrashSafetyWire.getInstance().isAppCrashSafetyWireOK()) {
                            LogUtils.d(TAG, "gotoCrashActiviy: isAppCrashSafetyWireOK");
                            //AppCrashSafetyWire.getInstance().postCrashSafetyWire(app);
                            intent.setClass(app, GlobalCrashActiviy.class);
                            intent.putExtra(GlobalCrashActiviy.EXTRA_CRASH_INFO, errorLog);
                            // 如果发生了 CrashHandler 内部崩溃， 就调用基础的应用崩溃显示类
//                            intent.setClass(app, GlobalCrashActiviy.class);
//                            intent.putExtra(GlobalCrashActiviy.EXTRA_CRASH_INFO, errorLog);
                        } else {
                            LogUtils.d(TAG, "gotoCrashActiviy: else");
                            AppCrashSafetyWire.getInstance().resumeToMaximumImmediately();
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

        volatile Integer currentSafetyLevel; // 熔断值，为 0 表示熔断了。
        private static final int _MINI = 1;
        private static final int _MAX = 2;

        AppCrashSafetyWire() {
            LogUtils.d(TAG, "AppCrashSafetyWire()");
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
            LogUtils.d(TAG, "saveCurrentSafetyLevel()");
            this.currentSafetyLevel = currentSafetyLevel;
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(_CrashCountFilePath));
                oos.writeInt(currentSafetyLevel);
                oos.flush();
                oos.close();
                LogUtils.d(TAG, String.format("saveCurrentSafetyLevel writeInt currentSafetyLevel %d", currentSafetyLevel));
            } catch (IOException e) {
                LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
            }
        }

        public int loadCurrentSafetyLevel() {
            LogUtils.d(TAG, "loadCurrentSafetyLevel()");
            try {
                File f = new File(_CrashCountFilePath);
                if (f.exists()) {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(_CrashCountFilePath));
                    currentSafetyLevel = ois.readInt();
                    LogUtils.d(TAG, String.format("loadCurrentSafetyLevel() readInt currentSafetyLevel %d", currentSafetyLevel));
                } else {
                    currentSafetyLevel = _MAX;
                    LogUtils.d(TAG, String.format("loadCurrentSafetyLevel() currentSafetyLevel init to _MAX->%d", _MAX));
                    saveCurrentSafetyLevel(currentSafetyLevel);
                }
            } catch (IOException e) {
                LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
            }
            return currentSafetyLevel;
        }

        boolean burnSafetyWire() {
            LogUtils.d(TAG, "burnSafetyWire()");
            // 崩溃计数进入崩溃保险值
            int safeLevel = loadCurrentSafetyLevel();
            if (isSafetyWireWorking(safeLevel)) {
                // 如果保险丝未熔断, 就增加一次熔断值
                LogUtils.d(TAG, "burnSafetyWire() use");
                saveCurrentSafetyLevel(safeLevel - 1);
                return isSafetyWireWorking(safeLevel - 1);
            }
            return false;
        }

        boolean resumeSafetyLevel() {
            LogUtils.d(TAG, "resumeSafetyLevel()");
            // 崩溃计数进入崩溃保险值
            int safeLevel = loadCurrentSafetyLevel();
            if (isSafetyWireWorking(safeLevel)) {
                // 如果保险丝未熔断, 就增加一次熔断值
                LogUtils.d(TAG, "resumeSafetyLevel() resume 1");
                saveCurrentSafetyLevel(safeLevel + 1);
                return isSafetyWireWorking(safeLevel + 1);
            } else {
                LogUtils.d(TAG, "resumeSafetyLevel() resume immediately");
                resumeToMaximumImmediately();
            }
            return false;
        }

        boolean isSafetyWireWorking(int safetyLevel) {
            LogUtils.d(TAG, "isSafetyWireOK()");
            //safetyLevel = _MINI;
            //safetyLevel = _MINI - 1;
            //safetyLevel = _MINI + 1;
            //safetyLevel = _MAX;
            //safetyLevel = _MAX + 1;
            LogUtils.d(TAG, String.format("SafetyLevel %d", safetyLevel));

            if (safetyLevel >= _MINI && safetyLevel <= _MAX) {
                // 如果在保险值之内
                LogUtils.d(TAG, String.format("In Safety Level"));
                return true;
            }
            LogUtils.d(TAG, String.format("Out of Safety Level"));
            return false;
        }

        void resumeToMaximumImmediately() {
            LogUtils.d(TAG, "resumeToMaximumImmediately() call saveCurrentSafetyLevel(_MAX)");
            AppCrashSafetyWire.getInstance().saveCurrentSafetyLevel(_MAX);
        }

//        boolean resumeToMaximum(int safetyLevel) {
//            if (safetyLevel + 1 < _MAX
//                && safetyLevel >= _MINI
//                && isSafetyWireWorking(safetyLevel + 1)) {
//                AppCrashSafetyWire.getInstance().saveCurrentSafetyLevel(currentSafetyLevel + 1);
//                return true;
//            }
//            return false;
//        }

        void off() {
            LogUtils.d(TAG, "off()");
            saveCurrentSafetyLevel(_MINI);
        }

        boolean isAppCrashSafetyWireOK() {
            LogUtils.d(TAG, "isAppCrashSafetyWireOK()");
            currentSafetyLevel = loadCurrentSafetyLevel();
            return isSafetyWireWorking(currentSafetyLevel);
        }

        // 调用函数以启用持续崩溃保险，从而调用 CrashHandler 内部崩溃处理窗口
//        boolean postCrashSafetyWire(final Context context) {
//            LogUtils.d(TAG, "postCrashSafetyWire()");
//            if (AppCrashSafetyWire.getInstance().isAppCrashSafetyWireOK()) {
//                // 保险丝在工作连接状态
//                // 设置内部崩溃处理模块失效
//                new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
//                        @Override
//                        public void run() {
//                            // 进程持续运行时，恢复保险丝熔断值
//                            //Resume to maximum
//                            LogUtils.d(TAG, "postCrashSafetyWire Resume to maximum");
//                            while (resumeToMaximum(currentSafetyLevel + 1)) {
//                                try {
//                                    Thread.sleep(1000);
//                                } catch (InterruptedException e) {
//                                    LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
//                                }
//                            }
//                        }
//                    }, 10000);
//                return true;
//            }
//            return false;
//        }

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

