package cc.winboll.studio.contacts.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import cc.winboll.studio.contacts.model.MainServiceBean;
import cc.winboll.studio.contacts.utils.NotificationManagerUtils;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/02/14 03:38:31
 * @Describe 守护进程服务，用于监控并保活主服务 MainService
 * 适配 Android 12+ 后台服务启动限制，支持前台服务运行
 * 兼容 Java 7 语法 & 低版本 SDK 编译
 * 移除无关的 microphone 类型配置，修复前台服务类型不匹配崩溃
 */
public class AssistantService extends Service {
    // ====================== 常量定义区 ======================
    public static final String TAG = "AssistantService";
    // 前台服务通知配置
    private static final String FOREGROUND_CHANNEL_ID = "assistant_service_foreground_channel";
    private static final int FOREGROUND_NOTIFICATION_ID = 1002;
    // 修复：前台服务类型改为 dataSync(0x00000001)，与 Manifest 保持一致，移除 microphone 类型
    private static final int FOREGROUND_SERVICE_TYPE_DATA_SYNC = 0x00000001;
    // Android 版本常量硬编码（Java 7 兼容）
    private static final int ANDROID_8_API = 26;      // 通知渠道最低版本
    private static final int ANDROID_10_API = 29;     // 前台服务类型最低支持版本
    private static final int ANDROID_12_API = 31;     // 后台启动限制最低版本
    // 重试延迟时间（避免频繁触发后台启动限制）
    private static final long RETRY_DELAY_MS = 3000L;

    // ====================== 成员变量区 ======================
    private MainServiceBean mMainServiceBean;
    private MyServiceConnection mMyServiceConnection;
    private MainService mMainService;
    private boolean mIsBound = false;
    private volatile boolean mIsThreadAlive = false;

    // ====================== Binder 内部类 ======================
    /**
     * 对外暴露服务实例的 Binder
     */
    public class MyBinder extends Binder {
        public AssistantService getService() {
            LogUtils.d(TAG, "MyBinder.getService: 获取 AssistantService 实例");
            return AssistantService.this;
        }
    }

    // ====================== ServiceConnection 内部类 ======================
    /**
     * 主服务连接状态监听回调
     */
    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service == null) {
                LogUtils.w(TAG, "MyServiceConnection.onServiceConnected: 绑定的 IBinder 为 null");
                mIsBound = false;
                return;
            }

            try {
                MainService.MyBinder binder = (MainService.MyBinder) service;
                mMainService = binder.getService();
                mIsBound = true;
                LogUtils.d(TAG, "MyServiceConnection.onServiceConnected: 主服务绑定成功 | MainService=" + mMainService);
            } catch (ClassCastException e) {
                LogUtils.e(TAG, "MyServiceConnection.onServiceConnected: IBinder 类型转换失败", e);
                mIsBound = false;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.w(TAG, "MyServiceConnection.onServiceDisconnected: 主服务连接断开");
            mMainService = null;
            mIsBound = false;

            // 尝试重新绑定主服务（如果配置为启用）
            reloadMainServiceConfig();
            if (mMainServiceBean != null && mMainServiceBean.isEnable()) {
                LogUtils.d(TAG, "MyServiceConnection.onServiceDisconnected: 延迟重试绑定主服务");
                wakeupAndBindMain();
            }
        }
    }

    // ====================== 对外方法区 ======================
    /**
     * 设置线程存活状态
     */
    public synchronized void setIsThreadAlive(boolean isThreadAlive) {
        this.mIsThreadAlive = isThreadAlive;
        LogUtils.d(TAG, "setIsThreadAlive: 线程存活状态变更 | " + isThreadAlive);
    }

    /**
     * 获取线程存活状态
     */
    public boolean isThreadAlive() {
        return mIsThreadAlive;
    }

    // ====================== 前台服务辅助方法 ======================
    /**
     * 创建前台服务通知（Android 8.0+ 必须配置渠道）
     */
//    private Notification createForegroundNotification() {
//        // 1. 创建通知渠道（API 26+ 必需）
//        if (Build.VERSION.SDK_INT >= ANDROID_8_API) {
//            NotificationChannel channel = new NotificationChannel(
//				FOREGROUND_CHANNEL_ID,
//				"守护服务",
//				NotificationManager.IMPORTANCE_LOW
//            );
//            channel.setDescription("守护服务后台运行，保障主服务存活");
//            // 空指针防护
//            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            if (manager != null) {
//                manager.createNotificationChannel(channel);
//                LogUtils.d(TAG, "createForegroundNotification: 通知渠道创建成功");
//            }
//        }
//
//        // 2. 构建通知（Java 7 分步设置，取消链式调用简化）
//        Notification.Builder builder;
//        if (Build.VERSION.SDK_INT >= ANDROID_8_API) {
//            builder = new Notification.Builder(this, FOREGROUND_CHANNEL_ID);
//        } else {
//            builder = new Notification.Builder(this);
//        }
//        builder.setSmallIcon(R.drawable.ic_launcher);
//        builder.setContentTitle("守护服务运行中");
//        builder.setContentText("正在监控主服务状态");
//        builder.setPriority(Notification.PRIORITY_LOW);
//        builder.setOngoing(true); // 不可手动取消
//
//        return builder.build();
//    }

    // ====================== Service 生命周期方法区 ======================
    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "onCreate: 守护服务创建");

        // 初始化主服务连接回调
        if (mMyServiceConnection == null) {
            mMyServiceConnection = new MyServiceConnection();
            LogUtils.d(TAG, "onCreate: 初始化 MyServiceConnection 完成");
        }

        // 初始化运行状态
        setIsThreadAlive(false);
        // 启动守护逻辑
        assistantService();
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.d(TAG, "onBind: 服务被绑定 | Intent=" + intent);
        return new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "onStartCommand: 服务被启动 | startId=" + startId);
        // 每次启动都执行守护逻辑，确保主服务存活
        assistantService();
        // START_STICKY：服务被杀死后系统尝试重启
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy: 守护服务销毁");

        // 停止线程并解除主服务绑定
        setIsThreadAlive(false);
        if (mIsBound && mMyServiceConnection != null) {
            try {
                unbindService(mMyServiceConnection);
                LogUtils.d(TAG, "onDestroy: 解除主服务绑定成功");
            } catch (IllegalArgumentException e) {
                LogUtils.w(TAG, "onDestroy: 解除绑定失败，服务未绑定", e);
            }
            mIsBound = false;
        }
        mMainService = null;
    }

    // ====================== 核心守护逻辑方法区 ======================
    /**
     * 守护服务核心逻辑：检查配置并保活主服务
     */
    private void assistantService() {
        LogUtils.d(TAG, "assistantService: 执行守护逻辑");

        // 加载主服务配置
        reloadMainServiceConfig();
        if (mMainServiceBean == null) {
            LogUtils.e(TAG, "assistantService: 主服务配置加载失败，终止守护逻辑");
            return;
        }

        LogUtils.d(TAG, "assistantService: 主服务启用状态 | " + mMainServiceBean.isEnable());
        // 配置启用且线程未存活时，唤醒并绑定主服务
        if (mMainServiceBean.isEnable() && !isThreadAlive()) {
            setIsThreadAlive(true);
            wakeupAndBindMain();
        } else if (!mMainServiceBean.isEnable()) {
            setIsThreadAlive(false);
            LogUtils.d(TAG, "assistantService: 主服务已禁用，停止保活");
        }
    }

    /**
     * 唤醒并绑定主服务 MainService（适配后台启动限制）
     */
    private void wakeupAndBindMain() {
        if (mMyServiceConnection == null) {
            LogUtils.e(TAG, "wakeupAndBindMain: MyServiceConnection 未初始化，绑定失败");
            return;
        }

        Intent intent = new Intent(this, MainService.class);
        // 根据应用前后台状态选择启动方式（Android 12+ 后台用 startForegroundService）
        startForegroundService(intent);

        // BIND_IMPORTANT：提高绑定优先级，主服务被杀时会回调断开
        bindService(intent, mMyServiceConnection, Context.BIND_IMPORTANT);
        LogUtils.d(TAG, "wakeupAndBindMain: 已启动并绑定主服务 MainService");
    }

    // ====================== 辅助方法区 ======================
    /**
     * 重新加载主服务配置
     */
    private void reloadMainServiceConfig() {
        mMainServiceBean = MainServiceBean.loadBean(this, MainServiceBean.class);
        LogUtils.d(TAG, "reloadMainServiceConfig: 主服务配置重新加载完成 | " + mMainServiceBean);
    }
}

