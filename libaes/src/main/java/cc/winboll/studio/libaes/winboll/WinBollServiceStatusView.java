package cc.winboll.studio.libaes.winboll;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/28 17:41:55
 * @Describe WinBoll 服务主机连接状态视图
 */
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cc.winboll.studio.libaes.winboll.WinBollClientService;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libapputils.R;
import cc.winboll.studio.libapputils.utils.PrefUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
//import okhttp3.Authenticator;
//import okhttp3.Credentials;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//import okhttp3.Route;

public class WinBollServiceStatusView extends LinearLayout {

    public static final String TAG = "WinBollServiceStatusView";

    public static final int MSG_CONNECTION_INFO = 0;
    public static final int MSG_UPDATE_CONNECTION_STATUS = 1;

    static WinBollServiceStatusView _WinBollServiceStatusView;
    Context mContext;
    //boolean mIsConnected;
    volatile ConnectionThread mConnectionThread;

    String mszServerHost;
    WinBollClientService mWinBollService;
    ImageView mImageView;
    TextView mTextView;
    WinBollServiceViewHandler mWinBollServiceViewHandler;
    //WebView mWebView;
    static volatile ConnectionStatus mConnectionStatus;
    View.OnClickListener mViewOnClickListener;
    static String _mUserName;
    static String _mPassword;

    static enum ConnectionStatus {
        DISCONNECTED,
        START_CONNECT,
        CONNECTING,
        CONNECTED;
    };

    boolean isBound = false;
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IWinBollClientServiceBinder binder = (IWinBollClientServiceBinder) service;
            mWinBollService = binder.getService();
            isBound = true;
            // 可以在这里调用Service的方法进行通信，比如获取数据
            mImageView.setBackgroundDrawable(mWinBollService.getCurrentStatusIconDrawable());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    public WinBollServiceStatusView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public WinBollServiceStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    public WinBollServiceStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    public WinBollServiceStatusView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        initView();
    }

    ConnectionStatus getConnectionStatus() {
        return false ?
            ConnectionStatus.CONNECTED 
            : ConnectionStatus.DISCONNECTED;
    }

    void initView() {
        _WinBollServiceStatusView = this;

        mImageView = new ImageView(mContext);
        setImageViewByConnection(mImageView, false);
        mConnectionStatus = getConnectionStatus();
        //mIsConnected = false;
        //mWinBollServerHostConnectionStatus = WinBollServerHostConnectionStatus.DISCONNECTED;
        //ToastUtils.show("initView()");

        mViewOnClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                LogUtils.d(TAG, "onClick()");
                if (mConnectionStatus == ConnectionStatus.CONNECTED) {
                    LogUtils.d(TAG, "Click to stop service.");
                    WinBollClientServiceBean bean = WinBollClientServiceBean.loadWinBollClientServiceBean(mContext);
                    bean.setIsEnable(false);
                    WinBollClientServiceBean.saveBean(mContext, bean);
                    Intent intent = new Intent(mContext, WinBollClientService.class);
                    mContext.stopService(intent);
                    //stopConnectionThread();
                    mTextView.setText("");
                    setImageViewByConnection(mImageView, false);
                    mConnectionStatus = ConnectionStatus.DISCONNECTED;
                } else if (mConnectionStatus == ConnectionStatus.DISCONNECTED) {
                    LogUtils.d(TAG, "Click to start service.");
                    WinBollClientServiceBean bean = WinBollClientServiceBean.loadWinBollClientServiceBean(mContext);
                    bean.setIsEnable(true);
                    WinBollClientServiceBean.saveBean(mContext, bean);
                    Intent intent = new Intent(mContext, WinBollClientService.class);
                    mContext.startService(intent);
                    //startConnectionThread();
                }
            }
        };
        setOnClickListener(mViewOnClickListener);
        addView(mImageView);
        mTextView = new TextView(mContext);
        mWinBollServiceViewHandler = new WinBollServiceViewHandler(this);
        addView(mTextView);
        /*mWebView = new WebView(mContext);
         mWebView.setWebViewClient(new WebViewClient() {
         @Override
         public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
         // 弹出系统基本HTTP验证窗口
         handler.proceed("username", "password");
         }
         });
         addView(mWebView);*/
    }

    void checkWinBollServerStatusAndUpdateCurrentView() {
        LogUtils.d(TAG, "checkWinBollServerStatusAndUpdateCurrentView()");
        /*if (getConnectionStatus() == ConnectionStatus.CONNECTED) {
         mConnectionStatus = ConnectionStatus.CONNECTED;
         } else {
         mConnectionStatus = ConnectionStatus.DISCONNECTED;
         }*/
    }

    public void setServerHost(String szWinBollServerHost) {
        mszServerHost = szWinBollServerHost;
    }

    public void setAuthInfo(String username, String password) {
        _mUserName = username;
        _mPassword = password;
    }

    void setImageViewByConnection(ImageView imageView, boolean isConnected) {
        //mIsConnected = isConnected;
        // 获取vector drawable
        Drawable drawable = mContext.getDrawable(isConnected ? R.drawable.ic_dev_connected : R.drawable.ic_dev_disconnected);
        if (drawable != null) {
            imageView.setImageDrawable(drawable);
        }
    }

    TextCallback apiTextCallback = new TextCallback() {
        @Override
        public void onSuccess(String text) {
            // 处理成功响应
            LogUtils.d(TAG, text);
        }

        @Override
        public void onFailure(Exception e) {
            // 处理失败情况
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
    };

    TextCallback cipTextCallback = new TextCallback() {
        @Override
        public void onSuccess(String text) {
            // 处理成功响应
            LogUtils.d(TAG, text);
            LogUtils.d(TAG, "Develop Host Connection IP is : " + text);
            mConnectionStatus = ConnectionStatus.CONNECTED;
            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            // 定义时间格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            // 按照指定格式格式化时间并输出
            String formattedDateTime = now.format(formatter);
            String msg = "ClientIP<" + formattedDateTime + ">: " + text;
            mWinBollServiceViewHandler.postMessageText(msg);
            mWinBollServiceViewHandler.postMessageConnectionStatus(true);
            
        }

        @Override
        public void onFailure(Exception e) {
            // 处理失败情况
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
            // 处理网络请求失败
            setImageViewByConnection(mImageView, false);
            mWinBollServiceViewHandler.postMessageText(e.getMessage());
            mWinBollServiceViewHandler.postMessageConnectionStatus(false);
        }
    };

    public void requestAPIWithBasicAuth() {
        String targetUrl = "https://" + (GlobalApplication.isDebuging() ?"dev.winboll": "winboll") + ".cc/api/"; // 替换为实际测试的URL
        requestWithBasicAuth(targetUrl, apiTextCallback);
    }

    public void requestCIPWithBasicAuth() {
        String targetUrl = mszServerHost + "/cip/?simple=true";
        requestWithBasicAuth(targetUrl, cipTextCallback);
    }

    public void requestWithBasicAuth(String targetUrl, TextCallback callback) {
        String username = "";
        String password = "";
        if (GlobalApplication.isDebuging()) {
            username = PrefUtils.getString(mContext, "metDevUserName", "");
            password = PrefUtils.getString(mContext, "metDevUserPassword", "");
        } else {
            username = "WinBoll";
            password = "WinBollPowerByZhanGSKen";
        }
        LogUtils.d(TAG, String.format("Connection Start. targetUrl %s", targetUrl));
        WinBollServerConnectionThread thread = new WinBollServerConnectionThread(
            targetUrl,
            username,
            password,
            cipTextCallback
        );
        thread.start();
    }

    /*void requestWithBasicAuth(final WinBollServiceViewHandler textViewHandler, String targetUrl, final String username, final String password) {
        // 用户名和密码，替换为实际的认证信息
        //String username = "your_username";
        //String password = "your_password";
        LogUtils.d(TAG, "requestWithBasicAuth(...)");
        LogUtils.d(TAG, String.format("targetUrl %s", targetUrl));

        // 构建包含认证信息的请求
        String credential = Credentials.basic(username, password);
        LogUtils.d(TAG, String.format("credential %s", credential));

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(targetUrl)
            .header("Accept", "text/plain") // 设置正确的Content-Type头
            .header("Authorization", credential)
            .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // 处理网络请求失败
                    setImageViewByConnection(mImageView, false);
                    textViewHandler.postMessageText(e.getMessage());
                    textViewHandler.postMessageConnectionStatus(false);
                    LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                    //String sz = "请求失败，状态码: " + response.code();
                    //setImageViewByConnection(mImageView, false);
                    //LogUtils.d(TAG, sz);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        setImageViewByConnection(mImageView, false);
                        textViewHandler.postMessageText("Unexpected code " + response);
                        textViewHandler.postMessageConnectionStatus(false);
                        LogUtils.d(TAG, "Unexpected code " + response, Thread.currentThread().getStackTrace());
                        return;
                    }

                    try {
                        // 读取响应体作为字符串，注意这里可能需要解码
                        String text = response.body().string();
                        LogUtils.d(TAG, "Develop Host Connection IP is : " + text);
                        mConnectionStatus = ConnectionStatus.CONNECTED;
                        // 获取当前时间
                        LocalDateTime now = LocalDateTime.now();
                        // 定义时间格式
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                        // 按照指定格式格式化时间并输出
                        String formattedDateTime = now.format(formatter);
                        textViewHandler.postMessageText("ClientIP<" + formattedDateTime + ">: " + text);
                        textViewHandler.postMessageConnectionStatus(true);

                        //org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(text);
                        //LogUtils.d(TAG, doc.text());

                        // 使用id选择器找到具有特定id的元素
                        //org.jsoup.nodes.Element elementWithId = doc.select("#LastRelease").first(); // 获取第一个匹配的元素

                        // 提取并打印元素的文本内容
                        //mszNewestAppPackageName = elementWithId.text();
                        //ToastUtils.delayedShow(text + "\n" + mszNewestAppPackageName, 5000);

                        //mHandler.sendMessage(mHandler.obtainMessage(MSG_APPUPDATE_CHECKED));
                        //System.out.println(response.body().string());
                        //                        mConnectionStatus = ConnectionStatus.CONNECTED;
                        //                        // 获取当前时间
                        //                        LocalDateTime now = LocalDateTime.now();
                        //
                        //                        // 定义时间格式
                        //                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                        //                        // 按照指定格式格式化时间并输出
                        //                        String formattedDateTime = now.format(formatter);
                        //                        //System.out.println(formattedDateTime);
                        //                        textViewHandler.postMessageText("ClientIP<" + formattedDateTime + ">: " + response.body().string());
                        //                        textViewHandler.postMessageConnectionStatus(true);
                    } catch (Exception e) {
                        LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                    }
                }
            });

    }*/

    class WinBollServiceViewHandler extends Handler {
        WinBollServiceStatusView mDevelopHostConnectionStatusView;

        public WinBollServiceViewHandler(WinBollServiceStatusView view) {
            mDevelopHostConnectionStatusView = view;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_CONNECTION_INFO) {
                mDevelopHostConnectionStatusView.mTextView.setText((String)msg.obj);
            } else if (msg.what == MSG_UPDATE_CONNECTION_STATUS) {
                mDevelopHostConnectionStatusView.setImageViewByConnection(mImageView, (boolean)msg.obj);
                mDevelopHostConnectionStatusView.mConnectionStatus = ((boolean)msg.obj) ? ConnectionStatus.CONNECTED : ConnectionStatus.DISCONNECTED;
            }
            super.handleMessage(msg);
        }

        void postMessageText(String szMSG) {
            Message msg = new Message();
            msg.what = MSG_CONNECTION_INFO;
            msg.obj = szMSG;
            sendMessage(msg);
        }

        void postMessageConnectionStatus(boolean isConnected) {
            Message msg = new Message();
            msg.what = MSG_UPDATE_CONNECTION_STATUS;
            msg.obj = isConnected;
            sendMessage(msg);
        }
    }

    public static void startConnection() {
        if (_WinBollServiceStatusView != null) {
            _WinBollServiceStatusView.startConnectionThread();
        }
    }

    public static void stopConnection() {
        if (_WinBollServiceStatusView != null) {
            _WinBollServiceStatusView.stopConnectionThread();
        }
    }

    void startConnectionThread() {
        if (mConnectionStatus == ConnectionStatus.DISCONNECTED) {
            mConnectionStatus = ConnectionStatus.START_CONNECT;
            LogUtils.d(TAG, "startConnectionThread");
            if (mConnectionThread != null) {
                mConnectionThread.mIsExist = true;
            }
            mConnectionThread = new ConnectionThread();
            mConnectionThread.start();
        } else if (mConnectionStatus == ConnectionStatus.CONNECTING) {
            //LogUtils.d(TAG, "mConnectionStatus == ConnectionStatus.CONNECTING");
        } else {
            LogUtils.d(TAG, "Unknow mConnectionStatus, can not start ConnectionThread.");
        }
    }

    void stopConnectionThread() {
        if (mConnectionStatus == ConnectionStatus.CONNECTED) {
            LogUtils.d(TAG, "stopConnectionThread");
            if (mConnectionThread != null) {
                mConnectionThread.mIsExist = true;
                mConnectionThread = null;
            }
        } else {
            LogUtils.d(TAG, "Unknow mConnectionStatus, can not start ConnectionThread.");
        }
    }



    class ConnectionThread extends Thread {

        public volatile boolean mIsExist;

        //DevelopHostConnectionStatusViewHandler mDevelopHostConnectionStatusViewHandler;

        //public ConnectionThread(DevelopHostConnectionStatusViewHandler developHostConnectionStatusViewHandler) {
        //mDevelopHostConnectionStatusViewHandler = developHostConnectionStatusViewHandler;
        //}
        public ConnectionThread() {
            mIsExist = false;
        }

        @Override
        public void run() {
            super.run();
            while (mIsExist == false) {
                if (mConnectionStatus == ConnectionStatus.START_CONNECT) {
                    mConnectionStatus = ConnectionStatus.CONNECTING;
                    //requestAPIWithBasicAuth();
                    requestCIPWithBasicAuth();
                } else if (mConnectionStatus == ConnectionStatus.CONNECTED
                           || mConnectionStatus == ConnectionStatus.DISCONNECTED) {
                    //ToastUtils.show("mWinBollServerHostConnectionStatus " + mConnectionStatus);
                    LogUtils.d(TAG, String.format("mConnectionStatus done %s", mConnectionStatus));
                } else {
                    LogUtils.d(TAG, String.format("mConnectionStatus unknow %s", mConnectionStatus));
                }

                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException e) {
                    LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                }
            }
            //ToastUtils.show("ConnectionThread exit.");
            LogUtils.d(TAG, "ConnectionThread exit.");
        }
    }

    /*WinBollService.OnServiceStatusChangeListener mOnServerStatusChangeListener = new WinBollService.OnServiceStatusChangeListener(){
     @Override
     public void onServerStatusChange(boolean isServiceAlive) {
     }
     };*/
}
