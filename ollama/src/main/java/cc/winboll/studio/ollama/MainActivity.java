package cc.winboll.studio.ollama;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public final static int MSG_APPEND = 0;

    private Handler _Handler = new Handler(Looper.getMainLooper());
    private TextView mtvMessage;
    private EditText metAsk;
    private Button mbtSend;
    private ScrollView msvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mtvMessage = (TextView) findViewById(R.id.message_tv);
        metAsk = (EditText) findViewById(R.id.ask_et);
        mbtSend = (Button) findViewById(R.id.send_bt);
        msvMessage = findViewById(R.id.message_sv);

        mbtSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendQuestion();
                }
            });
    }


    //
    // 设置输入框获得焦点的类
    //
//    static class MyHandler extends Handler {
//        WeakReference<MainActivity> mActivity;  
//        MyHandler(MainActivity activity) {  
//            mActivity = new WeakReference<MainActivity>(activity);  
//        }
//        public void handleMessage(Message msg) {
//            MainActivity theActivity = mActivity.get();
//            switch (msg.what) {
//                case MSG_APPEND:
//                    theActivity.mtvMessage.append((String)msg.obj);
//                    break;
//                default:
//                    break;
//            }
//            super.handleMessage(msg);
//        }
//	}

    private void sendQuestion() {
        final String question = metAsk.getText().toString().trim();
        if (!question.equals("")) {
            mtvMessage.append("\n\nI ：" + metAsk.getText().toString() + "\nOllama : ");
            metAsk.setText("");
            new OllamaClient.SyncAskThread(question, new OllamaClient.OnAnswerCallback() {
                    @Override
                    public void onAnswer(final String answer) {
                        _Handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mtvMessage.append(answer);
                                    msvMessage.post(new Runnable(){
                                            @Override
                                            public void run() {
                                                msvMessage.fullScroll(View.FOCUS_DOWN);
                                            }
                                        });
                                }
                            });
                    }
                }).start();
        }
    }
}

