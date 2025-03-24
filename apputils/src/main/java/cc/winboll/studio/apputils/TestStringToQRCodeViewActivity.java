package cc.winboll.studio.apputils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/17 19:50:46
 */
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toolbar;
import cc.winboll.studio.apputils.R;
import cc.winboll.studio.libapputils.view.StringToQrCodeView;

public class TestStringToQRCodeViewActivity extends Activity {

    public static final String TAG = "TestStringToQrCodeViewActivity";

    StringToQrCodeView mStringToQrCodeView;
//
//    @Override
//    public Activity getActivity() {
//        return this;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teststringtoqrcodeview);
        
        // 初始化工具栏
        Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setSubtitle(TAG);
        setActionBar(mToolbar);
        
        mStringToQrCodeView = findViewById(R.id.activityteststringtoqrcodeviewStringToQrCodeView1);
    }
}
