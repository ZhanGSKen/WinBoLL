package cc.winboll.studio.winboll.views;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/27 11:05
 * @Describe 自定义WebView控件（WinBoLLView）
 * 集成进度监听、页面加载控制、安全配置等核心能力
 */
public class WinBoLLView extends WebView {
	public static final String TAG = "WinBoLLView";

	private ProgressBar mProgressBar; // 页面加载进度条（可选，增强用户体验）
	private OnPageStatusListener mStatusListener; // 页面状态监听回调
	private boolean mIsLoading = false; // 自定义加载状态标记（替代系统isLoading()）

	// 构造方法（兼容代码创建和XML布局引用）
	public WinBoLLView(Context context) {
		super(context);
		initWebViewSettings();
		initWebViewClient();
	}

	public WinBoLLView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initWebViewSettings();
		initWebViewClient();
	}

	public WinBoLLView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initWebViewSettings();
		initWebViewClient();
	}

	/**
	 * 初始化WebView基础配置（安全+性能+兼容性）
	 */
	private void initWebViewSettings() {
		WebSettings settings = getSettings();

		// 基础功能配置
		settings.setJavaScriptEnabled(true); // 启用JS（根据需求决定是否开启）
		settings.setSupportZoom(true); // 支持缩放
		settings.setBuiltInZoomControls(true); // 显示缩放控件
		settings.setDisplayZoomControls(false); // 隐藏系统缩放控件（优化UI）
		settings.setLoadWithOverviewMode(true); // 自适应屏幕
		settings.setUseWideViewPort(true); // 支持宽视角

		// 缓存配置（提升加载速度）
		settings.setCacheMode(WebSettings.LOAD_DEFAULT);
		settings.setDomStorageEnabled(true); // 启用DOM存储
		settings.setDatabaseEnabled(true); // 启用数据库存储

		// 安全配置（防止XSS和恶意跳转）
		settings.setJavaScriptCanOpenWindowsAutomatically(false); // 禁止JS自动打开窗口
		setBackgroundColor(0x00000000); // 透明背景（避免白屏闪烁）

		// 适配HTTPS和HTTP混合内容（Android 5.0+）
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
		}
	}

	/**
	 * 初始化WebViewClient和ChromeClient（控制页面加载和进度）
	 */
	private void initWebViewClient() {
		// 控制页面跳转（不打开系统浏览器）
		setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
					// 拦截URL加载，使用当前WebView打开
					view.loadUrl(request.getUrl().toString());
					return true;
				}

				@Override
				public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					// 页面开始加载：更新自定义加载状态标记
					mIsLoading = true;
					// 更新进度条+回调状态
					if (mProgressBar != null) mProgressBar.setVisibility(VISIBLE);
					if (mStatusListener != null) mStatusListener.onPageStarted(url);
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					// 页面加载完成：更新自定义加载状态标记
					mIsLoading = false;
					// 隐藏进度条+回调状态
					if (mProgressBar != null) mProgressBar.setVisibility(GONE);
					if (mStatusListener != null) mStatusListener.onPageFinished(url);
				}

				@Override
				public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
					super.onReceivedError(view, request, error);
					// 页面加载错误：更新自定义加载状态标记
					mIsLoading = false;
					// 回调错误信息
					if (mStatusListener != null) {
						String errorMsg = error.getDescription().toString();
						mStatusListener.onPageError(errorMsg);
					}
				}
			});

		// 监听页面加载进度
		setWebChromeClient(new WebChromeClient() {
				@Override
				public void onProgressChanged(WebView view, int newProgress) {
					super.onProgressChanged(view, newProgress);
					// 更新进度条进度
					if (mProgressBar != null) mProgressBar.setProgress(newProgress);
				}
			});
	}

	// ------------------- 对外暴露的控制方法（供Fragment调用） -------------------
	/**
	 * 加载URL（增加空值校验+优先HTTPS协议）
	 */
	public void loadUrlSafe(String url) {
		if (url == null || url.trim().isEmpty()) {
			if (mStatusListener != null) mStatusListener.onPageError("URL不能为空");
			return;
		}
		// 协议补全：优先HTTPS，兼容HTTP（解决ERR_CLEARTEXT_NOT_PERMITTED）
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			url = "https://" + url; // 改为优先HTTPS，而非HTTP
		}
		super.loadUrl(url);
	}
	
	/**
	 * 刷新当前页面（使用自定义mIsLoading判断加载状态）
	 */
	public void refreshPage() {
		if (mIsLoading) { // 替换原isLoading()，使用自定义标记
			stopLoading(); // 若正在加载，先停止再刷新
		}
		reload();
	}

	/**
	 * 停止页面加载（使用自定义mIsLoading判断加载状态）
	 */
	public void stopPageLoad() {
		if (mIsLoading) { // 替换原isLoading()，使用自定义标记
			stopLoading();
			mIsLoading = false; // 停止后更新状态标记
		}
	}

	/**
	 * 前进（判断是否有前进历史）
	 */
	public boolean goForwardSafe() {
		if (canGoForward()) {
			goForward();
			return true;
		}
		return false;
	}

	/**
	 * 后退（判断是否有后退历史）
	 */
	public boolean goBackSafe() {
		if (canGoBack()) {
			goBack();
			return true;
		}
		return false;
	}

	// ------------------- 辅助功能：进度条和状态监听 -------------------
	/**
	 * 设置进度条（绑定Fragment中的进度条控件）
	 */
	public void setProgressBar(ProgressBar progressBar) {
		this.mProgressBar = progressBar;
		if (mProgressBar != null) {
			mProgressBar.setMax(100);
			mProgressBar.setVisibility(GONE);
		}
	}

	/**
	 * 设置页面状态监听（供Fragment接收加载状态）
	 */
	public void setOnPageStatusListener(OnPageStatusListener listener) {
		this.mStatusListener = listener;
	}

	/**
	 * 页面状态监听接口
	 */
	public interface OnPageStatusListener {
		void onPageStarted(String url); // 页面开始加载
		void onPageFinished(String url); // 页面加载完成
		void onPageError(String errorMsg); // 页面加载错误
	}

	// ------------------- 资源释放（防止内存泄漏） -------------------
	/**
	 * 销毁WebView（必须在Fragment销毁时调用）
	 */
	public void destroyWebView() {
		// 停止加载并清空历史
		stopLoading();
		mIsLoading = false; // 销毁时重置加载状态
		clearHistory();
		// 移除所有WebViewClient（避免内存泄漏）
		setWebViewClient(null);
		setWebChromeClient(null);
		// 销毁WebView
		destroy();
	}
}
