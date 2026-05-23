package cc.winboll.studio.libappbase.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.UUID;
import cc.winboll.studio.libappbase.GlobalApplication;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/04/06 19:32
 * @Describe 具有调试模式切换功能的应用Logo控件，连续点击10次弹出提示
 */
public class DebugSwitchInfoImageView extends ImageView {

    public static final String TAG = "DebugSwitchInfoImageView";

    // 连续点击计数
    private int mClickCount = 0;
    // 目标点击次数
    private static final int TARGET_CLICK_COUNT = 10;

    private static String mDebugToken = null;
    private static final String SP_DEBUG_TOKEN = "debug_token_prefs";
    private static final String KEY_DEBUG_TOKEN = "debug_token";

    public static String getDebugToken() {
        if (mDebugToken != null) {
            return mDebugToken;
        }
        Context context = GlobalApplication.getInstance();
        if (context != null) {
            SharedPreferences sp = context.getSharedPreferences(SP_DEBUG_TOKEN, Context.MODE_PRIVATE);
            mDebugToken = sp.getString(KEY_DEBUG_TOKEN, null);
            if (mDebugToken == null) {
                mDebugToken = UUID.randomUUID().toString();
                sp.edit().putString(KEY_DEBUG_TOKEN, mDebugToken).apply();
            }
        }
        return mDebugToken;
    }

	public DebugSwitchInfoImageView(Context context) {
        super(context);
        init();
    }

    public DebugSwitchInfoImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DebugSwitchInfoImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DebugSwitchInfoImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

