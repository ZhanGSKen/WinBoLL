/*
 * Copyright 2026 WinBoLL Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package cc.winboll.studio.appbase;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toolbar;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * @Author Doubao&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/11/15
 * @Describe Displays the bundled third-party open source licenses from
 *           assets/open_source_licenses (provided by the libappbase module).
 */
public class OpenSourceLicensesActivity extends Activity {

    public static final String TAG = "OpenSourceLicensesActivity";

    /** Asset folder that holds the license texts. */
    private static final String LICENSE_ASSET_DIR = "open_source_licenses";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_source_licenses);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);
        setTitle(R.string.open_source_licenses);

        TextView textLicenses = findViewById(R.id.text_licenses);
        textLicenses.setText(readAllLicenses());
    }

    /**
     * Reads every .txt / .md file under assets/open_source_licenses and joins
     * them into one readable document.
     */
    private String readAllLicenses() {
        StringBuilder builder = new StringBuilder();
        try {
            String[] files = getAssets().list(LICENSE_ASSET_DIR);
            if (files == null || files.length == 0) {
                return getString(R.string.open_source_licenses_empty);
            }
            Arrays.sort(files);
            for (String file : files) {
                if (!file.endsWith(".txt") && !file.endsWith(".md")) {
                    continue;
                }
                builder.append("\n\n==== ").append(file).append(" ====\n\n");
                builder.append(readAsset(LICENSE_ASSET_DIR + "/" + file));
            }
        } catch (IOException e) {
            Log.e(TAG, "readAllLicenses() failed: " + e.getMessage(), e);
            return getString(R.string.open_source_licenses_empty);
        }
        if (builder.length() == 0) {
            return getString(R.string.open_source_licenses_empty);
        }
        return builder.toString();
    }

    private String readAsset(String path) throws IOException {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open(path), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return builder.toString();
    }
}
