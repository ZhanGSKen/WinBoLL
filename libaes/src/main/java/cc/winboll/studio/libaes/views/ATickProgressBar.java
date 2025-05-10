package cc.winboll.studio.libaes.views;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/07/16 01:56:38
 * @Describe ATickProgressBar
 */
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class ATickProgressBar extends ProgressBar {

    public static final String TAG = "ATickProgressBar";

    int mnStepDistantce = 100 / 10;
    int mnProgress = 0;

    public ATickProgressBar(Context context) {
        super(context);

    }

    public ATickProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setProgress(50);
    }

    public int stepOnTick(int nStepDistantce) {
        if (mnProgress < 100) {
            int nProgressOld = mnProgress;
            mnProgress += nStepDistantce;
            new Handler().postDelayed(new Runnable(){

                    @Override
                    public void run() {
                        ;
                    }
                }, 1000);
            return nProgressOld;
        } else {
            return mnProgress;
        }
    }

    /*@Override
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
     super.onMeasure(widthMeasureSpec, heightMeasureSpec);
     int nWidthSize = MeasureSpec.getSize(widthMeasureSpec);
     int nHeightSize = MeasureSpec.getSize(heightMeasureSpec);

     setMeasuredDimension(nWidthSize, nHeightSize);
     }*/
}
