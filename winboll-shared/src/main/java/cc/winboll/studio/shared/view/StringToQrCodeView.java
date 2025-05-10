package cc.winboll.studio.shared.view;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/12/19 13:49:14
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cc.winboll.studio.R;
import cc.winboll.studio.shared.log.LogUtils;
import com.google.zxing.EncodeHintType;
import com.hjq.toast.ToastUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StringToQrCodeView extends LinearLayout {

    static String TAG = "StringToQrCodeView";

    public static final String ACTION_UNITTEST_QRCODE = StringToQrCodeView.class.getName() + "_ACTION_UNITTEST_QRCODE";
    public static final String EXTRA_UNITTEST_QRCODE_IMAGENULL_SRCPATH = StringToQrCodeView.class.getName() + "_EXTRA_UNITTEST_QRCODE_IMAGENULL_SRCPATH";

    static Context _Context;
    static TextView mTextView;
    static ImageView mImageView;

    public StringToQrCodeView(Context context) {
        super(context);
        _Context = context;
        View view = inflate(context, R.layout.view_string2qrcode, null);
        mTextView = view.findViewById(R.id.viewstring2qrcodeTextView1);
        mImageView = view.findViewById(R.id.viewstring2qrcodeImageView1);
        addView(view);
    }

    public void stringToQrCode(String text) {
        String imageFilePath = StringToQrCode.stringToQrCode(text);
        if (!imageFilePath.equals("")) {
            // 在Java中
            //String filePath = "/storage/emulated/0/your_folder/your_png_file.png";
            String filePath = imageFilePath;
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            mTextView.setText(text);
            mImageView.setBackground(drawable);
            ToastUtils.show("filePath : " + filePath);
        } else {
            mTextView.setText("NULL");
            ToastUtils.show("NULL");
        }
    }

    static class StringToQrCode {

        static String TAG = "StringToQrCode"; 

        public static String stringToQrCode(String text) { 
            //String text = "这是一个示例字符串"; 
            text = "这是一个示例字符串";
            int width = 300; 
            int height = 300; 
            String format = "png"; 

            Map<EncodeHintType, Object> hints = new HashMap<>(); 
            hints.put(EncodeHintType.CHARACTER_SET, "UTF - 8"); 

            //try {
            //String qrFileName = MD5Utils.encrypt(UUID.randomUUID().toString()) + ".dat";
            String qrFileName = UUID.randomUUID().toString() + ".dat";
            File outputFile = new File(_Context.getCacheDir(), File.separator + qrFileName); 
            //ToastUtils.show("outputFile : " + outputFile.getAbsolutePath());

            /*BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints); 
             MatrixToImageWriter.writeToFile(bitMatrix, format, outputFile); 
             //System.out.println("二维码已生成: " + outputFile.getAbsolutePath()); 
             //_ImageView.setBackground();
             String msg = "二维码已生成: " + outputFile.getAbsolutePath();
             ToastUtils.show(msg);
             return outputFile.getAbsolutePath();

             } catch (WriterException | IOException e) { 
             //e.printStackTrace();
             LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
             }*/
            try {
                Files.copy(Paths.get("/storage/emulated/0/Pictures/Gallery/owner/篮球/kuX8B6aXAG_small-选取图标位置-透明背景.png"),
                           Paths.get(outputFile.getPath()));
                return outputFile.getAbsolutePath();
            } catch (IOException e) {
                LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
            }
            return "";
        } 
    }
}
