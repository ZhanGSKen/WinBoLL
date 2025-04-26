# ☁ ☁ ☁ WinBoLL APP ☁ ☁ ☁ ☁ ☁ ☁   ☁ ☁   ☁  ☁ ☁ ☁ ☁  ☁ ☁ ☁ ☁ ☁ ☁ 
# ☁    ☁ WinBoLL Studio Android 应用开源项目。☁ ☁   ☁ ☁ ☁ ☁  ☁ ☁ ☁ ☁ ☁  ☁ ☁ ☁ 
# ☁ ☁ ☁ WinBoLL 网站地址 https://www.winboll.cc/ ☁ ☁ ☁ ☁  ☁ ☁ ☁ ☁

## WinBoll 提问
同样是 /sdcard 目录，在开发 Android 应用时，
能否实现手机编译与电脑编译的源码同步。
☁因而 WinBoll 项目组诞生了。

## WinBoll 项目组研发计划
致力于把 WinBoll-APP 应用在手机端 Android 项目开发。
也在探索 https://gitea.winboll.cc/<WinBoll 项目组>/APP.git 应用于 WinBoll-APP APK 分发。
更想进阶 https://github.com/<WinBoll 项目组>/APP.git 应用于 WinBoll-APP Beta APK 分发。

## WinBoll-APP 汗下...
#### ☁应用何置如此呢。且观用户云云。

#### ☁ 正当下 ☁ ###
#### ☁ 且容傻家叙说 ☁ WinBoll-APP 应用场景
### ☁ WinBoll 设备资源概述
#### ☁ 1. Raid Disk.
概述：这是一个矩阵存储类设备。
优点：该设备具有数据容错存储功能，
     数据存储具有特长持久性。
缺点：设备使用能源消耗比较高，
     设备存取速度一般。
     
#### ☁ 2. Data Disk.
概述：这是一个普通硬盘存储设备
优点：该设备独立于操作系统，
     数据持久性一般，
     存取能源消耗小于 Raid Disk。
缺点：数据存储速度一般，存储能源消耗一般。

#### ☁ 3. SSD Disk.
概述：这是一个 SSD 硬盘存储设备。
优点：存取速度快于 Data Disk 与 Raid Disk，
     存取能源消耗小于 Data Disk 与 Raid Disk。
缺点：数据持久性一般，
     设备位于操作系统内部文件系统。
     数据持久性与操作系统挂钩。
     
#### ☁ 4. WinBoll 用户资源概述。
1> /home/<用户名> 位于 WinBoll 操作系统目录下。
2> /rdisk/<用户名> 挂载用户 Raid Disk.
3> /data/<用户名> 挂载用户 Data Disk.
4> /sdcard/<用户名> 挂载用户 SSD Disk.

#### ☁ 5. WinBoll-APP 用户资源概述。
1> /sdcard 挂载用户手机 SD 存储/storage/emulated/0

### ☁ 稍稍歇 ☁ ###
### ☁ 急急停 ☁ WinBoll 应用前置条件
☁ WinBoll 主机建立 1Panel MySQL 应用。
☁ WinBoll 主机建立 1Panel Gitea 应用。
☁ WinBoll 主机设置 WinBoll 应用为非登录状态。
☁ WinBoll 主机建立 WinBoll 账户与 WinBoll 用户组。
☁ WinBoll 账户 User ID 为： J。
☁ WinBoll 用户组 Group ID 为： Studio。
☁ WinBoll 主机 WinBoll 1Panel Gitea 建立 WinBoll 工作组。
☁ WinBoll 主机 WinBoll 1Panel Gitea 用户项目 APK 编译输出目录为 /sdcard/WinBollStudio/<用户名>/APKs/
☁ WinBoll 项目配置文件示例为 "<WinBoll 项目根目录>/.winboll/winboll.properties-demo"(WinBoll 项目已设置)
☁ WinBoll 项目配置文件为 "<WinBoll 项目根目录>/.winboll/winboll.properties"
☁ WinBoll 项目配置文件设定为源码提交时忽略。(WinBoll 项目已设置)
☁ Gradle 项目配置文件示例为 "<WinBoll 项目根目录>/.winboll/local.properties-demo"(WinBoll 项目已设置)
☁ Gradle 项目配置文件为 "<WinBoll 项目根目录>/local.properties"(WinBoll 项目已设置)
☁ Gradle 项目配置文件设定为源码提交时忽略。(WinBoll 项目已设置)

### ☁ 登高处 ☁ WinBoll 应用需求规划
☁ WinBoll 主机建立 WinBoll 客户端用户数据库为 MySQL winbollclient 数据库。
☁ WinBoll 主机设置 WinBoll 客户端用户信息存储在 winbollclient 数据库中。
☁ MySQL winbollclient 数据库中
   WinBoll 客户端用户信息设定为：
   <用户名, 验证密码, 验证邮箱, 验证手机, 唯一存储令牌Token, 备用验证邮箱>。
☁ WinBoll 项目源码仓库托管在 WinBoll 1Panel Gitea 目录 /opt/1panel/apps/gitea/gitea/data/git/repositories/studio/app.git中。
☁ WinBoll 主机提供 WinBoll 1Panel Gitea 应用的 WinBoll 项目源码仓库存取功能。（Gitea 应用已提供）
☁ WinBoll 主机提供 WinBoll Gitea 项目仓库存档功能。（Gitea 应用已提供）
☁ 提供 WinBoll 客户端用户登录功能。（Gitea 应用已提供）

### ☁ 看远方 ☁ ###
### ☁ 心忧虑 ☁ WinBoll-APP 应用前置需求
☁ WinBoll-APP WinBoll 项目根目录设定为手机的 /sdcard/WinBollStudio/Sources 目录。（需要用户手动建立文件夹）
☁ WinBoll-APP 具有手机 /sdcard/WinBoll 目录的存储权限。（需要手机操作系统授权）
☁ WinBoll-APP WinBoll 项目仓库源码存储路径为 /sdcard/WinBollStudio/Sources/APP.git（需要用户手动建立文件夹）
☁ WinBoll-APP 项目 APK 编译输出目录为 /sdcard/WinBollStudio/APKs/
☁ WinBoll-APP 应用签名验证可定制化。（WinBoll 项目已提供）
☁ WinBoll-APP 与系列衍生 APP 应用共享 cc.winboll.studio 命名空间资源。（WinBoll 项目已提供）
☁ WinBoll-APP 用户客户端信息存储在命名空间为 WinBoll APP MySQLLite 应用的 winbollappclient 数据库中。
☁ WinBoll-APP MySQLLite 应用的 winbollappclient 数据库中， 
   WinBoll 用户客户端信息设定为：
   <用户名, 唯一存储令牌Token>。

### ☁ 云游四方 ☁ ###
### ☁ 呔！ ☁ WinBoll-APP 应用需求规划
☁ WinBoll-APP 提供手机目录 /sdcard/WinBollStudio/Sources 的 WinBoll 项目源码管理功能。 

### ☁ 吁！ ☁ WinBoll-APP 共享计划前景
☁ WinBoll-APP 将会实现 https://winboll.cc/api 访问功能。
☁ WinBoll-APP 将会实现手机端 Android 应用的开发与管理功能。

## ☁ WinBoll ☁ WinBoll 主机忧虑
☁ WinBoll 将会提供 gitea.winboll.cc 域名用户注册登录功能。
☁ WinBoll 将会提供 WinBoll-APP 及其衍生应用的 Gitea 仓库管理服务。
☁ WinBoll 将会提供 winboll.cc 域名 WinBoll 项目组注册登录功能。

# 本项目要实际运用需要注意以下几个步骤：
# 在项目根目录下：
## 1. 项目模块编译环境设置(必须)，settings.gradle-demo 要复制为 settings.gradle，并取消相应项目模块的注释。
## 2. 项目 Android SDK 编译环境设置(可选)，local.properties-demo 要复制为 local.properties，并按需要设置 Android SDK 目录。
## 3. 类库型模块编译环境设置(可选)，winboll.properties-demo 要复制为 winboll.properties，并按需要设置 WinBoll Maven 库登录用户信息。


# ☆类库型项目编译方法
## 先编译类库对应的模块测试项目
### 修改模块测试项目的 build.properties 文件
设置属性 libraryProject=<类库项目模块文件夹名称>
### 再编译测试项目
$ bash .winboll/bashPublishAPKAddTag.sh <应用项目模块文件夹名称>
#### 测试项目编译后，编译器会复制一份 APK 到以下路径："/sdcard/WinBollStudio/APKs/<项目根目录名称>/tag/" 文件夹。
### 最后编译类库项目
$ bash .winboll/bashPublishLIBAddTag.sh <类库项目模块文件夹名称>
#### 类库模块编译命令执行后，编译器会发布到 WinBoll Nexus Maven 库：Maven 库地址可以参阅根项目目录配置 build.gradle 文件。
             
# ☆应用型项目编译方法
## 直接调用以下命令编译应用型项目
$ bash .winboll/bashPublishAPKAddTag.sh <应用项目模块文件夹名称>
#### 应用模块编译命令执行后，编译器会复制一份 APK 到以下路径："/sdcard/WinBollStudio/APKs/<项目根目录名称>/tag/" 文件夹。
