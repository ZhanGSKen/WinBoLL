package cc.winboll.studio.powerbell.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.App;
import cc.winboll.studio.powerbell.services.ControlCenterService;
import cc.winboll.studio.powerbell.utils.ServiceUtils;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2024/06/06 15:01:39
 * @Describe 应用核心广播接收器
 * 功能：监听开机完成广播，实现服务开机自启
 * 适配：Java7 | API30 | 服务启动兼容性处理
 */
public class MainReceiver extends BroadcastReceiver {
    // ====================== 静态常量区（置顶归类，消除魔法值） ======================
    public static final String TAG = "MainReceiver";
    // 系统广播Action常量
    private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    // API版本常量（适配前台服务启动要求）
    private static final int API_LEVEL_26 = 26;

    // ====================== 静态状态标记（volatile保证多线程可见性） ======================
    // 历史电量值，用于校验电量变化（暂未使用，保留扩展能力）
    private static volatile int sLastBatteryLevel = -1;

    // ====================== 广播核心接收逻辑（入口方法，分Action处理） ======================
    @Override
    public void onReceive(Context context, Intent intent) {
        // 基础参数校验
        if (context == null || intent == null) {
            LogUtils.e(TAG, "onReceive: 上下文或意图为空，终止处理");
            return;
        }

        String action = intent.getAction();
        LogUtils.d(TAG, String.format("onReceive: 接收广播 | Action：%s", action));

        // 仅处理开机完成广播
        if (ACTION_BOOT_COMPLETED.equals(action)) {
            handleBootCompleted(context);
        } else {
            LogUtils.w(TAG, String.format("onReceive: 忽略未知Action：%s", action));
        }
    }

    // ====================== 业务处理方法（处理开机完成广播，实现服务自启） ======================
    /**
     * 处理开机完成广播，自动启动控制中心服务
     * @param context 上下文
     */
    private void handleBootCompleted(Context context) {
        LogUtils.d(TAG, "handleBootCompleted: 开始处理开机完成广播");
        try {
            // 1. 校验服务启用状态
            boolean isServiceEnabled = App.getAppConfigUtils(context).isServiceEnabled();
            LogUtils.d(TAG, String.format("handleBootCompleted: 服务启用状态：%b", isServiceEnabled));
            if (!isServiceEnabled) {
                LogUtils.d(TAG, "handleBootCompleted: 服务未启用，跳过自启");
                return;
            }

            // 2. 校验服务是否已运行
            String serviceClassName = ControlCenterService.class.getName();
            boolean isServiceAlive = ServiceUtils.isServiceAlive(context.getApplicationContext(), serviceClassName);
            LogUtils.d(TAG, String.format("handleBootCompleted: 服务运行状态：%b", isServiceAlive));
            if (isServiceAlive) {
                LogUtils.d(TAG, "handleBootCompleted: 服务已运行，无需重复启动");
                return;
            }

            // 3. 按API版本启动服务（适配前台服务要求）
            Intent serviceIntent = new Intent(context, ControlCenterService.class);
            if (Build.VERSION.SDK_INT >= API_LEVEL_26) {
                context.startForegroundService(serviceIntent);
                LogUtils.d(TAG, "handleBootCompleted: 启动前台服务（API >= 26）");
            } else {
                context.startService(serviceIntent);
                LogUtils.d(TAG, "handleBootCompleted: 启动普通服务（API < 26）");
            }

            LogUtils.d(TAG, "handleBootCompleted: 服务自启处理完成");
        } catch (Exception e) {
            LogUtils.e(TAG, "handleBootCompleted: 服务自启失败", e);
        }
    }
}

