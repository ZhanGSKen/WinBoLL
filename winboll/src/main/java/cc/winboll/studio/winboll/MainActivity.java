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
import cc.winboll.studio.libaes.activitys.DrawerFragmentActivity;
import cc.winboll.studio.libaes.models.DrawerMenuBean;
import cc.winboll.studio.libaes.utils.AESThemeUtil;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.winboll.R;
import cc.winboll.studio.winboll.activities.AboutActivity;
import cc.winboll.studio.winboll.activities.SettingsActivity;
import cc.winboll.studio.winboll.applications.MyTermuxActivity;
import cc.winboll.studio.winboll.fragments.BrowserFragment;
import cc.winboll.studio.winboll.unittest.TermuxEnvTestActivity;
import java.util.ArrayList;

public class MainActivity extends DrawerFragmentActivity {


    public static final String TAG = "MainActivity";

    BrowserFragment mBrowserFragment;

    // ------------------- 新增：Handler 消息定义（接收URL历史更新消息） -------------------
    // 消息标识：URL加载历史更新（刷新抽屉菜单的历史列表）
    public static final int MSG_URLLOADHISTORY_UPDATE = 1002;
    // 自定义Handler（接收应用内消息，如BrowserFragment发送的历史更新消息）
    private static Handler _mMainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(cc.winboll.studio.winboll.theme.WinBoLLThemeUtil.getThemeTypeID(this));
        super.onCreate(savedInstanceState);
        initMainHandler();
        if (mBrowserFragment == null) {
            String externalUrl = extractExternalUrl(getIntent());
            if (externalUrl != null) {
                mBrowserFragment = BrowserFragment.newInstance(externalUrl);
            } else {
                mBrowserFragment = BrowserFragment.newInstance();
            }
            addFragment(mBrowserFragment);
        }
        showFragment(mBrowserFragment);
    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        String externalUrl = extractExternalUrl(intent);
//        if (externalUrl != null && mBrowserFragment != null && mBrowserFragment.getBrowserHandler() != null) {
//            Message msg = Message.obtain();
//            msg.what = BrowserFragment.MSG_OPEN_URL;
//            msg.obj = externalUrl;
//            mBrowserFragment.getBrowserHandler().sendMessage(msg);
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
		_mMainHandler.sendMessage(msg);
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
		if (BrowserFragment._mUrlLoadHistory != null) {
			for (String url : BrowserFragment._mUrlLoadHistory) {
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
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
		if (App.isDebugging()) {
			getMenuInflater().inflate(R.menu.toolbar_test, menu);
		}
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);
		if (mBrowserFragment != null && mBrowserFragment.getBrowserHandler() != null) {
			Message msg = Message.obtain();
			msg.what = BrowserFragment.MSG_HISTORY_POSITION;
			msg.obj = position;
			mBrowserFragment.getBrowserHandler().sendMessage(msg);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (AESThemeUtil.onWinBoLLThemeItemSelected(this, item)) {
            recreate();
        } else {
            int nItemId = item.getItemId();
            if (nItemId == R.id.item_home) {
                if (mBrowserFragment != null && mBrowserFragment.getBrowserHandler() != null) {
                    Message msg = Message.obtain();
                    msg.what = BrowserFragment.MSG_HOMEPAGE;
                    mBrowserFragment.getBrowserHandler().sendMessage(msg);
                }
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
            } else if (nItemId == R.id.item_library_activity) {
                Intent intent = new Intent(getApplicationContext(), cc.winboll.studio.libwinboll.WinBoLLLibraryActivity.class);
                WinBoLLActivityManager.getInstance().startWinBoLLActivity(getApplicationContext(), intent, AboutActivity.class);
            } else {
                return super.onOptionsItemSelected(item);
            }
        }
        return true;
    }

    // ------------------- 新增：对外提供Handler（供其他组件发送消息，如BrowserFragment） -------------------
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

