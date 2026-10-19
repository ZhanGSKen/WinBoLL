package cc.winboll.studio.mymessagemanager.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import cc.winboll.studio.mymessagemanager.R;

public class DraggableView extends FrameLayout {
    // SP配置常量（新增尺寸保存键）
    private static final String SP_NAME = "TTS_FLOAT_DRAG_CONFIG";
    private static final String KEY_LEFT = "drag_view_left";
    private static final String KEY_TOP = "drag_view_top";
    private static final String KEY_WIDTH = "drag_view_width"; // 新增：保存布局宽度
    private static final String KEY_HEIGHT = "drag_view_height"; // 新增：保存布局高度

    // 位置/尺寸变量
    private int viewLeft;
    private int viewTop;
    private int viewWidth;
    private int viewHeight;
    private int screenWidth;
    private int screenHeight;
    // 拖动相关
    private float downX;
    private float downY;
    private boolean isDragging = false;

    // 构造方法
    public DraggableView(Context context) {
        super(context);
        init();
    }

    public DraggableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DraggableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_tts_back, this, true);
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					ViewTreeObserver currentVto = getViewTreeObserver();
					if (currentVto.isAlive()) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
							currentVto.removeOnGlobalLayoutListener(this);
						} else {
							currentVto.removeGlobalOnLayoutListener(this);
						}
					}
					// 获取布局实际宽高
					viewWidth = getMeasuredWidth();
					viewHeight = getMeasuredHeight();
					// 保存尺寸到SP（新增）
					saveViewSize();
					// 初始化位置
					initPosition();
					updateViewPosition();
				}
			});
    }

    // 初始化位置（不变）
    private void initPosition() {
        SharedPreferences sp = getContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        int defaultLeft = screenWidth - viewWidth;
        int defaultTop = screenHeight - viewHeight;
        viewLeft = sp.getInt(KEY_LEFT, defaultLeft);
        viewTop = sp.getInt(KEY_TOP, defaultTop);
        checkBoundary();
    }

    // 新增：保存布局尺寸到SP
    private void saveViewSize() {
        if (viewWidth > 0 && viewHeight > 0) {
            SharedPreferences sp = getContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
            sp.edit()
				.putInt(KEY_WIDTH, viewWidth)
				.putInt(KEY_HEIGHT, viewHeight)
				.apply();
        }
    }

    // 新增：公共静态方法 - 查询最后保存的布局尺寸
    public static int[] getLastViewSize(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        // 默认尺寸：120x120像素（与view_tts_back.xml示例尺寸一致，避免无值时异常）
        int defaultWidth = dp2px(context, 120);
        int defaultHeight = dp2px(context, 120);
        // 从SP读取尺寸（无值则用默认）
        int width = sp.getInt(KEY_WIDTH, defaultWidth);
        int height = sp.getInt(KEY_HEIGHT, defaultHeight);
        return new int[]{width, height};
    }

    // 新增：dp转px工具方法（确保默认尺寸适配不同屏幕）
    private static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    // 原有方法（checkBoundary、updateViewPosition、savePosition、onTouchEvent、getLastPosition）保持不变
    private void checkBoundary() {
        viewLeft = Math.max(0, viewLeft);
        viewTop = Math.max(0, viewTop);
        viewLeft = Math.min(screenWidth - viewWidth, viewLeft);
        viewTop = Math.min(screenHeight - viewHeight, viewTop);
    }

    private void updateViewPosition() {
        LayoutParams params = (LayoutParams) getLayoutParams();
        if (params != null) {
            params.leftMargin = viewLeft;
            params.topMargin = viewTop;
            setLayoutParams(params);
        }
    }

    private void savePosition() {
        SharedPreferences sp = getContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit()
			.putInt(KEY_LEFT, viewLeft)
			.putInt(KEY_TOP, viewTop)
			.apply();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (viewWidth == 0 || viewHeight == 0) return super.onTouchEvent(event);

        float rawX = event.getRawX();
        float rawY = event.getRawY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDragging = true;
                downX = rawX - viewLeft;
                downY = rawY - viewTop;
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    viewLeft = (int) (rawX - downX);
                    viewTop = (int) (rawY - downY);
                    checkBoundary();
                    updateViewPosition();
                }
                break;

            case MotionEvent.ACTION_UP:
                if (isDragging) {
                    isDragging = false;
                    savePosition();
                }
                break;
        }
        return true;
    }

    public static int[] getLastPosition(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        int defaultLeft = 0;
        int defaultTop = 0;

        int left = sp.getInt(KEY_LEFT, defaultLeft);
        int top = sp.getInt(KEY_TOP, defaultTop);
        return new int[]{left, top};
    }
}

