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
import cc.winboll.studio.winboll.fragments.SimpleBrowserFragment;
import cc.winboll.studio.winboll.unittest.TermuxEnvTestActivity;
import java.util.ArrayList;

public class MainActivity extends DrawerFragmentActivity {


    public static final String TAG = "MainActivity";

    ManagementFragment mManagementFragment;
    // 简易浏览器启动窗口 Fragment
    SimpleBrowserFragment mSimpleBrowserFragment;

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
        if (mManagementFragment == null) {
            String externalUrl = extractExternalUrl(getIntent());
            if (externalUrl != null) {
                mManagementFragment = ManagementFragment.newInstance(externalUrl);
            } else {
                mManagementFragment = ManagementFragment.newInstance();
            }
            addFragment(mManagementFragment);
        }
        // 组装简易浏览器启动窗口 Fragment（保持 DrawerFragmentActivity 架构不变）
        // 启动时不显示，由主菜单点击后懒加载切换
        showFragment(mManagementFragment);
        // 主窗口副标题跟随当前显示 Fragment 的标题
        updateToolbarSubtitle(mManagementFragment);
    }

    /**
     * 切换显示简易浏览器窗口 Fragment（首次点击时懒加载 addFragment）。
     * 已在简易窗口时再次点击，则切回完整浏览器。
     */
    private void toggleSimpleBrowser() {
        if (mSimpleBrowserFragment == null) {
            mSimpleBrowserFragment = SimpleBrowserFragment.newInstance();
            addFragment(mSimpleBrowserFragment);
            showFragment(mSimpleBrowserFragment);
            updateToolbarSubtitle(mSimpleBrowserFragment);
        } else if (mSimpleBrowserFragment.isVisible()) {
            showFragment(mManagementFragment);
            updateToolbarSubtitle(mManagementFragment);
        } else {
            showFragment(mSimpleBrowserFragment);
            updateToolbarSubtitle(mSimpleBrowserFragment);
        }
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
        } else if (fragment instanceof SimpleBrowserFragment) {
            mToolbar.setSubtitle(((SimpleBrowserFragment) fragment).getFragmentTitle());
        } else {
            mToolbar.setSubtitle(null);
        }
    }

    @Override
    public void onBackPressed() {
        // 简易浏览器窗口显示时，优先由其处理 WebView 页面后退
        if (mSimpleBrowserFragment != null && mSimpleBrowserFragment.isVisible()
                && mSimpleBrowserFragment.handleBackPressed()) {
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
                if (mManagementFragment != null && mManagementFragment.getBrowserHandler() != null) {
                    Message msg = Message.obtain();
                    msg.what = ManagementFragment.MSG_HOMEPAGE;
                    mManagementFragment.getBrowserHandler().sendMessage(msg);
                }
            } else if (nItemId == R.id.item_simple_browser) {
                // 点击切换显示简易浏览器窗口
                toggleSimpleBrowser();
            } else if (nItemId == R.id.item_settings) {
                WinBoLLActivityManager.getInstance().startWinBoLLActivity(getApplicationContext(), SettingsActivity.class);
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

