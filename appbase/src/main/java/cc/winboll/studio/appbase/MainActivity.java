package cc.winboll.studio.appbase;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import cc.winboll.studio.appbase.R;
import cc.winboll.studio.appbase.activities.NewActivity;
import cc.winboll.studio.appbase.services.MainService;
import cc.winboll.studio.appbase.services.TestDemoBindService;
import cc.winboll.studio.appbase.services.TestDemoService;
import cc.winboll.studio.libappbase.CrashHandler;
import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.GlobalCrashActivity;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.sos.SOS;
import cc.winboll.studio.libappbase.utils.ToastUtils;
import cc.winboll.studio.libappbase.widgets.StatusWidget;
import cc.winboll.studio.libappbase.winboll.IWinBollActivity;

public class MainActivity extends WinBoLLActivity implements IWinBollActivity {

    public static final String TAG = "MainActivity";

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    Toolbar mToolbar;
    //LogView mLogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ToastUtils.show("onCreate");
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        CheckBox cbIsDebugMode = findViewById(R.id.activitymainCheckBox1);
        cbIsDebugMode.setChecked(GlobalApplication.isDebuging());
        //mLogView = findViewById(R.id.activitymainLogView1);

//        if (GlobalApplication.isDebuging()) {
//            mLogView.start(); 
//            ToastUtils.show("LogView start.");
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        getMenuInflater().inflate(R.menu.toolbar_appbase, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 在switch语句中处理每个ID，并在处理完后返回true，未处理的情况返回false。
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intentAPPWidget = new Intent(this, StatusWidget.class);
        intentAPPWidget.setAction(StatusWidget.ACTION_STATUS_UPDATE);
        sendBroadcast(intentAPPWidget);
    }

	public void onSwitchDebugMode(View view) {
        boolean isDebuging = ((CheckBox)view).isChecked();
        GlobalApplication.setIsDebuging(isDebuging);
        GlobalApplication.saveDebugStatus();
    }
   
    public void onPreviewGlobalCrashActivity(View view) {
        Intent intent = new Intent(this, GlobalCrashActivity.class);
        intent.putExtra(CrashHandler.EXTRA_CRASH_INFO, "Demo log...");
        startActivity(intent);
    }

    public void onStartCenter(View view) {
        MainService.startMainService(this);
    }

    public void onStopCenter(View view) {
        MainService.stopMainService(this);
    }

    public void onTestStopMainServiceWithoutSettingEnable(View view) {
        LogUtils.d(TAG, "onTestStopMainServiceWithoutSettingEnable");
        stopService(new Intent(this, MainService.class));
    }

    public void onTestUseComponentStartService(View view) {
        LogUtils.d(TAG, "onTestUseComponentStartService");

        // 目标服务的包名和类名
        String packageName = this.getPackageName();
        String serviceClassName = TestDemoService.class.getName();

        // 构建Intent
        Intent intentService = new Intent();
        intentService.setComponent(new ComponentName(packageName, serviceClassName));

        startService(intentService);
    }

    public void onTestDemoServiceSOS(View view) {
        Intent intent = new Intent(this, TestDemoService.class);
        stopService(intent);
        if (App.isDebuging()) {
            SOS.sosToAppBaseBeta(this, TestDemoService.class.getName());
        } else {
            SOS.sosToAppBase(this, TestDemoService.class.getName());
        }
    }

    public void onSartTestDemoService(View view) {
        Intent intent = new Intent(this, TestDemoService.class);
        intent.setAction(TestDemoService.ACTION_ENABLE);
        startService(intent);

    }
    
    

    public void onStopTestDemoService(View view) {
        Intent intent = new Intent(this, TestDemoService.class);
        intent.setAction(TestDemoService.ACTION_DISABLE);
        startService(intent);

        Intent intentStop = new Intent(this, TestDemoService.class);
        stopService(intentStop);
    }

    public void onStopTestDemoServiceNoSettings(View view) {
        Intent intent = new Intent(this, TestDemoService.class);
        stopService(intent);
    }

    public void onSartTestDemoBindService(View view) {
        Intent intent = new Intent(this, TestDemoBindService.class);
        intent.setAction(TestDemoBindService.ACTION_ENABLE);
        startService(intent);

    }

    public void onStopTestDemoBindService(View view) {
        Intent intent = new Intent(this, TestDemoBindService.class);
        intent.setAction(TestDemoBindService.ACTION_DISABLE);
        startService(intent);

        Intent intentStop = new Intent(this, TestDemoBindService.class);
        stopService(intentStop);
    }

    public void onStopTestDemoBindServiceNoSettings(View view) {
        Intent intent = new Intent(this, TestDemoBindService.class);
        stopService(intent);
    }

    public void onTestOpenNewActivity(View view) {
        GlobalApplication.getWinBollActivityManager().startWinBollActivity(this, NewActivity.class);
    }

    
}
