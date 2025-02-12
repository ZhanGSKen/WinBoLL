package cc.winboll.studio.appbase;

import android.content.ComponentName;
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
import cc.winboll.studio.libappbase.SOSCSBroadcastReceiver;
import cc.winboll.studio.libappbase.SimpleOperateSignalCenterService;
import com.hjq.toast.ToastUtils;
import cc.winboll.studio.libappbase.ISOSAPP;

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

    public void onStartCenter(View view) {
        SimpleOperateSignalCenterService.startISOSService(this);
    }

    public void onStopCenter(View view) {
        SimpleOperateSignalCenterService.stopISOSService(this);
    }

    public void onTestStopWithoutSettingEnable(View view) {
        LogUtils.d(TAG, "onTestStopWithoutSettingEnable");
        stopService(new Intent(this, SimpleOperateSignalCenterService.class));
    }

    public void onTestStartWithString(View view) {
        LogUtils.d(TAG, "onTestStartWithString");
        
        // 目标服务的包名和类名
        String packageName = this.getPackageName();
        String serviceClassName = SimpleOperateSignalCenterService.class.getName();

        // 构建Intent
        Intent intentService = new Intent();
        intentService.setComponent(new ComponentName(packageName, serviceClassName));

        startService(intentService);
    }

    public void onSOS(View view) {
        // 创建Intent对象，指定广播的action
        Intent intent = new Intent(SOSCSBroadcastReceiver.ACTION_SOS);
        // 目标服务的包名和类名
        String packageName = this.getPackageName();
        String serviceClassName = SimpleOperateSignalCenterService.class.getName();
        intent.putExtra(ISOSAPP.EXTRA_PACKAGE, packageName);
        intent.putExtra(ISOSAPP.EXTRA_SERVICE, serviceClassName);
        // 发送广播
        sendBroadcast(intent);
        LogUtils.d(TAG, "onSOS");
    }
}
