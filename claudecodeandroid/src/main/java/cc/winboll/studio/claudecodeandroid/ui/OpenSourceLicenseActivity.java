package cc.winboll.studio.claudecodeandroid.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import cc.winboll.studio.claudecodeandroid.R;
import cc.winboll.studio.claudecodeandroid.utils.LogUtils;

/**
 * 开源许可窗口（仿 WinBoLL Project 的 OpenSourceLicenseFragment）。
 *
 * <p>列出本程序分发的第三方开源组件与自有库，说明其许可证与源码地址；
 * 点击"查看完整许可证"可读取随附在 assets 内的许可证全文。</p>
 *
 * @author 豆包&amp;ZhanGSKen
 * @since 1.0.0
 */
public class OpenSourceLicenseActivity extends BaseWinBoLLActivity implements View.OnClickListener {

    public static final String TAG = "OpenSourceLicenseActivity";

    /** 许可证全文在 assets 内的文件名（即仓库根 LICENSE 的 Apache-2.0 全文）。 */
    private static final String ASSET_LICENSE = "LICENSE.txt";

    /** 组件数据：名称、许可证、说明、源码地址；isSelf 标记自有组件（读取随包 LICENSE 全文）。 */
    private static final LicenseItem[] LICENSE_ITEMS = new LicenseItem[]{
            new LicenseItem(
                    "ClaudeCodeAndroid 本项目",
                    "Apache-2.0",
                    "本程序源码依据仓库根 LICENSE 的 Apache-2.0 授权分发。",
                    "https://gitea.winboll.cc/Studio/WinBoLL",
                    true),
            new LicenseItem(
                    "libaes（WinBoLL 自有）",
                    "Apache-2.0",
                    "WinBoLL 自有组件：AES 主题、工具栏与 Activity 框架库，不属第三方依赖。",
                    "https://gitea.winboll.cc/Studio/WinBoLL",
                    true),
            new LicenseItem(
                    "libappbase（WinBoLL 自有）",
                    "Apache-2.0",
                    "WinBoLL 自有组件：日志、提示、关于页与通用模型库，不属第三方依赖。",
                    "https://gitea.winboll.cc/Studio/WinBoLL",
                    true),
            new LicenseItem(
                    "AndroidX / Jetpack",
                    "Apache-2.0",
                    "Google Android Jetpack 组件库（AppCompat、Core、Fragment、RecyclerView 等），随 AppCompat 主题与工具栏能力引入。",
                    "https://developer.android.com/jetpack",
                    false)
    };

    private LinearLayout mListContainer;

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate: enter");
        setContentView(R.layout.activity_open_source_license);

        Toolbar toolbar = bindToolbar(R.id.toolbar, true);
        if (toolbar != null) {
            toolbar.setTitle(R.string.title_open_source);
            toolbar.setSubtitle(TAG);
        }

        mListContainer = (LinearLayout) findViewById(R.id.ll_license_list);
        if (mListContainer != null) {
            buildLicenseCards();
        }
    }

    /**
     * 动态构建各开源组件许可卡片（Java 7 语法，不使用布局资源）。
     */
    private void buildLicenseCards() {
        mListContainer.removeAllViews();
        for (int i = 0; i < LICENSE_ITEMS.length; i++) {
            final LicenseItem item = LICENSE_ITEMS[i];
            mListContainer.addView(buildCard(item));
        }
    }

    private View buildCard(LicenseItem item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(8));
        bg.setStroke(1, 0xFFE0E0E0);
        card.setBackground(bg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(dp(10), dp(5), dp(10), dp(5));
        card.setLayoutParams(cardParams);

        // ---- 名称 + 许可证标签 ----
        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView tvName = new TextView(this);
        tvName.setText(item.name);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tvName.setTextColor(0xFF212121);
        tvName.setTypeface(Typeface.DEFAULT_BOLD);
        titleRow.addView(tvName, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView tvType = new TextView(this);
        tvType.setText(item.license);
        tvType.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tvType.setTextColor(0xFF455A64);
        tvType.setTypeface(Typeface.DEFAULT_BOLD);
        tvType.setPadding(dp(7), dp(2), dp(7), dp(2));
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(0xFFECEFF1);
        badgeBg.setCornerRadius(dp(4));
        tvType.setBackground(badgeBg);
        titleRow.addView(tvType, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        card.addView(titleRow);

        // ---- 说明 ----
        TextView tvDesc = new TextView(this);
        tvDesc.setText(item.desc);
        tvDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvDesc.setTextColor(0xFF616161);
        tvDesc.setLineSpacing(dp(2), 1.0f);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        descParams.topMargin = dp(5);
        card.addView(tvDesc, descParams);

        // ---- 源码地址（可点击跳转浏览器）----
        TextView tvUrl = new TextView(this);
        tvUrl.setText(item.url);
        tvUrl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tvUrl.setTextColor(0xFF1565C0);
        tvUrl.setTag(item.url);
        tvUrl.setOnClickListener(this);
        LinearLayout.LayoutParams urlParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        urlParams.topMargin = dp(5);
        card.addView(tvUrl, urlParams);

        // ---- 查看完整许可证 ----
        TextView tvView = new TextView(this);
        tvView.setText(R.string.open_source_license_view_full);
        tvView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvView.setTextColor(0xFF1565C0);
        tvView.setTypeface(Typeface.DEFAULT_BOLD);
        tvView.setPadding(0, dp(2), 0, dp(2));
        tvView.setTag(item);
        tvView.setOnClickListener(this);
        LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        viewParams.topMargin = dp(6);
        card.addView(tvView, viewParams);

        return card;
    }

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();
        if (tag instanceof String) {
            // 源码地址：交给系统浏览器
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) tag));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                LogUtils.e(TAG, "onClick: 无法打开浏览器", e);
            }
        } else if (tag instanceof LicenseItem) {
            showLicenseDialog((LicenseItem) tag);
        }
    }

    /**
     * 在可滚动对话框中展示许可证全文。
     */
    private void showLicenseDialog(LicenseItem item) {
        String text;
        if (item.isSelf) {
            text = readAssetText(ASSET_LICENSE);
        } else {
            text = getString(R.string.open_source_license_not_bundled) + "\n\n" + item.url;
        }

        ScrollView scrollView = new ScrollView(this);
        TextView tv = new TextView(this);
        tv.setTextIsSelectable(true);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tv.setLineSpacing(2f, 1.0f);
        tv.setText(text);
        tv.setPadding(dp(16), dp(16), dp(16), dp(16));
        scrollView.addView(tv);

        new AlertDialog.Builder(this)
                .setTitle(item.name + "  ·  " + item.license)
                .setView(scrollView)
                .setPositiveButton("关闭", null)
                .show();
    }

    /**
     * 以 UTF-8 读取 assets 中的文本文件。
     */
    private String readAssetText(String assetName) {
        StringBuilder sb = new StringBuilder();
        InputStream is = null;
        try {
            is = getAssets().open(assetName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            reader.close();
        } catch (Exception e) {
            sb.setLength(0);
            sb.append(getString(R.string.open_source_license_read_failed)).append(e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ignored) {
                    // 关闭失败忽略
                }
            }
        }
        return sb.toString();
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }

    /**
     * 开源组件许可信息数据结构。
     */
    private static final class LicenseItem {
        final String name;
        final String license;
        final String desc;
        final String url;
        final boolean isSelf;

        LicenseItem(String name, String license, String desc, String url, boolean isSelf) {
            this.name = name;
            this.license = license;
            this.desc = desc;
            this.url = url;
            this.isSelf = isSelf;
        }
    }
}
