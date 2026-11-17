/*
 * Copyright 2026 WinBoLL Studio
 * SPDX-License-Identifier: Apache-2.0
 */

package cc.winboll.studio.myccconnector.model;

import org.json.JSONObject;

/**
 * 会话元信息。
 *
 * @author 豆包&amp;Big Pickle&amp;MiMo&amp;ZhanGSKen
 * @since 1.0.0
 */
public final class SessionInfo {

    public final String id;
    public final String title;
    public final String cwd;
    public final long updatedAt;
    public final int turnCount;

    public SessionInfo(final String id, final String title, final String cwd,
                       final long updatedAt, final int turnCount) {
        this.id = id;
        this.title = title;
        this.cwd = cwd;
        this.updatedAt = updatedAt;
        this.turnCount = turnCount;
    }

    public static SessionInfo fromJson(final JSONObject json) {
        return new SessionInfo(
                json.optString("id", ""),
                json.optString("title", "未命名会话"),
                json.optString("cwd", ""),
                json.optLong("updatedAt", 0L),
                json.optInt("turnCount", 0));
    }
}
