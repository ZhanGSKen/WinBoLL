package cc.winboll.studio.autoinstaller.utils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/05/27 17:56:31
 * @Describe 文件管理类
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class FileUtil {
    
    public static final String TAG = "FileUtil";
    
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
    
}
