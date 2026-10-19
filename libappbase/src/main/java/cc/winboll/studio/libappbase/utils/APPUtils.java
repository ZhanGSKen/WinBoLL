package cc.winboll.studio.libappbase.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import cc.winboll.studio.libappbase.GlobalApplication;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.libappbase.models.SignCheckResponse;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @Author 豆包&ZhanGSKen<zhangsken@qq.com>
 * @CreateTime 2026-01-20 19:17:00
 * @LastEditTime 2026-01-24 17:58:00
 * @Describe APPUtils 应用合法性校验工具类（OKHTTP网络校验版，兼容Java7）
 * 对外传入签名/哈希值，拼接调试标识后发起网络校验，主线程返回校验结果
 */
public class APPUtils {
    // ===================================== 全局常量/单例属性 =====================================
    public static final String TAG = "APPUtils";
    // 网络校验接口基础地址
    private static final String CHECK_API_URI = "api/app-signatures-check";
    // OKHTTP客户端单例（复用连接，避免资源浪费）
    private static final OkHttpClient sOkHttpClient = new OkHttpClient();
    // Gson解析单例（全局复用，提高解析效率）
    private static final Gson sGson = new Gson();

    // ===================================== 对外核心校验方法 =====================================
    /**
     * 检查应用合法性（外部传入签名+哈希，拼接调试标识发起网络校验）
     * @param context      上下文，用于主线程回调
     * @param projectName  项目名称（服务端区分项目标识）
     * @param versionName  应用版本名（服务端版本校验）
     * @param clientSign   外部计算的应用签名字符串（Base64）
     * @param clientHash   外部计算的APK SHA256哈希字符串（小写16进制）
     * @param callback     校验结果回调（主线程调用，返回是否合法+提示信息）
     */
    public void checkAPKValidation(Context context, String appName, String versionName,
                                   String clientSign, String clientHash, final CheckResultCallback callback) {
        // 方法调用+全量入参调试日志
        LogUtils.d(TAG, "checkAPKValidation: 方法调用，入参-> appName=" + appName
				   + ", versionName=" + versionName + ", clientSign=" + clientSign + ", clientHash=" + clientHash);

        // 1. 核心入参空值校验（快速失败）
        if (context == null) {
            LogUtils.w(TAG, "checkAPKValidation: 入参context为空，直接返回校验失败");
            callCallbackOnMainThread(callback, false, "上下文对象不能为空");
            return;
        }
        if (isStringEmpty(appName)) {
            LogUtils.w(TAG, "checkAPKValidation: 入参projectName为空/空白，直接返回校验失败");
            callCallbackOnMainThread(callback, false, "项目名称不能为空");
            return;
        }
        if (isStringEmpty(versionName)) {
            LogUtils.w(TAG, "checkAPKValidation: 入参versionName为空/空白，直接返回校验失败");
            callCallbackOnMainThread(callback, false, "应用版本名不能为空");
            return;
        }
        if (isStringEmpty(clientSign)) {
            LogUtils.w(TAG, "checkAPKValidation: 入参clientSign为空/空白，直接返回校验失败");
            callCallbackOnMainThread(callback, false, "应用签名字符串不能为空");
            return;
        }
        if (isStringEmpty(clientHash)) {
            LogUtils.w(TAG, "checkAPKValidation: 入参clientHash为空/空白，直接返回校验失败");
            callCallbackOnMainThread(callback, false, "APK SHA256哈希字符串不能为空");
            return;
        }
        LogUtils.d(TAG, "checkAPKValidation: 入参校验通过，开始处理网络请求");

        // 2. 动态参数URL编码（避免特殊字符导致请求解析异常）
        LogUtils.d(TAG, "checkAPKValidation: 开始对动态参数进行UTF-8 URL编码");
        String encodeProjectName = urlEncode(appName);
        String encodeVersionName = urlEncode(versionName);
        String encodeClientSign = urlEncode(clientSign);
        String encodeClientHash = urlEncode(clientHash);
        String isDebug = String.valueOf(GlobalApplication.isDebugging());
        LogUtils.d(TAG, "checkAPKValidation: 参数编码完成，debug标识=" + isDebug);

        // 3. 构建完整网络校验请求URL
        String requestUrl = String.format("%s?isDebug=%s&projectName=%s&versionName=%s&clientSign=%s&clientHash=%s",
										  GlobalApplication.getWinbollHost() + CHECK_API_URI,
										  isDebug,
										  encodeProjectName,
										  encodeVersionName,
										  encodeClientSign,
										  encodeClientHash);
        LogUtils.d(TAG, "checkAPKValidation: 构建网络校验请求URL=" + requestUrl);

        // 4. 发起OKHTTP异步GET请求（避免阻塞主线程）
        LogUtils.d(TAG, "checkAPKValidation: 发起异步网络校验请求");
        Request request = new Request.Builder().url(requestUrl).build();
        sOkHttpClient.newCall(request).enqueue(new Callback() {
				@Override
				public void onFailure(Call call, IOException e) {
					String errorMsg = "网络校验请求失败：" + e.getMessage();
					LogUtils.e(TAG, "checkAPKValidation: " + errorMsg, e);
					callCallbackOnMainThread(callback, false, errorMsg);
				}

				@Override
				public void onResponse(Call call, Response response) throws IOException {
					if (response.isSuccessful() && response.body() != null) {
						// 响应成功，解析返回JSON
						String responseJson = response.body().string();
						LogUtils.d(TAG, "checkAPKValidation: 网络校验响应成功，JSON=" + responseJson);
						SignCheckResponse checkResponse = sGson.fromJson(responseJson, SignCheckResponse.class);
						boolean isValid = checkResponse != null && checkResponse.isValid();
						String msg = checkResponse != null ? checkResponse.getMessage() : "服务端响应解析失败";
						LogUtils.d(TAG, "checkAPKValidation: 校验结果解析完成，isValid=" + isValid + ", 提示信息=" + msg);
						callCallbackOnMainThread(callback, isValid, msg);
					} else {
						// 响应失败，返回状态码信息
						String errorMsg = "网络校验响应失败，服务端状态码=" + response.code();
						LogUtils.e(TAG, "checkAPKValidation: " + errorMsg);
						callCallbackOnMainThread(callback, false, errorMsg);
					}
				}
			});
    }

    // ===================================== 内部工具方法 =====================================
    /**
     * 字符串空值/空白校验工具
     * @param str 待校验字符串
     * @return true=空/空白，false=非空
     */
    private boolean isStringEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * URL编码工具（Java7适配，UTF-8编码，处理特殊字符）
     * @param content 待编码内容
     * @return 编码后的字符串，编码失败返回原内容
     */
    private String urlEncode(String content) {
        try {
            return URLEncoder.encode(content, "UTF-8");
        } catch (Exception e) {
            LogUtils.e(TAG, "urlEncode: 字符串编码失败，content=" + content, e);
            return content;
        }
    }

    /**
     * 主线程执行回调（统一处理，避免外部线程切换）
     * @param callback 回调接口
     * @param isValid  是否合法
     * @param message   提示信息
     */
    private void callCallbackOnMainThread(final CheckResultCallback callback,
                                          final boolean isValid, final String message) {
        if (callback == null) {
            LogUtils.w(TAG, "callCallbackOnMainThread: 回调接口为null，无需执行");
            return;
        }
        // 已在主线程直接执行，否则切换主线程
        if (Looper.myLooper() == Looper.getMainLooper()) {
            callback.onResult(isValid, message);
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						callback.onResult(isValid, message);
					}
				});
        }
    }

    // ===================================== 校验结果回调接口 =====================================
    /**
     * 应用合法性校验结果回调接口（主线程调用）
     */
    public interface CheckResultCallback {
        /**
         * 校验结果回调方法
         * @param isValid 是否合法（true=校验通过，false=校验失败）
         * @param message 校验提示信息（失败时返回错误原因，成功时返回服务端提示）
         */
        void onResult(boolean isValid, String message);
    }
}

