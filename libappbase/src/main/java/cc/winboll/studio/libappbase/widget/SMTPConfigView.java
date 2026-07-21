package cc.winboll.studio.libappbase.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.utils.SMTPUtils;

/**
 * @Author 豆包&BigPickle&MiMo&ZhanGSKen<zhangsken@qq.com>
 * @CreateDate 2026/07/21
 * @LastEditDate 2026/07/21
 * @Describe SMTP邮件配置自定义控件
 * 封装SMTP服务器参数的输入、保存、测试发送功能
 * 可在任意Activity中直接嵌入使用
 */
public class SMTPConfigView extends LinearLayout {

    private static final String TAG = "SMTPConfigView";

    private static final String DEFAULT_PREF_NAME = "smtp_config";
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

    private String mPrefName = DEFAULT_PREF_NAME;

    private OnTestSendListener mTestSendListener;

    public interface OnTestSendListener {
        void onTestSendStart();
        void onTestSendResult(boolean success);
    }

    public SMTPConfigView(Context context) {
        super(context);
        init(context);
    }

    public SMTPConfigView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SMTPConfigView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setPrefName(String prefName) {
        mPrefName = prefName;
    }

    public void setOnTestSendListener(OnTestSendListener listener) {
        mTestSendListener = listener;
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        int padH = dp2px(context, 16);
        int padV = dp2px(context, 8);
        setPadding(padH, padV, padH, padV);

        mEtSmtpServer = createEditText(context, "smtp.qq.com", "textUri");
        mEtSmtpPort = createEditText(context, "465", "number");
        mEtSenderEmail = createEditText(context, "123456@qq.com", "textEmailAddress");
        mEtAuthCode = createEditText(context, "请输入授权码", "textPassword");
        mEtRecipientEmail = createEditText(context, "123456@qq.com", "textEmailAddress");

        addField(context, "SMTP 服务器", mEtSmtpServer);
        addField(context, "SMTP 端口", mEtSmtpPort);
        addField(context, "发件人邮箱（QQ邮箱地址）", mEtSenderEmail);
        addField(context, "邮箱授权码（非QQ密码，在QQ邮箱设置中获取）", mEtAuthCode);
        addField(context, "收件人邮箱", mEtRecipientEmail);

        Button btnSave = createButton(context, "保存设置", "#007AFF", "#FFFFFF");
        addView(btnSave);
        addVerticalSpacing(context, 12);

        Button btnTest = createButton(context, "发送测试邮件", "#FFFFFF", "#007AFF");
        addView(btnTest);
        addVerticalSpacing(context, 12);

        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConfig();
            }
        });

        btnTest.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                testSendMail();
            }
        });

        loadConfig();
    }

    private void addField(Context context, String label, EditText editText) {
        android.widget.TextView tv = new android.widget.TextView(context);
        tv.setText(label);
        tv.setTextSize(13);
        tv.setTextColor(0xFF666666);
        LayoutParams tvLp = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tvLp.bottomMargin = dp2px(context, 4);
        addView(tv, tvLp);

        LayoutParams etLp = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        etLp.bottomMargin = dp2px(context, 12);
        addView(editText, etLp);
    }

    private EditText createEditText(Context context, String hint, String inputType) {
        EditText et = new EditText(context);
        et.setHint(hint);
        et.setTextSize(15);
        et.setTextColor(0xFF000000);
        et.setHintTextColor(0xFF999999);
        et.setBackgroundColor(0xFFFFFFFF);
        int pad = dp2px(context, 12);
        et.setPadding(pad, pad, pad, pad);
        et.setInputType(getInputType(inputType));
        return et;
    }

    private int getInputType(String type) {
        if ("number".equals(type)) return android.text.InputType.TYPE_CLASS_NUMBER;
        if ("textUri".equals(type)) return android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_URI;
        if ("textEmailAddress".equals(type)) return android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
        if ("textPassword".equals(type)) return android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;
        return android.text.InputType.TYPE_CLASS_TEXT;
    }

    private Button createButton(Context context, String text, String bgColor, String textColor) {
        Button btn = new Button(context);
        btn.setText(text);
        btn.setTextSize(16);
        btn.setTextColor(android.graphics.Color.parseColor(textColor));
        btn.setBackgroundColor(android.graphics.Color.parseColor(bgColor));
        int pad = dp2px(context, 14);
        btn.setPadding(pad, pad, pad, pad);
        return btn;
    }

    private void addVerticalSpacing(Context context, int dp) {
        View spacer = new View(context);
        LayoutParams lp = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp2px(context, dp));
        addView(spacer, lp);
    }

    private int dp2px(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    public void loadConfig() {
        SharedPreferences sp = getContext().getSharedPreferences(mPrefName, Context.MODE_PRIVATE);
        mEtSmtpServer.setText(sp.getString(KEY_SMTP_SERVER, "smtp.qq.com"));
        mEtSmtpPort.setText(sp.getString(KEY_SMTP_PORT, "465"));
        mEtSenderEmail.setText(sp.getString(KEY_SENDER_EMAIL, ""));
        mEtAuthCode.setText(sp.getString(KEY_AUTH_CODE, ""));
        mEtRecipientEmail.setText(sp.getString(KEY_RECIPIENT_EMAIL, ""));
    }

    public void saveConfig() {
        String server = mEtSmtpServer.getText().toString().trim();
        String port = mEtSmtpPort.getText().toString().trim();
        String sender = mEtSenderEmail.getText().toString().trim();
        String auth = mEtAuthCode.getText().toString().trim();
        String recipient = mEtRecipientEmail.getText().toString().trim();

        SharedPreferences.Editor editor = getContext().getSharedPreferences(
                mPrefName, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_SMTP_SERVER, server);
        editor.putString(KEY_SMTP_PORT, port);
        editor.putString(KEY_SENDER_EMAIL, sender);
        editor.putString(KEY_AUTH_CODE, auth);
        editor.putString(KEY_RECIPIENT_EMAIL, recipient);
        editor.apply();

        Toast.makeText(getContext(), "设置已保存", Toast.LENGTH_SHORT).show();
        LogUtils.d(TAG, "saveConfig success, server=" + server + ", port=" + port);
    }

    public String getSmtpServer() {
        return mEtSmtpServer.getText().toString().trim();
    }

    public String getSmtpPort() {
        return mEtSmtpPort.getText().toString().trim();
    }

    public String getSenderEmail() {
        return mEtSenderEmail.getText().toString().trim();
    }

    public String getAuthCode() {
        return mEtAuthCode.getText().toString().trim();
    }

    public String getRecipientEmail() {
        return mEtRecipientEmail.getText().toString().trim();
    }

    public boolean isConfigValid() {
        return !getSmtpServer().isEmpty()
                && !getSmtpPort().isEmpty()
                && !getSenderEmail().isEmpty()
                && !getAuthCode().isEmpty()
                && !getRecipientEmail().isEmpty();
    }

    public void testSendMail() {
        if (!isConfigValid()) {
            Toast.makeText(getContext(), "请填写所有配置项", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mTestSendListener != null) {
            mTestSendListener.onTestSendStart();
        }

        Toast.makeText(getContext(), "正在发送测试邮件...", Toast.LENGTH_SHORT).show();

        final String recipient = getRecipientEmail();
        final String sender = getSenderEmail();
        final String auth = getAuthCode();
        final String host = getSmtpServer();
        final int port;
        try {
            port = Integer.parseInt(getSmtpPort());
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "端口号格式错误", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean success = SMTPUtils.sendTextMail(
                        recipient,
                        "SMTPConfigView 测试邮件",
                        "这是一封SMTPConfigView的测试邮件。\n\n发送时间: "
                                + System.currentTimeMillis(),
                        sender,
                        auth,
                        host,
                        port);

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            Toast.makeText(getContext(),
                                    "测试邮件发送成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(),
                                    "测试邮件发送失败，请检查设置", Toast.LENGTH_SHORT).show();
                        }
                        if (mTestSendListener != null) {
                            mTestSendListener.onTestSendResult(success);
                        }
                    }
                });
            }
        }).start();
    }
}
