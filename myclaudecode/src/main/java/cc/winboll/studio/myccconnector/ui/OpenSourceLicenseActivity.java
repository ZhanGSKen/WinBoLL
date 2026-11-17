/*
 * Copyright 2026 WinBoLL Studio
 * SPDX-License-Identifier: Apache-2.0
 */

package cc.winboll.studio.myccconnector.ui;

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

import cc.winboll.studio.myccconnector.R;
import cc.winboll.studio.myccconnector.utils.LogUtils;

/**
 * 开源许可窗口（仿 WinBoLL Project 的 OpenSourceLicenseFragment）。
 *
 * <p>逐项列出本程序分发的自有库与第三方组件，说明其许可证与源码地址；
 * 点击"查看完整许可证"可读取随附在 assets 内的许可证全文。清单口径与
 * 实际打包内容一致（依据 betaDebugRuntimeClasspath 依赖树与 APK dex 实证）。</p>
 *
 * @author 豆包&amp;ZhanGSKen
 * @since 1.0.0
 */
public class OpenSourceLicenseActivity extends BaseWinBoLLActivity implements View.OnClickListener {

    public static final String TAG = "OpenSourceLicenseActivity";

    /** 本项目许可证全文（即仓库根 LICENSE 的 Apache-2.0 全文）。 */
    private static final String ASSET_PROJECT_LICENSE = "LICENSE.txt";

    /** 框架库随包的第三方许可文本目录。 */
    private static final String ASSET_DIR = "open_source_licenses/";

    private static final String ASSET_APACHE = ASSET_DIR + "Apache-2.0.txt";
    private static final String ASSET_APPBASE = ASSET_DIR + "APPBase-LICENSE.txt";
    private static final String ASSET_GLIDE = ASSET_DIR + "Glide-LICENSE.txt";

    private static final String URL_WINBOLL = "https://gitea.winboll.cc/Studio/WinBoLL";

    /** 组件数据：名称、许可证、说明、源码地址、随包许可证文本（null 表示未随包）。 */
    private static final LicenseItem[] LICENSE_ITEMS = new LicenseItem[]{
            // ---- 自有组件 ----
            new LicenseItem(
                    "MyCCConnector 本项目",
                    "Apache-2.0",
                    "本程序源码（含自有库 libaes / libappbase，源码同在本项目远程仓库）依据仓库根 LICENSE 的 Apache-2.0 授权分发。",
                    URL_WINBOLL,
                    ASSET_PROJECT_LICENSE),
            new LicenseItem(
                    "libaes（WinBoLL 自有）",
                    "Apache-2.0",
                    "WinBoLL 自有组件：AES 主题、工具栏与 Activity 框架库，不属第三方依赖。",
                    URL_WINBOLL,
                    ASSET_APACHE),
            new LicenseItem(
                    "libappbase（WinBoLL 自有）",
                    "Apache-2.0",
                    "WinBoLL 自有组件：日志、提示、关于页与通用模型库，不属第三方依赖。",
                    URL_WINBOLL,
                    ASSET_APPBASE),
            // ---- 平台与第三方组件（随本程序打包）----
            new LicenseItem(
                    "AndroidX / Jetpack",
                    "Apache-2.0",
                    "Google Android Jetpack 组件库（AppCompat、Core、Fragment、RecyclerView 等），随 AppCompat 主题与工具栏能力引入。",
                    "https://developer.android.com/jetpack",
                    ASSET_APACHE),
            new LicenseItem(
                    "Tink（Android 加密库）",
                    "Apache-2.0",
                    "Google Tink 的 Android 版本（1.5.0），由 AndroidX Security 在运行时传递引入，用于访问令牌等敏感配置的加密存储。",
                    "https://github.com/google/tink",
                    ASSET_APACHE),
            new LicenseItem(
                    "OkHttp",
                    "Apache-2.0",
                    "Square 的 HTTP 客户端（4.4.1），由 libaes 传递引入。本程序自身的网络请求使用平台 HttpURLConnection，未直接调用该库。",
                    "https://github.com/square/okhttp",
                    ASSET_APACHE),
            new LicenseItem(
                    "Kotlin Standard Library",
                    "Apache-2.0",
                    "JetBrains Kotlin 标准库（1.3.61），作为 OkHttp 4.x 的运行时依赖引入。",
                    "https://github.com/JetBrains/kotlin",
                    ASSET_APACHE),
            new LicenseItem(
                    "Gson",
                    "Apache-2.0",
                    "Google 的 JSON 序列化库（2.8.5），由 libaes 传递引入。",
                    "https://github.com/google/gson",
                    ASSET_APACHE),
            new LicenseItem(
                    "ZXing Core / zxing-android-embedded",
                    "Apache-2.0",
                    "二维码编解码库（core 3.4.1）与 JourneyApps 的 Android 封装，由 libaes 的二维码能力引入。",
                    "https://github.com/zxing/zxing",
                    ASSET_APACHE),
            new LicenseItem(
                    "Glide",
                    "BSD-2-Clause / Apache-2.0 / MIT",
                    "图片加载库（4.9.0），含 gif 解码与磁盘缓存子模块，由 libaes 传递引入。其许可为多许可证组合，全文随包分发。",
                    "https://github.com/bumptech/glide",
                    ASSET_GLIDE),
            new LicenseItem(
                    "PullRefreshLayout",
                    "Apache-2.0",
                    "下拉刷新控件（library 1.2.0），由 libaes 传递引入。",
                    "https://github.com/baoyongzhang/android-PullRefreshLayout",
                    ASSET_APACHE)
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
     * 在可滚动对话框中展示许可证全文；未随包时提示查看源码地址。
     */
    private void showLicenseDialog(LicenseItem item) {
        final String text;
        if (item.assetName != null) {
            text = readAssetText(item.assetName);
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
        /** 随包许可证文本的 assets 路径；null 表示未随包。 */
        final String assetName;

        LicenseItem(String name, String license, String desc, String url, String assetName) {
            this.name = name;
            this.license = license;
            this.desc = desc;
            this.url = url;
            this.assetName = assetName;
        }
    }
}
