package cc.winboll.studio.libappbase.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import cc.winboll.studio.libappbase.R;
import cc.winboll.studio.libappbase.utils.APPMSGMailUtils;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author 豆包&BigPickle&MiMo&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/07/23 17:35
 * @Describe SMTP邮件配置视图控件，用于设置QQ邮件SMTP服务参数
 */
public class SMTPConfigView extends LinearLayout {

    public static final String TAG = "SMTPConfigView";

    private EditText mEtServer;
    private EditText mEtPort;
    private EditText mEtSender;
    private EditText mEtAuthCode;
    private EditText mEtRecipient;
    private CheckBox mCbEnabled;

    public SMTPConfigView(Context context) {
        super(context);
        initView(context);
    }

    public SMTPConfigView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LogUtils.d(TAG, "initView: 初始化SMTP配置视图");
        View.inflate(context, R.layout.view_smtp_config, this);

        mEtServer = (EditText) findViewById(R.id.et_smtp_server);
        mEtPort = (EditText) findViewById(R.id.et_smtp_port);
        mEtSender = (EditText) findViewById(R.id.et_smtp_sender);
        mEtAuthCode = (EditText) findViewById(R.id.et_smtp_auth_code);
        mEtRecipient = (EditText) findViewById(R.id.et_smtp_recipient);
        mCbEnabled = (CheckBox) findViewById(R.id.cb_smtp_enabled);

        mCbEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					APPMSGMailUtils.setEnabled(getContext(), isChecked);
				}
			});

        // 加载已保存的配置
        loadConfig();

        // 保存按钮点击事件
        findViewById(R.id.btn_smtp_save).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					saveConfig();
				}
			});

        // 发送测试邮件按钮点击事件
        findViewById(R.id.btn_smtp_test_send).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					saveConfig();
					Context ctx = getContext();
					String recipient = mEtRecipient.getText().toString().trim();
					APPMSGMailUtils.sendMail(ctx, "SMTP测试邮件",
											 "这是一封SMTP配置测试邮件。如果您收到此邮件，说明SMTP配置正确。",
											 recipient);
				}
			});
    }

    //
    // 从SharedPreferences加载已保存的SMTP配置
    //
    private void loadConfig() {
        Context context = getContext();
        mEtServer.setText(APPMSGMailUtils.getServer(context));
        mEtPort.setText(APPMSGMailUtils.getPort(context));
        mEtSender.setText(APPMSGMailUtils.getSender(context));
        mEtAuthCode.setText(APPMSGMailUtils.getAuthCode(context));
        mEtRecipient.setText(APPMSGMailUtils.getRecipient(context));
        mCbEnabled.setChecked(APPMSGMailUtils.isEnabled(context));
        LogUtils.d(TAG, "loadConfig: SMTP配置已加载");
    }

    //
    // 将当前输入的SMTP配置保存到SharedPreferences
    //
    private void saveConfig() {
        Context context = getContext();
        String server = mEtServer.getText().toString().trim();
        String port = mEtPort.getText().toString().trim();
        String sender = mEtSender.getText().toString().trim();
        String authCode = mEtAuthCode.getText().toString().trim();
        String recipient = mEtRecipient.getText().toString().trim();

        APPMSGMailUtils.saveConfig(context, server, port, sender, authCode, recipient);
        Toast.makeText(context, "SMTP配置已保存", Toast.LENGTH_SHORT).show();
        LogUtils.d(TAG, "saveConfig: SMTP配置已保存");
    }
}
