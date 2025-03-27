package cc.winboll.studio.ollama;

import android.app.Activity;
import android.os.Bundle;
import cc.winboll.studio.libappbase.LogView;

public class MainActivity extends Activity {

    LogView mLogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLogView = findViewById(R.id.logview);
        mLogView.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLogView.start();
    }
}
