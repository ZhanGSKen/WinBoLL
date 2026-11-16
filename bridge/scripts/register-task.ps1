# 注册 Claude Bridge 为登录自启的计划任务
#
# 任务名：ClaudeBridge
# 行为：用户登录后隐藏窗口启动 bridge/start-bridge.ps1，异常退出自动重启 3 次
#
# @author 豆包&Big Pickle&MiMo&ZhanGSKen
# @since 1.0.0

$ErrorActionPreference = "Stop"

$TaskName = "ClaudeBridge"
$StartScript = "C:\0WINBOT\ClaudeCodeAndroid-Project-Development\ClaudeCodeAndroid\bridge\start-bridge.ps1"

if (-not (Test-Path -LiteralPath $StartScript)) {
    throw "启动脚本不存在: $StartScript"
}

$existing = Get-ScheduledTask -TaskName $TaskName -ErrorAction SilentlyContinue
if ($existing) {
    Write-Host "已存在同名任务，先删除旧任务"
    Unregister-ScheduledTask -TaskName $TaskName -Confirm:$false
}

$action = New-ScheduledTaskAction `
    -Execute "powershell.exe" `
    -Argument "-NoProfile -WindowStyle Hidden -ExecutionPolicy Bypass -File `"$StartScript`""

$trigger = New-ScheduledTaskTrigger -AtLogOn -User $env:USERNAME

$settings = New-ScheduledTaskSettingsSet `
    -AllowStartIfOnBatteries `
    -DontStopIfGoingOnBatteries `
    -StartWhenAvailable `
    -RestartCount 3 `
    -RestartInterval (New-TimeSpan -Minutes 1) `
    -ExecutionTimeLimit ([TimeSpan]::Zero)

$principal = New-ScheduledTaskPrincipal -UserId $env:USERNAME -LogonType Interactive -RunLevel Limited

Register-ScheduledTask -TaskName $TaskName -Action $action -Trigger $trigger `
    -Settings $settings -Principal $principal `
    -Description "Claude Code 桥接服务（安卓客户端后端，默认端口 8787）" | Out-Null

Write-Host "任务已注册: $TaskName"
Start-ScheduledTask -TaskName $TaskName
Start-Sleep -Seconds 3
Get-ScheduledTask -TaskName $TaskName | Select-Object TaskName, State | Format-Table -AutoSize
