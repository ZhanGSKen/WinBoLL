package cc.winboll.studio.libappbase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/11 20:45
 * @Describe UTF-8 编码文件操作工具类
 * 提供字符串与文件的相互转换，强制使用 UTF-8 编码，确保跨平台字符兼容性
 */
public class UTF8FileUtils {

    /** 工具类日志 TAG（用于调试输出） */
    public static final String TAG = "UTF8FileUtils";

    /**
     * 将字符串写入文件（强制 UTF-8 编码）
     * 若文件父目录不存在，自动创建；覆盖原有文件内容
     * @param filePath 文件路径（包含文件名，如 "/sdcard/test.txt"）
     * @param content 要写入的字符串内容
     * @throws IOException 写入失败时抛出（如权限不足、路径无效等）
     */
    public static void writeStringToFile(String filePath, String content) throws IOException {
        // 根据路径创建文件对象
        File file = new File(filePath);
        // 获取父目录，若不存在则递归创建
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // 初始化文件输出流（覆盖模式）
        FileOutputStream outputStream = new FileOutputStream(file);
        // 包装为 UTF-8 编码的字符输出流
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

        try {
            // 写入字符串内容
            writer.write(content);
        } finally {
            // 强制关闭流，避免资源泄漏（即使写入失败也确保流关闭）
            writer.close();
        }
    }

    /**
     * 从文件读取字符串（强制 UTF-8 编码）
     * 逐字符读取文件内容，拼接为完整字符串返回
     * @param filePath 文件路径（包含文件名，如 "/sdcard/test.txt"）
     * @return 文件内容字符串（空文件返回空字符串）
     * @throws IOException 读取失败时抛出（如文件不存在、权限不足等）
     */
    public static String readStringFromFile(String filePath) throws IOException {
        // 根据路径创建文件对象
        File file = new File(filePath);
        // 初始化文件输入流
        FileInputStream inputStream = new FileInputStream(file);
        // 包装为 UTF-8 编码的字符输入流
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

        // 字符串构建器，用于拼接读取的字符
        StringBuilder content = new StringBuilder();
        int charCode; // 存储单个字符的 ASCII 码

        try {
            // 逐字符读取（-1 表示读取到文件末尾）
            while ((charCode = reader.read()) != -1) {
                // 将 ASCII 码转换为字符，追加到字符串
                content.append((char) charCode);
            }
        } finally {
            // 强制关闭流，避免资源泄漏
            reader.close();
        }

        // 返回读取的完整字符串
        return content.toString();
    }
}

