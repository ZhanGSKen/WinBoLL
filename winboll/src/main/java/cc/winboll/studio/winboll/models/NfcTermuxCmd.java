package cc.winboll.studio.winboll.models;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/03/15 08:46
 */
public class NfcTermuxCmd {
    public String script;       // 要执行的预制脚本名（如 auth.sh）
    public String[] args;       // 脚本参数
    public String workDir;      // 工作目录
    public boolean background;  // 是否后台执行
    public String resultDir;    // 结果输出目录（可为 null）
}

