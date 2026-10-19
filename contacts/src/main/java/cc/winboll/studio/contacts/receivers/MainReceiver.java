package cc.winboll.studio.contacts.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import cc.winboll.studio.contacts.services.MainService;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import java.lang.ref.WeakReference;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/02/13 06:58:04
 * @Describe 主要广播接收器，监听系统开机广播并自动启动主服务
 */
public class MainReceiver extends BroadcastReceiver {
    // ====================== 常量定义区 ======================
    public static final String TAG = "MainReceiver";
    // 监听的系统广播 Action
    private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    // ====================== 成员变量区 ======================
    // 使用弱引用关联 MainService，避免内存泄漏
    private WeakReference<MainService> mMainServiceWeakRef;

    // ====================== 构造函数区 ======================
    public MainReceiver(MainService service) {
        this.mMainServiceWeakRef = new WeakReference<>(service);
        LogUtils.d(TAG, "MainReceiver: 初始化完成，已关联 MainService 实例");
    }

    // ====================== 重写 BroadcastReceiver 核心方法 ======================
    @Override
    public void onReceive(Context context, Intent intent) {
        // 空值校验，避免空指针异常
        if (context == null) {
            LogUtils.e(TAG, "onReceive: Context 为 null，无法处理广播");
            return;
        }
        if (intent == null || intent.getAction() == null) {
            LogUtils.w(TAG, "onReceive: 接收到空 Intent 或空 Action");
            return;
        }

        String action = intent.getAction();
        LogUtils.d(TAG, "onReceive: 接收到广播 | Action=" + action);

        // 处理开机完成广播
        if (ACTION_BOOT_COMPLETED.equals(action)) {
            LogUtils.i(TAG, "onReceive: 监听到开机完成广播，自动启动 MainService");
            ToastUtils.show("设备开机，启动拨号主服务");
            MainService.startMainService(context);
        } else {
            LogUtils.i(TAG, "onReceive: 接收到未处理的广播 | Action=" + action);
            ToastUtils.show("收到广播：" + action);
        }
    }

    // ====================== 广播注册/注销方法区 ======================
    /**
     * 注册广播接收器，监听指定系统广播
     * @param context 上下文对象
     */
    public void registerAction(Context context) {
        if (context == null) {
            LogUtils.e(TAG, "registerAction: Context 为 null，注册失败");
            return;
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_BOOT_COMPLETED);
        // 可按需添加其他监听的 Action
        // intentFilter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);

        context.registerReceiver(this, intentFilter);
        LogUtils.d(TAG, "registerAction: 广播接收器注册成功 | 监听 Action=" + ACTION_BOOT_COMPLETED);
    }

    /**
     * 注销广播接收器，释放资源（解决 mMainReceiver.unregisterAction(this) 调用缺失问题）
     * @param context 上下文对象
     */
    public void unregisterAction(Context context) {
        if (context == null) {
            LogUtils.e(TAG, "unregisterAction: Context 为 null，注销失败");
            return;
        }

        try {
            context.unregisterReceiver(this);
            LogUtils.d(TAG, "unregisterAction: 广播接收器注销成功");
        } catch (IllegalArgumentException e) {
            LogUtils.w(TAG, "unregisterAction: 广播接收器未注册，无需注销", e);
        }
    }
}

