package cc.winboll.studio.libapputils.view;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/12/19 13:49:14
 * @Describe 把字符串转化为二维码的视图
 */
import cc.winboll.studio.libapputils.R;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cc.winboll.studio.libapputils.util.QRCodeGenerator;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class StringToQrCodeView extends LinearLayout {

    static String TAG = "StringToQrCodeView";

    Context mContext;
    TextView mTextView;
    ImageView mImageView;
    
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private DecoratedBarcodeView barcodeView;

    public StringToQrCodeView(Context context) {
        super(context);
        initView(context);
    }

    public StringToQrCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public StringToQrCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public StringToQrCodeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    void initView(Context context) {
        mContext = context;
        View view = inflate(context, R.layout.view_string2qrcode, null);
        mTextView = view.findViewById(R.id.viewstring2qrcodeTextView1);
        mImageView = view.findViewById(R.id.viewstring2qrcodeImageView1);
        addView(view);
        stringToQrCode("Hello, World!");
    }

    public void stringToQrCode(String text) {
        Drawable drawable = new BitmapDrawable(getResources(), QRCodeGenerator.generateQRCodeImage(text, 300, 300));
        mTextView.setText(text);
        mImageView.setBackground(drawable);
    }
    
}
