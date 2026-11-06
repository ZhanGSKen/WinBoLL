package cc.winboll.studio.winboll;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import androidx.fragment.app.Fragment;
import cc.winboll.studio.libaes.activitys.DrawerFragmentActivity;
import cc.winboll.studio.libaes.models.DrawerMenuBean;
import cc.winboll.studio.libaes.utils.AESThemeUtil;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.winboll.R;
import cc.winboll.studio.winboll.activities.AboutActivity;
import cc.winboll.studio.winboll.activities.SettingsActivity;
import cc.winboll.studio.winboll.termux.MyTermuxActivity;
import cc.winboll.studio.winboll.fragments.ManagementFragment;
import cc.winboll.studio.winboll.fragments.OpenSourceLicenseFragment;
import cc.winboll.studio.winboll.fragments.WWWBrowserFragment;
import cc.winboll.studio.winboll.push.PushNotifier;
import cc.winboll.studio.winboll.services.PushService;
import cc.winboll.studio.winboll.unittest.TermuxEnvTestActivity;
import cc.winboll.studio.winboll.utils.CertTrustUtils;
import java.util.ArrayList;

public class MainActivity extends DrawerFragmentActivity {


    public static final String TAG = "MainActivity";

    // 网站管理窗口 Fragment（启动时不创建，点击"网站管理/HOME"菜单时懒加载）
    ManagementFragment mManagementFragment;
    // 浏览器启动窗口 Fragment（启动时默认创建并显示）
    WWWBrowserFragment mWWWBrowserFragment;
    // 开源许可说明窗口 Fragment（启动时不创建，点击"开源许可"菜单时懒加载）
    OpenSourceLicenseFragment mOpenSourceLicenseFragment;

    // WinBoLL 根证书是否已安装并被信任；未安装时不启用"网站管理"菜单
    boolean mCertTrusted = false;

    // ------------------- 新增：Handler 消息定义（接收URL历史更新消息） -------------------
    // 消息标识：URL加载历史更新（刷新抽屉菜单的历史列表）
    public static final int MSG_URLLOADHISTORY_UPDATE = 1002;
    // 自定义Handler（接收应用内消息，如ManagementFragment发送的历史更新消息）
    private static Handler _mMainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(cc.winboll.studio.winboll.theme.WinBoLLThemeUtil.getThemeTypeID(getApplicationContext()));
        super.onCreate(savedInstanceState);
        initMainHandler();
        // 启动默认窗口为 WWWBrowser（浏览器窗口）。外部 ACTION_VIEW 传入的 URL 由 WWWBrowser 打开，
        // 无外部 URL 时加载 Settings 配置的自定义首页。
        if (mWWWBrowserFragment == null) {
            // 推送通知跳转的 URL 优先于外部 ACTION_VIEW 传入的 URL
            String initialUrl = extractPushUrl(getIntent());
            if (initialUrl == null) {
                initialUrl = extractExternalUrl(getIntent());
            }
            if (initialUrl != null) {
                mWWWBrowserFragment = WWWBrowserFragment.newInstance(initialUrl);
            } else {
                mWWWBrowserFragment = WWWBrowserFragment.newInstance();
            }
            addFragment(mWWWBrowserFragment);
        }
        showFragment(mWWWBrowserFragment);
        // 主窗口副标题跟随当前显示 Fragment 的标题
        updateToolbarSubtitle(mWWWBrowserFragment);
        // 其他窗口（网站管理 ManagementFragment）启动时不创建，点击对应菜单时才懒加载
        // 启动时检测 WinBoLL 根证书是否已安装，决定是否启用"网站管理"菜单
        refreshCertTrustedState();
        // 启动消息推送前台服务（自建 WSS 通道，收通知帧发系统通知）
        PushService.start(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 仅处理推送通知跳转；ACTION_VIEW 外部链接刻意只在 onCreate 生效（见下方 extractExternalUrl）。
        if (intent != null && PushNotifier.ACTION_PUSH_OPEN.equals(intent.getAction())) {
            String url = intent.getStringExtra(PushNotifier.EXTRA_PUSH_URL);
            if (url != null && mWWWBrowserFragment != null) {
                mWWWBrowserFragment.navigateTo(url);
                showFragment(mWWWBrowserFragment);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从 Settings 安装证书返回后，重新检测并刷新菜单
        refreshCertTrustedState();
        // 回前台立即检查推送连接并重连（不必等退避结束，推送文档 §五）
        PushService.reconnect(this);
    }

    /**
     * 后台线程检测 WinBoLL 根证书是否已被信任，完成后回主线程刷新菜单。
     */
    private void refreshCertTrustedState() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean trusted = CertTrustUtils.isWinBoLLRootTrusted();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCertTrusted = trusted;
                        invalidateOptionsMenu();
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // 仅在已安装 WinBoLL 根证书时启用"网站管理"菜单
        MenuItem manageItem = menu.findItem(R.id.item_website_manage);
        if (manageItem != null) {
            manageItem.setVisible(mCertTrusted);
        }
        return true;
    }

    /**
     * 显示 WWWBrowser 窗口 Fragment（启动时已创建，点击菜单直接切换显示）。
     * 再次点击仍停留在 WWWBrowser 窗口；需要 ManagementFragment 时通过"网站管理/HOME"菜单切换。
     */
    private void showWWWBrowser() {
        if (mWWWBrowserFragment == null) {
            mWWWBrowserFragment = WWWBrowserFragment.newInstance();
            addFragment(mWWWBrowserFragment);
        }
        showFragment(mWWWBrowserFragment);
        updateToolbarSubtitle(mWWWBrowserFragment);
    }

    /**
     * 显示网站管理（ManagementFragment）窗口，副标题跟随其标题。
     * 启动时不创建，首次点击菜单时懒加载（Fragment 创建时自动加载首页）。
     */
    private void showWebsiteManage() {
        if (mManagementFragment == null) {
            mManagementFragment = ManagementFragment.newInstance();
            addFragment(mManagementFragment);
        }
        showFragment(mManagementFragment);
        updateToolbarSubtitle(mManagementFragment);
    }

    /**
     * 显示开源许可说明（OpenSourceLicenseFragment）窗口，副标题跟随其标题。
     * 启动时不创建，首次点击菜单时懒加载。
     */
    private void showOpenSourceLicense() {
        if (mOpenSourceLicenseFragment == null) {
            mOpenSourceLicenseFragment = OpenSourceLicenseFragment.newInstance();
            addFragment(mOpenSourceLicenseFragment);
        }
        showFragment(mOpenSourceLicenseFragment);
        updateToolbarSubtitle(mOpenSourceLicenseFragment);
    }

    /**
     * 主窗口副标题跟随当前显示的 Fragment 标题（Fragment 的标题就是副标题）。
     */
    private void updateToolbarSubtitle(Fragment fragment) {
        if (mToolbar == null) {
            return;
        }
        if (fragment instanceof ManagementFragment) {
            mToolbar.setSubtitle(((ManagementFragment) fragment).getFragmentTitle());
        } else if (fragment instanceof WWWBrowserFragment) {
            mToolbar.setSubtitle(((WWWBrowserFragment) fragment).getFragmentTitle());
        } else if (fragment instanceof OpenSourceLicenseFragment) {
            mToolbar.setSubtitle(((OpenSourceLicenseFragment) fragment).getFragmentTitle());
        } else {
            mToolbar.setSubtitle(null);
        }
    }

    @Override
    public void onBackPressed() {
        // 简易浏览器窗口显示时，优先由其处理 WebView 页面后退
        if (mWWWBrowserFragment != null && mWWWBrowserFragment.isVisible()
                && mWWWBrowserFragment.handleBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        String externalUrl = extractExternalUrl(intent);
//        if (externalUrl != null && mManagementFragment != null && mManagementFragment.getBrowserHandler() != null) {
//            Message msg = Message.obtain();
//            msg.what = ManagementFragment.MSG_OPEN_URL;
//            msg.obj = externalUrl;
//            mManagementFragment.getBrowserHandler().sendMessage(msg);
//        }
//    }

    private String extractExternalUrl(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri != null) {
                return uri.toString();
            }
        }
        return null;
    }

    /** 提取推送通知点击携带的目标 URL（应用内 WebView 打开）。 */
    private String extractPushUrl(Intent intent) {
        if (intent != null && PushNotifier.ACTION_PUSH_OPEN.equals(intent.getAction())) {
            return intent.getStringExtra(PushNotifier.EXTRA_PUSH_URL);
        }
        return null;
    }

	public static void sendMessage(Message msg) {
		if (_mMainHandler != null && msg != null) {
			_mMainHandler.sendMessage(msg);
		}
	}

    /**
     * 初始化Handler（接收MSG_URLLOADHISTORY_UPDATE消息，刷新抽屉历史菜单）
     */
    private void initMainHandler() {
		// 清理旧数据
		if (_mMainHandler != null) {
            _mMainHandler.removeCallbacksAndMessages(null);
            _mMainHandler = null;
        }

        // Java 7 匿名内部类实现Handler（主线程创建，安全更新UI/抽屉菜单）
        _mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_URLLOADHISTORY_UPDATE:
                        // 处理URL历史更新消息：刷新抽屉菜单的历史列表
                        LogUtils.d(TAG, "收到URL历史更新消息，刷新抽屉菜单");
                        refreshUrlHistoryDrawerMenu();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public void initDrawerMenuItemList(ArrayList<DrawerMenuBean> listDrawerMenu) {
        super.initDrawerMenuItemList(listDrawerMenu);
        //LogUtils.d(TAG, "initDrawerMenuItemList");
        // 加载URL历史菜单（初始化时加载）
        refreshUrlHistoryDrawerMenu();
        notifyDrawerMenuDataChanged();
    }

    @Override
    public void reinitDrawerMenuItemList(ArrayList<DrawerMenuBean> listDrawerMenu) {
        super.reinitDrawerMenuItemList(listDrawerMenu);
        //LogUtils.d(TAG, "reinitDrawerMenuItemList");
        // 重新加载URL历史菜单（菜单重置时加载）
        refreshUrlHistoryDrawerMenu();
        notifyDrawerMenuDataChanged();
    }

	void loadUrlLoadHistotyMenu(ArrayList<DrawerMenuBean> listDrawerMenu) {
		listDrawerMenu.clear();
		if (mManagementFragment != null && mManagementFragment.getUrlLoadHistory() != null) {
			for (String url : mManagementFragment.getUrlLoadHistory()) {
				listDrawerMenu.add(new DrawerMenuBean(R.drawable.ic_launcher, url));
			}
		}
	}

    // ------------------- 新增：刷新URL历史抽屉菜单（提取独立方法，复用） -------------------
    private void refreshUrlHistoryDrawerMenu() {
        // 获取抽屉菜单列表，重新加载历史数据并刷新
        ArrayList<DrawerMenuBean> drawerMenuList = super.malDrawerMenuItem; // 假设父类提供获取菜单列表的方法
        if (drawerMenuList != null) {
            loadUrlLoadHistotyMenu(drawerMenuList); // 重新加载更新后的历史数据
            notifyDrawerMenuDataChanged(); // 通知抽屉菜单刷新UI
        }
    }

    @Override
    public DrawerFragmentActivity.ActivityType initActivityType() {
        return DrawerFragmentActivity.ActivityType.Main;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		if (App.isDebugging()) {
			getMenuInflater().inflate(R.menu.toolbar_test, menu);
		}
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
		return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);
		if (mManagementFragment != null && mManagementFragment.getBrowserHandler() != null) {
			// 历史项属于网站管理窗口，先切换显示该窗口再打开对应历史 URL
			showWebsiteManage();
			Message msg = Message.obtain();
			msg.what = ManagementFragment.MSG_HISTORY_POSITION;
			msg.obj = position;
			mManagementFragment.getBrowserHandler().sendMessage(msg);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (AESThemeUtil.onWinBoLLThemeItemSelected(this, item)) {
            recreate();
        } else {
            int nItemId = item.getItemId();
            if (nItemId == R.id.item_home) {
                // HOME：切换到 WWWBrowser 窗口并打开其默认主页（Settings 自定义首页）
                showWWWBrowser();
                if (mWWWBrowserFragment != null) {
                    mWWWBrowserFragment.loadHomePage();
                }
            } else if (nItemId == R.id.item_www_browser) {
                // 点击显示 WWWBrowser 窗口（再次点击仍停留，不切回）
                showWWWBrowser();
            } else if (nItemId == R.id.item_website_manage) {
                // 点击显示网站管理（ManagementFragment）
                showWebsiteManage();
            } else if (nItemId == R.id.item_settings) {
                WinBoLLActivityManager.getInstance().startWinBoLLActivity(getApplicationContext(), SettingsActivity.class);
            } else if (nItemId == R.id.item_open_source) {
                // 点击显示开源许可说明窗口
                showOpenSourceLicense();
            } else if (nItemId == R.id.item_about) {
                Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
                WinBoLLActivityManager.getInstance().startWinBoLLActivity(getApplicationContext(), intent, AboutActivity.class);
            } else if (nItemId == R.id.item_mytermux) {
                Intent intent = new Intent(getApplicationContext(), MyTermuxActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (nItemId == R.id.item_termux_env_test) {
                Intent intent = new Intent(getApplicationContext(), TermuxEnvTestActivity.class);
                WinBoLLActivityManager.getInstance().startWinBoLLActivity(getApplicationContext(), intent, AboutActivity.class);

            } else {
                return super.onOptionsItemSelected(item);
            }
        }
        return true;
    }

    // ------------------- 新增：对外提供Handler（供其他组件发送消息，如ManagementFragment） -------------------
    public Handler getMainHandler() {
        return _mMainHandler;
    }

    // ------------------- 新增：生命周期管理（防止Handler内存泄漏） -------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清除Handler所有消息和回调，避免内存泄漏
        if (_mMainHandler != null) {
            _mMainHandler.removeCallbacksAndMessages(null);
            _mMainHandler = null;
        }
    }
}

