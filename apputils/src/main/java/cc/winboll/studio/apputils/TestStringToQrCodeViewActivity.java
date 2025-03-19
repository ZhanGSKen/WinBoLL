package cc.winboll.studio.apputils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/17 19:50:46
 */
import cc.winboll.studio.apputils.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toolbar;
import cc.winboll.studio.libapputils.app.IWinBollActivity;
import cc.winboll.studio.libapputils.bean.APPInfo;
import cc.winboll.studio.libapputils.view.StringToQrCodeView;

public class TestStringToQrCodeViewActivity extends Activity implements IWinBollActivity {

    public static final String TAG = "TestStringToQrCodeViewActivity";

    StringToQrCodeView mStringToQrCodeView;
//
//    @Override
//    public Activity getActivity() {
//        return this;
//    }

    @Override
    public APPInfo getAppInfo() {
        return null;
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
