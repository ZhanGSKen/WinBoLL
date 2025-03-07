package cc.winboll.studio.positions.views;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/22 03:32:48
 * @Describe GridMapView
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class GridMapView extends View {
    // 网格参数
    private float gridTotalWidth = 1000f;
    private float gridTotalHeight = 1000f;
    private float gridSpacing = 50f;
    
    // 视图变换参数
    private float offsetX = 0f;
    private float offsetY = 0f;
    private float scaleFactor = 1.0f;
    private final float minScale = 0.5f;
    private final float maxScale = 5.0f;
    
    // 手势检测
    private final ScaleGestureDetector scaleDetector;
    private float lastTouchX;
    private float lastTouchY;
    
    // 图形存储
    private final List<MapShape> shapes = new ArrayList<MapShape>();

    public GridMapView(Context context) {
        this(context, null);
    }

    public GridMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.scale(scaleFactor, scaleFactor);

        drawGrid(canvas);
        drawShapes(canvas);

        canvas.restore();
    }

    private void drawGrid(Canvas canvas) {
        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(1f / scaleFactor);

        // 绘制垂直线
        for (float x = 0; x <= gridTotalWidth; x += gridSpacing) {
            canvas.drawLine(x, 0, x, gridTotalHeight, gridPaint);
        }

        // 绘制水平线
        for (float y = 0; y <= gridTotalHeight; y += gridSpacing) {
            canvas.drawLine(0, y, gridTotalWidth, y, gridPaint);
        }
    }

    private void drawShapes(Canvas canvas) {
        for (MapShape shape : shapes) {
            shape.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);

        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!scaleDetector.isInProgress()) {
                    lastTouchX = x;
                    lastTouchY = y;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (!scaleDetector.isInProgress()) {
                    final float dx = x - lastTouchX;
                    final float dy = y - lastTouchY;

                    offsetX += dx;
                    offsetY += dy;
                    invalidate();

                    lastTouchX = x;
                    lastTouchY = y;
                }
                break;
        }

        return true;
    }

    // 初始化视图显示区域
    public void initViewport(final float centerX, final float centerY, 
                            final float viewWidth, final float viewHeight) {
        post(new Runnable() {
            @Override
            public void run() {
                float viewportWidth = getWidth();
                float viewportHeight = getHeight();

                float widthScale = viewportWidth / viewWidth;
                float heightScale = viewportHeight / viewHeight;
                scaleFactor = Math.min(widthScale, heightScale);

                offsetX = viewportWidth/2 - centerX * scaleFactor;
                offsetY = viewportHeight/2 - centerY * scaleFactor;
                invalidate();
            }
        });
    }

    // 图形绘制方法
    public void drawPoint(float x, float y, int color, float size) {
        shapes.add(new MapShape(MapShape.TYPE_POINT, x, y, color, size));
        invalidate();
    }

    public void drawCircle(float x, float y, float radius, int color, float strokeWidth) {
        MapShape shape = new MapShape(MapShape.TYPE_CIRCLE, x, y, color, radius);
        shape.setStrokeWidth(strokeWidth);
        shapes.add(shape);
        invalidate();
    }

    public void drawLine(float startX, float startY, float endX, float endY,
                        int color, float strokeWidth) {
        MapShape shape = new MapShape(MapShape.TYPE_LINE, startX, startY, endX, endY, color);
        shape.setStrokeWidth(strokeWidth);
        shapes.add(shape);
        invalidate();
    }

    // 网格参数设置
    public void setGridParameters(float totalWidth, float totalHeight, float spacing) {
        gridTotalWidth = totalWidth;
        gridTotalHeight = totalHeight;
        gridSpacing = spacing;
        invalidate();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float newScale = scaleFactor * detector.getScaleFactor();
            newScale = Math.max(minScale, Math.min(newScale, maxScale));

            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();

            offsetX = focusX - ((focusX - offsetX) / scaleFactor * newScale);
            offsetY = focusY - ((focusY - offsetY) / scaleFactor * newScale);

            scaleFactor = newScale;
            invalidate();
            return true;
        }
    }

    private static class MapShape {
        static final int TYPE_POINT = 0;
        static final int TYPE_CIRCLE = 1;
        static final int TYPE_LINE = 2;

        final int type;
        final PointF[] points;
        final int color;
        float radius;
        float strokeWidth = 2f;

        MapShape(int type, float x, float y, int color, float size) {
            this.type = type;
            this.points = new PointF[]{new PointF(x, y)};
            this.color = color;
            this.radius = size;
        }

        MapShape(int type, float x1, float y1, float x2, float y2, int color) {
            this.type = type;
            this.points = new PointF[]{
                    new PointF(x1, y1),
                    new PointF(x2, y2)
            };
            this.color = color;
        }

        MapShape setStrokeWidth(float width) {
            this.strokeWidth = width;
            return this;
        }

        void draw(Canvas canvas) {
            Paint paint = new Paint();
            paint.setColor(color);

            switch (type) {
                case TYPE_POINT:
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(points[0].x, points[0].y, radius, paint);
                    break;

                case TYPE_CIRCLE:
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(strokeWidth);
                    canvas.drawCircle(points[0].x, points[0].y, radius, paint);
                    break;

                case TYPE_LINE:
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(strokeWidth);
                    canvas.drawLine(
                            points[0].x, points[0].y,
                            points[1].x, points[1].y,
                            paint
                    );
                    break;
            }
        }
    }
}
