# Claude Bridge 防火墙配置脚本
# 仅允许内网网段 192.168.123.0/24 访问桥接服务 TCP 8787
# 需以管理员身份运行
#
# @author 豆包&Big Pickle&MiMo&ZhanGSKen
# @since 1.0.0

$ErrorActionPreference = "Stop"
$RuleName = "Claude Bridge - Allow 192.168.123.0/24"

Write-Host "=== 1. 清理旧的 Claude Bridge 规则 ===" -ForegroundColor Cyan
Get-NetFirewallRule -DisplayName $RuleName -ErrorAction SilentlyContinue | ForEach-Object {
    Write-Host "  删除: $($_.DisplayName)"
    Remove-NetFirewallRule -Name $_.Name
}

Write-Host "`n=== 2. 新建入站允许规则 ===" -ForegroundColor Cyan
New-NetFirewallRule `
    -DisplayName $RuleName `
    -Direction Inbound `
    -Protocol TCP `
    -LocalPort 8787 `
    -RemoteAddress "192.168.123.0/24" `
    -Action Allow `
    -Profile Any `
    -Enabled True `
    -Description "仅允许内网 192.168.123.0/24 访问 Claude Bridge (TCP 8787)"

Write-Host "`n=== 3. 验证 ===" -ForegroundColor Cyan
$rule = Get-NetFirewallRule -DisplayName $RuleName
$port = $rule | Get-NetFirewallPortFilter
$addr = $rule | Get-NetFirewallAddressFilter
Write-Host "  名称:     $($rule.DisplayName)"
Write-Host "  状态:     $($rule.Enabled)"
Write-Host "  方向:     $($rule.Direction)"
Write-Host "  端口:     $($port.LocalPort)"
Write-Host "  远程地址: $($addr.RemoteAddress)"
Write-Host "`n=== 完成 ===" -ForegroundColor Green
