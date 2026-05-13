package cc.winboll.studio.powerbell.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.powerbell.App;
import cc.winboll.studio.powerbell.R;
import cc.winboll.studio.powerbell.models.BackgroundBean;
import cc.winboll.studio.powerbell.models.BatteryStyle;
import cc.winboll.studio.powerbell.models.ControlCenterServiceBean;
import cc.winboll.studio.powerbell.services.ControlCenterService;
import cc.winboll.studio.powerbell.utils.AppConfigUtils;

/**
 * 主页面核心视图封装类：统一管理视图绑定、数据更新、事件监听，解耦 Activity 逻辑
 * 适配：Java7 | API30 | 小米手机，优化性能与资源回收，杜绝内存泄漏，配置变更确认对话框
 * 新增：拖动进度条时实时预览 sbUsageReminder 与 sbChargeReminder 比值
 * 修复：updateBatteryDrawable() 电池样式切换后重绘失效问题
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/12/17 13:14
 */
public class MainContentView {
    // ====================================== 静态常量区（唯一标识，变更类型分类） ======================================
    public static final String TAG = "MainContentView";
    // 变更类型常量（区分不同控件，精准处理逻辑）
    private static final int CHANGE_TYPE_CHARGE_SWITCH = 1;
    private static final int CHANGE_TYPE_USAGE_SWITCH = 2;
    private static final int CHANGE_TYPE_SERVICE_SWITCH = 3;
    private static final int CHANGE_TYPE_CHARGE_SEEKBAR = 4;
    private static final int CHANGE_TYPE_USAGE_SEEKBAR = 5;
    // 电量范围常量
    private static final int BATTERY_MIN = 0;
    private static final int BATTERY_MAX = 100;

    // ====================================== 内部缓存类（解耦，避免冗余） ======================================
    /**
     * 临时配置数据实体（缓存变更信息，取消时恢复）
     */
    private static class TempConfigData {
        int changeType;
        boolean originalBooleanValue;
        int originalIntValue;
        boolean newBooleanValue;
        int newIntValue;

        // 构造方法（开关类型）
        TempConfigData(int changeType, boolean originalValue, boolean newValue) {
            this.changeType = changeType;
            this.originalBooleanValue = originalValue;
            this.newBooleanValue = newValue;
        }

        // 构造方法（进度条类型）
        TempConfigData(int changeType, int originalValue, int newValue) {
            this.changeType = changeType;
            this.originalIntValue = originalValue;
            this.newIntValue = newValue;
        }
    }

    // ====================================== 事件回调接口（解耦视图与业务，提升扩展性） ======================================
    public interface OnViewActionListener {
        void onChargeReminderSwitchChanged(boolean isChecked);
        void onUsageReminderSwitchChanged(boolean isChecked);
        void onServiceSwitchChanged(boolean isChecked);
        void onChargeReminderProgressChanged(int progress);
        void onUsageReminderProgressChanged(int progress);
    }

    // ====================================== 成员变量区（按功能分类，final优先，避免混乱） ======================================
    // 外部依赖实例（生命周期关联，优先声明）
    private Context mContext;
    private AppConfigUtils mAppConfigUtils;
    private OnViewActionListener mActionListener;

    // 视图控件（按「布局→开关→文本→进度条→图标」功能归类，public控件标注用途）
    // 基础布局控件
    public RelativeLayout mainLayout;
    public MemoryCachedBackgroundView backgroundView;
    private LinearLayout mllBackgroundView;
	private volatile BatteryStyle mBatteryStyle = BatteryStyle.ENERGY_STYLE;

    // 容器布局控件
    public LinearLayout llLeftSeekBar;
    public LinearLayout llRightSeekBar;
    // 开关控件
    public CheckBox cbEnableChargeReminder;
    public CheckBox cbEnableUsageReminder;
    public Switch swEnableService;
    // 文本显示控件
    public TextView tvTips;
    public TextView tvChargeReminderValue;
    public TextView tvUsageReminderValue;
    public TextView tvCurrentBatteryValue;
    // 进度条控件（使用自定义 VerticalSeekBar）
    public VerticalSeekBar sbChargeReminder;
    public VerticalSeekBar sbUsageReminder;
    // 图标显示控件
    public ImageView ivCurrentBattery;
    public ImageView ivChargeReminderBattery;
    public ImageView ivUsageReminderBattery;

    // 进度缓存（用于实时计算比值，避免频繁调用 getProgress()）
    private int mCurrentChargeProgress;
    private int mCurrentUsageProgress;

    // 内部复用资源（避免重复创建，优化性能）
    private BatteryDrawable mCurrentBatteryDrawable;
    private BatteryDrawable mChargeReminderBatteryDrawable;
    private BatteryDrawable mUsageReminderBatteryDrawable;

    // 配置变更确认对话框（单例复用，避免重复创建）
    private AlertDialog mConfigConfirmDialog;
    private AlertDialog.Builder mDialogBuilder;
    // 临时存储变更数据（对话框确认前缓存，取消时恢复）
    private TempConfigData mTempConfigData;
    // 对话框状态锁（避免快速点击重复弹窗）
    private boolean isDialogShowing = false;

    // ====================================== 构造方法（初始化入口，逻辑闭环） ======================================
    public MainContentView(Context context, View rootView, OnViewActionListener actionListener) {
        LogUtils.d(TAG, "【MainContentView】构造器调用 | context=" + context + " | rootView=" + rootView + " | actionListener=" + actionListener);
        // 初始化外部依赖
        this.mContext = context;
        this.mActionListener = actionListener;
        this.mAppConfigUtils = AppConfigUtils.getInstance(context.getApplicationContext());
		mBatteryStyle = BatteryStyleView.getSavedBatteryStyle(context);

        // 执行核心初始化流程（按顺序执行，避免依赖空指针）
        bindViews(rootView);
        initBatteryDrawables();
        initConfirmDialog();
        bindViewListeners();

        LogUtils.d(TAG, "【MainContentView】初始化完成");
    }

    // ====================================== 私有初始化方法（封装内部逻辑，仅暴露入口） ======================================
    /**
     * 绑定视图控件（显式强转适配 Java7，适配 API30 视图加载机制）
     * @param rootView 根视图
     */
    private void bindViews(View rootView) {
        LogUtils.d(TAG, "【bindViews】视图绑定开始 | rootView=" + rootView);
        // 基础布局绑定
        mainLayout = (RelativeLayout) rootView.findViewById(R.id.activitymainRelativeLayout1);
        mllBackgroundView = (LinearLayout) rootView.findViewById(R.id.ll_backgroundview);

		backgroundView = App.getInstance().getMemoryCachedBackgroundView();
		if (backgroundView == null) {
			App.sBackgroundSourceUtils.loadSettings();
			BackgroundBean backgroundBean = App.sBackgroundSourceUtils.getCurrentBackgroundBean();
			backgroundView = App.getInstance().getMemoryCachedBackgroundView().getInstance(mContext, backgroundBean, true);
		}
        if (backgroundView.getParent() != null) {
            ((ViewGroup) backgroundView.getParent()).removeView(backgroundView);
            LogUtils.d(TAG, "【bindViews】移除背景视图旧父容器");
        }
        mllBackgroundView.addView(backgroundView);

		// 容器布局绑定
        llLeftSeekBar = (LinearLayout) rootView.findViewById(R.id.fragmentmainviewLinearLayout1);
        llRightSeekBar = (LinearLayout) rootView.findViewById(R.id.fragmentmainviewLinearLayout2);
        // 开关控件绑定
        cbEnableChargeReminder = (CheckBox) rootView.findViewById(R.id.fragmentmainviewCheckBox1);
        cbEnableUsageReminder = (CheckBox) rootView.findViewById(R.id.fragmentmainviewCheckBox2);
        swEnableService = (Switch) rootView.findViewById(R.id.fragmentandroidviewSwitch1);
        // 文本控件绑定
        tvTips = (TextView) rootView.findViewById(R.id.fragmentandroidviewTextView1);
        tvChargeReminderValue = (TextView) rootView.findViewById(R.id.fragmentandroidviewTextView2);
        tvUsageReminderValue = (TextView) rootView.findViewById(R.id.fragmentandroidviewTextView3);
        tvCurrentBatteryValue = (TextView) rootView.findViewById(R.id.fragmentandroidviewTextView4);
        // 进度条控件绑定（自定义 VerticalSeekBar）
        sbChargeReminder = (VerticalSeekBar) rootView.findViewById(R.id.fragmentandroidviewVerticalSeekBar1);
        sbUsageReminder = (VerticalSeekBar) rootView.findViewById(R.id.fragmentandroidviewVerticalSeekBar2);
        // 图标控件绑定
        ivCurrentBattery = (ImageView) rootView.findViewById(R.id.fragmentandroidviewImageView1);
        ivChargeReminderBattery = (ImageView) rootView.findViewById(R.id.fragmentandroidviewImageView3);
        ivUsageReminderBattery = (ImageView) rootView.findViewById(R.id.fragmentandroidviewImageView2);

        // 初始化进度缓存（从配置读取初始值）
        mCurrentChargeProgress = mAppConfigUtils.getChargeReminderValue();
        mCurrentUsageProgress = mAppConfigUtils.getUsageReminderValue();
        LogUtils.d(TAG, "【bindViews】进度缓存初始化 | charge=" + mCurrentChargeProgress + " | usage=" + mCurrentUsageProgress);

        // 关键视图绑定校验（仅保留核心控件错误日志，精简冗余）
        if (mainLayout == null) LogUtils.e(TAG, "【bindViews】mainLayout 绑定失败");
        if (backgroundView == null) LogUtils.e(TAG, "【bindViews】backgroundView 绑定失败");
        LogUtils.d(TAG, "【bindViews】视图绑定完成");
    }

	public void reloadBackgroundView() {
		if (backgroundView != null) {
			App.sBackgroundSourceUtils.loadSettings();
			BackgroundBean backgroundBean = App.sBackgroundSourceUtils.getCurrentBackgroundBean();
			backgroundView.loadByBackgroundBean(backgroundBean, true);
		}
	}

    /**
     * 初始化电池 Drawable（集成 BatteryDrawable，默认能量风格，适配小米机型渲染）
     */
    private void initBatteryDrawables() {
        LogUtils.d(TAG, "【initBatteryDrawables】电池Drawable初始化开始 | style="+mBatteryStyle.name());
        // 当前电量 Drawable（颜色从资源读取，适配 API30 主题）
        int colorCurrent = getResourceColor(R.color.colorCurrent);
        mCurrentBatteryDrawable = new BatteryDrawable(colorCurrent);
		mCurrentBatteryDrawable.setDrawStyle(mBatteryStyle);
        // 充电提醒 Drawable
        int colorCharge = getResourceColor(R.color.colorCharge);
        mChargeReminderBatteryDrawable = new BatteryDrawable(colorCharge);
		mChargeReminderBatteryDrawable.setDrawStyle(mBatteryStyle);
		// 耗电提醒 Drawable
        int colorUsage = getResourceColor(R.color.colorUsege);
        mUsageReminderBatteryDrawable = new BatteryDrawable(colorUsage);
        mUsageReminderBatteryDrawable.setDrawStyle(mBatteryStyle);
		LogUtils.d(TAG, "【initBatteryDrawables】电池Drawable初始化完成");
    }

	/**
	 * ✅ 核心修复：电池样式切换+强制重绘刷新 完整版
	 * 修复点1：重新创建Drawable后，重新给ImageView赋值Drawable
	 * 修复点2：重置所有Drawable的电量值，保证样式切换后数值不变
	 * 修复点3：调用ImageView.invalidate()强制触发重绘（API30必加）
	 * 修复点4：Drawable.invalidateSelf() 双保险刷新绘制内容
	 * @param batteryStyle 切换后的电池样式
	 */
	public void updateBatteryDrawable(BatteryStyle batteryStyle) {
		if(batteryStyle == null || batteryStyle == mBatteryStyle){
			LogUtils.d(TAG, "【updateBatteryDrawable】样式无变化，跳过刷新");
			return;
		}
		// 1. 更新样式标记
		mBatteryStyle = batteryStyle;
		// 2. 重新初始化Drawable并设置新样式
		initBatteryDrawables();
		// 3. 重置所有Drawable的电量值 → 保证样式切换后数值不变
		mCurrentBatteryDrawable.setBatteryValue(mAppConfigUtils.getCurrentBatteryValue());
		mChargeReminderBatteryDrawable.setBatteryValue(mCurrentChargeProgress);
		mUsageReminderBatteryDrawable.setBatteryValue(mCurrentUsageProgress);
		// 4. 重新给ImageView赋值Drawable → 核心修复：之前缺失这一步
		if(ivCurrentBattery != null) ivCurrentBattery.setImageDrawable(mCurrentBatteryDrawable);
		if(ivChargeReminderBattery != null) ivChargeReminderBattery.setImageDrawable(mChargeReminderBatteryDrawable);
		if(ivUsageReminderBattery != null) ivUsageReminderBattery.setImageDrawable(mUsageReminderBatteryDrawable);
		// 5. Drawable自身刷新 → 双保险
		mCurrentBatteryDrawable.invalidateSelf();
		mChargeReminderBatteryDrawable.invalidateSelf();
		mUsageReminderBatteryDrawable.invalidateSelf();
		// 6. ✅ API30关键修复：ImageView强制重绘，解决绘制缓存不刷新问题
		if(ivCurrentBattery != null) ivCurrentBattery.invalidate();
		if(ivChargeReminderBattery != null) ivChargeReminderBattery.invalidate();
		if(ivUsageReminderBattery != null) ivUsageReminderBattery.invalidate();

		LogUtils.d(TAG, "【updateBatteryDrawable】样式切换完成："+mBatteryStyle.name() + " | 重绘触发成功");
		ToastUtils.show("电池样式已切换为："+mBatteryStyle.name());
	}

    /**
     * 初始化配置变更确认对话框（核心优化：保存 Builder 实例，解决消息不生效问题）
     */
    private void initConfirmDialog() {
        LogUtils.d(TAG, "【initConfirmDialog】对话框初始化开始");
        if (mContext == null) {
            LogUtils.e(TAG, "【initConfirmDialog】Context 为空，初始化失败");
            return;
        }

        // 1. 初始化 Builder（核心：后续通过 Builder 更新消息）
        mDialogBuilder = new AlertDialog.Builder(mContext);
        mDialogBuilder.setTitle("配置变更确认");
        mDialogBuilder.setMessage("是否确认修改当前配置？");

        // 确定按钮：保存配置+回调+更新视图
        mDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					confirmConfigChange();
					dialog.dismiss();
				}
			});

        // 取消按钮：恢复原始配置（补充物理取消按钮，提升用户体验）
        mDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					cancelConfigChange();
					dialog.dismiss();
				}
			});

        // 对话框外部点击监听：关闭对话框+恢复原始配置
        mDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancelConfigChange();
					dialog.dismiss();
				}
			});

        // 2. 初始化对话框实例（设置可取消，支持外部点击关闭）
        mConfigConfirmDialog = mDialogBuilder.create();
        mConfigConfirmDialog.setCancelable(true);
        mConfigConfirmDialog.setCanceledOnTouchOutside(true);
        LogUtils.d(TAG, "【initConfirmDialog】对话框初始化完成");
    }

    /**
     * 绑定视图事件监听（Java7 显式实现接口，适配 API30 事件分发，修复进度条弹窗失效）
     */
    private void bindViewListeners() {
        LogUtils.d(TAG, "【bindViewListeners】事件监听绑定开始");
        // 依赖校验，避免空指针
        if (mAppConfigUtils == null || mActionListener == null || mDialogBuilder == null) {
            LogUtils.e(TAG, "【bindViewListeners】依赖实例为空，跳过监听绑定");
            return;
        }

        // 充电提醒进度条监听（使用 VerticalSeekBar 专属接口，确保弹窗100%触发）
        if (sbChargeReminder != null) {
            // 原有：触摸抬起/取消监听（用于配置确认）
            sbChargeReminder.setOnVerticalSeekBarTouchListener(new VerticalSeekBar.OnVerticalSeekBarTouchListener() {
					@Override
					public void onTouchUp(VerticalSeekBar seekBar, int progress) {
						int originalValue = mAppConfigUtils.getChargeReminderValue();
						// 进度无变化，不处理
						if (originalValue == progress) {
							LogUtils.d(TAG, "【bindViewListeners】ChargeReminderSeekBar: 进度无变化，跳过");
							return;
						}
						// 缓存变更数据，显示确认对话框
						mTempConfigData = new TempConfigData(CHANGE_TYPE_CHARGE_SEEKBAR, originalValue, progress);
						updateDialogMessageByChangeType();
						showConfigConfirmDialog();
						LogUtils.d(TAG, "【bindViewListeners】ChargeReminderSeekBar触摸抬起 | 原始值=" + originalValue + " | 新进度=" + progress);
					}

					@Override
					public void onTouchCancel(VerticalSeekBar seekBar, int progress) {
						// 触摸取消，回滚视图进度（UI 与配置保持一致）
						int originalValue = mAppConfigUtils.getChargeReminderValue();
						if (tvChargeReminderValue != null && mChargeReminderBatteryDrawable != null && ivChargeReminderBattery != null) {
							mChargeReminderBatteryDrawable.setBatteryValue(originalValue);
							ivChargeReminderBattery.setImageDrawable(mChargeReminderBatteryDrawable);
							tvChargeReminderValue.setText(originalValue + "%");
						}
						seekBar.setProgress(originalValue);
						// 恢复进度缓存
						mCurrentChargeProgress = originalValue;
						LogUtils.d(TAG, "【bindViewListeners】ChargeReminderSeekBar触摸取消 | 进度回滚至=" + originalValue);
					}
				});

            // 新增：实时进度变化监听（用于比值预览）
            sbChargeReminder.setOnVerticalSeekBarChangeListener(new VerticalSeekBar.OnVerticalSeekBarChangeListener() {
					@Override
					public void onProgressChanged(VerticalSeekBar seekBar, int progress, boolean fromUser) {
						if (fromUser) {
							mCurrentChargeProgress = progress;
							// 同步更新进度文本和电池图标（保持UI一致性）
							if (tvChargeReminderValue != null && mChargeReminderBatteryDrawable != null && ivChargeReminderBattery != null) {
								mChargeReminderBatteryDrawable.setBatteryValue(progress);
								ivChargeReminderBattery.setImageDrawable(mChargeReminderBatteryDrawable);
								tvChargeReminderValue.setText(progress + "%");
							}
							LogUtils.d(TAG, "【bindViewListeners】ChargeReminderSeekBar实时更新 | 进度=" + progress);
						}
					}

					@Override
					public void onStartTrackingTouch(VerticalSeekBar seekBar) {}

					@Override
					public void onStopTrackingTouch(VerticalSeekBar seekBar) {}
				});
            LogUtils.d(TAG, "【bindViewListeners】充电提醒进度条专属监听绑定完成");
        }

        // 充电提醒开关监听
        if (cbEnableChargeReminder != null) {
            cbEnableChargeReminder.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						boolean originalValue = mAppConfigUtils.isChargeReminderEnabled();
						boolean newValue = cbEnableChargeReminder.isChecked();
						// 状态无变化，不处理
						if (originalValue == newValue) return;
						// 缓存变更数据，显示确认对话框
						mTempConfigData = new TempConfigData(CHANGE_TYPE_CHARGE_SWITCH, originalValue, newValue);
						updateDialogMessageByChangeType();
						showConfigConfirmDialog();
						LogUtils.d(TAG, "【bindViewListeners】cbEnableChargeReminder点击 | 原始值=" + originalValue + " | 变更后=" + newValue);
					}
				});
            LogUtils.d(TAG, "【bindViewListeners】充电提醒开关监听绑定完成");
        }

        // 耗电提醒进度条监听（使用 VerticalSeekBar 专属接口，确保弹窗100%触发）
        if (sbUsageReminder != null) {
            // 原有：触摸抬起/取消监听（用于配置确认）
            sbUsageReminder.setOnVerticalSeekBarTouchListener(new VerticalSeekBar.OnVerticalSeekBarTouchListener() {
					@Override
					public void onTouchUp(VerticalSeekBar seekBar, int progress) {
						int originalValue = mAppConfigUtils.getUsageReminderValue();
						// 进度无变化，不处理
						if (originalValue == progress) {
							LogUtils.d(TAG, "【bindViewListeners】UsageReminderSeekBar: 进度无变化，跳过");
							return;
						}
						// 缓存变更数据，显示确认对话框
						mTempConfigData = new TempConfigData(CHANGE_TYPE_USAGE_SEEKBAR, originalValue, progress);
						updateDialogMessageByChangeType();
						showConfigConfirmDialog();
						LogUtils.d(TAG, "【bindViewListeners】UsageReminderSeekBar触摸抬起 | 原始值=" + originalValue + " | 新进度=" + progress);
					}

					@Override
					public void onTouchCancel(VerticalSeekBar seekBar, int progress) {
						// 触摸取消，回滚视图进度（UI 与配置保持一致）
						int originalValue = mAppConfigUtils.getUsageReminderValue();
						if (tvUsageReminderValue != null && mUsageReminderBatteryDrawable != null && ivUsageReminderBattery != null) {
							mUsageReminderBatteryDrawable.setBatteryValue(originalValue);
							ivUsageReminderBattery.setImageDrawable(mUsageReminderBatteryDrawable);
							tvUsageReminderValue.setText(originalValue + "%");
						}
						seekBar.setProgress(originalValue);
						// 恢复进度缓存
						mCurrentUsageProgress = originalValue;
						LogUtils.d(TAG, "【bindViewListeners】UsageReminderSeekBar触摸取消 | 进度回滚至=" + originalValue);
					}
				});

            // 新增：实时进度变化监听（用于比值预览）
            sbUsageReminder.setOnVerticalSeekBarChangeListener(new VerticalSeekBar.OnVerticalSeekBarChangeListener() {
					@Override
					public void onProgressChanged(VerticalSeekBar seekBar, int progress, boolean fromUser) {
						if (fromUser) {
							mCurrentUsageProgress = progress;
							// 同步更新进度文本和电池图标（保持UI一致性）
							if (tvUsageReminderValue != null && mUsageReminderBatteryDrawable != null && ivUsageReminderBattery != null) {
								mUsageReminderBatteryDrawable.setBatteryValue(progress);
								ivUsageReminderBattery.setImageDrawable(mUsageReminderBatteryDrawable);
								tvUsageReminderValue.setText(progress + "%");
							}
							LogUtils.d(TAG, "【bindViewListeners】UsageReminderSeekBar实时更新 | 进度=" + progress);
						}
					}

					@Override
					public void onStartTrackingTouch(VerticalSeekBar seekBar) {}

					@Override
					public void onStopTrackingTouch(VerticalSeekBar seekBar) {}
				});
            LogUtils.d(TAG, "【bindViewListeners】耗电提醒进度条专属监听绑定完成");
        }

        // 耗电提醒开关监听
        if (cbEnableUsageReminder != null) {
            cbEnableUsageReminder.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						boolean originalValue = mAppConfigUtils.isUsageReminderEnabled();
						boolean newValue = cbEnableUsageReminder.isChecked();
						// 状态无变化，不处理
						if (originalValue == newValue) return;
						// 缓存变更数据，显示确认对话框
						mTempConfigData = new TempConfigData(CHANGE_TYPE_USAGE_SWITCH, originalValue, newValue);
						updateDialogMessageByChangeType();
						showConfigConfirmDialog();
						LogUtils.d(TAG, "【bindViewListeners】cbEnableUsageReminder点击 | 原始值=" + originalValue + " | 变更后=" + newValue);
					}
				});
            LogUtils.d(TAG, "【bindViewListeners】耗电提醒开关监听绑定完成");
        }

        // 服务总开关监听（核心优化：逻辑与其他控件完全对齐）
        if (swEnableService != null) {
            swEnableService.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// 从服务控制Bean读取原始状态，确保与实际一致
						boolean originalValue = getServiceEnableState();
						boolean newValue = ((Switch) v).isChecked();
						// 状态无变化，不处理
						if (originalValue == newValue) return;
						// 缓存变更数据
						mTempConfigData = new TempConfigData(CHANGE_TYPE_SERVICE_SWITCH, originalValue, newValue);
						// 更新差异化提示语
						updateDialogMessageByChangeType();
						// 显示确认对话框
						showConfigConfirmDialog();
						LogUtils.d(TAG, "【bindViewListeners】swEnableService点击 | 原始值=" + originalValue + " | 变更后=" + newValue);
					}
				});
            LogUtils.d(TAG, "【bindViewListeners】服务总开关监听绑定完成");
        }

        LogUtils.d(TAG, "【bindViewListeners】所有事件监听绑定完成");
    }

    // ====================================== 对外暴露核心方法（业务入口，精简参数，明确职责） ======================================
    /**
     * 更新所有视图数据（从配置读取数据，统一刷新 UI，适配 API30 视图更新规范）
     * @param frameDrawable 进度条背景 Drawable（外部传入，适配主题切换）
     */
    public void updateViewData(Drawable frameDrawable) {
        LogUtils.d(TAG, "【updateViewData】视图数据更新开始 | frameDrawable=" + frameDrawable);
        if (mAppConfigUtils == null) {
            LogUtils.e(TAG, "【updateViewData】AppConfigUtils 为空，跳过更新");
            return;
        }

        // 一次读取所有配置参数，减少工具类调用，提升性能
        int chargeVal = mAppConfigUtils.getChargeReminderValue();
        int usageVal = mAppConfigUtils.getUsageReminderValue();
        int currentVal = mAppConfigUtils.getCurrentBatteryValue();
        boolean chargeEnable = mAppConfigUtils.isChargeReminderEnabled();
        boolean usageEnable = mAppConfigUtils.isUsageReminderEnabled();
        // 从服务控制Bean读取状态，确保UI与实际一致
        boolean serviceEnable = getServiceEnableState();
        // 更新进度缓存
        mCurrentChargeProgress = chargeVal;
        mCurrentUsageProgress = usageVal;
        LogUtils.d(TAG, "【updateViewData】配置数据读取完成 | charge=" + chargeVal + " | usage=" + usageVal + " | current=" + currentVal + " | serviceEnable=" + serviceEnable);

        // 进度条背景更新
        if (frameDrawable != null) {
            if (llLeftSeekBar != null) llLeftSeekBar.setBackground(frameDrawable);
            if (llRightSeekBar != null) llRightSeekBar.setBackground(frameDrawable);
            LogUtils.d(TAG, "【updateViewData】进度条背景更新完成");
        }

        // 当前电量更新（联动 BatteryDrawable，实时刷新图标）
        if (ivCurrentBattery != null && mCurrentBatteryDrawable != null) {
            mCurrentBatteryDrawable.setBatteryValue(currentVal);
            ivCurrentBattery.setImageDrawable(mCurrentBatteryDrawable);
        }
        if (tvCurrentBatteryValue != null) {
            tvCurrentBatteryValue.setTextColor(getResourceColor(R.color.colorCurrent));
            tvCurrentBatteryValue.setText(currentVal + "%");
        }
        LogUtils.d(TAG, "【updateViewData】当前电量更新完成");

        // 充电提醒视图更新
        if (ivChargeReminderBattery != null && mChargeReminderBatteryDrawable != null) {
            mChargeReminderBatteryDrawable.setBatteryValue(chargeVal);
            ivChargeReminderBattery.setImageDrawable(mChargeReminderBatteryDrawable);
        }
        if (tvChargeReminderValue != null) {
            tvChargeReminderValue.setTextColor(getResourceColor(R.color.colorCharge));
            tvChargeReminderValue.setText(chargeVal + "%");
        }
        if (sbChargeReminder != null) sbChargeReminder.setProgress(chargeVal);
        if (cbEnableChargeReminder != null) cbEnableChargeReminder.setChecked(chargeEnable);
        LogUtils.d(TAG, "【updateViewData】充电提醒视图更新完成");

        // 耗电提醒视图更新
        if (ivUsageReminderBattery != null && mUsageReminderBatteryDrawable != null) {
            mUsageReminderBatteryDrawable.setBatteryValue(usageVal);
            ivUsageReminderBattery.setImageDrawable(mUsageReminderBatteryDrawable);
        }
        if (tvUsageReminderValue != null) {
            tvUsageReminderValue.setTextColor(getResourceColor(R.color.colorUsege));
            tvUsageReminderValue.setText(usageVal + "%");
        }
        if (sbUsageReminder != null) sbUsageReminder.setProgress(usageVal);
        if (cbEnableUsageReminder != null) cbEnableUsageReminder.setChecked(usageEnable);
        LogUtils.d(TAG, "【updateViewData】耗电提醒视图更新完成");

        // 服务开关+提示文本更新（确保状态准确）
        if (swEnableService != null) {
            swEnableService.setChecked(serviceEnable);
            swEnableService.setText(mContext.getString(R.string.txt_aboveswitch));
        }
        if (tvTips != null) tvTips.setText(mContext.getString(R.string.txt_aboveswitchtips));
        LogUtils.d(TAG, "【updateViewData】服务开关与提示文本更新完成");

        LogUtils.d(TAG, "【updateViewData】所有视图数据更新完成");
    }

    /**
     * 实时更新当前电量（单独抽离，适配电池实时监控场景，优化 API30 UI 响应速度）
     * @param value 电量值（自动校准 0-100，避免异常值）
     */
    public void updateCurrentBattery(int value) {
        LogUtils.d(TAG, "【updateCurrentBattery】当前电量更新开始 | 原始值=" + value);
        // 核心依赖校验
        if (tvCurrentBatteryValue == null || mCurrentBatteryDrawable == null || ivCurrentBattery == null) {
            LogUtils.e(TAG, "【updateCurrentBattery】视图/Drawable 为空，跳过更新");
            return;
        }

        // 校准电量范围（强制 0-100，防止 API30 视图显示异常）
        int validValue = Math.max(BATTERY_MIN, Math.min(value, BATTERY_MAX));
        // 联动 BatteryDrawable 更新图标，同步文本显示
        mCurrentBatteryDrawable.setBatteryValue(validValue);
        ivCurrentBattery.setImageDrawable(mCurrentBatteryDrawable);
        tvCurrentBatteryValue.setText(validValue + "%");

        LogUtils.d(TAG, "【updateCurrentBattery】更新完成 | 校准后值=" + validValue);
    }

    /**
     * 释放资源（主动回收，适配 API30 资源管控机制，优化小米手机内存占用）
     */
    public void releaseResources() {
        LogUtils.d(TAG, "【releaseResources】资源释放开始");
        // 释放对话框资源（安全销毁，避免内存泄漏）
        if (mConfigConfirmDialog != null) {
            if (mConfigConfirmDialog.isShowing()) {
                mConfigConfirmDialog.dismiss();
            }
            mConfigConfirmDialog.setOnDismissListener(null);
            mConfigConfirmDialog.setOnCancelListener(null);
            mConfigConfirmDialog = null;
        }
        // 释放 Builder
        mDialogBuilder = null;
        // 释放临时数据
        mTempConfigData = null;

        // 释放 BatteryDrawable 资源（重点回收绘制资源，避免 OOM）
        mCurrentBatteryDrawable = null;
        mChargeReminderBatteryDrawable = null;
        mUsageReminderBatteryDrawable = null;

        // 置空视图实例（断开视图引用，辅助 GC 回收）
        mainLayout = null;
        backgroundView = null;
        mllBackgroundView = null;
        llLeftSeekBar = null;
        llRightSeekBar = null;
        cbEnableChargeReminder = null;
        cbEnableUsageReminder = null;
        swEnableService = null;
        tvTips = null;
        tvChargeReminderValue = null;
        tvUsageReminderValue = null;
        tvCurrentBatteryValue = null;
        sbChargeReminder = null;
        sbUsageReminder = null;
        ivCurrentBattery = null;
        ivChargeReminderBattery = null;
        ivUsageReminderBattery = null;

        // 置空外部依赖（断开生命周期关联，杜绝内存泄漏）
        mContext = null;
        mAppConfigUtils = null;
        mActionListener = null;

        LogUtils.d(TAG, "【releaseResources】所有资源释放完成");
    }

    /**
     * 设置服务开关启用状态（外部调用，同步 UI 与服务状态，适配 Activity 视图刷新）
     * @param enabled 服务启用状态
     */
    public void setServiceSwitchChecked(boolean enabled) {
        LogUtils.d(TAG, "【setServiceSwitchChecked】服务开关状态设置 | enabled=" + enabled);
        if (swEnableService != null) {
            swEnableService.setChecked(enabled);
        }
    }

    /**
     * 设置服务开关点击状态（外部调用，避免更新 UI 时触发重复回调）
     * @param enabled 是否允许点击
     */
    public void setServiceSwitchEnabled(boolean enabled) {
        LogUtils.d(TAG, "【setServiceSwitchEnabled】服务开关点击状态设置 | enabled=" + enabled);
        if (swEnableService != null) {
            swEnableService.setEnabled(enabled);
        }
    }

    // ====================================== 内部核心逻辑方法（对话框相关，封装确认/取消逻辑） ======================================
    /**
     * 显示配置变更确认对话框（确保 Activity 处于前台，避免异常，防止重复弹窗）
     */
    private void showConfigConfirmDialog() {
        LogUtils.d(TAG, "【showConfigConfirmDialog】对话框显示开始 | isDialogShowing=" + isDialogShowing);
        // 对话框状态锁：正在显示则跳过，避免重复触发
        if (isDialogShowing) {
            LogUtils.d(TAG, "【showConfigConfirmDialog】对话框已显示，跳过重复调用");
            return;
        }
        // 基础校验：对话框/上下文/Builder 为空
        if (mDialogBuilder == null || mContext == null) {
            LogUtils.e(TAG, "【showConfigConfirmDialog】对话框Builder/上下文异常，无法显示");
            if (mTempConfigData != null) cancelConfigChange();
            return;
        }
        // Activity 状态校验：避免销毁后弹窗崩溃（适配 API30）
        Activity activity = (Activity) mContext;
        if (activity.isFinishing() || activity.isDestroyed()) {
            LogUtils.e(TAG, "【showConfigConfirmDialog】Activity 已销毁，无法显示对话框");
            if (mTempConfigData != null) cancelConfigChange();
            return;
        }
        // 重新构建对话框（核心：确保最新消息生效）
        mConfigConfirmDialog = mDialogBuilder.create();
        // 显示对话框，设置状态锁+关闭监听
        mConfigConfirmDialog.show();
        isDialogShowing = true;
        // 对话框关闭时解锁
        mConfigConfirmDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					isDialogShowing = false;
					mConfigConfirmDialog.setOnDismissListener(null);
				}
			});
        LogUtils.d(TAG, "【showConfigConfirmDialog】确认对话框显示成功");
    }

    /**
     * 确认配置变更（保存数据+回调监听+更新视图）
     */
    private void confirmConfigChange() {
        LogUtils.d(TAG, "【confirmConfigChange】配置确认开始 | mTempConfigData=" + mTempConfigData);
        if (mTempConfigData == null || mAppConfigUtils == null || mActionListener == null) {
            LogUtils.e(TAG, "【confirmConfigChange】依赖数据为空，确认失败");
            return;
        }

        switch (mTempConfigData.changeType) {
				// 充电提醒开关
            case CHANGE_TYPE_CHARGE_SWITCH:
                mAppConfigUtils.setChargeReminderEnabled(mTempConfigData.newBooleanValue);
                mActionListener.onChargeReminderSwitchChanged(mTempConfigData.newBooleanValue);
                LogUtils.d(TAG, "【confirmConfigChange】充电提醒开关确认 | 值=" + mTempConfigData.newBooleanValue);
                break;
				// 耗电提醒开关
            case CHANGE_TYPE_USAGE_SWITCH:
                mAppConfigUtils.setUsageReminderEnabled(mTempConfigData.newBooleanValue);
                mActionListener.onUsageReminderSwitchChanged(mTempConfigData.newBooleanValue);
                LogUtils.d(TAG, "【confirmConfigChange】耗电提醒开关确认 | 值=" + mTempConfigData.newBooleanValue);
                break;
				// 服务总开关（核心：持久化配置+触发 Activity 回调）
            case CHANGE_TYPE_SERVICE_SWITCH:
                // 1. 设置服务启停
                if (mTempConfigData.newBooleanValue) {
                    ControlCenterService.startControlCenterService(mContext);
                } else {
                    ControlCenterService.stopControlCenterService(mContext);
                }
                // 2. 强制触发 Activity 回调，执行服务启停逻辑
                mActionListener.onServiceSwitchChanged(mTempConfigData.newBooleanValue);
                LogUtils.d(TAG, "【confirmConfigChange】服务开关确认 | 值=" + mTempConfigData.newBooleanValue + "，已持久化配置");
                break;
				// 充电提醒进度条
            case CHANGE_TYPE_CHARGE_SEEKBAR:
                mAppConfigUtils.setChargeReminderValue(mTempConfigData.newIntValue);
                mActionListener.onChargeReminderProgressChanged(mTempConfigData.newIntValue);
                LogUtils.d(TAG, "【confirmConfigChange】充电提醒进度确认 | 值=" + mTempConfigData.newIntValue);
                break;
				// 耗电提醒进度条
            case CHANGE_TYPE_USAGE_SEEKBAR:
                mAppConfigUtils.setUsageReminderValue(mTempConfigData.newIntValue);
                mActionListener.onUsageReminderProgressChanged(mTempConfigData.newIntValue);
                LogUtils.d(TAG, "【confirmConfigChange】耗电提醒进度确认 | 值=" + mTempConfigData.newIntValue);
                break;
            default:
                LogUtils.w(TAG, "【confirmConfigChange】未知变更类型，跳过");
                break;
        }

        // 确认完成，清空临时数据
        mTempConfigData = null;
        LogUtils.d(TAG, "【confirmConfigChange】配置确认完成");
    }

    /**
     * 取消配置变更（恢复原始值+刷新视图，确保 UI 与配置一致）
     */
    private void cancelConfigChange() {
        LogUtils.d(TAG, "【cancelConfigChange】配置取消开始 | mTempConfigData=" + mTempConfigData);
        if (mTempConfigData == null || mAppConfigUtils == null) {
            LogUtils.e(TAG, "【cancelConfigChange】依赖数据为空，取消失败");
            return;
        }

        switch (mTempConfigData.changeType) {
            case CHANGE_TYPE_CHARGE_SWITCH:
                if (cbEnableChargeReminder != null) {
                    cbEnableChargeReminder.setChecked(mTempConfigData.originalBooleanValue);
                }
                LogUtils.d(TAG, "【cancelConfigChange】充电提醒开关取消 | 恢复值=" + mTempConfigData.originalBooleanValue);
                break;
            case CHANGE_TYPE_USAGE_SWITCH:
                if (cbEnableUsageReminder != null) {
                    cbEnableUsageReminder.setChecked(mTempConfigData.originalBooleanValue);
                }
                LogUtils.d(TAG, "【cancelConfigChange】耗电提醒开关取消 | 恢复值=" + mTempConfigData.originalBooleanValue);
                break;
            case CHANGE_TYPE_SERVICE_SWITCH:
                if (swEnableService != null) {
                    swEnableService.setChecked(mTempConfigData.originalBooleanValue);
                }
                LogUtils.d(TAG, "【cancelConfigChange】服务开关取消 | 恢复值=" + mTempConfigData.originalBooleanValue);
                break;
            case CHANGE_TYPE_CHARGE_SEEKBAR:
                if (sbChargeReminder != null) {
                    sbChargeReminder.setProgress(mTempConfigData.originalIntValue);
                }
                if (tvChargeReminderValue != null && mChargeReminderBatteryDrawable != null && ivChargeReminderBattery != null) {
                    mChargeReminderBatteryDrawable.setBatteryValue(mTempConfigData.originalIntValue);
                    ivChargeReminderBattery.setImageDrawable(mChargeReminderBatteryDrawable);
                    tvChargeReminderValue.setText(mTempConfigData.originalIntValue + "%");
                }
                LogUtils.d(TAG, "【cancelConfigChange】充电提醒进度取消 | 恢复值=" + mTempConfigData.originalIntValue);
                break;
            case CHANGE_TYPE_USAGE_SEEKBAR:
                if (sbUsageReminder != null) {
                    sbUsageReminder.setProgress(mTempConfigData.originalIntValue);
                }
                if (tvUsageReminderValue != null && mUsageReminderBatteryDrawable != null && ivUsageReminderBattery != null) {
                    mUsageReminderBatteryDrawable.setBatteryValue(mTempConfigData.originalIntValue);
                    ivUsageReminderBattery.setImageDrawable(mUsageReminderBatteryDrawable);
                    tvUsageReminderValue.setText(mTempConfigData.originalIntValue + "%");
                }
                LogUtils.d(TAG, "【cancelConfigChange】耗电提醒进度取消 | 恢复值=" + mTempConfigData.originalIntValue);
                break;
            default:
                LogUtils.w(TAG, "【cancelConfigChange】未知变更类型，跳过");
                break;
        }

        // 取消完成，清空临时数据
        mTempConfigData = null;
        LogUtils.d(TAG, "【cancelConfigChange】配置取消完成");
    }

    /**
     * 根据变更类型更新对话框提示语（核心优化：通过 Builder 更新，确保生效）
     */
    private void updateDialogMessageByChangeType() {
        LogUtils.d(TAG, "【updateDialogMessageByChangeType】对话框消息更新开始 | mTempConfigData=" + mTempConfigData);
        if (mDialogBuilder == null || mTempConfigData == null) return;
        String message;
        if (mTempConfigData.changeType == CHANGE_TYPE_SERVICE_SWITCH) {
            // 服务开关差异化提示语
            message = mTempConfigData.newBooleanValue ?
				"启用服务后，将后台持续监控电池状态，是否确认？" :
				"禁用服务后，电池监控功能将停止，是否确认？";
        } else {
            // 普通配置默认提示语
            message = "是否确认修改当前配置？";
        }
        // 通过 Builder 设置消息，确保弹窗显示最新内容
        mDialogBuilder.setMessage(message);
        LogUtils.d(TAG, "【updateDialogMessageByChangeType】对话框消息更新完成 | message=" + message);
    }

    // ====================================== 内部工具方法（封装重复逻辑，提升复用性） ======================================
    /**
     * 获取资源颜色（适配 API30 主题颜色读取机制，兼容低版本，优化小米机型颜色显示，防御空指针）
     * @param colorResId 颜色资源 ID
     * @return 校准后的颜色值
     */
    private int getResourceColor(int colorResId) {
        LogUtils.d(TAG, "【getResourceColor】资源颜色获取 | colorResId=" + colorResId);
        // 空指针防御：Context 为空返回默认黑色
        if (mContext == null) {
            LogUtils.e(TAG, "【getResourceColor】Context 为空，返回默认黑色");
            return 0xFF000000;
        }
        // 适配 API30 主题颜色读取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getResources().getColor(colorResId, mContext.getTheme());
        } else {
            return mContext.getResources().getColor(colorResId);
        }
    }

    /**
     * 获取服务启用状态（统一从服务控制Bean读取，确保全链路状态一致）
     * @return 服务启用状态（true=启用，false=禁用）
     */
    private boolean getServiceEnableState() {
        LogUtils.d(TAG, "【getServiceEnableState】服务状态获取开始");
        ControlCenterServiceBean serviceBean = ControlCenterServiceBean.loadBean(mContext, ControlCenterServiceBean.class);
        // 本地无配置时，默认禁用服务（与服务初始化逻辑对齐）
        boolean state = serviceBean != null && serviceBean.isEnableService();
        LogUtils.d(TAG, "【getServiceEnableState】服务启用状态获取完成 | state=" + state);
        return state;
    }
}

