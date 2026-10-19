package cc.winboll.studio.mymessagemanager.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class SMSAcceptRuleListViewForScrollView extends ListView {

    public SMSAcceptRuleListViewForScrollView(Context context) {
        super(context);
    }

    public SMSAcceptRuleListViewForScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SMSAcceptRuleListViewForScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 重写onMeasure方法，重新计算高度，达到使ListView适应ScrollView的效果
     *
     * @param widthMeasureSpec  宽度测量规则
     * @param heightMeasureSpec 高度测量规则
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec);
    }

}
