package cc.winboll.studio.ollama;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/03/28 03:38:50
 */
import org.json.JSONArray;
import org.json.JSONObject;

public class OllamaResponseFormatter {
    public static final String TAG = "OllamaResponseFormatter";

    // 处理模型列表响应
    public static String formatModelList(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONArray models = json.getJSONArray("data");
            StringBuilder sb = new StringBuilder();
            sb.append("可用模型列表：\n");

            for (int i = 0; i < models.length(); i++) {
                JSONObject model = models.getJSONObject(i);
                String modelId = model.getString("id");
                sb.append(String.format("-%d. %s\n", i + 1, modelId));
            }
            return sb.toString();
        } catch (Exception e) {
            return "格式解析错误: " + e.getMessage();
        }
    }

    // 处理流式生成文本响应
    public static String formatStreamingResponse(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            StringBuilder sb = new StringBuilder();
            String responseText = json.getString("response");
            boolean isDone = json.getBoolean("done");

            // 添加时间戳和模型标识
            String timestamp = json.getString("created_at");
            String modelName = json.getString("model");
            sb.append(String.format("[%s] [%s] ", timestamp, modelName));

            // 处理响应内容
            if (responseText.isEmpty() && isDone) {
                sb.append("生成完成\n");
            } else {
                sb.append(responseText);
                if (isDone) {
                    String doneReason = json.optString("done_reason", "unknown");
                    sb.append(String.format(" (完成原因: %s)\n", doneReason));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "格式解析错误: " + e.getMessage();
        }
    }

    // 使用示例
    public static void main(String[] args) {
        // 模型列表测试
        String modelListJson = "{\"object\":\"list\",\"data\":[{...}]}";
        System.out.println(formatModelList(modelListJson));

        // 流式响应测试
        String streamingJson = "{\"model\":\"llama3.1:8b\",\"created_at\":\"2025-03-27T19:34:29.274955439Z\",\"response\":\"It\",\"done\":false}";
        System.out.println(formatStreamingResponse(streamingJson));
    }
}

