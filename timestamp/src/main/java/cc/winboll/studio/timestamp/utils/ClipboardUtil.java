package cc.winboll.studio.timestamp.utils;

/**
 * @Author ZhanGSKen
 * @Date 2025/05/06 10:53
 * @Describe 剪贴板工具集
 */
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;

public class ClipboardUtil {
    public static final String TAG = "ClipboardUtil";

    private static final long COPY_DELAY = 500; // 延迟 500 毫秒
    private static Handler handler = new Handler();

    /**
     * 拷贝文本到剪贴板
     * @param context 上下文
     * @param text 要拷贝的文本
     */
    public static void copyTextToClipboard(final Context context, final String text) {
        handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboardManager != null) {
                        ClipData clipData = ClipData.newPlainText("label", text);
                        clipboardManager.setPrimaryClip(clipData);
                    }
                }
            }, COPY_DELAY);
    }
}

