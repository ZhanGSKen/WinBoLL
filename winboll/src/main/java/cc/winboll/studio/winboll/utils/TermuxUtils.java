package cc.winboll.studio.winboll.utils;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2025/06/08 09:05
 * @Describe Termux 应用操作工具集
 */
import android.content.Intent;

public abstract class TermuxUtils {

    public static final String TAG = "TermuxUtils";

    private void runTermuxCommand(String command) {
        // 1. 创建 Intent，指定 Termux 的 RunCommandService
        /*Intent intent = new Intent("com.termux.RUN_COMMAND");
        intent.setPackage("com.termux"); // Termux 应用的包名

        // 2. 传递命令参数（必填）
        //intent.putExtra("command", command); 
        intent.putExtra("cd ~/WinBoLL&&echo 'WinBoLL cmd exec at (data", command); 

        // 3. 可选：设置工作目录（默认为 Termux 的 home 目录）
        intent.putExtra("dir", "/data/data/com.termux/files/home/WinBoLL"); 

        // 4. 发送 Intent（需处理可能的安全异常或 ActivityNotFoundException）
        try {
            getApplicationContext().startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}
