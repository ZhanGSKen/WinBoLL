package cc.winboll.studio.gallery;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CropOverlayView extends View {
    public interface OnCanvasSizeChangedListener {
        void onCanvasSizeChanged(RectF canvasBounds);
    }
    
    private OnCanvasSizeChangedListener canvasSizeChangedListener;
    
    private Paint borderPaint;
    private Paint cornerPaint;
    private RectF cropRect;
    private int touchArea = 50;
    
    private float lastX, lastY;
    private int activeCorner = -1;
    private static final int CORNER_TOP_LEFT = 0;
    private static final int CORNER_TOP_RIGHT = 1;
    private static final int CORNER_BOTTOM_LEFT = 2;
    private static final int CORNER_BOTTOM_RIGHT = 3;
    private static final int CORNER_CENTER = 4;
    
    private float targetRatio = 2.0f;
    private RectF canvasBounds = new RectF();
    private float minSize = 50;
    
    public CropOverlayView(Context context) {
        super(context);
        initPaints();
    }
    
    public CropOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }
    
    public CropOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaints();
    }
    
    private void initPaints() {
        borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor("#CCAA00"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);
        
        cornerPaint = new Paint();
        cornerPaint.setColor(Color.WHITE);
        cornerPaint.setStyle(Paint.Style.FILL);
    }
    
    public void setTargetRatio(float ratio) {
        this.targetRatio = ratio;
    }
    
    public float getTargetRatio() {
        return targetRatio;
    }
    
    public void setOnCanvasSizeChangedListener(OnCanvasSizeChangedListener listener) {
        this.canvasSizeChangedListener = listener;
    }
    
    public void initCanvas(int canvasW, int canvasH) {
        canvasBounds.set(0, 0, canvasW, canvasH);
        
        cropRect = new RectF(0, 0, canvasW, canvasH);
        
        if (canvasSizeChangedListener != null) {
            canvasSizeChangedListener.onCanvasSizeChanged(new RectF(canvasBounds));
        }
        invalidate();
    }
    
    public RectF getCropRect() {
        return new RectF(cropRect);
    }
    
    public RectF getCanvasBounds() {
        return new RectF(canvasBounds);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        canvas.drawColor(Color.TRANSPARENT);
        
        if (cropRect != null) {
            canvas.drawRect(cropRect, borderPaint);
            
            canvas.drawCircle(cropRect.left, cropRect.top, 12, cornerPaint);
            canvas.drawCircle(cropRect.right, cropRect.top, 12, cornerPaint);
            canvas.drawCircle(cropRect.left, cropRect.bottom, 12, cornerPaint);
            canvas.drawCircle(cropRect.right, cropRect.bottom, 12, cornerPaint);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (cropRect == null || canvasBounds.isEmpty()) return false;
        
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;
                activeCorner = getActiveCorner(x, y);
                return true;
                
            case MotionEvent.ACTION_MOVE:
                float dx = x - lastX;
                float dy = y - lastY;
                
                if (activeCorner == CORNER_CENTER) {
                    float newLeft = cropRect.left + dx;
                    float newTop = cropRect.top + dy;
                    float newRight = cropRect.right + dx;
                    float newBottom = cropRect.bottom + dy;
                    
                    if (newLeft >= canvasBounds.left && newRight <= canvasBounds.right) {
                        cropRect.left = newLeft;
                        cropRect.right = newRight;
                    }
                    if (newTop >= canvasBounds.top && newBottom <= canvasBounds.bottom) {
                        cropRect.top = newTop;
                        cropRect.bottom = newBottom;
                    }
                } else if (activeCorner == CORNER_TOP_LEFT) {
                    adjustCorner(cropRect.left + dx, cropRect.top + dy, true, true);
                } else if (activeCorner == CORNER_TOP_RIGHT) {
                    adjustCorner(cropRect.right + dx, cropRect.top + dy, false, true);
                } else if (activeCorner == CORNER_BOTTOM_LEFT) {
                    adjustCorner(cropRect.left + dx, cropRect.bottom + dy, true, false);
                } else if (activeCorner == CORNER_BOTTOM_RIGHT) {
                    adjustCorner(cropRect.right + dx, cropRect.bottom + dy, false, false);
                }
                
                lastX = x;
                lastY = y;
                invalidate();
                return true;
                
            case MotionEvent.ACTION_UP:
                activeCorner = -1;
                return true;
        }
        return super.onTouchEvent(event);
    }
    
    private void adjustCorner(float nx, float ny, boolean left, boolean top) {
        float newWidth;
        float newLeft = cropRect.left;
        float newTop = cropRect.top;
        float newRight = cropRect.right;
        float newBottom = cropRect.bottom;
        
        if (left) {
            newWidth = cropRect.width() - (nx - cropRect.left);
            newLeft = Math.max(canvasBounds.left, Math.min(nx, cropRect.right - minSize));
        } else {
            newWidth = nx - cropRect.left;
            newRight = Math.min(canvasBounds.right, Math.max(nx, cropRect.left + minSize));
            newLeft = cropRect.left;
        }
        
        float newHeight = newWidth / targetRatio;
        
        if (top) {
            newTop = Math.max(canvasBounds.top, Math.min(cropRect.bottom - minSize, cropRect.bottom - newHeight));
            newBottom = newTop + newHeight;
        } else {
            newBottom = Math.min(canvasBounds.bottom, Math.max(cropRect.top + minSize, cropRect.top + newHeight));
            newTop = newBottom - newHeight;
        }
        
        if (left) {
            cropRect.left = newLeft;
            cropRect.right = newLeft + newWidth;
        } else {
            cropRect.right = newRight;
            cropRect.left = newRight - newWidth;
        }
        
        if (top) {
            cropRect.top = newTop;
            cropRect.bottom = newTop + newHeight;
        } else {
            cropRect.bottom = newBottom;
            cropRect.top = newBottom - newHeight;
        }
    }
    
    private int getActiveCorner(float x, float y) {
        if (Math.abs(x - cropRect.left) <= touchArea && Math.abs(y - cropRect.top) <= touchArea) {
            return CORNER_TOP_LEFT;
        }
        if (Math.abs(x - cropRect.right) <= touchArea && Math.abs(y - cropRect.top) <= touchArea) {
            return CORNER_TOP_RIGHT;
        }
        if (Math.abs(x - cropRect.left) <= touchArea && Math.abs(y - cropRect.bottom) <= touchArea) {
            return CORNER_BOTTOM_LEFT;
        }
        if (Math.abs(x - cropRect.right) <= touchArea && Math.abs(y - cropRect.bottom) <= touchArea) {
            return CORNER_BOTTOM_RIGHT;
        }
        if (cropRect.contains(x, y)) {
            return CORNER_CENTER;
        }
        return -1;
    }
}