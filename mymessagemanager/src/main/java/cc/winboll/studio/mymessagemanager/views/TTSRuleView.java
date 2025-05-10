package cc.winboll.studio.mymessagemanager.views;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2023/07/24 15:08:31
 * @Describe TTS语音规则视图
 */
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.cardview.widget.CardView;
import cc.winboll.studio.mymessagemanager.R;

public class TTSRuleView extends CardView {
    
    public static final String TAG = "TTSRuleView";
    
    Context mContext;

    public TTSRuleView(Context context) {
        super(context);
        mContext = context;
    }

    public TTSRuleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.TTSRuleView, 0, 0);
        int colorBackground = a.getColor(R.styleable.TTSRuleView_attrTTSRuleViewBackgroundColor, 0);
        a.recycle();
        setCardBackgroundColor(colorBackground);
    }

    public TTSRuleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Integer.MAX_VALUE:表示int类型能够表示的最大值，值为2的31次方-1
        //>>2:右移N位相当于除以2的N的幂
        //MeasureSpec.AT_MOST：子布局可以根据自己的大小选择任意大小的模式
        int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec);
    }
    
}
