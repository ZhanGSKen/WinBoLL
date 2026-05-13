package cc.winboll.studio.autonfc.models;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/03/16 09:38
 */
public class NfcTermuxCmd {

    private String script;        // 要执行的预制脚本名（如 auth.sh）
    private String[] args;         // 脚本参数
    private String workDir;       // 工作目录
    private boolean background;   // 是否后台执行
    private String resultDir;     // 结果输出目录（可为 null）

    public NfcTermuxCmd() {
    }

    public NfcTermuxCmd(String script, String[] args, String workDir, boolean background, String resultDir) {
        this.script = script;
        this.args = args;
        this.workDir = workDir;
        this.background = background;
        this.resultDir = resultDir;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public boolean isBackground() {
        return background;
    }

    public void setBackground(boolean background) {
        this.background = background;
    }

    public String getResultDir() {
        return resultDir;
    }

    public void setResultDir(String resultDir) {
        this.resultDir = resultDir;
    }
}

