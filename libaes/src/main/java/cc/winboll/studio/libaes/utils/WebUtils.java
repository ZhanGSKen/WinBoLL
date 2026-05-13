package cc.winboll.studio.libaes.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import cc.winboll.studio.libappbase.LogUtils;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/05 15:45
 * @LastEditTime 2026/01/05 19:30:00 HKT
 * @Describe 网页工具集（优化：新增合法性校验+浏览器可用性检查+链接格式自动修复）
 */
public class WebUtils {

    public static final String TAG = "WebUtils";

    /**
     * 唤起系统默认浏览器打开指定网站
     * @param context 上下文对象（建议使用 ApplicationContext 避免内存泄漏）
     * @param url 目标 URL（支持自动修复格式错误）
     */
    public static void openUrlInBrowser(Context context, String url) {
        // 1. 空指针与合法性校验
        if (context == null) {
            LogUtils.e(TAG, "openUrlInBrowser: Context is null");
            return;
        }
        if (url == null || url.trim().isEmpty()) {
            LogUtils.e(TAG, "openUrlInBrowser: Url is null or empty");
            showToast(context, "链接不能为空");
            return;
        }

        // 2. 链接格式自动修复（核心新增：处理多斜杠、补全协议头）
        String fixedUrl = fixUrlFormat(url.trim());
        LogUtils.d(TAG, "openUrlInBrowser: Fixed url from [" + url + "] to [" + fixedUrl + "]");

        // 3. 构建隐式意图
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fixedUrl));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 新任务栈启动

        // 4. 检查浏览器可用性
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                LogUtils.e(TAG, "openUrlInBrowser: Start activity failed", e);
                showToast(context, "打开浏览器失败，请手动复制链接");
            }
        } else {
            LogUtils.e(TAG, "openUrlInBrowser: No browser app found");
            showToast(context, "未找到可用的浏览器应用");
        }
    }

    /**
     * 工具方法：修复 URL 格式错误
     * 1. 补全 http/https 协议头
     * 2. 处理协议头后的多斜杠问题（如 https://mmec//path → https://mmec/path）
     * @param originalUrl 原始 URL
     * @return 修复后的 URL
     */
    private static String fixUrlFormat(String originalUrl) {
        String url = originalUrl;

        // 步骤1：补全协议头（优先 https）
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        // 步骤2：修复协议头后的多斜杠问题
        // 匹配 https:// 或 http:// 后的任意数量斜杠，替换为单斜杠
        url = url.replaceAll("(?<=https?://)[/]+", "/");

        return url;
    }

    /**
     * 工具方法：显示 Toast 提示（确保在主线程执行）
     */
    private static void showToast(final Context context, final String message) {
        if (context == null || message == null) {
            return;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
					}
				});
        }
    }
}

