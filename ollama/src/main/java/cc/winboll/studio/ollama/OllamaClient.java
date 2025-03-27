package cc.winboll.studio.ollama;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/27 19:55:28
 * @Describe 简单Http协议访问客户端
 */
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import cc.winboll.studio.libappbase.LogUtils;

public class OllamaClient {

    public static final String TAG = "OllamaClient";

    //private static final String API_BASE_URL = "http://localhost:11434";
    private static final String API_BASE_URL = "https://ollama.winboll.cc";
    private static final OkHttpClient client = new OkHttpClient();

    // 1. 生成文本示例
    static void generateText(String prompt, String model) {
        String url = API_BASE_URL + "/api/generate";
        try {
            JSONObject payload = new JSONObject()
                .put("model", model)
                .put("prompt", prompt)
                .put("temperature", 0.7)
                .put("max_tokens", 200);

            Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(payload.toString(), MediaType.get("application/json")))
                .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String result = response.body().string();
                JSONObject json = new JSONObject(result);
                LogUtils.d(TAG, "生成结果: " + json.getString("response"));
                //System.out.println("生成结果: " + json.getString("response"));
            } else {
                LogUtils.d(TAG, "请求失败: " + response.code());
                //System.out.println("请求失败: " + response.code() + " " + response.message());
            }
        } catch (JSONException|IOException e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
    }

    // 2. 获取模型列表示例
    static void getModelList() {
        String url = API_BASE_URL + "/api/models";
        LogUtils.d(TAG, "url : " + url);
        Request request = new Request.Builder().url(url).build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                JSONArray models = new JSONArray(response.body().string());
                //System.out.println("可用模型列表:");
                LogUtils.d(TAG, "可用模型列表:");
                for (int i = 0; i < models.length(); i++) {
                    JSONObject model = models.getJSONObject(i);
                    LogUtils.d(TAG, "- " + model.getString("name") + " (" + model.getString("size") + ")");
                    //System.out.println("- " + model.getString("name") + " (" + model.getString("size") + ")");
                }
            } else {
                LogUtils.d(TAG, "获取模型列表失败: " + response.code());
                //System.out.println("获取模型列表失败: " + response.code());
            }
        } catch (JSONException | IOException e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
    }

    static void unittest(String ask) {
        // 获取模型列表
        getModelList();

        // 生成文本
        generateText(ask, "llama2");
    }
    
    public static class SyncAskThread extends Thread {
        String ask;
        public SyncAskThread(String ask) {
            this.ask = ask;
        }

        @Override
        public void run() {
            super.run();
            LogUtils.d(TAG, "run() start.");
            unittest(this.ask);
            LogUtils.d(TAG, "run() end.");
        }
        
    }
}
    

