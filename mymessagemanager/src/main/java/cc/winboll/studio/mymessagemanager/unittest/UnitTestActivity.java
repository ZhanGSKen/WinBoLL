package cc.winboll.studio.mymessagemanager.unittest;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/25 19:00:10
 * @Describe 应用单元测试窗口
 */
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.LogView;
import cc.winboll.studio.mymessagemanager.R;
import cc.winboll.studio.mymessagemanager.views.ProtectModeTextView;

public class UnitTestActivity extends Activity {

    public static final String TAG = "UnitTestActivity";

    LogView mLogView;
    // 新增自定义控件
    ProtectModeTextView mProtectModeTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unittest);

        mLogView = findViewById(R.id.logview);
        mLogView.start();

        // 初始化ProtectModeTextView
        mProtectModeTv = findViewById(R.id.protect_mode_tv);
        // 设置测试文本，可自行修改
        String testText = "abcdefghijklmnopqrstuvwxyz消息管理 隐私保护 文本随机组合 滑动刻度测试1234567890";
        mProtectModeTv.setContentText(testText);
    }

    public void onMain(View view) {
        LogUtils.d(TAG, "SMSRecevier_Test");
        SMSRecevier_Test.main(this);
        LogUtils.d(TAG, "AddressUtils_Test");
        AddressUtils_Test.main(this);
    }
}

