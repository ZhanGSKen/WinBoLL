package cc.winboll.studio.gallery;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;

public class ZoomContainerView extends FrameLayout {
    private float scaleFactor = 1.0f;
    private float minScale = 0.1f;
    private float maxScale = 5.0f;
    private static final float ZOOM_STEP = 0.25f;
    
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private Paint borderPaint;
    
    public ZoomContainerView(Context context) {
        super(context);
        init();
    }
    
    public ZoomContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ZoomContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        setWillNotDraw(false);
        setBackgroundColor(Color.YELLOW);
        
        borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor("#333333"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2);
        
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(minScale, Math.min(scaleFactor, maxScale));
                invalidate();
                requestLayout();
                return true;
            }
        });
        
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (scaleFactor > 1.5f) {
                    scaleFactor = 1.0f;
                } else {
                    scaleFactor = 2.0f;
                }
                invalidate();
                requestLayout();
                return true;
            }
        });
    }
    
    public void zoomIn() {
        scaleFactor = Math.min(maxScale, scaleFactor + ZOOM_STEP);
        requestMeasure();
    }
    
    public void zoomOut() {
        scaleFactor = Math.max(minScale, scaleFactor - ZOOM_STEP);
        requestMeasure();
    }
    
    private void requestMeasure() {
        removeCallbacks(measureRunable);
        post(measureRunable);
    }
    
    private Runnable measureRunable = new Runnable() {
        @Override
        public void run() {
            requestLayout();
            invalidate();
        }
    };
    
    public float getScaleFactor() {
        return scaleFactor;
    }
    
    public void setScaleFactor(float scale) {
        scaleFactor = Math.max(minScale, Math.min(scale, maxScale));
        int childCount = getChildCount();
        if (childCount > 0) {
            View child = getChildAt(0);
            if (child instanceof CropCanvasView) {
                ((CropCanvasView) child).setContainerScale(scaleFactor);
            }
        }
        requestLayout();
        invalidate();
    }
    
    public void resetZoom() {
        scaleFactor = 1.0f;
        invalidate();
        requestLayout();
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childCount = getChildCount();
        if (childCount > 0) {
            View child = getChildAt(0);
            child.measure(widthMeasureSpec, heightMeasureSpec);
            int childW = child.getMeasuredWidth();
            int childH = child.getMeasuredHeight();
            int scaledW = (int) (childW * scaleFactor);
            int scaledH = (int) (childH * scaleFactor);
            int widthSpec = MeasureSpec.makeMeasureSpec(scaledW, MeasureSpec.EXACTLY);
            int heightSpec = MeasureSpec.makeMeasureSpec(scaledH, MeasureSpec.EXACTLY);
            child.measure(widthSpec, heightSpec);
            setMeasuredDimension(scaledW, scaledH);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childCount = getChildCount();
        if (childCount > 0) {
            View child = getChildAt(0);
            int childW = child.getMeasuredWidth();
            int childH = child.getMeasuredHeight();
            int scaledW = (int) (childW * scaleFactor);
            int scaledH = (int) (childH * scaleFactor);
            int parentW = right - left;
            int parentH = bottom - top;
            int childLeft = (parentW - scaledW) / 2;
            int childTop = (parentH - scaledH) / 2;
            child.layout(childLeft, childTop, childLeft + scaledW, childTop + scaledH);
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = scaleGestureDetector.onTouchEvent(event);
        if (!scaleGestureDetector.isInProgress()) {
            handled = gestureDetector.onTouchEvent(event) || handled;
        }
        return handled || super.onTouchEvent(event);
    }
}