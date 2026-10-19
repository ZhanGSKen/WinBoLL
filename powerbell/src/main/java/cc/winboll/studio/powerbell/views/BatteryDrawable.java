package cc.winboll.studio.powerbell.views;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.models.BatteryStyle;

/**
 * 电池电量Drawable：适配API30，兼容小米机型，支持能量/条纹两种绘制风格切换
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/12/17 12:55
 */
public class BatteryDrawable extends Drawable {
    // ====================================== 静态常量区（按功能归类，消除魔法值） ======================================
    public static final String TAG = "BatteryDrawable";
    // 小米机型绘制偏移校准（适配MIUI渲染特性，避免绘制错位）
    private static final int MIUI_DRAW_OFFSET = 1;
    // 默认电量透明度（兼顾显示效果与API30渲染性能）
    private static final int DEFAULT_BATTERY_ALPHA = 210;
    // 电量范围常量
    private static final int BATTERY_MIN = 0;
    private static final int BATTERY_MAX = 100;
    // 条纹风格拆分数量
    private static final int STRIPE_COUNT = 100;

    // ====================================== 成员变量区（final优先，按功能归类） ======================================
    // 绘制画笔（final修饰，避免重复创建，提升性能）
    private final Paint mBatteryPaint;
    // 业务控制变量
    private int mBatteryValue = -1; // 当前电量（0-100，-1=未初始化）
    private BatteryStyle mBatteryStyle = BatteryStyle.ENERGY_STYLE; // 绘制风格（true=能量，false=条纹）

    // ====================================== 构造方法（重载适配，优先暴露常用构造） ======================================
    /**
     * 构造方法（默认能量风格，常用场景）
     * @param batteryColor 电量显示颜色
     */
    public BatteryDrawable(int batteryColor) {
        LogUtils.d(TAG, "【BatteryDrawable】构造器1调用 | 能量风格 | 颜色=" + Integer.toHexString(batteryColor));
        mBatteryPaint = new Paint();
        initPaintConfig(batteryColor);
    }

    /**
     * 构造方法（支持指定绘制风格，扩展场景）
     * @param batteryColor 电量显示颜色
     * @param isEnergyStyle 是否启用能量风格
     */
    public BatteryDrawable(int batteryColor, BatteryStyle batteryStyle) {
        mBatteryPaint = new Paint();
        mBatteryStyle = batteryStyle;
        initPaintConfig(batteryColor);
    }

	public void setIsEnergyStyle(BatteryStyle batteryStyle) {
		this.mBatteryStyle = batteryStyle;
	}

    // ====================================== 私有初始化方法（封装复用，隐藏内部逻辑） ======================================
    /**
     * 初始化画笔配置（适配API30渲染特性，优化小米机型兼容性）
     * @param color 电量显示颜色
     */
    private void initPaintConfig(int color) {
        LogUtils.d(TAG, "【initPaintConfig】画笔配置开始 | 颜色=" + Integer.toHexString(color));
        mBatteryPaint.setColor(color);
        mBatteryPaint.setAlpha(DEFAULT_BATTERY_ALPHA);
        mBatteryPaint.setAntiAlias(true); // 抗锯齿，解决小米低分辨率锯齿问题
        mBatteryPaint.setStyle(Paint.Style.FILL); // 固定填充模式，避免混乱
        mBatteryPaint.setDither(false); // 禁用抖动，提升API30颜色显示一致性
        LogUtils.d(TAG, "【initPaintConfig】画笔配置完成");
    }

    // ====================================== 核心绘制方法（Drawable抽象方法，优先级最高） ======================================
    @Override
    public void draw(Canvas canvas) {
		// 未初始化/异常电量，直接跳过，避免无效绘制
        if (mBatteryValue < 0) {
            LogUtils.w(TAG, "【draw】电量未初始化，跳过绘制");
            return;
        }
        // 强制校准电量范围（0-100），防止异常值导致绘制错误
        int validBattery = Math.max(BATTERY_MIN, Math.min(mBatteryValue, BATTERY_MAX));
        LogUtils.d(TAG, "【draw】电量校准完成 | 有效电量=" + validBattery);

        Rect drawBounds = getBounds();
        // 绘制边界空指针防护
        if (drawBounds == null) {
            LogUtils.e(TAG, "【draw】绘制边界为空，跳过绘制");
            return;
        }
        int drawHeight = drawBounds.height();

        // 小米机型绘制偏移校准（解决MIUI系统渲染偏移问题）
        int offset = MIUI_DRAW_OFFSET;
        int left = drawBounds.left + offset;
        int right = drawBounds.right - offset;
        LogUtils.d(TAG, "【draw】绘制参数校准 | 左边界=" + left + " | 右边界=" + right + " | 高度=" + drawHeight);

        // 按风格执行绘制
        if (mBatteryStyle == BatteryStyle.ENERGY_STYLE) {
            drawEnergyStyle(canvas, validBattery, left, right, drawHeight);
        } else if (mBatteryStyle == BatteryStyle.ZEBRA_STYLE) {
            drawZebraStyle(canvas, validBattery, left, right, drawHeight);
        } else if (mBatteryStyle == BatteryStyle.POINT_STYLE) {
            drawPointStyle(canvas, validBattery, left, right, drawHeight);
        }
        LogUtils.d(TAG, "【draw】绘制完成");
    }

    // ====================================== 绘制风格实现（私有封装，按风格拆分） ======================================
    /**
     * 能量风格绘制（整块填充，高效简洁，默认风格）
     * @param canvas 绘制画布
     * @param battery 有效电量（0-100）
     * @param left 左边界
     * @param right 右边界
     * @param height 绘制高度
     */
    private void drawEnergyStyle(Canvas canvas, int battery, int left, int right, int height) {
        LogUtils.d(TAG, "【drawEnergyStyle】能量风格绘制开始 | 电量=" + battery);
//        int top = height - (height * battery / BATTERY_MAX); // 计算电量对应顶部坐标
//        canvas.drawRect(new Rect(left, top, right, height), mBatteryPaint);
//        LogUtils.d(TAG, "【drawEnergyStyle】能量风格绘制完成 | 顶部坐标=" + top);
		int nWidth = getBounds().width();
        int nHeight = getBounds().height();
        int mnDx = nHeight / 203;

        // 绘制耗电电量提醒值电量
        // 能量绘图风格
        int nTop;
        int nLeft    = 0;
        int nBottom;
        int nRight  = nWidth;

        //for (int i = 0; i < mnValue; i ++) {
        nBottom = nHeight;
        nTop = nHeight - (nHeight * mBatteryValue / 100);
        canvas.drawRect(new Rect(nLeft, nTop, nRight, nBottom), mBatteryPaint);

    }

    /**
     * 条纹风格绘制（分段条纹，扩展风格）
     * @param canvas 绘制画布
     * @param battery 有效电量（0-100）
     * @param left 左边界
     * @param right 右边界
     * @param height 绘制高度
     */
    private void drawZebraStyle(Canvas canvas, int battery, int left, int right, int height) {
        LogUtils.d(TAG, "【drawStripeStyle】条纹风格绘制开始 | 电量=" + battery);
//        int stripeHeight = height / STRIPE_COUNT; // 单条条纹高度（均匀拆分）
//        // 从底部向上绘制对应电量条纹
//        for (int i = 0; i < battery; i++) {
//            int bottom = height - (stripeHeight * i);
//            int top = bottom - stripeHeight;
//            canvas.drawRect(new Rect(left, top, right, bottom), mBatteryPaint);
//        }

		int nWidth = getBounds().width();
        int nHeight = getBounds().height();
        int mnDx = nHeight / 203;


		// 意兴阑珊绘图风格
		int nTop;
		int nLeft    = 0;
		int nBottom;
		int nRight  = nWidth;

		for (int i = 0; i < mBatteryValue; i ++) {
			nBottom = (nHeight * (100 - i) / 100) - mnDx;
			nTop = nBottom + mnDx;
			canvas.drawRect(new Rect(nLeft, nTop, nRight, nBottom), mBatteryPaint);
		}
        LogUtils.d(TAG, "【drawStripeStyle】条纹风格绘制完成 | 条纹数量=" + battery);
    }


    /**
     * 点阵风格绘制
     * @param canvas 绘制画布
     * @param battery 有效电量（0-100）
     * @param left 左边界
     * @param right 右边界
     * @param height 绘制高度
     */
    private void drawPointStyle(Canvas canvas, int battery, int left, int right, int height) {
        LogUtils.d(TAG, "【drawStripeStyle】条纹风格绘制开始 | 电量=" + battery);

		int nWidth = getBounds().width();
        int nHeight = getBounds().height();
        int mnDx = nHeight / 203;


		// 意兴阑珊绘图风格
		int nTop;
		int nLeft    = 0;
		int nBottom;
		int nRight  = nWidth;

		int nLineWidth = nRight - nLeft;
		int radius_horizontal = (nLineWidth / 10) / 2;
		int radius_vertical = mnDx/2;
		int radius = Math.min(radius_horizontal, radius_vertical);

		for (int i = 0; i < mBatteryValue; i ++) {
			nBottom = (nHeight * (100 - i) / 100) - mnDx;
			nTop = nBottom + mnDx;
			//canvas.drawRect(new Rect(nLeft, nTop, nRight, nBottom), mBatteryPaint);

			for (int j = 0; j < 10; j++) {
				// cx, cy 圆心坐标；radius 半径；paint 画笔
				int cx = radius_horizontal + radius_horizontal * j * 2;
				int cy = nTop + radius_vertical;
				canvas.drawCircle(cx, cy, radius, mBatteryPaint);
			}
		}
        LogUtils.d(TAG, "【drawStripeStyle】条纹风格绘制完成 | 条纹数量=" + battery);
    }

    // ====================================== 对外暴露方法（业务控制入口，按功能排序） ======================================
    /**
     * 设置当前电量（外部核心调用入口）
     * @param value 电量值（0-100）
     */
    public void setBatteryValue(int value) {
        LogUtils.d(TAG, "【setBatteryValue】电量更新 | 旧值=" + mBatteryValue + " | 新值=" + value);
        mBatteryValue = value;
        invalidateSelf(); // 触发重绘，确保UI实时更新
        LogUtils.d(TAG, "【setBatteryValue】已触发重绘");
    }

    /**
     * 切换绘制风格
     * @param isEnergyStyle true=能量风格，false=条纹风格
     */
    public void setDrawStyle(BatteryStyle batteryStyle) {
        mBatteryStyle = batteryStyle;
        invalidateSelf();
        LogUtils.d(TAG, "【switchDrawStyle】已触发重绘");
    }

    /**
     * 更新电量显示颜色
     * @param color 新颜色值
     */
    public void updateBatteryColor(int color) {
        String oldColor = Integer.toHexString(mBatteryPaint.getColor());
        String newColor = Integer.toHexString(color);
        LogUtils.d(TAG, "【updateBatteryColor】颜色更新 | 旧颜色=" + oldColor + " | 新颜色=" + newColor);
        mBatteryPaint.setColor(color);
        invalidateSelf();
        LogUtils.d(TAG, "【updateBatteryColor】已触发重绘");
    }

    // ====================================== Getter方法（按需暴露，简洁无冗余） ======================================
    /**
     * 获取当前电量
     * @return 电量值（0-100，-1=未初始化）
     */
    public int getBatteryValue() {
        return mBatteryValue;
    }



    public BatteryStyle getEnergyStyle() {
        return mBatteryStyle;
    }

    // ====================================== Drawable抽象方法（必须实现，精简逻辑） ======================================
    @Override
    public void setAlpha(int alpha) {
        LogUtils.d(TAG, "【setAlpha】透明度更新 | 旧值=" + mBatteryPaint.getAlpha() + " | 新值=" + alpha);
        mBatteryPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        LogUtils.d(TAG, "【setColorFilter】设置颜色过滤 | filter=" + colorFilter);
        mBatteryPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        // 固定返回半透明，适配API30透明度渲染机制，兼容小米机型
        return PixelFormat.TRANSLUCENT;
    }
}

