package cc.winboll.studio.libaes.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import cc.winboll.studio.libappbase.LogUtils;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/07/16
 * @Describe APPMSG邮件发送工具类，使用QQ邮件SMTP服务发送邮件
 */
public class APPMSGMailUtils {

    public static final String TAG = "APPMSGMailUtils";

    //
    // SharedPreferences 配置
    //
    private static final String SP_NAME = "appmsg_mail_config";
    private static final String KEY_SMTP_SERVER = "smtp_server";
    private static final String KEY_SMTP_PORT = "smtp_port";
    private static final String KEY_SMTP_SENDER = "smtp_sender";
    private static final String KEY_SMTP_AUTH_CODE = "smtp_auth_code";
    private static final String KEY_SMTP_RECIPIENT = "smtp_recipient";
    private static final String KEY_SMTP_ENABLED = "smtp_enabled";

    private static final String DEFAULT_SMTP_SERVER = "smtp.qq.com";
    private static final String DEFAULT_SMTP_PORT = "465";

    //
    // 保存SMTP配置到SharedPreferences
    //
    public static void saveConfig(Context context, String server, String port,
            String sender, String authCode, String recipient) {
        LogUtils.d(TAG, "saveConfig: 开始保存SMTP配置");
        LogUtils.d(TAG, "saveConfig: server=" + server + ", port=" + port);
        LogUtils.d(TAG, "saveConfig: sender=" + sender + ", authCode长度=" + (authCode != null ? authCode.length() : 0));
        LogUtils.d(TAG, "saveConfig: recipient=" + recipient);
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_SMTP_SERVER, server);
        editor.putString(KEY_SMTP_PORT, port);
        editor.putString(KEY_SMTP_SENDER, sender);
        editor.putString(KEY_SMTP_AUTH_CODE, authCode);
        editor.putString(KEY_SMTP_RECIPIENT, recipient);
        editor.apply();
        LogUtils.d(TAG, "saveConfig: SMTP配置已保存完成");
    }

    //
    // 从SharedPreferences读取SMTP配置
    //
    public static String getServer(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String value = sp.getString(KEY_SMTP_SERVER, DEFAULT_SMTP_SERVER);
        LogUtils.d(TAG, "getServer: " + value);
        return value;
    }

    public static String getPort(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String value = sp.getString(KEY_SMTP_PORT, DEFAULT_SMTP_PORT);
        LogUtils.d(TAG, "getPort: " + value);
        return value;
    }

    public static String getSender(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String value = sp.getString(KEY_SMTP_SENDER, "");
        LogUtils.d(TAG, "getSender: " + value);
        return value;
    }

    public static String getAuthCode(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String value = sp.getString(KEY_SMTP_AUTH_CODE, "");
        LogUtils.d(TAG, "getAuthCode: 长度=" + (value != null ? value.length() : 0));
        return value;
    }

    public static String getRecipient(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String value = sp.getString(KEY_SMTP_RECIPIENT, "");
        LogUtils.d(TAG, "getRecipient: " + value);
        return value;
    }

    //
    // SMTP邮件发送开关
    //
    public static void setEnabled(Context context, boolean enabled) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(KEY_SMTP_ENABLED, enabled);
        editor.apply();
        LogUtils.d(TAG, "setEnabled: " + enabled);
    }

    public static boolean isEnabled(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_SMTP_ENABLED, false);
    }

    //
    // 发送邮件（异步，在后台线程执行）
    //
    public static void sendMail(final Context context, final String subject,
            final String content, final String recipients) {
        LogUtils.d(TAG, "sendMail: ====== 开始调用sendMail ======");
        LogUtils.d(TAG, "sendMail: subject=" + subject);
        LogUtils.d(TAG, "sendMail: content长度=" + (content != null ? content.length() : 0));
        LogUtils.d(TAG, "sendMail: recipients=" + recipients);
        boolean enabled = isEnabled(context);
        LogUtils.d(TAG, "sendMail: isEnabled=" + enabled);
        if (!enabled) {
            LogUtils.d(TAG, "sendMail: SMTP邮件发送已禁用，跳过操作");
            return;
        }
        final String server = getServer(context);
        final String port = getPort(context);
        final String sender = getSender(context);
        final String authCode = getAuthCode(context);
        LogUtils.d(TAG, "sendMail: server=" + server + ", port=" + port);
        LogUtils.d(TAG, "sendMail: sender=" + sender + ", authCode长度=" + (authCode != null ? authCode.length() : 0));
        LogUtils.d(TAG, "sendMail: recipients=" + recipients);

        if (sender == null || sender.length() == 0) {
            LogUtils.w(TAG, "sendMail: 请先配置发件人邮箱");
            showToast(context, "请先配置发件人邮箱");
            return;
        }
        if (authCode == null || authCode.length() == 0) {
            LogUtils.w(TAG, "sendMail: 请先配置邮箱授权码");
            showToast(context, "请先配置邮箱授权码");
            return;
        }
        if (recipients == null || recipients.length() == 0) {
            LogUtils.w(TAG, "sendMail: 收件人不能为空");
            showToast(context, "收件人不能为空");
            return;
        }

        LogUtils.d(TAG, "sendMail: 参数校验通过，准备启动发送线程");
        showToast(context, "正在发送邮件...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtils.d(TAG, "sendMail: 发送线程已启动");
                try {
                    LogUtils.d(TAG, "sendMail: 创建邮件属性配置");
                    Properties props = new Properties();
                    props.put("mail.smtp.host", server);
                    props.put("mail.smtp.port", port);
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.socketFactory.port", port);
                    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

                    LogUtils.d(TAG, "sendMail: 创建邮件会话");
                    Session session = Session.getInstance(props, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            LogUtils.d(TAG, "sendMail: 身份验证回调触发");
                            return new PasswordAuthentication(sender, authCode);
                        }
                    });

                    LogUtils.d(TAG, "sendMail: 创建MimeMessage");
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(sender));
                    message.setRecipients(Message.RecipientType.TO,
                            InternetAddress.parse(recipients));
                    message.setSubject(subject);
                    message.setText(content);

                    LogUtils.d(TAG, "sendMail: 调用Transport.send开始发送");
                    Transport.send(message);

                    LogUtils.d(TAG, "sendMail: 邮件发送成功");
                    showToast(context, "邮件发送成功");
                } catch (Exception e) {
                    LogUtils.e(TAG, "sendMail: 邮件发送异常", e);
                    showToast(context, "邮件发送失败: " + e.getMessage());
                }
            }
        }).start();
        LogUtils.d(TAG, "sendMail: ====== sendMail调用结束 ======");
    }

    //
    // 在主线程显示Toast提示
    //
    private static void showToast(final Context context, final String message) {
        if (context == null || message == null) {
            return;
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context.getApplicationContext(),
                        message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
