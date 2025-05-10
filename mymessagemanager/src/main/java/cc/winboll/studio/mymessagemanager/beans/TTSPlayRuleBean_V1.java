package cc.winboll.studio.mymessagemanager.beans;

/**
 * @Author ZhanGSKen<zhangsken@188.com>
 * @Date 2024/05/28 20:22:12
 * @Describe TTS 语音播放规则类，V1 旧版。
 */
import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.mymessagemanager.utils.FileUtil;
import cc.winboll.studio.shared.log.LogUtils;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

public class TTSPlayRuleBean_V1 {

    public static final String TAG = "TTSPlayRuleBean2";

    // 用户ID
    int userId = -1;
    // TTS语音规则名称
	String ruleName = "";
    // 短信测试文本
	String demoSMSText = "";
    // 短信内容查询正则文本
	String patternText = "";
    // TTS语音播报正则文本
	String ttsRuleText = "";
    // 是否启用简单视图
    boolean isSimpleView = false;
    // 是否启用规则
    boolean isEnable = false;

    public TTSPlayRuleBean_V1(int userId, String ruleName, String demoSMSText, String patternText, String ttsRuleText, boolean isSimpleView, boolean isEnable) {
        this.userId = userId;
        this.ruleName = ruleName;
        this.demoSMSText = demoSMSText;
        this.patternText = patternText;
        this.ttsRuleText = ttsRuleText;
        this.isSimpleView = isSimpleView;
        this.isEnable = isEnable;
    }

    public TTSPlayRuleBean_V1() {}

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setDemoSMSText(String demoSMSText) {
        this.demoSMSText = demoSMSText;
    }

    public String getDemoSMSText() {
        return demoSMSText;
    }

    public void setPatternText(String patternText) {
        this.patternText = patternText;
    }

    public String getPatternText() {
        return patternText;
    }

    public void setTtsRuleText(String ttsRuleText) {
        this.ttsRuleText = ttsRuleText;
    }

    public String getTtsRuleText() {
        return ttsRuleText;
    }

    public void setIsSimpleView(boolean isSimpleView) {
        this.isSimpleView = isSimpleView;
    }

    public boolean isSimpleView() {
        return isSimpleView;
    }

    public void setIsEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    public boolean isEnable() {
        return isEnable;
    }

    static String getBeanJsonFilePath(Context context) {
        return context.getExternalFilesDir(TAG) + "/" + TAG + ".json";
    }

    static String getBeanListJsonFilePath(Context context) {
        return context.getExternalFilesDir(TAG) + "/" + TAG + "_List.json";
    }

    static void writeBean(JsonWriter writer, TTSPlayRuleBean_V1 bean) throws IOException {
        // 开始 JSON 对象
        writer.beginObject();
        // 写入键值对
        writer.name("userId").value(bean.userId);
        writer.name("ruleName").value(bean.ruleName);
        writer.name("demoSMSText").value(bean.demoSMSText);
        writer.name("patternText").value(bean.patternText);
        writer.name("ttdRuleText").value(bean.ttsRuleText);
        writer.name("isSimpleView").value(bean.isSimpleView);
        writer.name("isEnable").value(bean.isEnable);
        // 结束 JSON 对象
        writer.endObject();
    }

    static TTSPlayRuleBean_V1 parseBean(JsonReader jsonReader) {
        try {
            TTSPlayRuleBean_V1 bean = new TTSPlayRuleBean_V1();
            // 开始 JSON 对象
            jsonReader.beginObject();
            // 写入键值对
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                if (name.equals("ruleName")) {
                    bean.setRuleName(jsonReader.nextString());
                } else if (name.equals("userId")) {
                    bean.setUserId(jsonReader.nextInt());
                } else if (name.equals("demoSMSText")) {
                    bean.setDemoSMSText(jsonReader.nextString());
                } else if (name.equals("patternText")) {
                    bean.setPatternText(jsonReader.nextString());
                } else if (name.equals("ttdRuleText")) {
                    bean.setTtsRuleText(jsonReader.nextString());
                } else if (name.equals("isSimpleView")) {
                    bean.setIsSimpleView(jsonReader.nextBoolean());
                } else if (name.equals("isEnable")) {
                    bean.setIsEnable(jsonReader.nextBoolean());
                } else {
                    jsonReader.skipValue();
                }
            }
            // 结束 JSON 对象
            jsonReader.endObject();
            return bean;
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        return null;
    }

    static ArrayList<TTSPlayRuleBean_V1> parseBeanList(String beanList) {
        try {
            StringReader stringReader = new StringReader(beanList);
            JsonReader jsonReader = new JsonReader(stringReader);
            ArrayList<TTSPlayRuleBean_V1> list = new ArrayList<TTSPlayRuleBean_V1>();
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                TTSPlayRuleBean_V1 bean = parseBean(jsonReader);
                if (bean != null) {
                    list.add(bean);
                }
            }
            jsonReader.endArray();
            return list;
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        return null;
    }

    @Override
    public String toString() {
        // 创建 JsonWriter 对象
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.setIndent("  ");
        try {
            writeBean(jsonWriter, this);
            return stringWriter.toString();
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        // 获取 JSON 字符串
        return "";
    }

    public static String toStringByBeanList(ArrayList<TTSPlayRuleBean_V1> beanList) {
        try {
            StringWriter stringWriter = new StringWriter();
            JsonWriter writer = new JsonWriter(stringWriter);
            writer.setIndent("  ");
            writer.beginArray();
            for (TTSPlayRuleBean_V1 bean : beanList) {
                writeBean(writer, bean);
            }
            writer.endArray();
            writer.close();
            return stringWriter.toString();
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        return "";
    }

    public static TTSPlayRuleBean_V1 parseBean(String szBean) {
        // 创建 JsonWriter 对象
        StringReader stringReader = new StringReader(szBean);
        JsonReader jsonReader = new JsonReader(stringReader);
        return parseBean(jsonReader);
    }

    /*public static TTSPlayRuleBean_V1 loadBean(Context context) {
        return loadBeanFromFile(getBeanJsonFilePath(context));
    }

    public static TTSPlayRuleBean_V1 loadBeanFromFile(String szFilePath) {
        TTSPlayRuleBean_V1 bean = null;
        try {
            String szJson = FileUtil.readFile(szFilePath);
            bean = TTSPlayRuleBean_V1.parseBean(szJson);
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        return bean;
    }

    public static void saveBean(Context context, TTSPlayRuleBean_V1 bean) {
        saveBeanToFile(getBeanJsonFilePath(context), bean);
    }

    public static void saveBeanToFile(String szFilePath, TTSPlayRuleBean_V1 bean) {
        try {
            String szJson = bean.toString();
            FileUtil.writeFile(szFilePath, szJson);
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
    }

    public static ArrayList<TTSPlayRuleBean_V1> loadBeanList(Context context) {
        return loadBeanListFromFile(getBeanListJsonFilePath(context));
    }*/

    public static ArrayList<TTSPlayRuleBean_V1> loadBeanListFromFile(String szFilePath) {
        ArrayList<TTSPlayRuleBean_V1> beanList = null;
        try {
            String szListJson = FileUtil.readFile(szFilePath);
            beanList = TTSPlayRuleBean_V1.parseBeanList(szListJson);
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        return beanList;
    }

    /*public static boolean saveBeanList(Context context, ArrayList<TTSPlayRuleBean_V1> beanList) {
        return saveBeanListToFile(getBeanListJsonFilePath(context), beanList);
    }

    public static boolean saveBeanListToFile(String szFilePath, ArrayList<TTSPlayRuleBean_V1> beanList) {
        try {
            String szJson = TTSPlayRuleBean_V1.toStringByBeanList(beanList);
            FileUtil.writeFile(szFilePath, szJson);
            return true;
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        return false;
    }*/
}
