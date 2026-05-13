package cc.winboll.studio.powerbell.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import cc.winboll.studio.libappbase.LogUtils;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.UUID;

/**
 * 文件操作工具类
 * 功能：文件读写、复制、图片转换、文件名处理等常用文件操作
 * 适配：Java 7 + Android API 30
 * 注意：调用文件操作前需确保已获取存储权限（Android 6.0+ 需动态申请）
 */
public class FileUtils {
    // ================================== 静态常量区（置顶归类，消除魔法值）=================================
    public static final String TAG = "FileUtils";
    /** 读取文件默认缓冲区大小（10KB） */
    private static final int BUFFER_SIZE = 10240;
    /** 最大读取文件大小（1GB），防止OOM */
    private static final long MAX_READ_FILE_SIZE = 1024 * 1024 * 1024;
    /** 最大文件后缀长度（避免异常文件名） */
    private static final int MAX_SUFFIX_LENGTH = 5;
    /** 缓冲区大小（流复制专用） */
    private static final int STREAM_BUFFER_SIZE = 1024;

    // ================================== 文件读取相关（字符串 + 字节数组）=================================
    /**
     * 读取文件内容并转为字符串
     * @param filePath 文件绝对路径（非空）
     * @return 文件内容字符串
     * @throws IOException 异常：文件不存在、文件过大、读取失败等
     */
    public static String readFileAsString(String filePath) throws IOException {
        LogUtils.d(TAG, "【readFileAsString】调用开始 | 文件路径=" + filePath);
        // 1. 校验文件合法性
        File file = new File(filePath);
        if (!file.exists()) {
            LogUtils.e(TAG, "【readFileAsString】文件不存在：" + filePath);
            throw new FileNotFoundException("文件不存在：" + filePath);
        }
        if (file.length() > MAX_READ_FILE_SIZE) {
            LogUtils.e(TAG, "【readFileAsString】文件过大（超过1GB）：" + filePath);
            throw new IOException("文件过大（超过1GB），禁止读取：" + filePath);
        }

        // 2. 读取文件内容（使用StringBuilder高效拼接）
        StringBuilder sb = new StringBuilder((int) file.length());
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] buffer = new byte[BUFFER_SIZE];
            int readLen;
            while ((readLen = fis.read(buffer)) > 0) {
                sb.append(new String(buffer, 0, readLen));
            }
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        LogUtils.d(TAG, "【readFileAsString】读取成功 | 文件大小=" + file.length() + "字节");
        return sb.toString();
    }

    /**
     * 读取文件内容并转为byte数组（适用于二进制文件：图片、音频等）
     * @param filePath 文件绝对路径（非空）
     * @return 文件内容byte数组
     * @throws IOException 异常：文件不存在、读取失败等
     */
    public static byte[] readFileByBytes(String filePath) throws IOException {
        LogUtils.d(TAG, "【readFileByBytes】调用开始 | 文件路径=" + filePath);
        // 1. 校验文件合法性
        File file = new File(filePath);
        if (!file.exists()) {
            LogUtils.e(TAG, "【readFileByBytes】文件不存在：" + filePath);
            throw new FileNotFoundException("文件不存在：" + filePath);
        }

        // 2. 缓冲流读取（高效，减少IO次数）
        ByteArrayOutputStream bos = null;
        BufferedInputStream bis = null;
        try {
            bos = new ByteArrayOutputStream((int) file.length());
            bis = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[BUFFER_SIZE];
            int readLen;
            while ((readLen = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, readLen);
            }
            bos.flush();
            LogUtils.d(TAG, "【readFileByBytes】读取成功 | 文件大小=" + file.length() + "字节");
            return bos.toByteArray();
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
    }

    // ================================== 文件复制相关（FileChannel + 简化版 + 流复制）=================================
    /**
     * 基于FileChannel复制文件（高效，适用于大文件复制）
     * @param source 源文件（非空，必须存在）
     * @param dest 目标文件（非空，父目录会自动创建）
     * @throws IOException 异常：源文件不存在、复制失败等
     */
    public static void copyFileUsingFileChannels(File source, File dest) throws IOException {
        LogUtils.d(TAG, "【copyFileUsingFileChannels】调用开始 | 源文件=" + source.getAbsolutePath() + " | 目标文件=" + dest.getAbsolutePath());
        // 1. 校验源文件合法性
        if (!source.exists() || !source.isFile()) {
            LogUtils.e(TAG, "【copyFileUsingFileChannels】源文件无效：" + source.getAbsolutePath());
            throw new FileNotFoundException("源文件不存在或不是文件：" + source.getAbsolutePath());
        }

        // 2. 创建目标文件父目录
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
            LogUtils.d(TAG, "【copyFileUsingFileChannels】创建父目录：" + dest.getParentFile().getAbsolutePath());
        }

        // 3. 通道复制（手动关闭流，兼容Java 7）
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            LogUtils.d(TAG, "【copyFileUsingFileChannels】复制成功");
        } finally {
            if (inputChannel != null) {
                inputChannel.close();
            }
            if (outputChannel != null) {
                outputChannel.close();
            }
        }
    }

    /**
     * 简化版文件复制（基于传统IO，兼容全版本，适用于中小文件）
     * @param oldFile 源文件（非空，必须存在）
     * @param newFile 目标文件（非空，父目录会自动创建）
     * @return 复制结果：true-成功，false-失败
     */
    public static boolean copyFile(File oldFile, File newFile) {
        LogUtils.d(TAG, "【copyFile】调用开始 | 源文件=" + (oldFile != null ? oldFile.getAbsolutePath() : "null") + " | 目标文件=" + (newFile != null ? newFile.getAbsolutePath() : "null"));
        // 1. 校验源文件合法性
        if (oldFile == null || !oldFile.exists() || !oldFile.isFile()) {
            LogUtils.e(TAG, "【copyFile】源文件无效");
            return false;
        }

        // 2. 创建目标文件父目录
        if (!newFile.getParentFile().exists()) {
            newFile.getParentFile().mkdirs();
            LogUtils.d(TAG, "【copyFile】创建父目录：" + newFile.getParentFile().getAbsolutePath());
        }

        // 3. 复制文件（覆盖已有目标文件）
        if (newFile.exists()) {
            newFile.delete();
            LogUtils.d(TAG, "【copyFile】删除已有目标文件：" + newFile.getAbsolutePath());
        }

        try {
            copyFileUsingFileChannels(oldFile, newFile);
            return true;
        } catch (Exception e) {
            LogUtils.e(TAG, "【copyFile】复制失败：" + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 复制输入流到文件（兼容Uri解析失败场景）
     * @param inputStream 输入流（非空）
     * @param file 目标文件（非空）
     * @throws IOException 异常：流关闭失败、目录创建失败等
     */
    public static void copyStreamToFile(InputStream inputStream, File file) throws IOException {
        LogUtils.d(TAG, "【copyStreamToFile】调用开始 | 目标文件=" + file.getAbsolutePath());
        // 1. 校验参数合法性
        if (inputStream == null || file == null) {
            LogUtils.e(TAG, "【copyStreamToFile】参数为空：InputStream=" + (inputStream == null) + " | File=" + (file == null));
            throw new IllegalArgumentException("InputStream或File不能为空");
        }

        // 2. 创建目标文件父目录
        File parentDir = file.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            LogUtils.e(TAG, "【copyStreamToFile】无法创建父目录：" + parentDir.getAbsolutePath());
            throw new IOException("无法创建父目录：" + parentDir.getAbsolutePath());
        }

        // 3. 流复制（手动关闭流，兼容Java 7）
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[STREAM_BUFFER_SIZE];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            LogUtils.d(TAG, "【copyStreamToFile】复制成功");
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                LogUtils.e(TAG, "【copyStreamToFile】关闭输入流失败：" + e.getMessage());
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    // ================================== 图片文件相关（BitmapDrawable 获取）=================================
    /**
     * 从文件路径获取BitmapDrawable（适用于Android图片显示）
     * @param path 图片文件绝对路径（非空）
     * @return BitmapDrawable 图片对象（文件不存在/读取失败返回null）
     * @throws IOException 异常：文件读取IO错误
     */
    public static BitmapDrawable getImageDrawable(String path) throws IOException {
        LogUtils.d(TAG, "【getImageDrawable】调用开始 | 图片路径=" + path);
        // 1. 校验文件合法性
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            LogUtils.e(TAG, "【getImageDrawable】图片文件无效：" + path);
            return null;
        }

        // 2. 读取文件并转为BitmapDrawable（缓冲流读取，减少内存占用）
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        try {
            is = new FileInputStream(file);
            bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int readLen;
            while ((readLen = is.read(buffer)) != -1) {
                bos.write(buffer, 0, readLen);
            }
            byte[] imageBytes = bos.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            LogUtils.d(TAG, "【getImageDrawable】转换成功 | 图片尺寸=" + bitmap.getWidth() + "x" + bitmap.getHeight());
            return new BitmapDrawable(bitmap);
        } finally {
            if (is != null) {
                is.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
    }

    // ================================== 文件名处理相关（后缀截取 + 唯一文件名）=================================
    /**
     * 截取文件后缀名（兼容多 "." 场景，如"image.2025.png" → ".png"）
     * @param file 目标文件（可为null）
     * @return 文件后缀名：带点（如".jpg"），无后缀/文件无效返回空字符串
     */
    public static String getFileSuffixWithMultiDot(File file) {
        LogUtils.d(TAG, "【getFileSuffixWithMultiDot】调用开始 | 文件=" + (file != null ? file.getAbsolutePath() : "null"));
        // 1. 校验文件合法性
        if (file == null || !file.isFile()) {
            LogUtils.d(TAG, "【getFileSuffixWithMultiDot】文件无效，返回空后缀");
            return "";
        }

        // 2. 提取文件名并查找最后一个 "."
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf(".");

        // 3. 校验后缀合法性（排除无后缀、以点结尾、后缀过长的异常文件）
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1 || (fileName.length() - lastDotIndex) > MAX_SUFFIX_LENGTH) {
            LogUtils.d(TAG, "【getFileSuffixWithMultiDot】无有效后缀 | 文件名=" + fileName);
            return "";
        }

        // 4. 返回小写后缀（统一格式，避免大小写不一致问题）
        String suffix = fileName.substring(lastDotIndex).toLowerCase();
        LogUtils.d(TAG, "【getFileSuffixWithMultiDot】获取成功 | 后缀=" + suffix);
        return suffix;
    }

    /**
     * 获取文件后缀（不带点，忽略大小写，适配空文件名/无后缀场景）
     * @param file 目标文件
     * @return 后缀字符串（无后缀返回空字符串，非空统一小写）
     */
    public static String getFileSuffix(File file) {
        LogUtils.d(TAG, "【getFileSuffix】调用开始 | 文件=" + (file != null ? file.getAbsolutePath() : "null"));
        if (file == null || file.getName().isEmpty()) {
            LogUtils.d(TAG, "【getFileSuffix】文件无效，返回空后缀");
            return "";
        }
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf(".");
        // 无后缀（没有点，或点在开头/结尾）
        if (lastDotIndex == -1 || lastDotIndex == 0 || lastDotIndex == fileName.length() - 1) {
            LogUtils.d(TAG, "【getFileSuffix】无有效后缀 | 文件名=" + fileName);
            return "";
        }
        // 截取后缀并转小写（统一格式，避免 PNG/png 差异）
        String suffix = fileName.substring(lastDotIndex + 1).toLowerCase();
        LogUtils.d(TAG, "【getFileSuffix】获取成功 | 后缀=" + suffix);
        return suffix;
    }

    /**
     * 生成唯一文件名（优化版：唯一、合法、简洁）
     * 生成规则：UUID(去掉"-") + "_" + 时间戳 + 原文件后缀
     * @param refFile 参考文件（用于提取后缀名，可为null）
     * @return 唯一文件名（如"a1b2c3d4e5f6_1730000000000.jpg"，无后缀则不带点）
     */
    public static String createUniqueFileName(File refFile) {
        LogUtils.d(TAG, "【createUniqueFileName】调用开始 | 参考文件=" + (refFile != null ? refFile.getAbsolutePath() : "null"));
        // 1. 获取参考文件的后缀名（自动容错null/无效文件）
        String suffix = getFileSuffixWithMultiDot(refFile);
        // 2. 生成唯一标识（UUID确保全局唯一，时间戳进一步降低重复概率）
        String uniqueId = UUID.randomUUID().toString().replace("-", "");
        long timeStamp = System.currentTimeMillis();
        // 3. 拼接文件名（分场景处理，避免多余点）
        String fileName;
        if (suffix.isEmpty()) {
            fileName = String.format("%s_%d", uniqueId, timeStamp);
        } else {
            fileName = String.format("%s_%d%s", uniqueId, timeStamp, suffix);
        }
        LogUtils.d(TAG, "【createUniqueFileName】生成成功 | 文件名=" + fileName);
        return fileName;
    }

    // ================================== 工具辅助方法（文件存在性判断）=================================
    /**
     * 判断文件是否存在
     * @param path 文件绝对路径
     * @return true-存在，false-不存在
     */
    public static boolean isFileExists(String path) {
        LogUtils.d(TAG, "【isFileExists】调用开始 | 文件路径=" + path);
        File file = new File(path);
        boolean exists = file.exists();
        LogUtils.d(TAG, "【isFileExists】判断结果 | 路径=" + path + " | 存在=" + exists);
        return exists;
    }
}

