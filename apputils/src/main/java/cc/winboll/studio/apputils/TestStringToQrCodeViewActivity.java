package cc.winboll.studio.apputils;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libapputils.app.BaseWinBollActivity;
import cc.winboll.studio.libapputils.app.IWinBollActivity;
import cc.winboll.studio.libapputils.view.StringToQrCodeView;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/17 19:50:46
 */
public class TestStringToQrCodeViewActivity extends BaseWinBollActivity implements IWinBollActivity {


    public static final String TAG = "TestStringToQrCodeViewActivity";

    StringToQrCodeView mStringToQrCodeView;

    @Override
    public AppCompatActivity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public Toolbar initToolBar() {
        return findViewById(R.id.activityteststringtoqrcodeviewToolbar1);
    }

    @Override
    public boolean isEnableDisplayHomeAsUp() {
        return true;
    }

    @Override
    public boolean isAddWinBollToolBar() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teststringtoqrcodeview);
        mStringToQrCodeView = findViewById(R.id.activityteststringtoqrcodeviewStringToQrCodeView1);
    }
}
