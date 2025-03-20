package cc.winboll.studio.libappbase.views;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/12 12:29:01
 * @Describe 水平布局的 ListView
 */
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;
import cc.winboll.studio.libappbase.LogUtils;

public class HorizontalListView extends ListView {

    public static final String TAG = "HorizontalListView";
    int verticalOffset = 0;

    public HorizontalListView(Context context) {
        super(context);
    }

    public HorizontalListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setVerticalOffset(int verticalOffset) {
        this.verticalOffset = verticalOffset;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int childCount = getChildCount();
        int left = getPaddingLeft();
        int viewHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        LogUtils.d(TAG, String.format("HorizontalListView的高度 %d", viewHeight));
        
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            // 计算每个子视图的宽度和高度
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            //LogUtils.d(TAG, String.format("child : width %d , height %d", width, height));
            // 设置子视图的位置，实现水平布局

            child.layout(left, verticalOffset, left + width, verticalOffset + height);
            left += width;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        //super.onMeasure(widthMeasureSpec, newHeightMeasureSpec);
        int newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        //LogUtils.d(TAG, String.format("newWidthMeasureSpec %d, newHeightMeasureSpec %d", newWidthMeasureSpec, newHeightMeasureSpec));
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
    }
}

