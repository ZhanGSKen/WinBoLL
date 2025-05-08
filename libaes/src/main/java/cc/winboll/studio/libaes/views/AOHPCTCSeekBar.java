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

    volatile int thumbWidth = 1;
    volatile int progressBarWidth = 1;
    // 设置按钮模糊右边边缘像素
    volatile int blurRightDP = 1;
    // 是否从起点拉动的标志
    volatile boolean isStartSeek = false;

    // 外部接口对象，确定事件提交会调用该对象的方法
    OnOHPCListener mOnOHPCListener;


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
    }

    public AOHPCTCSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    void initView(Context context) {
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (thumbWidth + blurRightDP > event.getX() && event.getX() > 0) {
                getParent().requestDisallowInterceptTouchEvent(true);
                isStartSeek = true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (isStartSeek) {
                super.dispatchTouchEvent(event);
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP 
                   || event.getAction() == MotionEvent.ACTION_CANCEL) {
            getParent().requestDisallowInterceptTouchEvent(false);
            if (getProgress() == progressBarWidth) {
                mOnOHPCListener.onOHPCommit();
            }
            // 重置控件状态
            setProgress(0);
            isStartSeek = false;
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        //int height = MeasureSpec.getSize(heightMeasureSpec);
        //LogUtils.d(TAG, String.format("width %d height %d", width, height));

        // 获取SeekBar的图标宽度
        Drawable thumbDrawable = getThumb();
        if (thumbDrawable != null) {
            // 获取图标宽度
            thumbWidth = thumbDrawable.getIntrinsicWidth();
        }

        // 获取进度条宽度
        progressBarWidth = width;

        //LogUtils.d(TAG, String.format("thumbWidth %d progressBarWidth %d", thumbWidth, progressBarWidth));

        // 设置图标位置
        setThumbOffset(0);
        // 设置进度条刻度
        setMax(progressBarWidth);
    }
}
