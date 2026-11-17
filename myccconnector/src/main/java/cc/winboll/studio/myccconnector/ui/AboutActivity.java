/*
 * Copyright 2026 WinBoLL Studio
 * SPDX-License-Identifier: Apache-2.0
 */

package cc.winboll.studio.myccconnector.ui;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import cc.winboll.studio.myccconnector.R;
import cc.winboll.studio.myccconnector.utils.LogUtils;
import cc.winboll.studio.libappbase.models.APPInfo;
import cc.winboll.studio.libappbase.views.AboutView;

/**
 * 关于介绍窗口（仿 WinBoLL Project 的 AboutActivity）。
 *
 * <p>结构：ASupportToolbar 工具栏 + libappbase 的 AboutView 内容视图，
 * 由 APPInfo 填充应用名、图标、版本、描述、仓库地址与 APK 信息。</p>
 *
 * @author 豆包&amp;ZhanGSKen
 * @since 1.0.0
 */
public class AboutActivity extends BaseWinBoLLActivity {

    public static final String TAG = "AboutActivity";

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
        setContentView(R.layout.activity_about);

        Toolbar toolbar = bindToolbar(R.id.toolbar, true);
        if (toolbar != null) {
            toolbar.setTitle(R.string.title_about);
            toolbar.setSubtitle(TAG);
        } else {
            LogUtils.e(TAG, "onCreate: toolbar 未找到");
        }

        AboutView aboutView = (AboutView) findViewById(R.id.aboutview);
        if (aboutView != null) {
            aboutView.setAPPInfo(genDefaultAppInfo());
        } else {
            LogUtils.e(TAG, "onCreate: aboutview 未找到");
        }
    }

    /**
     * 生成关于页所需的 APPInfo 数据。
     */
    private APPInfo genDefaultAppInfo() {
        APPInfo appInfo = new APPInfo();
        appInfo.setAppName(getString(R.string.app_name));
        appInfo.setAppIcon(android.R.drawable.sym_def_app_icon);
        appInfo.setAppDescription(getString(R.string.app_description));
        appInfo.setAppGitName("WinBoLL");
        appInfo.setAppGitOwner("Studio");
        // AboutView 拼接规则：{owner}/{gitName}/src/branch/{branch}/{subFolder}
        // 结果为 https://gitea.winboll.cc/Studio/WinBoLL/src/branch/myccconnector/myccconnector
        appInfo.setAppGitAPPBranch("myccconnector");
        appInfo.setAppGitAPPSubProjectFolder("myccconnector");
        appInfo.setAppHomePage("https://gitea.winboll.cc/Studio/WinBoLL");
        appInfo.setAppAPKName("MyCCConnector");
        appInfo.setAppAPKFolderName("MyCCConnector");
        appInfo.setIsAddDebugTools(false);
        return appInfo;
    }
}
