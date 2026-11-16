# Claude Bridge 启动脚本（供计划任务调用）
#
# 使用系统 Node（C:\Program Files\nodejs\node.exe），不依赖其他运行时。
#
# @author 豆包&Big Pickle&MiMo&ZhanGSKen
# @since 1.0.0

$ErrorActionPreference = "Stop"

$BridgeRoot = "C:\0WINBOT\\ClaudeCodeAndroid-Project-Development\\ClaudeCodeAndroid\bridge"
$NodeExe = "C:\Program Files\nodejs\node.exe"

if (-not (Test-Path -LiteralPath $NodeExe)) {
    $NodeExe = (Get-Command node -ErrorAction SilentlyContinue).Source
}
if (-not $NodeExe) {
    throw "找不到 node.exe，请先安装 Node.js 18+"
}

Set-Location -LiteralPath $BridgeRoot
& $NodeExe "src\server.mjs"
