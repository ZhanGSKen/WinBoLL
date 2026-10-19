package cc.winboll.studio.contacts;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telecom.TelecomManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import cc.winboll.studio.contacts.activities.SettingsActivity;
import cc.winboll.studio.contacts.activities.WinBollActivity;
import cc.winboll.studio.contacts.dun.Rules;
import cc.winboll.studio.contacts.fragments.CallLogFragment;
import cc.winboll.studio.contacts.fragments.ContactsFragment;
import cc.winboll.studio.contacts.fragments.LogFragment;
import cc.winboll.studio.contacts.model.MainServiceBean;
import cc.winboll.studio.contacts.services.MainService;
import cc.winboll.studio.contacts.utils.PermissionUtils;
import cc.winboll.studio.contacts.views.DunTemperatureView;
import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libaes.views.ADsBannerView;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.LogView;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/08/30 14:32
 * @Describe Contacts 主窗口（完全适配 API 30 + Java 7 语法）
 * 核心优化：1. 移除电话状态监听 2. 移除通话筛选服务 3. 移除 MainService 所有相关逻辑 4. ViewPager 实现 Fragment 懒加载（仅首屏初始化）
 * 问题修复：解决首屏 Fragment 空白问题（删除 setPrimaryItem 冲突逻辑+延迟首屏初始化）
 */
public final class MainActivity extends WinBollActivity implements IWinBoLLActivity, ViewPager.OnPageChangeListener, View.OnClickListener {

    // ====================== 1. 常量定义区（硬编码API版本，避免高版本依赖） ======================
    public static final String TAG = "MainActivity";
    public static final int REQUEST_HOME_ACTIVITY = 0;
    public static final int REQUEST_ABOUT_ACTIVITY = 1;
    public static final int REQUEST_APP_SETTINGS = 2;
    public static final String ACTION_SOS = "cc.winboll.studio.libappbase.WinBoLL.ACTION_SOS";
    private static final int DIALER_REQUEST_CODE = 1;
    private static final int REQUEST_REQUIRED_PERMISSIONS = 1002;
    private static final int REQUEST_OVERLAY_PERMISSION = 1003;

    // API版本硬编码常量（Java 7兼容，杜绝Build.VERSION_CODES高版本引用）
    private static final int ANDROID_6_API = 23;
    private static final int ANDROID_8_API = 26;
    private static final int ANDROID_10_API = 29;
    private static final int ANDROID_14_API = 34;

    // ====================== 2. 静态成员区 ======================
    static MainActivity _MainActivity;

    // ====================== 3. 权限常量区 ======================
    private final String[] REQUIRED_PERMISSIONS = PermissionUtils.BASE_PERMISSIONS;

    // ====================== 4. UI控件成员区 ======================
    private ADsBannerView mADsBannerView;
    private LogView mLogView;
    private Toolbar mToolbar;
    private CheckBox cbMainService;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private List<View> views;
    private ImageView[] imageViews;
    private LinearLayout linearLayout;

    // ====================== 5. 业务逻辑成员区 ======================
    private int currentPoint = 0;
    private List<Fragment> fragmentList;
    private List<String> tabTitleList;
    // 记录已初始化的Fragment位置（避免重复初始化）
    private boolean[] isFragmentInit;

    // ====================== 6. 接口实现区 ======================
    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    // ====================== 7. 生命周期函数区 ======================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "===== onCreate: 主Activity开始创建 =====");
        _MainActivity = this;

        // 直接初始化UI（原权限检查逻辑注释保留，按需启用）
        initUIAndLogic(savedInstanceState);

		MainServiceBean mainServiceBean = MainServiceBean.loadBean(this, MainServiceBean.class);
		if (mainServiceBean != null && mainServiceBean.isEnable()) {
			Intent intent = new Intent(this, MainService.class);
			// 根据应用前后台状态选择启动方式（Android 12+ 后台用 startForegroundService）
			if (Build.VERSION.SDK_INT >= 31) {
				startForegroundService(intent);
			} else {
				startService(intent);
			}
		}
        LogUtils.d(TAG, "===== onCreate: 主Activity创建流程结束 =====");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        LogUtils.d(TAG, "onPostCreate: 主Activity创建完成");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mADsBannerView != null) {
            mADsBannerView.resumeADs(MainActivity.this);
            LogUtils.d(TAG, "onResume: 广告栏资源已恢复");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "===== onDestroy: 主Activity开始销毁 =====");
        // 释放广告资源
        if (mADsBannerView != null) {
            mADsBannerView.releaseAdResources();
            LogUtils.d(TAG, "onDestroy: 广告栏资源已释放");
        }
        // 清空Fragment相关引用，避免内存泄漏
        if (fragmentList != null) {
            fragmentList.clear();
            fragmentList = null;
        }
        if (tabTitleList != null) {
            tabTitleList.clear();
            tabTitleList = null;
        }
        isFragmentInit = null;
        LogUtils.d(TAG, "===== onDestroy: 主Activity销毁完成 =====");
    }

    // ====================== 8. 权限相关回调函数区 ======================
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LogUtils.d(TAG, "onRequestPermissionsResult: 权限请求回调，requestCode=" + requestCode);

        if (requestCode == REQUEST_REQUIRED_PERMISSIONS) {
            String deniedPerms = PermissionUtils.getDeniedPermissions(this, permissions);
            if (deniedPerms.length() == 0) {
                LogUtils.d(TAG, "onRequestPermissionsResult: 所有危险权限授予成功");
                checkAndRequestRemainingPermissions();
            } else {
                LogUtils.e(TAG, "onRequestPermissionsResult: 被拒权限：" + deniedPerms);
                showPermissionDeniedDialogAndExit("应用需要「" + deniedPerms + "」权限才能正常运行，请授予权限后重新打开应用。");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.d(TAG, "onActivityResult: 页面回调触发，requestCode=" + requestCode + "，resultCode=" + resultCode);

        switch (requestCode) {
            case DIALER_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    LogUtils.d(TAG, "onActivityResult: 设为默认拨号应用成功");
                    Toast.makeText(MainActivity.this, getString(R.string.app_name) + " 已成为默认电话应用", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_APP_SETTINGS:
                LogUtils.d(TAG, "onActivityResult: 从设置页返回，重建Activity");
                recreate();
                break;
            case REQUEST_OVERLAY_PERMISSION:
                handleOverlayPermissionResult();
                break;
            default:
                LogUtils.w(TAG, "onActivityResult: 未知requestCode=" + requestCode);
                break;
        }
    }

    /**
     * 处理悬浮窗权限申请结果
     */
    private void handleOverlayPermissionResult() {
        if (PermissionUtils.isOverlayPermissionGranted(this)) {
            LogUtils.d(TAG, "handleOverlayPermissionResult: 悬浮窗权限申请成功");
            LogUtils.d(TAG, "handleOverlayPermissionResult: 所有权限已授予");
            initUIAndLogic(null);
        } else {
            LogUtils.e(TAG, "handleOverlayPermissionResult: 悬浮窗权限申请失败");
            showPermissionDeniedDialogAndExit("应用需要悬浮窗权限才能展示来电弹窗，请授予后重新打开应用。");
        }
    }

    /**
     * 检查并申请剩余权限（仅保留悬浮窗）
     */
    private void checkAndRequestRemainingPermissions() {
        if (!PermissionUtils.isOverlayPermissionGranted(this)) {
            LogUtils.d(TAG, "checkAndRequestRemainingPermissions: 悬浮窗权限未授予，跳转设置页");
            PermissionUtils.requestOverlayPermission(this, REQUEST_OVERLAY_PERMISSION);
        } else {
            LogUtils.d(TAG, "checkAndRequestRemainingPermissions: 所有权限已授予");
            initUIAndLogic(null);
        }
    }

    /**
     * 权限拒绝提示对话框（Java 7 匿名内部类实现，禁止Lambda）
     */
    private void showPermissionDeniedDialogAndExit(String tip) {
        LogUtils.d(TAG, "showPermissionDeniedDialogAndExit: 弹出权限不足提示框");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("权限不足，无法使用");
        builder.setMessage(tip);
        builder.setCancelable(false);

        builder.setNegativeButton("去设置", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					LogUtils.d(TAG, "showPermissionDeniedDialogAndExit: 用户选择去设置权限");
					PermissionUtils.goAppDetailsSettings(MainActivity.this);
				}
			});

        builder.setPositiveButton("确定退出", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					LogUtils.d(TAG, "showPermissionDeniedDialogAndExit: 用户选择退出应用");
					finishAndRemoveTask();
				}
			});

        builder.show();
    }

    // ====================== 9. UI与业务逻辑初始化区 ======================
    private void initUIAndLogic(Bundle savedInstanceState) {
        if (mToolbar != null) {
            LogUtils.d(TAG, "initUIAndLogic: UI已初始化，无需重复执行");
            return;
        }

        LogUtils.d(TAG, "===== initUIAndLogic: 开始初始化UI与业务逻辑 =====");
        setContentView(R.layout.activity_main);

        // 1. 工具栏初始化
        mToolbar = (Toolbar) findViewById(R.id.activitymainToolbar1);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setSubtitle(TAG);
        LogUtils.d(TAG, "initUIAndLogic: 工具栏初始化完成");

        // 2. TabLayout与ViewPager初始化
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        initViewPagerAndTabs();
        tabLayout.setupWithViewPager(viewPager);
        LogUtils.d(TAG, "initUIAndLogic: ViewPager与TabLayout初始化完成");

        // 3. 广告栏初始化
        mADsBannerView = (ADsBannerView) findViewById(R.id.adsbanner);
        LogUtils.d(TAG, "initUIAndLogic: 广告栏控件初始化完成");

        // 左边盾值视图初始化（Java7分步写法，禁止链式调用）
        DunTemperatureView tempViewLeft = (DunTemperatureView) findViewById(R.id.dun_temp_view_left);
        tempViewLeft.setMaxValue(Rules.getInstance(this).getSettingsModel().getDunTotalCount());
        tempViewLeft.setCurrentValue(Rules.getInstance(this).getSettingsModel().getDunCurrentCount());

        int[] customColors = new int[2];
        customColors[0] = Color.parseColor("#FF3366FF");
        customColors[1] = Color.parseColor("#FF9900CC");
        float[] positions = new float[2];
        positions[0] = 0.0f;
        positions[1] = 1.0f;
        tempViewLeft.setGradientColors(customColors, positions);
        // 文本放在温度条右侧（默认，可省略）
        tempViewLeft.setTextPosition(true);
        // 右边盾值视图初始化（Java7分步写法，禁止链式调用）
        DunTemperatureView tempViewRight = (DunTemperatureView) findViewById(R.id.dun_temp_view_right);
        tempViewRight.setMaxValue(Rules.getInstance(this).getSettingsModel().getDunTotalCount());
        tempViewRight.setCurrentValue(Rules.getInstance(this).getSettingsModel().getDunCurrentCount());

        tempViewRight.setGradientColors(customColors, positions);
        // 文本放在温度条左侧
        tempViewRight.setTextPosition(false);
        LogUtils.d(TAG, "initUIAndLogic: 盾值视图初始化完成");
        LogUtils.d(TAG, "===== initUIAndLogic: 初始化流程全部结束 =====");
    }

    /**
     * 初始化ViewPager与Tab数据（Java7规范，泛型完整声明），添加懒加载标记
     * 关键修改：延迟50ms初始化首屏，确保Fragment控件就绪；删除setPrimaryItem冲突逻辑
     */
    private void initViewPagerAndTabs() {
        LogUtils.d(TAG, "initViewPagerAndTabs: 开始初始化ViewPager数据");
        fragmentList = new ArrayList<Fragment>();
        tabTitleList = new ArrayList<String>();

        // 添加Fragment实例（仅创建对象，不初始化业务逻辑）
        fragmentList.add(CallLogFragment.newInstance(0));
        fragmentList.add(ContactsFragment.newInstance(1));
        fragmentList.add(LogFragment.newInstance(2));
        tabTitleList.add("通话记录");
        tabTitleList.add("联系人");
        tabTitleList.add("应用日志");

        // 初始化懒加载标记数组（默认均未初始化）
        int fragmentCount = fragmentList.size();
        isFragmentInit = new boolean[fragmentCount];
        for (int i = 0; i < fragmentCount; i++) {
            isFragmentInit[i] = false;
        }

        // 设置自定义适配器（已删除setPrimaryItem，避免初始化冲突）
        LazyLoadPagerAdapter adapter = new LazyLoadPagerAdapter(getSupportFragmentManager(), fragmentList, tabTitleList);
        viewPager.setAdapter(adapter);
        // 关闭预加载（设为0仅加载当前页，关键）
        viewPager.setOffscreenPageLimit(0);
        viewPager.addOnPageChangeListener(this);

        // 关键优化：延迟50ms初始化首屏（确保Fragment已完成onCreateView，控件绑定就绪）
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					initFragmentByPosition(0);
					LogUtils.d(TAG, "initViewPagerAndTabs: 延迟初始化首屏Fragment，位置=0");
				}
			}, 50);

        LogUtils.d(TAG, "initViewPagerAndTabs: ViewPager初始化完成，等待延迟初始化首屏");
    }

    /**
     * 根据位置初始化Fragment（调用Fragment的初始化逻辑，避免重复执行）
     * 优化：添加isAdded判断，确保Fragment已附加到Activity，防止上下文空指针
     */
    private void initFragmentByPosition(int position) {
        // 校验位置合法性 + 避免重复初始化 + 确保Fragment已附加到Activity
        if (position < 0 || position >= fragmentList.size() || isFragmentInit[position]) {
            return;
        }
        Fragment targetFragment = fragmentList.get(position);
        if (targetFragment != null && targetFragment.isAdded()) {
            // 触发Fragment初始化（调用各Fragment的initData方法）
            if (targetFragment instanceof CallLogFragment) {
                ((CallLogFragment) targetFragment).initData();
            } else if (targetFragment instanceof ContactsFragment) {
                ((ContactsFragment) targetFragment).initData();
            } else if (targetFragment instanceof LogFragment) {
                ((LogFragment) targetFragment).initData();
            }
            // 标记为已初始化
            isFragmentInit[position] = true;
            LogUtils.d(TAG, "initFragmentByPosition: 初始化Fragment，位置=" + position + "，标题=" + tabTitleList.get(position));
        } else {
            LogUtils.w(TAG, "initFragmentByPosition: Fragment未附加到Activity/实例为空，位置=" + position);
        }
    }

    // ====================== 10. 菜单相关函数区 ======================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        LogUtils.d(TAG, "onCreateOptionsMenu: 菜单加载完成");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_settings) {
            LogUtils.d(TAG, "onOptionsItemSelected: 用户点击设置菜单");
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ====================== 11. ViewPager页面回调区（切换时初始化对应Fragment） ======================
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) {
        currentPoint = position;
        LogUtils.d(TAG, "onPageSelected: 页面切换至[" + position + "]，标题=" + tabTitleList.get(position));
        // 切换页面时，初始化当前页Fragment（未初始化过才执行）
        initFragmentByPosition(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Override
    public void onClick(View v) {}

    // ====================== 12. 工具函数区 ======================
    /**
     * 拨号工具方法（添加空指针防护）
     */
    public static void dialPhoneNumber(String phoneNumber) {
        if (_MainActivity == null) {
            LogUtils.e(TAG, "dialPhoneNumber: MainActivity实例为空，无法拨号");
            return;
        }
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            LogUtils.e(TAG, "dialPhoneNumber: 拨号号码为空");
            return;
        }
        if (PermissionUtils.checkPermission(_MainActivity, Manifest.permission.CALL_PHONE)) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            LogUtils.d(TAG, "dialPhoneNumber: 发起拨号，号码=" + phoneNumber);
            _MainActivity.startActivity(intent);
        } else {
            LogUtils.e(TAG, "dialPhoneNumber: 拨号权限不足，无法发起拨号");
            Toast.makeText(_MainActivity, "拨号权限不足", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 判断是否为默认拨号应用（适配API30，硬编码版本判断）
     */
    public boolean isDefaultPhoneCallApp() {
        if (Build.VERSION.SDK_INT >= ANDROID_6_API) {
            TelecomManager manager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            if (manager != null && manager.getDefaultDialerPackage() != null) {
                boolean isDefault = manager.getDefaultDialerPackage().equals(getPackageName());
                LogUtils.d(TAG, "isDefaultPhoneCallApp: 是否为默认拨号应用=" + isDefault);
                return isDefault;
            }
        }
        LogUtils.d(TAG, "isDefaultPhoneCallApp: 系统版本低于Android 6，无法判断");
        return false;
    }

    /**
     * 检查服务是否正在运行（通用工具方法，添加空指针防护）
     */
    public boolean isServiceRunning(Class<?> serviceClass) {
        if (serviceClass == null) {
            LogUtils.e(TAG, "isServiceRunning: 服务类参数为null");
            return false;
        }
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) {
            LogUtils.w(TAG, "isServiceRunning: ActivityManager获取失败");
            return false;
        }

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                LogUtils.d(TAG, "isServiceRunning: 服务[" + serviceClass.getSimpleName() + "]正在运行");
                return true;
            }
        }
        LogUtils.d(TAG, "isServiceRunning: 服务[" + serviceClass.getSimpleName() + "]未运行");
        return false;
    }

    // ====================== 13. 内部类定义区（Java 7 规范，禁止Lambda） ======================
    /**
     * 自定义懒加载ViewPager适配器（删除setPrimaryItem方法，解决首屏初始化冲突）
     */
    private class LazyLoadPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList;
        private final List<String> tabTitleList;

        public LazyLoadPagerAdapter(FragmentManager fm, List<Fragment> fragmentList, List<String> tabTitleList) {
            super(fm);
            this.fragmentList = fragmentList;
            this.tabTitleList = tabTitleList;
            LogUtils.d(MainActivity.TAG, "LazyLoadPagerAdapter: 初始化完成，Fragment数量=" + fragmentList.size());
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitleList.get(position);
        }

        // 【已删除】移除setPrimaryItem方法，避免与手动初始化+onPageSelected回调冲突
    }
}

