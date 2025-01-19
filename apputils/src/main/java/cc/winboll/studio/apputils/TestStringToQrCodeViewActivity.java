package cc.winboll.studio.apputils;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libapputils.app.WinBollActivity;
import cc.winboll.studio.libapputils.view.StringToQrCodeView;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/17 19:50:46
 */
public class TestStringToQrCodeViewActivity extends WinBollActivity {
    
    public static final String TAG = "TestStringToQrCodeViewActivity";
    
    StringToQrCodeView mStringToQrCodeView;
    
    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected Toolbar initToolBar() {
        return findViewById(R.id.activityteststringtoqrcodeviewToolbar1);
    }

    @Override
    protected boolean isEnableDisplayHomeAsUp() {
        return true;
    }

    @Override
    protected boolean isAddWinBollToolBar() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teststringtoqrcodeview);
        mStringToQrCodeView = findViewById(R.id.activityteststringtoqrcodeviewStringToQrCodeView1);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setSubTitle(TAG);
    }
}
