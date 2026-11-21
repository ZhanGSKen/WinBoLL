package cc.winboll.studio.winboll.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2026/11/02 20:05
 * @Describe HttpAuthHelper —— HTTP Auth（Basic/Digest）网页验证模块。
 *
 * 当网页返回 401 并携带 WWW-Authenticate 时，WebView 回调
 * WebViewClient.onReceivedHttpAuthRequest，由本模块统一处理：
 *
 * 1. 已有可用凭据（会话缓存 / 持久保存）→ 直接 handler.proceed() 静默通过；
 * 2. 无凭据 → 弹出对话框输入用户名/密码，可勾选「保持登录状态」持久保存；
 * 3. 短时间连续挑战超过阈值才判定凭据被拒，重新弹窗（仅覆盖、不删凭据）；
 * 4. 同一保护空间（host+realm）的并发挑战合并在同一个对话框中应答。
 *
 * 保持登录状态两级：
 * - 会话级：进程内有效（Map 缓存），未勾选保存时也保持到应用退出；
 * - 持久级：勾选「保持登录状态」后写入 SharedPreferences，重启后免输入。
 */
public class HttpAuthHelper {
    public static final String TAG = "HttpAuthHelper";

    /** 持久凭据存储。 */
    private static final String PREFS_NAME = "winboll_http_auth";

    /** 进程内会话凭据：key(host+realm) -> {username, password}。 */
    private static final Map<String, String[]> sSessionCreds = new HashMap<String, String[]>();

    /** 同一保护空间的挑战时间线（识别缓存丢失重挑与凭据被拒的区别）。 */
    private static final Map<String, Long> sLastChallengeAt = new HashMap<String, Long>();

    /** 连续挑战计数（静默期后归 1）。 */
    private static final Map<String, Integer> sBurst = new HashMap<String, Integer>();

    /** 静默期：超过该间隔的挑战视为新一轮（缓存丢失/新页面），重新允许静默通过。 */
    private static final long BURST_QUIET_MS = 5000;

    /**
     * 并发首发窗口：相邻挑战间隔小于该值视为「并行加载的多个资源各自首发挑战」
     * （页面 HTML/JS/图片/曲目同时请求，凭据库尚未生效时会一起回调），
     * 不计入被拒计数；真正的凭据被拒是 proceed 后隔一个网络往返再挑战，
     * 间隔必然大于该窗口。
     */
    private static final long PARALLEL_CHALLENGE_MS = 500;

    /** 连续静默重试上限：排除并发首发后，连续再挑战超过该值才认为凭据被拒并弹窗。 */
    private static final int MAX_AUTO_RETRY = 6;

    /** 正在弹窗应答中的保护空间及其排队的 handler（并发挑战合并）。 */
    private static final Map<String, List<HttpAuthHandler>> sPending = new HashMap<String, List<HttpAuthHandler>>();

    private HttpAuthHelper() {
    }

    /**
     * WebViewClient.onReceivedHttpAuthRequest 的统一入口。
     *
     * @param context 用于弹窗的上下文（Activity），为空时取消本次验证
     * @param view    发起挑战的 WebView（用于同步 WebView 自带凭据库）
     * @param handler 系统回调句柄，必须 proceed 或 cancel
     * @param host    服务器主机
     * @param realm   保护空间名（可为 null）
     */
    public static void handle(Context context, WebView view, HttpAuthHandler handler,
                              String host, String realm) {
        if (handler == null) {
            return;
        }
        if (context == null) {
            handler.cancel();
            return;
        }
        final String key = makeKey(host, realm);

        // 同一保护空间已有对话框在等待输入 → 排队，由同一对话框统一应答
        List<HttpAuthHandler> pending = sPending.get(key);
        if (pending != null) {
            pending.add(handler);
            return;
        }

        // 挑战间隔统计：静默期后的挑战视为新一轮（1）；短时间连续挑战累加。
        // 关键区分：切到托管播放再切回（SW/网络缓存丢失）导致的“再次挑战”是
        // 单发的，凭据仍有效，必须静默通过；只有短时间内连续挑战多次才说明
        // 凭据真的被服务器拒绝。绝不能因再次挑战就删除已保存凭据。
        // 并发首发窗口（<500ms）内的挑战是并行资源加载的各自首发，不计入
        // 被拒计数——否则一个含多资源的页面就能把 burst 顶过阈值误弹窗。
        long now = android.os.SystemClock.elapsedRealtime();
        Long last = sLastChallengeAt.get(key);
        int burst;
        if (last == null || (now - last) >= BURST_QUIET_MS) {
            burst = 1;
        } else if ((now - last) < PARALLEL_CHALLENGE_MS) {
            // 并发首发：保持当前计数（首轮为 1），不累加。
            burst = sBurst.containsKey(key) ? sBurst.get(key) : 1;
        } else {
            burst = (sBurst.containsKey(key) ? sBurst.get(key) : 1) + 1;
        }
        sLastChallengeAt.put(key, now);
        sBurst.put(key, burst);
        Log.d(TAG, "challenge host=" + host + " realm=" + realm
            + " gap=" + (last == null ? -1 : (now - last)) + "ms burst=" + burst);

        // 会话缓存 → 持久保存，命中即静默通过（不删除、不弹窗）
        String[] cred = sSessionCreds.get(key);
        if (cred == null) {
            cred = loadStored(context, key);
            if (cred != null) {
                sSessionCreds.put(key, cred);
            }
        }
        if (cred != null && burst <= MAX_AUTO_RETRY) {
            syncWebViewStore(view, host, realm, cred[0], cred[1]);
            handler.proceed(cred[0], cred[1]);
            return;
        }

        // 无凭据，或短时间连续挑战超过重试上限（凭据已被拒）→ 弹窗。
        // 已存凭据不删除，仅作用户名预填；用户输入新凭据后覆盖。
        pending = new ArrayList<HttpAuthHandler>();
        pending.add(handler);
        sPending.put(key, pending);
        promptForCredentials(context, view, host, realm, key,
            cred != null ? cred[0] : null);
    }

    /**
     * 丢弃全部已保存凭据（设置页「清除登录信息」可调用）。
     */
    public static void clearCredentials(Context context) {
        sSessionCreds.clear();
        sBurst.clear();
        sLastChallengeAt.clear();
        if (context != null) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().clear().apply();
        }
    }

    // ------------------------------------------------------------------
    // 对话框
    // ------------------------------------------------------------------

    private static void promptForCredentials(final Context context, final WebView view,
                                             final String host, final String realm,
                                             final String key, String prefillUser) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(context, 20);
        layout.setPadding(pad, dp(context, 8), pad, 0);

        TextView msg = new TextView(context);
        String realmText = (realm == null || realm.length() == 0) ? "" : (" (" + realm + ")");
        msg.setText("服务器 " + host + realmText + " 需要用户名和密码");
        msg.setTextSize(14);
        LinearLayout.LayoutParams msgLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        msgLp.bottomMargin = dp(context, 12);
        layout.addView(msg, msgLp);

        final EditText etUser = new EditText(context);
        etUser.setHint("用户名");
        etUser.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        etUser.setSingleLine(true);
        if (!TextUtils.isEmpty(prefillUser)) {
            etUser.setText(prefillUser);
            etUser.setSelection(prefillUser.length());
        }
        layout.addView(etUser, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final EditText etPass = new EditText(context);
        etPass.setHint("密码");
        etPass.setSingleLine(true);
        // 注意顺序：setSingleLine 之后再 setInputType，否则密码掩码会被重置；
        // 再显式设置 PasswordTransformationMethod 兜底。
        etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etPass.setTransformationMethod(
            android.text.method.PasswordTransformationMethod.getInstance());
        LinearLayout.LayoutParams passLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        passLp.topMargin = dp(context, 6);
        layout.addView(etPass, passLp);

        final CheckBox cbRemember = new CheckBox(context);
        cbRemember.setText("保持登录状态");
        cbRemember.setChecked(true);
        LinearLayout.LayoutParams cbLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cbLp.topMargin = dp(context, 4);
        layout.addView(cbRemember, cbLp);

        // 防止确定/取消/返回键重复应答 handler
        final boolean[] answered = new boolean[]{false};

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("身份验证")
            .setView(layout)
            .setCancelable(true)
            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int which) {
                    if (answered[0]) {
                        return;
                    }
                    answered[0] = true;
                    String user = etUser.getText().toString().trim();
                    String pass = etPass.getText().toString();
                    if (user.length() == 0) {
                        // 用户名为空视为取消，避免 proceed 空凭据形成死循环
                        answerCancel(key);
                        return;
                    }
                    if (cbRemember.isChecked()) {
                        saveStored(context, key, user, pass);
                    } else {
                        removeStored(context, key);
                    }
                    syncWebViewStore(view, host, realm, user, pass);
                    answerProceed(key, user, pass);
                }
            })
            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int which) {
                    if (answered[0]) {
                        return;
                    }
                    answered[0] = true;
                    answerCancel(key);
                }
            })
            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface d) {
                    if (answered[0]) {
                        return;
                    }
                    answered[0] = true;
                    answerCancel(key);
                }
            })
            .create();
        dialog.show();
    }

    /** 对话框确认：向排队的全部 handler 发送凭据。 */
    private static void answerProceed(String key, String user, String pass) {
        sSessionCreds.put(key, new String[]{user, pass});
        // 新凭据重新给足静默通过的机会（重置连续挑战计数）
        sBurst.put(key, 0);
        List<HttpAuthHandler> list = sPending.remove(key);
        if (list == null) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            try {
                list.get(i).proceed(user, pass);
            } catch (Throwable t) {
                // handler 可能已失效（页面已离开），忽略
            }
        }
    }

    /** 对话框取消：向排队的全部 handler 发送取消。 */
    private static void answerCancel(String key) {
        List<HttpAuthHandler> list = sPending.remove(key);
        if (list == null) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            try {
                list.get(i).cancel();
            } catch (Throwable t) {
                // 同上，忽略失效 handler
            }
        }
    }

    /**
     * 按 host 查询可用凭据（任意 realm）。
     * 供原生网络层复用（如托管引擎拉取 tracks[].url，bridge-spec §3.1）：
     * 该 host 处于验证保护空间内时，给请求带上 Authorization 头。
     *
     * @return {username, password}，无可用凭据返回 null
     */
    public static String[] getCredentialsForHost(Context context, String host) {
        if (context == null || host == null || host.length() == 0) {
            return null;
        }
        String prefix = host + "\n";
        // 会话级优先（后台线程也可能调用，整体容错）
        try {
            for (Map.Entry<String, String[]> e : sSessionCreds.entrySet()) {
                if (e.getKey().startsWith(prefix) && e.getValue() != null) {
                    return e.getValue();
                }
            }
            // 持久级
            Map<String, ?> all = prefs(context).getAll();
            for (Map.Entry<String, ?> e : all.entrySet()) {
                if (e.getKey().startsWith(prefix)) {
                    String[] cred = loadStored(context, e.getKey());
                    if (cred != null) {
                        return cred;
                    }
                }
            }
        } catch (Throwable t) {
            // 并发/存储异常静默忽略（与本模块其余存取路径一致）
        }
        return null;
    }

    // ------------------------------------------------------------------
    // 凭据存取
    // ------------------------------------------------------------------

    private static String makeKey(String host, String realm) {
        return (host == null ? "" : host) + "\n" + (realm == null ? "" : realm);
    }

    /** 持久凭据编码：JSON {"u":user,"p":pass}，避免分隔符歧义。 */
    private static void saveStored(Context context, String key, String user, String pass) {
        try {
            JSONObject o = new JSONObject();
            o.put("u", user);
            o.put("p", pass);
            prefs(context).edit().putString(key, o.toString()).apply();
        } catch (Throwable t) {
            // JSON 异常理论上不会发生，忽略（本次仅会话级保持）
        }
    }

    private static String[] loadStored(Context context, String key) {
        try {
            String raw = prefs(context).getString(key, null);
            if (raw == null) {
                return null;
            }
            JSONObject o = new JSONObject(raw);
            return new String[]{o.optString("u", ""), o.optString("p", "")};
        } catch (Throwable t) {
            return null;
        }
    }

    private static void removeStored(Context context, String key) {
        prefs(context).edit().remove(key).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** 同步 WebView 自带 HttpAuth 凭据库，保持跨页面一致。 */
    @SuppressWarnings("deprecation")
    private static void syncWebViewStore(WebView view, String host, String realm,
                                         String user, String pass) {
        if (view != null) {
            try {
                view.setHttpAuthUsernamePassword(host, realm, user, pass);
            } catch (Throwable t) {
                // 个别 WebView 实现未开放，忽略（不影响自有存储）
            }
        }
    }

    private static int dp(Context context, int value) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (value * density + 0.5f);
    }
}
