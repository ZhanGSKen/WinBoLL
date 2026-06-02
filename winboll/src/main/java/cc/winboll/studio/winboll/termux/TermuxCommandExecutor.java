package cc.winboll.studio.winboll.termux;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.winboll.R;
import com.termux.shared.termux.TermuxConstants;
import com.termux.shared.shell.command.ExecutionCommand.Runner;
import java.util.concurrent.Executor;

/**
 * Termux 命令调用工具类（基于 RunCommandService 原型封装）
 * 用于向 Termux 发送命令执行请求，兼容 Termux RUN_COMMAND Intent 规范
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/19 16:30:00
 * @LastEditTime 2026/01/20 10:15:00
 */
public class TermuxCommandExecutor {
    private static final String TAG = "TermuxCommandExecutor";
    // 核心修复：Termux 官方包名（无 .app 后缀）
    private static final String TERMUX_PACKAGE_NAME = "com.termux";
    // Termux RunCommandService 完整类名（包名+类名）
    private static final String TERMUX_RUN_CMD_SERVICE_CLASS = "com.termux.app.RunCommandService";
    private static final String TERMUX_RUN_CMD_ACTION = TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND;
	private static final String TERMUX_HOME_PATH = "/data/data/com.termux/files/home";

    /**
     * 执行 Termux 命令（核心方法）
     * @param context 上下文（如 Activity、Service）
     * @param command 要执行的命令路径（如 "/bin/ls"、"/data/data/com.termux/files/usr/bin/bash"）
     * @param args 命令参数（如 ["-l", "/data/data/com.termux/files/home"]）
     * @param workDir 工作目录（可为 null，默认 Termux 主目录）
     * @param isBackground 是否后台执行（true=后台，false=终端会话执行）
     * @param resultDir 命令结果输出目录（可为 null，不输出到文件）
     * @return 是否成功发送命令请求
     */
    public static boolean executeCommand(Context context, String command, String[] args, String workDir, boolean isBackground, String resultDir) {
        // 1. 校验上下文和命令合法性
        if (context == null || command == null || command.isEmpty()) {
            LogUtils.e(TAG, "执行命令失败：上下文或命令为空");
            return false;
        }

        // 2. 校验 Termux 是否安装（新增：提前校验，避免白跑流程）
        if (!isTermuxInstalled(context)) {
            LogUtils.e(TAG, "执行命令失败：Termux 未安装");
            return false;
        }

        // 3. 创建 Intent 并设置目标 Service
        Intent intent = new Intent(TERMUX_RUN_CMD_ACTION);
        intent.setClassName(TERMUX_PACKAGE_NAME, TERMUX_RUN_CMD_SERVICE_CLASS); // 用正确包名
        intent.setPackage(TERMUX_PACKAGE_NAME); // 明确包名，避免歧义

        // 4. 设置核心命令参数（遵循 Termux RunCommandService 规范）
        intent.putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_COMMAND_PATH, command);
        if (args != null && args.length > 0) {
            intent.putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_ARGUMENTS, args);
            LogUtils.d(TAG, "命令参数：" + String.join(",", args));
        }
        if (workDir != null && !workDir.isEmpty()) {
            intent.putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_WORKDIR, workDir);
            LogUtils.d(TAG, "工作目录：" + workDir);
        }

        // 5. 设置执行模式（后台/终端会话）
        intent.putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_BACKGROUND, isBackground);
        String runner = isBackground ? Runner.APP_SHELL.getName() : Runner.TERMINAL_SESSION.getName();
        intent.putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_RUNNER, runner);
        LogUtils.d(TAG, "执行模式：" + (isBackground ? "后台" : "终端会话") + "，Runner：" + runner);

        // 6. 设置命令结果输出（可选，输出到文件）
        if (resultDir != null && !resultDir.isEmpty()) {
            intent.putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_RESULT_DIRECTORY, resultDir);
            intent.putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_RESULT_SINGLE_FILE, true);
            intent.putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_RESULT_FILE_BASENAME, "authcenter_cmd_result");
            LogUtils.d(TAG, "结果输出目录：" + resultDir);
        }

        // 7. 强制创建新终端会话（而非复用已有会话），避免第二次点击直接弹出旧窗口
        if (!isBackground) {
            intent.putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", 0);
        }

        // 8. 允许替换参数中的逗号替代字符
        intent.putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_REPLACE_COMMA_ALTERNATIVE_CHARS_IN_ARGUMENTS, true);

        // 8. 发送请求（区分 Android O 及以上的前台服务）
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
                LogUtils.d(TAG, "Android O+ 启动前台服务发送命令");
            } else {
                context.startService(intent);
                LogUtils.d(TAG, "启动普通服务发送命令");
            }
            LogUtils.i(TAG, "命令发送成功：command=" + command);
            return true;
        } catch (Exception e) {
            LogUtils.e(TAG, "命令发送失败：" + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 简化方法：执行 Termux 终端命令（默认工作目录，终端会话执行）
     * @param context 上下文
     * @param command 命令（如 "ls -l /home"、"echo 'hello termux'"）
     * @return 是否成功发送
     */
    public static boolean executeTerminalCommand(Context context, String command) {
        LogUtils.d(TAG, "调用 executeTerminalCommand，命令：" + command);
        if (command == null || command.isEmpty()) {
            LogUtils.e(TAG, "命令为空，执行失败");
            return false;
        }
        // 通过 bash 执行任意终端命令
        String[] args = {"-c", command};
        return executeCommand(
            context,
            "/data/data/com.termux/files/usr/bin/bash", // Termux 默认 bash 路径（正确）
            args,
            TERMUX_HOME_PATH, // 默认工作目录
            false, // 终端会话执行（可见）
            null // 不输出到文件
        );
    }

    /**
     * 简化方法：后台执行 Termux 命令（无输出文件）
     * @param context 上下文
     * @param command 命令路径
     * @param args 命令参数
     * @return 是否成功发送
     */
    public static boolean executeBackgroundCommand(Context context, String command, String[] args) {
        LogUtils.d(TAG, "调用 executeBackgroundCommand，command=" + command);
        return executeCommand(
            context,
            command,
            args,
            null,
            true, // 后台执行
            null
        );
    }

    /**
     * 校验 Termux 是否安装（修复核心错误）
     * @param context 上下文
     * @return Termux 是否已安装
     */
    public static boolean isTermuxInstalled(Context context) {
        LogUtils.d(TAG, "校验 Termux 是否安装，包名：" + TERMUX_PACKAGE_NAME);
        if (context == null) {
            LogUtils.e(TAG, "校验失败：上下文为空");
            return false;
        }
        try {
            // 用正确的 Termux 包名查询（com.termux）
            context.getPackageManager().getPackageInfo(TERMUX_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            LogUtils.d(TAG, "Termux 已安装");
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.w(TAG, "Termux 未安装：" + e.getMessage());
            return false;
        } catch (Exception e) {
            LogUtils.e(TAG, "校验 Termux 安装状态异常：" + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 校验 Termux 是否允许外部应用调用
     * @return 校验提示信息
     */
    public static String checkTermuxExternalAppPermission() {
        String tip = "请确保 Termux 已开启「允许外部应用调用」权限：\n1. 打开 Termux 输入：termux-setup-storage\n2. 编辑配置文件：echo \"allow-external-apps = true\" > ~/.termux/termux.properties\n3. 重启 Termux 生效";
        LogUtils.d(TAG, "外部应用调用权限提示：" + tip);
        return tip;
    }

    private static String pendingTargetCmd;
    private static String pendingDisplayCmd;
    private static String pendingDisplayName;

    public static boolean openTermuxBash(Context context, String command) {
        return openTermuxBash(context, null, command, "~", true);
    }

    public static boolean openTermuxBash(Context context, String command, String workDir) {
        return openTermuxBash(context, null, command, workDir, true);
    }

    public static boolean openTermuxBash(Context context, String command, String workDir, boolean keepAlive) {
        return openTermuxBash(context, null, command, workDir, keepAlive);
    }

    public static boolean openTermuxBash(Context context, String displayName, String command, String workDir, boolean keepAlive) {
        LogUtils.d(TAG, "openTermuxBash() 按钮点击，执行Gradle命令（实时输出）");

        // 1. 校验Termux是否安装
        if (!TermuxCommandExecutor.isTermuxInstalled(context)) {
            LogUtils.e(TAG, "openTermuxBash() 错误：未安装Termux应用");
            return false;
        }

        // 2. 定义核心路径（确保路径与Termux中一致）
        String projectPath = TERMUX_HOME_PATH;
        if (workDir.startsWith("~") || workDir.startsWith(".")) {
            projectPath = TERMUX_HOME_PATH + "/" + workDir.substring(1);
        }

        // 3. 构造命令（核心：用stdbuf禁用缓冲，实现实时输出）
        String targetCmd = "";
        // 步骤1：进入项目目录（不存在则创建）
        targetCmd += "cd " + projectPath + " && ";
        // 步骤2：加载环境变量
        targetCmd += "source ~/.bashrc && ";
        // 步骤3：显式配置PATH
        targetCmd += "export PATH=/data/data/com.termux/files/usr/bin:$PATH && ";
        // 步骤4：将用户输入的字面\n转换为shell命令分隔符，
        //         确保"cd ~/Sources\npwd"这类输入能分段执行
        String execCommand = command.replace("\\n", "; ");
        // 步骤5：执行设定的命令（直接由外层bash解释，避免stdbuf对shell内置命令无效）
        targetCmd += execCommand;
        // 步骤6：需要保持终端可见时追加交互式bash
        if (keepAlive) {
            targetCmd += "; stdbuf -o0 -e0 -i0 bash";
        }

        // 步骤7：指纹验证后执行命令
        if (context instanceof FragmentActivity) {
            pendingTargetCmd = targetCmd;
            pendingDisplayCmd = execCommand;
            pendingDisplayName = displayName;
            showFingerprintAndExecute((FragmentActivity) context);
            return true;
        } else {
            return TermuxCommandExecutor.executeTerminalCommand(context, targetCmd);
        }
    }

    private static void showFingerprintAndExecute(final FragmentActivity activity) {
        String displayName = pendingDisplayName != null
            ? pendingDisplayName : "";
        final StringBuilder sb = new StringBuilder();
        if (pendingDisplayCmd != null) {
            sb.append(pendingDisplayCmd);
        }
        final String cmdText = sb.toString();

        SpannableString message = new SpannableString(
            activity.getString(R.string.biometric_description,
                displayName, cmdText));
        int nameIdx = message.toString().indexOf(displayName);
        if (nameIdx >= 0 && displayName.length() > 0) {
            message.setSpan(new StyleSpan(Typeface.BOLD), nameIdx,
                nameIdx + displayName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            message.setSpan(new ForegroundColorSpan(Color.BLUE), nameIdx,
                nameIdx + displayName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        final AlertDialog dialog = new AlertDialog.Builder(activity)
            .setTitle(R.string.biometric_title)
            .setMessage(message)
            .setPositiveButton(R.string.biometric_start,
                new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startBiometricAuth(activity);
                }
            })
            .setNegativeButton(R.string.dialog_cancel,
                new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    clearPending();
                }
            })
            .setCancelable(false)
            .show();

        TextView tv = (TextView) dialog.findViewById(android.R.id.message);
        if (tv != null) {
            tv.setText(message);
        }
    }

    private static void startBiometricAuth(final FragmentActivity activity) {
        Executor executor = ContextCompat.getMainExecutor(activity);
        final BiometricPrompt biometricPrompt = new BiometricPrompt(activity,
            executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(
                    BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                executePendingCommand(activity);
            }

            @Override
            public void onAuthenticationError(int errorCode,
                    CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                clearPending();
                Toast.makeText(activity, R.string.toast_auth_failed,
                    Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        BiometricPrompt.PromptInfo promptInfo =
            new BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.biometric_title))
            .setNegativeButtonText(activity.getString(R.string.dialog_cancel))
            .setConfirmationRequired(false)
            .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private static void executePendingCommand(Context context) {
        if (pendingTargetCmd != null) {
            String cmd = pendingTargetCmd;
            clearPending();
            executeTerminalCommand(context, cmd);
        }
    }

    private static void clearPending() {
        pendingTargetCmd = null;
        pendingDisplayCmd = null;
    }
}

