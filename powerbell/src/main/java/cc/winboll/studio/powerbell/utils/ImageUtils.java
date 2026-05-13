package cc.winboll.studio.powerbell.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.R;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 图片处理工具类（质量压缩专用）
 * 功能：1. 图片JPEG质量压缩（覆盖源文件）；2. 获取主题colorAccent颜色；3. 位图纯色背景合成
 * 适配：Java 7 + Android API 30
 * 核心逻辑：
 * - 压缩：Bitmap.compress + FileChannel 高效文件复制
 * - 主题颜色：TypedArray 解析主题属性
 * - 位图合成：Canvas 绘制（支持透明通道）
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 */
public class ImageUtils {

    // ================================== 静态常量区（置顶归类，消除魔法值）=================================
    public static final String TAG = "ImageUtils";
    private static final Bitmap.CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    private static final int MIN_COMPRESS_QUALITY = 0;
    private static final int MAX_COMPRESS_QUALITY = 100;
    private static final int[] COLOR_ACCENT_ATTR = new int[]{R.attr.colorAccent}; // colorAccent属性数组
    private static final int DEFAULT_COLOR = Color.parseColor("#FFFFFFFF"); // 默认返回颜色

    // ================================== 主题颜色获取方法 =================================
    /**
     * 从当前应用主题中获取 colorAccent 颜色值
     * @param context 上下文，用于获取主题与资源
     * @return 解析到的 colorAccent 颜色值，解析失败返回默认白色#FFFFFFFF
     */
    public static int getColorAccent(Context context) {
        // 方法调用日志
        LogUtils.d(TAG, "【getColorAccent】方法调用");
        // 参数校验
        if (context == null) {
            LogUtils.e(TAG, "【getColorAccent】参数异常：Context为空");
            return DEFAULT_COLOR;
        }

        TypedArray typedArray = null;
        try {
            // 从主题解析属性
            typedArray = context.obtainStyledAttributes(COLOR_ACCENT_ATTR);
            int colorAccent = typedArray.getColor(0, DEFAULT_COLOR);
            LogUtils.d(TAG, String.format("【getColorAccent】解析成功 | colorAccent=0x%08X", colorAccent));
            return colorAccent;
        } catch (Exception e) {
            LogUtils.e(TAG, "【getColorAccent】解析失败，返回默认颜色", e);
            return DEFAULT_COLOR;
        } finally {
            // 回收资源
            if (typedArray != null) {
                typedArray.recycle();
                LogUtils.d(TAG, "【getColorAccent】TypedArray资源已回收");
            }
        }
    }

    // ================================== 位图合成方法 =================================
    /**
     * 在纯色背景上绘制前景位图，实现FIT_CENTER居中效果
     * @param bgColor 背景颜色
     * @param originalFrameW 目标画布宽度
     * @param originalFrameH 目标画布高度
     * @param fgBitmap 前景位图
     * @return 合成后的位图，失败返回null
     */
    public static Bitmap drawBitmapOnSolidBackground(final int bgColor, int originalFrameW, int originalFrameH, Bitmap fgBitmap) {
        // 方法调用及入参日志
        LogUtils.d(TAG, String.format("【drawBitmapOnSolidBackground】方法调用 | 背景色=0x%08X | 目标尺寸=%dx%d | 前景位图=%s",
									  bgColor, originalFrameW, originalFrameH,
									  (fgBitmap != null ? fgBitmap.getWidth() + "x" + fgBitmap.getHeight() : "null")));

        // 1. 严格参数校验
        if (fgBitmap == null || fgBitmap.isRecycled()) {
            LogUtils.e(TAG, "【drawBitmapOnSolidBackground】参数异常：前景Bitmap为空或已回收");
            return null;
        }
        if (fgBitmap.getWidth() <= 0 || fgBitmap.getHeight() <= 0) {
            LogUtils.e(TAG, "【drawBitmapOnSolidBackground】参数异常：前景Bitmap尺寸无效");
            return null;
        }
        if (originalFrameW <= 0 || originalFrameH <= 0) {
            LogUtils.e(TAG, "【drawBitmapOnSolidBackground】参数异常：原画框尺寸无效（宽高必须大于0）");
            return null;
        }

        // 2. 强制画布尺寸为目标尺寸
        int canvasW = originalFrameW;
        int canvasH = originalFrameH;
        LogUtils.d(TAG, String.format("【drawBitmapOnSolidBackground】画布尺寸已确定：%dx%d", canvasW, canvasH));

        // 3. 创建结果位图（ARGB_8888支持透明通道）
        Bitmap resultBitmap = Bitmap.createBitmap(canvasW, canvasH, Bitmap.Config.ARGB_8888);
        if (resultBitmap == null) {
            LogUtils.e(TAG, "【drawBitmapOnSolidBackground】创建结果Bitmap失败");
            return null;
        }

        // 4. 画布绘制
        Canvas canvas = new Canvas(resultBitmap);
        try {
            // 4.1 绘制纯色背景
            Paint bgPaint = new Paint();
            bgPaint.setColor(bgColor);
            bgPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, canvasW, canvasH, bgPaint);

            // 4.2 计算前景 FIT_CENTER 变换参数（等比缩放至完全放入画布，居中显示）
            float fgWidth = fgBitmap.getWidth();
            float fgHeight = fgBitmap.getHeight();
            float scaleX = (float) canvasW / fgWidth;
            float scaleY = (float) canvasH / fgHeight;
            float scale = Math.min(scaleX, scaleY); // 取最小比例，保证完全放入画布

            // 4.3 计算缩放后前景尺寸
            float scaledW = fgWidth * scale;
            float scaledH = fgHeight * scale;

            // 4.4 计算居中位置（缩放后居中，无裁剪）
            float translateX = (canvasW - scaledW) / 2f;
            float translateY = (canvasH - scaledH) / 2f;

            // 4.5 构建变换矩阵（缩放+平移，实现 FIT_CENTER 效果）
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale); // 等比缩放
            matrix.postTranslate(translateX, translateY); // 居中平移

            // 4.6 绘制前景（保留透明通道，抗锯齿）
            Paint fgPaint = new Paint();
            fgPaint.setAntiAlias(true);
            fgPaint.setDither(true);
            canvas.drawBitmap(fgBitmap, matrix, fgPaint);

            LogUtils.d(TAG, String.format("【drawBitmapOnSolidBackground】合成成功 | 缩放比例=%.2f | 居中位置=(%.1f,%.1f) | 效果=FIT_CENTER",
										  scale, translateX, translateY));
            return resultBitmap;
        } catch (Exception e) {
            LogUtils.e(TAG, "【drawBitmapOnSolidBackground】合成失败", e);
            if (resultBitmap != null && !resultBitmap.isRecycled()) {
                resultBitmap.recycle();
            }
            return null;
        }
    }

    // ================================== 核心压缩方法 =================================
    /**
     * 图片质量压缩（JPEG格式），压缩后覆盖源文件
     * @param context 上下文（备用）
     * @param srcImagePath 源图片文件路径（非空，文件需存在）
     * @param dstImagePath 压缩后临时保存路径（非空）
     * @param compressQuality 压缩质量（0-100，数值越小压缩率越高）
     */
    public static void bitmapCompress(Context context, String srcImagePath, String dstImagePath, int compressQuality) {
        // 方法调用及入参日志
        LogUtils.d(TAG, String.format("【bitmapCompress】方法调用 | 源路径=%s | 临时路径=%s | 压缩质量=%d",
									  srcImagePath, dstImagePath, compressQuality));

        // 1. 前置参数校验
        if (srcImagePath == null || srcImagePath.isEmpty()) {
            LogUtils.e(TAG, "【bitmapCompress】参数异常：源文件路径为空");
            return;
        }
        if (dstImagePath == null || dstImagePath.isEmpty()) {
            LogUtils.e(TAG, "【bitmapCompress】参数异常：临时文件路径为空");
            return;
        }
        if (compressQuality < MIN_COMPRESS_QUALITY || compressQuality > MAX_COMPRESS_QUALITY) {
            LogUtils.e(TAG, String.format("【bitmapCompress】参数异常：压缩质量超出范围（0-100），当前值=%d", compressQuality));
            return;
        }

        File srcFile = new File(srcImagePath);
        if (!srcFile.exists() || !srcFile.isFile()) {
            LogUtils.e(TAG, "【bitmapCompress】源文件无效：不存在或不是文件 " + srcImagePath);
            return;
        }

        Bitmap compressBitmap = null;
        OutputStream outputStream = null;
        try {
            // 2. 读取源图片为Bitmap
            compressBitmap = BitmapFactory.decodeFile(srcImagePath);
            if (compressBitmap == null) {
                LogUtils.e(TAG, "【bitmapCompress】Bitmap解码失败：无法读取源图片 " + srcImagePath);
                return;
            }
            LogUtils.d(TAG, String.format("【bitmapCompress】Bitmap解码成功 | 尺寸=%dx%d",
										  compressBitmap.getWidth(), compressBitmap.getHeight()));

            // 3. 创建临时压缩文件目录
            File dstFile = new File(dstImagePath);
            File dstParentDir = dstFile.getParentFile();
            if (dstParentDir != null && !dstParentDir.exists()) {
                boolean isDirCreated = dstParentDir.mkdirs();
                LogUtils.d(TAG, String.format("【bitmapCompress】临时目录创建%s：%s",
											  isDirCreated ? "成功" : "失败", dstParentDir.getAbsolutePath()));
            }

            // 4. 写入压缩数据
            outputStream = new FileOutputStream(dstFile);
            boolean isCompressSuccess = compressBitmap.compress(COMPRESS_FORMAT, compressQuality, outputStream);
            if (!isCompressSuccess) {
                LogUtils.e(TAG, "【bitmapCompress】压缩失败：Bitmap.compress 执行失败");
                return;
            }
            LogUtils.d(TAG, "【bitmapCompress】压缩成功：临时文件已生成 " + dstFile.getAbsolutePath());

            // 5. 复制压缩文件覆盖源文件
            FileUtils.copyFileUsingFileChannels(dstFile, srcFile);
            LogUtils.d(TAG, String.format("【bitmapCompress】%d%%压缩结束：已覆盖源文件 %s",
										  compressQuality, srcImagePath));

        } catch (FileNotFoundException e) {
            LogUtils.e(TAG, "【bitmapCompress】文件未找到异常", e);
        } catch (IOException e) {
            LogUtils.e(TAG, "【bitmapCompress】IO异常", e);
        } finally {
            // 6. 关闭输出流
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LogUtils.e(TAG, "【bitmapCompress】输出流关闭失败", e);
                }
            }
            // 7. 回收Bitmap
            if (compressBitmap != null && !compressBitmap.isRecycled()) {
                compressBitmap.recycle();
                LogUtils.d(TAG, "【bitmapCompress】Bitmap资源已回收");
            }
        }
    }
}

