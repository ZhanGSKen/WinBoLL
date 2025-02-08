package cc.winboll.studio.libappbase;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/05 10:10:23
 * @Describe 全局应用类
 */
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GlobalApplication extends Application {

    public static final String TAG = "GlobalApplication";

    final static String PREFS = GlobalApplication.class.getName() + "PREFS";
    final static String PREFS_ISDEBUGING = "PREFS_ISDEBUGING";


    private static Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    // 是否处于调试状态
    volatile static boolean isDebuging = false;

    public static void setIsDebuging(Context context, boolean isDebuging) {
        GlobalApplication.isDebuging = isDebuging;
        // 获取SharedPreferences实例
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        // 获取编辑器
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // 保存数据
        editor.putBoolean(PREFS_ISDEBUGING, GlobalApplication.isDebuging);
        // 提交更改
        editor.apply();
    }

    public static boolean isDebuging() {
        return isDebuging;
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    public Application getApplication() {
        return this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        GlobalApplication.isDebuging = true;
        GlobalApplication.setIsDebuging(this, true);
        LogUtils.init(this);
        LogUtils.setLogLevel(LogUtils.LOG_LEVEL.Debug);
        //LogUtils.setTAGListEnable(GlobalApplication.TAG, true);
        LogUtils.setALlTAGListEnable(true);
        LogUtils.d(TAG, "LogUtils init");

        // 设置应用异常处理窗口
        CrashHandler.init(this);

        // 设置应用调试状态
        //SharedPreferences sharedPreferences = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        //GlobalApplication.isDebuging = sharedPreferences.getBoolean(PREFS_ISDEBUGING, GlobalApplication.isDebuging);

    }

    public static void write(InputStream input, OutputStream output) throws IOException {
        byte[] buf = new byte[1024 * 8];
        int len;
        while ((len = input.read(buf)) != -1) {
            output.write(buf, 0, len);
        }
    }

    public static void write(File file, byte[] data) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        ByteArrayInputStream input = new ByteArrayInputStream(data);
        FileOutputStream output = new FileOutputStream(file);
        try {
            write(input, output);
        } finally {
            closeIO(input, output);
        }
    }

    public static String toString(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(input, output);
        try {
            return output.toString("UTF-8");
        } finally {
            closeIO(input, output);
        }
    }

    public static void closeIO(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            try {
                if (closeable != null) closeable.close();
            } catch (IOException ignored) {}
        }
    }
}
