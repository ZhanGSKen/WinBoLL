package cc.winboll.studio.timestamp.models;

/**
 * @Author ZhanGSKen
 * @Date 2025/05/05 09:51
 * @Describe 应用配置数据模型
 */
import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.timestamp.models.AppConfigs;
import cc.winboll.studio.timestamp.utils.FileUtil;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;

public class AppConfigs implements Serializable {

    public static final String TAG = "AppConfigs";

    volatile static AppConfigs _AppConfigs;
    Context mContext;

    // 是否启动服务
    boolean isEnableService;

    AppConfigs(Context context) {
        this.mContext = context;
        this.isEnableService = false;
    }

    public synchronized static AppConfigs getInstance(Context context) {
        if (_AppConfigs == null) {
            _AppConfigs = new AppConfigs(context);
            _AppConfigs.loadAppConfigs();
        }
        return _AppConfigs;
    }

    public void setIsEnableService(boolean isEnableService) {
        this.isEnableService = isEnableService;
    }

    public boolean isEnableService() {
        return isEnableService;
    }

    @Override
    public String toString() {
        // 创建 JsonWriter 对象
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new
            JsonWriter(stringWriter);
        try {
            // 开始 JSON 对象
            writer.beginObject();

            // 写入键值对
            writer.name("isEnableService").value(this.isEnableService);

            // 结束 JSON 对象
            writer.endObject();
            return stringWriter.toString();
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        // 获取 JSON 字符串
        return "";
    }

    public AppConfigs parseAppConfigs(String szAppConfigs) {
        // 创建 JsonWriter 对象
        StringReader stringReader = new StringReader(szAppConfigs);
        JsonReader jsonReader = new
            JsonReader(stringReader);
        try {
            // 开始 JSON 对象
            jsonReader.beginObject();

            // 写入键值对
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                if (name.equals("isEnableService")) {
                    setIsEnableService(jsonReader.nextBoolean());
                } else {
                    jsonReader.skipValue();
                }
            }
            // 结束 JSON 对象
            jsonReader.endObject();
            return this;
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        // 获取 JSON 字符串
        return null;
    }

    static String getDataPath(Context context) {
        return context.getExternalFilesDir(TAG) + "/" + TAG + ".json";
    }

    public AppConfigs loadAppConfigs() {
        AppConfigs appConfigs = null;
        try {
            String szJson = FileUtil.readFile(getDataPath(mContext));
            appConfigs = parseAppConfigs(szJson);

            if (appConfigs != null) {
                _AppConfigs = appConfigs;
            }
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        return _AppConfigs;
    }

    public void saveAppConfigs(AppConfigs appConfigs) {
        try {
            String szJson = appConfigs.toString();
            FileUtil.writeFile(getDataPath(mContext), szJson);
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
    }
}
