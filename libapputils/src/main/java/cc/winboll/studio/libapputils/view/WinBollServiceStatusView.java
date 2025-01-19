package cc.winboll.studio.libapputils.view;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/12/07 20:15:47
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
import androidx.core.content.ContextCompat;
import cc.winboll.studio.libapputils.R;
import cc.winboll.studio.libapputils.log.LogUtils;
import cc.winboll.studio.libapputils.service.IWinBollClientServiceBinder;
import cc.winboll.studio.libapputils.service.WinBollClientService;
import cc.winboll.studio.libapputils.service.WinBollClientServiceBean;
import com.hjq.toast.ToastUtils;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class WinBollServiceStatusView extends LinearLayout {

    public static final String TAG = "WinBollServiceStatusView";

    public static final int MSG_CONNECTION_INFO = 0;
    public static final int MSG_UPDATE_CONNECTION_STATUS = 1;

    Context mContext;
    //boolean mIsConnected;
    ConnectionThread mConnectionThread;

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
        mImageView = new ImageView(mContext);
        setImageViewByConnection(mImageView, false);
        mConnectionStatus = getConnectionStatus();
        //mIsConnected = false;
        //mWinBollServerHostConnectionStatus = WinBollServerHostConnectionStatus.DISCONNECTED;
        //ToastUtils.show("initView()");

        mViewOnClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //ToastUtils.show("onClick()");
                //ToastUtils.show("mWinBollServerHostConnectionStatus : " + mWinBollServerHostConnectionStatus);
                //isConnected = !isConnected;
                if (mConnectionStatus == ConnectionStatus.CONNECTED) {
                    ToastUtils.show("Click to stop service.");
                    WinBollClientServiceBean bean = WinBollClientServiceBean.loadWinBollClientServiceBean(mContext);
                    bean.setIsEnable(false);
                    WinBollClientServiceBean.saveBean(mContext, bean);
                    Intent intent = new Intent(mContext, WinBollClientService.class);
                    mContext.stopService(intent);
                    mConnectionStatus = ConnectionStatus.DISCONNECTED;
//                  
                    /*//ToastUtils.show("CONNECTED");
                     setConnectionStatusView(false);
                     mWinBollServerHostConnectionStatusViewHandler.postMessageText("");
                     if (mConnectionThread != null) {
                     mConnectionThread.mIsExist = true;
                     mConnectionThread = null;
                     mWinBollServerHostConnectionStatus = WinBollServerHostConnectionStatus.DISCONNECTED;
                     ToastUtils.show("WinBoll Server Disconnected.");
                     }*/
                } else if (mConnectionStatus == ConnectionStatus.DISCONNECTED) {
                    ToastUtils.show("Click to start service.");
                    WinBollClientServiceBean bean = WinBollClientServiceBean.loadWinBollClientServiceBean(mContext);
                    bean.setIsEnable(true);
                    WinBollClientServiceBean.saveBean(mContext, bean);
                    Intent intent = new Intent(mContext, WinBollClientService.class);
                    mContext.startService(intent);
                    mConnectionStatus = ConnectionStatus.CONNECTED;
                    ToastUtils.show("startService");
                    /*//ToastUtils.show("DISCONNECTED");
                     setConnectionStatusView(true);

                     if (mConnectionThread == null) {
                     ToastUtils.show("mConnectionThread == null");
                     mConnectionThread = new ConnectionThread();
                     mWinBollServerHostConnectionStatus = WinBollServerHostConnectionStatus.START_CONNECT;
                     mConnectionThread.start();
                     }*/
                } else {
                    ToastUtils.show("Other Click condition.");
                }

                /*if (isConnected) {
                 mWebView.loadUrl("https://dev.winboll.cc");
                 } else {
                 mWebView.stopLoading();
                 }*/
                //ToastUtils.show(mDevelopHostConnectionStatus);
                //LogUtils.d(TAG, "mDevelopHostConnectionStatus : " + mWinBollServerHostConnectionStatus);
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
        Drawable drawable = ContextCompat.getDrawable(mContext, isConnected ? R.drawable.ic_dev_connected : R.drawable.ic_dev_disconnected);
        if (drawable != null) {
            imageView.setImageDrawable(drawable);
        }
    }

    void requestWithBasicAuth(WinBollServiceViewHandler textViewHandler, String targetUrl, final String username, final String password) {
        // 用户名和密码，替换为实际的认证信息
        //String username = "your_username";
        //String password = "your_password";

        OkHttpClient client = new OkHttpClient.Builder()
            .authenticator(new Authenticator() {
                @Override
                public Request authenticate(Route route, Response response) throws IOException {
                    String credential = Credentials.basic(username, password);
                    return response.request().newBuilder()
                        .header("Authorization", credential)
                        .build();
                }
            })
            .build();

        Request request = new Request.Builder()
            .url(targetUrl) // 替换为实际要请求的网页地址
            .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                //System.out.println(response.body().string());
                //ToastUtils.show("Develop Host Connection IP is : " + response.body().string());
                // 获取当前时间
                LocalDateTime now = LocalDateTime.now();

                // 定义时间格式
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                // 按照指定格式格式化时间并输出
                String formattedDateTime = now.format(formatter);
                //System.out.println(formattedDateTime);
                textViewHandler.postMessageText("ClientIP<" + formattedDateTime + ">: " + response.body().string());
                textViewHandler.postMessageConnectionStatus(true);
            } else {
                String sz = "请求失败，状态码: " + response.code();
                setImageViewByConnection(mImageView, false);
                textViewHandler.postMessageText(sz);
                textViewHandler.postMessageConnectionStatus(false);
                LogUtils.d(TAG, sz);
            }
        } catch (IOException e) {
            textViewHandler.postMessageText(e.getMessage());
            textViewHandler.postMessageConnectionStatus(false);
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
    }

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
                    ToastUtils.show("WinBoll Server Connection Start.");
                    //LogUtils.d(TAG, "Develop Host Connection Start.");
                    String targetUrl = "https://" + mszServerHost + "/cip/?simple=true";  // 这里替换成你实际要访问的网址
                    requestWithBasicAuth(mWinBollServiceViewHandler, targetUrl, _mUserName, _mPassword);
                } else if (mConnectionStatus == ConnectionStatus.CONNECTED
                           && mConnectionStatus == ConnectionStatus.DISCONNECTED) {
                    ToastUtils.show("mWinBollServerHostConnectionStatus " + mConnectionStatus);
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
