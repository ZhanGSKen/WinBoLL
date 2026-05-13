package cc.winboll.studio.mymessagemanager.views;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/08/23 00:39
 * @Describe 多级拉动响应自定义控件
 */
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;

public class BottomPositionFixedScrollView extends ScrollView {
	public static final String TAG = "BottomPositionFixedScrollView";
	// 记录底部对应的内容绝对位置（即底部位置在内容中的y坐标，该位置需始终保持在视图底部）
	private int mBottomContentY = 0;
	// 标记是否是首次布局（避免初始加载误触发）
	private boolean isFirstLayout = true;

	public BottomPositionFixedScrollView(Context context) {
		super(context);
		init();
	}

	public BottomPositionFixedScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BottomPositionFixedScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		// 监听布局变化（高度改变时触发）
		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					if (isFirstLayout) {
						isFirstLayout = false;
						return;
					}
					// 布局变化后，恢复底部位置
					restoreBottomPosition();
				}
			});
	}

	/**
	 * 重写滚动事件，记录“底部对应的内容绝对位置”
	 * （即当前视图底部边缘对应的内容y坐标，该坐标需始终保持在视图底部）
	 */
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (getChildCount() == 0) {
			mBottomContentY = 0;
			return;
		}

		// 内容总高度
		int contentHeight = getChildAt(0).getMeasuredHeight();
		// 视图可视高度（自身高度）
		int scrollViewHeight = getMeasuredHeight();
		// 当前视图底部边缘对应的内容y坐标 = 顶部滚动距离(t) + 可视高度
		// （该坐标就是“底部内容的绝对位置”，需始终保持在视图底部）
		mBottomContentY = t + scrollViewHeight;

		// 避免超过内容总高度（比如内容不足一屏时，底部最多到内容底部）
		if (mBottomContentY > contentHeight) {
			mBottomContentY = contentHeight;
		}
	}

	/**
	 * 恢复底部位置：让原记录的“底部内容绝对位置”仍保持在视图底部
	 */
	private void restoreBottomPosition() {
		if (getChildCount() == 0) {
			return;
		}

		// 新的内容总高度
		int newContentHeight = getChildAt(0).getMeasuredHeight();
		// 新的视图可视高度
		int newScrollViewHeight = getMeasuredHeight();

		// 目标：让原mBottomContentY（底部内容绝对位置）仍位于视图底部
		// 此时需要的顶部滚动距离 = mBottomContentY - 新的可视高度
		int targetScrollY = mBottomContentY - newScrollViewHeight;

		// 边界修正：
		// 1. 不能小于0（避免滚动到负数位置）
		// 2. 不能大于“最大可滚动距离”（内容高度 - 可视高度，避免超出内容范围）
		int maxScrollY = Math.max(newContentHeight - newScrollViewHeight, 0);
		targetScrollY = Math.max(targetScrollY, 0);
		targetScrollY = Math.min(targetScrollY, maxScrollY);

		// 滚动到目标位置，保持底部内容位置不变
		smoothScrollTo(0, targetScrollY);
	}

	/**
	 * 外部手动设置底部内容绝对位置（可选）
	 */
	public void setBottomContentY(int bottomContentY) {
		if (getChildCount() == 0) {
			mBottomContentY = bottomContentY;
			return;
		}
		// 限制不超过内容总高度
		int contentHeight = getChildAt(0).getMeasuredHeight();
		mBottomContentY = Math.min(bottomContentY, contentHeight);
		restoreBottomPosition();
	}

	/**
	 * 获取当前底部内容绝对位置（可选）
	 */
	public int getBottomContentY() {
		return mBottomContentY;
	}
}

