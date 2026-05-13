package cc.winboll.studio.positions.utils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/09/30 16:09
 * @Describe NotificationUtils（适配API 30，修复系统默认铃声获取，任务通知循环响铃）
 */
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager; // 导入RingtoneManager（关键：用于获取系统默认铃声）
import android.net.Uri; // 导入Uri（存储铃声路径）
import android.os.Build;
import androidx.core.app.NotificationCompat;
import cc.winboll.studio.positions.R;
import cc.winboll.studio.positions.activities.LocationActivity;

/**
 * 通知栏工具类：
 * 1. 任务通知：铃声循环播放（适配API 30），修复系统默认铃声获取方式
 * 2. 前台服务通知：低打扰（无声无震动），符合API 30规范
 */
public class NotificationUtil {
    public static final String TAG = "NotificationUtils";
    // 任务通知常量（独立渠道，确保循环铃声配置不冲突）
    private static final String TASK_NOTIFICATION_CHANNEL_ID = "task_notification_channel_01";
    private static final String TASK_NOTIFICATION_CHANNEL_NAME = "任务通知（循环铃声）";
    // 前台服务通知常量（独立渠道，低打扰）
    private static final String FOREGROUND_SERVICE_CHANNEL_ID = "foreground_location_service_channel_02";
    private static final String FOREGROUND_SERVICE_CHANNEL_NAME = "位置服务";
    public static final int FOREGROUND_SERVICE_NOTIFICATION_ID = 10086; // 固定前台服务通知ID

    // ---------------------- 核心：任务通知（循环响铃+修复系统默认铃声） ----------------------
    private static int getNotificationId(String taskId) {
        return taskId.hashCode() & 0xFFFFFF; // 确保通知ID唯一且非负
    }

    public static void show(Context context, String taskId, String positionId, String taskDescription) {
        if (context == null || taskId == null || positionId == null) {
            return;
        }

        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }

        // 1. 初始化通知渠道（配置循环铃声参数，使用修复后的铃声获取方式）
        createNotificationChannel(notificationManager);

        // 2. 点击跳转Intent（携带任务/位置参数，适配API 30页面栈）
        Intent jumpIntent = new Intent(context, LocationActivity.class);
        jumpIntent.putExtra("EXTRA_POSITION_ID", positionId);
        jumpIntent.putExtra("EXTRA_TASK_ID", taskId);
        jumpIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // 3. PendingIntent（API 30强制加IMMUTABLE，避免安全异常）
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            getNotificationId(taskId),
            jumpIntent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // 4. 构建通知（核心：循环响铃+修复的系统默认铃声）
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, TASK_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // API 30强制要求，否则通知不显示
            .setContentTitle("任务提醒")
            .setContentText(taskDescription)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // 点击后取消通知，停止循环响铃
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // 确保铃声能正常播放（API 30规则）
            //.setVibrationPattern(new long[]{0, 300, 200, 300}) // 震动与铃声同步循环
            .setOnlyAlertOnce(false); // 重复通知也触发循环提醒

        // 关键修复：用RingtoneManager获取系统默认通知铃声（替代废弃的NotificationManager.getDefaultUri）
        Uri defaultNotificationRingtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (defaultNotificationRingtone != null) {
            builder.setSound(defaultNotificationRingtone); // 设置系统默认铃声
        } else {
            builder.setDefaults(NotificationCompat.DEFAULT_SOUND); // 极端情况：铃声Uri为空时，用默认提醒音兜底
        }

        // 5. 循环响铃核心：设置FLAG_INSISTENT（通知未取消则持续循环）
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_INSISTENT;

        // 6. 显示通知（触发循环响铃）
        notificationManager.notify(getNotificationId(taskId), notification);
    }

    // ---------------------- 核心：创建通知渠道（修复铃声配置，适配API 30） ----------------------
    private static void createNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 1. 任务通知渠道（用修复后的方式配置系统默认铃声）
            NotificationChannel taskChannel = new NotificationChannel(
                TASK_NOTIFICATION_CHANNEL_ID,
                TASK_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT // 重要性≥DEFAULT，否则铃声不响（API 30规则）
            );
            taskChannel.setDescription("任务提醒通知，铃声循环播放至点击取消");
            taskChannel.enableVibration(true);
            taskChannel.setVibrationPattern(new long[]{0, 300, 200, 300});
            taskChannel.setAllowBubbles(false); // 避免气泡打断循环铃声

            // 关键修复：渠道铃声也用RingtoneManager获取（与通知Builder保持一致，确保铃声统一）
            Uri channelDefaultRingtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            taskChannel.setSound(channelDefaultRingtone, null); // 绑定系统默认铃声到渠道

            // 2. 前台服务渠道（低打扰，无声无震动）
            NotificationChannel foregroundChannel = new NotificationChannel(
                FOREGROUND_SERVICE_CHANNEL_ID,
                FOREGROUND_SERVICE_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            );
            foregroundChannel.setDescription("位置服务运行中，无声音/震动提醒");
            foregroundChannel.enableVibration(false);
            foregroundChannel.setSound(null, null); // 明确关闭铃声
            foregroundChannel.setShowBadge(false);

            // 注册渠道（API 30覆盖旧配置，确保修复后的铃声生效）
            notificationManager.createNotificationChannel(taskChannel);
            notificationManager.createNotificationChannel(foregroundChannel);
        }
    }

    // ---------------------- 原有功能：取消任务通知（停止循环响铃） ----------------------
    public static void cancel(Context context, String taskId) {
        if (context == null || taskId == null) {
            return;
        }
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(getNotificationId(taskId)); // 取消后循环铃声自动停止
        }
    }

    // ---------------------- 前台服务通知（适配API 30，无声无震动） ----------------------
    public static Notification createForegroundServiceNotification(Context context, String serviceStatus) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null for foreground service notification");
        }

        // 确保前台服务渠道已创建（低打扰配置）
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            createNotificationChannel(notificationManager);
        }

        // 点击跳转Intent
        Intent jumpIntent = new Intent(context, LocationActivity.class);
        jumpIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // PendingIntent（API 30必加IMMUTABLE）
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            FOREGROUND_SERVICE_NOTIFICATION_ID,
            jumpIntent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // 构建前台服务通知（无声无震动，符合低打扰）
        return new NotificationCompat.Builder(context, FOREGROUND_SERVICE_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("位置服务运行中")
            .setContentText(serviceStatus)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // 不可手动清除（前台服务规范）
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setDefaults(0) // 禁用所有默认提醒（无声无震动）
            .build();
    }

    // ---------------------- 前台服务通知状态更新（适配API 30） ----------------------
    public static void updateForegroundServiceStatus(Context context, String newServiceStatus) {
        if (context == null) {
            return;
        }
        Notification updatedNotification = createForegroundServiceNotification(context, newServiceStatus);
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(FOREGROUND_SERVICE_NOTIFICATION_ID, updatedNotification);
        }
    }
}

