package cc.winboll.studio.mymessagemanager.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class TTSRuleListViewForScrollView extends ListView {
    static int nMaxWidth = 0;

    public TTSRuleListViewForScrollView(Context context) {
        super(context);
    }

    public TTSRuleListViewForScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public TTSRuleListViewForScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setSelection(int position) {
        super.setSelection(position);
    }

    /**
     * 重写onMeasure方法，重新计算高度，达到使ListView适应ScrollView的效果
     *
     * @param widthMeasureSpec  宽度测量规则
     * @param heightMeasureSpec 高度测量规则
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        /*
         // 子控件TextView不换行显示
         ListAdapter listAdapter = this.getAdapter();  
         if (listAdapter == null) { 
         return; 
         }
         int maxWidth = 0;
         for (int i = 0; i < listAdapter.getCount(); i++) { 
         View listItem = listAdapter.getView(i, null, this);
         View cb = listItem.findViewById(R.id.listviewfiledataCheckBox1);
         View iv = listItem.findViewById(R.id.listviewfiledataImageView1);
         View tv = listItem.findViewById(R.id.listviewfiledataTextView1);

         cb.measure(0,0);
         iv.measure(0,0);
         tv.measure(0,0);

         //listItem.measure(0, 0);
         //int width = listItem.getMeasuredWidth();
         int width = cb.getMeasuredWidth() + iv.getMeasuredWidth()+ tv.getMeasuredWidth();
         if(width>maxWidth) maxWidth = width;
         }
         int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
         int newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST);
         super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
         */

        // 子控件TextView换行显示
        //Integer.MAX_VALUE:表示int类型能够表示的最大值，值为2的31次方-1
        //>>2:右移N位相当于除以2的N的幂
        //MeasureSpec.AT_MOST：子布局可以根据自己的大小选择任意大小的模式

        int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);

        //int newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.AT_MOST);

        //int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightMeasureSpec, MeasureSpec.AT_MOST);

        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec);
    }

}
    
    
    

