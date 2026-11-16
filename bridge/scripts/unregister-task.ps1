# 卸载 Claude Bridge 计划任务
#
# @author 豆包&Big Pickle&MiMo&ZhanGSKen
# @since 1.0.0

$TaskName = "ClaudeBridge"

$task = Get-ScheduledTask -TaskName $TaskName -ErrorAction SilentlyContinue
if ($task) {
    Stop-ScheduledTask -TaskName $TaskName -ErrorAction SilentlyContinue
    Unregister-ScheduledTask -TaskName $TaskName -Confirm:$false
    Write-Host "已删除任务: $TaskName"
} else {
    Write-Host "未找到任务: $TaskName"
}

# 顺带停掉残留进程
Get-Process -Name node -ErrorAction SilentlyContinue |
    Where-Object { $_.Path -and $_.Path -like "*nodejs*" } |
    ForEach-Object {
        Write-Host "停止 node 进程: $($_.Id)"
        Stop-Process -Id $_.Id -Force
    }
