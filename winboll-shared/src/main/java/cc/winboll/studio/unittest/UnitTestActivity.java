package cc.winboll.studio.unittest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.R;
import cc.winboll.studio.shared.app.WinBollActivity;
import cc.winboll.studio.shared.log.LogUtils;
import cc.winboll.studio.shared.util.UriUtils;
import cc.winboll.studio.shared.view.StringToQrCodeView;
import com.hjq.toast.ToastUtils;
import java.util.UUID;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/12/19 14:00:26
 */
public class UnitTestActivity extends WinBollActivity {

    public static final String TAG = "UnitTestActivity";

    @Override
    public String getTag() {
        return this.TAG;
    }

    @Override
    protected Toolbar initToolBar() {
        return null;
    }

    @Override
    protected boolean isEnableDisplayHomeAsUp() {
        return true;
    }

    @Override
    protected boolean isAddWinBollToolBar() {
        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 接收并处理 Intent 数据，函数 Intent 处理接收就直接返回
        if (prosessIntents(getIntent())) return;
        // 以下正常创建主窗口
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unittest);

        // 接收分享数据
        Intent intent = getIntent();
        prosessIntents(getIntent());

        LinearLayout llMain = findViewById(R.id.activityunittestLinearLayout2);
        final StringToQrCodeView stringToQrCodeView = new StringToQrCodeView(this){};
        stringToQrCodeView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    ToastUtils.show("onClick");
                    stringToQrCodeView.stringToQrCode(UUID.randomUUID().toString());
                }
            }
        );
        stringToQrCodeView.stringToQrCode(UUID.randomUUID().toString());
        llMain.addView(new StringToQrCodeView(this){});
    }

    //
    // 处理传入的 Intent 数据
    //
    boolean prosessIntents(Intent intent) {
        if (intent == null 
            || intent.getAction() == null
            || intent.getAction().equals(""))
            return false;
            
        if (intent.getAction().equals(StringToQrCodeView.ACTION_UNITTEST_QRCODE)) {
            LogUtils.d(TAG, "prosessIntents");

            StringBuilder sb = new StringBuilder();
            String szSrcPath = "";

            String action = intent.getAction();//action
            String type = intent.getType();//类型
            //LogUtils.d(TAG, "action is " + action);
            //LogUtils.d(TAG, "type is " + type);
            if ((Intent.ACTION_SEND.equals(action) || Intent.ACTION_VIEW.equals(action) || Intent.ACTION_EDIT.equals(action))
                && type != null && (("application/json".equals(type)) || ("text/x-json".equals(type)))) {

                //取出文件uri
                Uri uri = intent.getData();
                if (uri == null) {
                    uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                }
                //获取文件真实地址
                szSrcPath = UriUtils.getFileFromUri(getApplication(), uri);
                if (TextUtils.isEmpty(szSrcPath)) {
                    return false;
                }
            } else {
                sb.append("Not supported action.");
            }
            LogUtils.d(TAG, "szSrcPath : " + szSrcPath);
        } else {
            LogUtils.d(TAG, "prosessIntents|" + intent.getAction() + "|yet");
        }
        return false;
    }
}
