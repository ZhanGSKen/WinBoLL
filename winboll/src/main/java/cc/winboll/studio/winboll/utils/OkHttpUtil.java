package cc.winboll.studio.winboll.utils;

import android.os.Handler;
import android.os.Looper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp网络请求工具类
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2026/01/07
 */
public class OkHttpUtil {

    private static OkHttpClient sOkHttpClient;
    private static Handler sMainHandler = new Handler(Looper.getMainLooper());

    static {
        sOkHttpClient = new OkHttpClient.Builder()
			.connectTimeout(10, TimeUnit.SECONDS)
			.readTimeout(10, TimeUnit.SECONDS)
			.writeTimeout(10, TimeUnit.SECONDS)
			.build();
    }

    /**
     * GET请求
     * @param url 请求地址
     * @param callback 回调
     */
    public static void get(String url, final OnResultCallback callback) {
        Request request = new Request.Builder()
			.url(url)
			.get()
			.build();

        sOkHttpClient.newCall(request).enqueue(new Callback() {
				@Override
				public void onFailure(Call call, final IOException e) {
					sMainHandler.post(new Runnable() {
							@Override
							public void run() {
								if (callback != null) {
									callback.onFailure(e.getMessage());
								}
							}
						});
				}

				@Override
				public void onResponse(Call call, final Response response) throws IOException {
					final String result = response.body().string();
					sMainHandler.post(new Runnable() {
							@Override
							public void run() {
								if (callback != null) {
									if (response.isSuccessful()) {
										callback.onSuccess(result);
									} else {
										callback.onFailure("请求失败：" + response.code());
									}
								}
							}
						});
				}
			});
    }

    /**
     * 回调接口
     */
    public interface OnResultCallback {
        void onSuccess(String result);
        void onFailure(String errorMsg);
    }
}

