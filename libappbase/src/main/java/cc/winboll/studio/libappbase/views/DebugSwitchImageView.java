package cc.winboll.studio.libappbase.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import cc.winboll.studio.libappbase.GlobalApplication;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/04/06 19:32
 * @Describe 具有调试模式切换功能的应用Logo控件，连续点击10次弹出提示
 */
public class DebugSwitchImageView extends ImageView {

    public static final String TAG = "DebugSwitchImageView";

    // 连续点击计数
    private int mClickCount = 0;
    // 目标点击次数
    private static final int TARGET_CLICK_COUNT = 10;

	public DebugSwitchImageView(Context context) {
        super(context);
        init();
    }

    public DebugSwitchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DebugSwitchImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DebugSwitchImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mClickCount++;
					if (mClickCount == TARGET_CLICK_COUNT) {
						// 达到10次，弹出Toast
						Toast.makeText(getContext(), "连续点击已达到10次，现在开启应用调试功能。", Toast.LENGTH_SHORT).show();
						GlobalApplication.setIsDebugging(true);
						GlobalApplication.saveDebugStatus(GlobalApplication.getInstance());
						// 重置计数，可再次触发
						mClickCount = 0;
					}
				}
			});
    }
}

