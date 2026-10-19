package cc.winboll.studio.gallery.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

public class CropBackgroundUtils {

    public enum DrawableType {
        RESOURCE_ID,
        COLOR
    }

    private static volatile CropBackgroundUtils instance;

    private static final String PREF_NAME = "crop_background_prefs";
    private static final String KEY_TYPE = "crop_bg_type";
    private static final String KEY_RES_ID = "crop_bg_res_id";
    private static final String KEY_COLOR = "crop_bg_color";

    private Context context;
    private Drawable drawable;
    private DrawableType drawableType;
    private int resId;
    private int color;

    private CropBackgroundUtils() {
    }

    public static CropBackgroundUtils getInstance() {
        if (instance == null) {
            synchronized (CropBackgroundUtils.class) {
                if (instance == null) {
                    instance = new CropBackgroundUtils();
                }
            }
        }
        return instance;
    }

    public static CropBackgroundUtils initFromResource(Context context, @DrawableRes int resId) {
        synchronized (CropBackgroundUtils.class) {
            CropBackgroundUtils utils = getInstance();
            utils.context = context.getApplicationContext();
            utils.drawableType = DrawableType.RESOURCE_ID;
            utils.resId = resId;
            utils.drawable = ContextCompat.getDrawable(utils.context, resId);
            utils.saveToPreferences();
            return utils;
        }
    }

    public static CropBackgroundUtils initFromColor(Context context, @ColorInt int color) {
        synchronized (CropBackgroundUtils.class) {
            CropBackgroundUtils utils = getInstance();
            utils.context = context.getApplicationContext();
            utils.drawableType = DrawableType.COLOR;
            utils.color = color;
            utils.drawable = new ColorDrawable(color);
            utils.saveToPreferences();
            return utils;
        }
    }

    public static CropBackgroundUtils initFromPreferences(Context context) {
        synchronized (CropBackgroundUtils.class) {
            Context appContext = context.getApplicationContext();
            SharedPreferences prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            int type = prefs.getInt(KEY_TYPE, -1);
            if (type == 0) {
                int resId = prefs.getInt(KEY_RES_ID, 0);
                if (resId != 0) {
                    return initFromResource(appContext, resId);
                }
            } else if (type == 1) {
                int color = prefs.getInt(KEY_COLOR, Color.BLACK);
                return initFromColor(appContext, color);
            }
            return initFromColor(appContext, 0xFF00FF00);
        }
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public DrawableType getDrawableType() {
        return drawableType;
    }

    public int getResId() {
        return resId;
    }

    public int getColor() {
        return color;
    }

    public void saveToPreferences() {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (drawableType == DrawableType.RESOURCE_ID) {
            editor.putInt(KEY_TYPE, 0);
            editor.putInt(KEY_RES_ID, resId);
            editor.remove(KEY_COLOR);
        } else {
            editor.putInt(KEY_TYPE, 1);
            editor.putInt(KEY_COLOR, color);
            editor.remove(KEY_RES_ID);
        }
        editor.apply();
    }

    public static void clearPreferences(Context context) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
