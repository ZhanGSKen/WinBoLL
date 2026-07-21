package cc.winboll.studio.winboll.utils;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/06/04 17:21
 * @Describe 应用登录与接口工具
 */
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.winboll.models.ResponseData;
import cc.winboll.studio.winboll.models.UserInfoModel;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class YunUtils {
    public static final String TAG = "YunUtils";
    // 私有静态实例，类加载时创建
    private static volatile YunUtils INSTANCE;
    Context mContext;
    UserInfoModel mUserInfoModel;
    String token = "";
    String mDataFolderPath = "";
    String mUserInfoModelPath = "";

    private static final int CONNECT_TIMEOUT = 15; // 连接超时时间（秒）
    private static final int READ_TIMEOUT = 20;    // 读取超时时间（秒）
    private static volatile YunUtils instance;
    private OkHttpClient okHttpClient;
    private Handler mainHandler; // 主线程 Handler

    // 私有构造方法，防止外部实例化
    private YunUtils(Context context) {
        LogUtils.d(TAG, "YunUtils");
        mContext = context;
        mDataFolderPath = mContext.getExternalFilesDir(TAG).toString();
        File fTest = new File(mDataFolderPath);
        if (!fTest.exists()) {
            fTest.mkdirs();
        }
        mUserInfoModelPath = mDataFolderPath + File.separator + "UserInfoModel.rsajson";

        okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .build();
        mainHandler = new Handler(Looper.getMainLooper()); // 获取主线程 Looper
    }

    // 公共静态方法，返回唯一实例
    public static synchronized YunUtils getInstance(Context context) {
        LogUtils.d(TAG, "getInstance");
        if (INSTANCE == null) {
            INSTANCE = new YunUtils(context);
        }
        return INSTANCE;
    }

    public void checkLoginStatus() {
        String token = getLocalToken();
        LogUtils.d(TAG, String.format("checkLoginStatus token is %s", token));
    }

    String getLocalToken() {
        UserInfoModel userInfoModel = loadUserInfoModel();
        return (userInfoModel == null) ?"": userInfoModel.getToken();
    }

    public void login(String host, UserInfoModel userInfoModel) {
        LogUtils.d(TAG, "login");

        // 发送 POST 请求
        String apiUrl = host + "/login/index.php";
        // 序列化对象为JSON
        Gson gson = new Gson();
        String jsonData = gson.toJson(userInfoModel); // 自动生成标准JSON
        //String jsonData = userInfoModel.toString();
        LogUtils.d(TAG, "要发送的数据 : " + jsonData);

        sendPostRequest(apiUrl, jsonData, new OnResponseListener() {
                // 成功回调（主线程）
                @Override
                public void onSuccess(String responseBody) {
                    LogUtils.d(TAG, "onSuccess");
                    LogUtils.d(TAG, String.format("responseBody %s", responseBody));
                    Gson gson = new Gson();
                    ResponseData result = gson.fromJson(responseBody, ResponseData.class); // 转为 Result 实例
                    if(result.getStatus().equals(ResponseData.STATUS_SUCCESS)) {
                        
                            UserInfoModel userInfoModel = result.getData();
                            if (userInfoModel != null) {
                                LogUtils.d(TAG, "收到网站 UserInfoModel");
                                String token = userInfoModel.getToken();
                                saveLocalToken(token);
                                checkLoginStatus();
                            }
                       
                    } else if(result.getStatus().equals(ResponseData.STATUS_ERROR)) {
                        try {
                            String decodedMessage = URLDecoder.decode(result.getMessage(), "UTF-8");
                            LogUtils.d(TAG, "服务器返回信息: " + decodedMessage);
                        } catch (UnsupportedEncodingException e) {
                            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
                        }
                    }
                }

                // 失败回调（主线程）
                @Override
                public void onFailure(String errorMsg) {
                    LogUtils.d(TAG, errorMsg);
                    // 处理错误
                }
            });
    }

    public void saveLocalToken(String token) {
        UserInfoModel userInfoModel = new UserInfoModel();
        userInfoModel.setToken(token);
        saveUserInfoModel(userInfoModel);
    }

    UserInfoModel loadUserInfoModel() {
//        LogUtils.d(TAG, "loadUserInfoModel");
//        if (new File(mUserInfoModelPath).exists()) {
//            try {
//                // 加载加密后的模型数据
//                byte[] encryptedData = FileUtils.readByteArrayFromFile(mUserInfoModelPath);
//                // 加载 RSA 工具
//                RSAUtils utils = RSAUtils.getInstance(mContext);
//                KeyPair keyPair = utils.getOrGenerateKeys();
//                //PublicKey publicKey = keyPair.getPublic();
//                PrivateKey privateKey = keyPair.getPrivate();
//                // 私钥解密模型数据
//                String szInfo = utils.decryptWithPrivateKey(encryptedData, keyPair.getPrivate());
//                LogUtils.d(TAG, String.format("szInfo %s", szInfo));
//                mUserInfoModel = UserInfoModel.parseStringToBean(szInfo, UserInfoModel.class);
//                if (mUserInfoModel == null) {
//                    LogUtils.d(TAG, "模型数据解析为空数据。");
//                }
//                LogUtils.d(TAG, "UserInfoModel 解密加载结束。");
//            } catch (Exception e) {
//                LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
//            }
//        } else {
//            LogUtils.d(TAG, "云服务登录信息不存在。");
//            mUserInfoModel = null;
//        }
//        return mUserInfoModel;
		return null;
    }

    void saveUserInfoModel(UserInfoModel userInfoModel) {
//        LogUtils.d(TAG, "saveUserInfoModel");
//        try {
//            String szInfo = userInfoModel.toString();
//            LogUtils.d(TAG, "原始数据: " + szInfo);
//
//            RSAUtils utils = RSAUtils.getInstance(mContext);
//            KeyPair keyPair = utils.getOrGenerateKeys();
//            PublicKey publicKey = keyPair.getPublic();
//
//            // 公钥加密（传入字节数组，避免中间字符串转换）
//            byte[] encryptedData = utils.encryptWithPublicKey(szInfo, publicKey);
//
//            // 保存加密字节数组到文件（直接操作字节，无需转字符串）
//            FileUtils.writeByteArrayToFile(encryptedData, mUserInfoModelPath);
//            LogUtils.d(TAG, "加密数据已保存");
//
//            // 测试解密（仅调试用）
//            String szInfo2 = utils.decryptWithPrivateKey(encryptedData, keyPair.getPrivate());
//            LogUtils.d(TAG, "解密结果: " + szInfo2);
//
//            mUserInfoModel = UserInfoModel.parseStringToBean(szInfo2, UserInfoModel.class);
//            if (mUserInfoModel == null) {
//                LogUtils.d(TAG, "模型解析失败");
//            }
//        } catch (Exception e) {
//            LogUtils.d(TAG, "加密/解密失败: " + e.getMessage());
//        }
    }

    // 发送 POST 请求（JSON 数据）
    public void sendPostRequest(String url, String data, OnResponseListener listener) {
        RequestBody requestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"), // 关键头信息
            data.getBytes(StandardCharsets.UTF_8)
        );

        Request request = new Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json") // 显式添加头
            .build();

        executeRequest(request, listener);
    }

    // 发送 GET 请求
    public void sendGetRequest(String url, OnResponseListener listener) {
        Request request = new Request.Builder()
            .url(url)
            .get()
            .build();
        executeRequest(request, listener);
    }

    // 执行请求（子线程处理）
    private void executeRequest(final Request request, final OnResponseListener listener) {
        okHttpClient.newCall(request).enqueue(new Callback() {
                // 响应成功（子线程）
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) {
                            postFailure(listener, "响应码错误：" + response.code());
                            return;
                        }
                        String responseBody = response.body().string();
                        postSuccess(listener, responseBody);
                    } catch (Exception e) {
                        postFailure(listener, "解析失败：" + e.getMessage());
                    }
                }

                // 响应失败（子线程）
                @Override
                public void onFailure(Call call, IOException e) {
                    postFailure(listener, "网络失败：" + e.getMessage());
                }

                // 主线程回调（使用 Handler）
                private void postSuccess(final OnResponseListener listener, final String msg) {
                    mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onSuccess(msg);
                            }
                        });
                }

                private void postFailure(final OnResponseListener listener, final String msg) {
                    mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onFailure(msg);
                            }
                        });
                }
            });
    }

    public interface OnResponseListener {
        /**
         * 成功响应（主线程回调）
         * @param responseBody 响应体字符串
         */
        void onSuccess(String responseBody);

        /**
         * 失败回调（包含错误信息）
         * @param errorMsg 错误描述
         */
        void onFailure(String errorMsg);
    }
}
