package cc.winboll.studio.contacts.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.core.app.ActivityCompat;
import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/02/26 15:21:48
 * @Describe 拨打电话工具类：封装拨打电话逻辑与权限校验
 */
public class PhoneUtils {
    // ====================== 常量定义区 ======================
    public static final String TAG = "PhoneUtils";
    // 拨打电话 Action 与 Uri 前缀
    private static final String CALL_ACTION = Intent.ACTION_CALL;
    private static final String TEL_URI_PREFIX = "tel:";

    // ====================== 核心工具方法区 ======================
    /**
     * 直接拨打电话（需申请 CALL_PHONE 权限）
     * @param context     上下文对象
     * @param phoneNumber 目标电话号码
     */
    public static void call(Context context, String phoneNumber) {
        // 空值校验：防止上下文或号码为空导致异常
        if (context == null) {
            LogUtils.e(TAG, "call: Context 为 null，无法执行拨打电话操作");
            return;
        }
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            LogUtils.e(TAG, "call: 电话号码为空，无法执行拨打电话操作");
            return;
        }
        String targetPhone = phoneNumber.trim();
        LogUtils.d(TAG, "call: 准备拨打号码 | " + targetPhone);

        // 权限校验：检查是否持有拨打电话权限
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            LogUtils.w(TAG, "call: 缺少 CALL_PHONE 权限，无法直接拨打电话");
            return;
        }

        // 构建拨打电话 Intent 并启动
        Intent callIntent = new Intent(CALL_ACTION);
        callIntent.setData(Uri.parse(TEL_URI_PREFIX + targetPhone));
        // 添加 FLAG 支持非 Activity 上下文启动
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(callIntent);
        LogUtils.i(TAG, "call: 拨打电话 Intent 已发送 | 号码=" + targetPhone);
    }
}

