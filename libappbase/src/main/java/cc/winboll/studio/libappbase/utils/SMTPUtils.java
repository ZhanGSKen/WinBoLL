package cc.winboll.studio.libappbase.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import javax.net.ssl.SSLSocketFactory;

import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author 豆包&BigPickle&MiMo&ZhanGSKen<zhangsken@qq.com>
 * @CreateDate 2026/07/21
 * @LastEditDate 2026/07/21
 * @Describe SMTP邮件发送工具类（原生Socket实现，零外部依赖）
 * 基于QQ SMTP服务（smtp.qq.com:465 SSL），支持纯文本和HTML邮件
 * Java7兼容标准，无高版本语法特性
 */
public final class SMTPUtils {

    private static final String TAG = "SMTPUtils";

    // QQ SMTP 服务器地址
    private static final String DEFAULT_SMTP_HOST = "smtp.qq.com";
    // QQ SMTP SSL 端口
    private static final int DEFAULT_SMTP_PORT = 465;

    // 私有构造，禁止实例化
    private SMTPUtils() {
        final String errMsg = "Cannot instantiate static class SMTPUtils";
        LogUtils.e(TAG, "constructor invoke failed, msg = " + errMsg);
        throw new AssertionError(errMsg);
    }

    /**
     * 发送纯文本邮件（QQ SMTP SSL）
     * @param to收件人邮箱地址
     * @param subject 邮件主题
     * @param body 邮件正文（纯文本）
     * @param from 发件人QQ邮箱地址
     * @param authCode QQ邮箱授权码（非QQ密码）
     * @return true=发送成功，false=发送失败
     */
    public static boolean sendTextMail(
            final String to,
            final String subject,
            final String body,
            final String from,
            final String authCode) {
        return sendMail(to, subject, body, from, authCode, false,
                DEFAULT_SMTP_HOST, DEFAULT_SMTP_PORT);
    }

    /**
     * 发送纯文本邮件（自定义SMTP服务器）
     * @param to 收件人邮箱地址
     * @param subject 邮件主题
     * @param body 邮件正文（纯文本）
     * @param from 发件人邮箱地址
     * @param authCode 邮箱授权码
     * @param host SMTP服务器地址
     * @param port SMTP端口
     * @return true=发送成功，false=发送失败
     */
    public static boolean sendTextMail(
            final String to,
            final String subject,
            final String body,
            final String from,
            final String authCode,
            final String host,
            final int port) {
        return sendMail(to, subject, body, from, authCode, false, host, port);
    }

    /**
     * 发送HTML邮件（QQ SMTP SSL）
     * @param to 收件人邮箱地址
     * @param subject 邮件主题
     * @param body 邮件正文（HTML格式）
     * @param from 发件人QQ邮箱地址
     * @param authCode QQ邮箱授权码（非QQ密码）
     * @return true=发送成功，false=发送失败
     */
    public static boolean sendHtmlMail(
            final String to,
            final String subject,
            final String body,
            final String from,
            final String authCode) {
        return sendMail(to, subject, body, from, authCode, true,
                DEFAULT_SMTP_HOST, DEFAULT_SMTP_PORT);
    }

    /**
     * 发送HTML邮件（自定义SMTP服务器）
     * @param to 收件人邮箱地址
     * @param subject 邮件主题
     * @param body 邮件正文（HTML格式）
     * @param from 发件人邮箱地址
     * @param authCode 邮箱授权码
     * @param host SMTP服务器地址
     * @param port SMTP端口
     * @return true=发送成功，false=发送失败
     */
    public static boolean sendHtmlMail(
            final String to,
            final String subject,
            final String body,
            final String from,
            final String authCode,
            final String host,
            final int port) {
        return sendMail(to, subject, body, from, authCode, true, host, port);
    }

    /**
     * 发送邮件核心方法（QQ SMTP SSL）
     * @param to 收件人邮箱地址
     * @param subject 邮件主题
     * @param body 邮件正文
     * @param from 发件人QQ邮箱地址
     * @param authCode QQ邮箱授权码
     * @param isHtml 是否HTML格式
     * @return true=发送成功，false=发送失败
     */
    private static boolean sendMail(
            final String to,
            final String subject,
            final String body,
            final String from,
            final String authCode,
            final boolean isHtml,
            final String host,
            final int port) {

        LogUtils.d(TAG, "sendMail invoke, to=" + to + ", subject=" + subject
                + ", isHtml=" + isHtml + ", host=" + host + ":" + port);

        // 参数校验
        if (to == null || to.isEmpty()) {
            LogUtils.e(TAG, "sendMail failed, reason: recipient address is empty");
            return false;
        }
        if (from == null || from.isEmpty()) {
            LogUtils.e(TAG, "sendMail failed, reason: sender address is empty");
            return false;
        }
        if (authCode == null || authCode.isEmpty()) {
            LogUtils.e(TAG, "sendMail failed, reason: auth code is empty");
            return false;
        }

        Socket socket = null;
        BufferedReader reader = null;
        OutputStream output = null;

        try {
            // 1. 创建SSL连接
            LogUtils.d(TAG, " connecting to " + host + ":" + port);
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = factory.createSocket(host, port);
            socket.setSoTimeout(10000);

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            output = socket.getOutputStream();

            // 2. 读取服务器欢迎信息
            String response = readResponse(reader);
            LogUtils.d(TAG, "S: " + response);
            if (!response.startsWith("220")) {
                LogUtils.e(TAG, "sendMail failed, SMTP server greeting error: " + response);
                return false;
            }

            // 3. EHLO 握手
            sendCommand(output, "EHLO localhost", reader);

            // 4. AUTH LOGIN
            sendCommand(output, "AUTH LOGIN", reader);

            // 5. 发送Base64编码的用户名（发件人邮箱）
            sendCommand(output, base64Encode(from), reader);

            // 6. 发送Base64编码的授权码
            sendCommand(output, base64Encode(authCode), reader);

            // 7. 设置发件人
            sendCommand(output, "MAIL FROM:<" + from + ">", reader);

            // 8. 设置收件人
            sendCommand(output, "RCPT TO:<" + to + ">", reader);

            // 9. 开始数据传输
            sendCommand(output, "DATA", reader);

            // 10. 构建邮件内容
            String contentType = isHtml ? "text/html; charset=UTF-8" : "text/plain; charset=UTF-8";
            StringBuilder mailContent = new StringBuilder();
            mailContent.append("From: ").append(from).append("\r\n");
            mailContent.append("To: ").append(to).append("\r\n");
            mailContent.append("Subject: ").append(subject).append("\r\n");
            mailContent.append("Content-Type: ").append(contentType).append("\r\n");
            mailContent.append("\r\n");
            mailContent.append(body).append("\r\n");
            mailContent.append(".\r\n");

            // 11. 发送邮件内容
            output.write(mailContent.toString().getBytes("UTF-8"));
            output.flush();
            response = readResponse(reader);
            LogUtils.d(TAG, "S: " + response);

            // 12. QUIT 退出
            sendCommand(output, "QUIT", reader);

            LogUtils.d(TAG, "sendMail success, to=" + to + ", subject=" + subject);
            return true;

        } catch (Exception e) {
            LogUtils.e(TAG, "sendMail exception: " + e.getMessage(), e);
            return false;
        } finally {
            // 关闭资源
            try {
                if (output != null) output.close();
            } catch (Exception e) {
                LogUtils.e(TAG, "close output exception: " + e.getMessage());
            }
            try {
                if (reader != null) reader.close();
            } catch (Exception e) {
                LogUtils.e(TAG, "close reader exception: " + e.getMessage());
            }
            try {
                if (socket != null) socket.close();
            } catch (Exception e) {
                LogUtils.e(TAG, "close socket exception: " + e.getMessage());
            }
        }
    }

    /**
     * 发送SMTP命令并读取多行响应
     * @param output 输出流
     * @param command SMTP命令
     * @param reader 输入流
     * @return 服务器响应字符串
     */
    private static String sendCommand(
            final OutputStream output,
            final String command,
            final BufferedReader reader) throws Exception {

        LogUtils.d(TAG, "C: " + command);
        output.write((command + "\r\n").getBytes("UTF-8"));
        output.flush();
        return readResponse(reader);
    }

    /**
     * 读取SMTP服务器多行响应
     * SMTP协议：以"数字 "开头表示最后一行，以"数字-"开头表示后续还有行
     * @param reader 输入流
     * @return 完整的服务器响应字符串
     */
    private static String readResponse(final BufferedReader reader) throws Exception {
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line).append("\n");
            // 最后一行：第4位是空格（如 "250 OK"）
            if (line.length() >= 4 && line.charAt(3) == ' ') {
                break;
            }
        }
        return response.toString().trim();
    }

    /**
     * 标准Base64编码（Java7兼容，无第三方依赖）
     * @param input 待编码字符串
     * @return Base64编码后的字符串
     */
    static String base64Encode(final String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        try {
            byte[] data = input.getBytes("UTF-8");
            final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
            StringBuilder result = new StringBuilder();
            int i;
            // 每3字节一组处理
            for (i = 0; i + 2 < data.length; i += 3) {
                int triple = ((data[i] & 0xFF) << 16)
                           | ((data[i + 1] & 0xFF) << 8)
                           | (data[i + 2] & 0xFF);
                result.append(alphabet.charAt((triple >> 18) & 0x3F));
                result.append(alphabet.charAt((triple >> 12) & 0x3F));
                result.append(alphabet.charAt((triple >> 6) & 0x3F));
                result.append(alphabet.charAt(triple & 0x3F));
            }
            // 处理剩余字节
            if (i < data.length) {
                int remaining = data[i] & 0xFF;
                result.append(alphabet.charAt((remaining >> 2) & 0x3F));
                if (i + 1 < data.length) {
                    remaining = ((remaining << 8) | (data[i + 1] & 0xFF));
                    result.append(alphabet.charAt((remaining >> 4) & 0x3F));
                    result.append(alphabet.charAt((remaining << 2) & 0x3F));
                } else {
                    result.append(alphabet.charAt((remaining << 4) & 0x3F));
                    result.append('=');
                }
            }
            return result.toString();
        } catch (Exception e) {
            LogUtils.e(TAG, "base64Encode exception: " + e.getMessage(), e);
            return "";
        }
    }
}
