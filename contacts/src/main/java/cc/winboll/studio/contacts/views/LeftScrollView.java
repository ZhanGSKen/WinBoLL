package cc.winboll.studio.contacts.views;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/04 10:51:50
 * @Describe CustomHorizontalScrollView
 */
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cc.winboll.studio.contacts.R;
import cc.winboll.studio.libappbase.LogUtils;
import android.util.TypedValue;

public class LeftScrollView extends HorizontalScrollView {

    public static final String TAG = "LeftScrollView";

    private LinearLayout contentLayout;
    private LinearLayout toolLayout;
    private TextView textView;
    private Button editButton;
    private Button deleteButton;
    private float mLastX;
    private boolean isScrolling = false;

    public LeftScrollView(Context context) {
        super(context);
        init();
    }

    public LeftScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LeftScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void addContentLayout(TextView textView) {
        contentLayout.addView(textView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    }

    public void setContentWidth(int contentWidth) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) contentLayout.getLayoutParams();
        layoutParams.width = contentWidth;
        contentLayout.setLayoutParams(layoutParams);
    }

    private void init() {
        View viewMain = inflate(getContext(), R.layout.view_left_scroll, null);

        // 创建内容布局
        contentLayout = viewMain.findViewById(R.id.content_layout);
        toolLayout = viewMain.findViewById(R.id.action_layout);

        //LogUtils.d(TAG, String.format("getWidth() %d", getWidth()));

        addView(viewMain);

        // 创建编辑按钮
        editButton = viewMain.findViewById(R.id.edit_btn);
        // 创建删除按钮
        deleteButton = viewMain.findViewById(R.id.delete_btn);

        // 编辑按钮点击事件
        editButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onActionListener != null) {
                        onActionListener.onEdit();
                    }
                }
            });

        // 删除按钮点击事件
        deleteButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onActionListener != null) {
                        onActionListener.onDelete();
                    }
                }
            });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                LogUtils.d(TAG, "ACTION_DOWN");
                mLastX = event.getX();
                isScrolling = false;
                break;
            case MotionEvent.ACTION_MOVE:
                //LogUtils.d(TAG, "ACTION_MOVE");
                float currentX = event.getX();
                float deltaX = mLastX - currentX;
                mLastX = currentX;
                if (Math.abs(deltaX) > 0) {
                    isScrolling = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                LogUtils.d(TAG, "ACTION_UP");
                if (isScrolling) {
                    LogUtils.d(TAG, String.format("isScrolling \ngetScrollX() %d\neditButton.getWidth() %d", getScrollX(), editButton.getWidth()));
                    int scrollX = getScrollX();
                    if (scrollX > editButton.getWidth()) {
                        // 获取HorizontalScrollView的子视图
                        View childView = getChildAt(0);
                        if (childView != null) {
                            // 计算需要滑动到最右边的距离
                            int scrollToX = childView.getWidth() - getWidth();
                            // 确保滑动距离不小于0
                            final int scrollToX2 = Math.max(0, scrollToX);
                            // 平滑滑动到最右边
                            post(new Runnable() {
                                    @Override
                                    public void run() {
                                        smoothScrollTo(scrollToX2, 0);
                                        LogUtils.d(TAG, "smoothScrollTo(0, 0);");
                                    }
                                });
                            LogUtils.d(TAG, "smoothScrollTo(scrollToX, 0);");
                        }
                    } else {
                        // 恢复原状
                        // 在手指抬起时，使用 post 方法调用 smoothScrollTo(0, 0)
                        post(new Runnable() {
                                @Override
                                public void run() {
                                    smoothScrollTo(0, 0);
                                    LogUtils.d(TAG, "smoothScrollTo(0, 0);");
                                }
                            });
                        //toolLayout.setTranslationX(0);
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    // 设置文本内容
    public void setText(CharSequence text) {
        textView.setText(text);
    }

    // 定义回调接口
    public interface OnActionListener {
        void onEdit();
        void onDelete();
    }

    private OnActionListener onActionListener;

    public void setOnActionListener(OnActionListener listener) {
        this.onActionListener = listener;
    }
}

