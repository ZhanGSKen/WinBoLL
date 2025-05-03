package cc.winboll.studio.autoinstaller.models;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/05/27 17:36:01
 * @Describe 应用配置数据类
 */
import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.autoinstaller.models.AppConfigs;
import cc.winboll.studio.autoinstaller.utils.FileUtil;
import cc.winboll.studio.libappbase.LogUtils;
import com.hjq.toast.ToastUtils;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;

public class AppConfigs implements Serializable {

    public static final String TAG = "AppConfigs";

    // 安装模式
    public static enum SetupMode {
        WATCHOUTPUTINSTALLER, // 本应用直接调用安装
        NEWAPKINFONEWAPKINFO  // 调用[应用信息查看器]打开应用包
        };

    static volatile AppConfigs _AppConfigs;
    Context mContext;

    AppConfigs(Context context) {
        mContext = context.getApplicationContext();
    }

    public static synchronized AppConfigs getInstance(Context context) {
        if (_AppConfigs == null) {
            _AppConfigs = new AppConfigs(context);
            _AppConfigs.loadAppConfigs(_AppConfigs.mContext);
        }
        return _AppConfigs;
    }

    // 监控文件路径
    private String watchingFilePath = "";

    // 是否启动服务
    private boolean isEnableService = false;

    // 安装工具模式
    private SetupMode setupMode = SetupMode.WATCHOUTPUTINSTALLER;


    public void setWatchingFilePath(String watchingFilePath) {
        this.watchingFilePath = watchingFilePath;
    }

    public String getWatchingFilePath() {
        return watchingFilePath;
    }

    public void setIsEnableService(boolean isEnableService) {
        this.isEnableService = isEnableService;
    }

    public boolean isEnableService() {
        return isEnableService;
    }

    public void setSetupMode(SetupMode setupMode) {
        this.setupMode = setupMode;
    }

    public SetupMode getSetupMode() {
        return setupMode;
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
            writer.name("watchingFilePath").value(this.watchingFilePath);
            writer.name("isEnableService").value(this.isEnableService);
            writer.name("setupMode").value(this.setupMode.ordinal());

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
        AppConfigs appConfigs = new AppConfigs(mContext);
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
                if (name.equals("watchingFilePath")) {
                    appConfigs.setWatchingFilePath(jsonReader.nextString());
                } else if (name.equals("isEnableService")) {
                    appConfigs.setIsEnableService(jsonReader.nextBoolean());
                } else if (name.equals("setupMode")) {
                    appConfigs.setSetupMode(SetupMode.values()[jsonReader.nextInt()]);
                } else {
                    jsonReader.skipValue();
                }
            }
            // 结束 JSON 对象
            jsonReader.endObject();
            return appConfigs;
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
            appConfigs = AppConfigs.getInstance(mContext).parseAppConfigs(szJson);
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        return appConfigs;
    }

    public AppConfigs loadAppConfigs(Context context) {
        AppConfigs appConfigs = null;
        try {
            String szJson = FileUtil.readFile(getDataPath(context.getApplicationContext()));
            appConfigs = AppConfigs.getInstance(mContext).parseAppConfigs(szJson);
            if(appConfigs != null) {
                _AppConfigs = appConfigs;
            }
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
        return _AppConfigs;
    }

    public void saveAppConfigs(Context context, AppConfigs appConfigs) {
        try {
            ToastUtils.show(String.format("AppConfigs set enable service to %s", appConfigs.isEnableService()));
            //LogUtils.d(TAG, "appConfigs is : " + appConfigs.toString());
            String szJson = appConfigs.toString();
            FileUtil.writeFile(getDataPath(context), szJson);
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
    }

    public void saveAppConfigs() {
        saveAppConfigs(mContext, this);
    }
}
