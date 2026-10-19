package cc.winboll.studio.winboll.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import cc.winboll.studio.winboll.MainActivity;
import cc.winboll.studio.winboll.R;
import cc.winboll.studio.winboll.views.WinBoLLView;
import java.util.ArrayList;
import android.app.Activity;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/27 11:09
 * @Describe 浏览器Fragment（Java 7 语法完整版，新增Handler消息接收）
 * 适配Java 7特性，支持接收应用内消息（如MSG_HOMEPAGE跳转首页）
 */
public class BrowserFragment extends Fragment implements View.OnClickListener, WinBoLLView.OnPageStatusListener {

    // 控件声明（Java 7 成员变量显式声明）
    private EditText mEtUrl;
    private Button mBtnLoad;
    private Button mBtnRefresh;
    private Button mBtnStop;
    private Button mBtnForward;
    private Button mBtnBack;
    private ProgressBar mProgressBar;
    private WinBoLLView mWinBoLLView;
	public static ArrayList<String> _mUrlLoadHistory = new ArrayList<String>();

    // ------------------- 新增：Handler 消息定义（应用内通信） -------------------
    // 消息标识：跳转首页（winboll.cc）
    public static final int MSG_HOMEPAGE = 1001;
	// 跳转到历史记录位置
    public static final int MSG_HISTORY_POSITION = 1002;
	// 打开外部应用传入的 URL
	public static final int MSG_OPEN_URL = 1003;
    // 自定义Handler（接收应用内其他页面发送的消息）
    private Handler mBrowserHandler;

    // 单例创建方法（Java 7 显式工厂模式）
    public static BrowserFragment newInstance() {
        return new BrowserFragment();
    }

    // 创建带初始URL的实例（供外部应用调用时使用）
    public static BrowserFragment newInstance(String initialUrl) {
        BrowserFragment fragment = new BrowserFragment();
        Bundle args = new Bundle();
        args.putString("initial_url", initialUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 加载布局（Java 7 显式强转，无菱形语法）
        View view = inflater.inflate(R.layout.fragment_browser, container, false);
		// 清理旧历史记录
		_mUrlLoadHistory.clear();

        // 初始化控件
        initViews(view);
        // 绑定事件
        initEvents();
        // 初始化WinBoLLView
        initWinBoLLView();
        // ------------------- 新增：初始化Handler（关键） -------------------
        initHandler();
        return view;
    }

    /**
     * 初始化控件（Java 7 显式绑定，无Stream简化）
     */
    private void initViews(View view) {
        mEtUrl = (EditText) view.findViewById(R.id.et_url);
        mBtnLoad = (Button) view.findViewById(R.id.btn_load);
        mBtnRefresh = (Button) view.findViewById(R.id.btn_refresh);
        mBtnStop = (Button) view.findViewById(R.id.btn_stop);
        mBtnForward = (Button) view.findViewById(R.id.btn_forward);
        mBtnBack = (Button) view.findViewById(R.id.btn_back);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mWinBoLLView = (WinBoLLView) view.findViewById(R.id.winboll_webview);
    }

    /**
     * 绑定点击事件（Java 7 匿名内部类，无Lambda）
     */
    private void initEvents() {
        // 功能按钮点击事件
        mBtnLoad.setOnClickListener(this);
        mBtnRefresh.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);
        mBtnForward.setOnClickListener(this);
        mBtnBack.setOnClickListener(this);

        // 输入框软键盘“前往”按钮事件（Java 7 匿名内部类实现）
        mEtUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event) {
					// 处理软键盘“前往”点击
					loadUrlFromInput();
					return true;
				}
			});
    }

    /**
     * 初始化WinBoLLView（Java 7 显式调用，无方法引用）
     */
    private void initWinBoLLView() {
        // 绑定进度条
        mWinBoLLView.setProgressBar(mProgressBar);
        // 设置页面状态监听（this 实现 OnPageStatusListener）
        mWinBoLLView.setOnPageStatusListener(this);

        // 检查是否有外部传入的初始 URL
        String initialUrl = null;
        if (getArguments() != null) {
            initialUrl = getArguments().getString("initial_url");
        }

        if (initialUrl != null && !initialUrl.isEmpty()) {
            // 使用外部传入的 URL
            mWinBoLLView.loadUrlSafe(initialUrl);
            mEtUrl.setText(initialUrl);
        } else {
            // 预加载默认页面（winboll.cc 首页）
            String defaultUrl = "https://www.winboll.cc";
            mWinBoLLView.loadUrlSafe(defaultUrl);
            mEtUrl.setText(defaultUrl);
        }
    }

    // ------------------- 新增：初始化Handler（接收应用内消息） -------------------
    private void initHandler() {
        // Java 7 匿名内部类实现Handler（主线程中创建，用于更新UI）
        mBrowserHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                // 根据消息标识处理不同逻辑
                switch (msg.what) {
                    case MSG_HOMEPAGE:
                        // 处理“跳转首页”消息：加载winboll.cc
                        String homeUrl = "https://www.winboll.cc";
                        mWinBoLLView.loadUrlSafe(homeUrl);
                        mEtUrl.setText(homeUrl);
                        showToast("已跳转至首页");
                        break;
					case MSG_HISTORY_POSITION:
						int position = (int)msg.obj;
						if(-1 < position && position < _mUrlLoadHistory.size()) {
							// 处理“跳转首页”消息：加载winboll.cc
							String historyUrl = _mUrlLoadHistory.get(position);
							mWinBoLLView.loadUrlSafe(historyUrl);
							mEtUrl.setText(historyUrl);
							//showToast("已跳转至" + historyUrl);
						}
                        break;
                    case MSG_OPEN_URL:
                        String openUrl = (String) msg.obj;
                        if (openUrl != null && !openUrl.isEmpty()) {
                            mWinBoLLView.loadUrlSafe(openUrl);
                            mEtUrl.setText(openUrl);
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * 点击事件处理（Java 7 switch-case 语句，无增强switch）
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_load) {
            // 加载输入框中的URL
            loadUrlFromInput();
        } else if (id == R.id.btn_refresh) {
            // 刷新当前页面
            mWinBoLLView.refreshPage();
        } else if (id == R.id.btn_stop) {
            // 停止页面加载
            mWinBoLLView.stopPageLoad();
        } else if (id == R.id.btn_forward) {
            // 前进（无历史则提示）
            if (!mWinBoLLView.goForwardSafe()) {
                showToast("无前进历史");
            }
        } else if (id == R.id.btn_back) {
            // 后退（无历史则提示）
            if (!mWinBoLLView.goBackSafe()) {
                showToast("无后退历史");
            }
        }
    }

    /**
     * 从输入框获取URL并加载（Java 7 显式空值校验）
     */
    private void loadUrlFromInput() {
        // 空值校验（Java 7 显式判断，无Objects.requireNonNull）
        if (mEtUrl == null) {
            showToast("控件初始化失败");
            return;
        }
        String url = mEtUrl.getText().toString().trim();
        // 调用WinBoLLView安全加载方法
        mWinBoLLView.loadUrlSafe(url);
        // 隐藏软键盘
        hideSoftKeyboard();
    }

    /**
     * 隐藏软键盘（Java 7 显式获取系统服务，无Lambda简化）
     */
    private void hideSoftKeyboard() {
        if (getActivity() == null) {
            return;
        }
        // 获取InputMethodManager（Java 7 显式强转）
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null && getActivity().getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * 显示Toast提示（Java 7 简化封装，避免重复代码）
     */
    private void showToast(String msg) {
        if (getActivity() == null || msg == null) {
            return;
        }
        // Java 7 显式创建Toast，无Toast.makeText简化链式调用
        Toast toast = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    // ------------------- WinBoLLView.OnPageStatusListener 实现（Java 7 显式重写） -------------------
    @Override
    public void onPageStarted(String url) {
        // 页面开始加载：更新输入框URL（Java 7 显式非空判断）
        if (mEtUrl != null && url != null) {
            mEtUrl.setText(url);
        }
    }

    @Override
    public void onPageFinished(String url) {
        // 页面加载完成：更新输入框URL
        if (mEtUrl != null && url != null) {
            mEtUrl.setText(url);
			addUrlToHistory(url);
        }
    }

    @Override
    public void onPageError(String errorMsg) {
        // 页面加载错误：显示错误提示
        showToast("加载失败：" + errorMsg);
    }

    // ------------------- 新增：对外提供Handler（供其他页面获取并发送消息） -------------------
    public Handler getBrowserHandler() {
        return mBrowserHandler;
    }

    // ------------------- 生命周期管理（防止内存泄漏，Java 7 显式重写） -------------------
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 销毁WinBoLLView（释放资源，避免内存泄漏）
        if (mWinBoLLView != null) {
            mWinBoLLView.destroyWebView();
            mWinBoLLView = null;
        }
        // ------------------- 新增：移除Handler消息（关键，防止内存泄漏） -------------------
        if (mBrowserHandler != null) {
            mBrowserHandler.removeCallbacksAndMessages(null); // 清除所有消息和回调
            mBrowserHandler = null;
        }
        // 置空控件（帮助GC回收）
        mEtUrl = null;
        mBtnLoad = null;
        mBtnRefresh = null;
        mBtnStop = null;
        mBtnForward = null;
        mBtnBack = null;
        mProgressBar = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 彻底释放资源
        if (mWinBoLLView != null) {
            mWinBoLLView.destroyWebView();
            mWinBoLLView = null;
        }
        // 再次清除Handler（双重保险）
        if (mBrowserHandler != null) {
            mBrowserHandler.removeCallbacksAndMessages(null);
            mBrowserHandler = null;
        }
    }

	// 在 BrowserFragment 中添加以下代码（Java 7 语法）
	/**
	 * 发送URL历史更新消息给MainActivity（当历史列表变化时调用）
	 */
	private void sendUrlHistoryUpdateMsg() {
		Message msg = Message.obtain();
	    msg.what = MainActivity.MSG_URLLOADHISTORY_UPDATE;
		MainActivity.sendMessage(msg);
	}

	// 调用时机示例（在BrowserFragment加载URL并更新历史列表后调用）
	// 假设BrowserFragment中有添加URL到历史的方法：
	private void addUrlToHistory(String url) {
		if (_mUrlLoadHistory == null) {
			_mUrlLoadHistory = new ArrayList<String>();
		}
		if (!_mUrlLoadHistory.contains(url)) {
			_mUrlLoadHistory.add(0, url);
			// 关键：添加历史后发送更新消息，通知MainActivity刷新抽屉菜单
			sendUrlHistoryUpdateMsg();
		}
	}
}

