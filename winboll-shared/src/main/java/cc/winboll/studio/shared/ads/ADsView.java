package cc.winboll.studio.shared.ads;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/12/09 15:37:35
 * @Describe WinBoll 应用推广视图
 */
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import com.hjq.toast.ToastUtils;

public class ADsView extends RelativeLayout {

    public static final String TAG = "ADsView";

    public volatile boolean mIsHandling;
    public volatile boolean mIsAddNewLog;

    Context mContext;
    WebView mWebView;
    //ScrollView mScrollView;
    //TextView mTextView;
    //CheckBox mSelectableCheckBox;
    //LogViewThread mLogViewThread;
    //LogViewHandler mLogViewHandler;
    //Spinner mLogLevelSpinner;
    //ArrayAdapter<CharSequence> mLogLevelSpinnerAdapter;

    public ADsView(Context context) {
        super(context);
        initView(context);
    }

    public ADsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ADsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public ADsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

//    public void start() {
//        mLogViewThread = new LogViewThread(LogView.this);
//        mLogViewThread.start();
//        // 显示日志
//        showAndScrollLogView();
//    }
//
//    public void scrollLogUp() {
//        mScrollView.post(new Runnable() {
//                @Override
//                public void run() {
//                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
//                    // 日志显示结束
//                    mLogViewHandler.setIsHandling(false);
//                    // 检查是否添加了新日志
//                    if (mLogViewHandler.isAddNewLog()) {
//                        // 有新日志添加，先更改新日志标志
//                        mLogViewHandler.setIsAddNewLog(false);
//                        // 再次发送显示日志的显示
//                        Message message = mLogViewHandler.obtainMessage(LogViewHandler.MSG_LOGVIEW_UPDATE);
//                        mLogViewHandler.sendMessage(message);
//                    }
//                }
//            });
//    }

    void initView(Context context) {
        mContext = context;

//        mLogViewHandler = new LogViewHandler();
        // 加载视图布局
        View viewMain = inflate(mContext, cc.winboll.studio.R.layout.view_ads, null);
        mWebView = viewMain.findViewById(cc.winboll.studio.R.id.viewadsWebView1);
        mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    ToastUtils.show(url);
                    return true;
                }
            });

//        // 初始化日志子控件视图
//        //
//        mScrollView = findViewById(cc.winboll.studio.R.id.viewlogScrollViewLog);
//        mTextView = findViewById(cc.winboll.studio.R.id.viewlogTextViewLog);
//        // 获取Log Level spinner实例
//        mLogLevelSpinner = findViewById(cc.winboll.studio.R.id.viewlogSpinner1);
//
//        (findViewById(cc.winboll.studio.R.id.viewlogButtonClean)).setOnClickListener(new View.OnClickListener(){
//
//                @Override
//                public void onClick(View v) {
//                    LogUtils.cleanLog();
//                    LogUtils.d(TAG, "Log is cleaned.");
//                }
//            });
//        (findViewById(cc.winboll.studio.R.id.viewlogButtonCopy)).setOnClickListener(new View.OnClickListener(){
//
//                @Override
//                public void onClick(View v) {
//
//                    ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
//                    cm.setPrimaryClip(ClipData.newPlainText(mContext.getPackageName(), LogUtils.loadLog()));
//                    LogUtils.d(TAG, "Log is copied.");
//                }
//            });
//        mSelectableCheckBox = findViewById(cc.winboll.studio.R.id.viewlogCheckBoxSelectable);
//        mSelectableCheckBox.setOnClickListener(new View.OnClickListener(){
//                @Override
//                public void onClick(View v) {
//                    if (mSelectableCheckBox.isChecked()) {
//                        setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
//                    } else {
//                        setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
//                    }
//                }
//            });
//
//        // 设置日志级别列表
//        ArrayList<String> adapterItems = new ArrayList<>();
//        for (LogUtils.LOG_LEVEL e : LogUtils.LOG_LEVEL.values()) {
//            adapterItems.add(e.name());
//        }
//        // 假设你有一个字符串数组作为选项列表
//        //String[] options = {"Option 1", "Option 2", "Option 3"};
//        // 创建一个ArrayAdapter来绑定数据到spinner
//        mLogLevelSpinnerAdapter = ArrayAdapter.createFromResource(
//            context, cc.winboll.studio.R.array.enum_loglevel_array, android.R.layout.simple_spinner_item);
//        mLogLevelSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        // 设置适配器并将它应用到spinner上
//        mLogLevelSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // 设置下拉视图样式
//        mLogLevelSpinner.setAdapter(mLogLevelSpinnerAdapter);
//        // 为Spinner添加监听器
//        mLogLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                    //String selectedOption = mLogLevelSpinnerAdapter.getItem(position);
//                    // 处理选中的选项...
//                    LogUtils.setLogLevel(LogUtils.LOG_LEVEL.values()[position]);
//                }
//                @Override
//                public void onNothingSelected(AdapterView<?> parent) {
//                    // 如果没有选择，则执行此操作...
//                }
//            });
//        // 获取默认值的索引
//        int defaultValueIndex = LogUtils.getLogLevel().ordinal();
//
//        if (defaultValueIndex != -1) {
//            // 如果找到了默认值，设置默认选项
//            mLogLevelSpinner.setSelection(defaultValueIndex);
//        }
//
//        // 设置滚动时不聚焦日志
//        setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }
    
    public void loadUrl(String url) {
        mWebView.loadUrl(url);
        //webView.loadUrl("https://www.winboll.cc");
        //webView.loadUrl("https://ads.winboll.cc");
    }

//    public void updateLogView() {
//        if (mLogViewHandler.isHandling() == true) {
//            // 正在处理日志显示，
//            // 就先设置一个新日志标志位
//            // 以便日志显示完后，再次显示新日志内容
//            mLogViewHandler.setIsAddNewLog(true);
//        } else {
//            //LogUtils.d(TAG, "LogListener showLog(String path)");
//            Message message = mLogViewHandler.obtainMessage(LogViewHandler.MSG_LOGVIEW_UPDATE);
//            mLogViewHandler.sendMessage(message);
//            mLogViewHandler.setIsAddNewLog(false);
//        }
//    }
//
//    void showAndScrollLogView() {
//        mTextView.setText(LogUtils.loadLog());
//        scrollLogUp();
//    }
//
//    class LogViewHandler extends Handler {
//
//        final static int MSG_LOGVIEW_UPDATE = 0;
//        volatile boolean isHandling;
//        volatile boolean isAddNewLog;
//
//        public LogViewHandler() {
//            setIsHandling(false);
//            setIsAddNewLog(false);
//        }
//
//        public void setIsHandling(boolean isHandling) {
//            this.isHandling = isHandling;
//        }
//
//        public boolean isHandling() {
//            return isHandling;
//        }
//
//        public void setIsAddNewLog(boolean isAddNewLog) {
//            this.isAddNewLog = isAddNewLog;
//        }
//
//        public boolean isAddNewLog() {
//            return isAddNewLog;
//        }
//
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case MSG_LOGVIEW_UPDATE:{
//                        if (isHandling() == false) {
//                            setIsHandling(true);
//                            showAndScrollLogView();
//                        }
//                        break;
//                    }
//                default:
//                    break;
//            }
//            super.handleMessage(msg);
//        }
//    }

}
