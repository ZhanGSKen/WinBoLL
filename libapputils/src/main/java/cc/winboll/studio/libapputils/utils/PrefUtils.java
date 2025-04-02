package cc.winboll.studio.libapputils.utils;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/11/28 15:03:12
 * @Describe 应用变量保存工具
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;

public class PrefUtils {
    
    public static final String TAG = "PrefUtils";
    
    //
    // 保存字符串到SharedPreferences的函数
    //
    public static void saveString(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
    
    //
    // 从SharedPreferences读取字符串的函数
    //
    public static String getString(Context context, String key, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }
}
