/*
 * Copyright 2026 WinBoLL Studio
 * SPDX-License-Identifier: Apache-2.0
 */

package cc.winboll.studio.myccconnector.model;

import org.json.JSONObject;

/**
 * 会话元信息。
 *
 * <p>workId 指向所属工作任务，为 null（或空串）表示未归档会话。</p>
 *
 * @author 豆包&amp;Big Pickle&amp;MiMo&amp;ZhanGSKen
 * @since 1.0.0
 */
public final class SessionInfo {

    public final String id;
    public final String title;
    public final String cwd;
    public final String workId;
    public final long updatedAt;
    public final int turnCount;

    public SessionInfo(final String id, final String title, final String cwd,
                       final String workId, final long updatedAt, final int turnCount) {
        this.id = id;
        this.title = title;
        this.cwd = cwd;
        this.workId = workId == null || workId.length() == 0 ? null : workId;
        this.updatedAt = updatedAt;
        this.turnCount = turnCount;
    }

    public static SessionInfo fromJson(final JSONObject json) {
        // 注意：服务端 workId 为 JSON null 时，optString 会得到字符串 "null"，必须显式判空
        final String workId = json.isNull("workId") ? null : json.optString("workId", "");
        return new SessionInfo(
                json.optString("id", ""),
                json.optString("title", "未命名会话"),
                json.optString("cwd", ""),
                workId,
                json.optLong("updatedAt", 0L),
                json.optInt("turnCount", 0));
    }
}
