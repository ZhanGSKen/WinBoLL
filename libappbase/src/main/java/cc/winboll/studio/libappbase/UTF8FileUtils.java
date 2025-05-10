package cc.winboll.studio.libappbase;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/07/19 14:30:57
 * @Describe UTF-8编码文件工具类
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class UTF8FileUtils {

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
}
