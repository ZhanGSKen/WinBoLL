package cc.winboll.studio.timestamp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libappbase.LogView;
import com.hjq.toast.ToastUtils;

public class MainActivity extends AppCompatActivity {

    LogView mLogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

        mLogView = findViewById(R.id.logview);
        
        ToastUtils.show("onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLogView.start();
    }
}
