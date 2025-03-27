package cc.winboll.studio.ollama;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cc.winboll.studio.libappbase.LogView;

public class MainActivity extends Activity {

    LogView mLogView;
    TextView mtvMeaasge;
    EditText metAsk;
    Button mbtSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLogView = findViewById(R.id.logview);
        mLogView.start();
        
        mtvMeaasge = findViewById(R.id.message_tv);
        metAsk = findViewById(R.id.ask_et);
        mbtSend = findViewById(R.id.send_bt);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLogView.start();
    }
    
    public void onSend(View view) {
        OllamaClient.SyncAskThread thread = new OllamaClient.SyncAskThread(mtvMeaasge.getText().toString());
        thread.start();
        mtvMeaasge.setText("");
    }
}
