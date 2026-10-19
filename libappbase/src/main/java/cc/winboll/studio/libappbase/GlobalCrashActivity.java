package cc.winboll.studio.libappbase;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import cc.winboll.studio.libappbase.utils.CrashHandleNotifyUtils;

/**
 * 应用异常报告观察活动窗口类
 * 核心功能：应用发生未捕获崩溃时，由 CrashHandler 启动此页面，展示崩溃日志详情，
 * 并提供「复制日志」「重启应用」操作入口，便于开发者定位问题和用户恢复应用
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @CreateTime 2025/11/11 19:58:00
 * @EditTime 2026/05/11 15:40:12
 */
public final class GlobalCrashActivity extends Activity implements MenuItem.OnMenuItemClickListener {

    // ====================== 常量定义 ======================
    public static final String TAG = "GlobalCrashActivity";
    /** 菜单标识：复制崩溃日志 */
    private static final int MENU_ITEM_COPY = 0;
    /** 菜单标识：重启应用 */
    private static final int MENU_ITEM_RESTART = 1;

    // ====================== 成员变量 ======================
    /** 崩溃报告展示自定义视图 */
    private GlobalCrashReportView mCrashReportView;
    /** 崩溃日志文本内容 */
    private String mCrashLog;

    // ====================== 生命周期方法 ======================
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        LogUtils.d(TAG, "onCreate 方法进入");
        try {
            super.onCreate(savedInstanceState);
            final Context appContext = getApplicationContext();
            // 初始化崩溃安全防护机制
            AppCrashSafetyWire.getInstance().postResumeCrashSafetyWireHandler(appContext);

            // 获取传递的崩溃日志
            mCrashLog = getIntent().getStringExtra(CrashHandler.EXTRA_CRASH_LOG);
            LogUtils.d(TAG, "获取到崩溃日志，长度：" + (mCrashLog != null ? mCrashLog.length() : 0));

            setContentView(R.layout.activity_globalcrash);
            mCrashReportView = findViewById(R.id.activityglobalcrashGlobalCrashReportView1);
            mCrashReportView.setReport(mCrashLog);

            setActionBar(mCrashReportView.getToolbar());
            if (getActionBar() != null) {
                getActionBar().setTitle(CrashHandler.TITTLE);
                getActionBar().setSubtitle(GlobalApplication.getAppName(appContext));
            }
        } catch (final Exception e) {
            LogUtils.e(TAG, "GlobalCrashActivity onCreate 发生异常", e);
            AppCrashSafetyWire.getInstance().burnSafetyWire();

            mCrashLog = getIntent().getStringExtra(CrashHandler.EXTRA_CRASH_LOG);
            final Intent intent = new Intent();
            intent.putExtra(CrashHandler.EXTRA_CRASH_LOG, mCrashLog);
            CrashHandleNotifyUtils.handleUncaughtException(GlobalApplication.getInstance(), intent, CrashHandler.CrashActivity.class);

            StackTraceElement[] stackElements = Thread.currentThread().getStackTrace();
            StringBuilder sb = new StringBuilder("GlobalCrashActivity onCreate StackTrace");
            for (StackTraceElement item : stackElements) {
                sb.append("\n").append(item.toString());
            }
            LogUtils.d(TAG, sb.toString());
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        LogUtils.d(TAG, "onBackPressed 触发重启应用");
        restartApp();
    }

    // ====================== 菜单相关回调 ======================
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        LogUtils.d(TAG, "onCreateOptionsView 初始化菜单");
        menu.add(0, MENU_ITEM_COPY, 0, "Copy")
			.setOnMenuItemClickListener(this)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.add(0, MENU_ITEM_RESTART, 0, "Restart")
			.setOnMenuItemClickListener(this)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        mCrashReportView.updateMenuStyle();
        return true;
    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        LogUtils.d(TAG, "菜单项被点击，ID：" + item.getItemId());
        switch (item.getItemId()) {
            case MENU_ITEM_COPY:
                copyCrashLogToClipboard();
                break;
            case MENU_ITEM_RESTART:
                AppCrashSafetyWire.getInstance().resumeToMaximumImmediately();
                restartApp();
                break;
            default:
                break;
        }
        return false;
    }

    // ====================== 内部私有工具方法 ======================
    /**
     * 重启当前应用
     */
    private void restartApp() {
        LogUtils.d(TAG, "开始执行应用重启逻辑");
        final PackageManager packageManager = getPackageManager();
        final Intent launchIntent = packageManager.getLaunchIntentForPackage(getPackageName());

        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
								  | Intent.FLAG_ACTIVITY_CLEAR_TOP
								  | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(launchIntent);
        }
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /**
     * 将崩溃日志复制到系统剪贴板
     */
    private void copyCrashLogToClipboard() {
        LogUtils.d(TAG, "执行复制崩溃日志到剪贴板");
        final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        final ClipData clipData = ClipData.newPlainText(getPackageName(), mCrashLog);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(getApplication(), "The text is copied.", Toast.LENGTH_SHORT).show();
    }
}

