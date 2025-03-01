package cc.winboll.studio.mymessagemanager.unittest;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/25 19:00:10
 * @Describe 应用单元测试窗口
 */
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.shared.log.LogUtils;
import cc.winboll.studio.shared.log.LogView;

public class UnitTestActivity extends Activity {

    public static final String TAG = "UnitTestActivity";

    LogView mLogView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unittest);
        
        mLogView = findViewById(R.id.logview);
        mLogView.start();
    }

    public void onMain(View view) {
        LogUtils.d(TAG, "SMSRecevier_Test");
        SMSRecevier_Test.main(this);
        LogUtils.d(TAG, "AddressUtils_Test");
        AddressUtils_Test.main(this);
    }
}
