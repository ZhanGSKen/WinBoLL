package cc.winboll.studio.claudecodeandroid.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cc.winboll.studio.claudecodeandroid.AppSettings;
import cc.winboll.studio.claudecodeandroid.R;
import cc.winboll.studio.claudecodeandroid.ViewColorTable;
import cc.winboll.studio.claudecodeandroid.model.SessionInfo;
import cc.winboll.studio.claudecodeandroid.net.BridgeApi;
import cc.winboll.studio.claudecodeandroid.utils.LogUtils;

/**
 * 会话列表页（入口）。
 *
 * <p>展示桥接服务上的全部会话，支持新建、删除、下拉刷新与进入聊天页。</p>
 *
 * @author 豆包&amp;Big Pickle&amp;MiMo&amp;ZhanGSKen
 * @since 1.0.0
 */
public class MainActivity extends BaseWinBoLLActivity {

    private static final String TAG = "MainActivity";
    private static final int DP_UNIT = 1;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final List<SessionInfo> sessions = new ArrayList<SessionInfo>();

    private TextView statusView;
    private ListView listView;
    private SessionAdapter adapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate: enter");
        setContentView(R.layout.activity_main);

        // 工具栏（ASupportToolbar）：主窗口不显示返回箭头
        Toolbar toolbar = bindToolbar(R.id.toolbar, false);
        if (toolbar != null) {
            toolbar.setTitle(R.string.app_name);
        }

        // 内容区仍由代码构建，挂到工具栏下方容器
        final LinearLayout container = (LinearLayout) findViewById(R.id.content_container);
        if (container != null) {
            container.addView(buildRootView());
        }

        if (AppSettings.getToken(this).length() == 0) {
            showSettingsHint();
        }
        refreshSessions();
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if (itemId == R.id.action_open_source) {
            startActivity(new Intent(this, OpenSourceLicenseActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSessions();
    }

    /** 纯代码构建内容区（顶栏已由 ASupportToolbar 承担） */
    private View buildRootView() {
        final LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(ViewColorTable.COLOR_BG_GRAY);

        statusView = new TextView(this);
        statusView.setTextSize(12.0f);
        statusView.setTextColor(ViewColorTable.COLOR_SECONDARY_TEXT);
        statusView.setPadding(dp(16), dp(8), dp(16), dp(8));
        statusView.setText("正在连接 " + AppSettings.getServerUrl(this));
        root.addView(statusView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        listView = new ListView(this);
        listView.setDividerHeight(1);
        listView.setBackgroundColor(ViewColorTable.COLOR_BG_GRAY);
        adapter = new SessionAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view,
                                    final int position, final long id) {
                openChat(sessions.get(position));
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View view,
                                           final int position, final long id) {
                confirmDelete(sessions.get(position));
                return true;
            }
        });
        final LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);
        root.addView(listView, listParams);

        final LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setPadding(dp(12), dp(10), dp(12), dp(10));
        bottom.setBackgroundColor(ViewColorTable.COLOR_BG_WHITE);

        bottom.addView(buildButton("新建会话", new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showNewSessionDialog();
            }
        }), weightParams());
        bottom.addView(buildButton("刷新", new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                refreshSessions();
            }
        }), weightParams());
        bottom.addView(buildButton("设置", new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        }), weightParams());

        root.addView(bottom, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return root;
    }

    private Button buildButton(final String text, final View.OnClickListener listener) {
        final Button button = new Button(this);
        button.setText(text);
        button.setTextSize(14.0f);
        button.setOnClickListener(listener);
        return button;
    }

    private LinearLayout.LayoutParams weightParams() {
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        params.setMargins(dp(4), 0, dp(4), 0);
        return params;
    }

    private int dp(final int value) {
        return (int) (value * getResources().getDisplayMetrics().density * DP_UNIT);
    }

    private void showSettingsHint() {
        new AlertDialog.Builder(this)
                .setTitle("需要先配置")
                .setMessage("还没有填写访问令牌。请到「设置」页填入桥接服务地址与 config.json 中的 token。")
                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    }
                })
                .setNegativeButton("稍后", null)
                .show();
    }

    /** 拉取会话列表 */
    private void refreshSessions() {
        final String baseUrl = AppSettings.normalizeBaseUrl(AppSettings.getServerUrl(this));
        final String token = AppSettings.getToken(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONArray array = BridgeApi.listSessions(baseUrl, token);
                    final List<SessionInfo> loaded = new ArrayList<SessionInfo>();
                    if (array != null) {
                        for (int i = 0; i < array.length(); i++) {
                            loaded.add(SessionInfo.fromJson(array.getJSONObject(i)));
                        }
                    }
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            sessions.clear();
                            sessions.addAll(loaded);
                            adapter.notifyDataSetChanged();
                            statusView.setText("已连接 · " + sessions.size() + " 个会话 · "
                                    + baseUrl);
                            statusView.setTextColor(ViewColorTable.COLOR_SUCCESS);
                        }
                    });
                } catch (final Exception e) {
                    LogUtils.e(TAG, "refreshSessions failed", e);
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            statusView.setText("连接失败：" + e.getMessage());
                            statusView.setTextColor(ViewColorTable.COLOR_ERROR);
                        }
                    });
                }
            }
        }, "session-list").start();
    }

    private void showNewSessionDialog() {
        final LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(20), dp(12), dp(20), dp(4));

        final EditText titleInput = new EditText(this);
        titleInput.setHint("会话标题，例如：修 Android 崩溃");
        container.addView(titleInput);

        final EditText cwdInput = new EditText(this);
        cwdInput.setText(AppSettings.getCwd(this));
        cwdInput.setHint("工作目录（必须在桥接白名单内）");
        cwdInput.setTextSize(13.0f);
        container.addView(cwdInput);

        final TextView tip = new TextView(this);
        tip.setText("Claude Code 会在该目录下读写文件、执行命令。");
        tip.setTextSize(11.0f);
        tip.setTextColor(ViewColorTable.COLOR_SECONDARY_TEXT);
        container.addView(tip);

        new AlertDialog.Builder(this)
                .setTitle("新建会话")
                .setView(container)
                .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        final String title = titleInput.getText().toString();
                        final String cwd = cwdInput.getText().toString();
                        AppSettings.setCwd(MainActivity.this, cwd);
                        createSession(title, cwd);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void createSession(final String title, final String cwd) {
        final String baseUrl = AppSettings.normalizeBaseUrl(AppSettings.getServerUrl(this));
        final String token = AppSettings.getToken(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject result = BridgeApi.createSession(baseUrl, token, title, cwd);
                    if (result.optInt("_httpCode", 0) != 200) {
                        throw new IOExceptionWithText(result.optString("error", "创建失败"));
                    }
                    final JSONObject sessionJson = result.getJSONObject("session");
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            refreshSessions();
                            openChat(SessionInfo.fromJson(sessionJson));
                        }
                    });
                } catch (final Exception e) {
                    LogUtils.e(TAG, "createSession failed", e);
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "创建失败：" + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }, "session-create").start();
    }

    private void confirmDelete(final SessionInfo session) {
        new AlertDialog.Builder(this)
                .setTitle("删除会话")
                .setMessage("确认删除「" + session.title + "」？本机上的对话记录会一并删除。")
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        deleteSession(session);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteSession(final SessionInfo session) {
        final String baseUrl = AppSettings.normalizeBaseUrl(AppSettings.getServerUrl(this));
        final String token = AppSettings.getToken(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BridgeApi.deleteSession(baseUrl, token, session.id);
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            refreshSessions();
                        }
                    });
                } catch (final Exception e) {
                    LogUtils.e(TAG, "deleteSession failed", e);
                }
            }
        }, "session-delete").start();
    }

    private void openChat(final SessionInfo session) {
        final Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_SESSION_ID, session.id);
        intent.putExtra(ChatActivity.EXTRA_SESSION_TITLE, session.title);
        intent.putExtra(ChatActivity.EXTRA_SESSION_CWD, session.cwd);
        startActivity(intent);
    }

    /** 会话列表适配器 */
    private final class SessionAdapter extends BaseAdapter {

        private final SimpleDateFormat timeFormat =
                new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);

        @Override
        public int getCount() {
            return sessions.size();
        }

        @Override
        public Object getItem(final int position) {
            return sessions.get(position);
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final SessionInfo session = sessions.get(position);
            final LinearLayout row = new LinearLayout(MainActivity.this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(dp(16), dp(12), dp(16), dp(12));
            row.setBackgroundColor(ViewColorTable.COLOR_BG_WHITE);

            final TextView name = new TextView(MainActivity.this);
            name.setText(session.title);
            name.setTextSize(16.0f);
            name.setTextColor(ViewColorTable.COLOR_PRIMARY_TEXT);
            row.addView(name);

            final TextView detail = new TextView(MainActivity.this);
            final String timeText = session.updatedAt > 0
                    ? timeFormat.format(new Date(session.updatedAt)) : "-";
            detail.setText(session.cwd + " · " + session.turnCount + " 轮 · " + timeText);
            detail.setTextSize(11.0f);
            detail.setTextColor(ViewColorTable.COLOR_SECONDARY_TEXT);
            detail.setPadding(0, dp(4), 0, 0);
            row.addView(detail);

            return row;
        }
    }

    /** 带文本的 IOException，便于直接把服务端错误抛给 UI */
    private static final class IOExceptionWithText extends java.io.IOException {
        IOExceptionWithText(final String message) {
            super(message);
        }
    }
}
