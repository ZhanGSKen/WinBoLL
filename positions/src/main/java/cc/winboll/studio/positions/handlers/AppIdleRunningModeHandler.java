package cc.winboll.studio.positions.handlers;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.positions.App;
import cc.winboll.studio.positions.MainActivity;

/**
 * 应用空转事务处理器
 * 作用：接收空转开关消息、空转日志消息，回调MainActivity内部接口实现UI联动
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @CreateTime 2026/05/03 12:23:00
 * @EditTime   2026/05/03 14:42:18
 */
public class AppIdleRunningModeHandler extends Handler {

    //===================== 常量标识 =====================
    public static final String TAG = "AppIdleRunningModeHandler";
    public static final int MSG_IDLE_MODE_SWITCH = 1001;
    public static final int MSG_IDLE_LOG_PRINT  = 1002;

    //===================== 成员变量 =====================
    private static AppIdleRunningModeHandler mHandler;
    private MainActivity mMainActivity;

    //===================== 静态初始化 =====================
    static {
        mHandler = new AppIdleRunningModeHandler();
        LogUtils.d(TAG, "静态代码块：默认主线程Looper完成初始化");
    }

    //===================== 构造方法 =====================
    /**
     * 私有无参构造，禁止外部直接实例化
     */
    private AppIdleRunningModeHandler() {
        super(Looper.getMainLooper());
    }

    /**
     * 带MainActivity绑定构造
     * @param activity 主页面实例
     */
    public AppIdleRunningModeHandler(MainActivity activity) {
        super(Looper.getMainLooper());
        this.mMainActivity = activity;
        LogUtils.d(TAG, "构造方法：完成MainActivity绑定初始化");
    }

    //===================== 对外初始化与获取 =====================
    /**
     * 全局静态初始化、重新绑定MainActivity
     * @param activity 主页面实例
     */
    public static void init(MainActivity activity) {
        if (mHandler == null) {
            mHandler = new AppIdleRunningModeHandler(activity);
        } else {
            mHandler.mMainActivity = activity;
        }
        LogUtils.i(TAG, "init -> AppIdleRunningModeHandler初始化绑定成功");
    }

    /**
     * 获取当前绑定的MainActivity实例
     * @return 已绑定的Activity
     */
    public MainActivity getBindMainActivity() {
        return mMainActivity;
    }

    //===================== 对外静态发送方法 =====================
    /**
     * 发送空转开关控制消息
     * @param isOpen 是否开启空转状态
     */
    public static void sendIdleSwitch(boolean isOpen) {
        if (!App.isAppIdleRunning()) {
            LogUtils.d(TAG, "sendIdleSwitch -> 当前非空转状态，函数执行无效");
            return;
        }
        LogUtils.d(TAG, "sendIdleSwitch -> 发送空转开关信号，参数isOpen = " + isOpen);

        Message message = Message.obtain();
        message.what = MSG_IDLE_MODE_SWITCH;
        message.obj = isOpen;
        mHandler.sendMessage(message);
    }

    /**
     * 发送空转日志打印消息
     * @param logText 待输出的日志内容
     */
    public static void sendIdleLog(String logText) {
        if (!App.isAppIdleRunning()) {
            LogUtils.d(TAG, "sendIdleLog -> 当前非空转状态，函数执行无效");
            return;
        }
        LogUtils.d(TAG, "sendIdleLog -> 发送空转日志消息");

        Message message = Message.obtain();
        message.what = MSG_IDLE_LOG_PRINT;
        message.obj = logText;
        mHandler.sendMessage(message);
    }

    //===================== 消息接收处理 =====================
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        // 全局状态校验
        if (!App.isAppIdleRunning()) {
            return;
        }
        // 空指针安全防护
        if (mMainActivity == null || mMainActivity.getOnAppIdleRunningListener() == null) {
            LogUtils.d(TAG, "handleMessage -> Activity或监听接口为空，终止回调");
            return;
        }

        switch (msg.what) {
            case MSG_IDLE_MODE_SWITCH:
                boolean idleState = (boolean) msg.obj;
                App.setAppIdleRunning(idleState);
                LogUtils.i(TAG, "handleMessage -> 空转状态已变更：" + idleState);
                // 回调主页面接口
                mMainActivity.getOnAppIdleRunningListener().onIdleStatusChange(idleState);
                break;

            case MSG_IDLE_LOG_PRINT:
                String logContent = (String) msg.obj;
                LogUtils.i(TAG, logContent);
                // 回调主页面日志接收接口
                mMainActivity.getOnAppIdleRunningListener().onIdleLogReceive(logContent);
                break;
        }
    }
}

