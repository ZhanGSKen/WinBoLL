package cc.winboll.studio.claudecodeandroid.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import org.json.JSONObject;

import cc.winboll.studio.claudecodeandroid.AppSettings;
import cc.winboll.studio.claudecodeandroid.R;
import cc.winboll.studio.claudecodeandroid.ViewColorTable;
import cc.winboll.studio.claudecodeandroid.net.BridgeApi;
import cc.winboll.studio.claudecodeandroid.utils.LogUtils;

/**
 * 设置页：桥接地址、访问令牌、默认工作目录。
 *
 * <p>提供「测试连接」按钮，可直接验证 token 与 claude.exe 是否就绪。</p>
 *
 * @author 豆包&amp;Big Pickle&amp;MiMo&amp;ZhanGSKen
 * @since 1.0.0
 */
public class SettingsActivity extends BaseWinBoLLActivity {

    private static final String TAG = "SettingsActivity";

    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private EditText urlInput;
    private EditText tokenInput;
    private EditText cwdInput;
    private TextView resultView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 工具栏（ASupportToolbar）：子窗口，显示返回箭头
        Toolbar toolbar = bindToolbar(R.id.toolbar, true);
        if (toolbar != null) {
            toolbar.setTitle(R.string.title_settings);
            toolbar.setSubtitle(TAG);
        }

        final LinearLayout container = (LinearLayout) findViewById(R.id.content_container);
        if (container != null) {
            container.addView(buildRootView());
        }
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    private View buildRootView() {
        final LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(ViewColorTable.COLOR_BG_GRAY);
        root.setPadding(dp(18), dp(18), dp(18), dp(18));

        root.addView(buildLabel("桥接服务地址"));
        urlInput = buildInput(AppSettings.getServerUrl(this), "http://192.168.123.202:8787");
        root.addView(urlInput);

        root.addView(buildLabel("访问令牌（bridge/config.json 中的 token）"));
        tokenInput = buildInput(AppSettings.getToken(this), "粘贴 token");
        root.addView(tokenInput);

        root.addView(buildLabel("默认工作目录"));
        cwdInput = buildInput(AppSettings.getCwd(this), "C:\\0WINBOT");
        root.addView(cwdInput);

        final LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setPadding(0, dp(16), 0, dp(8));

        final Button save = new Button(this);
        save.setText("保存");
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                saveSettings();
            }
        });
        buttons.addView(save, weightParams());

        final Button test = new Button(this);
        test.setText("测试连接");
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                saveSettings();
                testConnection();
            }
        });
        buttons.addView(test, weightParams());
        root.addView(buttons);

        resultView = new TextView(this);
        resultView.setTextSize(12.0f);
        resultView.setTextColor(ViewColorTable.COLOR_SECONDARY_TEXT);
        root.addView(resultView);

        return root;
    }

    private TextView buildLabel(final String text) {
        final TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(12.0f);
        label.setTextColor(ViewColorTable.COLOR_SECONDARY_TEXT);
        label.setPadding(0, dp(12), 0, dp(4));
        return label;
    }

    private EditText buildInput(final String value, final String hint) {
        final EditText input = new EditText(this);
        input.setText(value);
        input.setHint(hint);
        input.setTextSize(13.0f);
        input.setSingleLine(true);
        input.setBackgroundColor(ViewColorTable.COLOR_BG_WHITE);
        input.setPadding(dp(10), dp(8), dp(10), dp(8));
        return input;
    }

    private LinearLayout.LayoutParams weightParams() {
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        params.setMargins(0, 0, dp(8), 0);
        return params;
    }

    private int dp(final int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void saveSettings() {
        AppSettings.setServerUrl(this,
                AppSettings.normalizeBaseUrl(urlInput.getText().toString()));
        AppSettings.setToken(this, tokenInput.getText().toString().trim());
        AppSettings.setCwd(this, cwdInput.getText().toString().trim());
        Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
    }

    private void testConnection() {
        final String baseUrl = AppSettings.normalizeBaseUrl(AppSettings.getServerUrl(this));
        final String token = AppSettings.getToken(this);
        resultView.setText("正在连接 " + baseUrl + " …");
        resultView.setTextColor(ViewColorTable.COLOR_SECONDARY_TEXT);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject ping = BridgeApi.ping(baseUrl, token);
                    final int code = ping.optInt("_httpCode", 0);
                    if (code != 200) {
                        throw new java.io.IOException("HTTP " + code + "，检查地址与 token");
                    }
                    final boolean exeOk = ping.optBoolean("claudeExecutableFound", false);
                    final String text = "连接成功 · 模型 " + ping.optString("model", "?")
                            + " · claude.exe " + (exeOk ? "就绪" : "未找到");
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            resultView.setText(text);
                            resultView.setTextColor(exeOk ? ViewColorTable.COLOR_SUCCESS
                                    : ViewColorTable.COLOR_WARNING);
                        }
                    });
                } catch (final Exception e) {
                    LogUtils.e(TAG, "ping failed", e);
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            resultView.setText("连接失败：" + e.getMessage());
                            resultView.setTextColor(ViewColorTable.COLOR_ERROR);
                        }
                    });
                }
            }
        }, "ping").start();
    }
}
