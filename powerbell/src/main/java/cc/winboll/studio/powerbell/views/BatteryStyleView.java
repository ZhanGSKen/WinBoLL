package cc.winboll.studio.powerbell.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.powerbell.MainActivity;
import cc.winboll.studio.powerbell.R;
import cc.winboll.studio.powerbell.models.BatteryStyle;

/**
 * 电池样式单选视图，水平展示所有BatteryStyle枚举选项
 * 每个选项 = RadioButton单选按钮 + BatteryDrawable预览控件
 * 适配API30、Java7规范，联动BatteryDrawable绘制样式
 * 包含：SP持久化存储 + 公共静态方法读取SP枚举值 + 彻底修复点击不回调+单选失效
 * 默认选中：BatteryStyle.ENERGY_STYLE
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 */
public class BatteryStyleView extends LinearLayout implements RadioGroup.OnCheckedChangeListener {
    // ====================== 常量区 ======================
    public static final String TAG = "BatteryStyleView";
    private static final int DEFAULT_BATTERY_COLOR = Color.parseColor("#FF4CAF50");
    private static final int DEFAULT_CHECKED_STYLE_INDEX = 1; // ✅ 修改默认选中下标 1 = ENERGY_STYLE
    public static final String SP_NAME = "sp_battery_style_config";
    public static final String SP_KEY_BATTERY_STYLE = "key_selected_battery_style";

    // ====================== 控件变量 ======================
    private RadioGroup rgBatteryStyle;
    private RadioButton rbZebraStyle;
    private RadioButton rbEnergyStyle;
    private RadioButton rbPointStyle; // ✅ 新增：圆点样式单选按钮
    private RelativeLayout rlZebraPreview;
    private RelativeLayout rlEnergyPreview;
    private RelativeLayout rlPointPreview; // ✅ 新增：圆点样式预览布局
    private BatteryDrawable mZebraDrawable;
    private BatteryDrawable mEnergyDrawable;
    private BatteryDrawable mPointDrawable; // ✅ 新增：圆点样式Drawable实例

    // ====================== 业务变量 ======================
    private BatteryStyle mCurrentStyle = BatteryStyle.ENERGY_STYLE; // ✅ 修改默认样式为 能量样式
    private OnBatteryStyleSelectedListener mStyleSelectedListener;
    private int mBatteryColor = DEFAULT_BATTERY_COLOR;
    private int mBatteryValue = 100;
    private SharedPreferences mSp;

    // ====================== 构造方法 ======================
    public BatteryStyleView(Context context) {
        super(context);
        initSP(context);
        initView(context, null);
    }

    public BatteryStyleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSP(context);
        initAttrs(context, attrs);
        initView(context, attrs);
    }

    public BatteryStyleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSP(context);
        initAttrs(context, attrs);
        initView(context, attrs);
    }

    // ====================== 初始化SP持久化 ======================
    private void initSP(Context context) {
        mSp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        LogUtils.d(TAG, "【initSP】SharedPreferences初始化完成，文件名称 = " + SP_NAME);
    }

    // ====================== 初始化方法 ======================
    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs == null) return;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BatteryStyleView);
        mBatteryColor = typedArray.getColor(R.styleable.BatteryStyleView_batteryPreviewColor, DEFAULT_BATTERY_COLOR);
        mBatteryValue = typedArray.getInt(R.styleable.BatteryStyleView_previewBatteryValue, 100);
        int styleIndex = typedArray.getInt(R.styleable.BatteryStyleView_defaultSelectedStyle, DEFAULT_CHECKED_STYLE_INDEX);
        mCurrentStyle = getStyleFromSP() == null ? (styleIndex == 0 ? BatteryStyle.ENERGY_STYLE : styleIndex ==1 ? BatteryStyle.ZEBRA_STYLE : BatteryStyle.POINT_STYLE) : getStyleFromSP();
        typedArray.recycle();
        LogUtils.d(TAG, "【initAttrs】解析属性完成 电量颜色=" + Integer.toHexString(mBatteryColor) + " 预览电量=" + mBatteryValue + " 默认样式=" + mCurrentStyle.name());
    }

    private void initView(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_battery_style, this, true);
        rgBatteryStyle = findViewById(R.id.rg_battery_style);
        rbZebraStyle = findViewById(R.id.rb_zebra_style);
        rbEnergyStyle = findViewById(R.id.rb_energy_style);
        rbPointStyle = findViewById(R.id.rb_point_style); // ✅ 新增：绑定圆点样式单选按钮
        rlZebraPreview = findViewById(R.id.rl_zebra_preview);
        rlEnergyPreview = findViewById(R.id.rl_energy_preview);
        rlPointPreview = findViewById(R.id.rl_point_preview); // ✅ 新增：绑定圆点样式预览布局

        initPreviewDrawable();
        rgBatteryStyle.setOnCheckedChangeListener(this);
        addRadioBtnClickLister();
        setDefaultChecked();
        LogUtils.d(TAG, "【initView】视图初始化完成");
    }

    private void initPreviewDrawable() {
        mZebraDrawable = new BatteryDrawable(mBatteryColor, BatteryStyle.ZEBRA_STYLE);
        mZebraDrawable.setBatteryValue(mBatteryValue);
        rlZebraPreview.setBackground(mZebraDrawable);

        mEnergyDrawable = new BatteryDrawable(mBatteryColor, BatteryStyle.ENERGY_STYLE);
        mEnergyDrawable.setBatteryValue(mBatteryValue);
        rlEnergyPreview.setBackground(mEnergyDrawable);

        // ✅ 新增：初始化圆点样式Drawable + 绑定预览布局 + 设置电量值
        mPointDrawable = new BatteryDrawable(mBatteryColor, BatteryStyle.POINT_STYLE);
        mPointDrawable.setBatteryValue(mBatteryValue);
        rlPointPreview.setBackground(mPointDrawable);

        LogUtils.d(TAG, "【initPreviewDrawable】Drawable预览初始化完成");
    }

    private void setDefaultChecked() {
        // ✅ 新增：圆点样式的默认选中判断
        if (mCurrentStyle == BatteryStyle.ZEBRA_STYLE) {
            rbZebraStyle.setChecked(true);
        } else if (mCurrentStyle == BatteryStyle.POINT_STYLE) {
            rbPointStyle.setChecked(true);
        } else {
            rbEnergyStyle.setChecked(true);
        }
        LogUtils.d(TAG, "【setDefaultChecked】默认选中样式 = " + mCurrentStyle.name());
    }

    private void addRadioBtnClickLister() {
        rbZebraStyle.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					rbZebraStyle.setChecked(true);
					rbEnergyStyle.setChecked(false);
                    rbPointStyle.setChecked(false); // ✅ 新增：取消圆点样式选中
					handleStyleSelect(BatteryStyle.ZEBRA_STYLE);
				}
			});

        rbEnergyStyle.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					rbEnergyStyle.setChecked(true);
					rbZebraStyle.setChecked(false);
                    rbPointStyle.setChecked(false); // ✅ 新增：取消圆点样式选中
					handleStyleSelect(BatteryStyle.ENERGY_STYLE);
				}
			});

        // ✅ 新增：圆点样式单选按钮点击事件
        rbPointStyle.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					rbPointStyle.setChecked(true);
					rbZebraStyle.setChecked(false);
					rbEnergyStyle.setChecked(false);
					handleStyleSelect(BatteryStyle.POINT_STYLE);
				}
			});
    }

    // ====================== RadioGroup 选中回调 (点击必触发) ======================
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        ToastUtils.show("onCheckedChanged");
        if (checkedId == R.id.rb_zebra_style) {
            handleStyleSelect(BatteryStyle.ZEBRA_STYLE);
        } else if (checkedId == R.id.rb_energy_style) {
            handleStyleSelect(BatteryStyle.ENERGY_STYLE);
        } else if (checkedId == R.id.rb_point_style) { // ✅ 新增：圆点样式选中回调
            handleStyleSelect(BatteryStyle.POINT_STYLE);
        }
    }

    private void handleStyleSelect(BatteryStyle style) {
        mCurrentStyle = style;
        saveStyle2SP(mCurrentStyle);
		MainActivity.sendUpdateBatteryDrawableMessage();
        LogUtils.d(TAG, "【handleStyleSelect】选中样式 → " + mCurrentStyle.name() + "，已存入SP");
        if (mStyleSelectedListener != null) {
            mStyleSelectedListener.onStyleSelected(mCurrentStyle);
        }
    }

    // ====================== SP持久化 存储+读取 封装方法 ======================
    private void saveStyle2SP(BatteryStyle style) {
        mSp.edit().putString(SP_KEY_BATTERY_STYLE, style.name()).commit();
    }

    private BatteryStyle getStyleFromSP() {
        String styleStr = mSp.getString(SP_KEY_BATTERY_STYLE, null);
        if (styleStr == null) return null;
        try {
            return BatteryStyle.valueOf(styleStr);
        } catch (IllegalArgumentException e) {
            LogUtils.e(TAG, "【getStyleFromSP】SP读取样式异常 = " + e.getMessage());
            return null;
        }
    }

    // ====================== 公共静态方法 读取SP存储的枚举值 ======================
    public static BatteryStyle getSavedBatteryStyle(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String styleStr = sp.getString(SP_KEY_BATTERY_STYLE, null);
        if (styleStr == null) {
            LogUtils.w(TAG, "【getSavedBatteryStyle】SP无存储值，返回默认样式 ENERGY_STYLE");
            return BatteryStyle.ENERGY_STYLE; // ✅ 静态方法默认值同步修改为能量样式
        }
        try {
            BatteryStyle style = BatteryStyle.valueOf(styleStr);
            LogUtils.d(TAG, "【getSavedBatteryStyle】SP读取成功 → " + style.name());
            return style;
        } catch (IllegalArgumentException e) {
            LogUtils.e(TAG, "【getSavedBatteryStyle】SP读取异常 = " + e.getMessage() + "，返回默认样式 ENERGY_STYLE");
            return BatteryStyle.ENERGY_STYLE; // ✅ 异常兜底值同步修改为能量样式
        }
    }

    public static BatteryStyle getSavedBatteryStyle(Context context, BatteryStyle defaultStyle) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String styleStr = sp.getString(SP_KEY_BATTERY_STYLE, null);
        if (styleStr == null) {
            LogUtils.w(TAG, "【getSavedBatteryStyle】SP无存储值，返回自定义默认样式 → " + defaultStyle.name());
            return defaultStyle;
        }
        try {
            BatteryStyle style = BatteryStyle.valueOf(styleStr);
            LogUtils.d(TAG, "【getSavedBatteryStyle】SP读取成功 → " + style.name());
            return style;
        } catch (IllegalArgumentException e) {
            LogUtils.e(TAG, "【getSavedBatteryStyle】SP读取异常 = " + e.getMessage() + "，返回自定义默认样式 → " + defaultStyle.name());
            return defaultStyle;
        }
    }

    // ====================== 对外暴露方法 ======================
    public void setSelectedStyle(BatteryStyle style) {
        mCurrentStyle = style;
        // ✅ 新增：圆点样式的手动选中赋值
        rbZebraStyle.setChecked(style == BatteryStyle.ZEBRA_STYLE);
        rbEnergyStyle.setChecked(style == BatteryStyle.ENERGY_STYLE);
        rbPointStyle.setChecked(style == BatteryStyle.POINT_STYLE);
        saveStyle2SP(style);
        LogUtils.d(TAG, "【setSelectedStyle】手动设置选中样式 → " + style.name() + "，已存入SP");
    }

    public BatteryStyle getCurrentStyle() {
        return mCurrentStyle;
    }

    public void setPreviewBatteryValue(int batteryValue) {
        this.mBatteryValue = batteryValue;
        mZebraDrawable.setBatteryValue(batteryValue);
        mEnergyDrawable.setBatteryValue(batteryValue);
        mPointDrawable.setBatteryValue(batteryValue); // ✅ 新增：圆点样式同步电量值
    }

    public void setPreviewBatteryColor(int color) {
        this.mBatteryColor = color;
        mZebraDrawable.updateBatteryColor(color);
        mEnergyDrawable.updateBatteryColor(color);
        mPointDrawable.updateBatteryColor(color); // ✅ 新增：圆点样式同步颜色值
    }

    public void setOnBatteryStyleSelectedListener(OnBatteryStyleSelectedListener listener) {
        this.mStyleSelectedListener = listener;
    }

    // ====================== 选中回调接口 ======================
    public interface OnBatteryStyleSelectedListener {
        void onStyleSelected(BatteryStyle batteryStyle);
    }
}

