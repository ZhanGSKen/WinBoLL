# WinBoLL 源生态计划项目说明书

## 一、项目概述

### 1. 核心定位
WinBoLL 手机源码计划，旨在通过核心项目 WinBoLL 构建手机端与服务器端的 Android 项目的开发源码生态。实现手机与服务器的源码的联合开发。

### 2. 仓库架构
#### **仓库类型：功能说明**
☆ 基础项目分支 WinBoLL：手机端安卓应用开发基础模板。
☆ 应用项目分支 APPBase、AES、PowerBell、Positions**：安卓应用单一管理系列项目。
☆ 源码汇总管理 Projects_Keeper**：各类分支源码合并存档，不适宜作为开发库使用。

### 3. 源码合并管理推送路线图
⚠️ **注意**：仅仅展示不同应用模块源码的综合管理路线。分支合并操作时，必须具备 Git 管理经验。

★ WinBoLL → APPBase → Projects_Keeper
★ WinBoLL → AES → Projects_Keeper
★ WinBoLL → PowerBell → Projects_Keeper
★ WinBoLL → Positions → Projects_Keeper

## 二、WinBoLL 项目核心信息

### 1. 项目简介
☆ WinBoLL 项目是为手机端开发Android 项目的需求而设计的项目。

### 2. 官方资源
#### ☆ 官方网站**：https://www.winboll.cc/
#### ☆ 源码地址：
★ Gitea：https://gitea.winboll.cc/Studio/WinBoLL.git
★ GitHub：https://github.com/ZhanGSKen/WinBoLL.git
★ 码云：https://gitee.com/zhangsken/winboll.git

## 三、应用编译环境检查问题
### 核心判断条件：
☆ WinBoLL 项目以文件夹 `"/sdcard/WinBoLLStudio/APKs"` 是否存在为判断环境编译输出条件，因为编译输出的APK文件需要一个可供保存的环境。

☆ 文件夹"/sdcard/WinBoLLStudio/APKs" 目录条件设置方法：
***Linux 服务器端方面***：建立 `/sdcard/WinBoLLStudio/APKs` 目录即可。
***手机开发端方面***：建立 `"/sdcard/WinBoLLStudio/APKs"` 目录(即 `"/storage/emulated/0/WinBoLLStudio/APKs"` 目录) 即可。

## 四、前置条件

### 1. WinBoLL APP 开发环境配置介绍
####  WinBoLL APK 编译输出内容包括：
☆ "/sdcard/WinBoLLStudio/APKs"` 目录内的所有应用分支的 APK 文件。
winboll.properties 文件的 APKOutputPath 属性可配置这个 APK 输出目录的路径。
☆ "/sdcard/AppProjects/app.apk"文件。
winboll.properties 文件的 ExtraAPKOutputPath 属性可配置这个 APK 额外输出文件的路径
#### WinBoLL APK 源码命名空间规范
☆ WinBoLL 项目使用 "cc.winboll.studio" 作为源码命名空间。在此命名空间下进行源码定义。

## 五、核心需求规划

### 1. WinBoLL 应用安全验证需求
#### ☆ 支持访问 https://console.winboll.cc/ 服务器以校验应用包签名与版本。

### 2. 手机端源码开发管理需求
#### ☆ 支持切换不同 WinBoLL 分支，以开发不同安卓应用。

## 六、编译与使用指南

### 1. 项目初始化（必须）
#### 1.  复制 `settings.gradle-demo` 为 `settings.gradle`。编辑 `settings.gradle` 文件内容，取消对应项目模块注释。
#### 2.  复制 `gradle.properties-androidx-demo` (Android X 项目) 或 `gradle.properties-android-demo` (基本 Android 项目) 为 `gradle.properties`。
#### 3.  复制（可选）`local.properties-demo` 为 `local.properties`，编辑 `local.properties` 文件内容，配置 Android SDK 目录。
#### 4.  **签名设置**：
    ☆ **调试编译秘钥制作**：使用 Termux 应用终端，cd 进入 GenKeyStore 目录，运行 `bash gen_debug_keystore.sh` 脚本即可生成应用调试秘钥。
    ☆  **应用秘钥配置方法**：拷贝调试编译秘钥制作生成的 `appkey.jks` 与 `appkey.keystore` 文件到项目根目录即可。

## 七、应用编译命令介绍

### （1）类库型模块配置要点
#### 1.  **优先修改配置文件**：优先修改应用测试项目（目录为 `"<WinBoLl根目录>/<类库测试应用>/"`）内 `build.properties` 文件，设置对应的类库项目名称：`libraryProject=<类库项目模块名>`。
#### 2.  **编译优先启动步骤**：使用 Termux 应用，进入 `"<WinBoLl根目录>"`，运行 `$ bash .winboll/bashPublishAPKAddTag.sh <类库测试项目模块名>` 命令。运行后可生成测试项目与类库项目的编译参数文件 `build.properties`。生成的 `build.properties` 文件有两份，一份在测试项目模块的文件夹内，一份在类库项目本身的模块文件夹内。
#### 3.  **最后类库编译发布步骤**：使用 Termux 应用，进入 `"<WinBoLl根目录>"`，运行 `$ bash .winboll/bashPublishLIBAddTag.sh <类库项目模块名>` 命令。运行后可发布至 WinBoLL Nexus Maven 库、本地 maven 目录或者是通用默认的 Gradle Maven 库。

### （2）单一应用型模块与类库测试型模块配置要点
#### ☆ APK 编译方法：
使用 Termux 应用，进入 `"<WinBoLl根目录>"`，运行 `$ bash .winboll/bashPublishAPKAddTag.sh <应用项目模块名>`。
#### ☆ 运行后的 APK 输出路径：
★ 默认路径 (`$ bash gradlew assembleBetaDebug` 任务)：APK 在 `/sdcard/WinBoLLStudio/APKs/<项目根目录名称>/debug/` 目录。
★ 默认路径 (`$ bash assembleStageRelease` 任务)：APK 在 `/sdcard/WinBoLLStudio/APKs/<项目根目录名称>/tag/` 目录。
★ 额外输出路径：(假设 `winboll.properties` 文件已配置 `ExtraAPKOutputPath` 属性) 输出至 `ExtraAPKOutputPath` 属性配置的目录下。

### （3）手机端应用调试命令介绍
#### ☆ Beta 渠道调试命令
$bash gradlew assembleBetaDebug

#### ☆ Stage 渠道调试命令
$bash gradlew assembleStageDebug

### （4）服务器端开发命令介绍
##### ☆ Stage 渠道应用发布命令为：
（"<WinBoLl根目录>/settings.gradle"文件需要配置编译模块开启参数，拷贝 settings.gradle-demo 为 settings.gradle 文件取消对应的分支配置部分即可。）
$bash .winboll/bashPublishAPKAddTag.sh <应用项目模块名> 
或者是
$bash gradlew assembleStageRelease
 

## 八、WinBoLL 应用 APK 版本号命名规则
### ☆ Stage 渠道：
#### V<应用开发环境编号><应用功能变更号><应用调试阶段号> （示例： APPBase_15.7.0 ）
### ☆ Beta 渠道：
#### V<应用开发环境编号><应用功能变更号><应用调试阶段号>-beta<调试编译计数>_<调试编译时间(分钟+秒钟)> （示例： APPBase_15.9.6-beta8_5413 ）

## 九、编译日志存放说明

### 1. 日志存放位置
☆ 编译日志统一保存在项目根目录 `编译日志` 文件夹（`C:\0WINBOT\WinBoLL项目开发\编译日志\`）中。

### 2. 日志文件命名与内容
☆ 每次编译执行时，将 Gradle 编译过程的控制台输出保存为一个 `.log` 文件（如 `build_beta_debug_after.log`）。日志内容包含编译任务进度、APK 输出路径、构建结果（BUILD SUCCESSFUL / FAILED）及告警信息。

### 3. 使用方法
☆ 执行编译命令时，将输出重定向到该文件夹，以便归档与排查。示例（Windows PowerShell，项目源码目录为 WinBoLL源码）：
```powershell
Set-Location "C:\0WINBOT\WinBoLL项目开发\WinBoLL源码"
.\gradlew.bat assembleBetaDebug --no-daemon 2>&1 | Tee-Object -FilePath "C:\0WINBOT\WinBoLL项目开发\编译日志\build_beta_debug_<日期>.log"
```

### 4. 注意事项
☆ 建议以日期或版本号为日志文件命名（如 `build_beta_debug_15.21.1.log`），便于按版本归档。
☆ 编译日志仅作记录与排查用途，可随时删除，不影响项目源码与编译。
☆ 本文件夹不推送到 git 仓库（已在项目根目录 `.gitignore` 中忽略）。

## 十、临时文件夹存放说明

### 1. 存放位置
☆ 项目根目录 `临时文件` 文件夹（`C:\0WINBOT\WinBoLL项目开发\临时文件\`）。

### 2. 用途
☆ 存放项目开发过程中产生的临时文件，如源码修改前的备份（`*.bak`）、草稿脚本等。当前已存放：`gradlew.bat.bak`、`winboll.properties.bak`、`winboll_app_build.gradle.bak`。

### 3. 使用方法
☆ 产生新的临时/备份文件时，统一放入该文件夹，保持项目源码目录整洁。

### 4. 注意事项
☆ 临时文件仅作本地备份与过渡用途，可随时清理。
☆ 本文件夹不推送到 git 仓库（已在项目根目录 `.gitignore` 中忽略）。