/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cc.winboll.studio.jcndk;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import cc.winboll.studio.jcndk.R;
import cc.winboll.studio.libjc.JAR_RUNNING_MODE;
import cc.winboll.studio.libjc.JCMainThread;
import cc.winboll.studio.libjc.Log;
import cc.winboll.studio.libjc.net.JCSocketServer;
import java.io.File;


public class HelloJni extends Activity implements JCMainThread.OnMessageListener {

    public static String TAG = "HelloJni";
    final public static int MSG_APPEN = 0;

    MessageHandler mMessageHandler;
    static JCMainThread _JCMainThread;
    StringBuilder mStringBuilder;
    Context mContext;
    ScrollView mScrollView;
    LinearLayout mRootLayout;
    TextView mTextView;
    EditText mEditText;
    Button mButton;
	JCSocketServer mJCSocketServer;
    JCMainServerThread mJCMainServerThread;

//    @Override
//    public void log(String message) {
//        Message msg = mMessageHandler.obtainMessage(MSG_APPEN, message);
//        mMessageHandler.sendMessage(msg);
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

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化日志类
        Log.init(getPackageName());
        Log.d(TAG, "onCreate");
        // 初始化视图元素
        mContext = this;
        mMessageHandler = new MessageHandler();
        //initView();
        initView2();
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
		_JCMainThread.setRunningMode(JAR_RUNNING_MODE.JCNDK);
        _JCMainThread.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Log.e(TAG, e, Thread.currentThread().getStackTrace());
        }
        appendMessage("Switch To Termux Bash Shell ...");
        File dirHome = new File(getFilesDir(), "home");
        if (!dirHome.exists()) {
            dirHome.mkdirs();
        }
        String szBashSwitchFileName = "switch2bash.sh";
        File fDstBashSwitch = new File(dirHome, szBashSwitchFileName);
        FileUtils.copyAssetsToSD(this, szBashSwitchFileName, fDstBashSwitch.toPath().toString());
        _JCMainThread.exeInit("cd " + dirHome.toPath().toString());
        _JCMainThread.exeInit("ash " + szBashSwitchFileName);

        //Intent intent = new Intent(this, JCMainService.class);
        //startService(intent);
		mJCMainServerThread = new JCMainServerThread();
		mJCMainServerThread.start();
    }

    void initView2() {
        View mainView = getLayoutInflater().inflate(R.layout.view_main, null);
        mRootLayout = mainView.findViewById(R.id.viewmainLinearLayout1);
        mScrollView = mainView.findViewById(R.id.viewmainScrollView1);
        mTextView = mainView.findViewById(R.id.viewmainTextView1);
        mButton = mainView.findViewById(R.id.viewmainButton1);
        mEditText = mainView.findViewById(R.id.viewmainEditText1);
        setContentView(mRootLayout);

        mButton.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
					mJCSocketServer.sendMessage(mEditText.getText().toString());
                    
					//System.out.println("onClick");
                    //_JCMainThread.exeBashCommand(mEditText.getText().toString());
                    mEditText.setText("");
                }
            });
    }
//
//    //
//    // 函数式初始化视图元素
//    //
//    void initView() {
//        // 创建一个线性布局作为根布局
//        mRootLayout = new LinearLayout(mContext);
//        mRootLayout.setOrientation(LinearLayout.VERTICAL);
//        mRootLayout.setLayoutParams(new LinearLayout.LayoutParams(
//                                       ViewGroup.LayoutParams.MATCH_PARENT,
//                                       ViewGroup.LayoutParams.MATCH_PARENT
//                                   ));
//
//        // 创建ScrollView
//        mScrollView = new ScrollView(mContext);
//        mScrollView.setLayoutParams(new LinearLayout.LayoutParams(
//                                       ViewGroup.LayoutParams.MATCH_PARENT,
//                                       ViewGroup.LayoutParams.MATCH_PARENT
//                                   ));
//
//        // 创建TextView
//        mTextView = new TextView(mContext);
//        mTextView.setTextColor(Color.BLACK);
//        mTextView.setTextSize(16);
//        mTextView.setGravity(Gravity.LEFT);
//        mTextView.setLayoutParams(new LinearLayout.LayoutParams(
//                                     ViewGroup.LayoutParams.MATCH_PARENT,
//                                     ViewGroup.LayoutParams.WRAP_CONTENT
//                                 ));
//
//        // 模拟添加文本内容
//        /*String longText = "";
//         for (int i = 0; i < 100; i++) {
//         longText += "这是一些示例文本内容，用于测试滚动效果。\n";
//         }
//         textView.setText(longText);*/
//
//        // 将TextView添加到ScrollView中
//        mScrollView.addView(mTextView);
//
//        // 将ScrollView添加到根布局中
//        mRootLayout.addView(mScrollView);
//
//        // 设置Activity的内容视图为根布局
//        setContentView(mRootLayout);
//
//    }

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

    /* A native method that is implemented by the
     * 'hello-jni' native library, which is packaged
     * with this application.
     */
    public native String  stringFromJNI();

    /* This is another native method declaration that is *not*
     * implemented by 'hello-jni'. This is simply to show that
     * you can declare as many native methods in your Java code
     * as you want, their implementation is searched in the
     * currently loaded native libraries only the first time
     * you call them.
     *
     * Trying to call this function will result in a
     * java.lang.UnsatisfiedLinkError exception !
     */
    public native String  unimplementedStringFromJNI();

    /* this is used to load the 'hello-jni' library on application
     * startup. The library has already been unpacked into
     * /data/data/com.example.hellojni/lib/libhello-jni.so at
     * installation time by the package manager.
     */
    static {
        System.loadLibrary("hello-jni");
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

	class JCMainServerThread extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println(String.format("%s Exception : %s", TAG, e.getMessage()));
                Log.e(TAG, e, Thread.currentThread().getStackTrace());
            }
            System.out.println("JCMainServerThread run()");
            mJCSocketServer = JCSocketServer.getInstance();
            mJCSocketServer.main(null);
            System.out.println("JCMainServerThread exit.");
        }

    }
}
