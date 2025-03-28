package cc.winboll.studio.libappbase;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/11 00:14:05
 */
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import cc.winboll.studio.libappbase.R;

public final class GlobalCrashActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {

    private static final int MENUITEM_COPY = 0;
    private static final int MENUITEM_RESTART = 1;

    GlobalCrashReportView mGlobalCrashReportView;
    String mLog;
    

    public static final String TAG = "GlobalCrashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashHandler.AppCrashSafetyWire.getInstance().postResumeCrashSafetyWireHandler(getApplicationContext());

        mLog = getIntent().getStringExtra(CrashHandler.EXTRA_CRASH_INFO);
        //setTheme(android.R.style.Theme_Holo_Light_NoActionBar);
        //setTheme(R.style.APPBaseTheme);
        setContentView(R.layout.activity_globalcrash);
        mGlobalCrashReportView = findViewById(R.id.activityglobalcrashGlobalCrashReportView1);
        mGlobalCrashReportView.setReport(mLog);
        setActionBar(mGlobalCrashReportView.getToolbar());
        
        getSupportActionBar().setTitle(CrashHandler.TITTLE);
        getSupportActionBar().setSubtitle(GlobalApplication.getAppName(getApplicationContext()));
    }

    @Override
    public void onBackPressed() {
        restart();
    }

    private void restart() {
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );
            startActivity(intent);
        }
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case MENUITEM_COPY: 
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText(getPackageName(), mLog));
                Toast.makeText(getApplication(), "The text is copied.", Toast.LENGTH_SHORT).show();
                break;
            case MENUITEM_RESTART: 
                CrashHandler.AppCrashSafetyWire.getInstance().resumeToMaximumImmediately();
                restart();
                break;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENUITEM_COPY, 0, "Copy").setOnMenuItemClickListener(this)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, MENUITEM_RESTART, 0, "Restart").setOnMenuItemClickListener(this)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        
        // 更新菜单文字风格
        mGlobalCrashReportView.updateMenuStyle();
        return true;
    }
}
