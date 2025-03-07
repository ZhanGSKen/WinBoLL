package cc.winboll.studio.libaes.beans;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/06/14 02:42:57
 * @Describe 主题元素项目类
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libaes.R;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;

public class AESThemeBean extends BaseBean {

    public static final String TAG = "AESThemeBean";

    public enum ThemeType {
        AES("默认主题"),
        DEPTH("深奥主题"),
        SKY("天空主题"),
        GOLDEN("辉煌主题"),
        MEMOR("梦箩主题"),
        TAO("黑白主题");

        private String name;

        // 枚举构造函数
        ThemeType(String name) {
            this.name = name;
        }

        // 将字符串转换为枚举
        public static ThemeType fromString(String themeTypeStr) {
            return ThemeType.valueOf(themeTypeStr.toUpperCase()); // 注意这里用了toUpperCase()，确保匹配时不区分大小写
        }

        // 获取枚举的名称
        public String getName() {
            return name;
        }
    }
    
    // 保存当前主题
    int currentThemeStyleID = getThemeStyleID(ThemeType.AES);
    
    public AESThemeBean() {
    }
    
    public AESThemeBean(int currentThemeStyleID) {
        this.currentThemeStyleID = currentThemeStyleID;
    }

    public void setCurrentThemeTypeID(int currentThemeTypeID) {
        this.currentThemeStyleID = currentThemeTypeID;
    }

    public int getCurrentThemeTypeID() {
        return this.currentThemeStyleID;
    }
    
    @Override
    public String getName() {
        return AESThemeBean.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        AESThemeBean bean = this;
        jsonWriter.name("currentThemeTypeID").value(bean.getCurrentThemeTypeID());
    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if(super.initObjectsFromJsonReader(jsonReader, name)) { return true; }
        else{
            if (name.equals("currentThemeTypeID")) {
                setCurrentThemeTypeID(jsonReader.nextInt());
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public BaseBean readBeanFromJsonReader(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if(!initObjectsFromJsonReader(jsonReader, name)) {
                jsonReader.skipValue();
            }
        }
        // 结束 JSON 对象
        jsonReader.endObject();
        return this;
    }

    public static int getThemeStyleID(ThemeType themeType) {
        int themeStyleID = R.style.AESTheme;
        if (AESThemeBean.ThemeType.DEPTH == themeType) {
            themeStyleID = R.style.DepthAESTheme;
        } else if (AESThemeBean.ThemeType.SKY == themeType) {
            themeStyleID = R.style.SkyAESTheme;
        } else if (AESThemeBean.ThemeType.GOLDEN == themeType) {
            themeStyleID = R.style.GoldenAESTheme;
        } else if (AESThemeBean.ThemeType.MEMOR == themeType) {
            themeStyleID = R.style.MemorAESTheme;
        } else if (AESThemeBean.ThemeType.TAO == themeType) {
            themeStyleID = R.style.TaoAESTheme;
        } else if (AESThemeBean.ThemeType.AES == themeType) {
            themeStyleID = R.style.AESTheme;
        }
        //LogUtils.d(TAG, "themeStyleID " + Integer.toString(themeStyleID));
        return themeStyleID;
    }

    public static AESThemeBean.ThemeType getThemeStyleType(int nThemeStyleID) {
        AESThemeBean.ThemeType themeStyle = AESThemeBean.ThemeType.AES;
        if (R.style.DepthAESTheme == nThemeStyleID) {
            themeStyle = AESThemeBean.ThemeType.DEPTH ;
        } else if (R.style.SkyAESTheme == nThemeStyleID) {
            themeStyle = AESThemeBean.ThemeType.SKY ;
        } else if (R.style.GoldenAESTheme == nThemeStyleID) {
            themeStyle = AESThemeBean.ThemeType.GOLDEN ;
        } else if (R.style.MemorAESTheme == nThemeStyleID) {
            themeStyle = AESThemeBean.ThemeType.MEMOR ;
        } else if (R.style.TaoAESTheme == nThemeStyleID) {
            themeStyle = AESThemeBean.ThemeType.TAO ;
        } else if (R.style.AESTheme == nThemeStyleID) {
            themeStyle = AESThemeBean.ThemeType.AES;
        }
        //LogUtils.d(TAG, "themeStyle " + Integer.toString(themeStyle.ordinal()));
        return themeStyle;
    }
}
