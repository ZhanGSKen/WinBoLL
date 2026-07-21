package cc.winboll.studio.appbase;

import android.content.SharedPreferences;

import cc.winboll.studio.libappbase.CrashActivity;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.libappbase.utils.CrashHandleNotifyUtils;
import cc.winboll.studio.libappbase.utils.SMTPUtils;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/01/05 09:54:42
 * @Describe 应用全局入口类（继承基础库 GlobalApplication）
 * 负责应用初始化、全局资源管理与生命周期回调处理，是整个应用的核心入口
 */
public class App extends GlobalApplication {

    /** 当前应用类的日志 TAG（用于调试输出，标识日志来源） */
    public static final String TAG = "App";

    /** SMTP配置 SharedPreferences 名称 */
    private static final String PREF_NAME = "smtp_config";

    /**
     * 应用创建时回调（全局初始化入口）
     * 在应用进程启动时执行，仅调用一次，用于初始化全局工具类、第三方库等
     */
    @Override
    public void onCreate() {
		try {
			super.onCreate();
			
			// 初始化 Toast 工具类（传入应用全局上下文，确保 Toast 可在任意地方调用）
			ToastUtils.init(getApplicationContext());
		} catch (Throwable e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.close();
            String stackTraceStr = sw.toString();
            CrashHandleNotifyUtils.handleUncaughtException(
                this,
                getPackageName(),
                stackTraceStr,
                CrashActivity.class
            );
            sendExceptionMail(stackTraceStr);
        }
    }

    /**
     * 发送异常报告邮件
     * 读取SMTP配置，若配置完整则在子线程发送异常堆栈邮件
     * @param stackTraceStr 异常堆栈信息
     */
    private void sendExceptionMail(final String stackTraceStr) {
        SharedPreferences sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        final String host = sp.getString("smtp_server", "");
        final String portStr = sp.getString("smtp_port", "");
        final String sender = sp.getString("sender_email", "");
        final String auth = sp.getString("auth_code", "");
        final String recipient = sp.getString("recipient_email", "");

        if (host.isEmpty() || portStr.isEmpty() || sender.isEmpty()
                || auth.isEmpty() || recipient.isEmpty()) {
            LogUtils.d(TAG, "sendExceptionMail skipped, SMTP config incomplete");
            return;
        }

        final int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            LogUtils.e(TAG, "sendExceptionMail failed, port format error: " + portStr);
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String deviceInfo = "\n\n--- Device Info ---\n"
                        + "Package: " + getPackageName() + "\n"
                        + "Time: " + System.currentTimeMillis() + "\n";

                boolean success = SMTPUtils.sendTextMail(
                        recipient,
                        "APPBase 异常报告",
                        stackTraceStr + deviceInfo,
                        sender,
                        auth,
                        host,
                        port);

                if (success) {
                    LogUtils.d(TAG, "sendExceptionMail success");
                } else {
                    LogUtils.e(TAG, "sendExceptionMail failed");
                }
            }
        }).start();
    }

    /**
     * 应用终止时回调（资源释放入口）
     * 仅在模拟环境（如 Android Studio 模拟器）中可靠触发，真机上可能因系统回收进程不执行
     * 用于释放全局资源，避免内存泄漏
     */
    @Override
    public void onTerminate() {
        super.onTerminate(); // 调用父类终止逻辑（如基础库资源释放）
        // 释放 Toast 工具类资源（销毁全局 Toast 实例，避免内存泄漏）
        ToastUtils.release();
    }
}
