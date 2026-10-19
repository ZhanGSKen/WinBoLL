package cc.winboll.studio.powerbell.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.ToastUtils;
import cc.winboll.studio.powerbell.R;
import com.a4455jkjh.colorpicker.ColorPickerDialog;
import com.a4455jkjh.colorpicker.view.OnColorChangedListener;

/**
 * 调色板对话框（支持颜色拾取、RGB输入、透明度/亮度调节，兼容 API29-30+ 小米机型）
 * 适配 API30，基于 Java7 开发，返回 0xAARRGGBB 格式颜色（含透明度）
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/12/16 11:47
 * @Describe 调色板对话框（支持颜色拾取、RGB输入、透明度/亮度调节，兼容 API29-30+ 小米机型）
 */
public class ColorPaletteDialog extends Dialog implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    // ====================== 静态常量（首屏可见，统一管理） ======================
    public static final String TAG = "ColorPaletteDialog";
    private static final int MAX_RGB_VALUE = 255;      // RGB分量最大值（0-255）
    private static final int DEFAULT_BRIGHTNESS = 100; // 默认亮度百分比（100%，无调节）
    private static final int BRIGHTNESS_STEP = 5;     // 亮度调节步长（每次±5%，精准流畅）
    private static final int MIN_BRIGHTNESS = 10;     // 亮度最小值（10%，避免全黑看不见）
    private static final int MAX_BRIGHTNESS = 200;    // 亮度最大值（200%，避免过曝失真）
    private static final int MAX_ALPHA_PERCENT = 100; // 透明度最大值（100%=不透明）
    private static final int MIN_ALPHA_PERCENT = 0;   // 透明度最小值（0%=完全透明）
    private static final String FORMAT_COLOR_HEX = "#%08X"; // 颜色值格式化（AARRGGBB）
    private static final String FORMAT_PERCENT = "%d%%"; // 百分比格式化（X%）

    // ====================== 回调接口（紧跟常量，逻辑关联） ======================
    public interface OnColorSelectedListener {
        void onColorSelected(int color); // 返回0xAARRGGBB格式颜色（含透明度）
    }

    // ====================== 成员变量（按优先级排序：核心数据→控件引用） ======================
    // 核心数据：原始基准值（用户输入/选择颜色时更新）+ 实时调节值（亮度/透明度变化时更新）
    private OnColorSelectedListener mListener; // 颜色选择回调（非空校验）
    private int mInitialColor;                 // 初始颜色（传入的默认颜色）
    private int mCurrentColor;                 // 当前最终颜色（含亮度+透明度调节）
    private int mCurrentBrightnessPercent;     // 当前亮度百分比（10%-200%）
    // 透明度：百分比（0-100%，用户直观操作）+ 原始/实时值（0-255，颜色计算用）
    private int mOriginalAlphaPercent;         // 原始透明度百分比（基准值，用户输入/选色时更新）
    private int mCurrentAlphaPercent;          // 实时透明度百分比（调节进度条时更新）
    private int mOriginalAlpha;                // 原始透明度（0-255，基准值）
    private int mCurrentAlpha;                 // 实时透明度（0-255，计算用）
    // RGB：原始基准值+实时调节值
    private int mOriginalR;                    // 原始R分量（基准值，用户输入/选色时更新）
    private int mOriginalG;                    // 原始G分量（基准值，用户输入/选色时更新）
    private int mOriginalB;                    // 原始B分量（基准值，用户输入/选色时更新）
    private int mCurrentR;                     // 实时R分量（亮度调节后，同步输入框显示）
    private int mCurrentG;                     // 实时G分量（亮度调节后，同步输入框显示）
    private int mCurrentB;                     // 实时B分量（亮度调节后，同步输入框显示）
    // 并发控制标记：是否是应用程序自身在更新颜色（避免循环回调/重复触发）
    private static volatile boolean isAppSelfUpdatingColor = false;

    // 控件引用
    private ImageView ivColorPicker;   // 颜色预览拾取框
    private ImageView ivColorScaler;   // 颜色渐变拾取框
    private EditText etR;              // R分量输入框（显示实时调节值）
    private EditText etG;              // G分量输入框（显示实时调节值）
    private EditText etB;              // B分量输入框（显示实时调节值）
    private EditText etColorValue;     // 颜色值输入框（#AARRGGBB，显示最终值）
    private SeekBar sbAlpha;           // 透明度调节进度条（0-100%）
    private TextView tvAlphaValue;     // 透明度数值显示（X%）
    private TextView tvBrightnessMinus;// 亮度减少按钮（-）
    private TextView tvBrightnessValue;// 亮度数值显示（X%，直观易懂）
    private TextView tvBrightnessPlus; // 亮度增加按钮（+）
    private TextView tvConfirm;        // 确认按钮
    private TextView tvCancel;         // 取消按钮

    // ====================== 构造方法（初始化核心数据，严格校验） ======================
    public ColorPaletteDialog(Context context, int initialColor, OnColorSelectedListener listener) {
        super(context, R.style.CustomDialogStyle);
        this.mInitialColor = initialColor;
        this.mListener = listener;

        // 1. 强制回调非空，避免后续空指针（容错）
        if (mListener == null) {
            throw new IllegalArgumentException("OnColorSelectedListener can not be null!");
        }

        // 2. 解析初始颜色：原始基准值 = 实时值（初始无调节）
        this.mOriginalAlpha = Color.alpha(initialColor);
        this.mOriginalAlphaPercent = alpha2Percent(mOriginalAlpha);
        this.mCurrentAlpha = mOriginalAlpha;
        this.mCurrentAlphaPercent = mOriginalAlphaPercent;

        this.mOriginalR = Color.red(initialColor);
        this.mOriginalG = Color.green(initialColor);
        this.mOriginalB = Color.blue(initialColor);
        this.mCurrentR = mOriginalR;
        this.mCurrentG = mOriginalG;
        this.mCurrentB = mOriginalB;

        // 3. 初始化当前状态（默认亮度100%，当前颜色=初始颜色）
        this.mCurrentBrightnessPercent = DEFAULT_BRIGHTNESS;
        this.mCurrentColor = initialColor;

        LogUtils.d(TAG, String.format("init dialog success | 初始颜色：%s | 原始RGB：%d,%d,%d | 原始透明度：%s | 初始亮度：%s",
									  String.format(FORMAT_COLOR_HEX, initialColor),
									  mOriginalR, mOriginalG, mOriginalB,
									  String.format(FORMAT_PERCENT, mOriginalAlphaPercent),
									  String.format(FORMAT_PERCENT, mCurrentBrightnessPercent)));
    }

    // ====================== 生命周期方法（按执行顺序排列，逻辑清晰） ======================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 隐藏标题栏
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_color_palette, null);
        setContentView(view);

        // 初始化流程：控件绑定→数据赋值→监听设置→尺寸适配（小米机型优先适配）
        initViewBind(view);
        initData();
        initListener();
        adjustDialogSize();
        LogUtils.d(TAG, "dialog create complete | 适配小米API29-30机型");
    }

    @Override
    public void dismiss() {
        super.dismiss();
        // 释放资源，避免内存泄漏（回调引用置空）
        mListener = null;
        LogUtils.d(TAG, "dialog dismiss | 释放资源完成");
    }

    // ====================== 初始化核心方法（职责单一，便于维护） ======================
    /**
     * 控件绑定
     */
    private void initViewBind(View view) {
        ivColorPicker = view.findViewById(R.id.iv_color_picker);
        ivColorScaler = view.findViewById(R.id.iv_color_scaler);
        etR = view.findViewById(R.id.et_r);
        etG = view.findViewById(R.id.et_g);
        etB = view.findViewById(R.id.et_b);
        etColorValue = view.findViewById(R.id.et_color_value);
        sbAlpha = view.findViewById(R.id.sb_alpha);
        tvAlphaValue = view.findViewById(R.id.tv_alpha_value);
        tvBrightnessMinus = view.findViewById(R.id.tv_brightness_minus);
        tvBrightnessValue = view.findViewById(R.id.tv_brightness_value);
        tvBrightnessPlus = view.findViewById(R.id.tv_brightness_plus);
        tvConfirm = view.findViewById(R.id.tv_confirm);
        tvCancel = view.findViewById(R.id.tv_cancel);

        // 控件非空校验（小米低版本容错，绑定失败直接关闭对话框）
        if (ivColorPicker == null || ivColorScaler == null || etR == null || etG == null || etB == null || etColorValue == null
			|| sbAlpha == null || tvAlphaValue == null
			|| tvBrightnessMinus == null || tvBrightnessValue == null || tvBrightnessPlus == null
			|| tvConfirm == null || tvCancel == null) {
            LogUtils.e(TAG, "view bind failed | 请检查布局ID是否正确！");
            dismiss();
            return;
        }
        LogUtils.d(TAG, "view bind complete | 所有控件绑定成功");
    }

    /**
     * 数据初始化（无监听状态下赋值，避免循环回调）
     */
    private void initData() {
        // 1. 颜色预览（显示当前最终颜色，初始=原始颜色）
        ivColorPicker.setBackgroundColor(mCurrentColor);

        // 2. RGB输入框（显示「实时分量」，初始=原始值）
        etR.setText(String.valueOf(mCurrentR));
        etG.setText(String.valueOf(mCurrentG));
        etB.setText(String.valueOf(mCurrentB));

        // 3. 颜色值输入框（显示当前最终颜色，格式#AARRGGBB）
        etColorValue.setText(String.format(FORMAT_COLOR_HEX, mCurrentColor));

        // 4. 透明度控件（进度条+文本，初始=原始透明度）
        sbAlpha.setProgress(mCurrentAlphaPercent);
        tvAlphaValue.setText(String.format(FORMAT_PERCENT, mCurrentAlphaPercent));

        // 5. 亮度控件（显示默认100%，初始化按钮状态）
        tvBrightnessValue.setText(String.format(FORMAT_PERCENT, mCurrentBrightnessPercent));
        updateBrightnessBtnStatus(); // 禁用边界值按钮

        LogUtils.d(TAG, String.format("init data complete | 原始透明度：%s",
									  String.format(FORMAT_PERCENT, mOriginalAlphaPercent)));
    }

    /**
     * 监听初始化
     */
    private void initListener() {
        // 点击监听（按钮+颜色拾取框）
        ivColorPicker.setOnClickListener(this);
        ivColorScaler.setOnClickListener(this);
        tvConfirm.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        tvBrightnessMinus.setOnClickListener(this);
        tvBrightnessPlus.setOnClickListener(this);
        // 透明度进度条监听
        sbAlpha.setOnSeekBarChangeListener(this);
        // 输入框监听（RGB+颜色值，避免循环同步）
        initTextWatcherListener();
        LogUtils.d(TAG, "all listener init complete | 监听绑定成功");
    }

    /**
     * 对话框尺寸适配（小米全面屏+软键盘优化，避免输入框被遮挡）
     */
    private void adjustDialogSize() {
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            // 宽度占屏幕80%，高度自适应（适配不同屏幕尺寸）
            lp.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.8);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            // 软键盘适配：小米虚拟导航栏兼容
            window.setAttributes(lp);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
									| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            LogUtils.d(TAG, "dialog size adjust complete | 适配全面屏+软键盘");
        }
    }

    // ====================== 监听子方法（细分类型，逻辑清晰） ======================
    /**
     * 输入框文本监听（RGB+颜色值，传入触发ID避免循环同步）
     */
    private void initTextWatcherListener() {
        // RGB输入框监听（复用方法，减少冗余）
        setEditTextWatcher(etR, R.id.et_r);
        setEditTextWatcher(etG, R.id.et_g);
        setEditTextWatcher(etB, R.id.et_b);

        // 颜色值输入框监听（支持#RRGGBB/#AARRGGBB格式）
        etColorValue.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}

				@Override
				public void afterTextChanged(Editable s) {
					// 关键：判断非应用自身更新，才执行解析（避免循环回调）
					if (!isAppSelfUpdatingColor) {
						parseColorFromStr(s.toString().trim(), R.id.et_color_value);
					}
				}
			});
    }

    // ====================== 透明度进度条监听实现 ======================
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // 仅处理用户手动拖动进度条（避免应用自身更新时触发）
        if (fromUser && !isAppSelfUpdatingColor) {
            updateAlphaBySeekBar(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    /**
     * 拖动透明度进度条更新颜色
     */
    private synchronized void updateAlphaBySeekBar(int alphaPercent) {
        if (!isAppSelfUpdatingColor) {
            isAppSelfUpdatingColor = true; // 标记为应用自身更新
            try {
                // 更新实时透明度（百分比+0-255值）
                mCurrentAlphaPercent = alphaPercent;
                mCurrentAlpha = percent2Alpha(alphaPercent);
                // 重新计算最终颜色（基于当前亮度+新透明度）
                calculateBrightnessAndUpdate();
                // 同步所有控件
                updateAllViews();
                LogUtils.d(TAG, String.format("update alpha by seekbar | 透明度：%s",
											  String.format(FORMAT_PERCENT, mCurrentAlphaPercent)));
            } finally {
                isAppSelfUpdatingColor = false; // 释放标记
            }
        }
    }

    // ====================== 颜色核心逻辑 ======================
    /**
     * 核心计算：基于原始RGB+当前亮度+当前透明度，计算实时RGB+最终颜色
     */
    private void calculateBrightnessAndUpdate() {
        // 亮度百分比转调节系数（10%→0.1，100%→1.0，200%→2.0）
        float brightnessFactor = mCurrentBrightnessPercent / 100.0f;

        // RGB三个分量同时调节（基于原始基准值，避免叠加失真），限制0-255
        mCurrentR = Math.min(Math.max(Math.round(mOriginalR * brightnessFactor), 0), MAX_RGB_VALUE);
        mCurrentG = Math.min(Math.max(Math.round(mOriginalG * brightnessFactor), 0), MAX_RGB_VALUE);
        mCurrentB = Math.min(Math.max(Math.round(mOriginalB * brightnessFactor), 0), MAX_RGB_VALUE);

        // 拼接「实时透明度」+「实时RGB」，得到最终颜色（0xAARRGGBB）
        mCurrentColor = Color.argb(mCurrentAlpha, mCurrentR, mCurrentG, mCurrentB);
    }

    /**
     * 亮度减少（每次减5%，最低10%）
     */
    private void decreaseBrightness() {
        changeBrightness(false);
    }

    /**
     * 亮度增加（每次加5%，最高200%）
     */
    private void increaseBrightness() {
        changeBrightness(true);
    }

    /**
     * 亮度调节核心方法（统一逻辑，加并发控制）
     */
    private synchronized void changeBrightness(boolean isIncrease) {
        if (!isAppSelfUpdatingColor) {
            isAppSelfUpdatingColor = true;
            try {
                if (isIncrease) {
                    if (mCurrentBrightnessPercent >= MAX_BRIGHTNESS) return;
                    mCurrentBrightnessPercent += BRIGHTNESS_STEP;
                } else {
                    if (mCurrentBrightnessPercent <= MIN_BRIGHTNESS) return;
                    mCurrentBrightnessPercent -= BRIGHTNESS_STEP;
                }
                // 计算亮度调节后的实时RGB+最终颜色
                calculateBrightnessAndUpdate();
                // 同步所有控件
                updateAllViews();
                LogUtils.d(TAG, String.format("%s brightness | 亮度：%s | 实时RGB：%d,%d,%d",
											  isIncrease ? "increase" : "decrease",
											  String.format(FORMAT_PERCENT, mCurrentBrightnessPercent),
											  mCurrentR, mCurrentG, mCurrentB));
            } finally {
                isAppSelfUpdatingColor = false;
            }
        }
    }

    /**
     * 解析颜色字符串（支持#RRGGBB/#AARRGGBB，更新原始基准值+实时值）
     */
    private void parseColorFromStr(String colorStr, int triggerViewId) {
        if (!isAppSelfUpdatingColor) {
            isAppSelfUpdatingColor = true;
            try {
                if (TextUtils.isEmpty(colorStr)) return;

                // 补全#前缀（兼容用户输入习惯）
                if (!colorStr.startsWith("#")) {
                    colorStr = "#" + colorStr;
                }

                // 格式校验（仅支持6位RRGGBB/8位AARRGGBB）
                if (colorStr.length() != 7 && colorStr.length() != 9) {
                    LogUtils.e(TAG, String.format("parse color failed | 格式错误（需#RRGGBB/#AARRGGBB），输入：%s", colorStr));
                    return;
                }

                // 解析颜色
                int parsedColor = Color.parseColor(colorStr);

                // 更新原始基准值与实时值
                mOriginalAlpha = Color.alpha(parsedColor);
                mOriginalAlphaPercent = alpha2Percent(mOriginalAlpha);
                mOriginalR = Color.red(parsedColor);
                mOriginalG = Color.green(parsedColor);
                mOriginalB = Color.blue(parsedColor);
                mCurrentAlpha = mOriginalAlpha;
                mCurrentAlphaPercent = mOriginalAlphaPercent;
                mCurrentR = mOriginalR;
                mCurrentG = mOriginalG;
                mCurrentB = mOriginalB;
                mCurrentBrightnessPercent = DEFAULT_BRIGHTNESS;
                mCurrentColor = parsedColor;

                // 同步所有控件
                updateAllViews();
                LogUtils.d(TAG, String.format("parse color success | 解析颜色：%s | 透明度：%s | 重置亮度：%s",
											  String.format(FORMAT_COLOR_HEX, parsedColor),
											  String.format(FORMAT_PERCENT, mCurrentAlphaPercent),
											  String.format(FORMAT_PERCENT, DEFAULT_BRIGHTNESS)));
            } catch (IllegalArgumentException e) {
                LogUtils.e(TAG, String.format("parse color failed | 非法颜色格式，输入：%s", colorStr), e);
            } finally {
                isAppSelfUpdatingColor = false;
            }
        }
    }

    /**
     * 通过RGB输入框更新颜色（用户输入后，更新原始基准值+实时值，重置亮度为100%）
     */
    private synchronized void updateColorByRGB(int triggerViewId) {
        if (!isAppSelfUpdatingColor) {
            isAppSelfUpdatingColor = true;
            try {
                // 解析用户输入的RGB值（限制0-255，非法输入设为0）
                int inputR = parseInputValue(etR.getText().toString());
                int inputG = parseInputValue(etG.getText().toString());
                int inputB = parseInputValue(etB.getText().toString());

                // 更新原始基准值与实时值
                mOriginalR = inputR;
                mOriginalG = inputG;
                mOriginalB = inputB;
                mCurrentR = inputR;
                mCurrentG = inputG;
                mCurrentB = inputB;
                mCurrentBrightnessPercent = DEFAULT_BRIGHTNESS;
                mCurrentColor = Color.argb(mCurrentAlpha, mCurrentR, mCurrentG, mCurrentB);

                // 同步所有控件
                updateAllViews();
                LogUtils.d(TAG, String.format("update color by RGB | 新原始RGB：%d,%d,%d | 透明度：%s | 重置亮度：%s",
											  mOriginalR, mOriginalG, mOriginalB,
											  String.format(FORMAT_PERCENT, mCurrentAlphaPercent),
											  String.format(FORMAT_PERCENT, DEFAULT_BRIGHTNESS)));
            } catch (Exception e) {
                LogUtils.e(TAG, "update color by RGB failed", e);
            } finally {
                isAppSelfUpdatingColor = false;
            }
        }
    }

    /**
     * 核心同步：更新所有控件显示
     */
    private void updateAllViews() {
        // 1. 同步颜色预览
        ivColorPicker.setBackgroundColor(mCurrentColor);

        // 2. 同步RGB输入框
        etR.setText(String.valueOf(mCurrentR));
        etG.setText(String.valueOf(mCurrentG));
        etB.setText(String.valueOf(mCurrentB));

        // 3. 同步颜色值输入框
        etColorValue.setText(String.format(FORMAT_COLOR_HEX, mCurrentColor));

        // 4. 同步透明度控件
        sbAlpha.setProgress(mCurrentAlphaPercent);
        tvAlphaValue.setText(String.format(FORMAT_PERCENT, mCurrentAlphaPercent));

        // 5. 同步亮度控件
        tvBrightnessValue.setText(String.format(FORMAT_PERCENT, mCurrentBrightnessPercent));
        updateBrightnessBtnStatus();

        LogUtils.d(TAG, String.format("sync all views complete | 最终颜色：%s | 实时RGB：%d,%d,%d | 透明度：%s | 亮度：%s",
									  String.format(FORMAT_COLOR_HEX, mCurrentColor),
									  mCurrentR, mCurrentG, mCurrentB,
									  String.format(FORMAT_PERCENT, mCurrentAlphaPercent),
									  String.format(FORMAT_PERCENT, mCurrentBrightnessPercent)));
    }

    /**
     * 更新亮度按钮状态（边界值禁用，提升交互体验）
     */
    private void updateBrightnessBtnStatus() {
        boolean canMinus = mCurrentBrightnessPercent > MIN_BRIGHTNESS;
        boolean canPlus = mCurrentBrightnessPercent < MAX_BRIGHTNESS;

        tvBrightnessMinus.setEnabled(canMinus);
        tvBrightnessPlus.setEnabled(canPlus);
        tvBrightnessMinus.setTextColor(canMinus ? Color.BLACK : Color.parseColor("#CCCCCC"));
        tvBrightnessPlus.setTextColor(canPlus ? Color.BLACK : Color.parseColor("#CCCCCC"));
    }

    // ====================== 工具方法 ======================
    /**
     * 透明度：0-255 → 0-100%
     */
    private int alpha2Percent(int alpha) {
        return Math.round((float) alpha / MAX_RGB_VALUE * MAX_ALPHA_PERCENT);
    }

    /**
     * 透明度：0-100% → 0-255
     */
    private int percent2Alpha(int percent) {
        return Math.round((float) percent / MAX_ALPHA_PERCENT * MAX_RGB_VALUE);
    }

    /**
     * 解析输入值（限制0-255，非法输入返回0）
     */
    private int parseInputValue(String input) {
        if (TextUtils.isEmpty(input)) return 0;
        try {
            int value = Integer.parseInt(input);
            return Math.min(Math.max(value, 0), MAX_RGB_VALUE);
        } catch (NumberFormatException e) {
            LogUtils.e(TAG, String.format("parse input failed | 非法数字，输入：%s", input), e);
            return 0;
        }
    }

    /**
     * RGB输入框监听复用
     */
    private void setEditTextWatcher(EditText editText, final int viewId) {
        editText.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}

				@Override
				public void afterTextChanged(Editable s) {
					if (!isAppSelfUpdatingColor) {
						updateColorByRGB(viewId);
					}
				}
			});
    }

    /**
     * dp转px（适配小米不同分辨率）
     */
    private int dp2px(float dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }

    /**
     * 显示系统颜色选择器（兼容API29-30，无高版本依赖，小米机型适配）
     */
    private void showSystemColorPicker() {
        LogUtils.d(TAG, "show system color picker | 兼容小米API29-30，支持横向滚动");
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("选择基础颜色");

        // 50种常用颜色：按彩虹光谱顺序排列
        final int[] systemColors = {
			0xFFCC0000, 0xFFFF0000, 0xFFFF6666, 0xFFFF1493, 0xFF8B0000, 0xFFFF4500,
			0xFFCC6600, 0xFFFF8800, 0xFFFFAA33, 0xFFFFBB00, 0xFFF5A623,
			0xFFCCCC00, 0xFFFFFF00, 0xFFFFEE99, 0xFFFFFACD, 0xFFFFD700,
			0xFF006600, 0xFF00FF00, 0xFF99FF99, 0xFF66CC66, 0xFF98FB98, 0xFF00FF99, 0xFF003300,
			0xFF006666, 0xFF00FFFF, 0xFF99FFFF, 0xFF00CCCC, 0xFF40E0D0,
			0xFF0000CC, 0xFF00008B, 0xFF0000FF, 0xFF6666FF, 0xFF87CEEB, 0xFF0066FF, 0xFF0099FF, 0xFF4B0082,
			0xFF660099, 0xFF8800FF, 0xFFAA99FF, 0xFF9370DB, 0xFFCBC3E3, 0xFF8A2BE2,
			0xFFFF00FF, 0xFFFF99CC, 0xFFFFCCDD, 0xFFFFB6C1, 0xFFFFA5A5,
			0xFF8B4513, 0xFFA0522D, 0xFFD2B48C, 0xFFCD853F,
			0xFF333333, 0xFF666666, 0xFF888888, 0xFFAAAAAA, 0xFFCCCCCC, 0xFFE6E6E6,
			0xFF000000, 0xFFFFFFFF, 0xFFFFFAFA
        };

        // 1. 第一级：水平滚动容器
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getContext());
        horizontalScrollView.setHorizontalScrollBarEnabled(true);
        horizontalScrollView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        horizontalScrollView.setPadding(dp2px(5), dp2px(5), dp2px(5), dp2px(5));

        // 2. 第二级：颜色排列容器（横向）
        LinearLayout colorLayout = new LinearLayout(getContext());
        colorLayout.setOrientation(LinearLayout.HORIZONTAL);
        colorLayout.setGravity(Gravity.CENTER_VERTICAL);
        colorLayout.setPadding(dp2px(10), dp2px(10), dp2px(10), dp2px(10));

        // 3. 循环添加颜色按钮（内置圆形效果）
        for (int i = 0; i < systemColors.length; i++) {
            final int color = systemColors[i];
            ImageView colorBtn = new ImageView(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp2px(40), dp2px(40));
            if (i != systemColors.length - 1) {
                lp.setMargins(0, 0, dp2px(10), 0); // 按钮间距
            }
            colorBtn.setLayoutParams(lp);

            // 内置圆形背景（白色边框+圆形形状）
            GradientDrawable circleBg = new GradientDrawable();
            circleBg.setShape(GradientDrawable.OVAL);
            circleBg.setColor(color);
            circleBg.setStroke(dp2px(2), Color.WHITE);
            colorBtn.setBackground(circleBg);

            colorBtn.setClickable(true);
            colorBtn.setFocusable(true);

            // 点击事件
            colorBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!isAppSelfUpdatingColor) {
							isAppSelfUpdatingColor = true;
							try {
								mOriginalAlpha = Color.alpha(color);
								mOriginalAlphaPercent = alpha2Percent(mOriginalAlpha);
								mOriginalR = Color.red(color);
								mOriginalG = Color.green(color);
								mOriginalB = Color.blue(color);
								mCurrentAlpha = mOriginalAlpha;
								mCurrentAlphaPercent = mOriginalAlphaPercent;
								mCurrentR = mOriginalR;
								mCurrentG = mOriginalG;
								mCurrentB = mOriginalB;
								mCurrentBrightnessPercent = DEFAULT_BRIGHTNESS;
								mCurrentColor = color;
								updateAllViews();
								builder.create().dismiss();
								LogUtils.d(TAG, String.format("select system color | 选择颜色：%s | 透明度：%s",
															  String.format(FORMAT_COLOR_HEX, color),
															  String.format(FORMAT_PERCENT, mCurrentAlphaPercent)));
							} finally {
								isAppSelfUpdatingColor = false;
							}
						}
					}
				});
            colorLayout.addView(colorBtn);
        }

        // 层级嵌套
        horizontalScrollView.addView(colorLayout);
        builder.setView(horizontalScrollView).setNegativeButton("关闭", null).show();
    }

    // ====================== 点击事件实现 ======================
    @Override
    public void onClick(View v) {
        int id = v.getId();
        // 所有点击事件均加并发判断
        if (!isAppSelfUpdatingColor) {
            if (id == R.id.iv_color_picker) {
                showSystemColorPicker();
            } else if (id == R.id.iv_color_scaler) {
                openColorScalerDialog(mCurrentColor);
            } else if (id == R.id.tv_confirm) {
                mListener.onColorSelected(mCurrentColor);
                LogUtils.d(TAG, String.format("confirm color | 回调颜色：%s",
											  String.format(FORMAT_COLOR_HEX, mCurrentColor)));
                dismiss();
            } else if (id == R.id.tv_cancel) {
                dismiss();
                LogUtils.d(TAG, "cancel color | 取消选择，关闭对话框");
            } else if (id == R.id.tv_brightness_minus) {
                decreaseBrightness();
            } else if (id == R.id.tv_brightness_plus) {
                increaseBrightness();
            }
        }
    }

    /**
     * 打开颜色渐变选择器
     */
    void openColorScalerDialog(int nColor) {
        LogUtils.d(TAG, String.format("openColorScalerDialog | 初始颜色：%s",
									  String.format(FORMAT_COLOR_HEX, nColor)));
        final ColorScalerDialog dlg = new ColorScalerDialog(getContext(), nColor);
        dlg.setOnColorChangedListener(new OnColorChangedListener() {
				@Override
				public void beforeColorChanged() {}

				@Override
				public void onColorChanged(int color) {
					dlg.currentColorScalerDialogColor = color;
				}

				@Override
				public void afterColorChanged() {}
			});
        dlg.show();
    }

    // ====================== 内部类 ======================
    class ColorScalerDialog extends ColorPickerDialog {
        public int currentColorScalerDialogColor = 0;

        public ColorScalerDialog(Context context, int p) {
            super(context, p);
            this.currentColorScalerDialogColor = p;
        }

        @Override
        public void dismiss() {
            super.dismiss();
            int color = currentColorScalerDialogColor;
            ToastUtils.show(String.format("选择颜色：%s", String.format(FORMAT_COLOR_HEX, color)));
            if (!isAppSelfUpdatingColor) {
                isAppSelfUpdatingColor = true;
                try {
                    mOriginalAlpha = Color.alpha(color);
                    mOriginalAlphaPercent = alpha2Percent(mOriginalAlpha);
                    mOriginalR = Color.red(color);
                    mOriginalG = Color.green(color);
                    mOriginalB = Color.blue(color);
                    mCurrentAlpha = mOriginalAlpha;
                    mCurrentAlphaPercent = mOriginalAlphaPercent;
                    mCurrentR = mOriginalR;
                    mCurrentG = mOriginalG;
                    mCurrentB = mOriginalB;
                    mCurrentBrightnessPercent = DEFAULT_BRIGHTNESS;
                    mCurrentColor = color;
                    updateAllViews();
                    LogUtils.d(TAG, String.format("select scaler color | 选择颜色：%s | 透明度：%s",
												  String.format(FORMAT_COLOR_HEX, color),
												  String.format(FORMAT_PERCENT, mCurrentAlphaPercent)));
                } finally {
                    isAppSelfUpdatingColor = false;
                }
            }
        }
    }
}

