package cc.winboll.studio.winboll.unittest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.winboll.R;
import cc.winboll.studio.winboll.activities.BaseWinBoLLActivity;
import cc.winboll.studio.winboll.termux.NfcTermuxBridgeActivity;
import cc.winboll.studio.winboll.termux.TermuxCommandExecutor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/19 11:11:00
 * @LastEditTime 2026/01/21 17:45:00
 * @Describe Termux环境测试工具（跨包+sharedUserId模式）
 * 适配不同应用包名（当前包：cc.winboll.studio.winboll.beta / Termux包：com.termux）
 * 支持Termux目录读取、Gradle命令实时输出执行（唤起窗口），基于sharedUserId实现跨包权限适配
 */
public class TermuxEnvTestActivity extends BaseWinBoLLActivity {

	@Override
	public Activity getActivity() {
		return this;
	}

    // 常量属性（置顶排列）
    public static final String TAG = "TermuxEnvTestActivity";
    private static final String TERMUX_HOME_PATH = "/data/data/com.termux/files/home/TermuxWorkSpaces";
    private static final String CMD_RESULT_FILE = TERMUX_HOME_PATH + "/CMD_RESULT_FILE.log";

    // 成员属性（常量后排列）
    private Toolbar mToolbar;
    private TextView tvMessage;
    private Handler mainHandler;

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate() 调用，初始化Activity");
        setContentView(R.layout.activity_termux_env_test);
        initView();
        initToolbar();
        initTermuxDirectory();
        LogUtils.d(TAG, "onCreate() 执行完成");
    }

    /**
     * 初始化视图组件
     */
    private void initView() {
        LogUtils.d(TAG, "initView() 开始初始化视图");
        tvMessage = (TextView) findViewById(R.id.tv_message);
        mainHandler = new Handler(Looper.getMainLooper());

        // 初始化提示信息
        StringBuilder initMsg = new StringBuilder();
        initMsg.append("Termux 测试工具（跨包+sharedUserId模式）\n");
        initMsg.append("-------------------------\n");
        initMsg.append("当前应用包名：");
        initMsg.append(getPackageName());
        initMsg.append("\n");
        initMsg.append("Termux应用包名：com.termux\n");
        initMsg.append("sharedUserId：com.termux（需一致）\n");
        initMsg.append("支持功能：目录读取、Gradle命令实时输出（唤起Termux窗口）\n");
        initMsg.append("-------------------------\n");
        tvMessage.setText(initMsg.toString());

        LogUtils.d(TAG, "initView() 初始化完成，tvMessage初始值：" + initMsg.toString().trim());
    }

    /**
     * 初始化Toolbar组件
     */
    private void initToolbar() {
        LogUtils.d(TAG, "initToolbar() 开始初始化Toolbar");
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        if (mToolbar == null) {
            LogUtils.e(TAG, "initToolbar() 错误：未找到Toolbar组件");
            return;
        }

        setSupportActionBar(mToolbar);
        mToolbar.setSubtitle(getTag());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogUtils.d(TAG, "initToolbar() 导航栏返回按钮点击");
					getActivity().finish();
				}
			});

        LogUtils.d(TAG, "initToolbar() 初始化完成");
    }

    /**
     * 初始化Termux目标目录（跨包+sharedUserId模式）
     */
    private void initTermuxDirectory() {
        LogUtils.d(TAG, "initTermuxDirectory() 开始初始化Termux目录，路径：" + TERMUX_HOME_PATH);
        File termuxDir = new File(TERMUX_HOME_PATH);

        if (termuxDir.exists()) {
            LogUtils.d(TAG, "initTermuxDirectory() Termux目录已存在，无需创建");
            return;
        }

        tvMessage.append("正在创建Termux目标目录...\n");
        boolean createSuccess = termuxDir.mkdirs();
        if (createSuccess) {
            LogUtils.d(TAG, "initTermuxDirectory() Termux目录创建成功：" + TERMUX_HOME_PATH);
            tvMessage.append("Termux目录创建成功：");
            tvMessage.append(TERMUX_HOME_PATH);
            tvMessage.append("\n");
        } else {
            LogUtils.e(TAG, "initTermuxDirectory() 错误：Termux目录创建失败");
            tvMessage.append("警告：Termux目录创建失败！\n");
            tvMessage.append("请检查：\n");
            tvMessage.append("1.AndroidManifest.xml中sharedUserId是否为com.termux\n");
            tvMessage.append("2.设备是否已root（部分机型需root才能跨包写私有目录）\n");
        }
    }

    /**
     * 测试读取Termux目录（按钮点击事件）
     */
    public void onTestTermuxEnv(View view) {
        LogUtils.d(TAG, "onTestTermuxEnv() 按钮点击，开始读取Termux目录");
        tvMessage.append("\n【测试：读取Termux目录】\n");

        String fileListStr = readTermuxHomeFileList();
        tvMessage.append(fileListStr);
        tvMessage.append("\n-------------------------\n");

        LogUtils.d(TAG, "onTestTermuxEnv() 执行完成，读取结果长度：" + fileListStr.length() + "字符");
    }

    /**
     * 测试执行Gradle命令（实时输出版，唤起Termux窗口）
     */
    public void onTestTermuxGradleBuildCMD(View view) {
        LogUtils.d(TAG, "onTestTermuxCMD() 按钮点击，执行Gradle命令（实时输出）");
        tvMessage.append("\n【测试：执行Gradle命令（实时输出）】\n");

        // 1. 校验Termux是否安装
        if (!TermuxCommandExecutor.isTermuxInstalled(this)) {
            LogUtils.e(TAG, "onTestTermuxCMD() 错误：未安装Termux应用");
            tvMessage.append("错误：未安装Termux应用（包名：com.termux）\n");
            return;
        }

        // 2. 定义核心路径（确保路径与Termux中一致）
        String gradleFullPath = "/data/data/com.termux/files/home/gradle/gradle-7.5.1/bin/gradle";
        String projectPath = TERMUX_HOME_PATH + "/Sources/DebugTemp"; // 项目目录

        // 3. 构造命令（核心：用stdbuf禁用缓冲，实现实时输出）
        String targetCmd = "";
        // 步骤1：进入项目目录（不存在则创建）
        //targetCmd += "cd " + projectPath + " || (mkdir -p " + projectPath + " && cd " + projectPath + ") && ";
        targetCmd += "cd " + projectPath + " && ";
		// 步骤2：加载环境变量
        targetCmd += "source ~/.bashrc && ";
        // 步骤3：显式配置PATH
        targetCmd += "export PATH=/data/data/com.termux/files/usr/bin:/data/data/com.termux/files/home/gradle/gradle-7.5.1/bin:$PATH && ";
        // 步骤4：用stdbuf禁用stdout/stderr缓冲（关键！），执行Gradle命令
        // -o0：stdout无缓冲；-e0：stderr无缓冲；-i0：stdin无缓冲
        //targetCmd += "stdbuf -o0 -e0 -i0 " + gradleFullPath + " task --all | grep assemble && ";
        targetCmd += "stdbuf -o0 -e0 -i0 " + gradleFullPath + " -Pandroid.aapt2FromMavenOverride=/data/data/com.termux/files/home/android-sdk/build-tools/34.0.4/aapt2 assembleBetaDebug && ";
        // 步骤5：执行成功提示
        targetCmd += "echo '\n✅ 命令执行完成！' && echo '\n📌 项目目录：" + projectPath + "' && read -p '按回车键关闭终端...'";

        LogUtils.d(TAG, "onTestTermuxCMD() 执行命令：" + targetCmd);

        // 4. 执行命令（终端会话模式，唤起Termux窗口）
        boolean cmdSuccess = TermuxCommandExecutor.executeTerminalCommand(this, targetCmd);
        if (!cmdSuccess) {
            LogUtils.e(TAG, "onTestTermuxCMD() 错误：命令发送失败");
            tvMessage.append("命令发送失败！\n");
            tvMessage.append("可能原因：\n");
            tvMessage.append("1.Termux未开启外部应用调用权限（执行termux-setup-storage后配置）\n");
            tvMessage.append("2.sharedUserId配置不一致\n");
            tvMessage.append("3.Termux未安装stdbuf（执行pkg install coreutils）\n");
            return;
        }

        // 5. 应用内提示（说明实时输出特性）
        tvMessage.append("已唤起Termux窗口，执行说明：\n");
        tvMessage.append("1. 自动创建/进入项目目录：");
        tvMessage.append(projectPath);
        tvMessage.append("\n");
        tvMessage.append("2. 禁用输出缓冲（stdbuf），实现Gradle实时输出\n");
        tvMessage.append("3. 筛选assemble相关任务（grep assemble）\n");
        tvMessage.append("⚙️ Gradle路径：");
        tvMessage.append(gradleFullPath);
        tvMessage.append("\n");
        tvMessage.append("💡 若未实时输出，请在Termux中执行：pkg install coreutils（安装stdbuf）\n");
        tvMessage.append("-------------------------\n");
    }

	public void onTestWinBoLLProjectBuild(View view) {
		ToastUtils.show("onTestWinBoLLProjectBuild");
		NfcTermuxBridgeActivity.testCommand(this);
	}
	
//	public void onTestWinBoLLProjectBuildView(View view) {
//		ToastUtils.show("onTestWinBoLLProjectBuildView");
//		NfcTermuxBridgeActivity.testViewCommand(this);
//	}

	public void onOpenTermuxBash(View view) {
        LogUtils.d(TAG, "onTestTermuxCMD() 按钮点击，执行Gradle命令（实时输出）");
        tvMessage.append("\n【测试：执行Gradle命令（实时输出）】\n");

        // 1. 校验Termux是否安装
        if (!TermuxCommandExecutor.isTermuxInstalled(this)) {
            LogUtils.e(TAG, "onTestTermuxCMD() 错误：未安装Termux应用");
            tvMessage.append("错误：未安装Termux应用（包名：com.termux）\n");
            return;
        }

        // 2. 定义核心路径（确保路径与Termux中一致）
        String gradleFullPath = "/data/data/com.termux/files/home/gradle/gradle-7.5.1/bin/gradle";
        String projectPath = TERMUX_HOME_PATH + "/"; // 项目目录

        // 3. 构造命令（核心：用stdbuf禁用缓冲，实现实时输出）
        String targetCmd = "";
        // 步骤1：进入项目目录（不存在则创建）
        targetCmd += "cd " + projectPath + " && ";
        // 步骤2：加载环境变量
        targetCmd += "source ~/.bashrc && ";
        // 步骤3：显式配置PATH
        targetCmd += "export PATH=/data/data/com.termux/files/usr/bin:/data/data/com.termux/files/home/gradle/gradle-7.5.1/bin:$PATH && ";
        // 步骤4：用stdbuf禁用stdout/stderr缓冲（关键！），执行Gradle命令
        // -o0：stdout无缓冲；-e0：stderr无缓冲；-i0：stdin无缓冲
        //targetCmd += "stdbuf -o0 -e0 -i0 " + gradleFullPath + " task --all | grep assemble && ";
        //targetCmd += "stdbuf -o0 -e0 -i0 " + gradleFullPath + " -Pandroid.aapt2FromMavenOverride=/data/data/com.termux/files/home/android-sdk/build-tools/34.0.4/aapt2 assembleBetaDebug && ";
        targetCmd += "stdbuf -o0 -e0 -i0 bash && ";
        // 步骤5：执行成功提示
        targetCmd += "echo '\n✅ 命令执行完成！' && echo '\n📌 项目目录：" + projectPath + "' && read -p '按回车键关闭终端...'";

        LogUtils.d(TAG, "onTestTermuxCMD() 执行命令：" + targetCmd);

        // 4. 执行命令（终端会话模式，唤起Termux窗口）
        boolean cmdSuccess = TermuxCommandExecutor.executeTerminalCommand(this, targetCmd);
        if (!cmdSuccess) {
            LogUtils.e(TAG, "onTestTermuxCMD() 错误：命令发送失败");
            tvMessage.append("命令发送失败！\n");
            tvMessage.append("可能原因：\n");
            tvMessage.append("1.Termux未开启外部应用调用权限（执行termux-setup-storage后配置）\n");
            tvMessage.append("2.sharedUserId配置不一致\n");
            tvMessage.append("3.Termux未安装stdbuf（执行pkg install coreutils）\n");
            return;
        }

        // 5. 应用内提示（说明实时输出特性）
        tvMessage.append("已唤起Termux窗口，执行说明：\n");
        tvMessage.append("1. 自动创建/进入项目目录：");
        tvMessage.append(projectPath);
        tvMessage.append("\n");
        tvMessage.append("2. 禁用输出缓冲（stdbuf），实现Gradle实时输出\n");
        tvMessage.append("3. 筛选assemble相关任务（grep assemble）\n");
        tvMessage.append("⚙️ Gradle路径：");
        tvMessage.append(gradleFullPath);
        tvMessage.append("\n");
        tvMessage.append("💡 若未实时输出，请在Termux中执行：pkg install coreutils（安装stdbuf）\n");
        tvMessage.append("-------------------------\n");
    }

    /**
     * 跨包读取Termux命令结果文件（保留原功能，兼容其他场景）
     */
    private String readCmdResultDirectly() {
        LogUtils.d(TAG, "readCmdResultDirectly() 开始读取命令结果，文件路径：" + CMD_RESULT_FILE);
        File resultFile = new File(CMD_RESULT_FILE);

        // 校验文件存在性
        if (!resultFile.exists()) {
            LogUtils.e(TAG, "readCmdResultDirectly() 错误：结果文件不存在");
            return "错误：结果文件不存在\n可能原因：\n1.命令执行失败\n2.延迟时间不足\n3.跨包写权限被拒绝（需root）";
        }

        // 校验文件可读性
        if (!resultFile.canRead()) {
            LogUtils.e(TAG, "readCmdResultDirectly() 错误：无结果文件读取权限");
            return "错误：无结果文件读取权限（sharedUserId配置错误或未root）";
        }

        // 读取文件内容
        StringBuilder result = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(resultFile), StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                result.append(line);
                result.append("\n");
            }
            LogUtils.d(TAG, "readCmdResultDirectly() 文件读取完成，结果长度：" + result.length() + "字符");
        } catch (IOException e) {
            LogUtils.e(TAG, "readCmdResultDirectly() 错误：文件读取异常", e);
            return "错误：读取文件异常 → " + e.getMessage();
        } finally {
            if (br != null) {
                try {
                    br.close();
                    LogUtils.d(TAG, "readCmdResultDirectly() BufferedReader资源已关闭");
                } catch (IOException e) {
                    LogUtils.e(TAG, "readCmdResultDirectly() 错误：BufferedReader关闭异常", e);
                }
            }
        }

        String resultStr = result.toString().trim();
        return resultStr.isEmpty() ? "命令执行成功，但无输出" : resultStr;
    }

    /**
     * 删除命令结果临时文件（保留原功能）
     */
    private void deleteCmdResultFile() {
        LogUtils.d(TAG, "deleteCmdResultFile() 开始删除临时文件，路径：" + CMD_RESULT_FILE);
        File resultFile = new File(CMD_RESULT_FILE);

        if (!resultFile.exists()) {
            LogUtils.d(TAG, "deleteCmdResultFile() 临时文件不存在，无需删除");
            return;
        }

        if (resultFile.canWrite()) {
            boolean deleteSuccess = resultFile.delete();
            if (deleteSuccess) {
                LogUtils.d(TAG, "deleteCmdResultFile() 临时文件删除成功");
            } else {
                LogUtils.w(TAG, "deleteCmdResultFile() 警告：临时文件删除失败");
                tvMessage.append("警告：临时结果文件删除失败\n");
            }
        } else {
            LogUtils.w(TAG, "deleteCmdResultFile() 警告：无临时文件删除权限");
        }
    }

    /**
     * 跨包读取Termux目录文件列表（保留原功能）
     */
    private String readTermuxHomeFileList() {
        LogUtils.d(TAG, "readTermuxHomeFileList() 开始读取目录，路径：" + TERMUX_HOME_PATH);
        File termuxHomeDir = new File(TERMUX_HOME_PATH);
        StringBuilder result = new StringBuilder();

        // 基础校验
        if (!termuxHomeDir.exists()) {
            LogUtils.e(TAG, "readTermuxHomeFileList() 错误：目录不存在");
            return "错误：Termux目录不存在 → " + TERMUX_HOME_PATH;
        }
        if (!termuxHomeDir.isDirectory()) {
            LogUtils.e(TAG, "readTermuxHomeFileList() 错误：指定路径不是目录");
            return "错误：指定路径不是目录 → " + TERMUX_HOME_PATH;
        }
        if (!termuxHomeDir.canRead()) {
            LogUtils.e(TAG, "readTermuxHomeFileList() 错误：无目录读取权限");
            return "错误：无目录读取权限（需满足：1.sharedUserId=com.termux 2.设备root）";
        }

        // 获取文件列表
        File[] files = termuxHomeDir.listFiles();
        if (files == null || files.length == 0) {
            LogUtils.d(TAG, "readTermuxHomeFileList() 目录为空");
            return "Termux目录为空 → " + TERMUX_HOME_PATH;
        }

        // 拼接结果字符串
        result.append("Termux主目录：");
        result.append(TERMUX_HOME_PATH);
        result.append("\n");
        result.append("文件/子目录总数：");
        result.append(files.length);
        result.append("\n");
        result.append("目录权限：");
        result.append("读：");
        result.append(termuxHomeDir.canRead());
        result.append(" | 写：");
        result.append(termuxHomeDir.canWrite());
        result.append("\n");
        result.append("-------------------------\n");

        for (File file : files) {
            String fileType = file.isDirectory() ? "[目录]" : "[文件]";
            String fileName = file.getName();
            String filePath = file.getAbsolutePath();
            String fileSize = file.isFile() ? " | 大小：" + formatFileSize(file.length()) : "";
            String filePerm = " | 权限：r:" + file.canRead() + "/w:" + file.canWrite();

            result.append(fileType);
            result.append("  ");
            result.append(fileName);
            result.append(fileSize);
            result.append(filePerm);
            result.append(" → ");
            result.append(filePath);
            result.append("\n");
        }

        LogUtils.d(TAG, "readTermuxHomeFileList() 读取完成，结果长度：" + result.length() + "字符");
        return result.toString().trim();
    }
    /**
     * 格式化文件大小（B→KB→MB）
     */
    private String formatFileSize(long length) {
        LogUtils.d(TAG, "formatFileSize() 调用，文件长度：" + length + "B");
        String sizeStr;
        if (length < 1024) {
            sizeStr = length + "B";
        } else if (length < 1024 * 1024) {
            sizeStr = String.format("%.1fKB", length / 1024.0);
        } else {
            sizeStr = String.format("%.1fMB", length / (1024.0 * 1024));
        }
        LogUtils.d(TAG, "formatFileSize() 格式化结果：" + sizeStr);
        return sizeStr;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy() 调用，清理资源");
        deleteCmdResultFile();
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
            LogUtils.d(TAG, "onDestroy() Handler资源已释放");
        }
        LogUtils.d(TAG, "onDestroy() 执行完成");
    }
}
