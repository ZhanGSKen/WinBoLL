package cc.winboll.studio.claudecodeandroid.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import cc.winboll.studio.claudecodeandroid.AppSettings;
import cc.winboll.studio.claudecodeandroid.R;
import cc.winboll.studio.claudecodeandroid.ViewColorTable;
import cc.winboll.studio.claudecodeandroid.net.BridgeApi;
import cc.winboll.studio.claudecodeandroid.net.SseClient;
import cc.winboll.studio.claudecodeandroid.utils.LogUtils;

/**
 * 聊天页：与 Claude Code 会话实时联动。
 *
 * <p>通过 SSE 接收增量文本、工具调用、工具结果与权限审批请求；
 * 权限请求会在手机上弹窗，允许后才在本机执行。</p>
 *
 * @author 豆包&amp;Big Pickle&amp;MiMo&amp;ZhanGSKen
 * @since 1.0.0
 */
public class ChatActivity extends BaseWinBoLLActivity {

    public static final String EXTRA_SESSION_ID = "session_id";
    public static final String EXTRA_SESSION_TITLE = "session_title";
    public static final String EXTRA_SESSION_CWD = "session_cwd";

    private static final String TAG = "ChatActivity";
    private static final int MAX_TOOL_RESULT_CHARS = 1200;
    private static final int MAX_TOOL_INPUT_CHARS = 600;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private String sessionId;
    private String sessionTitle;
    private String sessionCwd;

    private LinearLayout messagesContainer;
    private ScrollView scrollView;
    private EditText inputBox;
    private Button sendButton;
    private MenuItem interruptMenuItem;
    private boolean interruptEnabled;

    private SseClient sseClient;
    private TextView currentAssistantBubble;
    private final StringBuilder currentAssistantText = new StringBuilder();
    private boolean busy = false;
    private boolean permissionDialogShowing = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionId = getIntent().getStringExtra(EXTRA_SESSION_ID);
        sessionTitle = getIntent().getStringExtra(EXTRA_SESSION_TITLE);
        sessionCwd = getIntent().getStringExtra(EXTRA_SESSION_CWD);
        if (sessionId == null || sessionId.length() == 0) {
            finish();
            return;
        }
        setContentView(R.layout.activity_chat);

        // 工具栏（ASupportToolbar）：标题为会话名，副标题为工作目录与连接状态
        Toolbar toolbar = bindToolbar(R.id.toolbar, true);
        if (toolbar != null) {
            toolbar.setTitle(sessionTitle == null ? getString(R.string.title_chat) : sessionTitle);
            toolbar.setSubtitle((sessionCwd == null ? "" : sessionCwd + " · ") + "连接中…");
        }

        final LinearLayout container = (LinearLayout) findViewById(R.id.content_container);
        if (container != null) {
            container.addView(buildRootView());
        }
        startStream();
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
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        interruptMenuItem = menu.findItem(R.id.action_interrupt);
        if (interruptMenuItem != null) {
            interruptMenuItem.setEnabled(interruptEnabled);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.action_interrupt) {
            doInterrupt();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sseClient != null) {
            sseClient.stop();
        }
    }

    private View buildRootView() {
        final LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(ViewColorTable.COLOR_BG_GRAY);

        // 顶栏（标题 / 连接状态 / 中断）已由 ASupportToolbar 与菜单承担，内容区直接从消息区开始

        // ---- 消息区 ----
        scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        messagesContainer = new LinearLayout(this);
        messagesContainer.setOrientation(LinearLayout.VERTICAL);
        messagesContainer.setPadding(dp(10), dp(10), dp(10), dp(10));
        scrollView.addView(messagesContainer, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        root.addView(scrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));

        // ---- 输入区 ----
        final LinearLayout inputRow = new LinearLayout(this);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setPadding(dp(10), dp(8), dp(10), dp(8));
        inputRow.setBackgroundColor(ViewColorTable.COLOR_BG_WHITE);

        inputBox = new EditText(this);
        inputBox.setHint("给 Claude Code 下指令…");
        inputBox.setTextSize(14.0f);
        inputBox.setMinLines(1);
        inputBox.setMaxLines(4);
        inputBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        inputRow.addView(inputBox, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        sendButton = new Button(this);
        sendButton.setText("发送");
        sendButton.setTextSize(14.0f);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                doSend();
            }
        });
        inputRow.addView(sendButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        root.addView(inputRow, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return root;
    }

    private int dp(final int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    // ---------------------------------------------------------------- 事件流

    private void startStream() {
        final String baseUrl = AppSettings.normalizeBaseUrl(AppSettings.getServerUrl(this));
        final String token = AppSettings.getToken(this);
        sseClient = new SseClient(baseUrl, token, sessionId, new SseClient.Listener() {
            @Override
            public void onEvent(final JSONObject event) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        renderEvent(event);
                    }
                });
            }

            @Override
            public void onStatus(final String text, final boolean connected) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateToolbarStatus((sessionCwd == null ? "" : sessionCwd + " · ") + text, connected);
                    }
                });
            }
        });
        sseClient.start();
    }

    private void renderEvent(final JSONObject event) {
        final String type = event.optString("type", "");
        if ("user".equals(type)) {
            currentAssistantBubble = null;
            currentAssistantText.setLength(0);
            addBubble(event.optString("text", ""), true, false);
        } else if ("delta".equals(type)) {
            appendDelta(event.optString("text", ""));
        } else if ("assistant_message".equals(type)) {
            finalizeAssistant(event.optString("text", ""));
        } else if ("assistant_message_thinking".equals(type)) {
            // 预留：思考块
        } else if ("tool_use".equals(type)) {
            currentAssistantBubble = null;
            currentAssistantText.setLength(0);
            addToolCard("调用工具 · " + event.optString("name", "?"),
                    truncate(event.optJSONObject("input") == null
                            ? "" : event.optJSONObject("input").toString(), MAX_TOOL_INPUT_CHARS),
                    false);
        } else if ("tool_result".equals(type)) {
            addToolCard(event.optBoolean("isError", false) ? "工具返回（失败）" : "工具返回",
                    truncate(event.optString("content", ""), MAX_TOOL_RESULT_CHARS),
                    true);
        } else if ("permission_request".equals(type)) {
            showPermissionDialog(event.optString("requestId", ""),
                    event.optString("toolName", "?"),
                    event.optJSONObject("input") == null ? "" : event.optJSONObject("input").toString());
        } else if ("permission_timeout".equals(type)) {
            addSystemLine("权限请求超时，已按拒绝处理：" + event.optString("toolName", ""),
                    ViewColorTable.COLOR_WARNING);
        } else if ("permission_history".equals(type)) {
            // 历史回放中的审批请求，只展示一条痕迹，不弹窗
            addSystemLine("历史审批 · " + event.optString("toolName", "?"), ViewColorTable.COLOR_HINT_TEXT);
        } else if ("permission_resolved".equals(type)) {
            // 实时审批结果已由点击回调展示，这里不重复输出
        } else if ("result".equals(type)) {
            final double seconds = event.optLong("durationMs", 0L) / 1000.0;
            addSystemLine(String.format(LocaleHolder.LOCALE, "完成 · %.1f 秒 · %d 轮",
                    seconds, event.optInt("turns", 0)), ViewColorTable.COLOR_SUCCESS);
        } else if ("error".equals(type)) {
            addSystemLine("错误：" + event.optString("message", ""), ViewColorTable.COLOR_ERROR);
        } else if ("state".equals(type)) {
            setBusy(event.optBoolean("busy", false));
        } else if ("init".equals(type)) {
            final org.json.JSONArray tools = event.optJSONArray("tools");
            final int toolCount = tools == null ? 0 : tools.length();
            addSystemLine("已接入 " + event.optString("model", "模型") + " · 工具 " + toolCount + " 个",
                    ViewColorTable.COLOR_SECONDARY_TEXT);
        } else if ("turn_end".equals(type)) {
            setBusy(false);
        }
        scrollToBottom();
    }

    private void appendDelta(final String text) {
        if (currentAssistantBubble == null) {
            currentAssistantBubble = addBubble("", false, true);
        }
        currentAssistantText.append(text);
        currentAssistantBubble.setText(currentAssistantText.toString());
    }

    private void finalizeAssistant(final String text) {
        if (currentAssistantBubble == null) {
            addBubble(text, false, false);
        } else {
            currentAssistantBubble.setText(text);
        }
        currentAssistantBubble = null;
        currentAssistantText.setLength(0);
    }

    private void setBusy(final boolean value) {
        busy = value;
        sendButton.setEnabled(!value);
        interruptEnabled = value;
        supportInvalidateOptionsMenu();
    }

    /**
     * 更新工具栏副标题中的连接状态；断开时用警示色提示。
     */
    private void updateToolbarStatus(final String text, final boolean connected) {
        final Toolbar toolbar = getWindowToolbar();
        if (toolbar == null) {
            return;
        }
        toolbar.setSubtitle(text);
        toolbar.setSubtitleTextColor(connected ? ViewColorTable.COLOR_WHITE_TEXT
                : ViewColorTable.COLOR_WARNING);
    }

    // ---------------------------------------------------------------- 视图构建

    private TextView addBubble(final String text, final boolean isUser, final boolean placeholder) {
        final TextView bubble = new TextView(this);
        bubble.setText(text.length() == 0 && placeholder ? "…" : text);
        bubble.setTextSize(14.0f);
        bubble.setTextIsSelectable(true);
        bubble.setTextColor(isUser ? ViewColorTable.COLOR_BUBBLE_USER_TEXT
                : ViewColorTable.COLOR_PRIMARY_TEXT);
        bubble.setPadding(dp(12), dp(9), dp(12), dp(9));
        final GradientDrawable bg = new GradientDrawable();
        bg.setColor(isUser ? ViewColorTable.COLOR_BUBBLE_USER : ViewColorTable.COLOR_BUBBLE_ASSISTANT);
        bg.setCornerRadius(dp(10));
        bg.setStroke(dp(1), ViewColorTable.COLOR_DIVIDER);
        bubble.setBackground(bg);

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(4), 0, dp(4));
        params.gravity = isUser ? Gravity.END : Gravity.START;
        final int maxWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.86);
        bubble.setMaxWidth(maxWidth);
        messagesContainer.addView(bubble, params);
        return bubble;
    }

    private void addToolCard(final String title, final String body, final boolean isResult) {
        final LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(10), dp(8), dp(10), dp(8));
        final GradientDrawable bg = new GradientDrawable();
        bg.setColor(ViewColorTable.COLOR_TOOL_BG);
        bg.setCornerRadius(dp(6));
        bg.setStroke(dp(1), ViewColorTable.COLOR_TOOL_BORDER);
        card.setBackground(bg);

        final TextView titleView = new TextView(this);
        titleView.setText((isResult ? "↳ " : "🔧 ") + title);
        titleView.setTextSize(12.0f);
        titleView.setTextColor(ViewColorTable.COLOR_TOOL_TITLE);
        card.addView(titleView);

        if (body != null && body.length() > 0) {
            final TextView bodyView = new TextView(this);
            bodyView.setText(body);
            bodyView.setTextSize(11.0f);
            bodyView.setTextColor(ViewColorTable.COLOR_TOOL_BODY);
            bodyView.setTypeface(android.graphics.Typeface.MONOSPACE);
            bodyView.setPadding(0, dp(4), 0, 0);
            card.addView(bodyView);
        }

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(4), 0, dp(4));
        messagesContainer.addView(card, params);
    }

    private void addSystemLine(final String text, final int color) {
        final TextView line = new TextView(this);
        line.setText(text);
        line.setTextSize(11.0f);
        line.setTextColor(color);
        line.setGravity(Gravity.CENTER);
        line.setPadding(0, dp(4), 0, dp(4));
        messagesContainer.addView(line, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void scrollToBottom() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private String truncate(final String text, final int limit) {
        if (text == null) {
            return "";
        }
        if (text.length() <= limit) {
            return text;
        }
        return text.substring(0, limit) + "\n…（已截断 " + (text.length() - limit) + " 字）";
    }

    // ---------------------------------------------------------------- 交互

    private void doSend() {
        final String text = inputBox.getText().toString().trim();
        if (text.length() == 0) {
            return;
        }
        if (busy) {
            Toast.makeText(this, "上一轮还在执行，请稍等或点中断", Toast.LENGTH_SHORT).show();
            return;
        }
        final String baseUrl = AppSettings.normalizeBaseUrl(AppSettings.getServerUrl(this));
        final String token = AppSettings.getToken(this);
        setBusy(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final int code = BridgeApi.sendMessage(baseUrl, token, sessionId, text);
                    if (code != 202) {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                setBusy(false);
                                Toast.makeText(ChatActivity.this, "发送失败（HTTP " + code + "）",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                        return;
                    }
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            inputBox.setText("");
                        }
                    });
                } catch (final Exception e) {
                    LogUtils.e(TAG, "sendMessage failed", e);
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setBusy(false);
                            Toast.makeText(ChatActivity.this, "发送失败：" + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }, "send-message").start();
    }

    private void doInterrupt() {
        final String baseUrl = AppSettings.normalizeBaseUrl(AppSettings.getServerUrl(this));
        final String token = AppSettings.getToken(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final boolean ok = BridgeApi.interrupt(baseUrl, token, sessionId);
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            addSystemLine(ok ? "已请求中断" : "中断请求未生效",
                                    ViewColorTable.COLOR_WARNING);
                        }
                    });
                } catch (final Exception e) {
                    LogUtils.e(TAG, "interrupt failed", e);
                }
            }
        }, "interrupt").start();
    }

    /** 权限审批：允许 / 拒绝 / 本会话该工具一律允许 */
    private void showPermissionDialog(final String requestId, final String toolName,
                                      final String inputJson) {
        if (permissionDialogShowing) {
            // 同一时刻只弹一个，避免堆叠把界面顶住
            addSystemLine("新权限请求（" + toolName + "）等待处理，请先处理当前弹窗",
                    ViewColorTable.COLOR_WARNING);
            return;
        }
        permissionDialogShowing = true;
        final String body = truncate(inputJson, 900);
        new AlertDialog.Builder(this)
                .setTitle("允许执行 " + toolName + " ？")
                .setMessage(body.length() == 0 ? "该操作将在 WINBOT 主机上执行。" : body)
                .setPositiveButton("允许", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        answerPermission(requestId, true, false, toolName);
                        permissionDialogShowing = false;
                    }
                })
                .setNeutralButton("本会话总是允许", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        answerPermission(requestId, true, true, toolName);
                        permissionDialogShowing = false;
                    }
                })
                .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        answerPermission(requestId, false, false, toolName);
                        permissionDialogShowing = false;
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        answerPermission(requestId, false, false, toolName);
                        permissionDialogShowing = false;
                    }
                })
                .show();
    }

    private void answerPermission(final String requestId, final boolean allow,
                                  final boolean remember, final String toolName) {
        final String baseUrl = AppSettings.normalizeBaseUrl(AppSettings.getServerUrl(this));
        final String token = AppSettings.getToken(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final boolean ok = BridgeApi.answerPermission(baseUrl, token, requestId,
                            allow, remember, toolName);
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            addSystemLine((allow ? "已允许：" : "已拒绝：") + toolName,
                                    allow ? ViewColorTable.COLOR_SUCCESS : ViewColorTable.COLOR_ERROR);
                        }
                    });
                } catch (final Exception e) {
                    LogUtils.e(TAG, "answerPermission failed", e);
                }
            }
        }, "permission").start();
    }

    /** Locale 常量持有，避免每次调用 Locale.getDefault() */
    private static final class LocaleHolder {
        static final java.util.Locale LOCALE = java.util.Locale.CHINA;

        private LocaleHolder() {
            // 常量类禁止实例化
        }
    }
}
