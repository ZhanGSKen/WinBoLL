package cc.winboll.studio.libaes.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * Preview strip: left half keeps the initial color, right half shows the
 * current color, drawn over a checkerboard so alpha is visible.
 */
public class AColorPreviewView extends View {

    public static final String TAG = "AColorPreviewView";

    private final Paint paint;
    private final AAlphaDrawable alphaDrawable;
    private final Rect rect;
    private int color;

    public AColorPreviewView(Context context) {
        this(context, null);
    }

    public AColorPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        color = 0;
        alphaDrawable = new AAlphaDrawable(8);
        paint = new Paint();
        rect = new Rect();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int horizontalInset = w / 10;
        int verticalInset = h / 20;
        rect.set(horizontalInset, verticalInset, w - horizontalInset, h - verticalInset);
        alphaDrawable.setBounds(rect);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        alphaDrawable.draw(canvas);
        canvas.drawRect(rect, paint);
    }

    /** Sets the current color, gradient runs from the initial color to this one. */
    public void setColor(int color) {
        int middle = getWidth() / 2;
        Shader shader = new LinearGradient(middle - 1, 0, middle + 1, 0,
                this.color, color, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        invalidate();
    }

    /** Sets the initial color shown on the left half. */
    public void setInitColor(int color) {
        this.color = color;
        int middle = getWidth() / 2;
        Shader shader = new LinearGradient(middle - 1, 0, middle + 1, 0,
                color, color, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        invalidate();
    }
}
