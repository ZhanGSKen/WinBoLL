package cc.winboll.studio.winboll.fragments;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Describe 简易浏览器启动窗口 Fragment（Java 7 语法）。
 * 由原 MainActivity 简易浏览器窗口改造为 Fragment，
 * 遵循 DrawerFragmentActivity 架构，通过 addFragment()/showFragment() 组装。
 */

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import cc.winboll.studio.winboll.R;

public class SimpleBrowserFragment extends Fragment implements View.OnClickListener {

    private static final String DEFAULT_HOME_URL = "https://www.baidu.com";
    private static final String ARG_INITIAL_URL = "initial_url";
    // Fragment 标题（主窗口副标题使用，Fragment 的标题就是副标题）
    public static final String FRAGMENT_TITLE = "简易浏览器";

    private WebView mWebView;
    private EditText mEtUrl;
    private Button mBtnBack;
    private Button mBtnForward;
    private Button mBtnHome;
    private Button mBtnRefresh;
    private Button mBtnLoad;

    // 工厂方法（Java 7 显式工厂模式）
    public static SimpleBrowserFragment newInstance() {
        return new SimpleBrowserFragment();
    }

    // 创建带初始URL的实例（供外部应用 ACTION_VIEW 传入时使用）
    public static SimpleBrowserFragment newInstance(String initialUrl) {
        SimpleBrowserFragment fragment = new SimpleBrowserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INITIAL_URL, initialUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 加载简易浏览器窗口布局（Java 7 显式强转）
        View view = inflater.inflate(R.layout.fragment_simple_browser, container, false);

        initViews(view);
        initEvents();
        initWebView();

        // 外部传入的初始 URL，无则使用默认主页
        String initialUrl = DEFAULT_HOME_URL;
        if (getArguments() != null) {
            String arg = getArguments().getString(ARG_INITIAL_URL);
            if (arg != null && !arg.isEmpty()) {
                initialUrl = arg;
            }
        }
        mEtUrl.setText(initialUrl);
        loadUrl(initialUrl);
        return view;
    }

    /**
     * 初始化控件
     */
    private void initViews(View view) {
        mEtUrl = (EditText) view.findViewById(R.id.et_url);
        mWebView = (WebView) view.findViewById(R.id.webview);
        mBtnBack = (Button) view.findViewById(R.id.btn_back);
        mBtnForward = (Button) view.findViewById(R.id.btn_forward);
        mBtnHome = (Button) view.findViewById(R.id.btn_home);
        mBtnRefresh = (Button) view.findViewById(R.id.btn_refresh);
        mBtnLoad = (Button) view.findViewById(R.id.btn_load);
    }

    /**
     * 绑定点击事件（Java 7 匿名内部类，无 Lambda）
     */
    private void initEvents() {
        mBtnBack.setOnClickListener(this);
        mBtnForward.setOnClickListener(this);
        mBtnHome.setOnClickListener(this);
        mBtnRefresh.setOnClickListener(this);
        mBtnLoad.setOnClickListener(this);

        // 输入框软键盘“前往”按钮事件
        mEtUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    loadUrlFromInput();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * 初始化 WebView
     */
    private void initWebView() {
        WebSettings ws = mWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setUseWideViewPort(true);
        ws.setLoadWithOverviewMode(true);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_load) {
            loadUrlFromInput();
        } else if (id == R.id.btn_refresh) {
            mWebView.reload();
        } else if (id == R.id.btn_back) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            }
        } else if (id == R.id.btn_forward) {
            if (mWebView.canGoForward()) {
                mWebView.goForward();
            }
        } else if (id == R.id.btn_home) {
            mEtUrl.setText(DEFAULT_HOME_URL);
            loadUrl(DEFAULT_HOME_URL);
        }
    }

    /**
     * 从输入框获取 URL 并加载
     */
    private void loadUrlFromInput() {
        if (mEtUrl == null) {
            showToast("控件初始化失败");
            return;
        }
        String url = mEtUrl.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            showToast("请输入网址");
            return;
        }
        loadUrl(url);
        hideSoftKeyboard();
    }

    /**
     * 加载 URL，自动补全协议头
     */
    private void loadUrl(String url) {
        if (mWebView == null || TextUtils.isEmpty(url)) {
            return;
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        mWebView.loadUrl(url);
    }

    /**
     * 供宿主 Activity 处理返回键：WebView 可后退则后退并返回 true
     */
    public boolean handleBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return false;
    }

    /**
     * 获取 Fragment 标题
     */
    public String getFragmentTitle() {
        return FRAGMENT_TITLE;
    }

    /**
     * 隐藏软键盘
     */
    private void hideSoftKeyboard() {
        if (getActivity() == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null && getActivity().getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * 显示 Toast 提示
     */
    private void showToast(String msg) {
        if (getActivity() == null || msg == null) {
            return;
        }
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    // 生命周期管理：销毁 WebView，避免内存泄漏
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
        mEtUrl = null;
        mBtnBack = null;
        mBtnForward = null;
        mBtnHome = null;
        mBtnRefresh = null;
        mBtnLoad = null;
    }
}
