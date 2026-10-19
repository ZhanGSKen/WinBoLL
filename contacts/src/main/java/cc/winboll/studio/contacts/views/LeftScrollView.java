package cc.winboll.studio.contacts.views;

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

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/03/04 10:51:50
 * @Describe 左滑显示操作按钮的自定义滚动视图，支持编辑、删除、上移、下移功能
 */
public class LeftScrollView extends HorizontalScrollView {
    // ====================== 常量定义区 ======================
    public static final String TAG = "LeftScrollView";

    // ====================== 成员变量区 ======================
    // 布局控件
    private LinearLayout contentLayout;
    private LinearLayout toolLayout;
    private TextView textView;
    private Button editButton;
    private Button deleteButton;
    private Button upButton;
    private Button downButton;
    // 滑动事件相关
    private float mStartX;
    private float mEndX;
    private boolean isScrolling = false;
    private int nScrollAcceptSize;
    // 回调接口
    private OnActionListener onActionListener;

    // ====================== 构造函数区 ======================
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

    // ====================== 初始化方法区 ======================
    private void init() {
        LogUtils.d(TAG, "init: 开始初始化左滑滚动视图");
        // 加载布局
        View viewMain = inflate(getContext(), R.layout.view_left_scroll, null);
        if (viewMain == null) {
            LogUtils.e(TAG, "init: 布局加载失败，无法初始化控件");
            return;
        }

        // 绑定布局控件
        contentLayout = viewMain.findViewById(R.id.content_layout);
        toolLayout = viewMain.findViewById(R.id.action_layout);
        editButton = viewMain.findViewById(R.id.edit_btn);
        deleteButton = viewMain.findViewById(R.id.delete_btn);
        upButton = viewMain.findViewById(R.id.up_btn);
        downButton = viewMain.findViewById(R.id.down_btn);

        // 校验控件是否绑定成功
        if (contentLayout == null || toolLayout == null) {
            LogUtils.e(TAG, "init: 核心布局控件绑定失败");
            return;
        }

        // 添加主布局到当前视图
        addView(viewMain);
        // 设置按钮点击事件
        setButtonClickListener();

        LogUtils.d(TAG, "init: 左滑滚动视图初始化完成");
    }

    /**
     * 设置操作按钮的点击事件
     */
    private void setButtonClickListener() {
        // 编辑按钮
        if (editButton != null) {
            editButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						LogUtils.d(TAG, "onClick: 点击编辑按钮");
						if (onActionListener != null) {
							onActionListener.onEdit();
						}
					}
				});
        }

        // 删除按钮
        if (deleteButton != null) {
            deleteButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						LogUtils.d(TAG, "onClick: 点击删除按钮");
						if (onActionListener != null) {
							onActionListener.onDelete();
						}
					}
				});
        }

        // 上移按钮
        if (upButton != null) {
            upButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						LogUtils.d(TAG, "onClick: 点击上移按钮");
						if (onActionListener != null) {
							onActionListener.onUp();
						}
					}
				});
        }

        // 下移按钮
        if (downButton != null) {
            downButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						LogUtils.d(TAG, "onClick: 点击下移按钮");
						if (onActionListener != null) {
							onActionListener.onDown();
						}
					}
				});
        }
    }

    // ====================== 对外提供的方法区 ======================
    /**
     * 添加内容视图到容器
     * @param viewContent 待添加的内容视图
     */
    public void addContentLayout(View viewContent) {
        if (contentLayout == null) {
            LogUtils.w(TAG, "addContentLayout: 内容布局未初始化，无法添加视图");
            return;
        }
        if (viewContent == null) {
            LogUtils.w(TAG, "addContentLayout: 待添加视图为null");
            return;
        }
        contentLayout.addView(viewContent, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LogUtils.d(TAG, "addContentLayout: 内容视图添加成功");
    }

    /**
     * 设置内容布局的宽度
     * @param contentWidth 目标宽度
     */
    public void setContentWidth(int contentWidth) {
        if (contentLayout == null) {
            LogUtils.w(TAG, "setContentWidth: 内容布局未初始化，无法设置宽度");
            return;
        }
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) contentLayout.getLayoutParams();
        layoutParams.width = contentWidth;
        contentLayout.setLayoutParams(layoutParams);
        LogUtils.d(TAG, "setContentWidth: 内容布局宽度设置为 " + contentWidth);
    }

    /**
     * 设置文本内容（原代码未初始化textView，添加空校验）
     * @param text 待显示的文本
     */
    public void setText(CharSequence text) {
        if (textView == null) {
            LogUtils.w(TAG, "setText: 文本控件未初始化，无法设置文本");
            return;
        }
        textView.setText(text);
        LogUtils.d(TAG, "setText: 文本设置为 " + text);
    }

    /**
     * 设置事件回调监听器
     * @param listener 回调接口实例
     */
    public void setOnActionListener(OnActionListener listener) {
        this.onActionListener = listener;
        LogUtils.d(TAG, "setOnActionListener: 事件监听器已设置");
    }

    // ====================== 滑动事件处理区 ======================
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = event.getX();
                LogUtils.d(TAG, "onTouchEvent: ACTION_DOWN，起始X坐标 = " + mStartX);
                break;
            case MotionEvent.ACTION_MOVE:
                // 可根据需求添加滑动中逻辑
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mEndX = event.getX();
                int scrollX = getScrollX();
                LogUtils.d(TAG, String.format("onTouchEvent: ACTION_UP/CANCEL，起始X=%f 结束X=%f 滚动距离=%d",
											  mStartX, mEndX, scrollX));

                if (scrollX > 0) {
                    handleScrollLogic();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 处理滑动结束后的逻辑，判断滑动方向并执行滚动
     */
    private void handleScrollLogic() {
        float deltaX = Math.abs(mStartX - mEndX);
        // 校验按钮是否存在，避免空指针
        float threshold = editButton != null ? editButton.getWidth() : 50;

        if (mEndX < mStartX) {
            // 向左滑，显示操作按钮
            if (deltaX > threshold) {
                smoothScrollToRight();
            } else {
                smoothScrollToLeft();
            }
        } else {
            // 向右滑，隐藏操作按钮
            if (deltaX > threshold) {
                smoothScrollToLeft();
            } else {
                smoothScrollToRight();
            }
        }
    }

    /**
     * 平滑滚动到右侧（显示操作按钮）
     */
    private void smoothScrollToRight() {
        post(new Runnable() {
				@Override
				public void run() {
					View childView = getChildAt(0);
					if (childView != null) {
						int scrollToX = childView.getWidth() - getWidth();
						int targetX = Math.max(0, scrollToX);
						smoothScrollTo(targetX, 0);
						LogUtils.d(TAG, "smoothScrollToRight: 滚动到右侧，目标X坐标 = " + targetX);
					}
				}
			});
        // 重置坐标
        resetScrollCoordinate();
    }

    /**
     * 平滑滚动到左侧（隐藏操作按钮）
     */
    private void smoothScrollToLeft() {
        post(new Runnable() {
				@Override
				public void run() {
					smoothScrollTo(0, 0);
					LogUtils.d(TAG, "smoothScrollToLeft: 滚动到左侧");
				}
			});
        // 重置坐标
        resetScrollCoordinate();
    }

    /**
     * 重置滑动坐标
     */
    private void resetScrollCoordinate() {
        mStartX = 0;
        mEndX = 0;
    }

    // ====================== 回调接口定义区 ======================
    public interface OnActionListener {
        void onEdit();
        void onDelete();
        void onUp();
        void onDown();
    }
}

