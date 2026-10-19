package cc.winboll.studio.powerbell.utils;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/12/11 09:14
 * @Describe Assets 目录拷贝工具类
 * 支持将 assets/images/ 下所有文件、子目录拷贝到指定路径
 * 适配：Java7 | API30 | 递归拷贝 | 覆盖写入
 */
public class AssetsCopyUtils {
    // ======================== 静态常量区 ========================
    public static final String TAG = "AssetsCopyUtils";
    private static final int BUFFER_SIZE = 1024 * 8; // 8KB 缓冲区，平衡性能与内存占用

    // ======================== 公共快捷方法区（对外入口） ========================
    /**
     * 拷贝 assets/images/ 目录到指定目标目录
     * @param context      上下文
     * @param targetDirPath 目标目录完整路径（如 /sdcard/PowerBell/assets_images）
     * @return 拷贝是否成功
     */
    public static boolean copyAssetsImagesToDir(Context context, String targetDirPath) {
        LogUtils.d(TAG, "copyAssetsImagesToDir() 调用，目标路径：" + targetDirPath);
        // 拷贝 assets/images 根目录
        boolean result = copyAssetsDirToDir(context, "images", targetDirPath);
        LogUtils.d(TAG, "copyAssetsImagesToDir() 执行完成，结果：" + result);
        return result;
    }

    // ======================== 公共核心方法区（递归拷贝目录） ========================
    /**
     * 递归拷贝 assets 下指定目录到目标目录
     * @param context      上下文
     * @param assetsDir    assets 下的源目录（如 "images"、"images/subdir"）
     * @param targetDirPath 目标目录完整路径
     * @return 拷贝是否成功
     */
    public static boolean copyAssetsDirToDir(Context context, String assetsDir, String targetDirPath) {
        LogUtils.d(TAG, "copyAssetsDirToDir() 调用，源目录：" + assetsDir + "，目标路径：" + targetDirPath);
        if (context == null) {
            LogUtils.e(TAG, "copyAssetsDirToDir() 拷贝失败：上下文为空");
            return false;
        }

        File targetDir = new File(targetDirPath);
        // 创建目标目录（含多级父目录）
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            LogUtils.e(TAG, "copyAssetsDirToDir() 创建目标目录失败：" + targetDirPath);
            return false;
        }

        try {
            // 获取 assets 目录下的文件/子目录列表
            String[] fileList = context.getAssets().list(assetsDir);
            if (fileList == null || fileList.length == 0) {
                LogUtils.d(TAG, "copyAssetsDirToDir() assets 目录为空：" + assetsDir);
                return true;
            }

            for (String fileName : fileList) {
                String assetsFilePath = assetsDir + File.separator + fileName;
                String targetFilePath = targetDirPath + File.separator + fileName;

                // 判断当前项是文件还是子目录
                String[] subFileList = context.getAssets().list(assetsFilePath);
                if (subFileList != null && subFileList.length > 0) {
                    // 是子目录，递归拷贝
                    if (!copyAssetsDirToDir(context, assetsFilePath, targetFilePath)) {
                        LogUtils.e(TAG, "copyAssetsDirToDir() 递归拷贝子目录失败：" + assetsFilePath);
                        return false;
                    }
                } else {
                    // 是文件，直接拷贝
                    if (!copyAssetsFileToDir(context, assetsFilePath, targetFilePath)) {
                        LogUtils.e(TAG, "copyAssetsDirToDir() 拷贝文件失败：" + assetsFilePath);
                        return false;
                    }
                }
            }
            LogUtils.d(TAG, "copyAssetsDirToDir() assets 目录拷贝完成：" + assetsDir + " -> " + targetDirPath);
            return true;
        } catch (IOException e) {
            LogUtils.e(TAG, "copyAssetsDirToDir() 拷贝 assets 目录异常：" + e.getMessage(), e);
            return false;
        }
    }

    // ======================== 私有辅助方法区（单个文件拷贝） ========================
    /**
     * 拷贝 assets 下单个文件到指定路径
     * @param context       上下文
     * @param assetsFilePath assets 下的文件路径（如 "images/cloud.png"）
     * @param targetFilePath 目标文件完整路径
     * @return 拷贝是否成功
     */
    public static boolean copyAssetsFileToDir(Context context, String assetsFilePath, String targetFilePath) {
        LogUtils.d(TAG, "copyAssetsFileToDir() 调用，源文件：" + assetsFilePath + "，目标文件：" + targetFilePath);
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = context.getAssets().open(assetsFilePath);
            File targetFile = new File(targetFilePath);

            // 覆盖已存在的文件
            if (targetFile.exists() && !targetFile.delete()) {
                LogUtils.w(TAG, "copyAssetsFileToDir() 覆盖目标文件失败，跳过：" + targetFilePath);
                return true;
            }

            outputStream = new FileOutputStream(targetFile);
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            LogUtils.d(TAG, "copyAssetsFileToDir() 文件拷贝成功：" + assetsFilePath + " -> " + targetFilePath);
            return true;
        } catch (IOException e) {
            LogUtils.e(TAG, "copyAssetsFileToDir() 拷贝文件失败：" + assetsFilePath + "，异常：" + e.getMessage(), e);
            return false;
        } finally {
            // 关闭流
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                LogUtils.e(TAG, "copyAssetsFileToDir() 关闭流异常：" + e.getMessage(), e);
            }
        }
    }
}

