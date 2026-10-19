package cc.winboll.studio.contacts.views;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.contacts.services.LimitedTimeSpecialChannelService;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/04/18 15:05:00 (GMT+8)
 * @LastEditTime 2026/04/22 17:30:00 (GMT+8)
 * @Describe 限时特殊通道视图
 * 功能说明：
 * 1. Switch 打开时，EditText 不可编辑，布局背景变为绿色，加减按钮不可用
 * 2. Switch 关闭时，EditText 可编辑，布局背景恢复为白色，加减按钮恢复可用
 * 3. 监听服务销毁本地广播，到达后自动关闭 Switch 并恢复背景色与按钮状态
 * 4. 监听倒计时心跳广播，实时更新EditText显示剩余秒数
 * 5. 加减按钮控制秒数增减，最小值限制为0
 * 6. 新增快速设置按钮(btn_thisSeconds)，点击直接设置为当前量级3600秒
 * 7. [新增] 服务销毁时，恢复EditText显示为倒计时剩余秒数
 */
public class LimitedTimeSpecialChannelView extends LinearLayout {

    // ====================== 常量定义 =========================
    public static final String TAG = "LimitedTimeSpecialChannelView";
    // 定义秒数增量的量级
    private static final long SECONDS_INCREMENT = 3600;

    // ====================== 成员变量 =========================
    private Context mContext;
    private EditText mEtSeconds;
    private Switch mSwEnable;
    private LinearLayout mLlMain;
    private Button mBtnAddSeconds;
    private Button mBtnSubSeconds;
    // [新增] 快速设置量级按钮
    private Button mBtnThisSeconds;
    private android.content.BroadcastReceiver mCountdownReceiver;

    // ====================== 构造函数 =========================
    public LimitedTimeSpecialChannelView(Context context) {
        super(context);
        initView(context);
    }

    public LimitedTimeSpecialChannelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public LimitedTimeSpecialChannelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    // ====================== 初始化方法 =========================
    private void initView(Context context) {
        LogUtils.i(TAG, "开始初始化视图...");
        this.mContext = context;

        // 加载布局文件
        LayoutInflater.from(context).inflate(R.layout.view_limitedtimespecialchannel, this, true);
        LogUtils.i(TAG, "布局文件加载完成");

        // 绑定控件ID
        bindViews();
        // 初始化所有按钮点击事件
        initButtons();
        // 初始化开关状态
        initSwitchState();
        // 注册广播监听
        registerServiceDestroyedListener();
        registerCountdownTickListener();

        LogUtils.i(TAG, "视图初始化流程结束");
    }

    /**
     * 绑定布局中的所有控件ID
     */
    private void bindViews() {
        LogUtils.i(TAG, "开始绑定控件ID");
        mEtSeconds = (EditText) findViewById(R.id.et_seconds);
        mSwEnable = (Switch) findViewById(R.id.sw_enable);
        mLlMain = (LinearLayout) findViewById(R.id.ll_main);
        mBtnAddSeconds = (Button) findViewById(R.id.btn_addSeconds);
        mBtnSubSeconds = (Button) findViewById(R.id.btn_subSeconds);
        // [新增] 绑定快速设置按钮ID
        mBtnThisSeconds = (Button) findViewById(R.id.btn_thisSeconds);

        // 健壮性检查
        if (mEtSeconds == null) LogUtils.e(TAG, "未找到ID为 et_seconds 的EditText");
        if (mSwEnable == null) LogUtils.e(TAG, "未找到ID为 sw_enable 的Switch");
        if (mLlMain == null) LogUtils.w(TAG, "未找到ID为 ll_main 的LinearLayout，将无法设置背景色");
        if (mBtnAddSeconds == null) LogUtils.e(TAG, "未找到ID为 btn_addSeconds 的Button");
        if (mBtnSubSeconds == null) LogUtils.e(TAG, "未找到ID为 btn_subSeconds 的Button");
        if (mBtnThisSeconds == null) LogUtils.e(TAG, "未找到ID为 btn_thisSeconds 的Button");

        LogUtils.i(TAG, "控件ID绑定完成");
    }

    /**
     * 初始化所有按钮的点击事件
     * 包含：加号、减号、快速设置量级按钮
     */
    private void initButtons() {
        LogUtils.i(TAG, "初始化按钮点击事件");

        // 1. 加号按钮：增加一个量级
        if (mBtnAddSeconds != null) {
            mBtnAddSeconds.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						LogUtils.i(TAG, "点击加号按钮，准备增加秒数");
						try {
							long current = Long.parseLong(mEtSeconds.getText().toString().trim());
							long newValue = current + SECONDS_INCREMENT;
							mEtSeconds.setText(String.valueOf(newValue));
							LogUtils.i(TAG, "增加成功，当前秒数: " + newValue);
						} catch (NumberFormatException e) {
							LogUtils.e(TAG, "当前输入格式错误，无法增加", e);
							mEtSeconds.setText("0");
						}
					}
				});
        }

        // 2. 减号按钮：减少一个量级，最低为0
        if (mBtnSubSeconds != null) {
            mBtnSubSeconds.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						LogUtils.i(TAG, "点击减号按钮，准备减少秒数");
						try {
							long current = Long.parseLong(mEtSeconds.getText().toString().trim());
							long newValue = Math.max(0, current - SECONDS_INCREMENT);
							mEtSeconds.setText(String.valueOf(newValue));
							LogUtils.i(TAG, "减少成功，当前秒数: " + newValue);
						} catch (NumberFormatException e) {
							LogUtils.e(TAG, "当前输入格式错误，无法减少", e);
							mEtSeconds.setText("0");
						}
					}
				});
        }

        // 3. [新增] 快速设置按钮：点击直接设置为量级数值 3600
        if (mBtnThisSeconds != null) {
            mBtnThisSeconds.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						LogUtils.i(TAG, "点击快速设置按钮，设置秒数为: " + SECONDS_INCREMENT);
						// 直接设置为常量数值
						mEtSeconds.setText(String.valueOf(SECONDS_INCREMENT));
						LogUtils.i(TAG, "快速设置成功，当前秒数: " + SECONDS_INCREMENT);
					}
				});
        }
    }

    /**
     * 初始化开关状态
     * 设置Switch的监听事件，以及背景色、按钮可点击状态
     */
    private void initSwitchState() {
        LogUtils.i(TAG, "初始化开关状态");
        boolean isChecked = mSwEnable.isChecked();

        // 设置初始UI状态
        updateUIState(!isChecked);

        LogUtils.i(TAG, "初始状态 - 开关: " + (isChecked ? "开启" : "关闭") + ", 背景色: " + (isChecked ? "绿色" : "白色") + ", 按钮可用: " + (!isChecked ? "是" : "否"));

        // 设置开关切换监听
        mSwEnable.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					LogUtils.i(TAG, "开关状态变更为: " + (isChecked ? "开启" : "关闭"));
					// 根据开关状态更新UI可操作状态
					updateUIState(!isChecked);

					if (isChecked) {
						startLimitedTimeSpecialChannelService();
					} else {
						LimitedTimeSpecialChannelService.stopService(mContext);
					}
				}
			});
    }

    /**
     * 统一更新UI可操作状态
     * @param enabled 可用状态
     */
    private void updateUIState(boolean enabled) {
        if (mEtSeconds != null) mEtSeconds.setEnabled(enabled);
        if (mBtnAddSeconds != null) mBtnAddSeconds.setEnabled(enabled);
        if (mBtnSubSeconds != null) mBtnSubSeconds.setEnabled(enabled);
        // [新增] 同步快速设置按钮状态
        if (mBtnThisSeconds != null) mBtnThisSeconds.setEnabled(enabled);

        if (mLlMain != null) {
            mLlMain.setBackgroundColor(enabled ? android.graphics.Color.WHITE : android.graphics.Color.GREEN);
        }
    }

    /**
     * 注册服务销毁本地广播监听
     * [修改] 接收广播时检索倒计时剩余秒数参数并设置到EditText
     */
    private void registerServiceDestroyedListener() {
        LogUtils.i(TAG, "注册服务销毁广播监听");
        IntentFilter filter = new IntentFilter(LimitedTimeSpecialChannelService.ACTION_SERVICE_DESTROYED);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(new android.content.BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (LimitedTimeSpecialChannelService.ACTION_SERVICE_DESTROYED.equals(intent.getAction())) {
						LogUtils.i(TAG, "收到服务销毁广播，执行恢复操作");

						// 1. 恢复开关状态
						if (mSwEnable != null && mSwEnable.isChecked()) {
							mSwEnable.setChecked(false);
						}

						// 2. [核心修改] 检索并设置倒计时剩余秒数参数
						// 从广播附加数据中获取之前服务发送的剩余秒数
						long remainingSeconds = intent.getLongExtra(LimitedTimeSpecialChannelService.EXTRA_REMAINING_SECONDS, 0);
						// 实际倒计时消耗冲正
						remainingSeconds -= 1;
						// 倒计时秒数0值保底
						remainingSeconds = remainingSeconds < 0? 0 : remainingSeconds;
						LogUtils.i(TAG, "从销毁广播中检索到剩余秒数参数: " + remainingSeconds + " 秒");
						//ToastUtils.show("从销毁广播中检索到剩余秒数参数: " + remainingSeconds + " 秒");

						// 3. 恢复所有UI状态为可用
						updateUIState(true);
						// 4. 设置EditText显示为倒计时剩余秒数
						if (mEtSeconds != null) {
							mEtSeconds.setText(String.valueOf(remainingSeconds));
							LogUtils.i(TAG, "已将EditText设置为剩余秒数: " + remainingSeconds);
						}

						//ToastUtils.show("服务已销毁，恢复倒计时剩余状态");
					}
				}
			}, filter);
    }

    /**
     * 注册倒计时心跳广播监听
     */
    private void registerCountdownTickListener() {
        LogUtils.i(TAG, "注册倒计时心跳广播监听");
        mCountdownReceiver = new android.content.BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (LimitedTimeSpecialChannelService.ACTION_COUNTDOWN_TICK.equals(intent.getAction())) {
                    long remainingSeconds = intent.getLongExtra(LimitedTimeSpecialChannelService.EXTRA_REMAINING_SECONDS, 0);
                    if (mEtSeconds != null) {
                        mEtSeconds.setText(String.valueOf(remainingSeconds));
                    }
					if (mSwEnable != null) {
                        mSwEnable.setChecked(remainingSeconds>0);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(LimitedTimeSpecialChannelService.ACTION_COUNTDOWN_TICK);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mCountdownReceiver, filter);
        LogUtils.i(TAG, "倒计时广播注册成功");
    }

    /**
     * 启动限时特殊通道服务
     */
    private void startLimitedTimeSpecialChannelService() {
        LogUtils.i(TAG, "准备启动限时特殊通道服务");
        String secondsStr = mEtSeconds.getText().toString().trim();
        if (secondsStr.isEmpty()) {
            LogUtils.w(TAG, "输入框为空，使用默认值 3600 秒");
            secondsStr = "3600";
        }

        try {
            long seconds = Long.parseLong(secondsStr);
            long delayMillis = seconds * 1000;

            LogUtils.i(TAG, "启动参数 - 输入秒数: " + seconds + ", 转换毫秒: " + delayMillis);
            //ToastUtils.show("调用 LimitedTimeSpecialChannelService 服务");
            LimitedTimeSpecialChannelService.startService(mContext, delayMillis);
        } catch (NumberFormatException e) {
            LogUtils.e(TAG, "输入的秒数格式错误: " + secondsStr, e);
        }
    }

    // ====================== 生命周期方法 =========================
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        LogUtils.i(TAG, "视图销毁，反注册所有广播接收器");

        if (mCountdownReceiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mCountdownReceiver);
            LogUtils.i(TAG, "反注册倒计时心跳广播接收器");
        }

        LogUtils.i(TAG, "所有广播反注册完成");
    }
}

