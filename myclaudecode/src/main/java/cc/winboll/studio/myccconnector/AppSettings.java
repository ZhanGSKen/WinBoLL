/*
 * Copyright 2026 WinBoLL Studio
 * SPDX-License-Identifier: Apache-2.0
 */

package cc.winboll.studio.myccconnector;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import cc.winboll.studio.myccconnector.utils.LogUtils;

/**
 * 应用配置存取。
 *
 * <p>保存桥接服务地址、访问令牌、默认工作目录，供设置页与聊天页共用。
 * 访问令牌等同本机的远程执行授权，因此存放在 EncryptedSharedPreferences
 * （AES256-GCM，密钥由 Android Keystore 托管）；若设备不支持或初始化失败，
 * 回退到 MODE_PRIVATE 的普通私有存储，保证功能可用。</p>
 *
 * @author 豆包&amp;Big Pickle&amp;MiMo&amp;ZhanGSKen
 * @since 1.0.0
 */
public final class AppSettings {

    private static final String TAG = "AppSettings";

    private static final String PREF_NAME = "claude_bridge_prefs";
    private static final String KEY_SERVER_URL = "server_url";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_CWD = "cwd";

    /**
     * 默认桥接地址使用回环地址：具体主机由用户在设置页填写，
     * 代码中不硬编码任何内网地址或开发机路径。
     */
    public static final String DEFAULT_SERVER_URL = "http://127.0.0.1:8787";

    /**
     * 默认工作目录留空：桥接服务在 cwd 为空时会回退到其 config.json 的
     * defaultCwd，因此这里不再内置任何本机绝对路径。
     */
    public static final String DEFAULT_CWD = "";

    private static volatile SharedPreferences sPrefs;

    private AppSettings() {
        // 工具类禁止实例化
    }

    /**
     * 取得配置存储：优先加密存储，失败则回退普通私有存储。
     */
    private static SharedPreferences prefs(final Context context) {
        SharedPreferences cached = sPrefs;
        if (cached != null) {
            return cached;
        }
        synchronized (AppSettings.class) {
            if (sPrefs != null) {
                return sPrefs;
            }
            final Context appContext = context.getApplicationContext();
            try {
                // security-crypto 1.0.0 稳定版使用 MasterKeys（密钥别名由 Keystore 托管）
                String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                sPrefs = EncryptedSharedPreferences.create(
                        PREF_NAME,
                        masterKeyAlias,
                        appContext,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
                LogUtils.d(TAG, "prefs: 使用加密存储");
            } catch (Exception e) {
                LogUtils.e(TAG, "prefs: 加密存储不可用，回退普通私有存储", e);
                sPrefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            }
            return sPrefs;
        }
    }

    public static String getServerUrl(final Context context) {
        String value = prefs(context).getString(KEY_SERVER_URL, "");
        if (value == null || value.length() == 0) {
            return DEFAULT_SERVER_URL;
        }
        return value;
    }

    public static void setServerUrl(final Context context, final String url) {
        prefs(context).edit().putString(KEY_SERVER_URL, url).apply();
    }

    public static String getToken(final Context context) {
        String value = prefs(context).getString(KEY_TOKEN, "");
        return value == null ? "" : value;
    }

    public static void setToken(final Context context, final String token) {
        prefs(context).edit().putString(KEY_TOKEN, token).apply();
    }

    public static String getCwd(final Context context) {
        String value = prefs(context).getString(KEY_CWD, "");
        if (value == null || value.length() == 0) {
            return DEFAULT_CWD;
        }
        return value;
    }

    public static void setCwd(final Context context, final String cwd) {
        prefs(context).edit().putString(KEY_CWD, cwd).apply();
    }

    /** 去掉地址尾部的斜杠，避免拼接出双斜杠 */
    public static String normalizeBaseUrl(final String url) {
        String result = url == null ? "" : url.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
