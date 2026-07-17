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
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_SMTP_SERVER, server);
        editor.putString(KEY_SMTP_PORT, port);
        editor.putString(KEY_SMTP_SENDER, sender);
        editor.putString(KEY_SMTP_AUTH_CODE, authCode);
        editor.putString(KEY_SMTP_RECIPIENT, recipient);
        editor.apply();
        LogUtils.d(TAG, "saveConfig: SMTP配置已保存");
    }

    //
    // 从SharedPreferences读取SMTP配置
    //
    public static String getServer(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_SMTP_SERVER, DEFAULT_SMTP_SERVER);
    }

    public static String getPort(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_SMTP_PORT, DEFAULT_SMTP_PORT);
    }

    public static String getSender(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_SMTP_SENDER, "");
    }

    public static String getAuthCode(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_SMTP_AUTH_CODE, "");
    }

    public static String getRecipient(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_SMTP_RECIPIENT, "");
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
        if (!isEnabled(context)) {
            LogUtils.d(TAG, "sendMail: SMTP邮件发送已禁用，跳过操作");
            return;
        }
        final String server = getServer(context);
        final String port = getPort(context);
        final String sender = getSender(context);
        final String authCode = getAuthCode(context);

        if (sender == null || sender.length() == 0) {
            showToast(context, "请先配置发件人邮箱");
            return;
        }
        if (authCode == null || authCode.length() == 0) {
            showToast(context, "请先配置邮箱授权码");
            return;
        }
        if (recipients == null || recipients.length() == 0) {
            showToast(context, "收件人不能为空");
            return;
        }

        showToast(context, "正在发送邮件...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Properties props = new Properties();
                    props.put("mail.smtp.host", server);
                    props.put("mail.smtp.port", port);
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.socketFactory.port", port);
                    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

                    Session session = Session.getInstance(props, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(sender, authCode);
                        }
                    });

                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(sender));
                    message.setRecipients(Message.RecipientType.TO,
                            InternetAddress.parse(recipients));
                    message.setSubject(subject);
                    message.setText(content);

                    Transport.send(message);

                    LogUtils.d(TAG, "sendMail: 邮件发送成功");
                    showToast(context, "邮件发送成功");
                } catch (Exception e) {
                    LogUtils.e(TAG, "sendMail: 邮件发送失败", e);
                    showToast(context, "邮件发送失败: " + e.getMessage());
                }
            }
        }).start();
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
