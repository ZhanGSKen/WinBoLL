package cc.winboll.studio.powerbell.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

public class VerticalSeekBar extends SeekBar {
    public static final String TAG = VerticalSeekBar.class.getSimpleName();

    public volatile int _mnProgress = -1;
    
    public VerticalSeekBar(Context context) {
        super(context);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 去除冗余的水平阴影
        setBackgroundDrawable(null);
        
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }




    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    protected void onDraw(Canvas c) {
        // 0--------100,顺时针旋转,小在上
        //       c.rotate(+90);
        //       c.translate(0, -getWidth());

        // 0--------100,逆时针旋转,小在下
        c.rotate(-90);
        c.translate(-getHeight(), 0);

        super.onDraw(c);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 调用基类的处理函数
        // 该方法可以使得
        // SeekBar.OnSeekBarChangeListener
        // 的 onStopTrackingTouch 和 onStartTrackingTouch 等函数有效。
        boolean handled = super.onTouchEvent(event);

        if (handled) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    // 0--------100,顺时针旋转,小在上
                    //_mnProgress = (int)(getMax() * event.getY() / getHeight());
                    // // 0--------100,逆时针旋转,小在下
                    _mnProgress = getMax() - (int) (getMax() * event.getY() / getHeight());
                    _mnProgress = _mnProgress > 100 ? 100 : _mnProgress ;
                    //LogUtils.d(TAG, "_mnProgress is " + Integer.toString(_mnProgress));
                    setProgress(_mnProgress);
                    //onSizeChanged(getWidth(), getHeight(), 0, 0);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    break;
                default :
                    //LogUtils.d(TAG, "event.getAction() is " + event.getAction());
                    break;
            }
        }

        return handled;
    }

    // 解决调用setProgress（）方法时滑块不跟随的bug
    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
        onSizeChanged(getWidth(), getHeight(), 0, 0);
    }
}
