package cc.winboll.studio.timestamp;

import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libappbase.LogView;
import cc.winboll.studio.timestamp.utils.AppConfigsUtil;
import com.hjq.toast.ToastUtils;

public class MainActivity extends AppCompatActivity {

    LogView mLogView;
    Switch mswEnableMainService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

        mswEnableMainService = findViewById(R.id.activitymainSwitch1);
        mswEnableMainService.setChecked(AppConfigsUtil.getInstance(this).loadAppConfigs().isEnableService());

        mLogView = findViewById(R.id.logview);

        ToastUtils.show("onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLogView.start();
    }

    public void onSetMainServiceStatus(View view) {
        MainService.setMainServiceStatus(this, mswEnableMainService.isChecked());
    }
}
