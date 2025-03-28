package cc.winboll.studio.libapputils.views;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/03 11:05:45
 * @Describe 简单网页视图类
 */
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class SimpleWebView extends WebView {

    public static final String TAG = "SimpleWebView";

    public SimpleWebView(Context context) {
        super(context);
        initWebView();
    }

    public SimpleWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWebView();
    }

    public SimpleWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initWebView();
    }

    private void initWebView() {
        // 获取WebView的设置对象
        WebSettings webSettings = getSettings();
        // 启用JavaScript
        webSettings.setJavaScriptEnabled(true);
    }
}
