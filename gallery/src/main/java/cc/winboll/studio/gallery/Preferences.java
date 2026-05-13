package cc.winboll.studio.gallery;

import android.content.Context;
import android.content.SharedPreferences;
import cc.winboll.studio.libappbase.LogUtils;

public class Preferences {
    public static final String TAG = "Preferences";
    private static final String PREF_NAME = "gallery_prefs";
    private static final String KEY_FOLDER_PATH = "folder_path";
    private static final String KEY_BG_TYPE = "bg_type";
    private static final String KEY_ALBUM_SORT_MODE = "album_sort_mode";
    private static final String KEY_COVER_WIDTH = "cover_width";
    private static final String KEY_COVER_HEIGHT = "cover_height";
    private static final String KEY_COVER_RATIO = "cover_ratio";

    public static final String ACTION_COVER_UPDATED = "cc.winboll.studio.gallery.COVER_UPDATED";

    private static final int DEFAULT_BG_TYPE = 0;
    private static final int DEFAULT_SORT_MODE = 0;
    private static final int DEFAULT_COVER_WIDTH = 240;
    private static final int DEFAULT_COVER_HEIGHT = 120;
    private static final float DEFAULT_COVER_RATIO = 2.0f;
    private static final String DEFAULT_PATH = "/storage/emulated/0/Pictures/Gallery/owner";
    
    public static final int SORT_TIME_DESC = 0;
    public static final int SORT_TIME_ASC = 1;
    public static final int SORT_NAME_DESC = 2;
    public static final int SORT_NAME_ASC = 3;
    
    public static String getDefaultPath() {
        return DEFAULT_PATH;
    }
    
    private final SharedPreferences prefs;
    
    public Preferences(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public String getFolderPath() {
        String path = prefs.getString(KEY_FOLDER_PATH, DEFAULT_PATH);
        LogUtils.d(TAG, "getFolderPath: " + path);
        return path;
    }
    
    public void setFolderPath(String path) {
        LogUtils.d(TAG, "setFolderPath: " + path);
        prefs.edit().putString(KEY_FOLDER_PATH, path).apply();
    }
    
    public int getBgType() {
        return prefs.getInt(KEY_BG_TYPE, DEFAULT_BG_TYPE);
    }
    
    public void setBgType(int type) {
        LogUtils.d(TAG, "setBgType: " + type);
        prefs.edit().putInt(KEY_BG_TYPE, type).apply();
    }
    
    public int getAlbumSortMode() {
        return prefs.getInt(KEY_ALBUM_SORT_MODE, DEFAULT_SORT_MODE);
    }
    
    public void setAlbumSortMode(int mode) {
        LogUtils.d(TAG, "setAlbumSortMode: " + mode);
        prefs.edit().putInt(KEY_ALBUM_SORT_MODE, mode).apply();
    }
    
    public int getCoverWidth() {
        return prefs.getInt(KEY_COVER_WIDTH, DEFAULT_COVER_WIDTH);
    }
    
    public void setCoverWidth(int width) {
        prefs.edit().putInt(KEY_COVER_WIDTH, width).apply();
    }
    
    public int getCoverHeight() {
        return prefs.getInt(KEY_COVER_HEIGHT, DEFAULT_COVER_HEIGHT);
    }
    
    public void setCoverHeight(int height) {
        prefs.edit().putInt(KEY_COVER_HEIGHT, height).apply();
    }
    
    public float getCoverRatio() {
        return prefs.getFloat(KEY_COVER_RATIO, DEFAULT_COVER_RATIO);
    }
    
    public void setCoverRatio(float ratio) {
        prefs.edit().putFloat(KEY_COVER_RATIO, ratio).apply();
    }
}