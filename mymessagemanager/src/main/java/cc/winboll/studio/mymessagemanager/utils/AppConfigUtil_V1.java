package cc.winboll.studio.mymessagemanager.utils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/19 14:30:57
 * @Describe 应用配置工具类，V1 旧版。
 */
import android.util.JsonReader;
import cc.winboll.studio.mymessagemanager.beans.AppConfigBean_V1;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class AppConfigUtil_V1 {

    public final static String TAG = "ConfigUtil";

    static AppConfigUtil_V1 _mConfigUtil;
    //AppConfigBean_V1 mAppConfigBean_V1;

    AppConfigUtil_V1() {}

    public static AppConfigUtil_V1 getInstance() {
        if (_mConfigUtil == null) {
            _mConfigUtil = new AppConfigUtil_V1();
        }
        return _mConfigUtil;
    }

    /*public void setAppTheme(ThemeUtil.BaseTheme baseTheme) {
        loadConfigData();
        mAppConfigBean.setAppThemeID(ThemeUtil.getThemeID(baseTheme));
        saveConfigData();
    }*/

    //
    // 加载应用配置数据
    //
    /*void loadConfigData() {
        File fJson = new File(GlobalApplication._mszConfigUtilPath);
        ArrayList<AppConfigBean> listTemp = null;
        try {
            if (fJson.exists()) {
                listTemp = readJsonStream(new FileInputStream(fJson));
                if (listTemp != null) {
                    mAppConfigBean = listTemp.get(0);
                }
            } else {
                mAppConfigBean = new AppConfigBean();
                saveConfigData();
            }
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage(), Thread.currentThread().getStackTrace());
        }
    }*/

    //
    // 读取 Json 文件
    //
    public ArrayList<AppConfigBean_V1> readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        return readJsonArrayList(reader);
    }

    //
    // 读取 Json 文件的每一 Json 项
    //
    public ArrayList<AppConfigBean_V1> readJsonArrayList(JsonReader reader) throws IOException {
        ArrayList<AppConfigBean_V1> list = new ArrayList<AppConfigBean_V1>();
        reader.beginArray();
        while (reader.hasNext()) {
            list.add(readBeanItem(reader));
        }
        reader.endArray();
        return list;
    }

    //
    // 读取 Json 文件的某一 Json 项
    //
    public AppConfigBean_V1 readBeanItem(JsonReader reader) throws IOException {
        AppConfigBean_V1 bean = new AppConfigBean_V1();
        int nReaderCount = 0;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("countryCode")) {
                bean.setCountryCode(reader.nextString());
                nReaderCount++;
            } else if (name.equals("isMergeCountryCodePrefix")) {
                bean.setIsMergeCountryCodePrefix(reader.nextBoolean());
                nReaderCount++;
            } else if (name.equals("ttsPlayDelayTimes")) {
                bean.setTtsPlayDelayTimes(reader.nextInt());
                nReaderCount++;
            } else if (name.equals("enableService")) {
                bean.setEnableService(reader.nextBoolean());
                nReaderCount++;
            } else if (name.equals("enableOnlyReceiveContacts")) {
                bean.setEnableOnlyReceiveContacts(reader.nextBoolean());
                nReaderCount++;
            } else if (name.equals("enableTTS")) {
                bean.setEnableTTS(reader.nextBoolean());
                nReaderCount++;
            } else if (name.equals("enableTTSRuleMode")) {
                bean.setEnableTTSRuleMode(reader.nextBoolean());
                nReaderCount++;
            } /*else if (name.equals("appThemeID")) {
                bean.setAppThemeID(reader.nextInt());
                nReaderCount++;
            }*/ else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return nReaderCount > 0 ? bean : null;
    }

    //
    // 写入 Json 文件的某一 Json 项
    //
    /*public void writeBeanItem(JsonWriter writer, AppConfigBean bean) throws IOException {
        writer.beginObject();
        writer.name("countryCode").value(bean.getCountryCode());
        writer.name("isMergeCountryCodePrefix").value(bean.isMergeCountryCodePrefix());
        writer.name("ttsPlayDelayTimes").value(bean.getTtsPlayDelayTimes());
        writer.name("enableService").value(bean.isEnableService());
        writer.name("enableOnlyReceiveContacts").value(bean.isEnableOnlyReceiveContacts());
        writer.name("enableTTS").value(bean.isEnableTTS());
        writer.name("enableTTSRuleMode").value(bean.isEnableTTSRuleMode());
        writer.name("appThemeID").value(bean.getAppThemeID());
        writer.endObject();
    }

    //
    // 保存应用配置数据
    //
    public void saveConfigData() {
        try {
            File fJson = new File(GlobalApplication._mszConfigUtilPath);
            ArrayList<AppConfigBean> list = new ArrayList<AppConfigBean>();
            list.add(mAppConfigBean);
            writeJsonStream(new FileOutputStream(fJson, false), list);
        } catch (IOException e) {
            LogUtils.d(TAG, "IOException : " + e.getMessage());
        }
    }

    //
    // 写入 Json 文件
    //
    public void writeJsonStream(OutputStream out, ArrayList<AppConfigBean> beanList) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
        writer.setIndent("  ");
        writeJsonArrayList(writer, beanList);
        writer.close();
    }

    //
    // 记录 Json 文件的某一 Json 项
    //
    public void writeJsonArrayList(JsonWriter writer, ArrayList<AppConfigBean> beanList) throws IOException {
        writer.beginArray();
        for (AppConfigBean bean : beanList) {
            writeBeanItem(writer, bean);
        }
        writer.endArray();
    }*/
}
