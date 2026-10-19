package cc.winboll.studio.winboll.utils;

import android.graphics.Bitmap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * ZXing二维码生成工具类
 * 依赖：com.google.zxing:core:3.4.1 + com.journeyapps:zxing-android-embedded:3.6.0
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/07
 */
public class ZXingUtils {

    /**
     * 生成二维码Bitmap（核心方法，使用journeyapps工具类）
     * @param content 内容（如微信支付的code_url）
     * @param width 二维码宽度（px）
     * @param height 二维码高度（px）
     * @return 二维码Bitmap，失败返回null
     */
    public static Bitmap createQRCodeBitmap(String content, int width, int height) {
        // 1. 入参合法性校验
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        if (width <= 0 || height <= 0) {
            return null;
        }

        // 2. 配置二维码参数
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8"); // 字符编码
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // 高容错级别（H级可容忍30%遮挡）
        hints.put(EncodeHintType.MARGIN, 1); // 边距（值越小，二维码越紧凑，建议1-4）

        try {
            // 3. 生成BitMatrix（二维码矩阵）
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
				content,
				BarcodeFormat.QR_CODE,
				width,
				height,
				hints
            );

            // 4. 转换BitMatrix为Bitmap（关键：使用journeyapps的BarcodeEncoder）
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.createBitmap(bitMatrix);

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 重载方法：生成正方形二维码（宽度=高度）
     * @param content 内容
     * @param size 二维码边长（px）
     * @return 二维码Bitmap
     */
    public static Bitmap createQRCodeBitmap(String content, int size) {
        return createQRCodeBitmap(content, size, size);
    }
}

