package cc.winboll.studio.libaes.views;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/16 01:46:30
 * @Describe AOneHundredPercantClickToCommitSeekBar
 */
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

public class AOHPCTCSeekBar extends SeekBar {

    public static final String TAG = "AOHPCTCSeekBar";

    // 可开始拉动的起始位置(百分比值)
    static final int ENABLE_POST_PERCENT_X = 20;
    // 最小拉动值，滑块拉动值要超过这个值，确定事件才会提交。
    static final int TO_MIN_VALUE = 15;
    // 外部接口对象，确定事件提交会调用该对象的方法
    OnOHPCListener mOnOHPCListener;
    // 是否从起点拉动的标志
    boolean mIsStartTo = false;
    // 拉动的滑动值
    int mnTo = 0;

    public void setOnOHPCListener(OnOHPCListener listener) {
        mOnOHPCListener = listener;
    }

    public interface OnOHPCListener {
        abstract void onOHPCommit();
    } 

    public AOHPCTCSeekBar(Context context) {
        super(context);
    }

    public AOHPCTCSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        //LogUtils.d(TAG, "AOHPCTCSeekBar(...)");

        // 获得TypedArray
        //TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AToolbar);
        // 获得attrs.xml里面的属性值,格式为:名称_属性名,后面是默认值
        //int colorBackgroud = a.getColor(R.styleable.ACard_backgroudColor, context.getColor(R.color.colorACardBackgroung));
        //int centerColor = a.getColor(R.styleable.AToolbar_centerColor, context.getColor(R.color.colorAToolbarCenterColor));
        //int endColor = a.getColor(R.styleable.AToolbar_endColor, context.getColor(R.color.colorAToolbarEndColor));
        //float tSize = a.getDimension(R.styleable.CustomView_tSize, 35);
        //p.setColor(tColor);
        //p.setTextSize(tSize);
        //Drawable drawable = context.getDrawable(R.drawable.frame_atoolbar);

        //setBackground(context.getDrawable(R.drawable.acard_frame_main));

        // 返回一个绑定资源结束的信号给资源
        //a.recycle();
    }

    public AOHPCTCSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //LogUtils.d(TAG, "ACTION_DOWN");
            // 有效的拖动起始位置(ENABLE_POST_PERCENT_X)%
            int nEnablePostX = ((getRight() - getLeft()) * ENABLE_POST_PERCENT_X / 100) + getLeft();

            if ((getLeft() < event.getX())
                && (event.getX() < nEnablePostX)) {
                //LogUtils.d(TAG, "event.getX() is " + Float.toString(event.getX()));
                mIsStartTo = true;
                return super.dispatchTouchEvent(event);
            }
            if (!mIsStartTo) {
                resetView();
                return false;
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            //LogUtils.d(TAG, "ACTION_MOVE");
            if (mIsStartTo) {
                mnTo++;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            //LogUtils.d(TAG, Integer.toString(getProgress()));
            // 提交100%确定事件
            if ((getProgress() == 100) && (mnTo > TO_MIN_VALUE)) {
                //LogUtils.d(TAG, "Commit mnTo is " + Integer.toString(mnTo));
                mOnOHPCListener.onOHPCommit();
                //resetView();
                //return true;
            }
            resetView();
            return false;
        }
        //LogUtils.d(TAG, "dispatchTouchEvent End");
        return super.dispatchTouchEvent(event);
    }

    // 重置控件状态
    //
    void resetView() {
        setProgress(0);
        mnTo = 0;
        mIsStartTo = false;
    }
}
