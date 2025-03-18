package cc.winboll.studio.libapputils.activities;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/18 10:32:21
 * @Describe 二维码扫码解码窗口
 */
import cc.winboll.studio.libapputils.R;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toolbar;
import cc.winboll.studio.libapputils.app.IWinBollActivity;
import cc.winboll.studio.libapputils.bean.APPInfo;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import java.util.List;

public class QRCodeDecodeActivity extends Activity implements IWinBollActivity {

    public static final String TAG = "QRCodeDecodeActivity";

    public static final String EXTRA_RESULT = "EXTRA_RESULT";
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    TextView resultTextView;
    DecoratedBarcodeView barcodeView;

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public APPInfo getAppInfo() {
        return null;
    }
    
    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public boolean isEnableDisplayHomeAsUp() {
        return true;
    }


    @Override
    public boolean isAddWinBollToolBar() {
        return false;
    }

    @Override
    public Toolbar initToolBar() {
        return findViewById(R.id.activityqrcodedecodeToolbar1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcodedecode);
        resultTextView = findViewById(R.id.activityqrcodedecodeTextView1);
        barcodeView = findViewById(R.id.activityqrcodedecodeDecoratedBarcodeView1);
        // 请求相机权限
//        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
//            != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                                              new String[]{android.Manifest.permission.CAMERA},
//                                              REQUEST_CAMERA_PERMISSION);
//        } else {
//            startScanning();
//        }
        startScanning();


    }

    private void startScanning() {
        barcodeView.getBarcodeView().setDecoderFactory(null);
        barcodeView.decodeContinuous(barcodeCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                // 权限被拒绝的处理
            }
        }
    }

    BarcodeCallback barcodeCallback = new BarcodeCallback(){
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                //Toast.makeText(MainActivity.this, "Scanned: " + result.getText(), Toast.LENGTH_SHORT).show();
                //ToastUtils.show("Scanned: " + result.getText());
                barcodeView.pause();
                Intent intent = new Intent();
                intent.putExtra(EXTRA_RESULT, result.getText());
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }
}

