package cc.winboll.studio.ollama;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/27 19:55:28
 * @Describe 简单Http协议访问客户端
 */
import cc.winboll.studio.libappbase.LogUtils;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.Source;
import org.json.JSONException;
import org.json.JSONObject;

public class OllamaClient {

    public static final String TAG = "OllamaClient";

    private static final String API_BASE_URL = "https://ollama-api.winboll.cc";
    //private static final String API_BASE_URL = "http://10.8.0.10:11434";
    //private static final OkHttpClient client = new OkHttpClient();
    private static final OkHttpClient client = new OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build();

    // 1. 生成文本示例
//    static void generateText(String prompt, String model) {
//        String url = API_BASE_URL + "/api/generate";
//        try {
//            JSONObject payload = new JSONObject()
//                .put("model", model)
//                .put("prompt", prompt)
//                .put("temperature", 0.7)
//                .put("max_tokens", 200);
//
//            Request request = new Request.Builder()
//                .url(url)
//                .post(RequestBody.create(payload.toString(), MediaType.get("application/json")))
//                .build();
//
//            Response response = client.newCall(request).execute();
//            if (response.isSuccessful()) {
//                String result = response.body().string();
//                String formattedStream = OllamaResponseFormatter.formatStreamingResponse(result);
//                
//                // 输出示例：
//// [2025-03-27T19:34:29.274955439Z] [llama3.1:8b] It looks like you might have miss
//// [2025-03-27T19:34:30.482553089Z] [llama3.1:8b] pelled the word "Ollama" or perhaps said something that is not a standard word in the English language. However, I'm here to provide information and assistance on various topics, so please let me know what you meant by "Ollama." Was it related to a name, place, movie, game, or something else?
//                
//                LogUtils.d(TAG, formattedStream);
////                JSONObject json = new JSONObject(result);
////                LogUtils.d(TAG, "生成结果: " + json.getString("response"));
//                //System.out.println("生成结果: " + json.getString("response"));
//            } else {
//                LogUtils.d(TAG, "请求失败: " + response.code());
//                //System.out.println("请求失败: " + response.code() + " " + response.message());
//            }
//        } catch (JSONException|IOException e) {
//            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
//        }
//    }

    // 实时输出流式响应的函数
    static void generateTextStream(String prompt, String model, final OnAnswerCallback callback) {
        String url = API_BASE_URL + "/api/generate";
        try {
            JSONObject payload = new JSONObject()
                .put("model", model)
                .put("prompt", prompt)
                .put("temperature", 0.7)
                .put("max_tokens", 200)
                .put("stream", true); // 启用流式响应

            Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(payload.toString(), MediaType.get("application/json")))
                .build();

            LogUtils.d(TAG, "Request request");
            client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        LogUtils.d(TAG, "请求失败: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            LogUtils.d(TAG, "请求失败: " + response.code());
                            return;
                        }

                        try (ResponseBody body = response.body()) {
                            if (body == null) return;

                            // 实时流式解析
                            LogUtils.d(TAG, "实时流式解析");
                            Source source = body.source();
                            Buffer buffer = new Buffer();
                            StringBuilder fullResponse = new StringBuilder();
                            boolean isDone = false;

                            while (!isDone && source.read(buffer, 1024) != -1) {
                                //LogUtils.d(TAG, "!isDone");
                                String chunk = buffer.readUtf8();
                                String[] lines = chunk.split("\n");

                                for (String line : lines) {
                                    LogUtils.d(TAG, line);
                                    if (line.trim().startsWith("{\"model\":")) {
                                        LogUtils.d(TAG, line);
                                        String jsonStr = line;
                                        if (jsonStr.equals("[DONE]")) {
                                            isDone = true;
                                            LogUtils.d(TAG, "流式生成完成");
                                            break;
                                        }

                                        try {
                                            //LogUtils.d(TAG, jsonStr);
                                            JSONObject json = new JSONObject(jsonStr);
                                            //LogUtils.d(TAG, json.toString());
                                            String responseText = json.getString("response");
                                            //LogUtils.d(TAG, responseText);
                                            fullResponse.append(responseText);

                                            // 实时输出
                                            callback.onAnswer(responseText);
                                            LogUtils.d(TAG, "实时响应: " + responseText);

                                            // 处理完成状态
                                            if (json.getBoolean("done")) {
                                                isDone = true;
                                                String doneReason = json.optString("done_reason", "unknown");
                                                LogUtils.d(TAG, "生成完成 (原因: " + doneReason + ")");
                                                LogUtils.d(TAG, "完整回答: " + fullResponse.toString());
                                            }
                                        } catch (JSONException e) {
                                            LogUtils.d(TAG, "JSON解析错误: " + e.getMessage());
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
        } catch (JSONException e) {
            LogUtils.d(TAG, "JSON格式错误: " + e.getMessage());
        }
    }



    // 2. 获取模型列表示例
    static void getModelList() {
        String url = API_BASE_URL + "/v1/models";
        LogUtils.d(TAG, "url : " + url);
        Request request = new Request.Builder().url(url).build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                LogUtils.d(TAG, response.body().string());
//                JSONArray models = new JSONArray(response.body().string());
//                //System.out.println("可用模型列表:");
//                LogUtils.d(TAG, "可用模型列表:");
//                for (int i = 0; i < models.length(); i++) {
//                    JSONObject model = models.getJSONObject(i);
//                    LogUtils.d(TAG, "- " + model.getString("name") + " (" + model.getString("size") + ")");
//                    //System.out.println("- " + model.getString("name") + " (" + model.getString("size") + ")");
//                }
            } else {
                LogUtils.d(TAG, "获取模型列表失败: " + response.code());
                //System.out.println("获取模型列表失败: " + response.code());
            }
        } catch (IOException e) {
            LogUtils.d(TAG, e, Thread.currentThread().getStackTrace());
        }
    }

    static void unittest(String ask, OnAnswerCallback callback) {
        // 获取模型列表
        getModelList();

        // 生成文本
        generateTextStream(ask, "llama3.1:8b", callback);
    }

    public static class SyncAskThread extends Thread {
        private String ask;
        private OnAnswerCallback callback;

        public SyncAskThread(String ask, OnAnswerCallback callback) {
            this.ask = ask;
            this.callback = callback;
        }

        @Override
        public void run() {
            super.run();
            LogUtils.d(TAG, "run() start.");
            unittest(ask, callback);
            LogUtils.d(TAG, "run() end.");
        }
    }

    public interface OnAnswerCallback {
        void onAnswer(String answer);
    }
}
    

