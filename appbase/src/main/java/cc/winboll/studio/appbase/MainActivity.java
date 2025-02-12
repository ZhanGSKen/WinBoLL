package cc.winboll.studio.appbase;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.appbase.R;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.LogView;
import com.hjq.toast.ToastUtils;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    LogView mLogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ToastUtils.show("onCreate");
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.activitymainToolbar1);
        setSupportActionBar(toolbar);

        CheckBox cbIsDebugMode = findViewById(R.id.activitymainCheckBox1);
        cbIsDebugMode.setChecked(GlobalApplication.isDebuging());
        mLogView = findViewById(R.id.activitymainLogView1);

        if (GlobalApplication.isDebuging()) { mLogView.start(); }
    }

    @Override
    protected void onResume() {
        LogUtils.d(TAG, "onResume");
        super.onResume();
        mLogView.start();
    }

	public void onSwitchDebugMode(View view) {
        GlobalApplication.setIsDebuging(this, ((CheckBox)view).isChecked());
    }

    public void onSOS(View view) {
        // 创建Intent对象，指定广播的action
        Intent intent = new Intent("cc.winboll.studio.libappbase.SOSCSBroadcastReceiver.ACTION_SOS");
        // 可以添加额外的数据
        intent.putExtra("data", "这是广播携带的数据");
        // 发送广播
        sendBroadcast(intent);
        LogUtils.d(TAG, "onSOS");
    }
}
