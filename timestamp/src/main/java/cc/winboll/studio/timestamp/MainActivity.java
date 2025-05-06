package cc.winboll.studio.timestamp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.LogView;
import cc.winboll.studio.timestamp.MainService;
import cc.winboll.studio.timestamp.R;
import cc.winboll.studio.timestamp.utils.AppConfigsUtil;
import com.hjq.toast.ToastUtils;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    EditText metTimeStampFormatString;
    TextView mtvTimeStampFormatString;
    EditText metTimeStampCopyFormatString;
    TextView mtvTimeStampCopyFormatString;
    
    LogView mLogView;
    Switch mswEnableMainService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
        
        metTimeStampFormatString = findViewById(R.id.et_timestampformatstring);
        mtvTimeStampFormatString = findViewById(R.id.tv_timestampformatstring);
        metTimeStampCopyFormatString = findViewById(R.id.et_timestampcopyformatstring);
        mtvTimeStampCopyFormatString = findViewById(R.id.tv_timestampcopyformatstring);
        
        metTimeStampFormatString.setText(AppConfigsUtil.getInstance(this).getAppConfigsModel().getTimeStampFormatString());
        showPreViewResult(metTimeStampFormatString, mtvTimeStampFormatString);
        metTimeStampCopyFormatString.setText(AppConfigsUtil.getInstance(this).getAppConfigsModel().getTimeStampCopyFormatString());
        showPreViewResult(metTimeStampCopyFormatString, mtvTimeStampCopyFormatString);
        
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

    public void onSaveFormatString(View view) {
        if(showPreViewResult(metTimeStampFormatString, mtvTimeStampFormatString)) {
            AppConfigsUtil.getInstance(this).getAppConfigsModel().setTimeStampFormatString(metTimeStampFormatString.getText().toString());
            AppConfigsUtil.getInstance(this).saveAppConfigs();
        }
    }

    public void onSaveCopyFormatString(View view) {
        if(showPreViewResult(metTimeStampCopyFormatString, mtvTimeStampCopyFormatString)) {
            AppConfigsUtil.getInstance(this).getAppConfigsModel().setTimeStampCopyFormatString(metTimeStampCopyFormatString.getText().toString());
            AppConfigsUtil.getInstance(this).saveAppConfigs();
        }
    }

    boolean showPreViewResult(EditText etFormat, TextView tvShow) {
        try {
            long currentMillis = System.currentTimeMillis();
            Instant instant = Instant.ofEpochMilli(currentMillis);
            LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            String szTimeStampFormatString = etFormat.getText().toString();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(szTimeStampFormatString);
            String formattedDateTime = ldt.format(formatter);
            tvShow.setText(formattedDateTime);
            return true;
        } catch (Exception e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
        return false;
    }
}
