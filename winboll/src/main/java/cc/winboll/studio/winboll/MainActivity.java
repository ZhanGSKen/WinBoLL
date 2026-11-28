package cc.winboll.studio.winboll;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.OpenableColumns;
import android.util.Log;
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
import cc.winboll.studio.winboll.media.MidiBridge;
import cc.winboll.studio.winboll.termux.MyTermuxActivity;
import cc.winboll.studio.winboll.fragments.ManagementFragment;
import cc.winboll.studio.winboll.fragments.OpenSourceLicenseFragment;
import cc.winboll.studio.winboll.fragments.WWWBrowserFragment;
import cc.winboll.studio.winboll.push.PushNotifier;
import cc.winboll.studio.winboll.services.PushService;
import cc.winboll.studio.winboll.unittest.TermuxEnvTestActivity;
import cc.winboll.studio.winboll.utils.CertTrustUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends DrawerFragmentActivity {


    public static final String TAG = "MainActivity";

    /** 打开开源许可窗口的 Intent action（由 About 窗口底部按钮发起）。 */
    public static final String ACTION_SHOW_LICENSE = "cc.winboll.studio.winboll.ACTION_SHOW_LICENSE";

    /** 接收到的 MIDI 文件（系统“打开方式”/“分享”）保存在应用内部此目录，供后续组织播放。 */
    public static final String MIDI_INBOX_DIR = "midi_inbox";

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
        // 系统“打开方式”/“分享”传入的 MIDI 文件先落盘到应用内收件箱并托管播放；
        // 命中的意图不再当作外部 URL 交给浏览器窗口。
        boolean midiReceived = handleMidiReceiveIntent(getIntent());
        // 启动默认窗口为 WWWBrowser（浏览器窗口）。外部 ACTION_VIEW 传入的 URL 由 WWWBrowser 打开，
        // 无外部 URL 时加载 Settings 配置的自定义首页。
        if (mWWWBrowserFragment == null) {
            // 推送通知跳转的 URL 优先于外部 ACTION_VIEW 传入的 URL
            String initialUrl = extractPushUrl(getIntent());
            if (initialUrl == null && !midiReceived) {
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
        // About 窗口底部按钮经 ACTION_SHOW_LICENSE 进来时，直接显示开源许可窗口
        if (isShowLicenseIntent(getIntent())) {
            showOpenSourceLicense();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // About 窗口底部按钮：应用已在运行时直接切到开源许可窗口
        if (isShowLicenseIntent(intent)) {
            showOpenSourceLicense();
            return;
        }
        // MIDI 文件再次“打开/分享”时（应用已在运行）继续接住。
        if (handleMidiReceiveIntent(intent)) {
            return;
        }
        // 其余仅处理推送通知跳转；ACTION_VIEW 外部链接刻意只在 onCreate 生效（见下方 extractExternalUrl）。
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
                final boolean trusted = CertTrustUtils.isWinBoLLRootTrusted(MainActivity.this);
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
     * 是否请求显示开源许可窗口（About 窗口底部按钮与菜单入口都用它判断）。
     */
    private boolean isShowLicenseIntent(Intent intent) {
        return intent != null && ACTION_SHOW_LICENSE.equals(intent.getAction());
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

    // ------------------- 系统 MIDI 文件接收（打开方式 / 分享） -------------------

    /**
     * 处理系统“打开方式”(ACTION_VIEW) 与“分享”(ACTION_SEND/SEND_MULTIPLE) 送来的
     * MIDI 文件：复制进应用内部收件箱目录 {@link #MIDI_INBOX_DIR}，再用托管播放
     * （HostedMidiPlayer + MediaPlaybackService 通知栏）播放。
     *
     * @return true 表示该 Intent 确实是 MIDI 文件接收，调用方不应再把它当作外部 URL。
     */
    private boolean handleMidiReceiveIntent(Intent intent) {
        if (intent == null) {
            return false;
        }
        final ArrayList<Uri> uris = new ArrayList<Uri>();
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri data = intent.getData();
            if (isMidiReceiveUri(intent.getType(), data)) {
                uris.add(data);
            }
        } else if (Intent.ACTION_SEND.equals(action)) {
            Uri stream = streamExtra(intent);
            if (isMidiReceiveUri(intent.getType(), stream)) {
                uris.add(stream);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            ArrayList<Uri> streams = streamsExtra(intent);
            for (int i = 0; i < streams.size(); i++) {
                if (isMidiReceiveUri(intent.getType(), streams.get(i))) {
                    uris.add(streams.get(i));
                }
            }
        }
        if (uris.isEmpty()) {
            return false;
        }
        Log.i(TAG, "received MIDI intent: " + action + ", files=" + uris.size());
        // 复制是 IO，放后台线程；播放回主线程（引擎与 MediaSession 都在主线程）。
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<File> saved = saveMidiToInbox(uris);
                if (saved.isEmpty()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MidiBridge.init(getApplicationContext());
                        MidiBridge.playLocalMidiFiles(getApplicationContext(), saved, 0);
                        if (mWWWBrowserFragment != null) {
                            showFragment(mWWWBrowserFragment);
                        }
                    }
                });
            }
        }, "midi-inbox").start();
        return true;
    }

    /**
     * 读取 EXTRA_STREAM：标准发送方给 Uri，个别发送方（命令行 am、部分旧应用）
     * 给字符串 URI，两种都接受。
     */
    private static Uri streamExtra(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return null;
        }
        Object value = extras.get(Intent.EXTRA_STREAM);
        if (value instanceof Uri) {
            return (Uri) value;
        }
        if (value instanceof String) {
            try {
                return Uri.parse((String) value);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static ArrayList<Uri> streamsExtra(Intent intent) {
        ArrayList<Uri> result = new ArrayList<Uri>();
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return result;
        }
        Object value = extras.get(Intent.EXTRA_STREAM);
        if (value instanceof Object[]) {
            Object[] array = (Object[]) value;
            for (int i = 0; i < array.length; i++) {
                Object item = array[i];
                if (item instanceof Uri) {
                    result.add((Uri) item);
                } else if (item instanceof String) {
                    result.add(Uri.parse((String) item));
                }
            }
        } else if (value instanceof ArrayList) {
            ArrayList<?> list = (ArrayList<?>) value;
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                if (item instanceof Uri) {
                    result.add((Uri) item);
                } else if (item instanceof String) {
                    result.add(Uri.parse((String) item));
                }
            }
        } else if (value instanceof Uri) {
            result.add((Uri) value);
        } else if (value instanceof String) {
            result.add(Uri.parse((String) value));
        }
        return result;
    }

    /**
     * 判断 URI 是否是本应用要接收的 MIDI 文件。只接受 file/content 来源，
     * http(s) 链接仍交给浏览器窗口按普通网址打开。
     */
    private boolean isMidiReceiveUri(String mimeType, Uri uri) {
        if (uri == null) {
            return false;
        }
        String scheme = uri.getScheme();
        if (scheme == null) {
            return false;
        }
        String s = scheme.toLowerCase(Locale.US);
        if (!s.equals("file") && !s.equals("content")) {
            return false;
        }
        if (mimeType != null && isMidiMimeType(mimeType)) {
            return true;
        }
        String name = displayNameOf(uri);
        return hasMidiExtension(name) || hasMidiExtension(uri.getPath());
    }

    private static boolean isMidiMimeType(String mimeType) {
        String t = mimeType.toLowerCase(Locale.US).trim();
        return t.equals("audio/midi") || t.equals("audio/x-midi")
            || t.equals("audio/mid") || t.equals("application/x-midi")
            || t.equals("application/midi") || t.equals("audio/sp-midi");
    }

    private static boolean hasMidiExtension(String name) {
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase(Locale.US);
        return lower.endsWith(".mid") || lower.endsWith(".midi")
            || lower.endsWith(".kar") || lower.endsWith(".rmi");
    }

    /** content:// 走 OpenableColumns；file:// 取路径末段。 */
    private String displayNameOf(Uri uri) {
        if (uri == null) {
            return null;
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            Cursor cursor = null;
            try {
                ContentResolver cr = getContentResolver();
                cursor = cr.query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx >= 0) {
                        String name = cursor.getString(idx);
                        if (name != null && name.length() > 0) {
                            return name;
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "query display name failed: " + e.getMessage());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return uri.getLastPathSegment();
    }

    /**
     * 把收到的 MIDI 复制进应用内部收件箱目录（files/midi_inbox），文件名为
     * 「时间戳_原文件名」，重名自动加序号。目录长期保留，供后续组织播放。
     */
    private ArrayList<File> saveMidiToInbox(ArrayList<Uri> uris) {
        ArrayList<File> saved = new ArrayList<File>();
        File dir = new File(getFilesDir(), MIDI_INBOX_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            Log.w(TAG, "cannot create MIDI inbox dir: " + dir);
            return saved;
        }
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        for (int i = 0; i < uris.size(); i++) {
            Uri uri = uris.get(i);
            String raw = displayNameOf(uri);
            String name = sanitizeFileName(raw);
            if (name == null) {
                name = "received_" + (i + 1);
            }
            if (!hasMidiExtension(name)) {
                name = name + ".mid";
            }
            File target = new File(dir, stamp + "_" + name);
            int seq = 2;
            while (target.exists()) {
                target = new File(dir, stamp + "_" + seq + "_" + name);
                seq++;
            }
            if (copyUriToFile(uri, target)) {
                saved.add(target);
                Log.i(TAG, "MIDI saved to inbox: " + target.getAbsolutePath());
            }
        }
        return saved;
    }

    private boolean copyUriToFile(Uri uri, File target) {
        InputStream in = null;
        FileOutputStream out = null;
        File tmp = new File(target.getAbsolutePath() + ".part");
        try {
            in = getContentResolver().openInputStream(uri);
            if (in == null) {
                Log.w(TAG, "no stream for MIDI uri: " + uri);
                return false;
            }
            out = new FileOutputStream(tmp);
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            out.close();
            out = null;
            if (!tmp.renameTo(target)) {
                target.delete();
                if (!tmp.renameTo(target)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            Log.w(TAG, "copy MIDI failed: " + e.getMessage());
            tmp.delete();
            return false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    private static String sanitizeFileName(String raw) {
        if (raw == null) {
            return null;
        }
        String name = raw.trim();
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        if (name.length() == 0) {
            return null;
        }
        name = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (name.length() > 120) {
            name = name.substring(name.length() - 120);
        }
        return name;
    }

	public static void sendMessage(Message msg) {
		if (_mMainHandler != null && msg != null) {
			_mMainHandler.sendMessage(msg);
		}
	}

    /**
     * 初始化Handler（接收MSG_URLLOADHISTORY_UPDATE消息，刷新抽屉历史菜单）
     */
    @SuppressWarnings("deprecation")
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

