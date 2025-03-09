package cc.winboll.studio.libaes.views;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/16 01:46:30
 * @Describe AOneHundredPercantClickToCommitSeekBar
 */
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;
import cc.winboll.studio.libappbase.LogUtils;

public class AOHPCTCSeekBar extends SeekBar {

    public static final String TAG = "AOHPCTCSeekBar";

    Context mContext;

    int thumbWidth = 1;
    int progressBarWidth = 1;
    // 设置按钮模糊右边边缘像素
    int blurRightDP = 1;

    // 可开始拉动的起始位置(百分比值)
    //static final int ENABLE_POST_PERCENT_X = 20;
    //int seekablePosition;
    // 最小拉动值，滑块拉动值要超过这个值，确定事件才会提交。
    //static final int TO_MIN_VALUE = 15;
    // 外部接口对象，确定事件提交会调用该对象的方法
    OnOHPCListener mOnOHPCListener;
    // 是否从起点拉动的标志
    boolean mIsStartTo = false;
    // 拉动的滑动值
    int mnTo = 0;

    public void setBlurRightDP(int blurRight) {
        this.blurRightDP = blurRight;
    }
    
    public void setOnOHPCListener(OnOHPCListener listener) {
        mOnOHPCListener = listener;
    }

    public interface OnOHPCListener {
        abstract void onOHPCommit();
    } 

    public AOHPCTCSeekBar(Context context) {
        super(context);
        initView(context);
    }

    public AOHPCTCSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);

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
        initView(context);
    }

    void initView(Context context) {
        LogUtils.d(TAG, "initView(...)");
        mContext = context;
//        Drawable thumbDrawable = getThumb();
//        if (thumbDrawable!= null) {
//            int iconWidth = thumbDrawable.getIntrinsicWidth();
//            LogUtils.d(TAG, String.format("iconWidth %d", iconWidth));
//            seekablePosition = iconWidth;
//        }
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
            //int nEnablePostX = ((getRight() - getLeft()) * ENABLE_POST_PERCENT_X / 100) + getLeft();
            //int nEnablePostX = ((getRight() - getLeft()) * seekablePosition / 100) + getLeft();
            /*LogUtils.d(TAG, "event.getX() is " + Float.toString(event.getX()));
            LogUtils.d(TAG, String.format("thumbWidth %d progressBarWidth %d", thumbWidth, progressBarWidth));
            LogUtils.d(TAG, String.format("mIsStartTo %s", mIsStartTo));
            */
            if (thumbWidth + blurRightDP > event.getX() && event.getX() > 0) {
                mIsStartTo = true;
                return true;
                //return super.dispatchTouchEvent(event);
            }
//            if (!mIsStartTo) {
//                resetView();
//                return false;
//            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            //LogUtils.d(TAG, "ACTION_MOVE");
            /*LogUtils.d(TAG, "event.getX() is " + Float.toString(event.getX()));
            LogUtils.d(TAG, String.format("thumbWidth %d progressBarWidth %d", thumbWidth, progressBarWidth));
            LogUtils.d(TAG, String.format("mIsStartTo %s", mIsStartTo));
            */
            if (mIsStartTo) {
                return super.dispatchTouchEvent(event);
            } else {
                return false;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP 
                   || event.getAction() == MotionEvent.ACTION_CANCEL) {
            //LogUtils.d(TAG, Integer.toString(getProgress()));
            // 提交100%确定事件
//            if (getProgress() == progressBarWidth) {
//                //((getProgress() == 100) && (mnTo > TO_MIN_VALUE)) {
//                //LogUtils.d(TAG, "Commit mnTo is " + Integer.toString(mnTo));
//                mOnOHPCListener.onOHPCommit();
//                resetView();
//                //return true;
//            } else {
//                resetView();
//                mIsStartTo = false;
//            }
//
              if (getProgress() == progressBarWidth) {
                  mOnOHPCListener.onOHPCommit();
              }
              mIsStartTo = false;
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        LogUtils.d(TAG, String.format("width %d height %d", width, height));

        // 使用width和height进行后续操作

        // 获取SeekBar的图标
        Drawable thumbDrawable = getThumb();
        if (thumbDrawable != null) {
            // 获取图标宽度
            thumbWidth = thumbDrawable.getIntrinsicWidth();
            // 获取进度条宽度
            progressBarWidth = width;//getWidth();// - getPaddingLeft() - getPaddingRight();
            // 计算百分比
            //float percentage = (float) thumbWidth / progressBarWidth * 100;
            LogUtils.d(TAG, String.format("thumbWidth %d progressBarWidth %d", thumbWidth, progressBarWidth));
            //LogUtils.d(TAG, String.format("Thumb width / ProgressBar width percentage: %f", percentage));

            //seekablePosition = (int)percentage;
            setThumbOffset(0);
            setMax(progressBarWidth);
        }
    }
}
