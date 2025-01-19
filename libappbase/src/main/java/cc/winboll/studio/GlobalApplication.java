package cc.winboll.studio;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/05 10:10:23
 * @Describe 全局应用类
 */
import android.app.Activity;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
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

public class GlobalApplication extends Application {

    private static Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.init(this);
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
