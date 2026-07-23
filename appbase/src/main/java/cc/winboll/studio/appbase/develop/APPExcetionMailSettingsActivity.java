package cc.winboll.studio.appbase.develop;

import android.app.Activity;
import android.os.Bundle;
import cc.winboll.studio.appbase.R;
import cc.winboll.studio.libappbase.views.SMTPConfigView;

/**
 * @Author 豆包&BigPickle&MiMo&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/07/21 12:44
 * @Describe 应用异常信息邮件接收账号设置
 * 使用libaes的SMTPConfigView控件配置SMTP服务器参数
 */
public class APPExcetionMailSettingsActivity extends Activity {

    public static final String TAG = "APPExcetionMailSettingsActivity";

    private SMTPConfigView mSMTPConfigView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appexcetionmailsettings);

        mSMTPConfigView = (SMTPConfigView) findViewById(R.id.smtp_config_view);
    }
}
