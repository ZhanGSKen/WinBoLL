package cc.winboll.studio.libaes.winboll;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/29 15:57:28
 * @Describe WinBoll 服务器服务情况测试访问进程。
 */
import cc.winboll.studio.libappbase.LogUtils;
import java.io.IOException;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class WinBollServerConnectionTestThread extends Thread {

    public static final String TAG = "WinBollClientService";

    private final String url;
    private final String username;
    private final String password;
    private final int connectTimeout;
    private final int readTimeout;
    private final int maxRetries;
    private boolean testComplete = false;

    public WinBollServerConnectionTestThread(String url, String username, String password) {
        this(url, username, password, 10000, 10000, 5);
    }

    public WinBollServerConnectionTestThread(String url, String username, String password, 
                                             int connectTimeout, int readTimeout, int maxRetries) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.maxRetries = maxRetries;
    }

    @Override
    public void run() {
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(connectTimeout, java.util.concurrent.TimeUnit.MILLISECONDS)
            .readTimeout(readTimeout, java.util.concurrent.TimeUnit.MILLISECONDS)
            .authenticator(new Authenticator() {
                @Override
                public Request authenticate(Route route, Response response) throws IOException {
                    return response.request().newBuilder()
                        .header("Authorization", Credentials.basic(username, password))
                        .build();
                }
            })
            .build();

        Request request = new Request.Builder()
            .url(url)
            .build();

        int retryCount = 0;
        while (!testComplete && retryCount <= maxRetries) {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    if ("OK".equalsIgnoreCase(responseBody.trim())) {
                        LogUtils.d(TAG, "[" + new java.util.Date() + "] 测试成功，服务器返回OK");
                        testComplete = true;
                    } else {
                        LogUtils.d(TAG, "[" + new java.util.Date() + "] 响应内容不符合预期：" + responseBody);
                    }
                } else {
                    LogUtils.d(TAG, "[" + new java.util.Date() + "] 请求失败，状态码：" + response.code());
                }
            } catch (IOException e) {
                LogUtils.d(TAG, "[" + new java.util.Date() + "] 连接异常：" + e.getMessage());
            }

            if (!testComplete) {
                try {
                    Thread.sleep(2000); // 等待2秒后重试
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LogUtils.d(TAG, "测试线程被中断");
                    return;
                }
                retryCount++;
                LogUtils.d(TAG, "[" + new java.util.Date() + "] 第" + retryCount + "次重试...");
            }
        }

        if (testComplete) {
            
        } else {
            LogUtils.d(TAG, "[" + new java.util.Date() + "] 达到最大重试次数，测试失败");
        }
    }

//    public static void main(String[] args) {
//        String targetUrl = "http://your-protected-server.com";
//        String username = "your_username";
//        String password = "your_password";
//
//        WinBollServerConnectionTestThread testThread = new WinBollServerConnectionTestThread(
//            targetUrl,
//            username,
//            password,
//            15000,  // 连接超时15秒
//            20000,  // 读取超时20秒
//            3       // 最大重试次数
//        );
//
//        testThread.start();
//    }
}

