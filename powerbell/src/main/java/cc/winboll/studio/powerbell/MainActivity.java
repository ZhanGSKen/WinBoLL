package cc.winboll.studio.powerbell;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libaes.models.APPInfo;
import cc.winboll.studio.libaes.utils.AESThemeUtil;
import cc.winboll.studio.libaes.utils.DevelopUtils;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.libaes.views.ADsBannerView;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.powerbell.activities.AboutActivity;
import cc.winboll.studio.powerbell.activities.BackgroundSettingsActivity;
import cc.winboll.studio.powerbell.activities.BatteryReportActivity;
import cc.winboll.studio.powerbell.activities.ClearRecordActivity;
import cc.winboll.studio.powerbell.activities.SettingsActivity;
import cc.winboll.studio.powerbell.activities.WinBoLLActivity;
import cc.winboll.studio.powerbell.models.BackgroundBean;
import cc.winboll.studio.powerbell.models.BatteryStyle;
import cc.winboll.studio.powerbell.models.ControlCenterServiceBean;
import cc.winboll.studio.powerbell.services.ControlCenterService;
import cc.winboll.studio.powerbell.unittest.MainUnitTest2Activity;
import cc.winboll.studio.powerbell.unittest.MainUnitTestActivity;
import cc.winboll.studio.powerbell.utils.AppConfigUtils;
import cc.winboll.studio.powerbell.utils.BackgroundSourceUtils;
import cc.winboll.studio.powerbell.utils.ImageUtils;
import cc.winboll.studio.powerbell.utils.PermissionUtils;
import cc.winboll.studio.powerbell.utils.ServiceUtils;
import cc.winboll.studio.powerbell.views.BatteryStyleView;
import cc.winboll.studio.powerbell.views.MainContentView;

/**
 * 应用核心主活动
 * 功能：管理电池监控、背景设置、服务启停、权限申请等核心功能
 * 适配：Java7 | API30 | 内存泄漏防护 | UI与服务状态实时同步
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 */
public class MainActivity extends WinBoLLActivity implements MainContentView.OnViewActionListener {

    // ======================== 静态常量区（抽离魔法值，按功能分类）========================
    public static final String TAG = "MainActivity";
    private static final int REQUEST_BACKGROUND_SETTINGS_ACTIVITY = 1001;
    public static final String EXTRA_ISRELOAD_BACKGROUNDVIEW = "EXTRA_ISRELOAD_BACKGROUNDVIEW";
    public static final String EXTRA_ISRELOAD_ACCENTCOLOR = "EXTRA_ISRELOAD_ACCENTCOLOR";
    private static final long DELAY_LOAD_NON_CRITICAL = 500L;

    // Handler 消息常量
    public static final int MSG_RELOAD_APPCONFIG = 0;
    public static final int MSG_CURRENTVALUEBATTERY = 1;
    public static final int MSG_LOAD_BACKGROUND = 2;
    private static final int MSG_UPDATE_SERVICE_SWITCH = 3;
	private static final int MSG_UPDATE_BATTERYDRAWABLE = 4;

    // ======================== 静态成员区（全局共享，管控生命周期）========================
    private static MainActivity sMainActivity;
    private static Handler sGlobalHandler;

    // ======================== 工具类实例区（单例化，避免重复初始化）========================
    private PermissionUtils mPermissionUtils;
    private AppConfigUtils mAppConfigUtils;
    private BackgroundSourceUtils mBgSourceUtils;

    // ======================== 应用核心实例区 =========================
    private App mApplication;
    private MainContentView mMainContentView;
    private ControlCenterServiceBean mServiceControlBean;

    // ======================== 基础视图组件区 =========================
    private Toolbar mToolbar;
    private ViewStub mAdsViewStub;
    private ADsBannerView mADsBannerView;
    private Drawable mFrameDrawable;
    private Menu mMenu;

    // ======================== 生命周期方法区（按系统调用顺序排列）========================
    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate() 调用 | savedInstanceState: " + savedInstanceState);

        initGlobalHandler();
        setContentView(R.layout.activity_main);
        initPermissionUtils();
        initMainContentView();
        initCriticalView();
        initCoreUtilsAsync();
        loadNonCriticalViewDelayed();

        // 处理首次启动参数
        handleReloadBackgroundParam(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtils.d(TAG, "onNewIntent() 调用 | intent: " + intent);
        // 关键：更新Activity持有的Intent，确保后续获取最新值
        setIntent(intent);
        // 统一处理刷新背景参数
        handleReloadBackgroundParam(intent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        LogUtils.d(TAG, "onPostCreate() 调用 | savedInstanceState: " + savedInstanceState);
        mPermissionUtils.startPermissionRequest(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.d(TAG, "onResume() 调用");

        if (mADsBannerView != null) {
            mADsBannerView.resumeADs(this);
            LogUtils.d(TAG, "onResume: 广告视图已恢复");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.d(TAG, "onPause() 调用");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy() 调用");

        // 释放广告资源
        if (mADsBannerView != null) {
            mADsBannerView.releaseAdResources();
            mADsBannerView = null;
            LogUtils.d(TAG, "onDestroy: 广告资源已释放");
        }
        // 释放核心视图
        if (mMainContentView != null) {
            mMainContentView.releaseResources();
            mMainContentView = null;
            LogUtils.d(TAG, "onDestroy: 核心视图资源已释放");
        }
        // 销毁Handler防止内存泄漏
        if (sGlobalHandler != null) {
            sGlobalHandler.removeCallbacksAndMessages(null);
            sGlobalHandler = null;
            LogUtils.d(TAG, "onDestroy: 全局Handler已销毁");
        }
        // 释放Drawable
        if (mFrameDrawable != null) {
            mFrameDrawable.setCallback(null);
            mFrameDrawable = null;
            LogUtils.d(TAG, "onDestroy: 框架Drawable已释放");
        }
        // 置空所有引用，消除内存泄漏风险
        sMainActivity = null;
        mPermissionUtils = null;
        mAppConfigUtils = null;
        mBgSourceUtils = null;
        mServiceControlBean = null;
        mMenu = null;
        mApplication = null;
        mToolbar = null;
        mAdsViewStub = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.d(TAG, "onActivityResult() 调用 | requestCode: " + requestCode + " | resultCode: " + resultCode + " | data: " + data);
        mPermissionUtils.handlePermissionRequest(this, requestCode, resultCode, data);

        if (requestCode == REQUEST_BACKGROUND_SETTINGS_ACTIVITY && sGlobalHandler != null) {
            sGlobalHandler.sendEmptyMessage(MSG_LOAD_BACKGROUND);
            LogUtils.d(TAG, "onActivityResult: 发送背景加载消息");
        }
    }

    // ======================== 菜单与导航方法区 ========================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LogUtils.d(TAG, "onCreateOptionsMenu() 调用 | menu: " + menu);
        mMenu = menu;
        AESThemeUtil.inflateMenu(this, menu);

        // 调试模式加载测试菜单
        if (App.isDebugging()) {
            DevelopUtils.inflateMenu(this, menu);
            getMenuInflater().inflate(R.menu.toolbar_unittest, mMenu);
            LogUtils.d(TAG, "onCreateOptionsMenu: 已加载测试菜单");
        }
        getMenuInflater().inflate(R.menu.toolbar_main, mMenu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LogUtils.d(TAG, "onOptionsItemSelected() 调用 | itemId: " + item.getItemId());
        // 主题切换处理
        if (AESThemeUtil.onAppThemeItemSelected(this, item)) {
            recreate();
			Intent mainIntent = new Intent(MainActivity.this, MainActivity.class);
			mainIntent.putExtra(MainActivity.EXTRA_ISRELOAD_BACKGROUNDVIEW, true);
			mainIntent.putExtra(MainActivity.EXTRA_ISRELOAD_ACCENTCOLOR, true);
			startActivity(mainIntent);
            return true;
        }
        // 开发者功能处理
        if (DevelopUtils.onDevelopItemSelected(this, item)) {
            return true;
        }
        // 菜单点击事件分发
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_battery_report:
                startActivity(new Intent(this, BatteryReportActivity.class));
                break;
            case R.id.action_clearrecord:
                startActivity(new Intent(this, ClearRecordActivity.class));
                break;
            case R.id.action_changepicture:
                startActivityForResult(new Intent(this, BackgroundSettingsActivity.class), REQUEST_BACKGROUND_SETTINGS_ACTIVITY);
                break;
            case R.id.action_unittestactivity:
                startActivity(new Intent(this, MainUnitTestActivity.class));
                break;
            case R.id.action_unittest2activity:
                startActivity(new Intent(this, MainUnitTest2Activity.class));
                break;
            case R.id.action_about:
                startAboutActivity();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void setupToolbar() {
        super.setupToolbar();
        LogUtils.d(TAG, "setupToolbar() 调用");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            LogUtils.d(TAG, "setupToolbar: 已隐藏返回按钮");
        }
    }

    @Override
    public void onBackPressed() {
        LogUtils.d(TAG, "onBackPressed() 调用");
        moveTaskToBack(true);
        LogUtils.d(TAG, "onBackPressed: 应用已退至后台");
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        LogUtils.d(TAG, "dispatchKeyEvent() 调用 | event: " + event);
        return super.dispatchKeyEvent(event);
    }

    // ======================== 核心初始化方法区 ========================
    private void initPermissionUtils() {
        LogUtils.d(TAG, "initPermissionUtils() 调用");
        mPermissionUtils = PermissionUtils.getInstance();
        LogUtils.d(TAG, "initPermissionUtils: 权限工具类已初始化");
    }

    private void initGlobalHandler() {
        LogUtils.d(TAG, "initGlobalHandler() 调用");
        if (sGlobalHandler == null) {
            sGlobalHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    // Activity已销毁则跳过消息处理
                    if (sMainActivity == null || sMainActivity.isFinishing() || sMainActivity.isDestroyed()) {
                        LogUtils.w(TAG, "handleMessage: Activity已销毁，跳过消息 | what: " + msg.what);
                        return;
                    }
                    LogUtils.d(TAG, "handleMessage() 调用 | what: " + msg.what);

                    switch (msg.what) {
                        case MSG_RELOAD_APPCONFIG:
                            sMainActivity.updateViewData();
                            break;
                        case MSG_CURRENTVALUEBATTERY:
                            if (sMainActivity.mMainContentView != null) {
                                sMainActivity.mMainContentView.updateCurrentBattery(msg.arg1);
                                LogUtils.d(TAG, "handleMessage: 更新当前电量 | value: " + msg.arg1);
                            }
                            break;
                        case MSG_LOAD_BACKGROUND:
                            sMainActivity.reloadBackground();
                            break;
                        case MSG_UPDATE_SERVICE_SWITCH:
                            sMainActivity.updateServiceSwitchUI();
                            break;
						case MSG_UPDATE_BATTERYDRAWABLE:
                            sMainActivity.updateBatteryDrawable();
                            break;
                    }
                }
            };
            LogUtils.d(TAG, "initGlobalHandler: 全局Handler已创建");
        } else {
            LogUtils.d(TAG, "initGlobalHandler: 全局Handler已存在，无需重复创建");
        }
    }

    private void initMainContentView() {
        LogUtils.d(TAG, "initMainContentView() 调用");
        View rootView = findViewById(android.R.id.content);
        mMainContentView = new MainContentView(this, rootView, this);
        LogUtils.d(TAG, "initMainContentView: 核心内容视图已初始化");
    }

    private void initCriticalView() {
        LogUtils.d(TAG, "initCriticalView() 调用");
        sMainActivity = this;
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (mToolbar != null) {
            mToolbar.setTitleTextAppearance(this, R.style.Toolbar_TitleText);
            LogUtils.d(TAG, "initCriticalView: 工具栏已设置标题样式");
        }
        mAdsViewStub = findViewById(R.id.stub_ads_banner);
        LogUtils.d(TAG, "initCriticalView: 广告ViewStub已获取");
    }

    private void initCoreUtilsAsync() {
        LogUtils.d(TAG, "initCoreUtilsAsync() 调用");
        new Thread(new Runnable() {
				@Override
				public void run() {
					LogUtils.d(TAG, "initCoreUtilsAsync: 异步线程启动 | threadId: " + Thread.currentThread().getId());
					mApplication = (App) getApplication();
					mAppConfigUtils = AppConfigUtils.getInstance(getApplicationContext());
					mBgSourceUtils = BackgroundSourceUtils.getInstance(getActivity());

					// 初始化服务控制配置
					mServiceControlBean = ControlCenterServiceBean.loadBean(getApplicationContext(), ControlCenterServiceBean.class);
					if (mServiceControlBean == null) {
						mServiceControlBean = new ControlCenterServiceBean(false);
						ControlCenterServiceBean.saveBean(getApplicationContext(), mServiceControlBean);
						LogUtils.d(TAG, "initCoreUtilsAsync: 服务配置不存在，已创建默认配置");
					}

					// 根据配置启停服务
					final boolean isServiceEnable = mServiceControlBean.isEnableService();
					final boolean isServiceAlive = ServiceUtils.isServiceAlive(getApplicationContext(), ControlCenterService.class.getName());
					LogUtils.d(TAG, "initCoreUtilsAsync: 服务配置状态 | isServiceEnable: " + isServiceEnable + " | isServiceAlive: " + isServiceAlive);

					if (isServiceEnable && !isServiceAlive) {
						runOnUiThread(new Runnable() {
								@Override
								public void run() {
									ControlCenterService.startControlCenterService(getApplicationContext());
									LogUtils.d(TAG, "initCoreUtilsAsync: 服务已启动");
								}
							});
					} else if (!isServiceEnable && isServiceAlive) {
						runOnUiThread(new Runnable() {
								@Override
								public void run() {
									ControlCenterService.stopControlCenterService(getApplicationContext());
									LogUtils.d(TAG, "initCoreUtilsAsync: 服务已停止");
								}
							});
					}

					// 主线程更新UI
					runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (isFinishing() || isDestroyed()) {
									LogUtils.w(TAG, "initCoreUtilsAsync: Activity已销毁，跳过UI更新");
									return;
								}
								// 适配API30，兼容低版本Drawable加载
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
									mFrameDrawable = getResources().getDrawable(R.drawable.bg_frame, getTheme());
								} else {
									mFrameDrawable = getResources().getDrawable(R.drawable.bg_frame);
								}
								updateViewData();
								sGlobalHandler.sendEmptyMessage(MSG_LOAD_BACKGROUND);
								sGlobalHandler.sendEmptyMessage(MSG_UPDATE_SERVICE_SWITCH);
								LogUtils.d(TAG, "initCoreUtilsAsync: UI更新消息已发送");
							}
						});
				}
			}).start();
    }

    private void loadNonCriticalViewDelayed() {
        LogUtils.d(TAG, "loadNonCriticalViewDelayed() 调用 | 延迟时长: " + DELAY_LOAD_NON_CRITICAL + "ms");
        new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					if (isFinishing() || isDestroyed()) {
						LogUtils.w(TAG, "loadNonCriticalViewDelayed: Activity已销毁，跳过广告加载");
						return;
					}
					loadAdsView();
				}
			}, DELAY_LOAD_NON_CRITICAL);
    }

    // ======================== 视图操作方法区 ========================
    private void handleReloadBackgroundParam(Intent intent) {
        LogUtils.d(TAG, "handleReloadBackgroundParam() 调用 | intent: " + intent);
        if (intent == null) {
            LogUtils.d(TAG, "handleReloadBackgroundParam: Intent 为空");
            return;
        }

		boolean isReloadAccentColor = intent.getBooleanExtra(EXTRA_ISRELOAD_ACCENTCOLOR, false);
        if (isReloadAccentColor) {
			App.sBackgroundSourceUtils.getCurrentBackgroundBean().setPixelColor(ImageUtils.getColorAccent(this));
			App.sBackgroundSourceUtils.saveSettings();
        }

        boolean isReloadBackgroundView = intent.getBooleanExtra(EXTRA_ISRELOAD_BACKGROUNDVIEW, false);
        if (isReloadBackgroundView) {
            LogUtils.d(TAG, "handleReloadBackgroundParam: 接收到刷新背景视图指令");
            reloadBackgroundView();
        }
    }

    private void reloadBackgroundView() {
        LogUtils.d(TAG, "reloadBackgroundView() 调用");
        mMainContentView.reloadBackgroundView();
    }

    private void loadAdsView() {
        LogUtils.d(TAG, "loadAdsView() 调用");
        if (mAdsViewStub == null) {
            LogUtils.e(TAG, "loadAdsView: 广告ViewStub为空，加载失败");
            return;
        }
        if (mADsBannerView == null) {
            View adsView = mAdsViewStub.inflate();
            mADsBannerView = adsView.findViewById(R.id.adsbanner);
            LogUtils.d(TAG, "loadAdsView: 广告视图已加载");
        } else {
            LogUtils.d(TAG, "loadAdsView: 广告视图已存在，无需重复加载");
        }
    }

    private void updateViewData() {
        LogUtils.d(TAG, "updateViewData() 调用");
        if (mMainContentView == null || mFrameDrawable == null) {
            LogUtils.e(TAG, "updateViewData: 核心视图或框架背景为空，更新失败");
            return;
        }
        mMainContentView.updateViewData(mFrameDrawable);
        LogUtils.d(TAG, "updateViewData: 视图数据已更新");
    }

	void updateBatteryDrawable() {
		BatteryStyle batteryStyle = BatteryStyleView.getSavedBatteryStyle(this);
		mMainContentView.updateBatteryDrawable(batteryStyle);
	}

	public static void sendUpdateBatteryDrawableMessage() {
		if (sGlobalHandler != null) {
			sGlobalHandler.sendEmptyMessage(MSG_UPDATE_BATTERYDRAWABLE);
		}
	}

    private void reloadBackground() {
        LogUtils.d(TAG, "reloadBackground() 调用");
        if (mMainContentView == null || mBgSourceUtils == null) {
            LogUtils.e(TAG, "reloadBackground: 核心视图或背景工具类为空，加载失败");
            return;
        }
        BackgroundBean currentBgBean = mBgSourceUtils.getCurrentBackgroundBean();
        if (currentBgBean != null) {
            mMainContentView.backgroundView.loadByBackgroundBean(currentBgBean, true);
            LogUtils.d(TAG, "reloadBackground: 已加载自定义背景");
        } else {
            mMainContentView.backgroundView.setBackgroundResource(R.drawable.default_background);
            LogUtils.d(TAG, "reloadBackground: 已加载默认背景");
        }
    }

    private void updateServiceSwitchUI() {
        LogUtils.d(TAG, "updateServiceSwitchUI() 调用");
        if (mMainContentView == null || mServiceControlBean == null) {
            LogUtils.e(TAG, "updateServiceSwitchUI: 核心视图或服务配置为空，更新失败");
            return;
        }
        boolean configEnabled = mServiceControlBean.isEnableService();
        mMainContentView.setServiceSwitchEnabled(false);
        mMainContentView.setServiceSwitchChecked(configEnabled);
        mMainContentView.setServiceSwitchEnabled(true);
        LogUtils.d(TAG, "updateServiceSwitchUI: 服务开关已更新 | 状态: " + configEnabled);
    }

    // ======================== 服务与线程管理方法区 ========================
    private void toggleServiceEnableState(boolean isEnable) {
        LogUtils.d(TAG, "toggleServiceEnableState() 调用 | 目标状态: " + isEnable);
        if (mServiceControlBean == null) {
            LogUtils.e(TAG, "toggleServiceEnableState: 服务配置为空，切换失败");
            return;
        }
        mServiceControlBean.setIsEnableService(isEnable);
        ControlCenterServiceBean.saveBean(getApplicationContext(), mServiceControlBean);
        LogUtils.d(TAG, "toggleServiceEnableState: 服务配置已保存");

        // UI开关联动服务启停
        if (isEnable) {
            if (!ServiceUtils.isServiceAlive(getApplicationContext(), ControlCenterService.class.getName())) {
                ControlCenterService.startControlCenterService(getApplicationContext());
                LogUtils.d(TAG, "toggleServiceEnableState: 服务已启动");
            }
        } else {
            ControlCenterService.stopControlCenterService(getApplicationContext());
            LogUtils.d(TAG, "toggleServiceEnableState: 服务已停止");
        }

        sGlobalHandler.sendEmptyMessage(MSG_UPDATE_SERVICE_SWITCH);
    }

    // ======================== 页面跳转方法区 ========================
    private void startAboutActivity() {
        LogUtils.d(TAG, "startAboutActivity() 调用");
        Intent aboutIntent = new Intent(getApplicationContext(), AboutActivity.class);
        APPInfo appInfo = genDefaultAppInfo();
        WinBoLLActivityManager.getInstance().startWinBoLLActivity(getApplicationContext(), aboutIntent, AboutActivity.class);
        LogUtils.d(TAG, "startAboutActivity: 关于页面已启动");
    }

    // ======================== 消息发送方法区 ========================
    private void notifyServiceAppConfigChange() {
        LogUtils.d(TAG, "notifyServiceAppConfigChange() 调用");
        ControlCenterService.sendAppConfigStatusUpdateMessage(this);
        reloadAppConfig();
        LogUtils.d(TAG, "notifyServiceAppConfigChange: 服务配置已通知更新");
    }

    public static void reloadAppConfig() {
        LogUtils.d(TAG, "reloadAppConfig() 调用");
        if (sGlobalHandler != null) {
            sGlobalHandler.sendEmptyMessage(MSG_RELOAD_APPCONFIG);
            LogUtils.d(TAG, "reloadAppConfig: 配置重载消息已发送");
        } else {
            LogUtils.w(TAG, "reloadAppConfig: 全局Handler为空，消息发送失败");
        }
    }

    public static void sendCurrentBatteryValueMessage(int value) {
        LogUtils.d(TAG, "sendCurrentBatteryValueMessage() 调用 | 电量: " + value);
        if (sGlobalHandler != null) {
            Message msg = sGlobalHandler.obtainMessage(MSG_CURRENTVALUEBATTERY);
            msg.arg1 = value;
            sGlobalHandler.sendMessage(msg);
            LogUtils.d(TAG, "sendCurrentBatteryValueMessage: 电量消息已发送");
        } else {
            LogUtils.w(TAG, "sendCurrentBatteryValueMessage: 全局Handler为空，消息发送失败");
        }
    }

    // ======================== 辅助工具方法区 ========================
    private APPInfo genDefaultAppInfo() {
        LogUtils.d(TAG, "genDefaultAppInfo() 调用");
        String branchName = "powerbell";
        APPInfo appInfo = new APPInfo();
        appInfo.setAppName(getString(R.string.app_name));
        appInfo.setAppIcon(R.drawable.ic_launcher);
        appInfo.setAppDescription(getString(R.string.app_description));
        appInfo.setAppGitName("WinBoLL");
        appInfo.setAppGitOwner("Studio");
        appInfo.setAppGitAPPBranch(branchName);
        appInfo.setAppGitAPPSubProjectFolder(branchName);
        appInfo.setAppHomePage("https://www.winboll.cc/apks/index.php?project=PowerBell");
        appInfo.setAppAPKName("PowerBell");
        appInfo.setAppAPKFolderName("PowerBell");
        LogUtils.d(TAG, "genDefaultAppInfo: 应用信息已生成");
        return appInfo;
    }

    // ======================== MainContentView 事件回调区 ========================
    @Override
    public void onChargeReminderSwitchChanged(boolean isChecked) {
        LogUtils.d(TAG, "onChargeReminderSwitchChanged() 调用 | isChecked: " + isChecked);
        notifyServiceAppConfigChange();
    }

    @Override
    public void onUsageReminderSwitchChanged(boolean isChecked) {
        LogUtils.d(TAG, "onUsageReminderSwitchChanged() 调用 | isChecked: " + isChecked);
        notifyServiceAppConfigChange();
    }

    @Override
    public void onServiceSwitchChanged(boolean isChecked) {
        LogUtils.d(TAG, "onServiceSwitchChanged() 调用 | isChecked: " + isChecked);
        toggleServiceEnableState(isChecked);
    }

    @Override
    public void onChargeReminderProgressChanged(int progress) {
        LogUtils.d(TAG, "onChargeReminderProgressChanged() 调用 | progress: " + progress);
        notifyServiceAppConfigChange();
    }

    @Override
    public void onUsageReminderProgressChanged(int progress) {
        LogUtils.d(TAG, "onUsageReminderProgressChanged() 调用 | progress: " + progress);
        notifyServiceAppConfigChange();
    }
}

