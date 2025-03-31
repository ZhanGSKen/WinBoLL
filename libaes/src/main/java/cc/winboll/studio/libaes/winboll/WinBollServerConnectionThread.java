package cc.winboll.studio.libaes.winboll;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/29 15:57:28
 * @Describe WinBoll 服务器服务情况测试访问进程。
 */
import cc.winboll.studio.libappbase.LogUtils;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// 新增自定义回调接口
interface TextCallback {
    void onSuccess(String text);
    void onFailure(Exception e);
}

public class WinBollServerConnectionThread extends Thread {

    public static final String TAG = "WinBollClientService";

    private final String url;
    private final String username;
    private final String password;
    private final int connectTimeout;
    private final int readTimeout;
    private final int maxRetries;
    private final TextCallback callback; // 新增回调成员变量

    // 新增带回调的构造函数
    public WinBollServerConnectionThread(String url, String username, String password, TextCallback callback) {
        this(url, username, password, 10000, 10000, 5, callback);
    }

    // 修改原有构造函数，添加回调参数
    public WinBollServerConnectionThread(String url, String username, String password, 
                                             int connectTimeout, int readTimeout, int maxRetries, TextCallback callback) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.maxRetries = maxRetries;
        this.callback = callback;
    }

    @Override
    public void run() {
        LogUtils.d(TAG, String.format("run() url %s\nusername %s\npassword %s", url, username, password));
        String credential = Credentials.basic(username, password);
        LogUtils.d(TAG, String.format("credential %s", credential));

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(url)
            .header("Accept", "text/plain")
            .header("Authorization", credential)
            .build();
            
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // 优先调用自定义回调
                    if (callback != null) {
                        callback.onFailure(e);
                    } else {
                        LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        if (callback != null) {
                            callback.onFailure(new Exception("Unexpected code " + response));
                        } else {
                            LogUtils.d(TAG, "Unexpected code " + response, Thread.currentThread().getStackTrace());
                        }
                        return;
                    }

                    try {
                        String text = response.body().string();
                        // 优先调用自定义回调
                        if (callback != null) {
                            callback.onSuccess(text);
                        } else {
                            LogUtils.d(TAG, text);
                        }
                    } catch (Exception e) {
                        if (callback != null) {
                            callback.onFailure(e);
                        } else {
                            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                        }
                    }
                }
            });
    }
}
