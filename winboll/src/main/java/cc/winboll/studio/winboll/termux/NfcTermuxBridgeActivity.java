/*
 * 源码说明与描述：
 * NFC 与 Termux 桥接活动，用于接收外部应用（包调用）传递的 JSON 指令并执行 Termux 脚本命令。
 * 支持 ACTION_BUILD（后台执行）与 ACTION_BUILD_VIEW（终端窗口唤起）两种动作。
 *
 * 作者：豆包&ZhanGSKen<zhangsken@qq.com>
 * 创建时间：2025-03-15 14:00:00
 * 最后编辑时间：2026-03-16 10:00:00
 */
package cc.winboll.studio.winboll.termux;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.winboll.models.NfcTermuxCmd;
import com.google.gson.Gson;

public class NfcTermuxBridgeActivity extends Activity {

    // ========================= 常量与静态属性 =========================
    public static final String TAG = "NfcTermuxBridgeActivity";

    // 外部应用调用时使用的 Action 常量
    public static final String ACTION_BUILD = "cc.winboll.studio.winboll.termux.NfcTermuxBridgeActivity.ACTION_BUILD";

    private static final Gson GSON = new Gson();

    // ========================= 生命周期方法 =========================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate() 调用，savedInstanceState: " + (savedInstanceState != null ? "非空" : "空"));
        dispatchIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtils.d(TAG, "onNewIntent() 调用，intent: " + (intent != null ? intent.toString() : "null"));
        if (intent != null) {
            LogUtils.d(TAG, "onNewIntent() action: " + intent.getAction());
            LogUtils.d(TAG, "onNewIntent() data: " + intent.getDataString());
            LogUtils.d(TAG, "onNewIntent() extras: " + intent.getExtras());
            LogUtils.d(TAG, "onNewIntent() flags: " + intent.getFlags());
            LogUtils.d(TAG, "onNewIntent() component: " + intent.getComponent());
        } else {
            LogUtils.w(TAG, "onNewIntent() intent is null");
        }
        dispatchIntent(intent);
    }

    // ========================= 统一 Intent 分发（合并去重） =========================
    /**
     * 统一分发 Intent，根据 Action 路由到不同业务逻辑
     * @param intent 外部传入的 Intent
     */
    private void dispatchIntent(Intent intent) {
        LogUtils.d(TAG, "dispatchIntent() 分发 intent");
        if (intent == null) {
            LogUtils.w(TAG, "dispatchIntent() intent is null");
            return;
        }

        String action = intent.getAction();
        if (ACTION_BUILD.equals(action)) {
			ToastUtils.show("ACTION_BUILD 命中");
            onOpenTermuxProjectBuild(intent);
        } else {
            LogUtils.w(TAG, "dispatchIntent() 未知 Action: " + action);
            finish();
        }
    }

    // ========================= 核心业务方法 =========================
    /**
     * 处理 ACTION_BUILD 动作：后台执行 NFC 指令
     */
//    private void handleNfcIntent(Intent intent) {
//        LogUtils.d(TAG, "handleNfcIntent() 调用");
//        if (intent == null) {
//            LogUtils.w(TAG, "handleNfcIntent() intent 为空");
//            return;
//        }
//
//        try {
//            String json = intent.getStringExtra(Intent.EXTRA_TEXT);
//            LogUtils.d(TAG, "handleNfcIntent() json: " + json);
//
//            if (json == null || json.isEmpty()) {
//                LogUtils.e(TAG, "handleNfcIntent() 指令为空");
//                showToast("指令为空");
//                finish();
//                return;
//            }
//
//            NfcTermuxCmd cmd = GSON.fromJson(json, NfcTermuxCmd.class);
//            LogUtils.d(TAG, "handleNfcIntent() cmd: " + cmd);
//
//            if (cmd.script == null || cmd.script.isEmpty()) {
//                LogUtils.e(TAG, "handleNfcIntent() script 为空");
//                showToast("script 不能为空");
//                finish();
//                return;
//            }
//
//            //String scriptPath = "/data/data/com.termux/files/home/TermuxWorkSpaces/BashShells/AutoNFC/" + cmd.script;
//            String scriptPath = "/data/data/com.termux/files/home/TermuxWorkSpaces/BashShells/AutoNFC/" + "BuildWinBoLLProject.sh";
//            LogUtils.d(TAG, "handleNfcIntent() 脚本路径: " + scriptPath);
//
//            boolean success = TermuxCommandExecutor.executeCommand(
//                this, scriptPath, cmd.args, cmd.workDir, cmd.background, cmd.resultDir
//            );
//            LogUtils.d(TAG, "handleNfcIntent() 执行结果: " + success);
//
//            if (success) {
//                showToast("指令已发送: " + cmd.script);
//                LogUtils.i(TAG, "执行成功: " + scriptPath);
//            } else {
//                showToast("指令发送失败");
//                LogUtils.e(TAG, "执行失败");
//            }
//
//        } catch (Exception e) {
//            LogUtils.e(TAG, "handleNfcIntent() 异常: " + e.getMessage(), e);
//            showToast("解析失败");
//        } finally {
//            finish();
//        }
//    }

    /**
     * 处理 ACTION_BUILD_VIEW 动作：唤起 Termux 窗口执行命令
     */
    public void onOpenTermuxProjectBuild(Intent intent) {
        LogUtils.d(TAG, "onOpenTermuxProjectBuildView() 调用");
        if (intent == null) {
            LogUtils.w(TAG, "onOpenTermuxProjectBuildView() intent 为空");
            return;
        }

        try {
            String json = intent.getStringExtra(Intent.EXTRA_TEXT);
            LogUtils.d(TAG, "onOpenTermuxProjectBuildView() json: " + json);

            if (json == null || json.isEmpty()) {
                LogUtils.e(TAG, "onOpenTermuxProjectBuildView() 指令为空");
                showToast("指令为空");
                finish();
                return;
            }

            NfcTermuxCmd cmd = GSON.fromJson(json, NfcTermuxCmd.class);
            LogUtils.d(TAG, "onOpenTermuxProjectBuildView() cmd: " + cmd);

            if (cmd.script == null || cmd.script.isEmpty()) {
                LogUtils.e(TAG, "onOpenTermuxProjectBuildView() script 为空");
                showToast("script 不能为空");
                finish();
                return;
            }

            StringBuilder targetCmd = new StringBuilder();
            String nfcScriptFolder = "/data/data/com.termux/files/home/TermuxWorkSpaces/BashShells/AutoNFC/";
            targetCmd.append("cd " + nfcScriptFolder + " && ");
            //targetCmd.append("stdbuf -o0 -e0 -i0 bash ").append(cmd.script).append(" ");
            targetCmd.append("stdbuf -o0 -e0 -i0 bash ").append("BuildWinBoLLProject.sh").append(" ");
            if (cmd.args != null) {
                for (String arg : cmd.args) {
                    targetCmd.append(arg).append(" ");
                }
            }

            LogUtils.d(TAG, "onOpenTermuxProjectBuildView() 命令: " + targetCmd);
            boolean cmdSuccess = TermuxCommandExecutor.executeTerminalCommand(this, targetCmd.toString());
            LogUtils.d(TAG, "onOpenTermuxProjectBuildView() 执行结果: " + cmdSuccess);

            if (cmdSuccess) {
                showToast("指令已发送: " + cmd.script);
            } else {
                showToast("指令发送失败");
            }

        } catch (Exception e) {
            LogUtils.e(TAG, "onOpenTermuxProjectBuildView() 异常: " + e.getMessage(), e);
            showToast("解析失败");
        } finally {
            finish();
        }
    }

    // ========================= 公共静态测试方法 =========================
    /**
     * 内部测试方法：发送 ACTION_BUILD 指令
     */
    public static void testCommand(Context context) {
        LogUtils.d(TAG, "testCommand()");
        try {
            String testJson = "{\"script\":\"BuildWinBoLLProject.sh\",\"args\":[\"DebugTemp\"],\"workDir\":null,\"background\":true,\"resultDir\":null}";
            Intent intent = new Intent(context, NfcTermuxBridgeActivity.class);
            intent.setAction(ACTION_BUILD); // 指定 Action
            intent.putExtra(Intent.EXTRA_TEXT, testJson);
            context.startActivity(intent);
        } catch (Exception e) {
            LogUtils.e(TAG, "testCommand() 失败: " + e.getMessage());
        }
    }

    /**
     * 内部测试方法：发送 ACTION_BUILD_VIEW 指令
     */
//    public static void testViewCommand(Context context) {
//        LogUtils.d(TAG, "testViewCommand()");
//        try {
//            String testJson = "{\"script\":\"BuildWinBoLLProjectView.sh\",\"args\":[\"DebugTemp\"],\"workDir\":null,\"background\":true,\"resultDir\":null}";
//            Intent intent = new Intent(context, NfcTermuxBridgeActivity.class);
//            intent.setAction(ACTION_BUILD_VIEW); // 指定 Action
//            intent.putExtra(Intent.EXTRA_TEXT, testJson);
//            context.startActivity(intent);
//        } catch (Exception e) {
//            LogUtils.e(TAG, "testViewCommand() 失败: " + e.getMessage());
//        }
//    }

    // ========================= 工具方法 =========================
    /**
     * 统一显示 Toast，确保在主线程调用
     */
    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(NfcTermuxBridgeActivity.this, message, Toast.LENGTH_SHORT).show();
				}
			});
    }
}

