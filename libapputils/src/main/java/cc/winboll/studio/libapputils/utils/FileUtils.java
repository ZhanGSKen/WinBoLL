package cc.winboll.studio.libapputils.utils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/19 14:30:57
 * @Describe 文件工具类
 */
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import cc.winboll.studio.libappbase.LogUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import android.support.v4.content.FileProvider;

public class FileUtils {

    public static final String TAG = "FileUtil";

    public static void shareJSONFile(Context context, String szShareFilePath) {
        Uri uri;
        File file = new File(szShareFilePath);
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

    //
    // 把字符串写入文件，指定 UTF-8 编码
    //
    public static void writeStringToFile(String szFilePath, String szContent) throws IOException {
        File file = new File(szFilePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        FileOutputStream outputStream = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        writer.write(szContent);
        writer.close();
    }

    //
    // 读取文件到字符串，指定 UTF-8 编码
    //
    public static String readStringFromFile(String szFilePath) throws IOException {
        File file = new File(szFilePath);
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

    public static boolean copyFile(File srcFile, File dstFile) {
        if (!srcFile.exists()) {
            LogUtils.d(TAG, "The original file does not exist.");
        } else {
            try {
                // 源文件路径
                Path sourcePath = Paths.get(srcFile.getPath());
                // 目标文件路径
                Path destPath = Paths.get(dstFile.getPath());
                // 建立目标父级文件夹
                if (!dstFile.getParentFile().exists()) {
                    dstFile.getParentFile().mkdirs();
                }
                // 删除旧的目标文件
                if (dstFile.exists()) {
                    dstFile.delete();
                }
                // 拷贝文件
                Files.copy(sourcePath, destPath);
                LogUtils.d(TAG, "File copy successfully.");
                return true;
            } catch (Exception e) {
                LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
            }
        }
        return false;
    }
}
