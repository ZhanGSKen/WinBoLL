package cc.winboll.studio.powerbell.views;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;

public class BatteryDrawable extends Drawable {
    public static final String TAG = BatteryDrawable.class.getSimpleName();

    // 电量颜色画笔
    final Paint mPaint;
    // 电量值
    int mnValue = 1;

    // @int color ： 电量颜色
    //
    public BatteryDrawable(int color) {
        mPaint = new Paint();
        mPaint.setColor(color);
        mPaint.setAlpha(210);
    }

    // 设置电量值
    //
    public void setValue(int value) {
        mnValue = value;
    }

    @Override
    public void draw(Canvas canvas) {
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
        nTop = nHeight - (nHeight * mnValue / 100);
        canvas.drawRect(new Rect(nLeft, nTop, nRight, nBottom), mPaint);
        
        // 绘制耗电电量提醒值电量
        // 意兴阑珊绘图风格
        /*int nTop;
         int nLeft    = 0;
         int nBottom;
         int nRight  = nWidth;

         for (int i = 0; i < mnValue; i ++) {
         nBottom = (nHeight * (100-i)/100) - mnDx;
         nTop = nBottom + mnDx;
         canvas.drawRect(new Rect(nLeft, nTop, nRight, nBottom), mPaint);
         }*/
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        // This method is required
    }

    @Override
    public void setAlpha(int p1) {
        
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

}
