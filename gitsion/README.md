# GPSRelaySentinel

---

## 中文文档

### 项目介绍

GPSRelaySentinel 是一款专业的 **GPS 定位中继守护工具**，支持真实系统 GPS 定位监听与模拟 GPS 坐标仿真双模式运行。应用后台常驻前台服务，实时接收系统 GPS 位置数据，内置订阅者步长阈值判断机制，可对多个 GPS 订阅视图进行定点推送管理。

#### 核心功能

- **双模式运行**：支持真实 GPS 工作模式与虚拟仿真模式一键切换
- **前台常驻服务**：`MainService` 作为前台 Service 持续监听 GPS 定位变化
- **订阅者管理**：内置 `GpsSubscribeManager` 与 `SubscribeLocationManager`，支持多订阅者步长阈值推送
- **模拟控制面板**：支持八大方位选择、自定义移动距离，自动计算偏移目标经纬度
- **实时日志输出**：集成 `LogView` 面板，方便调试定位轨迹与订阅推送状态
- **崩溃处理**：`App` 类提供全局 CrashHandler 与 CrashActivity 展示崩溃日志
- **关于页面**：工具栏提供 About 按钮，可查看应用版本与项目信息

#### 技术栈

| 项目 | 版本/说明 |
|------|-----------|
| 编程语言 | Java 7（源码） |
| 编译环境 | Java 11（Gradle 编译） |
| Gradle 插件 | 7.2.1 |
| 最低 API | API 26 (Android 8.0) |
| 目标 API | API 30 (Android 11) |
| 编译 API | API 30 |

#### 模块结构

本项目采用多模块 Gradle 结构：

| 模块 | 类型 | 说明 |
|------|------|------|
| `:gpsrelaysentinel` | application | 主应用模块（MainActivity、MainService、AboutActivity 等） |
| `:libgpsrelaysentinel` | library | GPS 中继核心类库（GpsSubscribeManager、SubscribeLocationManager 等） |

#### 核心依赖库

**网络相关**
- OkHttp 4.4.1 / 3.14.9 — HTTP 客户端
- Gson 2.10.1 — JSON 解析

**终端模拟**
- Termux: terminal-emulator 0.118.0
- Termux: terminal-view 0.118.0
- Termux: termux-shared 0.118.0

**功能组件**
- ZXing 3.4.1 — 二维码生成与扫描
- JSch 0.1.55 — SSH/SFTP 客户端
- Jsoup 1.13.1 — HTML 解析
- FastJSON 1.2.76 — JSON 处理

**UI 组件**
- Material Design 1.4.0
- AndroidX 组件库
- PullRefreshLayout 1.2.0 — 下拉刷新

#### 编译说明

**调试版编译**
```bash
./gradlew assembleBetaDebug
```

**阶段版编译（发布）**
```bash
bash .winboll/bashPublishAPKAddTag.sh gpsrelaysentinel
```

**版本管理**
版本信息由 `gpsrelaysentinel/build.properties` 管理：
- `baseVersion` — 基础版本号
- `stageCount` — 阶段构建次数
- `publishVersion` — 发布版本号
- `buildCount` — 构建次数

#### 权限说明

应用需要以下权限：
- `ACCESS_FINE_LOCATION` — 精确定位
- `ACCESS_COARSE_LOCATION` — 大致定位
- `ACCESS_BACKGROUND_LOCATION` — 后台定位
- `FOREGROUND_SERVICE` — 前台服务

#### 项目结构

```
gpsrelaysentinel/
├── src/main/
│   ├── java/cc/winboll/studio/gpsrelaysentinel/
│   │   ├── App.java              # Application 类，初始化与崩溃处理
│   │   ├── MainActivity.java     # 主控制页面（GPS服务开关、模拟面板、订阅视图）
│   │   ├── MainService.java      # GPS 定位核心前台服务
│   │   ├── AboutActivity.java    # 关于页面
│   │   └── GpsReceiverChildService[1-3].java  # GPS 接收子服务
│   ├── res/
│   │   ├── layout/               # 布局文件
│   │   ├── menu/                 # 菜单文件
│   │   └── values/               # 资源值文件
│   ├── libs/                     # 本地库文件
│   └── AndroidManifest.xml       # 应用清单
├── build.gradle                  # 模块构建配置
└── build.properties              # 版本配置文件
```

#### 参与贡献

1. Fork 本仓库
2. 新建功能分支 (`git checkout -b feat_xxx`)
3. 提交代码（作者: ZhanGSKen <ZhanGSKen@QQ.COM>）
4. 新建 Pull Request

#### 许可证

[待添加许可证信息]

---

## English Documentation

### Project Introduction

GPSRelaySentinel is a professional **GPS relay and guardian tool**, supporting dual modes of real system GPS location monitoring and simulated GPS coordinate simulation. It runs as a foreground persistent background service, receives real-time system GPS location data, and builds-in subscriber step threshold judgment mechanism to manage fixed-point push for multiple GPS subscription views.

#### Core Features

- **Dual Mode Operation**: One-click switch between real GPS working mode and virtual simulation mode
- **Foreground Persistent Service**: `MainService` as a foreground Service continuously monitors GPS location changes
- **Subscriber Management**: Built-in `GpsSubscribeManager` and `SubscribeLocationManager`, supporting multi-subscriber step threshold push
- **Simulation Control Panel**: Supports eight direction selections, custom moving distance, and automatic offset target coordinate calculation
- **Real-time Log Output**: Integrated `LogView` panel for debugging location tracks and subscription push status
- **Crash Handling**: `App` class provides global CrashHandler and CrashActivity for crash log display
- **About Page**: Toolbar provides an About button to view app version and project information

#### Tech Stack

| Item | Version/Description |
|------|---------------------|
| Programming Language | Java 7 (source code) |
| Build Environment | Java 11 (Gradle compilation) |
| Gradle Plugin | 7.2.1 |
| Minimum API | API 26 (Android 8.0) |
| Target API | API 30 (Android 11) |
| Compile API | API 30 |

#### Module Structure

This project uses a multi-module Gradle structure:

| Module | Type | Description |
|--------|------|-------------|
| `:gpsrelaysentinel` | application | Main application module (MainActivity, MainService, AboutActivity, etc.) |
| `:libgpsrelaysentinel` | library | GPS relay core library (GpsSubscribeManager, SubscribeLocationManager, etc.) |

#### Core Dependencies

**Networking**
- OkHttp 4.4.1 / 3.14.9 — HTTP client
- Gson 2.10.1 — JSON parsing

**Terminal Emulation**
- Termux: terminal-emulator 0.118.0
- Termux: terminal-view 0.118.0
- Termux: termux-shared 0.118.0

**Functional Components**
- ZXing 3.4.1 — QR code generation and scanning
- JSch 0.1.55 — SSH/SFTP client
- Jsoup 1.13.1 — HTML parsing
- FastJSON 1.2.76 — JSON processing

**UI Components**
- Material Design 1.4.0
- AndroidX libraries
- PullRefreshLayout 1.2.0 — Pull-to-refresh

#### Build Instructions

**Debug Build**
```bash
./gradlew assembleBetaDebug
```

**Stage Build (Release)**
```bash
bash .winboll/bashPublishAPKAddTag.sh gpsrelaysentinel
```

**Version Management**
Version info is managed by `gpsrelaysentinel/build.properties`:
- `baseVersion` — Base version number
- `stageCount` — Stage build count
- `publishVersion` — Release version number
- `buildCount` — Build count

#### Permissions

The app requires the following permissions:
- `ACCESS_FINE_LOCATION` — Precise location
- `ACCESS_COARSE_LOCATION` — Approximate location
- `ACCESS_BACKGROUND_LOCATION` — Background location
- `FOREGROUND_SERVICE` — Foreground service

#### Project Structure

```
gpsrelaysentinel/
├── src/main/
│   ├── java/cc/winboll/studio/gpsrelaysentinel/
│   │   ├── App.java              # Application class, initialization and crash handling
│   │   ├── MainActivity.java     # Main control page (GPS service switch, simulation panel, subscription views)
│   │   ├── MainService.java      # GPS location core foreground service
│   │   ├── AboutActivity.java    # About page
│   │   └── GpsReceiverChildService[1-3].java  # GPS receiver child services
│   ├── res/
│   │   ├── layout/               # Layout files
│   │   ├── menu/                 # Menu files
│   │   └── values/               # Resource value files
│   ├── libs/                     # Local library files
│   └── AndroidManifest.xml       # App manifest
├── build.gradle                  # Module build configuration
└── build.properties              # Version configuration file
```

#### Contributing

1. Fork this repository
2. Create a feature branch (`git checkout -b feat_xxx`)
3. Commit your changes (Author: ZhanGSKen <ZhanGSKen@QQ.COM>)
4. Create a Pull Request

#### License

[License information to be added]
