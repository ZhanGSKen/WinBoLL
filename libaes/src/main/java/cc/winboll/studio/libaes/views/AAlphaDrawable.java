package cc.winboll.studio.libaes.views;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Checkerboard drawable used behind translucent colors.
 * Reimplementation of the transparency pattern required by AColorPickerView.
 */
class AAlphaDrawable extends Drawable {

    private final int rectangleSize;
    private final Paint paint;
    private final Paint paintWhite;
    private final Paint paintGray;
    private int numRectanglesHorizontal;
    private int numRectanglesVertical;
    private Bitmap bitmap;

    AAlphaDrawable(int rectangleSize) {
        this.rectangleSize = rectangleSize;
        paint = new Paint();
        paintWhite = new Paint();
        paintGray = new Paint();
        paintWhite.setColor(Color.WHITE);
        paintGray.setColor(0xFFCBCBCB);
    }

    @Override
    public void draw(Canvas canvas) {
        if (bitmap != null && !bitmap.isRecycled()) {
            canvas.drawBitmap(bitmap, null, getBounds(), paint);
        }
    }

    /**
     * Drawable.getOpacity() is deprecated since API 29; the override is kept
     * because the platform still calls it for backward compatibility.
     */
    @Override
    @SuppressWarnings("deprecation")
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
        throw new UnsupportedOperationException("Alpha is not supported by this drawable.");
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        throw new UnsupportedOperationException("ColorFilter is not supported by this drawable.");
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        int height = bounds.height();
        int width = bounds.width();
        numRectanglesHorizontal = (int) Math.ceil(width / (double) rectangleSize);
        numRectanglesVertical = (int) Math.ceil(height / (double) rectangleSize);
        generatePatternBitmap();
    }

    private void generatePatternBitmap() {
        Rect bounds = getBounds();
        int width = bounds.width();
        int height = bounds.height();
        if (width <= 0 || height <= 0) {
            return;
        }
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawRect(0, 0, width, height, paintWhite);
        for (int i = 0; i < numRectanglesHorizontal; i++) {
            for (int j = 0; j < numRectanglesVertical; j++) {
                if (((i + j) & 1) == 1) {
                    canvas.drawRect(i * rectangleSize, j * rectangleSize,
                            (i + 1) * rectangleSize, (j + 1) * rectangleSize, paintGray);
                }
            }
        }
    }
}
