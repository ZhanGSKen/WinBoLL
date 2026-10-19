package cc.winboll.studio.positions;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libaes.utils.AESThemeUtil;
import cc.winboll.studio.libaes.utils.DevelopUtils;
import cc.winboll.studio.libaes.views.ADsBannerView;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.positions.activities.AboutActivity;
import cc.winboll.studio.positions.activities.LocationActivity;
import cc.winboll.studio.positions.activities.SettingsActivity;
import cc.winboll.studio.positions.activities.WinBoLLActivity;
import cc.winboll.studio.positions.handlers.AppIdleRunningModeHandler;
import cc.winboll.studio.positions.utils.AppConfigsUtil;
import cc.winboll.studio.positions.utils.ServiceUtil;
import cc.winboll.studio.positions.services.IdleGpsService;
import cc.winboll.studio.positions.services.MainService;
import cc.winboll.studio.positions.R;
import android.os.Handler;
import android.os.Looper;
import android.os.Handler;
import android.os.Handler;
import android.os.Looper;
import android.content.Intent;
import android.os.Handler;

/**
 * 主页面控制器
 * 功能简述：
 * 1. 位置服务启停开关控制
 * 2. 页面菜单跳转与主题管理
 * 3. 应用空转状态接收与日志实时输出展示
 * 4. 全局权限申请与权限结果回调处理
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @CreateTime 2026/05/03 12:23:00
 * @EditTime   2026/05/03 15:42:15
 */
public class MainActivity extends WinBoLLActivity implements IWinBoLLActivity {

    // ===================== 常量定义 =====================
    public static final String TAG = "MainActivity";
    private static final int REQUEST_LOCATION_PERMISSIONS = 1001;
    private static final int REQUEST_BACKGROUND_LOCATION_PERMISSION = 1002;

    // ===================== UI控件声明 =====================
    private Toolbar mToolbar;
    private Switch mServiceSwitch;
    private Button mManagePositionsButton;
    private ADsBannerView mADsBannerView;
    private TextView mTvIdleLog;
    private ScrollView mScrollIdleLog;

    // ===================== 业务标记与回调 =====================
    private boolean isServiceBound = false;
    private OnAppIdleRunningListener mIdleRunningListener;

    /**
     * 应用空转状态回调内部接口
     */
    public interface OnAppIdleRunningListener {
        void onIdleStatusChange(boolean isRunning);
        void onIdleLogReceive(String log);
    }

    // ===================== 回调绑定方法 =====================
    public void setOnAppIdleRunningListener(OnAppIdleRunningListener listener) {
        this.mIdleRunningListener = listener;
        LogUtils.i(TAG, "setOnAppIdleRunningListener -> 空转监听绑定完成");
    }

    public OnAppIdleRunningListener getOnAppIdleRunningListener() {
        return mIdleRunningListener;
    }

    // ===================== 生命周期重写 =====================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LogUtils.i(TAG, "onCreate -> MainActivity页面创建完成");

        initToolbar();
        initViews();
        setLLMainBackgroundColor();

        if (!checkLocationPermissions()) {
            LogUtils.d(TAG, "onCreate -> 定位权限未授予，发起权限申请");
            requestLocationPermissions();
        }

        mADsBannerView = findViewById(R.id.adsbanner);
        initAppIdleHandler();
        refreshIdleLogLayout();

        // 根据调试模式控制日志区域的显示
        if (App.isDebugging()) {
            mScrollIdleLog.setVisibility(View.VISIBLE);
            LogUtils.d(TAG, "onCreate -> 调试模式，显示日志区域");
        } else {
            mScrollIdleLog.setVisibility(View.GONE);
            LogUtils.d(TAG, "onCreate -> 非调试模式，隐藏日志区域");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.d(TAG, "onResume -> 页面恢复可见");
        if (App.isAppIdleRunning()) {
            IdleGpsService.getInstance().start();
        }
        if (mADsBannerView != null) {
            mADsBannerView.resumeADs(MainActivity.this);
        }
        // 重新加载菜单以根据当前调试状态刷新
        invalidateOptionsMenu();
        LogUtils.d(TAG, "onResume -> 重新加载菜单完成");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.i(TAG, "onDestroy -> MainActivity页面销毁释放资源");
        if (mADsBannerView != null) {
            mADsBannerView.releaseAdResources();
        }
        mIdleRunningListener = null;
    }

    // ===================== 初始化相关方法 =====================
    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }
        LogUtils.d(TAG, "initToolbar -> 顶部工具栏初始化完毕");
    }

    private void initViews() {
        mTvIdleLog = (TextView) findViewById(R.id.tv_idle_log);
        mScrollIdleLog = (ScrollView) findViewById(R.id.scroll_idle_log);
        mServiceSwitch = (Switch) findViewById(R.id.switch_service_control);
        mManagePositionsButton = (Button) findViewById(R.id.btn_manage_positions);

        boolean serviceEnable = AppConfigsUtil.getInstance(this).isEnableMainService(true);
        mServiceSwitch.setChecked(serviceEnable);
        mManagePositionsButton.setEnabled(serviceEnable);

        mServiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //切换开关时先校验权限，无权限直接强制关闭开关
					if(isChecked && !checkLocationPermissions()){
                        mServiceSwitch.setChecked(false);
                        Toast.makeText(MainActivity.this,"未获取定位权限，无法开启GPS服务",Toast.LENGTH_SHORT).show();
                        return;
                    }

					LogUtils.d(TAG, "onCheckedChanged -> 服务开关状态变更，isChecked = " + isChecked);
					if (isChecked) {
						ServiceUtil.startAutoService(MainActivity.this);
					} else {
						ServiceUtil.stopAutoService(MainActivity.this);
					}
					mManagePositionsButton.setEnabled(isChecked);
                    refreshManageButtonState();
				}
			});
        LogUtils.i(TAG, "initViews -> 全部UI控件初始化绑定完成");
    }

    private void initAppIdleHandler() {
        AppIdleRunningModeHandler.init(MainActivity.this);
        setOnAppIdleRunningListener(new OnAppIdleRunningListener() {
				@Override
				public void onIdleStatusChange(boolean isRunning) {
					refreshIdleLogLayout();
					appendIdleLog("IdleRunning Status : " + isRunning);
				}

				@Override
				public void onIdleLogReceive(String log) {
					appendIdleLog(log);
				}
			});
        LogUtils.i(TAG, "initAppIdleHandler -> 空转处理器初始化与监听绑定完成");
    }

    private void setLLMainBackgroundColor() {
        TypedArray typedArray = getTheme().obtainStyledAttributes(new int[]{android.R.attr.colorAccent});
        int colorAccent = typedArray.getColor(0, Color.GRAY);
        typedArray.recycle();
        LinearLayout llmain = findViewById(R.id.llmain);
        llmain.setBackgroundColor(colorAccent);
    }

    // ===================== 空转日志输出工具 =====================
    private void appendIdleLog(final String logText) {
        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					String allLog = mTvIdleLog.getText().toString();
					mTvIdleLog.setText(allLog + logText + "\n");
					mScrollIdleLog.post(new Runnable() {
							@Override
							public void run() {
								mScrollIdleLog.fullScroll(ScrollView.FOCUS_DOWN);
							}
						});
				}
			});
    }

    private void refreshIdleLogLayout() {
        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (App.isAppIdleRunning()) {
						mScrollIdleLog.setBackgroundResource(R.drawable.shape_log_border);
					} else {
                        //关闭空转时：检测权限，无权限强制关闭GPS开关
                        if(!checkLocationPermissions()){
                            mServiceSwitch.setChecked(false);
                            ServiceUtil.stopAutoService(MainActivity.this);
                        }
						mTvIdleLog.setText("");
						mScrollIdleLog.setBackgroundColor(Color.TRANSPARENT);
						LogUtils.d(TAG, "refreshIdleLogLayout -> 非空转状态：日志清空、边框已移除");
					}
                    refreshToolbarSubTitle();
                    refreshManageButtonState();
				}
			});
    }

    private void refreshToolbarSubTitle() {
        if(getSupportActionBar() == null){
            return;
        }
        if(App.isAppIdleRunning()){
            getSupportActionBar().setSubtitle("当前处于空转运行状态");
        }else{
            getSupportActionBar().setSubtitle("");
        }
    }

    /**
     * 空转状态专属：按钮强制可点击 + 文字追加提示
     */
    private void refreshManageButtonState() {
        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (App.isAppIdleRunning()) {
						mManagePositionsButton.setText("位置与任务管理【当前空转中】");
						mManagePositionsButton.setEnabled(true);
					} else {
						mManagePositionsButton.setText("位置与任务管理");
						boolean serviceEnable = AppConfigsUtil.getInstance(MainActivity.this).isEnableMainService(true);
						mManagePositionsButton.setEnabled(serviceEnable);
					}
				}
			});
    }

    // ===================== 权限处理相关 =====================
    private boolean checkLocationPermissions() {
        int foregroundPerm = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        boolean hasForegroundPerm = (foregroundPerm == PackageManager.PERMISSION_GRANTED);
        boolean hasBackgroundPerm = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int backgroundPerm = checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            hasBackgroundPerm = (backgroundPerm == PackageManager.PERMISSION_GRANTED);
        }
        return hasForegroundPerm && hasBackgroundPerm;
    }

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
			!= PackageManager.PERMISSION_GRANTED) {
            String[] foregroundPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(foregroundPermissions, REQUEST_LOCATION_PERMISSIONS);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
					!= PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
									   REQUEST_BACKGROUND_LOCATION_PERMISSION);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LogUtils.d(TAG, "onRequestPermissionsResult -> 权限回调 requestCode = " + requestCode);

        if (requestCode == REQUEST_LOCATION_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationPermissions();
            } else {
                Toast.makeText(this, "需要前台定位权限才能使用该功能", Toast.LENGTH_SHORT).show();
                //权限被拒绝，强制关闭开关
                mServiceSwitch.setChecked(false);
                ServiceUtil.stopAutoService(MainActivity.this);
                mManagePositionsButton.setEnabled(false);
            }
        } else if (requestCode == REQUEST_BACKGROUND_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "已获得后台定位权限", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "拒绝后台权限将无法在后台持续定位", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ===================== 菜单与页面跳转 =====================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        AESThemeUtil.inflateMenu(this, menu);
        if (App.isDebugging()) {
            DevelopUtils.inflateMenu(this, menu);
            getMenuInflater().inflate(R.menu.toolbar_main_idle, menu);
        }
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuItemId = item.getItemId();
        LogUtils.d(TAG, "onOptionsItemSelected -> 点击菜单ID = " + menuItemId);

        if (AESThemeUtil.onAppThemeItemSelected(this, item)) {
            recreate();
        } else if (DevelopUtils.onDevelopItemSelected(this, item)) {
            LogUtils.d(TAG, "onOptionsItemSelected -> 进入开发工具菜单");
        } else if (menuItemId == R.id.item_idle_switch) {
            boolean idleNow = App.isAppIdleRunning();
            boolean idleNew = !idleNow;
            App.setAppIdleRunning(idleNew);
            AppIdleRunningModeHandler.sendIdleSwitch(idleNew);
            AppIdleRunningModeHandler.sendIdleLog("菜单手动切换空转状态：" + idleNew);
            LogUtils.d(TAG, "onOptionsItemSelected -> 空转状态已切换，当前：" + idleNew);
            refreshIdleLogLayout();
        } else if (menuItemId == R.id.item_settings) {
            Intent intent = new Intent();
            intent.setClass(this, SettingsActivity.class);
            startActivity(intent);
        } else if (menuItemId == R.id.item_about) {
            Intent intent = new Intent();
            intent.setClass(this, AboutActivity.class);
            startActivity(intent);
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void onPositions(View view) {
        LogUtils.d(TAG, "onPositions -> 跳转位置任务管理页面");
        startActivity(new Intent(MainActivity.this, LocationActivity.class));
    }

    // ===================== 接口实现 =====================
    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }
}

