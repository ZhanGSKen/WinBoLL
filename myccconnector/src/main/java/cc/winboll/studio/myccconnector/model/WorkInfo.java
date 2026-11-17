/*
 * Copyright 2026 WinBoLL Studio
 * SPDX-License-Identifier: Apache-2.0
 */

package cc.winboll.studio.myccconnector.model;

import org.json.JSONObject;

/**
 * 工作任务元信息（会话分组容器）。
 *
 * @author 豆包&amp;Big Pickle&amp;MiMo&amp;ZhanGSKen
 * @since 1.0.0
 */
public final class WorkInfo {

    public final String id;
    public final String name;
    public final String cwd;
    public final int sessionCount;
    public final long updatedAt;

    public WorkInfo(final String id, final String name, final String cwd,
                    final int sessionCount, final long updatedAt) {
        this.id = id;
        this.name = name;
        this.cwd = cwd;
        this.sessionCount = sessionCount;
        this.updatedAt = updatedAt;
    }

    public static WorkInfo fromJson(final JSONObject json) {
        return new WorkInfo(
                json.optString("id", ""),
                json.optString("name", "未命名任务"),
                json.optString("cwd", ""),
                json.optInt("sessionCount", 0),
                json.optLong("updatedAt", 0L));
    }
}
