package cc.winboll.studio.libaes.views;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import cc.winboll.studio.libaes.R;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/18 14:41
 * @Describe WinBoLL 横幅广告视图
 * 米盟 SDK 已移除：不再加载横幅广告，保留视图骨架与资源释放入口，避免影响主窗口布局引用
 */
public class ADsBannerView extends LinearLayout {

    public static final String TAG = "ADsBannerView";

    Context mContext;
    View mMianView;
    ViewGroup mContainer;
    // 新增：主线程Handler（保留用于资源释放）
    private Handler mMainHandler;

    public ADsBannerView(Context context) {
		super(context);
		initView(context);
	}

	public ADsBannerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public ADsBannerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(context);
	}

	public ADsBannerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initView(context);
	}

	void initView(Context context) {
		this.mContext = context;

		// 初始化主线程Handler（保留，用于资源释放回调）
		mMainHandler = new Handler(Looper.getMainLooper());

		this.mMianView = inflate(this.mContext, R.layout.view_adsbanner, null);
		mContainer = this.mMianView.findViewById(R.id.ads_container);
		addView(this.mMianView);
	}

	/**
	 * 恢复广告（米盟已移除，保留空实现以兼容宿主调用）
	 */
	public void resumeADs(final Activity activity) {
		// 米盟 SDK 已移除，横幅广告不再加载
	}

    /**
     * 释放广告资源（关键：避免内存泄漏和空Context调用）
     */
    public void releaseAdResources() {
        LogUtils.d(TAG, "releaseAdResources()");

        // 移除Handler回调
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
        }

        // 移除广告容器中的视图
        if (mContainer != null) {
            mContainer.removeAllViews();
        }
    }
}
