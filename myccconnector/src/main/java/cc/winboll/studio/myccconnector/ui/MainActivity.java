/*
 * Copyright 2026 WinBoLL Studio
 * SPDX-License-Identifier: Apache-2.0
 */

package cc.winboll.studio.myccconnector.ui;

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
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import cc.winboll.studio.myccconnector.AppSettings;
import cc.winboll.studio.myccconnector.R;
import cc.winboll.studio.myccconnector.ViewColorTable;
import cc.winboll.studio.myccconnector.model.SessionInfo;
import cc.winboll.studio.myccconnector.model.WorkInfo;
import cc.winboll.studio.myccconnector.net.BridgeApi;
import cc.winboll.studio.myccconnector.utils.LogUtils;

/**
 * 会话列表页（入口）。
 *
 * <p>会话按「工作任务」分组折叠展示：工作任务是可展开的行，其下为其归属会话；
 * 未归属任务的会话收在「未归档会话」分组。支持新建工作任务、在其下新建会话、
 * 会话改挂任务、删除任务（级联删除其下会话）等操作。</p>
 *
 * @author 豆包&amp;Big Pickle&amp;MiMo&amp;ZhanGSKen
 * @since 1.0.0
 */
public class MainActivity extends BaseWinBoLLActivity {

    private static final String TAG = "MainActivity";
    private static final int DP_UNIT = 1;

    private static final int ROW_WORK = 0;
    private static final int ROW_SESSION = 1;
    private static final int ROW_UNGROUPED = 2;

    private static final String UNGROUPED_LABEL = "未归档会话";

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final List<WorkInfo> works = new ArrayList<WorkInfo>();
    private final List<SessionInfo> sessions = new ArrayList<SessionInfo>();
    private final List<Row> rows = new ArrayList<Row>();
    /** 展开中的工作任务 id */
    private final Set<String> expandedWorks = new HashSet<String>();
    private boolean ungroupedExpanded = true;

    private TextView statusView;
    private ListView listView;
    private GroupedAdapter adapter;

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
        refreshAll();
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
        } else if (itemId == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAll();
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
        adapter = new GroupedAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view,
                                    final int position, final long id) {
                if (position < 0 || position >= rows.size()) {
                    return;
                }
                final Row row = rows.get(position);
                if (row.type == ROW_SESSION) {
                    openChat(row.session);
                } else if (row.type == ROW_WORK) {
                    toggleWork(row.work.id);
                } else {
                    ungroupedExpanded = !ungroupedExpanded;
                    rebuildRows();
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View view,
                                           final int position, final long id) {
                if (position < 0 || position >= rows.size()) {
                    return true;
                }
                final Row row = rows.get(position);
                if (row.type == ROW_SESSION) {
                    showSessionOptions(row.session);
                    return true;
                }
                if (row.type == ROW_WORK) {
                    showWorkOptions(row.work);
                    return true;
                }
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

        bottom.addView(buildButton("新建任务", new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showNewWorkDialog();
            }
        }), weightParams());
        bottom.addView(buildButton("新建会话", new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showNewSessionDialog(null, null);
            }
        }), weightParams());
        bottom.addView(buildButton("刷新", new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                refreshAll();
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

    /** 重新计算分组折叠后的行序列 */
    private void rebuildRows() {
        rows.clear();
        for (final WorkInfo work : works) {
            rows.add(Row.ofWork(work));
            if (!expandedWorks.contains(work.id)) {
                continue;
            }
            for (final SessionInfo session : sessions) {
                if (work.id.equals(session.workId)) {
                    rows.add(Row.ofSession(session));
                }
            }
        }
        final List<SessionInfo> ungrouped = new ArrayList<SessionInfo>();
        for (final SessionInfo session : sessions) {
            if (session.workId == null || findWork(session.workId) == null) {
                ungrouped.add(session);
            }
        }
        rows.add(Row.ofUngrouped(ungrouped.size()));
        if (ungroupedExpanded) {
            for (final SessionInfo session : ungrouped) {
                rows.add(Row.ofSession(session));
            }
        }
        adapter.notifyDataSetChanged();
    }

    private WorkInfo findWork(final String workId) {
        if (workId == null) {
            return null;
        }
        for (final WorkInfo work : works) {
            if (workId.equals(work.id)) {
                return work;
            }
        }
        return null;
    }

    private void toggleWork(final String workId) {
        if (expandedWorks.contains(workId)) {
            expandedWorks.remove(workId);
        } else {
            expandedWorks.add(workId);
        }
        rebuildRows();
    }

    /** 拉取工作任务与会话列表 */
    private void refreshAll() {
        final String baseUrl = AppSettings.normalizeBaseUrl(AppSettings.getServerUrl(this));
        final String token = AppSettings.getToken(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONArray workArray = BridgeApi.listWorks(baseUrl, token);
                    final List<WorkInfo> loadedWorks = new ArrayList<WorkInfo>();
                    if (workArray != null) {
                        for (int i = 0; i < workArray.length(); i++) {
                            loadedWorks.add(WorkInfo.fromJson(workArray.getJSONObject(i)));
                        }
                    }
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
                            works.clear();
                            works.addAll(loadedWorks);
                            sessions.clear();
                            sessions.addAll(loaded);
                            // 丢弃已不存在的任务的展开状态
                            final Set<String> valid = new HashSet<String>();
                            for (final WorkInfo work : works) {
                                if (expandedWorks.contains(work.id)) {
                                    valid.add(work.id);
                                }
                            }
                            expandedWorks.clear();
                            expandedWorks.addAll(valid);
                            rebuildRows();
                            statusView.setText("已连接 · " + works.size() + " 个工作任务 · "
                                    + sessions.size() + " 个会话 · " + baseUrl);
                            statusView.setTextColor(ViewColorTable.COLOR_SUCCESS);
                        }
                    });
                } catch (final Exception e) {
                    LogUtils.e(TAG, "refreshAll failed", e);
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

    /** 新建工作任务：名称 + 工作目录 */
    private void showNewWorkDialog() {
        final LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(20), dp(12), dp(20), dp(4));

        final EditText nameInput = new EditText(this);
        nameInput.setHint("工作任务名称，例如：修 Android 崩溃");
        container.addView(nameInput);

        final EditText cwdInput = new EditText(this);
        cwdInput.setText(AppSettings.getCwd(this));
        cwdInput.setHint("工作目录（必须在桥接白名单内）");
        cwdInput.setTextSize(13.0f);
        container.addView(cwdInput);

        final TextView tip = new TextView(this);
        tip.setText("该任务下的会话都在此目录中读写文件、执行命令。");
        tip.setTextSize(11.0f);
        tip.setTextColor(ViewColorTable.COLOR_SECONDARY_TEXT);
        container.addView(tip);

        new AlertDialog.Builder(this)
                .setTitle("新建工作任务")
                .setView(container)
                .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        final String name = nameInput.getText().toString();
                        final String cwd = cwdInput.getText().toString();
                        AppSettings.setCwd(MainActivity.this, cwd);
                        createWork(name, cwd);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void createWork(final String name, final String cwd) {
        final String baseUrl = AppSettings.normalizeBaseUrl(AppSettings.getServerUrl(this));
        final String token = AppSettings.getToken(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject result = BridgeApi.createWork(baseUrl, token, name, cwd);
                    if (result.optInt("_httpCode", 0) != 200) {
                        throw new IOExceptionWithText(result.optString("error", "创建失败"));
                    }
                    final WorkInfo work = WorkInfo.fromJson(result.getJSONObject("work"));
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            expandedWorks.add(work.id);
                            refreshAll();
                            Toast.makeText(MainActivity.this,
                                    "工作任务「" + work.name + "」已创建", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (final Exception e) {
                    LogUtils.e(TAG, "createWork failed", e);
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "创建失败：" + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }, "work-create").start();
    }

    /**
     * 新建会话对话框。
     *
     * @param presetWorkId 预选的工作任务（长按任务时传入），可为 null
     * @param presetTitle  预填标题，可为 null
     */
    private void showNewSessionDialog(final String presetWorkId, final String presetTitle) {
        final LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(20), dp(12), dp(20), dp(4));

        final List<String> labels = new ArrayList<String>();
        final List<WorkInfo> candidates = new ArrayList<WorkInfo>();
        labels.add(UNGROUPED_LABEL);
        candidates.add(null);
        for (final WorkInfo work : works) {
            labels.add(work.name);
            candidates.add(work);
        }

        final TextView workLabel = new TextView(this);
        workLabel.setText("所属工作任务");
        workLabel.setTextSize(13.0f);
        workLabel.setTextColor(ViewColorTable.COLOR_SECONDARY_TEXT);
        container.addView(workLabel);

        final Spinner workSpinner = new Spinner(this);
        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, labels);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workSpinner.setAdapter(spinnerAdapter);
        container.addView(workSpinner);

        final EditText titleInput = new EditText(this);
        titleInput.setHint("会话标题，例如：修 Android 崩溃");
        titleInput.setText(presetTitle == null ? "" : presetTitle);
        container.addView(titleInput);

        final EditText cwdInput = new EditText(this);
        cwdInput.setText(AppSettings.getCwd(this));
        cwdInput.setHint("工作目录（必须在桥接白名单内）");
        cwdInput.setTextSize(13.0f);
        container.addView(cwdInput);

        final TextView tip = new TextView(this);
        tip.setText("编码助手会在该目录下读写文件、执行命令。");
        tip.setTextSize(11.0f);
        tip.setTextColor(ViewColorTable.COLOR_SECONDARY_TEXT);
        container.addView(tip);

        // 预选任务：定位 spinner 并按任务目录回填
        int selected = 0;
        for (int i = 0; i < candidates.size(); i++) {
            final WorkInfo work = candidates.get(i);
            if (work != null && work.id.equals(presetWorkId)) {
                selected = i;
                break;
            }
        }
        workSpinner.setSelection(selected);
        final WorkInfo presetWork = candidates.get(selected);
        if (presetWork != null && presetWork.cwd.length() > 0) {
            cwdInput.setText(presetWork.cwd);
        }

        workSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view,
                                       final int position, final long id) {
                final WorkInfo work = candidates.get(position);
                if (work != null && work.cwd.length() > 0) {
                    cwdInput.setText(work.cwd);
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                // 无需处理
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("新建会话")
                .setView(container)
                .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        final WorkInfo work = candidates.get(workSpinner.getSelectedItemPosition());
                        final String title = titleInput.getText().toString();
                        final String cwd = cwdInput.getText().toString();
                        AppSettings.setCwd(MainActivity.this, cwd);
                        createSession(title, cwd, work == null ? null : work.id);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void createSession(final String title, final String cwd, final String workId) {
        final String baseUrl = AppSettings.normalizeBaseUrl(AppSettings.getServerUrl(this));
        final String token = AppSettings.getToken(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject result = BridgeApi.createSession(baseUrl, token, title, cwd, workId);
                    if (result.optInt("_httpCode", 0) != 200) {
                        throw new IOExceptionWithText(result.optString("error", "创建失败"));
                    }
                    final JSONObject sessionJson = result.getJSONObject("session");
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (workId != null) {
                                expandedWorks.add(workId);
                            }
                            refreshAll();
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

    /** 长按工作任务：新建会话 / 重命名 / 改目录 / 删除任务 */
    private void showWorkOptions(final WorkInfo work) {
        final String[] items = new String[]{"新建会话", "重命名任务", "修改工作目录", "删除工作任务"};
        new AlertDialog.Builder(this)
                .setTitle(work.name)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        if (which == 0) {
                            showNewSessionDialog(work.id, null);
                        } else if (which == 1) {
                            showRenameWorkDialog(work);
                        } else if (which == 2) {
                            showWorkCwdDialog(work);
                        } else {
                            confirmDeleteWork(work);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showRenameWorkDialog(final WorkInfo work) {
        final EditText input = new EditText(this);
        input.setText(work.name);
        input.setPadding(dp(20), dp(12), dp(20), dp(4));
        new AlertDialog.Builder(this)
                .setTitle("重命名工作任务")
                .setView(input)
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        final String name = input.getText().toString();
                        updateWork(work, name, null);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showWorkCwdDialog(final WorkInfo work) {
        final EditText input = new EditText(this);
        input.setText(work.cwd);
        input.setPadding(dp(20), dp(12), dp(20), dp(4));
        new AlertDialog.Builder(this)
                .setTitle("修改工作目录")
                .setView(input)
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        updateWork(work, null, input.getText().toString());
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateWork(final WorkInfo work, final String name, final String cwd) {
        final String baseUrl = AppSettings.normalizeBaseUrl(AppSettings.getServerUrl(this));
        final String token = AppSettings.getToken(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject result = BridgeApi.updateWork(baseUrl, token, work.id, name, cwd);
                    if (result.optInt("_httpCode", 0) != 200) {
                        throw new IOExceptionWithText(result.optString("error", "修改失败"));
                    }
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            refreshAll();
                        }
                    });
                } catch (final Exception e) {
                    LogUtils.e(TAG, "updateWork failed", e);
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "修改失败：" + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }, "work-update").start();
    }

    private void confirmDeleteWork(final WorkInfo work) {
        new AlertDialog.Builder(this)
                .setTitle("删除工作任务")
                .setMessage("确认删除「" + work.name + "」？其下 " + work.sessionCount
                        + " 个会话及其对话记录会一并删除。")
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        deleteWork(work);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteWork(final WorkInfo work) {
        final String baseUrl = AppSettings.normalizeBaseUrl(AppSettings.getServerUrl(this));
        final String token = AppSettings.getToken(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BridgeApi.deleteWork(baseUrl, token, work.id);
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            expandedWorks.remove(work.id);
                            refreshAll();
                        }
                    });
                } catch (final Exception e) {
                    LogUtils.e(TAG, "deleteWork failed", e);
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "删除失败：" + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }, "work-delete").start();
    }

    /** 长按会话：打开 / 删除 / 改挂任务 */
    private void showSessionOptions(final SessionInfo session) {
        final List<String> items = new ArrayList<String>();
        items.add("打开会话");
        if (works.size() > 0) {
            items.add(session.workId == null ? "移入工作任务" : "调整所属任务");
        }
        if (session.workId != null) {
            items.add("移出工作任务");
        }
        items.add("删除会话");
        final String[] array = items.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle(session.title)
                .setItems(array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        final String action = array[which];
                        if ("打开会话".equals(action)) {
                            openChat(session);
                        } else if ("删除会话".equals(action)) {
                            confirmDelete(session);
                        } else if ("移出工作任务".equals(action)) {
                            moveSession(session, null);
                        } else {
                            showMoveSessionDialog(session);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showMoveSessionDialog(final SessionInfo session) {
        final List<String> labels = new ArrayList<String>();
        final List<WorkInfo> candidates = new ArrayList<WorkInfo>();
        labels.add(UNGROUPED_LABEL);
        candidates.add(null);
        for (final WorkInfo work : works) {
            labels.add(work.name);
            candidates.add(work);
        }
        final String[] array = labels.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("选择工作任务")
                .setItems(array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        final WorkInfo work = candidates.get(which);
                        moveSession(session, work == null ? null : work.id);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void moveSession(final SessionInfo session, final String workId) {
        final String baseUrl = AppSettings.normalizeBaseUrl(AppSettings.getServerUrl(this));
        final String token = AppSettings.getToken(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject result = BridgeApi.moveSession(baseUrl, token, session.id, workId);
                    if (result.optInt("_httpCode", 0) != 200) {
                        throw new IOExceptionWithText(result.optString("error", "调整失败"));
                    }
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (workId != null) {
                                expandedWorks.add(workId);
                            }
                            refreshAll();
                        }
                    });
                } catch (final Exception e) {
                    LogUtils.e(TAG, "moveSession failed", e);
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "调整失败：" + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }, "session-move").start();
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
                            refreshAll();
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

    /** 列表行：工作任务头 / 会话 / 未归档头 */
    private static final class Row {

        final int type;
        final WorkInfo work;
        final SessionInfo session;
        final int sessionCount;

        private Row(final int type, final WorkInfo work, final SessionInfo session,
                    final int sessionCount) {
            this.type = type;
            this.work = work;
            this.session = session;
            this.sessionCount = sessionCount;
        }

        static Row ofWork(final WorkInfo work) {
            return new Row(ROW_WORK, work, null, 0);
        }

        static Row ofSession(final SessionInfo session) {
            return new Row(ROW_SESSION, null, session, 0);
        }

        static Row ofUngrouped(final int count) {
            return new Row(ROW_UNGROUPED, null, null, count);
        }
    }

    /** 分组折叠列表适配器 */
    private final class GroupedAdapter extends BaseAdapter {

        private final SimpleDateFormat timeFormat =
                new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);

        @Override
        public int getCount() {
            return rows.size();
        }

        @Override
        public Object getItem(final int position) {
            return rows.get(position);
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public int getItemViewType(final int position) {
            return rows.get(position).type;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final Row row = rows.get(position);
            if (row.type == ROW_WORK) {
                return buildWorkRow(row.work, expandedWorks.contains(row.work.id));
            }
            if (row.type == ROW_UNGROUPED) {
                return buildUngroupedRow(row.sessionCount, ungroupedExpanded);
            }
            return buildSessionRow(row.session);
        }

        private View buildWorkRow(final WorkInfo work, final boolean expanded) {
            final LinearLayout row = new LinearLayout(MainActivity.this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(16), dp(12), dp(16), dp(12));
            row.setBackgroundColor(ViewColorTable.COLOR_BG_WHITE);

            final LinearLayout texts = new LinearLayout(MainActivity.this);
            texts.setOrientation(LinearLayout.VERTICAL);
            final TextView name = new TextView(MainActivity.this);
            name.setText(work.name);
            name.setTextSize(16.0f);
            name.setTextColor(ViewColorTable.COLOR_PRIMARY_TEXT);
            texts.addView(name);
            final TextView detail = new TextView(MainActivity.this);
            detail.setText(work.cwd + " · " + work.sessionCount + " 个会话");
            detail.setTextSize(11.0f);
            detail.setTextColor(ViewColorTable.COLOR_SECONDARY_TEXT);
            detail.setPadding(0, dp(4), 0, 0);
            texts.addView(detail);
            row.addView(texts, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

            final TextView arrow = new TextView(MainActivity.this);
            arrow.setText(expanded ? "▾" : "▸");
            arrow.setTextSize(16.0f);
            arrow.setTextColor(ViewColorTable.COLOR_SECONDARY_TEXT);
            row.addView(arrow);
            return row;
        }

        private View buildUngroupedRow(final int count, final boolean expanded) {
            final LinearLayout row = new LinearLayout(MainActivity.this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(16), dp(10), dp(16), dp(10));
            row.setBackgroundColor(ViewColorTable.COLOR_BG_GRAY);

            final TextView name = new TextView(MainActivity.this);
            name.setText(UNGROUPED_LABEL + " · " + count);
            name.setTextSize(14.0f);
            name.setTextColor(ViewColorTable.COLOR_SECONDARY_TEXT);
            row.addView(name, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

            final TextView arrow = new TextView(MainActivity.this);
            arrow.setText(expanded ? "▾" : "▸");
            arrow.setTextSize(14.0f);
            arrow.setTextColor(ViewColorTable.COLOR_SECONDARY_TEXT);
            row.addView(arrow);
            return row;
        }

        private View buildSessionRow(final SessionInfo session) {
            final LinearLayout row = new LinearLayout(MainActivity.this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(dp(32), dp(10), dp(16), dp(10));
            row.setBackgroundColor(ViewColorTable.COLOR_BG_WHITE);

            final TextView name = new TextView(MainActivity.this);
            name.setText(session.title);
            name.setTextSize(15.0f);
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
