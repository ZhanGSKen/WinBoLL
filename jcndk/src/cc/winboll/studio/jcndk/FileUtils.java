package cc.winboll.studio.jcndk;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/01/08 16:31:28
 * @Describe 文件工具类
 */
import android.content.Context;
import android.content.res.AssetManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import cc.winboll.studio.libjc.Log;

public class FileUtils {

    public static final String TAG = "FileUtils";

    public static void copyAssetsToSD(Context context, String szSrcAssets, String szDstSD) {
        Log.d(TAG, "copyAssetsToSD [" + szSrcAssets + "] to [" + szDstSD + "]");
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = assetManager.open(szSrcAssets);
            File outputFile = new File(szDstSD);
            outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            Log.d(TAG, "copyAssetsToSD done.");
        } catch (IOException e) {
            Log.e(TAG, e, Thread.currentThread().getStackTrace());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, e, Thread.currentThread().getStackTrace());
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, e, Thread.currentThread().getStackTrace());
                }
            }
        }
    }
}
