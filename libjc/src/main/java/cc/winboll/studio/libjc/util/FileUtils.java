package cc.winboll.studio.libjc.util;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/08/12 13:46:32
 * @Describe 文件处理工具
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    public static final String TAG = "FileUtils";

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
                System.err.println(String.format("%s Exception : %s", TAG, e.getMessage()));
                LogUtils.e(TAG, e, Thread.currentThread().getStackTrace());
            }
        }
        return false;
    }
}
