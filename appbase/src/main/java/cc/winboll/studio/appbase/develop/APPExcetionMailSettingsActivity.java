package cc.winboll.studio.appbase.develop;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cc.winboll.studio.appbase.R;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.utils.SMTPUtils;

/**
 * @Author 豆包&BigPickle&MiMo&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/07/21 12:44
 * @Describe 应用异常信息邮件接收账号设置
 * 配置SMTP服务器参数，用于通过邮件发送异常报告
 */
public class APPExcetionMailSettingsActivity extends Activity {

    public static final String TAG = "APPExcetionMailSettingsActivity";

    private static final String PREF_NAME = "smtp_config";
    private static final String KEY_SMTP_SERVER = "smtp_server";
    private static final String KEY_SMTP_PORT = "smtp_port";
    private static final String KEY_SENDER_EMAIL = "sender_email";
    private static final String KEY_AUTH_CODE = "auth_code";
    private static final String KEY_RECIPIENT_EMAIL = "recipient_email";

    private EditText mEtSmtpServer;
    private EditText mEtSmtpPort;
    private EditText mEtSenderEmail;
    private EditText mEtAuthCode;
    private EditText mEtRecipientEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appexcetionmailsettings);

        mEtSmtpServer = (EditText) findViewById(R.id.et_smtp_server);
        mEtSmtpPort = (EditText) findViewById(R.id.et_smtp_port);
        mEtSenderEmail = (EditText) findViewById(R.id.et_sender_email);
        mEtAuthCode = (EditText) findViewById(R.id.et_auth_code);
        mEtRecipientEmail = (EditText) findViewById(R.id.et_recipient_email);

        loadConfig();

        Button btnSave = (Button) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConfig();
            }
        });

        Button btnTest = (Button) findViewById(R.id.btn_test);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testSendMail();
            }
        });
    }

    private void loadConfig() {
        SharedPreferences sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String server = sp.getString(KEY_SMTP_SERVER, "smtp.qq.com");
        String port = sp.getString(KEY_SMTP_PORT, "465");
        String sender = sp.getString(KEY_SENDER_EMAIL, "");
        String auth = sp.getString(KEY_AUTH_CODE, "");
        String recipient = sp.getString(KEY_RECIPIENT_EMAIL, "");

        mEtSmtpServer.setText(server);
        mEtSmtpPort.setText(port);
        mEtSenderEmail.setText(sender);
        mEtAuthCode.setText(auth);
        mEtRecipientEmail.setText(recipient);
    }

    private void saveConfig() {
        String server = mEtSmtpServer.getText().toString().trim();
        String port = mEtSmtpPort.getText().toString().trim();
        String sender = mEtSenderEmail.getText().toString().trim();
        String auth = mEtAuthCode.getText().toString().trim();
        String recipient = mEtRecipientEmail.getText().toString().trim();

        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putString(KEY_SMTP_SERVER, server);
        editor.putString(KEY_SMTP_PORT, port);
        editor.putString(KEY_SENDER_EMAIL, sender);
        editor.putString(KEY_AUTH_CODE, auth);
        editor.putString(KEY_RECIPIENT_EMAIL, recipient);
        editor.apply();

        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show();
        LogUtils.d(TAG, "saveConfig success, server=" + server + ", port=" + port + ", sender=" + sender);
    }

    private void testSendMail() {
        String server = mEtSmtpServer.getText().toString().trim();
        String portStr = mEtSmtpPort.getText().toString().trim();
        String sender = mEtSenderEmail.getText().toString().trim();
        String auth = mEtAuthCode.getText().toString().trim();
        String recipient = mEtRecipientEmail.getText().toString().trim();

        if (server.isEmpty()) {
            Toast.makeText(this, "请输入SMTP服务器地址", Toast.LENGTH_SHORT).show();
            return;
        }
        if (portStr.isEmpty()) {
            Toast.makeText(this, "请输入SMTP端口", Toast.LENGTH_SHORT).show();
            return;
        }
        if (sender.isEmpty()) {
            Toast.makeText(this, "请输入发件人邮箱", Toast.LENGTH_SHORT).show();
            return;
        }
        if (auth.isEmpty()) {
            Toast.makeText(this, "请输入邮箱授权码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (recipient.isEmpty()) {
            Toast.makeText(this, "请输入收件人邮箱", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "正在发送测试邮件...", Toast.LENGTH_SHORT).show();

        final String finalRecipient = recipient;
        final String finalSender = sender;
        final String finalAuth = auth;

        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean success = SMTPUtils.sendTextMail(
                        finalRecipient,
                        "APPBase 测试邮件",
                        "这是一封来自APPBase的测试邮件。\n\n发送时间: " + System.currentTimeMillis(),
                        finalSender,
                        finalAuth);

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            Toast.makeText(APPExcetionMailSettingsActivity.this,
                                    "测试邮件发送成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(APPExcetionMailSettingsActivity.this,
                                    "测试邮件发送失败，请检查设置", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }
}
