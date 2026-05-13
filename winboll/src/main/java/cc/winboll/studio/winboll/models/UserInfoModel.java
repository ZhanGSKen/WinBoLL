package cc.winboll.studio.winboll.models;

/**
 * @Author ZhanGSKen<zhangsken@qq.com>
 * @Date 2025/06/04 19:14
 */
import android.util.JsonReader;
import android.util.JsonWriter;
import cc.winboll.studio.libappbase.BaseBean;
import java.io.IOException;

public class UserInfoModel extends BaseBean {

    public static final String TAG = "UserInfoModel";

    String username;
    String password;
    String token;

    public UserInfoModel() {
        this.username = "";
        this.password = "";
        this.token = "";
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String getName() {
        return UserInfoModel.class.getName();
    }

    @Override
    public void writeThisToJsonWriter(JsonWriter jsonWriter) throws IOException {
        super.writeThisToJsonWriter(jsonWriter);
        jsonWriter.name("username").value(getUsername());
        jsonWriter.name("password").value(getPassword());
        jsonWriter.name("token").value(getToken());
    }

    @Override
    public boolean initObjectsFromJsonReader(JsonReader jsonReader, String name) throws IOException {
        if (super.initObjectsFromJsonReader(jsonReader, name)) { return true; } else {
            if (name.equals("username")) {
                setUsername(jsonReader.nextString());
            } else if (name.equals("password")) {
                setPassword(jsonReader.nextString());
            } else if (name.equals("token")) {
                setToken(jsonReader.nextString());
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
            if (!initObjectsFromJsonReader(jsonReader, name)) {
                jsonReader.skipValue();
            }
        }
        // 结束 JSON 对象
        jsonReader.endObject();
        return this;
    }
}
