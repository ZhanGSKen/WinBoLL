package cc.winboll.studio.gallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CropCanvasView extends View {
    public interface OnBackgroundColorChangedListener {
        void onBackgroundColorChanged(int color);
    }
    
    public interface OnColorPickedListener {
        void onColorPicked(int color);
    }
    
    public interface OnColorPickEndListener {
        void onColorPickEnd();
    }
    
    private OnBackgroundColorChangedListener backgroundColorChangedListener;
    private OnColorPickedListener colorPickedListener;
    private OnColorPickEndListener colorPickEndListener;
    
    public void setOnBackgroundColorChangedListener(OnBackgroundColorChangedListener listener) {
        this.backgroundColorChangedListener = listener;
    }
    
    public void setOnColorPickedListener(OnColorPickedListener listener) {
        this.colorPickedListener = listener;
    }
    
    public void setOnColorPickEndListener(OnColorPickEndListener listener) {
        this.colorPickEndListener = listener;
    }
    private Paint imagePaint;
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
    
    private int imageWidth;
    private int imageHeight;
    private float coverRatio = 2.0f;
    
    private int extendHeight;
    private int extendWidth;
    private int canvasWidth;
    private int canvasHeight;
    private float minSize = 50;
    
    private RectF imageBounds = new RectF();
    private RectF canvasBounds = new RectF();
    private Bitmap originalBitmap;
    private Bitmap canvasBitmap;
    private float bitmapScale = 1.0f;
    private Bitmap displayBitmap;
    private RectF initialSpanRect;
    private float initialSpan;
    private int bgType = 2;
    private Bitmap tileBitmap;
    private BitmapShader tileShader;
    private Paint bgPaint;
    private int backgroundColor = Color.BLUE;
    private boolean colorPickMode = false;
    private int previewColor = 0;
    private float lastPickX = 0;
    private float lastPickY = 0;
    private float pickX, pickY;
    
    private float displayScale = 1.0f;
    private float displayOffsetX = 0f;
    private float displayOffsetY = 0f;
    private static final int MAX_DISPLAY_SIZE = 2048;
    private float containerScale = 1.0f;
    
    public CropCanvasView(Context context) {
        super(context);
        init();
    }
    
    public CropCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public CropCanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        imagePaint = new Paint();
        imagePaint.setFilterBitmap(true);
        
        borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor("#CCAA00"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);
        
        cornerPaint = new Paint();
        cornerPaint.setColor(Color.WHITE);
        cornerPaint.setStyle(Paint.Style.FILL);
        
        bgType = 2;
        bgPaint = new Paint();
        initTileBitmap();
    }
    
    private void initTileBitmap() {
        if (tileBitmap != null && !tileBitmap.isRecycled()) {
            tileBitmap.recycle();
            tileBitmap = null;
        }
        tileShader = null;
        
        if (bgType == 0) {
            Drawable drawable = getContext().getDrawable(R.drawable.bg_checkerboard);
            if (drawable != null) {
                int w = drawable.getIntrinsicWidth();
                int h = drawable.getIntrinsicHeight();
                if (w <= 0) w = 10;
                if (h <= 0) h = 10;
                tileBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(tileBitmap);
                drawable.setBounds(0, 0, w, h);
                drawable.draw(c);
                tileShader = new BitmapShader(tileBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            }
        }
    }
    
    public void setBackgroundType(int type) {
        if (bgType != type) {
            bgType = type;
            initTileBitmap();
            invalidate();
        }
    }
    
    public int getBackgroundType() {
        return bgType;
    }
    
    private void drawBackground(Canvas canvas) {
        if (bgType == 0 && tileShader != null) {
            bgPaint.setShader(tileShader);
            canvas.drawRect(canvasBounds, bgPaint);
        } else if (bgType == 1) {
            bgPaint.setShader(null);
            bgPaint.setColor(Color.WHITE);
            canvas.drawRect(canvasBounds, bgPaint);
        } else {
            bgPaint.setShader(null);
            bgPaint.setColor(Color.BLACK);
            canvas.drawRect(canvasBounds, bgPaint);
        }
    }
    
    public void setImageBitmap(Bitmap bitmap) {
        if (displayBitmap != null && displayBitmap != originalBitmap) {
            displayBitmap.recycle();
        }
        this.originalBitmap = bitmap;
        createDisplayBitmap();
    }
    
    private void createDisplayBitmap() {
        if (displayBitmap != null && displayBitmap != originalBitmap) {
            displayBitmap.recycle();
            displayBitmap = null;
        }
        if (originalBitmap == null || originalBitmap.isRecycled()) {
            return;
        }
        int w = originalBitmap.getWidth();
        int h = originalBitmap.getHeight();
        float scale = 1.0f;
        if (w > MAX_DISPLAY_SIZE || h > MAX_DISPLAY_SIZE) {
            scale = Math.min((float) MAX_DISPLAY_SIZE / w, (float) MAX_DISPLAY_SIZE / h);
            int newW = (int) (w * scale);
            int newH = (int) (h * scale);
            displayBitmap = Bitmap.createScaledBitmap(originalBitmap, newW, newH, true);
        } else {
            displayBitmap = originalBitmap;
        }
        displayBitmapScale = scale;
    }
    
    private float displayBitmapScale = 1.0f;
    
    public Bitmap getOriginalBitmap() {
        return originalBitmap;
    }
    
    public float getDisplayBitmapScale() {
        return displayBitmapScale;
    }
    
    public void onContainerScaled() {
        containerScale = 1.0f;
        invalidate();
    }
    
    public void setContainerScale(float scale) {
        if (scale > 0 && scale != containerScale) {
            containerScale = scale;
            invalidate();
        }
    }
    
    public float getContainerScale() {
        return containerScale;
    }
    
    public Bitmap getCanvasBitmap() {
        if (canvasWidth <= 0 || canvasHeight <= 0) {
            return null;
        }
        Bitmap canvasBmp = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(canvasBmp);
        drawBackground(canvas);
        if (displayBitmap != null && !displayBitmap.isRecycled()) {
            if (displayBitmap == originalBitmap) {
                canvas.drawBitmap(displayBitmap, imageBounds.left, imageBounds.top, imagePaint);
            } else {
                float invScale = 1f / displayBitmapScale;
                canvas.save();
                canvas.scale(invScale, invScale);
                canvas.drawBitmap(displayBitmap, imageBounds.left * displayBitmapScale, 
                                  imageBounds.top * displayBitmapScale, imagePaint);
                canvas.restore();
            }
        }
        return canvasBmp;
    }
    
    public void initCanvas(int imgWidth, int imgHeight, float ratio) {
        this.imageWidth = imgWidth;
        this.imageHeight = imgHeight;
        this.coverRatio = ratio;
        
        float imgRatio = (float) imgWidth / imgHeight;
        
        if (coverRatio >= imgRatio) {
            extendHeight = imgHeight;
            extendWidth = (int) (imgHeight * coverRatio);
        } else {
            extendWidth = imgWidth;
            extendHeight = (int) (imgWidth / coverRatio);
        }
        
        canvasHeight = Math.max(imageHeight, extendHeight);
        canvasWidth = Math.max(imageWidth, extendWidth);
        
        float left = (canvasWidth - imageWidth) / 2f;
        float top = (canvasHeight - imageHeight) / 2f;
        imageBounds.set(left, top, left + imageWidth, top + imageHeight);
        canvasBounds.set(0, 0, canvasWidth, canvasHeight);
        
        cropRect = new RectF(canvasBounds);
        
        requestLayout();
        invalidate();
    }
    
    public void getDisplayMatrix(Matrix matrix) {
        if (canvasWidth <= 0 || canvasHeight <= 0 || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        matrix.reset();
        
        float displayScaleX = (float) getWidth() / canvasWidth;
        float displayScaleY = (float) getHeight() / canvasHeight;
        displayScale = Math.min(displayScaleX, displayScaleY);
        
        displayOffsetX = (getWidth() - canvasWidth * displayScale) / 2f;
        displayOffsetY = (getHeight() - canvasHeight * displayScale) / 2f;
        
        matrix.postTranslate(displayOffsetX, displayOffsetY);
        matrix.postScale(displayScale, displayScale);
    }
    
    public float screenToImageX(float screenX) {
        return (screenX - displayOffsetX) / displayScale;
    }
    
    public float screenToImageY(float screenY) {
        return (screenY - displayOffsetY) / displayScale;
    }
    
    public float imageToScreenX(float imageX) {
        return imageX * displayScale + displayOffsetX;
    }
    
    public float imageToScreenY(float imageY) {
        return imageY * displayScale + displayOffsetY;
    }
    
    public int getImageColorAt(float screenX, float screenY) {
        Bitmap bmp = (displayBitmap != null) ? displayBitmap : originalBitmap;
        if (bmp == null || bmp.isRecycled()) {
            return Color.TRANSPARENT;
        }
        float imgX = screenToImageX(screenX);
        float imgY = screenToImageY(screenY);
        int bmpX = (int) ((imgX - imageBounds.left) * displayBitmapScale);
        int bmpY = (int) ((imgY - imageBounds.top) * displayBitmapScale);
        if (bmpX >= 0 && bmpX < bmp.getWidth() && 
            bmpY >= 0 && bmpY < bmp.getHeight()) {
            return bmp.getPixel(bmpX, bmpY);
        }
        return Color.TRANSPARENT;
    }
    
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        if (backgroundColorChangedListener != null) {
            backgroundColorChangedListener.onBackgroundColorChanged(color);
        }
        invalidate();
    }
    
    public int getBackgroundColor() {
        return backgroundColor;
    }
    
    public int getPreviewColor() {
        return previewColor;
    }
    
    public float getLastPickX() {
        return lastPickX;
    }
    
    public float getLastPickY() {
        return lastPickY;
    }
    
    public int getLastPickImageX() {
        if (originalBitmap == null || originalBitmap.isRecycled()) {
            return 0;
        }
        float imgX = screenToImageX(lastPickX);
        float imgY = screenToImageY(lastPickY);
        int bmpX = (int) ((imgX - imageBounds.left) * displayBitmapScale);
        int bmpY = (int) ((imgY - imageBounds.top) * displayBitmapScale);
        return Math.max(0, Math.min(bmpX, originalBitmap.getWidth() - 1));
    }
    
    public int getLastPickImageY() {
        if (originalBitmap == null || originalBitmap.isRecycled()) {
            return 0;
        }
        float imgX = screenToImageX(lastPickX);
        float imgY = screenToImageY(lastPickY);
        int bmpX = (int) ((imgX - imageBounds.left) * displayBitmapScale);
        int bmpY = (int) ((imgY - imageBounds.top) * displayBitmapScale);
        return Math.max(0, Math.min(bmpY, originalBitmap.getHeight() - 1));
    }
    
    public void setColorPickMode(boolean enable) {
        this.colorPickMode = enable;
        if (enable) {
            pickX = -1;
            pickY = -1;
        }
        invalidate();
    }
    
    public boolean isColorPickMode() {
        return colorPickMode;
    }
    
    public void scaleToView(int viewWidth, int viewHeight) {
        if (viewWidth > 0 && viewHeight > 0 && canvasWidth > 0 && canvasHeight > 0) {
            float scale = Math.max((float) viewWidth / canvasWidth, (float) viewHeight / canvasHeight);
            int oldCanvasW = canvasWidth;
            int oldCanvasH = canvasHeight;
            canvasWidth = (int) (canvasWidth * scale);
            canvasHeight = (int) (canvasHeight * scale);
            
            float left = (canvasWidth - imageWidth) / 2f;
            float top = (canvasHeight - imageHeight) / 2f;
            imageBounds.set(left, top, left + imageWidth, top + imageHeight);
            canvasBounds.set(0, 0, canvasWidth, canvasHeight);
            
            if (cropRect != null) {
                float scaleX = (float) canvasWidth / oldCanvasW;
                float scaleY = (float) canvasHeight / oldCanvasH;
                cropRect.left *= scaleX;
                cropRect.top *= scaleY;
                cropRect.right *= scaleX;
                cropRect.bottom *= scaleY;
            }
            
            requestLayout();
            invalidate();
        }
    }
    
    public RectF getCropRect() {
        return new RectF(cropRect);
    }
    
    public int getCanvasWidth() {
        return canvasWidth;
    }
    
    public int getCanvasHeight() {
        return canvasHeight;
    }
    
    public RectF getCanvasBounds() {
        return new RectF(canvasBounds);
    }
    
    public RectF getImageBounds() {
        return new RectF(imageBounds);
    }
    
    public float getBitmapScale() {
        return displayScale;
    }
    
    public float getDisplayScale() {
        return displayScale;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = canvasWidth > 0 ? canvasWidth : 0;
        int h = canvasHeight > 0 ? canvasHeight : 0;
        setMeasuredDimension(w, h);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        getDisplayMatrix(new Matrix());
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (cropRect == null) return;
        
        if (displayBitmap != null && !displayBitmap.isRecycled()) {
            canvas.save();
            
            Matrix matrix = new Matrix();
            getDisplayMatrix(matrix);
            canvas.concat(matrix);
            
            drawBackground(canvas);
            
            if (displayBitmap == originalBitmap) {
                canvas.drawBitmap(displayBitmap, imageBounds.left, imageBounds.top, imagePaint);
            } else {
                float invScale = 1f / displayBitmapScale;
                canvas.save();
                canvas.scale(invScale, invScale);
                canvas.drawBitmap(displayBitmap, imageBounds.left * displayBitmapScale, 
                                  imageBounds.top * displayBitmapScale, imagePaint);
                canvas.restore();
            }
            
            canvas.restore();
            
            if (containerScale != 1.0f) {
                canvas.save();
                canvas.scale(containerScale, containerScale);
                canvas.drawRect(cropRect, borderPaint);
                float cornerRadius = 12f / containerScale;
                canvas.drawCircle(cropRect.left, cropRect.top, cornerRadius, cornerPaint);
                canvas.drawCircle(cropRect.right, cropRect.top, cornerRadius, cornerPaint);
                canvas.drawCircle(cropRect.left, cropRect.bottom, cornerRadius, cornerPaint);
                canvas.drawCircle(cropRect.right, cropRect.bottom, cornerRadius, cornerPaint);
                canvas.restore();
            } else {
                canvas.drawRect(cropRect, borderPaint);
                canvas.drawCircle(cropRect.left, cropRect.top, 12, cornerPaint);
                canvas.drawCircle(cropRect.right, cropRect.top, 12, cornerPaint);
                canvas.drawCircle(cropRect.left, cropRect.bottom, 12, cornerPaint);
                canvas.drawCircle(cropRect.right, cropRect.bottom, 12, cornerPaint);
            }
        } else {
            if (containerScale != 1.0f) {
                canvas.save();
                canvas.scale(containerScale, containerScale);
                canvas.drawRect(cropRect, borderPaint);
                float cornerRadius = 12f / containerScale;
                canvas.drawCircle(cropRect.left, cropRect.top, cornerRadius, cornerPaint);
                canvas.drawCircle(cropRect.right, cropRect.top, cornerRadius, cornerPaint);
                canvas.drawCircle(cropRect.left, cropRect.bottom, cornerRadius, cornerPaint);
                canvas.drawCircle(cropRect.right, cropRect.bottom, cornerRadius, cornerPaint);
                canvas.restore();
            } else {
                canvas.drawRect(cropRect, borderPaint);
                canvas.drawCircle(cropRect.left, cropRect.top, 12, cornerPaint);
                canvas.drawCircle(cropRect.right, cropRect.top, 12, cornerPaint);
                canvas.drawCircle(cropRect.left, cropRect.bottom, 12, cornerPaint);
                canvas.drawCircle(cropRect.right, cropRect.bottom, 12, cornerPaint);
            }
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (cropRect == null || canvasBounds.isEmpty()) return super.onTouchEvent(event);
        
        if (event.getPointerCount() == 2) {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                initialSpan = getSpan(event);
                return true;
            } else if (action == MotionEvent.ACTION_MOVE) {
                float span = getSpan(event);
                if (initialSpan > 0) {
                    float scale = span / initialSpan;
                    float centerX = cropRect.centerX();
                    float centerY = cropRect.centerY();
                    float newWidth = cropRect.width() * scale;
                    float newHeight = newWidth / coverRatio;
                    
                    newWidth = Math.max(minSize, Math.min(newWidth, canvasBounds.width()));
                    newHeight = newWidth / coverRatio;
                    
                    float newLeft = centerX - newWidth / 2;
                    float newTop = centerY - newHeight / 2;
                    float newRight = centerX + newWidth / 2;
                    float newBottom = centerY + newHeight / 2;
                    
                    if (newLeft >= canvasBounds.left && newRight <= canvasBounds.right &&
                        newTop >= canvasBounds.top && newBottom <= canvasBounds.bottom) {
                        cropRect.set(newLeft, newTop, newRight, newBottom);
                    }
                    
                    initialSpan = span;
                    invalidate();
                }
                return true;
            }
        }
        
        float x = event.getX();
        float y = event.getY();
        
        RectF bounds = canvasBounds;
        if (containerScale != 1.0f) {
            x = x / containerScale;
            y = y / containerScale;
            bounds = new RectF(
                canvasBounds.left,
                canvasBounds.top,
                canvasBounds.right,
                canvasBounds.bottom
            );
        }
        
        if (colorPickMode) {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                if (originalBitmap != null && !originalBitmap.isRecycled()) {
                    float imgX = screenToImageX(x);
                    float imgY = screenToImageY(y);
                    if (imageBounds.contains(imgX, imgY)) {
                        previewColor = getImageColorAt(x, y);
                    } else if (canvasBounds.contains(x, y)) {
                        previewColor = (bgType == 1) ? Color.WHITE : Color.BLACK;
                    } else {
                        previewColor = Color.TRANSPARENT;
                    }
                    lastPickX = x;
                    lastPickY = y;
                    if (backgroundColorChangedListener != null) {
                        backgroundColorChangedListener.onBackgroundColorChanged(previewColor);
                    }
                }
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (canvasBounds.contains(x, y)) {
                    int pickedColor;
                    float imgX = screenToImageX(x);
                    float imgY = screenToImageY(y);
                    if (imageBounds.contains(imgX, imgY)) {
                        int colorAtPoint = getImageColorAt(x, y);
                        pickedColor = colorAtPoint;
                        backgroundColor = colorAtPoint;
                    } else {
                        pickedColor = (bgType == 1) ? Color.WHITE : Color.BLACK;
                    }
                    if (colorPickedListener != null) {
                        colorPickedListener.onColorPicked(pickedColor);
                    }
                }
                if (colorPickEndListener != null) {
                    colorPickEndListener.onColorPickEnd();
                }
                previewColor = 0;
                invalidate();
                return true;
            }
            return true;
        }
        
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
                    
                    if (newLeft >= bounds.left && newRight <= bounds.right) {
                        cropRect.left = newLeft;
                        cropRect.right = newRight;
                    }
                    if (newTop >= bounds.top && newBottom <= bounds.bottom) {
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
        
        float newHeight = newWidth / coverRatio;
        
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
        float dx = Math.abs(x - cropRect.left);
        float dy = Math.abs(y - cropRect.top);
        float dx2 = Math.abs(x - cropRect.right);
        float dy2 = Math.abs(y - cropRect.bottom);
        
        if (dx <= touchArea && dy <= touchArea) {
            return CORNER_TOP_LEFT;
        }
        if (dx2 <= touchArea && dy <= touchArea) {
            return CORNER_TOP_RIGHT;
        }
        if (dx <= touchArea && dy2 <= touchArea) {
            return CORNER_BOTTOM_LEFT;
        }
        if (dx2 <= touchArea && dy2 <= touchArea) {
            return CORNER_BOTTOM_RIGHT;
        }
        if (x > cropRect.left && x < cropRect.right && y > cropRect.top && y < cropRect.bottom) {
            return CORNER_CENTER;
        }
        return -1;
    }
    
    private float getSpan(MotionEvent event) {
        float x0 = event.getX(0);
        float y0 = event.getY(0);
        float x1 = event.getX(1);
        float y1 = event.getY(1);
        return (float) Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
    }
    
    public int getColorAt(float x, float y) {
        return getImageColorAt(x, y);
    }
}