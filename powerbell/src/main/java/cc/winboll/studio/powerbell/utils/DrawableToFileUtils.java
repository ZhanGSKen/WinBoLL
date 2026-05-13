package cc.winboll.studio.powerbell.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/12/08 21:11
 * @Describe 把 R.drawable 中的图片保存为 File 对象的工具类
 * 适配 PowerBell 项目：支持指定保存路径、自动创建目录、处理PNG图片压缩
 */
public class DrawableToFileUtils {
    // ================================== 静态常量区（置顶归类，消除魔法值）=================================
    public static final String TAG = "DrawableToFileUtils";
    private static final String IMAGE_FORMAT_PNG = ".png"; // 目标图片格式
    private static final Bitmap.CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.PNG; // 压缩格式
    private static final int COMPRESS_QUALITY = 100; // PNG无损压缩质量
    private static final long MIN_FILE_SIZE = 100; // 有效文件最小字节数

    // ================================== 核心工具方法（基础版：指定文件路径）=================================
    /**
     * 核心方法：将 R.drawable 图片保存为 File 对象
     * @param context 上下文（用于获取 Resources）
     * @param drawableResId 图片资源ID（如 R.drawable.ic_test_png）
     * @param filePath 保存的文件路径（可带/不带.png后缀）
     * @return 保存成功返回 File 对象，失败返回 null
     */
    public static File saveDrawableToFile(Context context, int drawableResId, String filePath) {
        LogUtils.d(TAG, "【saveDrawableToFile】调用开始 | 资源ID=" + drawableResId + " | 目标路径=" + filePath);
        // 1. 校验核心参数（避免空指针/无效参数）
        if (context == null) {
            LogUtils.e(TAG, "【saveDrawableToFile】参数异常：context为空");
            return null;
        }
        if (drawableResId == 0) {
            LogUtils.e(TAG, "【saveDrawableToFile】参数异常：drawableResId为0");
            return null;
        }
        if (filePath == null || filePath.isEmpty()) {
            LogUtils.e(TAG, "【saveDrawableToFile】参数异常：filePath为空");
            return null;
        }

        // 2. 格式化文件路径（强制添加.png后缀）
        String targetFilePath = filePath.endsWith(IMAGE_FORMAT_PNG) ? filePath : filePath + IMAGE_FORMAT_PNG;
        if (!filePath.equals(targetFilePath)) {
            LogUtils.d(TAG, "【saveDrawableToFile】格式适配：自动添加.png后缀 | 最终路径=" + targetFilePath);
        }

        // 3. 构建目标File对象并创建父目录
        File targetFile = new File(targetFilePath);
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean isDirCreated = parentDir.mkdirs();
            if (!isDirCreated) {
                LogUtils.e(TAG, "【saveDrawableToFile】目录创建失败：" + parentDir.getAbsolutePath());
                return null;
            }
            LogUtils.d(TAG, "【saveDrawableToFile】目录创建成功：" + parentDir.getAbsolutePath());
        }
        LogUtils.d(TAG, "【saveDrawableToFile】目标文件路径：" + targetFile.getAbsolutePath());

        // 4. 读取drawable资源为Bitmap
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeResource(context.getResources(), drawableResId);
            if (bitmap == null) {
                LogUtils.e(TAG, "【saveDrawableToFile】读取失败：无法解析drawable资源（资源ID=" + drawableResId + "）");
                return null;
            }
            LogUtils.d(TAG, "【saveDrawableToFile】读取成功：Bitmap尺寸=" + bitmap.getWidth() + "x" + bitmap.getHeight());

            // 5. 将Bitmap写入File（PNG无损保存）
            FileOutputStream fos = new FileOutputStream(targetFile);
            boolean isSaved = bitmap.compress(COMPRESS_FORMAT, COMPRESS_QUALITY, fos);
            fos.flush();
            fos.close();

            // 6. 校验保存结果
            if (isSaved && targetFile.exists() && targetFile.length() > MIN_FILE_SIZE) {
                LogUtils.d(TAG, "【saveDrawableToFile】保存成功：" + targetFile.getAbsolutePath());
                return targetFile;
            } else {
                LogUtils.e(TAG, "【saveDrawableToFile】保存失败：文件无效（存在=" + targetFile.exists() + " | 大小=" + targetFile.length() + "字节）");
                // 清理无效文件
                if (targetFile.exists()) {
                    targetFile.delete();
                    LogUtils.d(TAG, "【saveDrawableToFile】清理无效文件：" + targetFile.getAbsolutePath());
                }
                return null;
            }
        } catch (IOException e) {
            LogUtils.e(TAG, "【saveDrawableToFile】保存异常：" + e.getMessage());
            return null;
        } finally {
            // 回收Bitmap资源（避免内存溢出）
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
                LogUtils.d(TAG, "【saveDrawableToFile】资源回收：Bitmap已回收");
            }
        }
    }

    // ================================== 重载工具方法（扩展版：指定目录+文件名）=================================
    /**
     * 重载方法：自定义保存路径（灵活适配不同场景）
     * @param context 上下文
     * @param drawableResId 图片资源ID
     * @param saveDirPath 自定义保存目录路径（如 "/storage/emulated/0/PowerBell/custom/"）
     * @param fileName 保存的文件名（可带/不带.png后缀）
     * @return 保存成功返回File对象，失败返回null
     */
    public static File saveDrawableToFile(Context context, int drawableResId, String saveDirPath, String fileName) {
        LogUtils.d(TAG, "【saveDrawableToFile】重载方法调用开始 | 资源ID=" + drawableResId + " | 目录=" + saveDirPath + " | 文件名=" + fileName);
        // 构建完整文件路径
        File targetFile = new File(saveDirPath, fileName);
        return saveDrawableToFile(context, drawableResId, targetFile.getAbsolutePath());
    }
}

