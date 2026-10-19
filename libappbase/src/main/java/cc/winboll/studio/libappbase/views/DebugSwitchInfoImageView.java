package cc.winboll.studio.libappbase.views;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.HashMap;
import java.util.UUID;
import cc.winboll.studio.libappbase.GlobalApplication;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/04/06 19:32
 * @Describe 应用Logo控件，连续点击6次弹出调试Token对话框，支持复制与重置
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

    private static final HashMap<Integer, String> NUMBER_WORDS = new HashMap<Integer, String>();
    static {
        NUMBER_WORDS.put(1, "one");
        NUMBER_WORDS.put(2, "two");
        NUMBER_WORDS.put(3, "three");
        NUMBER_WORDS.put(4, "four");
        NUMBER_WORDS.put(5, "five");
        NUMBER_WORDS.put(6, "six");
        NUMBER_WORDS.put(7, "seven");
        NUMBER_WORDS.put(8, "eight");
        NUMBER_WORDS.put(9, "nine");
        NUMBER_WORDS.put(10, "ten");
    }

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

    public static void resetDebugToken() {
        Context context = GlobalApplication.getInstance();
        if (context != null) {
            mDebugToken = UUID.randomUUID().toString();
            SharedPreferences sp = context.getSharedPreferences(SP_DEBUG_TOKEN, Context.MODE_PRIVATE);
            sp.edit().putString(KEY_DEBUG_TOKEN, mDebugToken).apply();
        }
    }

    private void showDebugTokenDialog() {
        final AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
        dialog.setTitle("调试Token");
        dialog.setMessage(getDebugToken());
        dialog.setCanceledOnTouchOutside(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "复制到剪贴板", (DialogInterface.OnClickListener) null);
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "重置", (DialogInterface.OnClickListener) null);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "关闭", (DialogInterface.OnClickListener) null);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        cm.setPrimaryClip(ClipData.newPlainText("DebugToken", getDebugToken()));
                    }
                });
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        resetDebugToken();
                        dialog.setMessage(getDebugToken());
                    }
                });
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
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
					String word = NUMBER_WORDS.get(mClickCount);
					if (word == null) {
						word = "One Piece!";
					}
					Toast.makeText(getContext(), word, Toast.LENGTH_SHORT).show();
					if (mClickCount >= TARGET_CLICK_COUNT) {
						mClickCount = 0;
						showDebugTokenDialog();
					}
				}
			});
    }
}

