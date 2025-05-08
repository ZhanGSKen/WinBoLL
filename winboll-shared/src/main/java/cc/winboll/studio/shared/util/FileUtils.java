package cc.winboll.studio.shared.util;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/19 14:30:57
 * @Describe 文件工具类
 */
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import androidx.core.content.FileProvider;
import cc.winboll.studio.shared.log.LogUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class FileUtils {

    public static final String TAG = "FileUtil";

    public static void shareJSONFile(Context context, String szConfigFile) {
        Uri uri;
        File file = new File(szConfigFile);
        uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        /*if (Build.VERSION.SDK_INT >= 24) {//android 7.0以上
         uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
         } else {
         uri = Uri.fromFile(file);
         }*/
        Intent shareIntent = new Intent();    
        shareIntent.setAction(Intent.ACTION_SEND);    
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);    
        shareIntent.setType("application/json");

        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        /*if (Build.VERSION.SDK_INT >= 24) {
         shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
         }*/

        // 设置分享的标题    
        context.startActivity(Intent.createChooser(shareIntent, "SHARE JSON"));  
    }
    
    public static void shareHtmlFile(Context context, String szHtmlFile) {
        File htmlFile = new File(szHtmlFile);
        Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", htmlFile);
        
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(contentUri, "text/html");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }

    //
    // 把字符串写入文件，指定 UTF-8 编码
    //
    public static void writeFile(String filePath, String content) throws IOException {
        File file = new File(filePath);
        FileOutputStream outputStream = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        writer.write(content);
        writer.close();
    }

    //
    // 读取文件到字符串，指定 UTF-8 编码
    //
    public static String readFile(String filePath) throws IOException {
        File file = new File(filePath);
        FileInputStream inputStream = new FileInputStream(file);
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        StringBuilder content = new StringBuilder();
        int character;
        while ((character = reader.read()) != -1) {
            content.append((char) character);
        }
        reader.close();
        return content.toString();
    }

    public static void copyAssetsToSD(Context context, String szSrcAssets, String szDstSD) {
        LogUtils.d(TAG, "copyAssetsToSD [" + szSrcAssets + "] to [" + szDstSD + "]");
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
            LogUtils.d(TAG, "copyAssetsToSD done.");
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
                }
            }
        }
    }
}
