package cc.winboll.studio.libaes.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @Author ZhanGSKen&豆包大模型<zhangsken@qq.com>
 * @Date 2025/11/13 06:50
 * @Describe 应用变量保存工具
 */

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
