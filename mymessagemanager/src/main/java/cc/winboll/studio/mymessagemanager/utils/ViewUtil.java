package cc.winboll.studio.mymessagemanager.utils;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/07/19 14:30:57
 * @Describe Uri 视图元素工具类
 */
import android.widget.ScrollView;

public class ViewUtil {

    public static void scrollScrollView(final ScrollView scrollView) {
        scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
    }
}
