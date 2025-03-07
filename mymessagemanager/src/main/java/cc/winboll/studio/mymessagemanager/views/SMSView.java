package cc.winboll.studio.mymessagemanager.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import androidx.cardview.widget.CardView;
import cc.winboll.studio.mymessagemanager.R;

public class SMSView extends CardView {

    public static final String TAG = "SMSView";

    Context mContext;
    int colorInbox;
    int colorSend;
    int colorItem;
    public enum SMSType { INBOX, SEND }
    SMSType mSMSType = SMSType.INBOX;

    public void setSMSType(SMSType smsType) {
        this.mSMSType = smsType;
        updateViewBackgroundColor();
    }

    public SMSType getCardType() {
        return mSMSType;
    }

    void updateViewBackgroundColor() {
        if (mSMSType == SMSType.INBOX) {
            setCardBackgroundColor(colorInbox);
        } else if (mSMSType == SMSType.SEND) {
            setCardBackgroundColor(colorSend);
        }
    }

    public SMSView(Context context) {
        super(context);
        mContext = context;
    }

    public SMSView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.SMSView, 0, 0);
        String szSMSType = a.getString(R.styleable.SMSView_attrSMSType);
        if((szSMSType == null)||szSMSType.equals("")) {
            mSMSType = SMSType.SEND;
        } else {
            mSMSType = SMSType.valueOf(szSMSType);
        }
        colorInbox = a.getColor(R.styleable.SMSView_attrSMSViewInboxColor, 0);
        colorSend = a.getColor(R.styleable.SMSView_attrSMSViewSendColor, 0);
        a.recycle();
    }

    public SMSView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 子控件TextView换行显示
        //Integer.MAX_VALUE:表示int类型能够表示的最大值，值为2的31次方-1
        //>>2:右移N位相当于除以2的N的幂
        //MeasureSpec.AT_MOST：子布局可以根据自己的大小选择任意大小的模式
        int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec);
    }
}
