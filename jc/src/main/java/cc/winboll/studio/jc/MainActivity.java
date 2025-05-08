package cc.winboll.studio.jc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import cc.winboll.studio.jc.R;
import cc.winboll.studio.libapputils.log.LogUtils;
import cc.winboll.studio.libjc.JAR_RUNNING_MODE;
import cc.winboll.studio.libjc.JCMainThread;
import cc.winboll.studio.libjc.net.JCSocketClient;

final public class MainActivity extends Activity implements JCMainThread.OnMessageListener {

	public static final String TAG = "MainActivity";

    public final static String DEFAULT_SERVER = "127.0.0.1";

    final public static int MSG_APPEN = 0;

    public static final int REQUEST_HOME_ACTIVITY = 0;
    public static final int REQUEST_ABOUT_ACTIVITY = 1;

    Context mContext;
    ScrollView mScrollView;
    LinearLayout mRootLayout;
    TextView mTextView;
    EditText mEditText;
    Button mButton;
    MessageHandler mMessageHandler;
    static JCMainThread _JCMainThread;
    JCSocketClient mJCSocketClient;
    JCClientMainThread mJCClientMainThread;
//    @Override
//    protected boolean isEnableDisplayHomeAsUp() {
//        return false;
//    }


    @Override
    public void outPrint(String message) {
        Message msg = mMessageHandler.obtainMessage(MSG_APPEN, message);
        mMessageHandler.sendMessage(msg);
    }

    @Override
    public void errPrint(String message) {
        Message msg = mMessageHandler.obtainMessage(MSG_APPEN, message);
        mMessageHandler.sendMessage(msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        mMessageHandler = new MessageHandler();
        // 测试添加消息 方式 1
        appendMessage("appendMessage(...) test.\n");
        // 测试添加消息 方式 2
        int szwhat = MSG_APPEN;
        String szobj = "mMessageHandler.sendMessage(...) test.\n";
        Message msg = mMessageHandler.obtainMessage(szwhat, szobj);
        mMessageHandler.sendMessage(msg);

        // 启动主线程
        _JCMainThread = JCMainThread.getInstance(getPackageName());
        _JCMainThread.setOnLogListener(this);
        _JCMainThread.setRunningMode(JAR_RUNNING_MODE.JC);
        _JCMainThread.start();

        // 设置 WinBoll 应用 UI 类型
        //WinBollApplication.setWinBollUI_TYPE(WinBollApplication.WinBollUI_TYPE.Aplication);
        //ToastUtils.show("WinBollUI_TYPE " + WinBollApplication.getWinBollUI_TYPE());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //mJCClientMainThread = new JCClientMainThread();
        //mJCClientMainThread.start();
    }



    void initView() {
        mScrollView = findViewById(R.id.activitymainScrollView1);
        mTextView = findViewById(R.id.activitymainTextView1);
        mButton = findViewById(R.id.activitymainButton1);
        mEditText = findViewById(R.id.activitymainEditText1);


        mButton.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    //System.out.println("onClick");
                    JCSocketClient.getInstance().sendMessage(mEditText.getText().toString());
                    mEditText.setText("");
                }
            });
    }

    //
    // 添加输出消息
    //
    void appendMessage(String szMessage) {
        mTextView.append(szMessage);

        // 让ScrollView滚动到底部
        mScrollView.post(new Runnable() {

                @Override
                public void run() {
                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }


            });
    }

//    @Override
//    public String getTag() {
//        return TAG;
//    }
//
//
//    @Override
//    protected boolean isAddWinBollToolBar() {
//        return true;
//    }
//
//    @Override
//    protected Toolbar initToolBar() {
//        return findViewById(R.id.activitymainToolbar1);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.item_about) {
//            try {
//                WinBollActivity clazzActivity = AboutActivity.class.newInstance();
//                String tag = clazzActivity.getTag();
//                LogUtils.d(TAG, "String tag = clazzActivity.getTag(); tag " + tag);
//                Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
//                startWinBollActivity(intent, tag);
//            } catch (IllegalAccessException e) {} catch (InstantiationException e) {}
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case REQUEST_HOME_ACTIVITY : {
                    LogUtils.d(TAG, "REQUEST_HOME_ACTIVITY");
                    break;
                }
            case REQUEST_ABOUT_ACTIVITY : {
                    LogUtils.d(TAG, "REQUEST_ABOUT_ACTIVITY");
                    break;
                }
            default : {
                    super.onActivityResult(requestCode, resultCode, data);
                }
        }
    }

    public void onConnectToServer(View view) {
        EditText etServer = (EditText)findViewById(R.id.activitymainEditText2);
        String server = etServer.getText().toString();
        if (server.equals("")) {
            mJCClientMainThread = new JCClientMainThread(DEFAULT_SERVER);
        } else {
            mJCClientMainThread = new JCClientMainThread(server);
        }
        mJCClientMainThread.start();
    }

    class MessageHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_APPEN : {
                        appendMessage(String.format("%s", msg.obj));
                    }
            }
            super.handleMessage(msg);
        }

    }

    class JCClientMainThread extends Thread {
        String szServer;

        JCClientMainThread() {
            szServer = DEFAULT_SERVER;
        }
        JCClientMainThread(String server) {
            szServer = server;
        }

        @Override
        public void run() {
            super.run();
            System.out.println("JCClientMainThread run()");
            mJCSocketClient = JCSocketClient.getInstance();
            mJCSocketClient.start(szServer);
            
            System.out.println("JCClientMainThread exit.");
        }

    }
}
