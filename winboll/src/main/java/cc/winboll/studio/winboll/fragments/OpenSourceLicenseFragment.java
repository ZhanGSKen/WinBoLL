package cc.winboll.studio.winboll.fragments;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Describe 开源许可说明窗口 Fragment（Java 7 语法）。
 * 列出本程序分发的第三方开源组件及其许可证，对 LGPL 组件做显著标注；
 * 终端功能由独立 Termux 应用经 Intent 提供，不列入分发组件，仅在页面底部说明。
 * 遵循 DrawerFragmentActivity 架构，通过 addFragment()/showFragment() 组装。
 */

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import cc.winboll.studio.winboll.R;

public class OpenSourceLicenseFragment extends Fragment implements View.OnClickListener {

    // Fragment 标题（主窗口副标题使用，Fragment 的标题就是副标题）
    public static final String FRAGMENT_TITLE = "OpenSource";

    // 组件数据：名称、许可证、说明、源码地址
    private static final LicenseItem[] LICENSE_ITEMS = new LicenseItem[] {
        new LicenseItem(
            "AndroidX / Jetpack",
            "Apache-2.0",
            "Google Android Jetpack 组件库（AppCompat、RecyclerView、Preference、ViewPager、Biometric、DrawerLayout 等）。",
            "https://developer.android.com/jetpack"),
        new LicenseItem(
            "Material Components for Android",
            "Apache-2.0",
            "Google Material Design 组件库（com.google.android.material:material:1.4.0）。",
            "https://github.com/material-components/material-components-android"),
        new LicenseItem(
            "Gson",
            "Apache-2.0",
            "Google JSON 序列化 / 反序列化库（com.google.code.gson:gson:2.10.1）。",
            "https://github.com/google/gson"),
        new LicenseItem(
            "Fastjson",
            "Apache-2.0",
            "阿里巴巴高性能 JSON 处理库（com.alibaba:fastjson:1.2.76）。",
            "https://github.com/alibaba/fastjson"),
        new LicenseItem(
            "OkHttp",
            "Apache-2.0",
            "Square HTTP 网络客户端（com.squareup.okhttp3:okhttp:4.4.1）。",
            "https://github.com/square/okhttp"),
        new LicenseItem(
            "jsoup",
            "MIT",
            "HTML 解析与处理库（org.jsoup:jsoup:1.13.1）。",
            "https://github.com/jhy/jsoup"),
        new LicenseItem(
            "Glide",
            "BSD-3-Clause",
            "图片加载与缓存库（com.github.bumptech.glide:glide:4.9.0，部分子组件为 MIT / Apache-2.0）。",
            "https://github.com/bumptech/glide"),
        new LicenseItem(
            "PullRefreshLayout",
            "MIT",
            "下拉刷新控件（com.baoyz.pullrefreshlayout:library:1.2.0）。",
            "https://github.com/baoyongzhang/android-PullRefreshLayout"),
        new LicenseItem(
            "Android About Page",
            "MIT",
            "关于页面快速构建库（io.github.medyo:android-about-page:2.0.0）。",
            "https://github.com/medyo/android-about-page"),
        new LicenseItem(
            "Pinyin4j",
            "LGPL-2.1",
            "汉字转拼音库（com.github.open-android:pinyin4j:2.5.0）。以独立、未经修改的库形式引用，允许替换。",
            "https://github.com/open-android/pinyin4j"),
        new LicenseItem(
            "ZXing",
            "Apache-2.0",
            "条形码 / 二维码处理（com.google.zxing:core:3.4.1；com.journeyapps:zxing-android-embedded:3.6.0）。",
            "https://github.com/zxing/zxing"),
        new LicenseItem(
            "JSch",
            "BSD-3-Clause",
            "SSH2 协议 Java 实现（com.jcraft:jsch:0.1.55）。",
            "https://github.com/mwiede/jsch"),
        new LicenseItem(
            "Guava",
            "Apache-2.0",
            "Google Java 核心工具库（com.google.guava:guava:24.1-jre）。",
            "https://github.com/google/guava"),
    };

    private LinearLayout mListContainer;

    // 工厂方法（Java 7 显式工厂模式）
    public static OpenSourceLicenseFragment newInstance() {
        return new OpenSourceLicenseFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_open_source_license, container, false);
        mListContainer = (LinearLayout) view.findViewById(R.id.ll_license_list);
        buildLicenseCards(inflater);
        return view;
    }

    /**
     * 动态构建各开源组件许可卡片。
     */
    private void buildLicenseCards(LayoutInflater inflater) {
        mListContainer.removeAllViews();
        for (int i = 0; i < LICENSE_ITEMS.length; i++) {
            final LicenseItem item = LICENSE_ITEMS[i];
            View card = inflater.inflate(R.layout.item_open_source_license, mListContainer, false);

            TextView tvName = (TextView) card.findViewById(R.id.tv_license_name);
            TextView tvType = (TextView) card.findViewById(R.id.tv_license_type);
            TextView tvDesc = (TextView) card.findViewById(R.id.tv_license_desc);
            TextView tvUrl = (TextView) card.findViewById(R.id.tv_license_url);

            tvName.setText(item.name);
            tvType.setText(item.license);
            tvDesc.setText(item.desc);
            tvUrl.setText(item.url);
            tvUrl.setTag(item.url);
            tvUrl.setOnClickListener(this);

            // 点击"查看完整许可证"在对话框内展示随附的 LICENSE 全文
            TextView tvView = (TextView) card.findViewById(R.id.tv_license_view);
            tvView.setTag(item);
            tvView.setOnClickListener(this);

            // LGPL 弱著佐权组件显著标注，其余宽松许可使用普通标签
            boolean isLgpl = item.license != null && item.license.startsWith("LGPL");
            if (isLgpl) {
                tvType.setBackgroundResource(R.drawable.bg_license_badge_lgpl);
                tvType.setTextColor(0xFFE65100);
            } else {
                tvType.setBackgroundResource(R.drawable.bg_license_badge_normal);
                tvType.setTextColor(0xFF455A64);
            }

            mListContainer.addView(card);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_license_url) {
            Object tag = v.getTag();
            if (tag instanceof String && getActivity() != null) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) tag));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    // 无可用浏览器时忽略
                }
            }
        } else if (id == R.id.tv_license_view) {
            Object tag = v.getTag();
            if (tag instanceof LicenseItem) {
                showLicenseDialog((LicenseItem) tag);
            }
        }
    }

    /**
     * 在可滚动对话框中展示随附的许可证全文（LICENSE 文本打包在 assets 内）。
     */
    private void showLicenseDialog(LicenseItem item) {
        if (getActivity() == null) {
            return;
        }
        String text = readAssetText(licenseAsset(item.license));

        ScrollView scrollView = new ScrollView(getActivity());
        TextView tv = new TextView(getActivity());
        tv.setTextIsSelectable(true);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tv.setLineSpacing(2f, 1.0f);
        tv.setText(text);
        int pad = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
                getResources().getDisplayMetrics());
        tv.setPadding(pad, pad, pad, pad);
        scrollView.addView(tv);

        new AlertDialog.Builder(getActivity())
                .setTitle(item.name + "  ·  " + item.license)
                .setView(scrollView)
                .setPositiveButton("关闭", null)
                .show();
    }

    /**
     * 许可证类型到 assets 内 LICENSE 全文文件的映射。
     */
    private String licenseAsset(String license) {
        if ("MIT".equals(license)) {
            return "open_source_licenses/MIT.txt";
        } else if ("BSD-3-Clause".equals(license)) {
            return "open_source_licenses/BSD-3-Clause.txt";
        } else if (license != null && license.startsWith("LGPL")) {
            return "open_source_licenses/LGPL-2.1.txt";
        }
        return "open_source_licenses/Apache-2.0.txt";
    }

    /**
     * 以 UTF-8 读取 assets 中的文本文件。
     */
    private String readAssetText(String assetName) {
        StringBuilder sb = new StringBuilder();
        InputStream is = null;
        try {
            is = getActivity().getAssets().open(assetName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            reader.close();
        } catch (Exception e) {
            sb.setLength(0);
            sb.append("许可证文本加载失败：").append(e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ignored) {
                    // ignore
                }
            }
        }
        return sb.toString();
    }

    /**
     * 返回 Fragment 标题，供主窗口副标题显示。
     */
    public String getFragmentTitle() {
        return FRAGMENT_TITLE;
    }

    /**
     * 开源组件许可信息数据结构。
     */
    private static final class LicenseItem {
        final String name;
        final String license;
        final String desc;
        final String url;

        LicenseItem(String name, String license, String desc, String url) {
            this.name = name;
            this.license = license;
            this.desc = desc;
            this.url = url;
        }
    }
}
