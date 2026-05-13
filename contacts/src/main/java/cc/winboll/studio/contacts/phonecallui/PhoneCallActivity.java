package cc.winboll.studio.contacts.phonecallui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import cc.winboll.studio.contacts.ActivityStack;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.libappbase.LogUtils;
import java.util.Timer;
import java.util.TimerTask;

import static cc.winboll.studio.contacts.listenphonecall.CallListenerService.formatPhoneNumber;

/**
 * @Author aJIEw, ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/12/14 21:01
 * @Describe 接打电话界面（单例模式 + 适配API29 - 30 + 小米机型兼容性优化）
 * 功能：单例通话窗口、来电/去电显示、通话计时、免提控制、锁屏显示
 */
public class PhoneCallActivity extends Activity implements View.OnClickListener {
    // 常量定义区（核心常量+小米适配标识）
    public static final String TAG = "PhoneCallActivity";
    private static final int MSG_CLOSE_ACTIVITY = 0x001;
    private static final String MI_ADAPT_TAG = "MiAdapt";
    private static final String TOAST_CALLING = "通话进行中，无法重复创建通话窗口";
    private static final long CLOSE_DELAY_MS = 100; // 小米机型关闭延迟时间

    // 静态属性区（单例核心+全局工具对象）
    private static volatile boolean sIsActivityAlive = false;
    private static Handler sCloseHandler;

    // 控件属性区（按界面布局顺序排列）
    private TextView mTvCallNumberLabel;
    private TextView mTvCallNumber;
    private TextView mTvPickUp;
    private TextView mTvCallingTime;
    private TextView mTvHangUp;

    // 业务属性区（按依赖优先级排列）
    private PhoneCallManager mPhoneCallManager;
    private PhoneCallService.CallType mCallType;
    private String mPhoneNumber;
    private Timer mOnGoingCallTimer;
    private int mCallingTime;
    private boolean isClosing = false; // 新增：避免重复关闭页面

    // 对外静态接口（单例启动+外部关闭）
    public static void actionStart(Context context, String phoneNumber, PhoneCallService.CallType callType) {
        if (context == null || phoneNumber == null || callType == null) {
            LogUtils.e(TAG, "actionStart: 入参为空，启动失败");
            return;
        }

        if (sIsActivityAlive) {
            LogUtils.w(TAG, MI_ADAPT_TAG + " 已有活跃通话窗口，拒绝重复启动");
            Toast.makeText(context, TOAST_CALLING, Toast.LENGTH_SHORT).show();
            return;
        }

        LogUtils.d(TAG, MI_ADAPT_TAG + " 启动通话界面，号码=" + phoneNumber + "，类型=" + callType.name());
        Intent intent = new Intent(context, PhoneCallActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.putExtra("call_type", callType);
        intent.putExtra(Intent.EXTRA_PHONE_NUMBER, phoneNumber);
        context.startActivity(intent);
    }

    public static void closePhoneCallActivity() {
        LogUtils.d(TAG, "closePhoneCallActivity: 收到外部关闭指令");
        if (sIsActivityAlive && sCloseHandler != null) {
            sCloseHandler.sendEmptyMessage(MSG_CLOSE_ACTIVITY);
            LogUtils.d(TAG, "closePhoneCallActivity: 关闭消息已发送");
        } else {
            LogUtils.w(TAG, "closePhoneCallActivity: 页面已销毁或Handler未初始化，关闭跳过");
        }
    }

    // 生命周期方法区（按执行流程排序）
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, MI_ADAPT_TAG + " 通话界面开始创建，SDK版本=" + Build.VERSION.SDK_INT);

        // 单例双重校验，防止异常场景多实例
        if (sIsActivityAlive) {
            Toast.makeText(this, TOAST_CALLING, Toast.LENGTH_SHORT).show();
            LogUtils.w(TAG, MI_ADAPT_TAG + " 拦截重复创建，即将关闭当前实例");
            finish();
            return;
        }
        sIsActivityAlive = false;

        setContentView(R.layout.activity_phone_call);
        ActivityStack.getInstance().addActivity(this);
        adaptLockScreenAndXiaomi();
        initHandler();
        initData();
        initView();
        LogUtils.d(TAG, MI_ADAPT_TAG + " 通话界面创建完成");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, MI_ADAPT_TAG + " 通话界面开始销毁");

        sIsActivityAlive = false;
        isClosing = false;
        stopTimer();
        // 销毁通话管理器
        if (mPhoneCallManager != null) {
            mPhoneCallManager.destroy();
            mPhoneCallManager = null;
            LogUtils.d(TAG, "销毁通话管理器资源");
        }
        // 销毁Handler避免内存泄漏
        if (sCloseHandler != null) {
            sCloseHandler.removeCallbacksAndMessages(null);
            sCloseHandler = null;
            LogUtils.d(TAG, "销毁关闭Handler");
        }
        ActivityStack.getInstance().removeActivity(this);
        LogUtils.d(TAG, MI_ADAPT_TAG + " 通话界面销毁完成");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            sIsActivityAlive = false;
            LogUtils.d(TAG, MI_ADAPT_TAG + " 页面即将关闭，重置单例标记");
        }
    }

    // 点击事件回调
    @Override
    public void onClick(View v) {
        if (v == null) {
            LogUtils.w(TAG, "onClick: 点击控件为空，忽略操作");
            return;
        }
        switch (v.getId()) {
            case R.id.tv_phone_pick_up:
                LogUtils.d(TAG, "onClick: 触发接听操作");
                answerCall();
                break;
            case R.id.tv_phone_hang_up:
                LogUtils.d(TAG, "onClick: 触发挂断操作，当前通话时长=" + mCallingTime + "秒");
                hangUpCall();
                break;
            default:
                LogUtils.w(TAG, "onClick: 未知点击事件，控件ID=" + v.getId());
        }
    }

    // 初始化方法区（按初始化顺序排列）
    private void initHandler() {
        sCloseHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MSG_CLOSE_ACTIVITY) {
                    LogUtils.d(TAG, "handleMessage: 收到关闭消息，执行挂断逻辑");
                    hangUpCall();
                }
            }
        };
        LogUtils.d(TAG, "initHandler: 关闭Handler初始化完成");
    }

    private void initData() {
        LogUtils.d(TAG, "initData: 开始初始化业务数据");
        mPhoneCallManager = PhoneCallManager.getInstance(this);
        Intent intent = getIntent();

        if (intent == null) {
            LogUtils.e(TAG, "initData: 启动Intent为空，终止初始化");
            removeFromRecentsAndFinish();
            return;
        }

        mPhoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        mCallType = (PhoneCallService.CallType) intent.getSerializableExtra("call_type");
        if (mPhoneNumber == null || mCallType == null) {
            LogUtils.e(TAG, "initData: 通话号码或类型解析失败");
            removeFromRecentsAndFinish();
            return;
        }

        mOnGoingCallTimer = new Timer();
        mCallingTime = 0;
        LogUtils.d(TAG, "initData: 业务数据初始化完成，号码=" + mPhoneNumber);
    }

    private void initView() {
        LogUtils.d(TAG, "initView: 开始初始化界面控件");
        // 修复沉浸式导航栏语法，适配小米全面屏
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
			| View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);

        // 绑定控件
        mTvCallNumberLabel = findViewById(R.id.tv_call_number_label);
        mTvCallNumber = findViewById(R.id.tv_call_number);
        mTvPickUp = findViewById(R.id.tv_phone_pick_up);
        mTvCallingTime = findViewById(R.id.tv_phone_calling_time);
        mTvHangUp = findViewById(R.id.tv_phone_hang_up);

        // 设置控件属性
        mTvCallNumber.setText(formatPhoneNumber(mPhoneNumber));
        mTvPickUp.setOnClickListener(this);
        mTvHangUp.setOnClickListener(this);

        // 区分来电/去电UI样式
        if (PhoneCallService.CallType.CALL_IN == mCallType) {
            mTvCallNumberLabel.setText("来电号码");
            mTvPickUp.setVisibility(View.VISIBLE);
            mTvCallingTime.setVisibility(View.GONE);
        } else if (PhoneCallService.CallType.CALL_OUT == mCallType) {
            mTvCallNumberLabel.setText("呼叫号码");
            mTvPickUp.setVisibility(View.GONE);
            mTvCallingTime.setVisibility(View.VISIBLE);
            mTvCallingTime.setText("通话中：00:00");
            if (mPhoneCallManager != null) {
                mPhoneCallManager.openSpeaker();
                LogUtils.d(TAG, MI_ADAPT_TAG + " 去电模式自动开启免提");
            }
            startCallTimer();
        }
        LogUtils.d(TAG, "initView: 界面控件初始化完成");
    }

    // 小米机型专属适配方法
    private void adaptLockScreenAndXiaomi() {
        LogUtils.d(TAG, MI_ADAPT_TAG + " 执行锁屏适配逻辑");
        Window window = getWindow();
        if (window == null) {
            LogUtils.e(TAG, MI_ADAPT_TAG + " Window对象为空，适配失败");
            return;
        }

        int flags = WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
			| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
			| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

        // 小米机型额外添加解锁屏标志，解决MIUI锁屏拦截问题
        if (Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")) {
            flags |= WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
            LogUtils.d(TAG, MI_ADAPT_TAG + " 已添加小米机型专属锁屏适配标志");
        }
        window.addFlags(flags);

        // 适配API29+锁屏新接口
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            LogUtils.d(TAG, MI_ADAPT_TAG + " 适配API29+锁屏接口完成");
        }
    }

    // 通话核心业务方法
    private void answerCall() {
        LogUtils.d(TAG, "answerCall: 执行接听操作");
        if (mPhoneCallManager == null) {
            LogUtils.e(TAG, "answerCall: 通话管理器为空，接听失败");
            return;
        }
        mPhoneCallManager.answer();
        mTvPickUp.setVisibility(View.GONE);
        mTvCallingTime.setVisibility(View.VISIBLE);
        mTvCallingTime.setText("通话中：00:00");
        startCallTimer();
        LogUtils.d(TAG, "answerCall: 接听操作完成，启动通话计时");
    }

    private void hangUpCall() {
        if (isClosing) {
            LogUtils.w(TAG, "hangUpCall: 挂断操作已执行，无需重复调用");
            return;
        }
        LogUtils.d(TAG, "hangUpCall: 执行挂断操作，当前时长=" + mCallingTime + "秒");
        isClosing = true;
        stopTimer();
        if (mPhoneCallManager != null) {
            mPhoneCallManager.disconnect();
            LogUtils.d(TAG, "hangUpCall: 通话连接已断开");
        }
        // 延迟关闭页面，适配小米机型通话时序
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					removeFromRecentsAndFinish();
				}
			}, CLOSE_DELAY_MS);
    }

    // 任务栈清理方法
    private void removeFromRecentsAndFinish() {
        if (isFinishing()) {
            LogUtils.d(TAG, "removeFromRecentsAndFinish: 页面已在关闭中，无需重复操作");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
            LogUtils.d(TAG, MI_ADAPT_TAG + " 移除任务栈并关闭页面");
        } else {
            finish();
            LogUtils.d(TAG, "兼容低版本，关闭页面");
        }
    }

    // 计时工具方法
    private void startCallTimer() {
        LogUtils.d(TAG, "startCallTimer: 启动通话计时器");
        if (mOnGoingCallTimer == null) {
            mOnGoingCallTimer = new Timer();
        }
        mOnGoingCallTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mCallingTime++;
								mTvCallingTime.setText("通话中：" + formatCallingTime(mCallingTime));
							}
						});
				}
			}, 0, 1000);
    }

    private void stopTimer() {
        LogUtils.d(TAG, "stopTimer: 停止通话计时器");
        if (mOnGoingCallTimer != null) {
            mOnGoingCallTimer.cancel();
            mOnGoingCallTimer = null;
        }
        mCallingTime = 0;
    }

    // 辅助工具方法：格式化通话时长
    private String formatCallingTime(int seconds) {
        int minute = seconds / 60;
        int second = seconds % 60;
        String minuteStr = minute < 10 ? "0" + minute : String.valueOf(minute);
        String secondStr = second < 10 ? "0" + second : String.valueOf(second);
        return minuteStr + ":" + secondStr;
    }
}

