/*
 * Copyright 2026 WinBoLL Studio
 * SPDX-License-Identifier: Apache-2.0
 */

package cc.winboll.studio.myccconnector;

import android.graphics.Color;

/**
 * 全局颜色常量表。
 *
 * <p>所有颜色值统一在此类中以 {@code public static final int} 管理，
 * 禁止在代码中硬编码 {@link Color#parseColor(String)} 或字面量色值。</p>
 *
 * <p>命名规范：COLOR_&lt;用途&gt;_&lt;状态&gt;，全大写下划线分隔。</p>
 *
 * @author 豆包&amp;Big Pickle&amp;MiMo&amp;ZhanGSKen
 * @since 1.0.0
 */
public final class ViewColorTable {

    // ---- 主色 ----
    public static final int COLOR_PRIMARY = Color.parseColor("#1976D2");
    public static final int COLOR_PRIMARY_DARK = Color.parseColor("#1565C0");
    public static final int COLOR_ACCENT = Color.parseColor("#FF4081");

    // ---- 文本 ----
    public static final int COLOR_PRIMARY_TEXT = Color.parseColor("#212121");
    public static final int COLOR_SECONDARY_TEXT = Color.parseColor("#757575");
    public static final int COLOR_HINT_TEXT = Color.parseColor("#BDBDBD");
    public static final int COLOR_WHITE_TEXT = Color.parseColor("#FFFFFF");

    // ---- 背景 ----
    public static final int COLOR_BG_WHITE = Color.parseColor("#FFFFFF");
    public static final int COLOR_BG_GRAY = Color.parseColor("#F2F3F5");
    public static final int COLOR_BG_DARK = Color.parseColor("#303030");

    // ---- 分割线 / 边框 ----
    public static final int COLOR_DIVIDER = Color.parseColor("#E0E0E0");

    // ---- 状态 ----
    public static final int COLOR_SUCCESS = Color.parseColor("#4CAF50");
    public static final int COLOR_WARNING = Color.parseColor("#FF9800");
    public static final int COLOR_ERROR = Color.parseColor("#F44336");

    // ---- 聊天气泡 ----
    public static final int COLOR_BUBBLE_USER = Color.parseColor("#D7E7FF");
    public static final int COLOR_BUBBLE_ASSISTANT = Color.parseColor("#FFFFFF");
    public static final int COLOR_BUBBLE_USER_TEXT = Color.parseColor("#12335B");

    // ---- 工具卡片 ----
    public static final int COLOR_TOOL_BG = Color.parseColor("#FFF8E1");
    public static final int COLOR_TOOL_BORDER = Color.parseColor("#F0C36D");
    public static final int COLOR_TOOL_TITLE = Color.parseColor("#8D6E00");
    public static final int COLOR_TOOL_BODY = Color.parseColor("#4A4A4A");

    private ViewColorTable() {
        // 常量类禁止实例化
    }
}
