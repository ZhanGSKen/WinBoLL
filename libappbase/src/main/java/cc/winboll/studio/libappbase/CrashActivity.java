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
package cc.winboll.studio.libappbase;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import cc.winboll.studio.libappbase.common.ViewColorTable;

public final class CrashActivity extends Activity implements MenuItem.OnMenuItemClickListener {
    private static final int MENUITEM_COPY = 0;
    private static final int MENUITEM_RESTART = 1;

    private String mLog;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCrashSafetyWire.getInstance().postResumeCrashSafetyWireHandler(getApplicationContext());
        mLog = getIntent().getStringExtra(CrashHandler.EXTRA_CRASH_LOG);
        setTheme(android.R.style.Theme_DeviceDefault_Light_DarkActionBar);
        initLayout();
    }

    private void initLayout() {
        ScrollView contentView = new ScrollView(this);
        contentView.setFillViewport(true);

        HorizontalScrollView hw = new HorizontalScrollView(this);
        hw.setBackgroundColor(ViewColorTable.ListItemBgPressedColor);

        TextView message = new TextView(this);
        final int padding = dp2px(16);
        message.setPadding(padding, padding, padding, padding);
        message.setText(mLog);
        message.setTextColor(ViewColorTable.TextPrimaryColor);
        message.setTextIsSelectable(true);

        hw.addView(message);
        contentView.addView(hw, ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(contentView);

        getActionBar().setTitle(CrashHandler.TITTLE);
        getActionBar().setSubtitle(GlobalApplication.getAppName(getApplicationContext()) + " Error");
    }

    @Override
    public void onBackPressed() {
        restartApp();
    }

    private void restartApp() {
        final Intent intent = getPackageManager()
            .getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private int dp2px(final float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        menu.add(0, MENUITEM_COPY, 0, "Copy")
            .setOnMenuItemClickListener(this)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.add(0, MENUITEM_RESTART, 0, "Restart")
            .setOnMenuItemClickListener(this)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        switch (item.getItemId()) {
            case MENUITEM_COPY:
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText(getPackageName(), mLog));
                Toast.makeText(getApplication(), "The text is copied.", Toast.LENGTH_SHORT).show();
                break;
            case MENUITEM_RESTART:
                AppCrashSafetyWire.getInstance().resumeToMaximumImmediately();
                restartApp();
                break;
            default:
                break;
        }
        return false;
    }
}
