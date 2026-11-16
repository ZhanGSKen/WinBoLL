# Claude Bridge 桥接服务

为安卓客户端提供 Claude Code 的远程访问能力：HTTP REST + SSE 事件流，
底层通过官方 `@anthropic-ai/claude-agent-sdk` 驱动本机 `claude.exe`，
模型走 Ollama 的 Anthropic 兼容接口（默认 `gemma4:12b`）。

## 快速开始

```powershell
cd C:\0WINBOT\ClaudeCodeAndroid-Project-Development\ClaudeCodeAndroid\bridge
npm install          # 仅首次
node src/server.mjs  # 前台调试
```

首次启动会自动生成 `config.json`（含随机 token）。默认监听 `0.0.0.0:8787`。

## 配置字段（config.json）

| 字段 | 说明 |
|---|---|
| `host` / `port` | 监听地址与端口 |
| `token` | 安卓端 Bearer 令牌（随机生成，勿外传） |
| `allowedRoots` | 允许作为工作目录的根（安全边界） |
| `defaultCwd` | 新建会话默认工作目录 |
| `claudeExecutable` | `claude.exe` 绝对路径 |
| `model` | Ollama 模型名 |
| `anthropicBaseUrl` | Ollama 服务地址 |
| `authToken` | 传给 Ollama 的占位令牌（Ollama 不校验） |
| `permissionTimeoutMs` | 审批超时，超时按拒绝 |
| `maxTurns` | 单轮最大 agent 轮次 |

## 接口

除 `/api/ping` 外都需要 `Authorization: Bearer <token>`。

```
GET    /api/ping
GET    /api/sessions
POST   /api/sessions            {title, cwd}
DELETE /api/sessions/:id
GET    /api/sessions/:id/messages
GET    /api/sessions/:id/events?afterSeq=N     (SSE)
POST   /api/sessions/:id/messages  {text}      -> 202
POST   /api/sessions/:id/interrupt
POST   /api/permissions/:requestId {allow, remember, toolName}
```

SSE 帧格式：

```
id: 12
data: {"type":"delta","text":"好","seq":12,"ts":...}
```

客户端断线后用 `afterSeq=最后收到的 seq` 重连即可无缝续播。

## 端到端自测

```powershell
cd C:\0WINBOT\ClaudeCodeAndroid-Project-Development\ClaudeCodeAndroid\bridge
node tools/test-client.mjs "请用一句中文回复：桥接已接通"
```

该客户端会自动允许权限请求，用于验证 `permission_request` 链路。

## 设计要点

1. **每轮独立进程 + `resume`**：单轮崩溃不污染整段会话。
2. **权限由手机决定**：`canUseTool` 回调把审批转发到 App，超时视为拒绝。
3. **事件全量落盘**：`data/transcripts/<sessionId>.jsonl`，进程重启后可回放。
4. **回放安全**：只有当前进程仍挂起的审批才以 `permission_request` 下发，历史审批降级为 `permission_history`。

## 安全

- token 等同本机执行授权；仅在局域网使用，公网访问必须 HTTPS 反代。
- 工作目录受 `allowedRoots` 白名单约束。
- 建议不要给 `claude` 配 `bypassPermissions`。
