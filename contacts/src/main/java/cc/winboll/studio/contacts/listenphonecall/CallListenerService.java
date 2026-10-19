package cc.winboll.studio.contacts.listenphonecall;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.contacts.model.MainServiceBean;
import cc.winboll.studio.contacts.phonecallui.PhoneCallActivity;
import cc.winboll.studio.contacts.phonecallui.PhoneCallService;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Describe 通话监听服务（无前台服务），负责监听通话状态、显示通话悬浮窗、跳转通话界面
 * 严格适配 Java7 语法 + Android API29-30 | 轻量稳定 | 避免内存泄漏
 */
public class CallListenerService extends Service {

    // ====================== 常量定义区（精准适配API29-30，无冗余） ======================
    public static final String TAG = "CallListenerService";

    // Android版本常量（仅保留适配必需版本，精简无用定义）
    private static final int ANDROID_8_API = 26;   // 悬浮窗类型适配（API26+必需）
    private static final int ANDROID_10_API = 29;  // API29+ 悬浮窗权限/参数适配
    private static final int ANDROID_19_API = 19;  // 透明状态栏/导航栏适配

    // 延迟初始化参数（让出主线程，避免启动阻塞）
    private static final long DELAY_INIT_MS = 100L;

    // ====================== 成员属性区（按功能归类，命名规范） ======================
    // 延迟初始化核心
    private Handler mDelayHandler;                // 延迟处理器（避免onCreate阻塞）

    // 通话监听核心
    private TelephonyManager mTelephonyManager;   // 电话管理器（监听通话状态）
    private PhoneStateListener mPhoneStateListener;// 通话状态监听回调
    private String mCallNumber;                   // 当前通话号码
    private boolean mIsCallingIn;                 // 是否为来电（true=来电，false=去电）

    // 悬浮窗核心
    private WindowManager mWindowManager;         // 窗口管理器（添加/移除悬浮窗）
    private WindowManager.LayoutParams mWindowParams;// 悬浮窗参数配置
    private View mPhoneCallView;                  // 通话悬浮窗根视图
    private TextView mTvCallNumber;               // 悬浮窗号码显示控件
    private Button mBtnOpenApp;                   // 悬浮窗跳转APP按钮
    private boolean mHasShown;                    // 悬浮窗显示状态标记（避免重复操作）

    // ====================== Service生命周期方法区（按执行顺序排列） ======================
    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "===== onCreate: 通话监听服务启动 =====");

        // 延迟初始化所有逻辑（让出主线程，避免启动阻塞，提升启动速度）
        initDelayHandlerAndLogic();

        LogUtils.d(TAG, "===== onCreate: 通话监听服务启动完成 =====");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "onStartCommand: 服务被启动，startId=" + startId);

        // 加载服务配置，决定重启策略（启用则自动重启，禁用则默认）
        MainServiceBean serviceConfig = MainServiceBean.loadBean(this, MainServiceBean.class);
        int startMode = (serviceConfig != null && serviceConfig.isEnable()) ? START_STICKY : super.onStartCommand(intent, flags, startId);
        LogUtils.d(TAG, "onStartCommand: 服务启动模式：" + (startMode == START_STICKY ? "START_STICKY（自动重启）" : "默认模式"));

        return startMode;
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.d(TAG, "onBind: 服务无需绑定，返回null");
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "===== onDestroy: 通话监听服务开始销毁 =====");

        // 全量清理资源，彻底避免内存泄漏
        dismissFloatWindow();        // 移除悬浮窗
        unregisterPhoneStateListener();// 注销通话监听
        clearDelayHandler();         // 清空延迟任务
        resetAllReferences();        // 置空所有成员属性

        LogUtils.d(TAG, "===== onDestroy: 通话监听服务销毁完成 =====");
    }

    // ====================== 延迟初始化方法区（非阻塞启动，提升稳定性） ======================
    /**
     * 初始化延迟处理器，执行核心逻辑（通话监听+悬浮窗）
     */
    private void initDelayHandlerAndLogic() {
        mDelayHandler = new Handler(Looper.getMainLooper());
        mDelayHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					LogUtils.d(TAG, "initDelayHandlerAndLogic: 开始延迟初始化核心逻辑");
					initPhoneStateListener();  // 初始化通话状态监听
					initFloatWindow();         // 初始化通话悬浮窗
					LogUtils.d(TAG, "initDelayHandlerAndLogic: 延迟初始化完成，服务就绪");
				}
			}, DELAY_INIT_MS);
    }

    /**
     * 初始化通话状态监听（注册TelephonyManager，响应通话状态变化）
     */
    private void initPhoneStateListener() {
        // 1. 创建通话状态监听回调
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int callState, String incomingNumber) {
                super.onCallStateChanged(callState, incomingNumber);
                mCallNumber = incomingNumber;
                LogUtils.d(TAG, "onCallStateChanged: 通话状态变化，状态=" + getCallStateDesc(callState) + "，号码=" + incomingNumber);

                // 响应不同通话状态
                switch (callState) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        // 通话空闲（挂断/未通话）：隐藏悬浮窗
                        dismissFloatWindow();
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        // 来电响铃：标记来电状态，更新UI并显示悬浮窗
                        mIsCallingIn = true;
                        updateFloatWindowUI();
                        showFloatWindow();
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        // 通话中（接听/拨号）：更新UI并显示悬浮窗
                        updateFloatWindowUI();
                        showFloatWindow();
                        break;
                }
            }
        };

        // 2. 注册通话监听（非空校验，避免崩溃）
        mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            LogUtils.d(TAG, "initPhoneStateListener: 通话状态监听注册成功");
        } else {
            LogUtils.e(TAG, "initPhoneStateListener: TelephonyManager获取失败，监听注册失败");
        }
    }

    /**
     * 初始化通话悬浮窗（配置参数+加载布局+绑定事件，适配API29-30）
     */
    private void initFloatWindow() {
        // 1. 获取窗口管理器（非空校验，避免后续崩溃）
        mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        if (mWindowManager == null) {
            LogUtils.e(TAG, "initFloatWindow: WindowManager获取失败，悬浮窗初始化失败");
            return;
        }

        // 2. 配置悬浮窗参数（精准适配API29+，兼容悬浮窗权限）
        initFloatWindowParams();

        // 3. 加载悬浮窗布局（添加返回键拦截，避免误关闭）
        FrameLayout keyInterceptorLayout = new FrameLayout(this) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                // 拦截返回键，保障通话时悬浮窗正常显示
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    LogUtils.d(TAG, "dispatchKeyEvent: 拦截悬浮窗返回键事件");
                    return true;
                }
                return super.dispatchKeyEvent(event);
            }
        };
        mPhoneCallView = LayoutInflater.from(this).inflate(R.layout.view_phone_call, keyInterceptorLayout);

        // 4. 绑定悬浮窗控件，设置跳转按钮事件
        bindFloatWindowViews();

        LogUtils.d(TAG, "initFloatWindow: 悬浮窗初始化完成");
    }

    /**
     * 配置悬浮窗参数（适配API29+窗口类型，确保正常显示）
     */
    private void initFloatWindowParams() {
        mWindowParams = new WindowManager.LayoutParams();
        // 窗口位置：顶部居中
        mWindowParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        // 窗口大小：宽度全屏，高度自适应
        mWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        // 固定竖屏显示
        mWindowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        // 窗口格式：半透明
        mWindowParams.format = PixelFormat.TRANSLUCENT;

        // 窗口类型（API29+ 强制用 TYPE_APPLICATION_OVERLAY，需悬浮窗权限）
        if (Build.VERSION.SDK_INT >= ANDROID_10_API) {
            mWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            LogUtils.d(TAG, "initFloatWindowParams: API29+ 悬浮窗类型=TYPE_APPLICATION_OVERLAY（需开启悬浮窗权限）");
        } else if (Build.VERSION.SDK_INT >= ANDROID_8_API) {
            mWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        // 窗口标志：无焦点（不抢占输入）、全屏布局、兼容透明状态栏/导航栏
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
			| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        if (Build.VERSION.SDK_INT >= ANDROID_19_API) {
            mWindowParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
				| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        }
    }

    /**
     * 绑定悬浮窗控件，设置跳转通话详情页事件
     */
    private void bindFloatWindowViews() {
        mTvCallNumber = (TextView) mPhoneCallView.findViewById(R.id.tv_call_number);
        mBtnOpenApp = (Button) mPhoneCallView.findViewById(R.id.btn_open_app);

        // 跳转按钮点击事件
        mBtnOpenApp.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (TextUtils.isEmpty(mCallNumber)) {
						LogUtils.w(TAG, "bindFloatWindowViews: 通话号码为空，跳过跳转");
						return;
					}
					LogUtils.d(TAG, "bindFloatWindowViews: 点击跳转通话详情页，号码=" + mCallNumber);
					PhoneCallService.CallType callType = mIsCallingIn ? PhoneCallService.CallType.CALL_IN : PhoneCallService.CallType.CALL_OUT;
					PhoneCallActivity.actionStart(CallListenerService.this, mCallNumber, callType);
				}
			});
    }

    // ====================== 悬浮窗功能逻辑区（显示/隐藏/更新UI） ======================
    /**
     * 显示通话悬浮窗（避免重复添加，防止窗口泄露）
     */
    private void showFloatWindow() {
        if (!mHasShown && mPhoneCallView != null && mWindowManager != null) {
            try {
                mWindowManager.addView(mPhoneCallView, mWindowParams);
                mHasShown = true;
                LogUtils.d(TAG, "showFloatWindow: 悬浮窗显示成功");
            } catch (SecurityException e) {
                LogUtils.e(TAG, "showFloatWindow: 悬浮窗显示失败（无悬浮窗权限，需引导用户开启）", e);
            } catch (Exception e) {
                LogUtils.e(TAG, "showFloatWindow: 悬浮窗显示异常", e);
            }
        } else {
            LogUtils.d(TAG, "showFloatWindow: 悬浮窗已显示/组件未初始化，跳过显示");
        }
    }

    /**
     * 隐藏通话悬浮窗（避免重复移除，防止崩溃）
     */
    private void dismissFloatWindow() {
        if (mHasShown && mPhoneCallView != null && mWindowManager != null) {
            try {
                mWindowManager.removeView(mPhoneCallView);
                LogUtils.d(TAG, "dismissFloatWindow: 悬浮窗隐藏成功");
            } catch (Exception e) {
                LogUtils.e(TAG, "dismissFloatWindow: 悬浮窗隐藏异常", e);
            } finally {
                mHasShown = false;
                mIsCallingIn = false;  // 重置来电状态标记
            }
        } else {
            LogUtils.d(TAG, "dismissFloatWindow: 悬浮窗已隐藏/组件未初始化，跳过隐藏");
        }
    }

    /**
     * 更新悬浮窗UI（显示格式化号码+通话类型图标）
     */
    private void updateFloatWindowUI() {
        if (mTvCallNumber == null || TextUtils.isEmpty(mCallNumber)) {
            LogUtils.w(TAG, "updateFloatWindowUI: 控件未初始化/号码为空，更新失败");
            return;
        }

        // 格式化11位手机号（3-4-4分隔，提升可读性）
        String formattedNumber = formatPhoneNumber(mCallNumber);
        mTvCallNumber.setText(formattedNumber);

        // 设置通话类型图标（来电/去电区分）
        int iconResId = mIsCallingIn ? R.drawable.ic_phone_call_in : R.drawable.ic_phone_call_out;
        mTvCallNumber.setCompoundDrawablesWithIntrinsicBounds(
			null, null, getResources().getDrawable(iconResId), null
        );
        LogUtils.d(TAG, "updateFloatWindowUI: 悬浮窗UI更新完成，号码=" + formattedNumber + "，类型=" + (mIsCallingIn ? "来电" : "去电"));
    }

    // ====================== 资源清理方法区（服务销毁时全量释放） ======================
    /**
     * 注销通话状态监听（释放TelephonyManager资源）
     */
    private void unregisterPhoneStateListener() {
        if (mTelephonyManager != null && mPhoneStateListener != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            LogUtils.d(TAG, "unregisterPhoneStateListener: 通话监听已注销");
        }
        mTelephonyManager = null;
        mPhoneStateListener = null;
    }

    /**
     * 清空延迟处理器（移除未执行任务，避免内存泄漏）
     */
    private void clearDelayHandler() {
        if (mDelayHandler != null) {
            mDelayHandler.removeCallbacksAndMessages(null);
            mDelayHandler = null;
            LogUtils.d(TAG, "clearDelayHandler: 延迟处理器已清空");
        }
    }

    /**
     * 置空所有成员属性（彻底释放引用，避免内存泄漏）
     */
    private void resetAllReferences() {
        mCallNumber = null;
        mPhoneCallView = null;
        mWindowParams = null;
        mWindowManager = null;
        mTvCallNumber = null;
        mBtnOpenApp = null;
    }

    // ====================== 工具方法区（通用辅助功能，独立归类） ======================
    /**
     * 格式化手机号（11位手机号：3-4-4分隔，非11位保持原格式）
     * @param phoneNum 待格式化的手机号
     * @return 格式化后的号码
     */
    public static String formatPhoneNumber(String phoneNum) {
        if (!TextUtils.isEmpty(phoneNum) && phoneNum.length() == 11) {
            String formatted = phoneNum.substring(0, 3) + "-"
				+ phoneNum.substring(3, 7) + "-"
				+ phoneNum.substring(7);
            LogUtils.d(TAG, "formatPhoneNumber: 号码格式化，原=" + phoneNum + "，新=" + formatted);
            return formatted;
        }
        LogUtils.d(TAG, "formatPhoneNumber: 非11位号码，无需格式化，号码=" + phoneNum);
        return phoneNum;
    }

    /**
     * 转换通话状态为文字描述（便于日志查看，快速定位问题）
     * @param callState 通话状态（TelephonyManager常量）
     * @return 状态描述文字
     */
    private String getCallStateDesc(int callState) {
        switch (callState) {
            case TelephonyManager.CALL_STATE_IDLE:
                return "空闲（挂断/未通话）";
            case TelephonyManager.CALL_STATE_RINGING:
                return "响铃（来电）";
            case TelephonyManager.CALL_STATE_OFFHOOK:
                return "通话中（接听/拨号）";
            default:
                return "未知状态";
        }
    }
}

