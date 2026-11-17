# MyCCConnector 项目说明

> 安卓端 Claude 客户端，通过本机 WINBOT 上的桥接服务连接 Claude Code（底层模型为 Ollama `gemma4:12b`）。
> 本项目基于 `C:\0WINBOT\WINBOT主机安卓项目开发模板` 派生。

## 一、项目概述

| 项目 | 值 |
|---|---|
| 项目路径 | `C:\0WINBOT\MyCCConnector-Project-Development\MyCCConnector` |
| 包名 / applicationId | `cc.winboll.studio.myccconnector` |
| 应用名 | MyCCConnector |
| 构建系统 | Gradle 7.5.1 + AGP 7.2.1（随 WinBoLL 编译框架） |
| compileSdk / targetSdk / minSdk | 30 / 30 / 24 |
| buildToolsVersion | 30.0.3 |
| 版本 | 1.0.0（versionCode 1） |
| 架构 | 安卓 App ⇄ HTTP/SSE ⇄ 桥接服务 ⇄ Claude Agent SDK ⇄ claude.exe ⇄ Ollama |

### 目录结构

```
MyCCConnector/
├── .winboll/                       # WinBoLL 编译框架脚本（随 origin/winboll 同步）
├── BaseVersion.flag                # 版本下限闸门（baseVersion 低于此值会被自动上调）
├── build.gradle                    # 框架入口：apply from '.winboll/winboll_app_build.gradle'
├── settings.gradle                 # rootProject.name = "myccconnector" + include ':myccconnector'
├── gradle/ · gradlew · gradlew.bat
└── myccconnector/                  # 安卓模块（含项目文档）
    ├── AGENTS.md                   # 本文档
    ├── 使用说明.md                  # 安装、配置、反代说明
    ├── build.gradle · build.properties   # 应用构建配置 / 版本数据（编译时回写）
    ├── docs/screenshot-chat.png    # 会话界面截图
    └── src/main/java/cc/winboll/studio/myccconnector/
        ├── AppSettings.java        # 地址 / token / 默认工作目录
        ├── ViewColorTable.java     # 全局颜色常量
        ├── model/SessionInfo.java  # 会话元信息
        ├── net/BridgeApi.java      # REST 客户端（HttpURLConnection）
        ├── net/SseClient.java      # SSE 长连接 + 自动重连
        ├── ui/MainActivity.java    # 会话列表
        ├── ui/ChatActivity.java    # 聊天 + 工具卡片 + 权限弹窗
        ├── ui/SettingsActivity.java# 设置 / 测试连接
        └── utils/LogUtils.java     # 统一日志
```

> 桥接服务 `bridge/` 已迁至**项目根目录**（与本仓库同级，即 `..\bridge`），由父仓库 `MyCCConnector-Project-Development` 托管；
> 本仓库只保留安卓模块 `myccconnector/` 与文档。桥接说明见 `..\bridge\README.md`。
> 防火墙放行脚本 `configure-bridge-firewall.ps1` 也已移至父仓库根目录（`..\configure-bridge-firewall.ps1`）。

## 二、构建

```powershell
cd C:\0WINBOT\MyCCConnector-Project-Development\MyCCConnector
java -jar gradle\wrapper\gradle-wrapper.jar :myccconnector:assembleBetaDebug
```

产物：`myccconnector\build\outputs\apk\beta\debug\myccconnector-beta-debug.apk`

> 中文路径下 `gradlew.bat` 会因 cmd 编码报错，统一走 `java -jar gradle\wrapper\gradle-wrapper.jar <任务>`。
>
> 明文 HTTP 放行：库（`libappbase` / `libaes`）自带 `res/xml/network_security_config.xml`，只允许 `winboll.cc` 明文，
> 会把局域网桥接地址拦成 `Cleartext HTTP traffic not permitted`。`myccconnector/src/main/res/xml/` 下的应用级同名文件
> 优先级更高，声明 `base-config cleartextTrafficPermitted=true` 覆盖之，勿删。

### 本机 SDK 说明

`local.properties` 指向 `C:\0WINBOT\AndroidStudio\AndroidSDK`，该路径原本不存在，
已建目录联接指向真实 SDK：

```powershell
cmd /c mklink /J "C:\0WINBOT\AndroidStudio\AndroidSDK" "C:\0WINBOT\AndroidStudio\AndroidSDK-windows"
```

## 三、桥接服务

| 项 | 值 |
|---|---|
| 服务目录 | 项目根 `bridge/`（子模块外，`..\bridge`） |
| 监听 | `0.0.0.0:8787` |
| 鉴权 | `Authorization: Bearer <config.json 的 token>` |
| 模型 | `gemma4:12b`（Ollama，Anthropic 兼容接口 `/v1/messages`） |
| 允许目录 | `C:\0WINBOT`、`C:\Users\ZhanGSKen` |
| 常驻 | 计划任务 `ClaudeBridge`（登录自启，隐藏窗口） |
| 数据 | 项目根 `bridge\data\sessions.json` + `bridge\data\transcripts\<id>.jsonl` |

启动 / 停止：

```powershell
Start-ScheduledTask -TaskName ClaudeBridge
Stop-ScheduledTask  -TaskName ClaudeBridge
# 或前台调试
cd C:\0WINBOT\MyCCConnector-Project-Development\bridge; node src/server.mjs
```

## 四、接口约定

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/ping` | 健康检查（无需鉴权） |
| GET | `/api/sessions` | 会话列表 |
| POST | `/api/sessions` | 新建会话 `{title, cwd}` |
| DELETE | `/api/sessions/:id` | 删除会话 |
| GET | `/api/sessions/:id/messages` | 会话历史事件 |
| GET | `/api/sessions/:id/events?afterSeq=N` | SSE 事件流（断线重放） |
| POST | `/api/sessions/:id/messages` | 发送一轮输入 `{text}`，返回 202 |
| POST | `/api/sessions/:id/interrupt` | 中断当前轮次 |
| POST | `/api/permissions/:requestId` | 权限审批 `{allow, remember, toolName}` |

### SSE 事件类型

`user` / `state` / `init` / `delta` / `assistant_message` / `tool_use` / `tool_result` /
`permission_request` / `permission_resolved` / `permission_timeout` / `permission_history` /
`result` / `error` / `turn_end`

> 回放历史时，只剩“当前进程仍挂起”的审批会以 `permission_request` 下发，其余降级为
> `permission_history`，避免安卓端把已处理过的审批重复弹窗。

## 五、安全约束

1. 桥接服务等于“手机可远程在本机执行命令”，token 等同于本机执行授权，**不要外泄**，不要在公网裸奔。
2. 工作目录必须在 `allowedRoots` 白名单内，越界直接 400。
3. 工具调用默认 `permissionMode=default`，写操作与危险命令必须在手机上点“允许”才执行。
4. 外网访问必须经 HTTPS 反代（见 `使用说明.md`），不要直接映射 8787 端口。

## 六、已知限制

1. `gemma4:12b` 是思考型模型，每轮输入约 1.1 万 token（Claude Code 系统提示 + 27 个工具），
   首字延迟约 15–60 秒，取决于提示长度与 KV 缓存命中。
2. 单轮独立进程 + `resume` 续接：崩溃只影响当轮，但每轮都有进程启动开销。
3. 一个会话同一时刻只允许一轮执行，重复发送返回 409。
4. 权限审批超时（默认 300 秒）按拒绝处理。

## 七、验证记录（2026-11-17）

| 项 | 结果 |
|---|---|
| 模板编译（派生前后） | 通过 |
| 桥接服务 `/api/ping` | 通过（model=gemma4:12b，claude.exe 就绪） |
| 纯对话流式回复 | 通过（159 tokens，增量 delta 26 条） |
| 工具调用（Bash `find`） | 通过（只读命令自动放行） |
| 权限审批（Write） | 通过（先 `permission_request`，允许后执行） |
| 安卓 App 会话列表 | 通过（模拟器 API 29，显示“已连接 · N 个会话”） |
| 安卓 App 收发消息 | 通过（“say hello…” → “Hello!” 完成 3.1 秒） |
| 历史回放不重复弹窗 | 通过（显示“历史审批 · Write”） |
