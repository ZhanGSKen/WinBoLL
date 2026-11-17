/*
 * Copyright 2026 WinBoLL Studio
 * SPDX-License-Identifier: Apache-2.0
 */

package cc.winboll.studio.myccconnector.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

import cc.winboll.studio.libaes.interfaces.IWinBoLLActivity;
import cc.winboll.studio.libaes.models.AESThemeBean;
import cc.winboll.studio.libaes.utils.AESThemeUtil;

/**
 * 窗口框架基类（仿 WinBoLL Project 的 BaseWinBoLLActivity）。
 *
 * <p>继承链路：BaseWinBoLLActivity → AppCompatActivity → FragmentActivity，
 * 从而天然获得 AppCompat 能力（Toolbar / 主题 / 菜单）。</p>
 *
 * <p>统一职责：应用 AES 主题、绑定 ASupportToolbar 工具栏、处理子窗口返回键。</p>
 *
 * <p>注意：本项目的 Application 未继承 libappbase 的 GlobalApplication，
 * 因此 WinBoLLActivityManager 未初始化，这里不接入 Activity 栈管理，
 * 避免 getInstance() 抛出 IllegalStateException。</p>
 *
 * @author 豆包&amp;ZhanGSKen
 * @since 1.0.0
 */
public abstract class BaseWinBoLLActivity extends AppCompatActivity implements IWinBoLLActivity {

    public static final String TAG = "BaseWinBoLLActivity";

    protected volatile AESThemeBean.ThemeType mThemeType;

    private Toolbar mToolbar;

    /** 主题样式 ID 集合只需初始化一次（AESThemeUtil.init 为库级静态状态）。 */
    private static boolean sThemeInited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // AESThemeUtil 的主题样式 ID 集合必须显式初始化，
        // 否则 getThemeStyleID() 会对空列表取下标并抛 IndexOutOfBoundsException。
        if (!sThemeInited) {
            final ArrayList<Integer> themeStyleIds = new ArrayList<Integer>();
            AESThemeBean.fillThemeStyleIDList(themeStyleIds);
            AESThemeUtil.init(themeStyleIds);
            sThemeInited = true;
        }
        // 主题必须在 super.onCreate() 之前应用，且必须为 AppCompat 系主题，
        // 否则 AppCompatActivity 会抛出 "You need to use a Theme.AppCompat theme"。
        mThemeType = AESThemeUtil.getThemeStyleType(AESThemeUtil.getThemeTypeID(getApplicationContext()));
        AESThemeUtil.applyAppCompatTheme(this);
        super.onCreate(savedInstanceState);
    }

    /**
     * 绑定窗口工具栏。
     *
     * @param toolbarId 布局中 ASupportToolbar 的资源 id
     * @param showBack  是否显示返回箭头（子窗口传 true）
     * @return 绑定成功的 Toolbar，布局中找不到时为 null
     */
    protected Toolbar bindToolbar(int toolbarId, boolean showBack) {
        mToolbar = (Toolbar) findViewById(toolbarId);
        if (mToolbar == null) {
            return null;
        }
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(showBack);
            actionBar.setDisplayShowHomeEnabled(showBack);
        }
        if (showBack) {
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        return mToolbar;
    }

    /**
     * 获取已绑定的窗口工具栏，未绑定时为 null。
     */
    protected Toolbar getWindowToolbar() {
        return mToolbar;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 窗口唯一标识。
     */
    @Override
    public abstract String getTag();

    /**
     * 当前窗口实例。
     */
    public abstract Activity getActivity();
}
