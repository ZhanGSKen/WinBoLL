package cc.winboll.studio.powerbell.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.App;
import cc.winboll.studio.powerbell.utils.AppConfigUtils;
import cc.winboll.studio.powerbell.utils.ServiceUtils;

/**
 * 电池提醒核心服务进程守护类
 * 功能：监听主服务 {@link ControlCenterService} 存活状态，异常断开时自动重启并绑定
 * 适配：Java7 | API30 | 前台服务启动规则 | 服务绑定稳定性保障
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Describe 守护服务：保障ControlCenterService持续运行
 */
public class AssistantService extends Service {
    // ====================== 静态常量区（置顶归类，消除魔法值） ======================
    private static final String TAG = "AssistantService";
    // 服务返回策略常量
    private static final int SERVICE_RETURN_STICKY = START_STICKY;
    // 服务绑定标记常量
    private static final int BIND_FLAG = Context.BIND_IMPORTANT;
    // API版本常量（适配前台服务启动要求）
    private static final int API_LEVEL_26 = Build.VERSION_CODES.O;

    // ====================== 成员变量区（按功能分层，volatile保证多线程可见性） ======================
    private AppConfigUtils mAppConfigUtils;
    private MyServiceConnection mMyServiceConnection;
    private volatile boolean mIsThreadAlive;

    // ====================== 内部类（服务连接状态监听，前置定义便于引用） ======================
    /**
     * 服务连接状态监听器
     * 主服务连接成功时记录状态，断开时自动重连
     */
    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            String className = name != null ? name.getClassName() : "null";
            LogUtils.d(TAG, String.format("onServiceConnected: 主服务连接成功 | 组件名=%s | Binder=%s", className, service));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            String className = name != null ? name.getClassName() : "null";
            LogUtils.d(TAG, String.format("onServiceDisconnected: 主服务连接断开 | 组件名=%s", className));
            // 主服务断开且配置启用时，重新唤醒绑定
            if (mAppConfigUtils != null && mAppConfigUtils.isServiceEnabled()) {
                LogUtils.d(TAG, "onServiceDisconnected: 配置启用，尝试重新唤醒并绑定主服务");
                wakeupAndBindMain();
            }
        }
    }

    // ====================== 服务生命周期方法（按执行顺序排列：onCreate→onStartCommand→onBind→onDestroy） ======================
    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, String.format("onCreate: 守护服务启动 | 进程ID=%d", android.os.Process.myPid()));

        // 初始化配置工具类，添加空指针防护
        mAppConfigUtils = App.getAppConfigUtils(this);
        if (mAppConfigUtils == null) {
            LogUtils.e(TAG, "onCreate: AppConfigUtils初始化失败，守护服务无法工作");
            stopSelf();
            return;
        }

        // 初始化服务连接对象
        if (mMyServiceConnection == null) {
            mMyServiceConnection = new MyServiceConnection();
            LogUtils.d(TAG, "onCreate: ServiceConnection初始化完成");
        }

        // 初始化运行状态，执行核心守护逻辑
        mIsThreadAlive = false;
        run();
        LogUtils.d(TAG, String.format("onCreate: 守护服务初始化完成 | 服务启用状态=%b", mAppConfigUtils.isServiceEnabled()));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, String.format("onStartCommand: 守护服务触发重启 | flags=%d | startId=%d", flags, startId));
        // 配置工具类为空时，直接返回非粘性策略
        if (mAppConfigUtils == null) {
            LogUtils.e(TAG, "onStartCommand: AppConfigUtils未初始化，终止服务");
            stopSelf();
            return START_NOT_STICKY;
        }

        run();
        int returnFlag = mAppConfigUtils.isServiceEnabled() ? SERVICE_RETURN_STICKY : super.onStartCommand(intent, flags, startId);
        LogUtils.d(TAG, String.format("onStartCommand: 处理完成 | 返回策略=%s", returnFlag == SERVICE_RETURN_STICKY ? "START_STICKY" : "DEFAULT"));
        return returnFlag;
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.d(TAG, String.format("onBind: 服务绑定请求 | intent=%s", intent));
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy: 守护服务销毁流程启动");

        // 重置运行状态，终止守护逻辑
        mIsThreadAlive = false;

        // 解绑主服务，添加异常捕获防止重复解绑崩溃
        unbindMainService();

        // 置空工具类引用，帮助GC回收
        mAppConfigUtils = null;
        LogUtils.d(TAG, "onDestroy: 守护服务销毁完成");
    }

    // ====================== 核心业务逻辑（守护主服务存活） ======================
    /**
     * 执行守护逻辑：检查主服务状态，按需唤醒并绑定
     * 前置条件：mAppConfigUtils 必须初始化完成
     */
    private void run() {
        boolean isServiceEnabled = mAppConfigUtils.isServiceEnabled();
        LogUtils.d(TAG, String.format("run: 执行守护逻辑 | 配置启用=%b | 线程存活=%b", isServiceEnabled, mIsThreadAlive));
        if (isServiceEnabled) {
            if (!mIsThreadAlive) {
                mIsThreadAlive = true;
                wakeupAndBindMain();
            }
        } else {
            LogUtils.d(TAG, "run: 服务未启用，跳过守护逻辑");
            // 服务未启用时，重置线程状态
            mIsThreadAlive = false;
        }
    }

    /**
     * 唤醒主服务并建立绑定，确保主服务持续运行
     * 适配 API26+ 前台服务启动规则，避免系统限制导致启动失败
     */
    private void wakeupAndBindMain() {
        // 检查主服务存活状态
        String mainServiceName = ControlCenterService.class.getName();
        boolean isMainServiceAlive = ServiceUtils.isServiceAlive(getApplicationContext(), mainServiceName);
        LogUtils.d(TAG, String.format("wakeupAndBindMain: 主服务存活状态=%b", isMainServiceAlive));

        // 主服务未存活时，按需启动（区分API版本）
        if (!isMainServiceAlive) {
            Intent mainServiceIntent = new Intent(AssistantService.this, ControlCenterService.class);
            if (Build.VERSION.SDK_INT >= API_LEVEL_26) {
                startForegroundService(mainServiceIntent);
                LogUtils.d(TAG, "wakeupAndBindMain: API26+ 以前台服务方式启动主服务");
            } else {
                startService(mainServiceIntent);
                LogUtils.d(TAG, "wakeupAndBindMain: 以普通服务方式启动主服务");
            }
        }

        // 绑定主服务，监听连接状态，添加结果日志
        Intent bindIntent = new Intent(AssistantService.this, ControlCenterService.class);
        boolean bindResult = bindService(bindIntent, mMyServiceConnection, BIND_FLAG);
        LogUtils.d(TAG, String.format("wakeupAndBindMain: 绑定主服务结果=%b | 绑定标记=BIND_IMPORTANT", bindResult));
    }

    // ====================== 辅助工具方法（拆分独立逻辑，提高可维护性） ======================
    /**
     * 解绑主服务，包含异常捕获与状态日志
     */
    private void unbindMainService() {
        if (mMyServiceConnection != null) {
            try {
                unbindService(mMyServiceConnection);
                LogUtils.d(TAG, "unbindMainService: 已成功解绑ControlCenterService");
            } catch (IllegalArgumentException e) {
                LogUtils.w(TAG, String.format("unbindMainService: 解绑服务失败，服务未绑定 | %s", e.getMessage()));
            }
            mMyServiceConnection = null;
        }
    }
}

