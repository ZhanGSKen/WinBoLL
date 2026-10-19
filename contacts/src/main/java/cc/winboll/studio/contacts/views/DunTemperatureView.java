package cc.winboll.studio.contacts.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import cc.winboll.studio.libappbase.LogUtils;
import java.util.WeakHashMap;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/03/19 14:04:20
 * @Describe 云盾华氏度热力视图，垂直盾值温度视图控件（带颜色渐变+静态Handler更新）
 * 采用绘图方式展示盾值温度，填充色随盾值比例渐变，支持设置文本在温度条左侧/右侧，底部对齐竖排显示
 * 温度条宽度=5dp，文本区宽度固定=5dp，整体左右边距=0，无任何多余空白间距
 */
public class DunTemperatureView extends View {
    // ====================== 常量定义区 ======================
    public static final String TAG = "DunTemperatureView";
    // 控件默认高度
    private static final int DEFAULT_HEIGHT = 200;
    // 温度条宽度（5dp）、文本区宽度（固定5dp）
    private static final int THERMOMETER_WIDTH_DP = 5;
    private static final int TEXT_AREA_WIDTH_DP = 5;
    // 填充区域内边距（左右=0，上下=2避免贴边）
    private static final int FILL_PADDING_HORIZONTAL = 0;
    private static final int FILL_PADDING_VERTICAL = 2;
    // 竖排文本字间距
    private static final float TEXT_CHAR_SPACING = 8f;
    // Handler消息标识
    public static final int MSG_UPDATE_DUN_VALUE = 0x01;
    // 消息参数Key
    public static final String KEY_MAX_VALUE = "max_value";
    public static final String KEY_CURRENT_VALUE = "current_value";

    // ====================== 静态成员区 ======================
    // 弱引用缓存控件实例，避免内存泄漏
    private static WeakHashMap<DunTemperatureView, Object> sViewCache = new WeakHashMap<>();
    // 静态Handler，处理跨线程更新消息
    private static Handler sStaticHandler;

    // ====================== 成员变量区 ======================
    // 画笔相关
    private Paint mThermometerPaint;
    private Paint mFillPaint;
    private Paint mTextPaint;
    // 尺寸参数（dp转px后的值）
    private int mThermometerWidth;
    private int mTextAreaWidth;
    private int mMaxValue = 100; // 最高盾值
    private int mCurrentValue = 0; // 当前盾值
    private RectF mThermometerRect; // 温度条矩形区域
    // 渐变颜色配置（低→中→高 对应绿→黄→红）
    private int[] mGradientColors = {Color.GREEN, Color.YELLOW, Color.RED};
    private float[] mGradientPositions = {0.0f, 0.5f, 1.0f};
    // 布局配置：true=文本在温度条右侧（默认），false=文本在温度条左侧
    private boolean isTextOnRight = true;
    // 其他颜色配置
    private int mBorderColor = Color.parseColor("#FF444444");
    private int mTextColor = Color.parseColor("#FF000000");

    // ====================== 静态代码块 ======================
    static {
        // 初始化静态Handler，绑定主线程Looper
        sStaticHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MSG_UPDATE_DUN_VALUE) {
                    // 获取消息中的盾值参数
                    int maxValue = msg.getData().getInt(KEY_MAX_VALUE, 100);
                    int currentValue = msg.getData().getInt(KEY_CURRENT_VALUE, 0);
                    LogUtils.d(TAG, "sStaticHandler: 收到更新消息，max=" + maxValue + ", current=" + currentValue);

                    // 遍历缓存的控件实例，更新所有实例
                    for (DunTemperatureView view : sViewCache.keySet()) {
                        if (view != null && view.isShown()) {
                            view.setMaxValue(maxValue);
                            view.setCurrentValue(currentValue);
                        }
                    }
                }
            }
        };
    }

    // ====================== 构造函数区 ======================
    public DunTemperatureView(Context context) {
        super(context);
        init();
    }

    public DunTemperatureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DunTemperatureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // ====================== 初始化方法区 ======================
    /**
     * 初始化画笔和参数
     */
    private void init() {
        LogUtils.d(TAG, "init: 开始初始化云盾温度视图控件");
        // dp转px（适配不同分辨率）
        mThermometerWidth = dp2px(getContext(), THERMOMETER_WIDTH_DP);
        mTextAreaWidth = dp2px(getContext(), TEXT_AREA_WIDTH_DP);
        LogUtils.d(TAG, "init: 温度条宽度5dp转px=" + mThermometerWidth + "，文本区宽度5dp转px=" + mTextAreaWidth);

        // 温度条边框画笔（宽度1px，适配5dp宽度）
        mThermometerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mThermometerPaint.setColor(mBorderColor);
        mThermometerPaint.setStyle(Paint.Style.STROKE);
        mThermometerPaint.setStrokeWidth(1);

        // 温度条填充画笔（支持渐变）
        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setStyle(Paint.Style.FILL);

        // 文本画笔（适配5dp窄文本区，文字居中绘制）
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(18); // 缩小字号适配5dp窄文本区（避免文字超出）
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setFakeBoldText(true); // 文字加粗，提升窄区域可读性

        // 初始化温度条矩形
        mThermometerRect = new RectF();

        // 将当前实例加入静态缓存
        sViewCache.put(this, null);
        LogUtils.d(TAG, "init: 云盾温度视图控件初始化完成，实例已加入缓存");
    }

    // ====================== 工具方法区 ======================
    /**
     * dp 转 px（适配不同屏幕分辨率）
     */
    private int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP,
			dpValue,
			context.getResources().getDisplayMetrics()
        );
    }

    // ====================== 对外控制方法区 ======================
    /**
     * 设置文本相对于温度条的位置
     * @param isOnRight true=文本在温度条右侧（默认），false=文本在温度条左侧
     */
    public void setTextPosition(boolean isOnRight) {
        this.isTextOnRight = isOnRight;
        invalidate(); // 刷新布局绘制
    }

    /**
     * 获取当前文本位置配置
     * @return true=右侧，false=左侧
     */
    public boolean isTextOnRight() {
        return isTextOnRight;
    }

    // ====================== 对外静态方法区 ======================
    /**
     * 静态外部方法：发送消息更新所有 DunTemperatureView 实例的盾值
     * 可在子线程中调用
     * @param maxValue 最高盾值
     * @param currentValue 当前盾值
     */
    public static void updateDunValue(int maxValue, int currentValue) {
        if (sStaticHandler == null) {
            LogUtils.w(TAG, "updateDunValue: 静态Handler未初始化");
            return;
        }
        // 封装参数到消息
        Message msg = sStaticHandler.obtainMessage(MSG_UPDATE_DUN_VALUE);
        msg.getData().putInt(KEY_MAX_VALUE, maxValue);
        msg.getData().putInt(KEY_CURRENT_VALUE, currentValue);
        // 发送消息
        sStaticHandler.sendMessage(msg);
    }

    // ====================== 对外实例方法区 ======================
    /**
     * 设置最高盾值
     * @param maxValue 最高盾值（需大于0）
     */
    public void setMaxValue(int maxValue) {
        if (maxValue <= 0) {
            LogUtils.w(TAG, "setMaxValue: 最高盾值必须大于0，当前值=" + maxValue);
            return;
        }
        this.mMaxValue = maxValue;
        // 限制当前值不超过最大值
        mCurrentValue = Math.min(mCurrentValue, maxValue);
        invalidate(); // 重绘控件
    }

    /**
     * 设置当前盾值
     * @param currentValue 当前盾值（范围 0~maxValue）
     */
    public void setCurrentValue(int currentValue) {
        int oldValue = this.mCurrentValue;
        this.mCurrentValue = Math.max(0, Math.min(currentValue, mMaxValue));
        if (oldValue != this.mCurrentValue) {
            LogUtils.d(TAG, "setCurrentValue: 当前盾值从" + oldValue + "更新为" + mCurrentValue);
            invalidate(); // 重绘控件
        }
    }

    /**
     * 获取当前盾值
     */
    public int getCurrentValue() {
        return mCurrentValue;
    }

    /**
     * 获取最高盾值
     */
    public int getMaxValue() {
        return mMaxValue;
    }

    /**
     * 设置自定义渐变颜色
     * @param colors 渐变颜色数组（至少2种颜色）
     * @param positions 颜色位置数组（与colors长度一致，0.0~1.0）
     */
    public void setGradientColors(int[] colors, float[] positions) {
        if (colors == null || colors.length < 2 || positions == null || positions.length != colors.length) {
            LogUtils.w(TAG, "setGradientColors: 渐变颜色参数不合法，颜色数组长度=" + (colors == null ? "null" : colors.length));
            return;
        }
        this.mGradientColors = colors;
        this.mGradientPositions = positions;
        LogUtils.d(TAG, "setGradientColors: 自定义渐变颜色已设置");
        invalidate();
    }

    /**
     * 设置温度条边框颜色
     */
    public void setBorderColor(int color) {
        this.mBorderColor = color;
        mThermometerPaint.setColor(color);
        LogUtils.d(TAG, "setBorderColor: 边框颜色已更新");
        invalidate();
    }

    /**
     * 设置文本颜色
     */
    public void setTextColor(int color) {
        this.mTextColor = color;
        mTextPaint.setColor(color);
        LogUtils.d(TAG, "setTextColor: 文本颜色已更新");
        invalidate();
    }

    // ====================== 生命周期方法 ======================
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // 控件从窗口移除时，从缓存中清除，避免内存泄漏
        sViewCache.remove(this);
        LogUtils.d(TAG, "onDetachedFromWindow: 控件实例已从缓存移除");
    }

    // ====================== 测量与绘制区 ======================
    /**
     * 测量辅助函数
     */
    private int measureSize(int defaultSize, int measureSpec) {
        int result = defaultSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(defaultSize, specSize);
        }
        return result;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 强制控件整体左右内边距=0，彻底消除外部边距
        setPadding(0, getPaddingTop(), 0, getPaddingBottom());

        // 控件宽度=温度条宽度 + 文本区宽度（均为5dp转px，无额外空白）
        int defaultWidth = mThermometerWidth + mTextAreaWidth;
        int width = measureSize(defaultWidth, widthMeasureSpec);
        int height = measureSize(DEFAULT_HEIGHT, heightMeasureSpec);
        setMeasuredDimension(width, height);

        // 根据文本位置配置，计算温度条矩形坐标
        float thermometerLeft, thermometerRight;
        if (isTextOnRight) {
            // 文本在右侧：温度条靠左，右接文本区
            thermometerLeft = 0;
            thermometerRight = thermometerLeft + mThermometerWidth;
        } else {
            // 文本在左侧：温度条靠右，左接文本区
            thermometerLeft = width - mThermometerWidth;
            thermometerRight = width;
        }
        float thermometerTop = getPaddingTop();
        float thermometerBottom = height - getPaddingBottom();
        mThermometerRect.set(thermometerLeft, thermometerTop, thermometerRight, thermometerBottom);

        LogUtils.v(TAG, "onMeasure: 文本位置=" + (isTextOnRight ? "右侧" : "左侧") + "，控件尺寸=" + width + "x" + height + "，温度条区域=" + mThermometerRect.toShortString());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 1. 绘制温度条边框（5dp宽度，小圆角适配）
        canvas.drawRoundRect(mThermometerRect, 3, 3, mThermometerPaint);

        // 2. 计算填充高度（根据当前值占最大值的比例）
        float fillRatio = (float) mCurrentValue / mMaxValue;
        float fillHeight = mThermometerRect.height() * fillRatio;
        float fillTop = mThermometerRect.bottom - fillHeight;

        // 3. 绘制渐变填充部分（左右贴边框，无间距）
        if (fillHeight > 0) {
            RectF fillRect = new RectF(
                mThermometerRect.left + FILL_PADDING_HORIZONTAL,
                fillTop + FILL_PADDING_VERTICAL,
                mThermometerRect.right - FILL_PADDING_HORIZONTAL,
                mThermometerRect.bottom - FILL_PADDING_VERTICAL
            );
            LinearGradient gradient = new LinearGradient(
                fillRect.centerX(), fillRect.bottom,
                fillRect.centerX(), fillRect.top,
                mGradientColors,
                mGradientPositions,
                Shader.TileMode.CLAMP
            );
            mFillPaint.setShader(gradient);
            canvas.drawRoundRect(fillRect, 2, 2, mFillPaint);
            mFillPaint.setShader(null);
        }

        // 4. 绘制文本（5dp固定宽度文本区，底部对齐竖排，文字居中）
        String text = String.format("%d/%d", mCurrentValue, mMaxValue);
        if (text.isEmpty()) return;

        float textBaseX;
        if (isTextOnRight) {
            // 文本在右侧：X=温度条右边缘 + 文本区宽度的一半（紧贴温度条）
            textBaseX = mThermometerRect.right + (mTextAreaWidth / 2f);
        } else {
            // 文本在左侧：X=文本区宽度的一半（紧贴控件左边缘）
            textBaseX = mTextAreaWidth / 2f;
        }

        // 文本绘制参数（适配5dp窄区域，缩小字间距提升紧凑度）
        float singleCharHeight = mTextPaint.getTextSize() + (TEXT_CHAR_SPACING - 2f); // 字间距减为6f
        float totalTextHeight = (singleCharHeight * text.length()) - (TEXT_CHAR_SPACING - 2f);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float charBottomOffset = fontMetrics.bottom;

        // 文本起始Y（底部对齐控件底部，无间距）
        float startTextY = getHeight() - getPaddingBottom() - charBottomOffset;

        // 逐字竖排绘制（文字居中于5dp文本区，无超出）
        for (int i = 0; i < text.length(); i++) {
            char singleChar = text.charAt(i);
            float currentTextY = startTextY - (i * singleCharHeight);
            canvas.drawText(String.valueOf(singleChar), textBaseX, currentTextY, mTextPaint);
        }
    }
}

