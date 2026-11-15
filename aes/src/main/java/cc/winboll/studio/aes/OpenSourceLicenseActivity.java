package cc.winboll.studio.aes;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import cc.winboll.studio.libaes.utils.WinBoLLActivityManager;
import cc.winboll.studio.libappbase.LogUtils;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/15 12:00
 * @Describe 开源许可页：列出第三方组件与其许可证，并可查看 assets 随附的许可证全文。
 * 许可证全文位于 libaes/src/main/assets/open_source_licenses/。
 */
public class OpenSourceLicenseActivity extends BaseWinBoLLActivity {

    public static final String TAG = "OpenSourceLicenseActivity";

    /** Asset directory holding the full license texts. */
    private static final String LICENSE_DIR = "open_source_licenses/";

    private Toolbar mToolbar;
    private LinearLayout mListLayout;
    private ArrayList<LicenseEntry> mEntryList;

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_source_license);

        mListLayout = findViewById(R.id.license_list);
        mEntryList = buildEntryList();
        fillLicenseList();
        initToolbar();
    }

    private void initToolbar() {
        mToolbar = findViewById(R.id.toolbar);
        if (mToolbar == null) {
            LogUtils.e(TAG, "initToolbar() | Toolbar not found");
            return;
        }
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getString(R.string.open_source_license_title));
        mToolbar.setSubtitle(getTag());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 入口在「关于」页底部按钮，返回介绍页而不是主界面
                    WinBoLLActivityManager.getInstance().resumeActivity(AboutActivity.class);
                    WinBoLLActivityManager.getInstance().finish(OpenSourceLicenseActivity.this);
                }
            });
    }

    private void fillLicenseList() {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < mEntryList.size(); i++) {
            final LicenseEntry entry = mEntryList.get(i);
            View itemView = inflater.inflate(R.layout.item_open_source_license, mListLayout, false);
            TextView nameView = itemView.findViewById(R.id.item_name);
            TextView coordinateView = itemView.findViewById(R.id.item_coordinate);
            TextView licenseView = itemView.findViewById(R.id.item_license);
            TextView viewLicenseView = itemView.findViewById(R.id.item_view_license);
            nameView.setText(entry.name);
            coordinateView.setText(entry.coordinate);
            licenseView.setText(entry.license);
            viewLicenseView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showLicenseText(entry);
                    }
                });
            mListLayout.addView(itemView);
        }
    }

    private void showLicenseText(final LicenseEntry entry) {
        String licenseText;
        try {
            licenseText = readAssetText(LICENSE_DIR + entry.licenseFile);
        } catch (Exception e) {
            LogUtils.e(TAG, "showLicenseText() | read asset failed: " + e);
            Toast.makeText(getApplication(),
                    getString(R.string.open_source_license_read_failed) + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        TextView textView = new TextView(this);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        textView.setPadding(padding, padding, padding, padding);
        textView.setTextSize(12);
        textView.setTextIsSelectable(true);
        textView.setTypeface(android.graphics.Typeface.MONOSPACE);
        textView.setText(licenseText);
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(textView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(entry.name + " · " + entry.license);
        builder.setView(scrollView);
        builder.setPositiveButton("关闭", null);
        builder.show();
    }

    private String readAssetText(String path) throws Exception {
        InputStreamReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new InputStreamReader(getAssets().open(path), "UTF-8");
            char[] buffer = new char[4096];
            int length;
            while ((length = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, length);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return builder.toString();
    }

    private ArrayList<LicenseEntry> buildEntryList() {
        ArrayList<LicenseEntry> list = new ArrayList<LicenseEntry>();
        list.add(new LicenseEntry("AndroidX（AppCompat / RecyclerView / Lifecycle 等）",
                "androidx.*:1.0.0 – 1.5.0", "Apache-2.0", "Apache-2.0.txt",
                "https://developer.android.com/jetpack/androidx"));
        list.add(new LicenseEntry("Kotlin Stdlib",
                "org.jetbrains.kotlin:kotlin-stdlib:1.3.61", "Apache-2.0", "Apache-2.0.txt",
                "https://kotlinlang.org/"));
        list.add(new LicenseEntry("OkHttp",
                "com.squareup.okhttp3:okhttp:4.4.1", "Apache-2.0", "Apache-2.0.txt",
                "https://github.com/square/okhttp"));
        list.add(new LicenseEntry("Okio",
                "com.squareup.okio:okio:2.4.3", "Apache-2.0", "Apache-2.0.txt",
                "https://github.com/square/okio"));
        list.add(new LicenseEntry("Gson",
                "com.google.code.gson:gson:2.8.5", "Apache-2.0", "Apache-2.0.txt",
                "https://github.com/google/gson"));
        list.add(new LicenseEntry("Glide",
                "com.github.bumptech.glide:glide:4.9.0", "BSD-3-Clause", "BSD-3-Clause.txt",
                "https://github.com/bumptech/glide"));
        list.add(new LicenseEntry("jsoup",
                "org.jsoup:jsoup:1.13.1", "MIT", "MIT.txt", "https://jsoup.org/"));
        list.add(new LicenseEntry("ZXing Core",
                "com.google.zxing:core:3.4.1", "Apache-2.0", "Apache-2.0.txt",
                "https://github.com/zxing/zxing"));
        list.add(new LicenseEntry("ZXing Android Embedded",
                "com.journeyapps:zxing-android-embedded:3.6.0", "Apache-2.0", "Apache-2.0.txt",
                "https://github.com/journeyapps/zxing-android-embedded"));
        list.add(new LicenseEntry("JSch",
                "com.jcraft:jsch:0.1.55", "BSD-3-Clause", "BSD-3-Clause.txt",
                "http://www.jcraft.com/jsch/"));
        list.add(new LicenseEntry("PullRefreshLayout",
                "com.baoyz.pullrefreshlayout:library:1.2.0", "MIT", "MIT.txt",
                "https://github.com/baoyongzhang/android-PullRefreshLayout"));
        list.add(new LicenseEntry("Android About Page",
                "io.github.medyo:android-about-page:2.0.0", "MIT", "MIT.txt",
                "https://github.com/medyo/android-about-page"));
        list.add(new LicenseEntry("JavaMail for Android（android-mail / android-activation）",
                "com.sun.mail:android-mail:1.6.7", "EPL-2.0", "EPL-2.0.txt",
                "https://javaee.github.io/javamail/"));
        // 自有模块（libappbase、libaes）不是第三方开源组件，不在本列表列举
        return list;
    }

    /** One row of the license list. */
    private static class LicenseEntry {
        final String name;
        final String coordinate;
        final String license;
        final String licenseFile;
        final String sourceUrl;

        LicenseEntry(String name, String coordinate, String license, String licenseFile, String sourceUrl) {
            this.name = name;
            this.coordinate = coordinate;
            this.license = license;
            this.licenseFile = licenseFile;
            this.sourceUrl = sourceUrl;
        }
    }
}
