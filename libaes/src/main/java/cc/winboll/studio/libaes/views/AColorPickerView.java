package cc.winboll.studio.libaes.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * HSV color picker: saturation/value square on the top left, vertical hue bar
 * on the right and horizontal alpha bar at the bottom.
 * Layout unit is 1/20 of the smaller side, the whole view is a square.
 */
public class AColorPickerView extends View {

    public static final String TAG = "AColorPickerView";

    /** Radius of the selection markers in dp-independent pixels. */
    private static final int MARKER_STROKE = 3;
    private static final int HUE_STOPS = 7;

    private static final int[] HUE_COLORS = new int[]{
            0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF,
            0xFF0000FF, 0xFFFF00FF, 0xFFFF0000
    };

    private final Paint huePaint;
    private final Paint satPaint;
    private final Paint alphaPaint;
    private final Paint valPaint;
    private final Paint labelPaint;

    private final Rect hueRect;
    private final Rect satRect;
    private final Rect alphaRect;

    private final AAlphaDrawable alpha;

    private Shader satShader;
    private Shader valShader;

    private float h;
    private float s;
    private float v;
    private int a;

    private float huey;
    private float satx;
    private float saty;
    private float alphax;

    private AColorChangedListener listener;

    private boolean touchHue;
    private boolean touchSat;
    private boolean touchAlpha;

    public AColorPickerView(Context context) {
        this(context, null);
    }

    public AColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        huePaint = new Paint();
        satPaint = new Paint();
        alphaPaint = new Paint();
        valPaint = new Paint();
        alpha = new AAlphaDrawable(8);
        satRect = new Rect();
        hueRect = new Rect();
        alphaRect = new Rect();
        listener = null;
        valPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        labelPaint = new Paint();
        labelPaint.setAntiAlias(true);
        labelPaint.setStrokeWidth(MARKER_STROKE);
        labelPaint.setStyle(Paint.Style.STROKE);
        h = 0;
        s = 0;
        v = 1;
        a = 255;
        huey = 0;
        satx = 0;
        saty = 0;
        alphax = 0;
    }

    public void setOnColorChangedListener(AColorChangedListener listener) {
        this.listener = listener;
    }

    public void setColor(int color) {
        setColor(Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color));
    }

    public void setColor(int r, int g, int b) {
        setColor(255, r, g, b);
    }

    public void setColor(int a, int r, int g, int b) {
        this.a = a;
        float[] hsv = new float[3];
        Color.RGBToHSV(r, g, b, hsv);
        h = hsv[0];
        s = hsv[1];
        v = hsv[2];
        updateMarks();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int unit = Math.min(w, h) / 20;
        int satEnd = unit * 17;
        int hueLeft = unit * 18;
        int hueRight = unit * 19;
        int alphaTop = unit * 18;
        int alphaBottom = unit * 19;
        satRect.set(unit, unit, satEnd, satEnd);
        hueRect.set(hueLeft, unit, hueRight, satEnd);
        alphaRect.set(unit, alphaTop, hueRight, alphaBottom);
        alpha.setBounds(alphaRect);
        setupHue();
        setupVal();
        updateMarks();
    }

    private void updateMarks() {
        Rect rect = hueRect;
        if (rect.height() == 0) {
            return;
        }
        huey = rect.top + h / 360f * rect.height();
        rect = alphaRect;
        alphax = rect.right - (a / 255f) * rect.width();
        rect = satRect;
        satx = rect.left + s * rect.width();
        saty = rect.bottom - v * rect.height();
        refresh(true, false);
    }

    private void setupVal() {
        Rect rect = satRect;
        valShader = new LinearGradient(0, 0, 0, rect.height(),
                Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP);
        satShader = new LinearGradient(0, 0, rect.width(), 0,
                Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP);
        valPaint.setShader(valShader);
    }

    private void setupHue() {
        Rect rect = hueRect;
        LinearGradient gradient = new LinearGradient(rect.left, rect.top,
                rect.left, rect.bottom, HUE_COLORS, null, Shader.TileMode.CLAMP);
        huePaint.setShader(gradient);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingBottom() - getPaddingTop();
        int side = Math.min(width, height);
        setMeasuredDimension(side, side);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect rect = satRect;
        Paint paint = labelPaint;
        canvas.drawRect(rect, satPaint);
        canvas.drawRect(rect, valPaint);
        paint.setStrokeWidth(1f);
        canvas.drawRect(rect, paint);
        int radius = MARKER_STROKE;
        paint.setStrokeWidth(MARKER_STROKE);
        float[] marker = new float[]{h, 0.5f, 1f - v};
        paint.setColor(Color.HSVToColor(marker));
        canvas.drawCircle(satx, saty, radius * 2, paint);
        paint.setColor(Color.BLACK);
        rect = hueRect;
        canvas.drawRect(rect, huePaint);
        canvas.drawRect(rect.left - radius, huey - radius,
                rect.right + radius, huey + radius, paint);
        alpha.draw(canvas);
        rect = alphaRect;
        canvas.drawRect(rect, alphaPaint);
        canvas.drawRect(alphax - radius, rect.top - radius,
                alphax + radius, rect.bottom + radius, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (listener != null) {
                    listener.beforeColorChanged();
                }
                if (hueRect.contains(x, y)) {
                    processHue(y);
                } else if (satRect.contains(x, y)) {
                    processSat(x, y);
                } else if (alphaRect.contains(x, y)) {
                    processAlpha(x);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (touchHue) {
                    processHue(y);
                } else if (touchSat) {
                    processSat(x, y);
                } else if (touchAlpha) {
                    processAlpha(x);
                }
                break;
            case MotionEvent.ACTION_UP:
                touchHue = false;
                touchSat = false;
                touchAlpha = false;
                if (listener != null) {
                    listener.afterColorChanged();
                }
                return true;
            default:
                break;
        }
        refresh(true, true);
        invalidate();
        return true;
    }

    private void processAlpha(float x) {
        touchAlpha = true;
        Rect rect = alphaRect;
        if (x < rect.left) {
            x = rect.left;
        }
        if (x > rect.right) {
            x = rect.right;
        }
        alphax = x;
        x = x - rect.left;
        a = (int) ((1f - x / rect.width()) * 255f);
    }

    private void processSat(float x, float y) {
        touchSat = true;
        Rect rect = satRect;
        if (y < rect.top) {
            y = rect.top;
        }
        if (y > rect.bottom) {
            y = rect.bottom;
        }
        if (x < rect.left) {
            x = rect.left;
        }
        if (x > rect.right) {
            x = rect.right;
        }
        satx = x;
        saty = y;
        x = x - rect.left;
        y = y - rect.top;
        s = x / rect.width();
        v = 1f - y / rect.height();
    }

    private void processHue(float y) {
        touchHue = true;
        Rect rect = hueRect;
        if (y < rect.top) {
            y = rect.top;
        }
        if (y > rect.bottom) {
            y = rect.bottom;
        }
        huey = y;
        y = y - rect.top;
        h = y / rect.height() * 360f;
    }

    private void refresh(boolean force, boolean notify) {
        float[] hsv = new float[]{h, 1f, 1f};
        if (force || touchHue) {
            int color = Color.HSVToColor(hsv);
            Rect rect = satRect;
            satShader = new LinearGradient(0, 0, rect.width(), 0,
                    Color.WHITE, color, Shader.TileMode.CLAMP);
            satPaint.setShader(satShader);
        }
        hsv[1] = s;
        hsv[2] = v;
        if (force || touchHue || touchSat) {
            int color = Color.HSVToColor(hsv);
            int rgb = color & 0xFFFFFF;
            Rect rect = alphaRect;
            Shader alphaShader = new LinearGradient(rect.left, rect.top,
                    rect.right, rect.top, color, rgb, Shader.TileMode.CLAMP);
            alphaPaint.setShader(alphaShader);
        }
        if (notify && listener != null) {
            int color = Color.HSVToColor(a, hsv);
            listener.onColorChanged(color);
        }
    }
}
