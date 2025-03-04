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

public class LeftScrollView extends HorizontalScrollView {

    public static final String TAG = "LeftScrollView";

    private LinearLayout contentLayout;
    private LinearLayout toolLayout;
    private TextView textView;
    private Button editButton;
    private Button deleteButton;
    private float startX;
    private boolean isDragging = false;

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

    private void init() {
        View viewMain = inflate(getContext(), R.layout.view_left_scroll, null);

        // 创建内容布局
        contentLayout = viewMain.findViewById(R.id.content_layout);
        toolLayout = viewMain.findViewById(R.id.action_layout);
        addView(viewMain);

        // 动态设置content_layout的宽度为scrollView的宽度
//        post(new Runnable() {
//                @Override
//                public void run() {
//                    int scrollViewWidth = getWidth();
//                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) contentLayout.getLayoutParams();
//                    layoutParams.width = scrollViewWidth;
//                    contentLayout.setLayoutParams(layoutParams);
//                }
//            });

        // 创建编辑按钮
        editButton = viewMain.findViewById(R.id.edit_btn);
        // 创建删除按钮
        deleteButton = viewMain.findViewById(R.id.delete_btn);

        // 设置触摸事件监听器
//        setOnTouchListener(new OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    switch (event.getAction()) {
//                        case MotionEvent.ACTION_DOWN:
//                            startX = event.getX();
//                            isDragging = true;
//                            break;
//                        case MotionEvent.ACTION_MOVE:
//                            if (isDragging) {
//                                float deltaX = startX - event.getX();
//                                if (deltaX > 0) { // 左滑
//                                    float translationX = Math.max(-(editButton.getWidth() + deleteButton.getWidth()), -deltaX);
//                                    toolLayout.setTranslationX(translationX);
//                                    scrollTo((int) translationX, 0);
//                                }
//                            }
//                            break;
//                        case MotionEvent.ACTION_UP:
//                        case MotionEvent.ACTION_CANCEL:
//                            isDragging = false;
//                            if (getScrollX() <= -(editButton.getWidth())) {
//                                // 编辑按钮完全显示，保持按钮显示
//                                smoothScrollTo(-(editButton.getWidth() + deleteButton.getWidth()), 0);
//                            } else {
//                                // 恢复原状
//                                smoothScrollTo(0, 0);
//                                toolLayout.setTranslationX(0);
//                            }
//                            break;
//                    }
//                    return true;
//                }
//            });

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

