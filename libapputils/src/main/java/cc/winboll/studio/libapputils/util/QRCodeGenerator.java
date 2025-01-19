package cc.winboll.studio.libapputils.util;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/17 20:49:55
 * @Describe 二维码工具
 */
import android.graphics.Bitmap;
import android.widget.TextView;
import cc.winboll.studio.libapputils.R;
import cc.winboll.studio.libapputils.log.LogUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class QRCodeGenerator {

    public static final String TAG = "QRCodeGenerator";

//    public static void main(String[] args) {
//        String qrCodeText = "https://www.example.com";
//        String filePath = "qrcode.png";
//        int width = 300;
//        int height = 300;
//        String imageFormat = "png";
//
//        generateQRCodeImage(qrCodeText, width, height, imageFormat, filePath);
//        // 新增的二维码读取部分
//        Result result = decodeQRCode(filePath);
//        if (result != null) {
//            System.out.println("解码结果: " + result.getText());
//        }
//    }

    public static Bitmap generateQRCodeImage(String qrCodeText, int width, int height) {
        // 创建QRCodeWriter对象
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            // 使用QRCodeWriter生成BitMatrix
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, width, height);
            // 使用BarcodeEncoder将BitMatrix转化为Bitmap
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            // 将Bitmap设置给ImageView显示二维码

            return bitmap;
        } catch (WriterException e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
        return null;
    }

    
       
    
    
}

