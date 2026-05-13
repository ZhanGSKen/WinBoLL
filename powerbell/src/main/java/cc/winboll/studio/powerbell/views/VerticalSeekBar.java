package cc.winboll.studio.powerbell.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * 垂直进度条控件，适配 API30，支持逆时针旋转（0在下，100在上）
 * 修复滑块同步+弹窗触发bug，新增实时进度变化监听接口，支持拖动时实时回调进度
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/12/17 14:11
 */
public class VerticalSeekBar extends SeekBar {
    // ======================== 静态常量 =========================
    private static final String TAG = VerticalSeekBar.class.getSimpleName();

    // ======================== 接口定义（前置，便于外部调用）========================
    /**
     * 垂直进度条触摸事件回调接口，解决原生 OnSeekBarChangeListener 回调失效问题
     * 直接在触摸抬起时回调，确保配置变更对话框100%触发
     */
    public interface OnVerticalSeekBarTouchListener {
        /**
         * 触摸抬起时回调（滑块停止滑动，触发弹窗的核心时机）
         * @param seekBar 当前垂直进度条实例
         * @param progress 最终滑动进度（0~100）
         */
        void onTouchUp(VerticalSeekBar seekBar, int progress);

        /**
         * 触摸取消时回调（可选，用于异常场景进度回滚）
         * @param seekBar 当前垂直进度条实例
         * @param progress 取消时的进度
         */
        void onTouchCancel(VerticalSeekBar seekBar, int progress);
    }

    /**
     * 垂直进度条实时进度变化监听接口
     * 支持拖动过程中实时回调进度，用于比值预览等实时UI更新场景
     */
    public interface OnVerticalSeekBarChangeListener {
        /**
         * 进度变化时回调
         * @param seekBar 当前垂直进度条实例
         * @param progress 当前进度（0~100）
         * @param fromUser 是否是用户触摸导致的进度变化
         */
        void onProgressChanged(VerticalSeekBar seekBar, int progress, boolean fromUser);

        /**
         * 开始触摸进度条时回调
         * @param seekBar 当前垂直进度条实例
         */
        void onStartTrackingTouch(VerticalSeekBar seekBar);

        /**
         * 停止触摸进度条时回调
         * @param seekBar 当前垂直进度条实例
         */
        void onStopTrackingTouch(VerticalSeekBar seekBar);
    }

    // ======================== 成员变量 =========================
    // 核心状态：当前进度缓存，修复滑块同步问题（volatile 保证多线程可见性）
    private volatile int mProgress = -1;
    // 监听接口：触摸事件回调（原有，用于弹窗触发）
    private OnVerticalSeekBarTouchListener mTouchListener;
    // 监听接口：实时进度变化回调（新增，用于比值计算）
    private OnVerticalSeekBarChangeListener mProgressChangeListener;

    // ======================== 构造方法 =========================
    public VerticalSeekBar(Context context) {
        super(context);
        initView();
        LogUtils.d(TAG, "【构造器1】VerticalSeekBar 初始化完成");
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        LogUtils.d(TAG, "【构造器2】VerticalSeekBar 初始化完成");
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
        LogUtils.d(TAG, "【构造器3】VerticalSeekBar 初始化完成");
    }

    // ======================== 初始化方法 =========================
    private void initView() {
        // 移除水平默认阴影，优化垂直显示效果，减少 API30 不必要的绘制开销
        setBackgroundDrawable(null);
        LogUtils.d(TAG, "【initView】移除默认背景阴影，完成视图初始化");
    }

    // ======================== 对外设置方法（监听接口绑定）========================
    /**
     * 设置触摸事件监听器（给外部调用，如 MainContentView 绑定）
     * @param listener 触摸事件回调实例
     */
    public void setOnVerticalSeekBarTouchListener(OnVerticalSeekBarTouchListener listener) {
        this.mTouchListener = listener;
        LogUtils.d(TAG, "【setOnVerticalSeekBarTouchListener】触摸监听器绑定完成");
    }

    /**
     * 设置实时进度变化监听器（给外部调用，如 MainContentView 绑定）
     * @param listener 实时进度变化回调实例
     */
    public void setOnVerticalSeekBarChangeListener(OnVerticalSeekBarChangeListener listener) {
        this.mProgressChangeListener = listener;
        LogUtils.d(TAG, "【setOnVerticalSeekBarChangeListener】实时进度监听器绑定完成");
    }

    // ======================== 重写系统方法（测量/布局/绘制）========================
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
        LogUtils.v(TAG, "【onMeasure】垂直测量完成，宽=" + getMeasuredHeight() + "，高=" + getMeasuredWidth());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
        LogUtils.v(TAG, "【onSizeChanged】尺寸变化，新宽=" + h + "，新高=" + w);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 逆时针旋转90度，平移画布避免绘制偏移（0在下，100在上）
        canvas.rotate(-90);
        canvas.translate(-getHeight(), 0);
        super.onDraw(canvas);
        LogUtils.v(TAG, "【onDraw】完成垂直绘制，旋转角度=-90°");
    }

    // ======================== 重写进度设置方法（修复滑块同步+新增实时回调）========================
    /**
     * 重写进度设置，调用尺寸变化方法强制刷新，解决 setProgress 滑块不跟随问题
     * 新增：支持外部调用 setProgress 时触发实时进度回调
     */
    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
        // 强制触发尺寸变化，同步刷新滑块位置（核心bug修复逻辑）
        onSizeChanged(getWidth(), getHeight(), 0, 0);
        mProgress = progress;
        LogUtils.d(TAG, "【setProgress】进度设置为" + progress + "，滑块同步刷新");
        // 触发实时进度监听（外部调用 setProgress 时 fromUser 为 false）
        if (mProgressChangeListener != null) {
            mProgressChangeListener.onProgressChanged(this, progress, false);
            LogUtils.v(TAG, "【setProgress】触发实时进度回调，fromUser=false");
        }
    }

    // ======================== 重写触摸事件（优化事件透传+实时进度回调）========================
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 先调用父类方法，保留原生监听器兼容性，同时强制透传事件
        super.onTouchEvent(event);
        boolean handled = true; // 强制消费事件，避免事件被拦截导致回调丢失
        boolean fromUser = true; // 标记是否是用户触摸导致的进度变化
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                LogUtils.d(TAG, "【onTouchEvent】触摸按下，Y坐标=" + event.getY());
                // 触发实时进度监听：开始触摸
                if (mProgressChangeListener != null) {
                    mProgressChangeListener.onStartTrackingTouch(this);
                    LogUtils.v(TAG, "【onTouchEvent】触发开始触摸回调");
                }
                break;

            case MotionEvent.ACTION_MOVE:
                calculateProgress(event.getY());
                setProgress(mProgress);
                LogUtils.v(TAG, "【onTouchEvent】触摸滑动，进度更新为" + mProgress);
                // 触发实时进度监听：进度变化
                if (mProgressChangeListener != null) {
                    mProgressChangeListener.onProgressChanged(this, mProgress, fromUser);
                }
                break;

            case MotionEvent.ACTION_UP:
                calculateProgress(event.getY());
                setProgress(mProgress);
                LogUtils.d(TAG, "【onTouchEvent】触摸抬起，进度=" + mProgress + "，触发弹窗回调");
                // 触发实时进度监听：进度变化+停止触摸
                if (mProgressChangeListener != null) {
                    mProgressChangeListener.onProgressChanged(this, mProgress, fromUser);
                    mProgressChangeListener.onStopTrackingTouch(this);
                    LogUtils.v(TAG, "【onTouchEvent】触发停止触摸回调");
                }
                // 核心：调用原有触摸接口，通知外部触发配置变更对话框
                if (mTouchListener != null) {
                    mTouchListener.onTouchUp(this, mProgress);
                    LogUtils.v(TAG, "【onTouchEvent】触发触摸抬起回调");
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                int currentProgress = getProgress();
                LogUtils.d(TAG, "【onTouchEvent】触摸取消，当前进度=" + currentProgress);
                // 触发实时进度监听：停止触摸
                if (mProgressChangeListener != null) {
                    mProgressChangeListener.onStopTrackingTouch(this);
                }
                // 可选：触摸取消时回调，外部可做进度回滚处理
                if (mTouchListener != null) {
                    mTouchListener.onTouchCancel(this, currentProgress);
                    LogUtils.v(TAG, "【onTouchEvent】触发触摸取消回调");
                }
                break;
        }
        return handled;
    }

    // ======================== 内部工具方法 =========================
    /**
     * 计算垂直进度，校准范围 0~100，避免异常值
     * @param touchY 触摸点Y坐标
     */
    private void calculateProgress(float touchY) {
        // 核心进度计算公式（逆时针旋转适配）
        mProgress = getMax() - (int) (getMax() * touchY / getHeight());
        // 校准进度范围，防止超出 0~100（兼容 API30 进度边界校验）
        mProgress = Math.max(0, Math.min(mProgress, getMax()));
        LogUtils.v(TAG, "【calculateProgress】触摸Y=" + touchY + "，计算进度=" + mProgress + "，校准后=" + mProgress);
    }
}

